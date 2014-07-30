var AuthProxy = {

	url : "api/auth",

	login : function($credentials) {
		return $.ajax({
			url : this.url,
			type : "POST",
			data : JSON.stringify($credentials),
			contentType : "application/json",
			error: function(){ } 
		});
	},

	logout : function() {
		return $.ajax({
			url : this.url,
			type : "DELETE",
			beforeSend : function(request) {
				request.setRequestHeader("Authorization", App.getToken());
			}
		});
	},

	getUser : function() {
		return $.ajax({
			url : this.url,
			type : "GET",
			beforeSend : function(request) {
				request.setRequestHeader("Authorization", App.getToken());
			}
		});
	}

}