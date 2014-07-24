$(function() {

	$("#menu").load("menu.html", function() {
		AuthProxy.getUser(getUserOk, getUserFailed);
		$("#logout").on("click", function() {
			sessionStorage.removeItem('credentials');
			location.href = "home.html";
		});
	});

});
