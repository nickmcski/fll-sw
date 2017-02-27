<%@ include file="/WEB-INF/jspf/init.jspf"%>
<%
    if (request.getParameter("pit") != null) {
        %>
        <c:set var="pit" value="TRUE"/>
        <%
    }
%>
<!DOCTYPE HTML>
<html>
<head>
<title>Main scoreboard</title>
<meta name="viewport" content="width=1080" >
<style>
html, body {
	height:100%;
}
</style>
</head>
<body>
 <div class="score_background"></div>
 <div class="score_header">
 <h1>Welcome to the 2017 Crown Cup!</h1>
 </div>
 <div class="score_body">
  <div class="content-wrap">
   <div class="content">
   <c:if test="${pit}">
   	<p>This page requires JavaScript</p>
   	</c:if>
   </div>
  </div>
 </div>
 <div class="score_footer">
 <!-- 
   <div class="entry">
  	<p class="team">Team 1: The Test team from Tesla</p>
  	<p class="score">Run 1: 212 Points</p>
  	<p class="score">Run 2: 512 Points</p>
  	<p class="score">Run 3: 13 Points</p>
  </div>
  <div class="entry">
  	<p class="team">Team 2: Second the Best</p>
  	<p class="score">Run 1: 212 Points</p>
  	<p class="score">Run 2: 512 Points</p>
  	<p class="score">Run 3: 13 Points</p>
  </div>
  <div class="entry">
  	<p class="team">Team 3: The team of three</p>
  	<p class="score">Run 1: 212 Points</p>
  	<p class="score">Run 2: 512 Points</p>
  	<p class="score">Run 3: 13 Points</p>
  </div>
  
  <div class="entry">
  	<p class="team">Team 4: Forth the horth</p>
  	<p class="score">Run 1: 212 Points</p>
  	<p class="score">Run 2: 512 Points</p>
  	<p class="score">Run 3: 13 Points</p>
  </div>
 -->
 </div>
 <script src="loadlast4.js"></script>
 <script src="loadpit.js"></script>
 <script>
 window.onload = init;
 
 function init(){
   <c:if test="${pit}">initpit();</c:if>
   init4();
 }
 </script>
</body>
</html>
