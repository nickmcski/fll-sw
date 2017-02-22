<%@ include file="/WEB-INF/jspf/init.jspf"%>

<!DOCTYPE HTML>
<html>
<head>
<title>Login Page</title>
<link rel="stylesheet" type="text/css"
 href="<c:url value='/style/fll-sw.css'/>" />
 
 <link
  rel="stylesheet"
  href="extlib/jquery.mobile-1.4.5.min.css" />

 
</head>
<body>
 <div class="sitelogin">
 <h1>Login to FLL</h1>

 <div class='status-message'>${message}</div>
 <%-- clear out the message, so that we don't see it again --%>
 <c:remove var="message" />
<div class ="sitelogin">
 <form method="POST" action="DoLogin" name="login" data-ajax="false">
  Username : <input class="form-control" type="text" size="15" maxlength="64" name="user" autocorrect="off" autocapitalize="off" autocomplete="off" spellcheck="false" autofocus placeholder="Username"/><br />
  Password : <input class="form-control" type="password" size="15" name="pass" placeholder="Password"/><br /> 
  <input class="btn btn-primay" name="submit_login" value="Login" type="submit" />
 </form>
 </div>
 </div>
</body>
</html>
