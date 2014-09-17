var BookmarkProxy = {

	url : "api/bookmark",

	findAll : function() {
		return $.ajax({
			type : "GET",
			url : this.url,
			beforeSend : function(request) {
				request.setRequestHeader("Authorization", App.getToken());
			}
		});
	},

	load : function($id) {
		return $.ajax({
			type : "GET",
			url : this.url + "/" + $id,
			beforeSend : function(request) {
				request.setRequestHeader("Authorization", App.getToken());
			}
		});
	},

	insert : function($data) {
		return $.ajax({
			type : "POST",
			url : this.url,
			data : JSON.stringify($data),
			contentType : "application/json",
			beforeSend : function(request) {
				request.setRequestHeader("Authorization", App.getToken());
			}
		});
	},

	update : function($id, $data) {
		return $.ajax({
			type : "PUT",
			url : this.url + "/" + $id,
			data : JSON.stringify($data),
			contentType : "application/json",
			beforeSend : function(request) {
				request.setRequestHeader("Authorization", App.getToken());
			}
		});
	},

	remove : function($ids) {
		return $.ajax({
			type : "DELETE",
			url : this.url,
			data : JSON.stringify($ids),
			contentType : "application/json",
			beforeSend : function(request) {
				request.setRequestHeader("Authorization", App.getToken());
			}
		});
	}
};
