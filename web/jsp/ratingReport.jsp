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
	
		<link rel="stylesheet" href="css/main.css"/>
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
					<td class="reportHdr"><strong>Rating Report:  ${billingCycle.billingType.name} 
								<sv:out value="${billingCycle.startDate}"/> - <sv:out value="${billingCycle.throughDate}"/></strong></td>
					<c:if test='${not export}'>
							<td width="33%" align="right">Switch Report Type: 
								<sv:select items="${reportTypes}" name="report" addEmptyEntry="false"
											onchange="svSubmitAction('viewReport')" value="${report}"/>
							</td>
						</tr>
					</c:if>
				</tr>
				<c:if test='${not export}'>
					<tr>
						<td>
							<a href="#" class="pgLink" onclick="svSubmitAction('')">Return to External Rating Home</a>&nbsp;&nbsp;&nbsp;
							<c:if test='${not empty reportData}'>
								&nbsp;&nbsp;|&nbsp;&nbsp;
								<a href="#" class="pgLink" onclick="svSubmitAction('export')">Export this Report to Excel</a>
							</c:if>
						</td>
					</tr>
				</c:if>
			</table>
			<br/>
			
			<center>
				<c:if test='${empty reportData}'>
					No results were found
				</c:if>
				<c:if test='${not empty reportData}'>
					<sv:dataTable data="${reportData}" styleClass="dataTbl">
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