$.ajaxSetup({
	error : function(request) {
		switch (request.status) {
			case 401:
				bootbox.alert("Você não está autenticado!", function() {
					location.href = "login.html";
				});

				break;
		}
	}
});

var App = {

	tokenKey : "Token",

	getToken : function() {
		return sessionStorage.getItem(this.tokenKey);
	},

	setToken : function(token) {
		console.log(token);
		sessionStorage.setItem(this.tokenKey, token);
	},

	setHeader : function(request) {
		request.setRequestHeader("Authorization", "Token " + App.getToken());
	},

	removeToken : function() {
		sessionStorage.removeItem(this.tokenKey);
		$.removeCookie("Token");
	},

	getUrlParameterByName : function(name) {
		name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
		var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"), results = regex.exec(location.search);
		return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
	}
};
