$(function() {
	$("#username").focus();

	MetadataProxy.getDemoiselleVersion().done(function(data) {
		$("#demoiselle-version").html(data);
	});

	$("form").submit(function(event) {
		event.preventDefault();

		$("[id$='-message']").hide();

		var credentials = {
			'username' : $("#username").val().trim(),
			'password' : $("#password").val().trim()
		};

		AuthProxy.login(credentials).done(loginOk).fail(loginFail);
	});
});

function loginOk(data, textStatus, jqXHR) {
	App.auth.setToken(jqXHR.getResponseHeader('Set-Token'));
	App.auth.setLoggedInUser(data);

	App.restoreSavedLocation();
}

function loginFail(jqXHR) {
	switch (jqXHR.status) {
		case 401:
			$("#global-message").html(jqXHR.responseText).show();
			break;

		case 422:
			$($("form input").get().reverse()).each(function() {
				var id = $(this).attr('id');
				var message = null;

				$.each(jqXHR.responseJSON, function(index, value) {
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
