<%@ include file="/WEB-INF/jspf/init.jspf"%>

<html>
<head>

<title>Timer</title>
</head>
<body>
  <video width="1920" height="1080" class="timer" id="timer">
  <source src="timer.mp4" type="video/mp4">
Your browser does not support the video tag.
</video>
<script src="timer.js"></script>
<script>
window.onload=init
function init(){
  initTimer();
}</script>
</body>
</html>
