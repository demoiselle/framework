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
		sessionStorage.setItem(this.tokenKey, token);
	},

	setHeader : function(request) {
		request.setRequestHeader("Authorization", "Basic " + App.getToken());
	},

	removeToken : function() {
		sessionStorage.removeItem(this.tokenKey);
	}
};
