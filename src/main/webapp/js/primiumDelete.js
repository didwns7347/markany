

//구독취소
$(function() {
  $('#true').click(function(){

	  $.ajax({
	
		 url : "/blinkist/user/primiumDelete",
		 type : "post",
		 success : function(data) {

	          swal("success", "구독취소하였습니다.", "success"); 
	          
	          $('.swal-button').click(function(){//팝업창 ok눌렀을때
                     
                           window.location.href=data;

             });
     }
   });
  }); 
});