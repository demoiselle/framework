$(function() {
	$("#username").focus();

	$("form").submit(function(event) {
		event.preventDefault();
		
		$("[id$='-message']").hide();
		
		var form = {
			'username' : $("#username").val().trim(),
			'password' : $("#password").val().trim()
		};
		
		AuthProxy.login(form).done(loginOk).fail(loginFail);
	});
});

function make_base_auth(user, password) {
	var tok = user + ':' + password;
	var hash = btoa(tok);
	return "Basic " + hash;
}

function loginOk(data) {
	App.setToken(make_base_auth($("#username").val().trim(), $("#password").val().trim()));
	location.href = "home.html";
}

function loginFail(request) {
	switch (request.status) {
		case 401:
			$("#global-message").html("Usuário ou senha inválidos.").show();
			break;

		case 412:
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