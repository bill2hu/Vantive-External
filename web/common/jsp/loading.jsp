<%--
    Copyright 2010 SAVVIS Communications. All rights reserved.

		This page shows the image /images/loading.gif
		$Header$
--%>

<%@taglib uri="/WEB-INF/c.tld" prefix="c"%> 

<html>
	<sv:head title="Loading"/>

	<body marginwidth="0" marginheight="0">
		<center>
			<table cellpadding=0 cellspacing=0>
				<tr>
					<td colspan=2><img src="<c:url value="/common/images/empty.gif"/>" height=10></td>
				</tr>
				<tr>
					<td><img src="<c:url value="/common/images/empty.gif"/>" width=12></td>
					<td><img src="<c:url value="/images/loading.gif"/>"></td>
				</tr>
			</table>
		</center>
	</body>
</html>
