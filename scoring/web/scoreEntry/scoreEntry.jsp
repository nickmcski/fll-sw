<%@ include file="/WEB-INF/jspf/init.jspf" %>

<%@ page import="fll.web.scoreEntry.ScoreEntry" %>

<html>
  <head>
  <c:choose>
    <c:when test="${1 == EditFlag}">
      <title>Score Edit</title>
    </c:when>
    <c:otherwise>
      <title>Score Entry</title>
    </c:otherwise>
    </c:choose>

      <link rel="stylesheet" type="text/css" href="<c:url value='/style/fll-sw.css'/>" />
	  <link rel="stylesheet" type="text/css" href="<c:url value='/css/signature.css'/>" />
    <style type='text/css'>
      TD {font-family: arial}
      table#top_info {   
      background-color: ${top_info_color};   
      }
      
      body { 
      background-color: ${body_background};
      }
      
      table .score-entry {     
table-layout: fixed;
width: 100%;
}

td .score-error {
  width: 100px;
}
      
    </style>

<script language="javascript">
    <c:if test="${not isBye}">


<!-- No Show -->
function submit_NoShow() {
 retval = confirm("Are you sure this is a 'No Show'?") 
 if(retval) {
  document.scoreEntry.NoShow.value = true;
  Verified = 1;
  refresh();
 }
 return retval;
}

function init() {
  // disable text selection
  document.onselectstart=new Function ("return false")

  <c:choose>
  <c:when test="${1 == EditFlag}">
  <%
    ScoreEntry.generateInitForScoreEdit(out, application, session);
  %>
  </c:when>
  <c:otherwise>
  <%
  ScoreEntry.generateInitForNewScore(out, application);
  %>

  </c:otherwise>
  </c:choose>

  refresh();
  
  /* Saves total score for smarter notification popups */
  savedTotalScore = document.getElementById('totalScore').innerHTML;
}

function refresh() { 
  var score = 0;

  <%ScoreEntry.generateRefreshBody(out, application);%>

  //check for minimum total score
  if(score < ${minimumAllowedScore}) {
    score = ${minimumAllowedScore};
  }

  savedTotalScore = document.getElementById('totalScore').innerHTML = score;

  check_restrictions();
}

function check_restrictions() {
  var error_found = false;

<%ScoreEntry.generateCheckRestrictionsBody(out, application);%>

  if(error_found) {
    document.getElementById("submit").disabled = true;
  } else {
    document.getElementById("submit").disabled = false;
  }
}

<%ScoreEntry.generateIsConsistent(out, application);%>


<%ScoreEntry.generateIncrementMethods(out, application);%>
</c:if> <!-- end check for bye -->

/**
 * Used to replace text in an element by ID.
 */
function replaceText(sId, sText) {
  var el;
  if(document.getElementById
     && (el = document.getElementById(sId))) {
     if(el.hasChildNodes && false) { // check for support for has child nodes
      while (el.hasChildNodes()) {
        el.removeChild(el.lastChild);
      }
      el.appendChild(document.createTextNode(sText));
    }
     var className = sId.substring(6) + '_row';
     if( el = document.getElementById(className)){
       if(sText.length > 0){
       	el.className = "bg-danger";
       }else{
         el.className = "";
       }
     }
  }
}

function handleErrors(message){
  if(el = document.getElementById("errors")){
    if(message.length > 0){
      el.style.display = "block";
      el.innerHTML=message;
    }else{
      el.style.display = "none";
      el.innerHTML="";
    }
  }
}

function showSignature(){
  
}

/**
 * Called when the cancel button is clicked.
 */
