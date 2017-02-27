<%@ include file="/WEB-INF/jspf/init.jspf" %>

<%@ page import="fll.db.Queries" %>
<%@ page import="fll.web.ApplicationAttributes"%>
  
<%@ page import="java.sql.Connection" %>
<%@ page import="javax.sql.DataSource" %>
  
<%
    final DataSource datasource = ApplicationAttributes.getDataSource(application);
    final Connection connection = datasource.getConnection();
    pageContext.setAttribute("tournament", Queries.getCurrentTournament(connection));
    pageContext.setAttribute("divisions", Queries.getAwardGroups(connection));
  %>

<html>
  <head>
    <link rel="stylesheet" type="text/css" href="<c:url value='/style/fll-sw.css'/>" />
    <title>Performance Runs</title>
  </head>

  <body>
    <h1>Performance Runs</h1>
      <c:forEach var="division" items="${divisions}">
        <h2>Division <c:out value="${division}"/></h2>
        <table border='1'>
          <tr>
           <th>Team Number </th>
           <th>Team Name </th>
           <th>Organization </th>
           <th>Run</th>
           <th>Verified</th>
           <th>Score</th>
           <th>Referee</th>
           <th>View scoresheet</th>
          </tr>
          <sql:query var="result" dataSource="${datasource}">
            SELECT Teams.TeamNumber,Teams.TeamName,Teams.Organization,Performance.ComputedTotal,Performance.NoShow,Performance.RunNumber,Performance.Verified,Performance.Referee
                     FROM Teams,Performance,current_tournament_teams
                     WHERE Teams.TeamNumber = Performance.TeamNumber
                       AND current_tournament_teams.TeamNumber = Teams.TeamNumber
                       AND Performance.Tournament = <c:out value="${tournament}"/>
                       AND current_tournament_teams.event_division  = '<c:out value="${division}"/>'
                       ORDER BY TIMESTAMP DESC
          </sql:query>
          <c:forEach items="${result.rows}" var="row">
          	<tr<c:if test="${row.Verified != true}" var="test"> class="alert alert-warning"</c:if>>
            
              <td><c:out value="${row.TeamNumber}"/></td>
              <td><c:out value="${row.TeamName}"/></td>
              <td><c:out value="${row.Organization}"/></td>
              <td><c:out value="${row.RunNumber}"/></td>
              <td><c:out value="${row.Verified}"/></td>
              <c:if test="${row.NoShow == True}" var="test">
                <td>No Show</td>
              </c:if>
              <c:if test="${row.NoShow != True}">
                <td><c:out value="${row.ComputedTotal}"/></td>
              </c:if>
              <td><c:out value="${row.Referee}"/></td>
              <td><a href="<c:url value='/playoff/ScoresheetServlet?team=${row.TeamNumber}&match=${row.RunNumber}'/>">Print scoresheet</a></td>
            </tr>
          </c:forEach>
        </table>
      </c:forEach>
      

  </body>
</html>
