<%@ include file="/WEB-INF/jspf/init.jspf" %>

<%@ page import="fll.xml.GenerateDB" %>
      
<c:if test="${not empty param.changeDatabase || not empty param.resetDatabase}">
  <c:if test="${not empty param.changeDatabase}" var="test">
    <c:set var="database" value="${param.database}" scope="application" />
  </c:if>
  <c:if test="${not test}">
    <c:set var="database" value="fll" scope="application" />
  </c:if>

  <%-- just remove the database connections and they'll get recreated on the redirect --%>
  <c:remove var="connection" />
  <c:remove var="datasource" />
  <c:remove var="challengeDocument" />
  <c:redirect url='index.jsp'>
    <c:param name="message">
      Changed database to <c:out value="${database}"/>
    </c:param>
  </c:redirect>

</c:if>
            
<html>
  <head>
    <link rel="stylesheet" type="text/css" href="<c:url value='/style/style.jsp'/>" />
    <title><x:out select="$challengeDocument/fll/@title"/> (Developer Commands)</title>
  </head>

  <body>
    <h1><x:out select="$challengeDocument/fll/@title"/> (Developer Commands)</h1>

    <p><font color='red'><b>This page is intended for developers only.  If you
    don't know what you're doing, LEAVE THIS PAGE!</b></font></p>

    <c:if test="${not empty param.message}">
      <p><i><c:out value="${param.message}"/></i></p>
    </c:if>
            
    <ul>
        
      <li>Current database is <c:out value="${database}"/><br>
          <form action='index.jsp' method='post'><input type='text' name='database'>
          <input type='submit' name='changeDatabase' value='Change Database''>
          <input type='submit' name='resetDatabase' value='Reset to standard database'>
          </form>
      </li>

        <li><a href="query.jsp">Do SQL queries and updates</a></li>
        
      <li><a href="<c:url value='/setup'/>">Go to database setup</a></li>
        
    </ul>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
  </body>
</html>
