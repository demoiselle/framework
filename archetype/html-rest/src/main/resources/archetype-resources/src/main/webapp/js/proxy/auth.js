var AuthProxy = {

	url : "api/auth",

	login : function(credentials) {
		return $.ajax({
			url : this.url + "/login",
			type : "POST",
			data : JSON.stringify(credentials),
			contentType : "application/json",
			error : function() {}
		});
	},

	logout : function() {
		return $.ajax({
			url : this.url + "/logout",
			type : "POST",
			beforeSend : function(jqXHR) {
				App.auth.setHeader(jqXHR)
			}
		});
	}
};
