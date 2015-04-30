$(function() {
	$("#menu").load("menu.html", function() {
		if (App.auth.isLoggedIn()) {
			$("#username").html(App.auth.getLoggedInUser().name);

			$("#logout").click(function(event) {
				event.preventDefault();
				AuthProxy.logout().done(logoutOk);
			});

			$("#logout").parent().show();

		} else {
			$("#login").click(function(event) {
				location.href = "login.html";
			});

			$("#login").parent().show();
		}
	});
});

function logoutOk() {
	App.auth.clearAuthentication();
	location.href = "";
}
