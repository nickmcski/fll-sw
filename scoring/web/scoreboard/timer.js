var runNumber = 0;
var timer = document.getElementById("timer");
function initTimer() {
  (function timerWorker() {
    $.ajax({
      url: 'Timer', 
      success: function(result) {
        if(result != runNumber){
          runNumber = result;
          timer.load();
          timer.play();
        }
      },
      complete: function() {
        setTimeout(timerWorker, 2000);
      }
    });
  })();
}

