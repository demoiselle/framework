$(function() {
	$("#username").focus();

	MetadataProxy.getDemoiselleVersion().done(function(data) {
		$("#demoiselle-version").html(data);
	});

	$("form").submit(function(event) {
		event.preventDefault();

		$("[id$='-message']").hide();

		var data = {
			'username' : $("#username").val().trim(),
			'password' : $("#password").val().trim()
		};

		AuthProxy.login(data).done(loginOk).fail(loginFail);
	});
});

function make_base_auth(user, password) {
	var tok = user + ':' + password;
	var hash = btoa(tok);
	return hash;
}

function loginOk(data) {
	var username = $("#username").val().trim();
	var password = $("#password").val().trim();
	var encoded = btoa(username + ':' + password);

	App.setToken(encoded);
	location.href = "home.html";
}

function loginFail(request) {
	switch (request.status) {
		case 401:
			$("#global-message").html(request.responseText).show();
			break;

		case 422:
			$($("form input").get().reverse()).each(function() {
				var id = $(this).attr('id');
				var message = null;

				$.each(request.responseJSON, function(index, value) {
					if (id == value.property) {
						message = value.message;
						return;
					}
				});

				if (message) {
					$("#" + id + "-message").html(message).show();
					$(this).focus();
				} else {
					$("#" + id + "-message").hide();

				}
			});
			break;
	}
}
