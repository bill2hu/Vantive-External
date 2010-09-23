<%--
	Copyright 2010 SAVVIS Communications. All rights reserved.
            
	The page is the index page for external ratings

	@author Ted Elrick
	@version $Id$

--%>

<%@taglib uri="/WEB-INF/savvis" prefix="sv"%> 
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
	<sv:head title="External Rating Report Index"></sv:head>
	
	<link rel="stylesheet" href="css/genericUpload.css"/>
	
	<body>
	
		<center>
		<c:if test='${fatalMsg ne "" and fatalMsg != null}'>
			<font color="red">${fatalMsg}</font>
		</c:if>
		<c:if test='${not empty message}'>
			<font color="blue">${message}</font>
		</c:if>
		</center>
		
		<form method="post" action="RatingReportIndexServlet">
			<input type="hidden" name="action" />
			
			<table width="100%" cellspacing="0" cellpadding="0">
				<tr><td class="uploadHdr" colspan=2><strong>Rating:  Generate New Report</strong></td></tr>
			</table>
			<center>
				<table cellspacing="0" cellpadding="1">
					<tr><td>&nbsp;</td></tr>
					<tr>
						<td class="inputTitle">SAVVIS Company</td>
						<td><sv:select items="${savvisCompanies}" name="savvisCompany" addEmptyEntry="false"/></td>
					</tr>
					<tr>
						<td class="inputTitle">Usage Month and Year</td>
						<td>
							<sv:select items="${monthValues}" labels="${monthLabels}" name="month" value="${defaultMonth}"/>&nbsp;
							<sv:input type="numeric" name="year" value="${defaultYear}" maxlength="4"/>&nbsp;
							(YYYY e.g. ${defaultYear})
						</td>
					</tr>
					<tr>
						<td class="inputTitle">Rating Report</td>
						<td>
							<sv:select items="External Rating" name="reportToRun" addEmptyEntry="false"/>
						</td>
					</tr>
					<tr>
						<td colspan="2" align="center">
							<button onclick="svSubmitAction('runReport')">Generate Charges</button>
						</td>
					</tr>
				</table>
			</center>
			
			<br/><br/>
			
			<table width="100%" cellspacing="0" cellpadding="0">
				<tr><td class="uploadHdr" colspan=2><strong>Rating:  View Existing Report</strong></td></tr>
			</table>
			<center>
				<table cellspacing="0" cellpadding="1">
					<tr><td>&nbsp;</td></tr>
					<tr>
						<td class="inputTitle">SAVVIS Company</td>
						<td><sv:select items="${savvisCompanies}" name="savvisCompany2" addEmptyEntry="false"/></td>
					</tr>
					<tr>
						<td class="inputTitle">Billing Cycle</td>
						<td>
							<sv:select items="${billingCycleIds}" labels="${billingCycleDescriptions}" 
										name="cycle" addEmptyEntry="false"/>
						</td>
					</tr>
					<tr>
						<td class="inputTitle">Rating Report</td>
						<td>
							<sv:select items="External Rating,Usage Exceptions" name="report" addEmptyEntry="false"/>
						</td>
					</tr>
					<tr>
						<td colspan="2" align="center">
							<button onclick="svSubmitAction('viewReport')">View Existing Report</button>
						</td>
					</tr>
				</table>
			</center>
				
				<!-- 
				<tr>
					<td style="padding: 3px;">
					<span class="fileListHdr" >$$$ {appl.key}</span>
					<table width="100%" cellspacing="2" cellpadding="2" class="actionTbl" style="border-bottom: none;">
							<td align="center" width="50%">
								<a class="pageNav" href="javascript:gotoMigrator('$$$ {client.absolutePath}')">$$$ {client.name}</a>
							</td>
							<c:if test="$$$ {x.index % 2 eq 1}">
								</tr>
								<tr>
							</c:if>
							<c:if test="$$$ {x.index % 2 eq 0 and x.index == sv:sizeOf(appl.value)-1}">
								<td width="50%">&nbsp;
								</td>
							</c:if>
						</tr>
					</table>
					
					</td>
				</tr>
				 -->
			
		</form>
	</body>
	<script>
		function gotoMigrator(config) {
			svSetInputValue("config", config);
			svSubmitAction("selectConfig");
		}
	</script>
</html>