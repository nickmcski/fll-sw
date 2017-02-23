function init4() {
  (function worker() {
    $.ajax({
      url: 'Last4', 
      success: function(result) {
        var current = $('.score_footer').html();
//        console.log(current);
//        console.log(result);
        if(current !== result){
          $('.entry').fadeOut();
          $('.entry').promise().done(function(){
            $('.score_footer').html(result);
            $('.entry').promise().done(function(){
              $('.entry').hide().fadeIn();
            });
          });
        }
      },
      complete: function() {
        setTimeout(worker, 5000);
      }
    });
  })();
  
}

