/**
 * Copyright 2010 SAVVIS Communications. All rights reserved.
 */
package com.savvis.it.vantive.er.servlet;


import static com.savvis.it.util.ObjectUtil.toCommaDelimitedString;
import static com.savvis.it.util.StringUtil.hasValue;
import static com.savvis.it.util.StringUtil.replaceAll;
import static com.savvis.it.util.StringUtil.toList;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.savvis.it.biz.db.BizDBUtil;
import com.savvis.it.biz.db.data.BillingCycle;
import com.savvis.it.biz.db.data.BillingType;
import com.savvis.it.db.DBConnection;
import com.savvis.it.db.DBUtil;
import com.savvis.it.db.util.BeanUtil;
import com.savvis.it.servlet.SavvisServlet;
import com.savvis.it.util.CommandLineProcess;
import com.savvis.it.util.Context;
import com.savvis.it.util.DateUtil;
import com.savvis.it.util.ObjectUtil;
import com.savvis.it.util.PropertyManager;
import com.savvis.it.util.StringUtil;
import com.savvis.it.util.SystemUtil;
import com.savvis.it.vantive.db.VantiveDBUtil;
import com.savvis.it.vantive.db.data.AcctInstProduct;
import com.savvis.it.vantive.externalrating.batch.VantiveExternalRating;

/**
 * This class 
 *
 * @author theodore.elrick
 * @version $Id$
 */
public class RatingReportIndexServlet extends SavvisServlet {
	
	private static Logger logger = Logger.getLogger(RatingReportIndexServlet.class);
	
	private static PropertyManager properties = new PropertyManager("/properties/externalRating.properties", 
				"externalRating.properties");

	@Override
	protected void processRequest(String action, HttpServletRequest request, HttpServletResponse response)
				throws Exception {
		logger.info("action = "+(action));
		
		DBConnection conn = VantiveDBUtil.currentVntConnection();
		DBConnection bizConn = BizDBUtil.currentConnection("biz");
		try {
			List savvisCompanies = DBUtil.executeProcedureOneColumn("vantive", "svsp_site_get_savvis_company");
			savvisCompanies.add(0, "-- All Savvis Companies --");
			request.setAttribute("savvisCompanies", savvisCompanies);
			request.setAttribute("monthValues", Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}));
			request.setAttribute("monthLabels", toList("January, February, March, April, May, June, July, August, " +
			"September, October, November, December"));
			request.setAttribute("defaultYear", Calendar.getInstance().get(Calendar.YEAR));
			request.setAttribute("defaultMonth", Calendar.getInstance().get(Calendar.MONTH));
			request.setAttribute("billingCycles", toList("July 13, 2010 9:46 AM (Start Date: May 01, 2010)", "|"));
			
			getBillingCycles(bizConn, request);

			if(!hasValue(action)) {
				forward("/jsp/reportIndex.jsp", request, response);
			} else if("runReport".equals(action)) {
				performDBOperations(conn, null, action, request, response);
				
				getBillingCycles(bizConn, request);

				forward("/jsp/reportIndex.jsp", request, response);
			} else if("viewReport".equals(action) || "export".equals(action)) {
				String report = request.getParameter("report");
				request.setAttribute("report", report);
				
				BillingCycle billingCycle = (BillingCycle)DBUtil.findById(bizConn, BillingCycle.class, 
							request.getParameter("cycle"));
				billingCycle.getBillingType().getName();
				billingCycle.getBurstableInternetChargeList().size();
				request.setAttribute("billingCycle", billingCycle);
				
				String reportType = VantiveExternalRating.CHARGES_REPORT_TYPE;
				if(report.equals("Usage Exceptions"))
					reportType = VantiveExternalRating.EXCEPTIONS_REPORT_TYPE;
				
				if("export".equals(action)) {
//					request.setAttribute("export", true);
					response.setContentType("application/vnd.ms-excel");
					SimpleDateFormat sdf = new SimpleDateFormat("MMMM");
					response.setHeader("Content-Disposition", "attachment; filename=\""+report+" "+
								sdf.format(billingCycle.getStartDate())+" "+
								billingCycle.getBillingCycleId()+".xls\"");
					HSSFWorkbook wb = new VantiveExternalRating().createBillingCycleWorkbook(conn, billingCycle, reportType);
					wb.write(response.getOutputStream());
					return;
				} else {
					List<Map<String,Object>> reportData = new VantiveExternalRating().createBillingCycleReport(
								conn, billingCycle, reportType);
					request.setAttribute("reportData", reportData);
					
					forward("/jsp/ratingReport.jsp", request, response);
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			VantiveDBUtil.closeConnection(conn);
			DBUtil.closeConnection(bizConn);
		}
	}
	
