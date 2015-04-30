$(function() {
	$("#menu").load("menu.html", function() {
		$("#username").html(App.auth.getLoggedInUser().name);

		$("#logout").click(function(event) {
			event.preventDefault();
			AuthProxy.logout().done(logoutOk);
		});
	});
});

function logoutOk() {
	App.auth.clearAuthentication();
	location.href = "";
}
