<%@ include file="/WEB-INF/jspf/init.jspf"%>

<html>
<head>
<title>Start Countdown</title>
<link rel="stylesheet" type="text/css"
 href="<c:url value='/style/fll-sw.css'/>" />

<script type='text/javascript' src='../extlib/jquery-1.11.1.min.js'></script>

<script type='text/javascript'>
	
window.onload = init

function init(){
  $( "#go" ).click(function() {
    $.ajax({
      url: '../scoreboard/Timer?go', 
      success: function(result) {
        alert("Done!");
      }
    });
  });
}

</script>
</head>

<body>

 <div class='status-message'>${message}</div>
 <%-- clear out the message, so that we don't see it again --%>
 <c:remove var="message" />

<button class="btn btn-large btn-sucuess" id='go'>GO!</button>

</body>
</html>
