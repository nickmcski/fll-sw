<%@ include file="/WEB-INF/jspf/init.jspf" %>

<%@ page import="fll.Utilities" %>
<%@ page import="fll.Queries" %>
  
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.Statement" %>
<%@ page import="java.sql.ResultSet" %>

<%
final Connection connection = (Connection)application.getAttribute("connection");
final String currentTournament = Queries.getCurrentTournament(connection);

final String sql = "SELECT Teams.TeamNumber, Teams.Organization, Teams.TeamName, Teams.Division, Performance.Tournament, Performance.RunNumber, Performance.Bye, Performance.NoShow, Performance.ComputedTotal"
  + " FROM Teams,Performance"
  + " WHERE Performance.Tournament = '" + currentTournament + "'"
  + " AND Teams.TeamNumber = Performance.TeamNumber"
  + " ORDER BY Teams.Organization, Teams.TeamNumber, Performance.RunNumber";
final Statement stmt = connection.createStatement();
final ResultSet rs = stmt.executeQuery(sql);
%>

<c:set var="thisURL">
  <c:url value="${pageContext.request.servletPath}">
    <c:param name="scroll" value="${param.scroll}" />
  </c:url>
</c:set>
    
<HTML>
<head>
<style>
	FONT {color: #ffffff; font-family: "Arial"}
	TABLE.A {background-color:#000080 }
	TABLE.B {background-color:#0000d0 }
</style>

<!-- stuff for automatic scrolling -->
<script type="text/javascript">
var scrollTimer;
var scrollAmount = 2;    // scroll by 100 pixels each time
var documentYposition = 0;
          
//http://www.evolt.org/article/document_body_doctype_switching_and_more/17/30655/index.html
function getScrollPosition() {
  if (window.pageYOffset) {
    return window.pageYOffset
  } else if (document.documentElement && document.documentElement.scrollTop) {
    return document.documentElement.scrollTop
  } else if (document.body) {
    return document.body.scrollTop
  }
}

function myScroll() {
  documentYposition += scrollAmount;
  window.scrollBy(0, scrollAmount);
  if(getScrollPosition()+300 < documentYposition) { //wait 300 pixels until we refresh
    window.clearInterval(scrollTimer);
    window.scroll(0, 0); //scroll back to top and then refresh
    location.href='<c:out value="${thisURL}" />'
  }
}

function start() {
<c:if test="${not empty param.scroll}">
  scrollTimer = window.setInterval('myScroll()',30);
</c:if>
}
</script>


</head>

<body bgcolor='#000080' onload='start()'>
<br><br><br><br><br><br><br><br>

<c:set var="colorStr" value="A" />
                            
<% 	
if(rs.next()) {
  boolean done = false;
  while(!done) {
%>
  <table border='0' cellpadding='0' cellspacing='0' width='99%' class='<c:out value="${colorStr}" />'>
    <tr><td colspan='2'><img src='<c:url value="/images/blank.gif"/>' height='15' width='1'></td></tr>
    <tr align='left'>
<%
    String divisionStr = rs.getString("Division");
    final String headerColor;
    if("1".equals(divisionStr)) {
      headerColor = "#800000";
    } else {
      headerColor = "#008000";
    }
%>
      <td width='25%' bgcolor='<%=headerColor%>'>
        <font size='2'><b>&nbsp;&nbsp;Division:&nbsp;<%=divisionStr%>&nbsp;&nbsp;</b></font>
     </td>
     <td align='right'>
       <font size='2'><b>Team&nbsp;#:&nbsp;<%=rs.getInt("TeamNumber")%>&nbsp;&nbsp;</b></font>
     </td>
  </tr>
  <tr align='left'>
    <td colspan='2'>
      <font size='4'>&nbsp;&nbsp;<%=rs.getString("Organization")%></font>
    </td>
  </tr>
  <tr align='left'>
    <td colspan='2'>
      <font size='4'>&nbsp;&nbsp;<%=rs.getString("TeamName")%></font>
    </td>
  </tr>
  <tr>
    <td colspan='2'><hr color='#ffffff' width='96%'></td>
  </tr>
  <tr>
  <td colspan='2'>

    <table border='0' cellpadding='1' cellspacing='0'>
      <tr align='center'>
        <td><img src='<c:url value="/images/blank.gif"/>' height='1' width='60'></td>
        <td><font size='4'>Run #</font></td>
        <td><img src='<c:url value="/images/blank.gif"/>' width='20' height='1'></td>
          <td><font size='4'>Score</font></td>
        </tr>
        <%
        int prevNum = rs.getInt("TeamNumber");
        do {
        %>
        <tr align='right'>
          <td><img src='<c:url value="/images/blank.gif"/>' height='1' width='60'></td>
          <td><font size='4'><%=rs.getInt("RunNumber")%></font></td>
          <td><img src='<c:url value="/images/blank.gif"/>' width='20' height='1'></td>
          <td><font size='4'>
          <%if(rs.getBoolean("NoShow")) {%>
            No Show
          <%} else if(rs.getBoolean("Bye")) {%>
            Bye
          <%} else {
              out.println(rs.getInt("ComputedTotal"));
            }
            if(!rs.next()) {
              done = true;
            }
           } while(!done && prevNum == rs.getInt("TeamNumber"));
          %>
          </font></td>
      </table>
      
    </td>
  </tr>
  <tr><td colspan='2'><img src='<c:url value="/images/blank.gif"/>' width='1' height='15'></td></tr>
</table>

<c:choose>
  <c:when test="${'A' == colorStr}">
    <c:set var="colorStr" value="B" />
  </c:when>
  <c:otherwise>
    <c:set var="colorStr" value="A" />
  </c:otherwise>
</c:choose>

<%
  } //end while(!done)
}//end if
%>
                          
</div>
</body>
<%
  Utilities.closeResultSet(rs);
  Utilities.closeStatement(stmt);
%>
</HTML>
