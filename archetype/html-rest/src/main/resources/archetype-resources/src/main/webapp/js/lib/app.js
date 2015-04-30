var App = {

	savedLocationKey : "Saved Location",

	restoreSavedLocation : function() {
		var url = sessionStorage.getItem(this.savedLocationKey);
		location.href = (url ? url : "");
	},

	saveLocation : function(url) {
		sessionStorage.setItem(this.savedLocationKey, url);
	},

	clearSavedLocation : function() {
		sessionStorage.removeItem(this.savedLocationKey);
	},

	getUrlParameterByName : function(name) {
		name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
		var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"), results = regex.exec(location.search);
		return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
	},

	auth : {
		tokenKey : "Token",

		userKey : "User",

		getLoggedInUser : function() {
			return JSON.parse(sessionStorage.getItem(this.userKey));
		},

		setLoggedInUser : function(user) {
			sessionStorage.setItem(this.userKey, JSON.stringify(user));
		},

		isLoggedIn : function() {
			return this.getToken() != null;
		},

		getToken : function() {
			return sessionStorage.getItem(this.tokenKey);
		},

		setToken : function(token) {
			sessionStorage.setItem(this.tokenKey, token);
		},

		clearAuthentication : function() {
			sessionStorage.removeItem(this.userKey);
			sessionStorage.removeItem(this.tokenKey);
		},

		setHeader : function(request) {
			request.setRequestHeader("Authorization", "Token " + this.getToken());
		}
	},

	handling : {
		handle401 : function(request) {
			App.auth.clearAuthentication();
			App.saveLocation(location.href);
			location.href = "login.html";
		},

		handle422 : function(request) {
			var elements = $("form input, form select, form textarea").get().reverse();

			$(elements).each(function() {
				var id = $(this).attr('id');
				var messages = [];

				$.each(request.responseJSON, function(index, value) {
					var aux = value.property ? value.property : "global";

					if (id == aux) {
						messages.push(value.message);
						return;
					}
				});

				if (!id) {
					return;
				}

				var message = $("#" + id.replace(".", "\\.") + "-message");

				if (messages.length > 1) {
					message.empty();
					var ul = message.append("<ul></ul>")

					while (messages.length > 0) {
						ul.append("<li>" + messages.pop() + "</li>");
					}

					message.show();
					$(this).focus();

				} else if (messages.length == 1) {
					message.html(messages.pop()).show();
					$(this).focus();

				} else {
					message.hide();
				}
			});
		},

		handle500 : function(request) {
			alert("Ocorreu um erro interno no servidor e o processamento não foi concluído. Informe ao administrador pelo e-mail: contato@soumaisaventura.com.br");
		}
	}
};

$.ajaxSetup({
	error : function(request) {
		switch (request.status) {
			case 401:
				App.handling.handle401(request);
				break;

			case 422:
				App.handling.handle422(request);
				break;

			case 500:
				App.handling.handle500(request);
				break;
		}
	}
});
