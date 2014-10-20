var AuthProxy = {

	url : "api/auth",

	login : function($credentials) {
		return $.ajax({
			url : this.url,
			type : "POST",
			data : JSON.stringify($credentials),
			contentType : "application/json",
			error : function() {}
		});
	},

	getUser : function() {
		return $.ajax({
			url : this.url,
			type : "GET",
			beforeSend : function(request) {
				App.setHeader(request)
			}
		});
	}
};
