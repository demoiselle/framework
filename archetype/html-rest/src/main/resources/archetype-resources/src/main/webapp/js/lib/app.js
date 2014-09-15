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

	tokenKey : "credentials",

	getToken : function() {
		return sessionStorage.getItem(this.tokenKey);
	},

	setToken : function(token) {
		sessionStorage.setItem(this.tokenKey, token);
	},

	removeToken : function() {
		sessionStorage.removeItem(this.tokenKey);
	}
};
