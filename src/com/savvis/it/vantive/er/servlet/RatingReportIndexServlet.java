/**
 * Copyright 2010 SAVVIS Communications. All rights reserved.
 */
package com.savvis.it.vantive.er.servlet;


import static com.savvis.it.util.StringUtil.replaceAll;
import static com.savvis.it.util.StringUtil.toList;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.savvis.it.biz.db.BizDBUtil;
import com.savvis.it.biz.db.data.BillingCycle;
import com.savvis.it.db.DBConnection;
import com.savvis.it.db.DBUtil;
import com.savvis.it.filter.WindowsAuthenticationFilter;
import com.savvis.it.servlet.SavvisServlet;
import com.savvis.it.tags.SelectList;
import com.savvis.it.util.CommandLineProcess;
import com.savvis.it.util.Context;
import com.savvis.it.util.ObjectUtil;
import com.savvis.it.util.PropertyManager;
import com.savvis.it.util.SimpleNode;
import com.savvis.it.util.StringUtil;
import com.savvis.it.util.SystemUtil;
import com.savvis.it.util.XmlUtil;
import com.savvis.it.vantive.db.VantiveDBUtil;
import com.savvis.it.vantive.externalrating.batch.VantiveExternalRating;

/**
 * This class 
 *
 * @author theodore.elrick
 * @version $Id$
 */
public class RatingReportIndexServlet extends SavvisServlet {
	
	private static Logger logger = Logger.getLogger(RatingReportIndexServlet.class);
	
	private static PropertyManager properties = new PropertyManager("/properties/external_rating_web.properties", "external_rating_web.properties");
	private static String productCfg = properties.getProperty("extRating.config");
	
	private static String jspPageReport = "/jsp/ratingReport.jsp";
	private static String jspPageIndex = "/jsp/reportIndex.jsp";
	
	private WindowsAuthenticationFilter.WindowsPrincipal winPrincipal = null;

	protected void processRequest(String action, HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.info("action = "+(action));

		/* get and authorize the user, otherwise, nothing else to do */
		winPrincipal = (WindowsAuthenticationFilter.WindowsPrincipal) request.getSession().getAttribute(
				WindowsAuthenticationFilter.AUTHENTICATION_PRINCIPAL_KEY);
		
		/* get db connections */
		DBConnection vanConn = VantiveDBUtil.currentConnection(properties.getProperty("vantive.db"));
		DBConnection bizConn = BizDBUtil.currentConnection(properties.getProperty("biz.db"));
		
		
		try {
			
			/* set up lists to use in dropdowns */
			List savvisCompanies = DBUtil.executeProcedureOneColumn(properties.getProperty("vantive.db"), "svsp_site_get_savvis_company");
			Collections.sort(savvisCompanies);
			savvisCompanies.add(0, "-- All Savvis Companies --");
			request.setAttribute("savvisCompanies", savvisCompanies);
			request.setAttribute("monthValues", Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}));
			request.setAttribute("monthLabels", 
					toList("January, February, March, " +
							"April, May, June, " +
							"July, August, September, " +
							"October, November, December")
			);
			request.setAttribute("reportTypes", VantiveExternalRating.getReportTypes());
			
			/* default the month and year to generate a report */
			request.setAttribute("defaultYear", Calendar.getInstance().get(Calendar.YEAR));
			request.setAttribute("defaultMonth", Calendar.getInstance().get(Calendar.MONTH));
			
			/* get product groups to display in drop down by using config file */
			String productConfigLocation = SystemUtil.getBASEDIR() + "/"+ SystemUtil.getAPPL() + "/config/misc/"+ productCfg;
			logger.info("productConfigLocation: " + productConfigLocation);
			logger.info("productConfigLocation).exists(): " + new File(productConfigLocation).exists());
			if(new File(productConfigLocation).exists()) {
				try {
					String x = XmlUtil.doc2String(XmlUtil.loadDocumentFromFile(productConfigLocation));
				} catch (Exception e) {
					logger.error("", e);
				}
				
				/* loop through the nodes and save into list */
				SimpleNode productsConfig = new SimpleNode(XmlUtil.loadDocumentFromFile(productConfigLocation).getFirstChild());
				List<SimpleNode> productGroups = productsConfig.getChildren("productGroup");
				SelectList products = new SelectList();
				SelectList billingTypes = new SelectList();
				billingTypes.addOption("", "");
				for (SimpleNode productGroup : productGroups) {
					logger.info("productGroup: " + productGroup);
					products.addOption(productGroup.getAttribute("name"), productGroup.getAttribute("billingType"));
					billingTypes.addOption(productGroup.getAttribute("billingType"), productGroup.getAttribute("billingType"));
				}
				request.setAttribute("products", products);
				request.setAttribute("billingTypes", billingTypes);
			}
			