function CancelClicked() {
  <c:choose>	
  <c:when test="${1 == EditFlag}">
  if (confirm("Cancel and lose changes?") == true) {
  </c:when>
  <c:otherwise>
  if (confirm("Cancel and lose data?") == true) {
  </c:otherwise>
  </c:choose>
    window.location.href= "select_team.jsp";
  }
}
/**
 * Called to check verified flag
 */
function verification() {
if (Verified == 1)
{
	// Smarter Score Popups
	if (savedTotalScore!= document.getElementById('totalScore').innerHTML)
	 {
		 m = "You are changing and verifying a score -- are you sure?";
	 }
	 else
	 {
		 m = "You are verifying a score -- are you sure?";
	 }
return m;
}
else 
{
m = "You are submitting a score without verification -- are you sure?";
return m;
}
}
</script>

  </head>

<body onload="init()">
    <form action="submit.jsp" method="POST" name="scoreEntry">
      <input type='hidden' name='NoShow' value="false"/>

      <c:if test="${1 == EditFlag}">
        <input type='hidden' name='EditFlag' value='1' readonly>
      </c:if>
      <input type='hidden' name='RunNumber' value='${lRunNumber}' readonly>
      <input type='hidden' name='TeamNumber' value='${team.teamNumber}' readonly>

      <table width='100%' border="0" cellpadding="0" cellspacing="0" align="center"> <!-- info bar -->
        <tr>
          <td align="center" valign="middle">
          <!-- top info bar (team name etc) -->
            <table id='top_info' border="1" cellpadding="0" cellspacing="0" width="100%">
              <tr align="center" valign="middle"><td>

                  <table border="0" cellpadding="5" cellspacing="0" width="90%"> <!--  inner box on title -->
                    <tr>
                      <td valign="middle" align="center">
                        <c:choose>
                        <c:when test="${1 == EditFlag}">
                          <font face="Arial" size="4">Score Edit</font>
                        </c:when>
                        <c:otherwise>
                          <font face="Arial" size="4">Score Entry</font>
                        </c:otherwise>
                        </c:choose>
                      </td>
                    </tr>
                    <tr align="center">
                      <td>
                        <font face="Arial" size="4" color='#0000ff'>#${team.teamNumber}&nbsp;${team.organization}&nbsp;${team.teamName}&nbsp;--&nbsp;${roundText}</font>
                      </td>
                    </tr>
                  </table> <!--  end inner box on title -->

                </td></tr> <!-- team info -->
                
                <c:if test="${not previousVerified }">
                  <!--  warning -->
                  <tr><td bgcolor='red' align='center'>
                  <font face="Arial" size="4">Warning: Previous run for this team has not been verified!</font>                  
                </c:if>
                
            </table> <!--  end info bar -->

          </td></tr>


      <!-- score entry -->
      <tr>
        <td align="center" valign="middle">

          <table class='score-entry' border='1'  bordercolor='#808080'>
          <colgroup>
          <col width="3*"/> <!--  goal -->
          <col width="2*"/> <!-- buttons -->
          <col width="120px"/> <!-- count -->
          <col width="50px"/> <!--  score -->
          <col width="3*"/> <!--  error -->
          </colgroup>
            <tr>
              <td colspan='2'>
                <font size='4'><u>Goal</u></font>
              </td>
              <td align='right'>
                <font size='4'><u>Count</u></font>
              </td>
              <td align='right'>
                <font size='4'><u>Score</u></font>
              </td>
                <td align='center'>
                  <font class='score-error' size='4'><u>Error Message</u></font>
                </td>
            </tr>

            <c:choose>
            <c:when test="${isBye}">
              <tr>
                <td colspan='3'><b>Bye Run</b></td>
              </tr>
            </c:when>
            <c:otherwise>
            <c:if test="${isNoShow}">
              <tr>
                <td colspan='5' class='center warning'>Editing a No Show - submitting score will change to a real run</td>
              </tr>
            </c:if>
              <%ScoreEntry.generateScoreEntry(out, application);%>
			  <!-- Error section -->
			  <tr>
			  	<td colspan='5'>
			  		<div class="errors alert alert-danger" id="errors" style="display:none"></div>
			  	</td>
              <!-- Total Score -->
              <tr>
                <td colspan='3'>
                  <font size='4'><u>Total Score:</u></font>
                </td>
                <td align='right'>
                  <!-- <input type='text' name='totalScore' size='3' readonly tabindex='-1'>   -->
                  <p class='score totalscore' id='totalScore'/></p>
                </td>
              </tr>
              <%ScoreEntry.generateVerificationInput(out);%>
            </c:otherwise>
            </c:choose>  <!-- end check for bye -->
			<tr>
			<!-- Trigger the modal with a button -->
			  <td>
				<button type="button" class="btn btn-info btn-lg" data-toggle="modal" data-target="#myModal">Open Modal</button>
				
				<!-- Modal -->
				<div id="myModal" class="modal fade" role="dialog">
				  <div class="modal-dialog">
				
				    <!-- Modal content-->
				    <div class="modal-content">
				      <div class="modal-header">
				        <button type="button" class="close" data-dismiss="modal">&times;</button>
				        <h4 class="modal-title">Modal Header</h4>
				      </div>
				        <div id="signature-pad" class="m-signature-pad">
					    <div class="m-signature-pad--body">
					      <canvas></canvas>
					    </div>
					    <div class="m-signature-pad--footer">
					      <div class="description">Sign above</div>
					      <button type="button" class="button clear" data-action="clear">Clear</button>
					      <button type="button" class="button save" data-action="save">Save</button>
					    </div>
					  </div>
				      <div class="modal-footer">
				        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
				      </div>
				    </div>
				
				  </div>
				</div>
			  </td>
			</tr>
            <tr>
              <td colspan='5' align='center'>
              	<div class="btn-group">
                <c:if test="${not isBye}">
                  <c:choose>
                  <c:when test="${1 == EditFlag}">
                    <!--<input type='submit' id='submit' name='submit' value='Submit Score' onclick='return confirm(verification())'>   -->
                    <button type='submit' id='submit' class='btn btn-primary' name='submit' onclick='return confirm(verification())'>Submit Score</button> 
                  </c:when>
                  <c:otherwise>
                    <!-- <input type='submit' id='submit' name='submit' value='Submit Score' onclick='return confirm("Submit Data -- Are you sure?")'> -->
                    <button type='submit' id='submit' class='btn btn-primary' name='submit' onclick='return confirm("Submit Data -- Are you sure?")'>Submit Score</button> 
                  </c:otherwise>
                  </c:choose>
                </c:if>
                <!-- <input type='button' id='cancel' value='Cancel' onclick='CancelClicked()'> -->
                <button type='submit' id='cancel' class='btn btn-default' name='cancel' onclick='CancelClicked()'>Cancel</button> 
                <c:if test="${1 == EditFlag and isLastRun}">
                  <!-- <input type='submit' id='delete' name='delete' value='Delete Score' onclick='return confirm("Are you sure you want to delete this score?")'> -->
                  <button type='submit' id='delete' class='btn btn-danger' name='delete' value="Delete Score" onclick='return confirm("Are you sure you want to delete this score?")'>Delete Score</button>
                </c:if>
                <c:if test="${not isBye}">
                	<!-- <input type='submit' id='no_show' name='submit' value='No Show' onclick='return submit_NoShow()'> -->
                	<button type='submit' id='no_show' class='btn btn-warning' name='submit' value='No Show' onclick='return submit_NoShow()'>No Show</button>
              	</c:if>
              </div>
              </td>	
            </tr>
          </table> <!-- end score entry table  -->

        </td>
      </tr>
    </table> <!-- end table to center everything -->
   	<script type="text/javascript" src="<c:url value='/js/signature_pad.min.js'/>"></script></form> <!-- end score entry form -->
	<script type="text/javascript" src="<c:url value='/js/app.js'/>"></script>

  </body>
</html>
