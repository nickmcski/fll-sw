<%--
  This page is used for commiting actions of editTeam.jsp.  The request parameter
  addTeam is set when this page is being used to add a team.
  --%>

<%@ include file="/WEB-INF/jspf/init.jspf"%>

<%@ page import="fll.Utilities"%>
<%@ page import="fll.db.Queries"%>

<%@ page import="org.w3c.dom.Document"%>

<%@ page import="java.sql.Connection"%>

<%@ page import="java.text.NumberFormat"%>

<%
      final Document challengeDocument = (Document) application.getAttribute("challengeDocument");
      final Connection connection = (Connection) application.getAttribute("connection");
%>

<html>
<head>
<title><x:out select="$challengeDocument/fll/@title" /> (Commit
Team)</title>
<link rel="stylesheet" type="text/css"
 href="<c:url value='/style/style.jsp'/>" />
</head>

<body>
<h1><x:out select="$challengeDocument/fll/@title" /> (Commit Team)</h1>

<%
      //parse the numbers first so that we don't get a partial commit
      final int teamNumber = NumberFormat.getInstance().parse(request.getParameter("teamNumber")).intValue();
      final String division = request.getParameter("division");
%>

<%-- check which button was pushed --%>
<c:choose>
 <c:when test='${not empty param.delete}'>
  <%
  Queries.deleteTeam(teamNumber, challengeDocument, connection, application);
  %>
  <a href="select_team.jsp">Normally you'd be redirected here</a>
  <c:redirect url='select_team.jsp' />
 </c:when>
 <%-- end delete --%>
 <c:when test='${not empty param.advance}'>
  <%
        final boolean result = Queries.advanceTeam(connection, teamNumber);
        if (result) {
  %>
  <a href="select_team.jsp">Normally you'd be redirected here</a>
  <c:redirect url='select_team.jsp' />
  <%
  } else {
  %>
  <font color='red'>Error advancing team</font>
  <%
  }
  %>
 </c:when>
 <%-- end advance --%>
 <c:when test='${not empty param.demote}'>
  <%
  Queries.demoteTeam(connection, challengeDocument, teamNumber);
  %>
  <a href="select_team.jsp">Normally you'd be redirected here</a>
  <c:redirect url='select_team.jsp' />
 </c:when>
 <c:when test='${not empty param.commit}'>
  <c:choose>
   <c:when test="${not empty param.addTeam}">
                added a team
<%
             if (null != Queries.addTeam(connection, teamNumber, request.getParameter("teamName"), request.getParameter("organization"), request
             .getParameter("region"), division)) {
   %>
    <c:redirect url='index.jsp'>
     <c:param name='message'>
      <font color='red'>Error, team number <%=teamNumber%> is
      already assigned..</font>
     </c:param>
    </c:redirect>
    <%
    }
    %>

    <a href="index.jsp">Normally you'd be redirected here</a>
    <c:redirect url='index.jsp' />
   </c:when>
   <c:otherwise>
    <%
              Queries.updateTeam(connection, teamNumber, request.getParameter("teamName"), request.getParameter("organization"), request.getParameter("region"),
              division);
          pageContext.setAttribute("teamCurrentTournament", Queries.getTeamCurrentTournament(connection, teamNumber));
    %>
    <c:if
     test="${not empty param.currentTournament and teamCurrentTournament != param.currentTournament}">
     <%
     Queries.changeTeamCurrentTournament(connection, challengeDocument, teamNumber, request.getParameter("currentTournament"));
     %>
    </c:if>

    <a href="select_team.jsp">Normally you'd be redirected here</a>
    <c:redirect url='select_team.jsp' />
   </c:otherwise>
  </c:choose>
 </c:when>
 <%-- end commit --%>
 <c:otherwise>
  <p>Internal error, cannot not figure out what editTeam.jsp did!</p>
 </c:otherwise>
</c:choose>
<%-- end checking which button --%>

<%@ include file="/WEB-INF/jspf/footer.jspf"%>
</body>
</html>