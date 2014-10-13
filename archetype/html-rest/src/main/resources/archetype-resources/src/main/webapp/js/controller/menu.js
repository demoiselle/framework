$(function() {
	$("#menu").load("menu.html", function() {
		AuthProxy.getUser().done(getUserOk);

		$("#logout").on("click", function() {
			App.removeToken();
			location.href = "index.html";
		});
	});
});

function getUserOk(data) {
	$("#username").html(data.name);
}