	/**
	 * @param bizConn
	 * @param request
	 * @throws Exception 
	 */
	private void getBillingCycles(DBConnection conn, HttpServletRequest request) throws Exception {
		List<BillingCycle> list = DBUtil.executeHqlQuery(conn, 
					"FROM BillingCycle WHERE billingType.name = 'External Rating' " +
					"ORDER BY dateCreated DESC");
		List<String> billingCycleDescriptions = new ArrayList<String>();
		List<Object> billingCycleIds = new ArrayList<Object>();
		for (BillingCycle billingCycle : list) {
			String s = getBillingCycleDescription(billingCycle);
			billingCycleDescriptions.add(s);
			billingCycleIds.add(billingCycle.getId());
		}
		request.setAttribute("billingCycleDescriptions", billingCycleDescriptions);
		request.setAttribute("billingCycleIds", billingCycleIds);
	}

	/**
	 * @param sdf
	 * @param sdf2
	 * @param billingCycle
	 * @return
	 */
	private String getBillingCycleDescription(BillingCycle billingCycle) {
		SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy h:mm aa");
		SimpleDateFormat sdf2 = new SimpleDateFormat("MMMM d, yyyy");
		return sdf.format(billingCycle.getDateCreated())+" (Start Date: "+
					sdf2.format(billingCycle.getStartDate())+")";
	}

	@Override
	protected void performProtectedDBOperations(DBConnection conn, Object data, String action,
				HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		if("runReport".equals(action)) {
//			properties.reload();

			List<Map<String, Object>> commands = toList("");
			Context globalContext = new Context();
			
			Calendar cal = Calendar.getInstance();
			cal.set(getIntegerParameter(request, "year"), getIntegerParameter(request, "month"), 1);
			
			String beginDate = DateUtil.formatDate(cal.getTime());
			
			cal.add(Calendar.MONTH, 1);
			cal.add(Calendar.DAY_OF_YEAR, -1);
			String endDate = DateUtil.formatDate(cal.getTime());
			
			CommandLineProcess clp = new CommandLineProcess();
			clp.setWaitForProcess(true);
			
			String cmd = properties.getProperty("batch.job.command")+" -beginDate "+beginDate+" -endDate "+endDate;
			cmd = replaceAll(cmd, "[[classpath]]", properties.getProperty("java.class.path", null));
			logger.info("properties.getProperty(\"start.dir\") = "+(properties.getProperty("start.dir")));
			clp.setDir(new File((String) properties.getProperty("start.dir")));
			logger.info("properties.getProperty(\"log.file\") = "+(properties.getProperty("log.file", null)));
			if(properties.getProperty("log.file", null) != null)
				clp.setOutputStream(new FileOutputStream(properties.getProperty("log.file", null), true));
			else
				clp.setOutputStream(System.out);
			
			Context envContext = new Context();
			envContext.fillWithEnvAndSystemProperties();
			List<String> envList = new ArrayList<String>();
			Map<String, String> envMap = System.getenv();
			for (Object key : envMap.keySet())
				envList.add(key + "=" + envMap.get(key));
			envList.add("CALLED_BY_USER=TEST");// + winPrincipal.getName());
			envList.add("APPL="+SystemUtil.getAPPL());
			envList.add("BASEDIR="+SystemUtil.getBASEDIR());
			clp.setEnvp((String[]) envList.toArray(new String[] {}));
			
			logger.info("cmd: " + cmd);
			int exitCode = clp.run(cmd);

			logger.info("output: " + clp.getOutput());
			if (exitCode != 0) {
				logger.info("error: " + clp.getError());
				
				DBUtil.rollback(conn);
				request.setAttribute("fatalMsg", "The process encountered a technical exception");
			} else {
				DBUtil.commit(conn);
				request.setAttribute("message", "The process completed successfully");
			}
		}
	}

}
