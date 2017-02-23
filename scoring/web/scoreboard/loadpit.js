function initpit() {
  (function pitWorker() {
    $.ajax({
      url: 'Pit', 
      success: function(result) {
        var current = $('.content').html();
//        console.log(current);
//        console.log(result);
        if(current !== result){
          $('.content').fadeOut();
          $('.content').promise().done(function(){
            $('.content').html(result);
            $('.content').promise().done(function(){
              $('.content').hide().fadeIn();
            });
          });
        }
      },
      complete: function() {
        setTimeout(pitWorker, 5000);
      }
    });
  })();
  
}

