var runNumber = 0;
var timer = document.getElementById("timer");
//var timeout;
function initTimer() {
  (function pitWorker() {
    $.ajax({
      url: 'Timer', 
      success: function(result) {
//        var current = $('.content').html();
//        console.log(current);
//        console.log(result);
        if(result != runNumber){
          //clearTimeout(timeout);
          runNumber = result;
          
          //$('.content').fadeOut();
          timer.load();
          timer.play();
          //timeout = window.setTimeout(stopVideo,(1000*157));
        }
      },
      complete: function() {
        setTimeout(pitWorker, 5000);
      }
    });
  })();
}

function stopVideo(){
  timer.pause();

}

