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

	<script type='text/javascript' src='<c:url value="/dwr/interface/RatingReportIndexServlet.js"/>'></script> 
	<script type='text/javascript' src='<c:url value="/dwr/engine.js"/>'></script>
	<script type='text/javascript' src='<c:url value="/dwr/util.js"/>'></script>
	<script language="Javascript">
	<!--
		function updateBillingCycles() {
			if (svGetInputValue("billingTypeName") != "") {
				RatingReportIndexServlet.getBillingCycles(svGetInputValue("billingTypeName"), {
					  callback:function(dataFromServer) {
							updateBillingCyclesCallback(dataFromServer);
					  }
					});
			}
		}
		function updateBillingCyclesCallback(cycles) {
			var fieldId = 'cycle';
			DWRUtil.removeAllOptions(fieldId);
			DWRUtil.addOptions(fieldId, cycles);
		}
	-->
	</script>
	
	<link rel="stylesheet" href="css/main.css"/>
	
	<body>
	
		<table style="width: 100%;" cellspacing="0" cellpadding="0">
			<tr>
				<td width="5"></td>
				<td>
					<img src="/VantiveExternalRating/common/images/savvisLogoRebranded.png" width="175"/>
				</td>
				<td class="pgLink"><a class="pgLink" href="?">Refresh This Page</a></td>
			</tr>
		</table>
		<br/>

		<c:if test='${not empty fatalMsg or not empty message}'>
		<table style="width: 100%;" cellspacing="0" cellpadding="0">
			<tr>
				<td width="5"></td>
				<td>
					<c:if test='${not empty fatalMsg}'>
						<span class="fatalMsg">${fatalMsg}</span>
						<br/>
					</c:if>
					<c:if test='${not empty message}'>
						<span class="msg">&nbsp;${message}</span>
					</c:if>
				</td>
			</tr>
		</table>
		<br/>
		</c:if>
	
		<form method="post" action="RatingReportIndexServlet">
			<input type="hidden" name="action" />
			
			<table width="100%" cellspacing="0" cellpadding="2">
				<tr><td class="sectionHdr" colspan="2"><strong>Generate New Report</strong></td></tr>
				<tr><td>&nbsp;</td></tr>
				<tr>
					<td class="inputTitle">SAVVIS Company</td>
					<td><sv:select items="${savvisCompanies}" name="savvisCompany" addEmptyEntry="false"/></td>
				</tr>
				<tr>
					<td class="inputTitle">Usage Month and Year</td>
					<td>
						<sv:select items="${monthValues}" labels="${monthLabels}" name="month" value="${defaultMonth}" addEmptyEntry="false"/>&nbsp;
						<sv:input type="numeric" name="year" value="${defaultYear}" maxlength="4" title="Year"/>&nbsp;
						(YYYY e.g. ${defaultYear})
					</td>
				</tr>
				<tr>
					<td class="inputTitle">Billing Type</td>
					<td>
						<sv:select items="${billingTypes}" name="billingType" addEmptyEntry="false"/>
					</td>
				</tr>
				<tr>
					<td class="inputTitle">&nbsp;</td>
					<td><br/><button onclick="svSubmitAction('runReport')">Generate Charges</button></td>
				</tr>
				<tr>
					<td colspan="2"><br/><br/></td>
				</tr>
				<tr>
					<td class="sectionHdr" colspan="2"><strong>View Existing Report</strong></td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<tr>
					<td class="inputTitle">SAVVIS Company</td>
					<td><sv:select items="${savvisCompanies}" name="savvisCompany2" addEmptyEntry="false"/></td>
				</tr>
				<tr>
					<td class="inputTitle">Billing Type</td>
					<td>
						<sv:select items="${billingTypes}" name="billingType" id="billingTypeName" onchange="Javascript:updateBillingCycles();"/>
					</td>
				</tr>
				<tr>
					<td class="inputTitle">Billing Cycle</td>
					<td>
						<sv:select items="" name="cycle" id="cycle" addEmptyEntry="false"/>
						&nbsp;<span class="inputDesc">(limited to past six months)</span>
					</td>
				</tr>
				<tr>
					<td class="inputTitle">Rating Report</td>
					<td>
						<sv:select items="External Rating,Usage Exceptions" name="report" addEmptyEntry="false"/>
					</td>
				</tr>
				<tr>
					<td class="inputTitle">&nbsp;</td>
					<td><br/><button onclick="svSubmitAction('viewReport')">View Existing Report</button></td>
				</tr>
			</table>
		</form>
		
		<br/><br/>
		
		
	</body>
</html>