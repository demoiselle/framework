var BookmarkProxy = {

	url : "api/bookmark",

	findAll : function() {
		return $.ajax({
			type : "GET",
			url : this.url,
			beforeSend : function(request) {
				App.setHeader(request)
			}
		});
	},

	load : function($id) {
		return $.ajax({
			type : "GET",
			url : this.url + "/" + $id,
			beforeSend : function(request) {
				App.setHeader(request)
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
				App.setHeader(request)
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
				App.setHeader(request)
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
				App.setHeader(request)
			}
		});
	}
};
