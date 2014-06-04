$(function() {

	$("#username").focus();

	$("form").submit(function(event) {
		event.preventDefault();
		$("[id$='-message']").hide();
		var form = {
			'username' : $("#username").val().trim(),
			'password' : $("#password").val().trim()
		};

		AuthProxy.login(form, loginOk, loginFail);
	});

});

// Funções de Callback

function loginOk(data) {
	location.href = "pendencies.html";
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