			if("runReport".equals(action)) {
				performDBOperations(vanConn, null, action, request, response);
				
				forward(jspPageIndex, request, response);
			} else if("viewReport".equals(action) || "export".equals(action)) {
				String report = request.getParameter("report");
				request.setAttribute("report", report);
				
				if (ObjectUtil.isEmpty(request.getParameter("cycle"))) {
					request.setAttribute("fatalMsg", "A billing cycle must be selected in order to view an existing report.");
					forward(jspPageIndex, request, response);
				} else {
					BillingCycle billingCycle = (BillingCycle)DBUtil.findById(bizConn, BillingCycle.class, 
							request.getParameter("cycle"));
					billingCycle.getBillingType().getName();
					billingCycle.getChargeList().size();
					request.setAttribute("billingCycle", billingCycle);
					
					if("export".equals(action)) {
						response.setContentType("application/vnd.ms-excel");
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
						logger.info("report: " + report);
						response.setHeader("Content-Disposition", "attachment; filename=\"EXTRATING_"+
									sdf.format(billingCycle.getStartDate())+"-"+
									sdf.format(billingCycle.getThroughDate())+".xls\"");
						HSSFWorkbook wb = new VantiveExternalRating().createBillingCycleWorkbook(vanConn, billingCycle, report);
						wb.write(response.getOutputStream());
						return;
					} else {
						List<Map<String,Object>> reportData = new VantiveExternalRating().createBillingCycleReport(
									vanConn, billingCycle, report);
						request.setAttribute("reportData", reportData);
						
						forward(jspPageReport, request, response);
					}
				}
			} else {
				forward(jspPageIndex, request, response);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			VantiveDBUtil.closeConnection(vanConn);
			DBUtil.closeConnection(bizConn);
		}
	}
	
	/**
	 * @param billingCycle
	 * @return String
	 */
	private static String getBillingCycleDescription(BillingCycle billingCycle) {
		SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy h:mm aa");
		SimpleDateFormat sdf2 = new SimpleDateFormat("MMMM d, yyyy");
		return sdf.format(billingCycle.getDateCreated())+" (Start Date: "+
					sdf2.format(billingCycle.getStartDate())+")";
	}

	protected void performProtectedDBOperations(DBConnection conn, Object data, String action,
				HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		if("runReport".equals(action)) {
//			properties.reload();

			Calendar cal = Calendar.getInstance();
			cal.set(getIntegerParameter(request, "year"), getIntegerParameter(request, "month"), 1);
			
			String beginDate = new SimpleDateFormat("MM/dd/yyyy").format(cal.getTime());
			
			cal.add(Calendar.MONTH, 1);
			
			// the services we call behave differently - need a better way to do this, but for now...
			if ("Image Management".equals(request.getParameter("billingType"))) {
				// need to subtract one to keep Image Mgmt working the same way - end goal is to have
				// the services we call behave the same
				cal.add(Calendar.DAY_OF_YEAR, -1);
			}
			String endDate = new SimpleDateFormat("MM/dd/yyyy").format(cal.getTime());
			
			CommandLineProcess clp = new CommandLineProcess();
			clp.setWaitForProcess(true);
			
			String cmd = properties.getProperty("batch.job.command") + 
				" -vantivedb " + properties.getProperty("vantive.db") +
				" -bizdb " + properties.getProperty("biz.db") +
				" -config " + properties.getProperty("extRating.config") +
				" -beginDate " + beginDate + 
				" -endDate " + endDate + 
				" -productGroup " + request.getParameter("billingType") +
				" -user " + winPrincipal.getName() +
				"";
			logger.info("cmd: " + cmd);
			cmd = replaceAll(cmd, "[[classpath]]", properties.getProperty("java.class.path", null));
			logger.info("start.dir: " + properties.getProperty("start.dir"));
			clp.setDir(new File((String) properties.getProperty("start.dir")));
			logger.info("log.file: " + properties.getProperty("log.file", null));
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
			envList.add("CALLED_BY_USER="+winPrincipal.getName());
			envList.add("APPL="+SystemUtil.getAPPL());
			envList.add("BASEDIR="+SystemUtil.getBASEDIR());
			clp.setEnvp((String[]) envList.toArray(new String[] {}));
			
			logger.info("cmd: " + cmd);
			int exitCode = clp.run(cmd);
//			int exitCode = 0;

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

	public static LinkedHashMap<Object, Object> getBillingCycles(String billingType) {
		LinkedHashMap<Object, Object> cycles = new LinkedHashMap<Object, Object>();
		
		DBConnection bizConn = BizDBUtil.currentConnection("biz");

		try {
			List<BillingCycle> list = DBUtil.executeHqlQuery(bizConn, 
					"FROM BillingCycle WHERE billingType.name = '" + billingType + "' " +
					"AND dateCreated > getdate() - 180 ORDER BY dateCreated DESC");
			for (BillingCycle billingCycle : list) {
				logger.info("adding [" + billingCycle.getId() + "] -> [" + getBillingCycleDescription(billingCycle) + "]");
				cycles.put(billingCycle.getId().toString(), getBillingCycleDescription(billingCycle));
			}

		} catch (Exception e) {
			logger.error("", e);
		} finally {
			// free the session
			DBUtil.closeConnection(bizConn);
		}
		
		return cycles;
	}
	

}
