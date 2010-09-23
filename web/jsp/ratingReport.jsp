<%--
	Copyright 2010 SAVVIS Communications. All rights reserved.
            
	The page is the index page for external ratings

	@author Ted Elrick
	@version $Id$

--%>

<%@taglib uri="/WEB-INF/savvis" prefix="sv"%> 
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
	<c:if test='${not export}'>
		<sv:head title="External Rating Report Index"></sv:head>
	
		<link rel="stylesheet" href="css/genericUpload.css"/>
	</c:if>
	
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
			<input type="hidden" name="cycle" value="${billingCycle.id}" />
			
			<table width="100%" cellspacing="0" cellpadding="4" border="0">
				<tr>
					<td class="uploadHdr" colspan="3"><strong>Rating Report:  ${billingCycle.billingType.name} 
								<sv:out value="${billingCycle.startDate}"/> - <sv:out value="${billingCycle.throughDate}"/></strong></td>
				</tr>
				<c:if test='${not export}'>
					<tr>
						<td width="33%">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
						<td width="34%" align="center">
							<a href="#" class="validationLink" onclick="svSubmitAction('')">Report Index</a>&nbsp;&nbsp;&nbsp;
							<c:if test='${not empty reportData}'>
								<a href="#" class="validationLink" onclick="svSubmitAction('export')">Export to Excel</a>
							</c:if>
						</td>
						<td width="33%" align="right">
							Switch to Report: 
							<sv:select items="External Rating,Usage Exceptions" name="report" addEmptyEntry="false"
										onchange="svSubmitAction('viewReport')" value="${report}"/>
						</td>
					</tr>
				</c:if>
			</table>
			
			<center>
				<c:if test='${empty reportData}'>
					No results were found
				</c:if>
				<c:if test='${not empty reportData}'>
					<sv:dataTable data="${reportData}" 
								border="1" cellpadding="4" cellspacing="1">
					</sv:dataTable>
				</c:if>
			</center>
			
		</form>
	</body>
	<script>
		function gotoMigrator(config) {
			svSetInputValue("config", config);
			svSubmitAction("selectConfig");
		}
	</script>
</html>