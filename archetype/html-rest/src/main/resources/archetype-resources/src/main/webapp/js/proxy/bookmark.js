var BookmarkProxy = {};

BookmarkProxy.url = "api/bookmark";

BookmarkProxy.findAll = function($success, $error) {
	$.ajax({
		type : "GET",
		url : this.url,
		success : $success,
		error : $error
	});
};

BookmarkProxy.load = function($id, $success, $error) {
	$.ajax({
		type : "GET",
		url : this.url + "/" + $id,
		success : $success,
		error : $error
	});
};

BookmarkProxy.insert = function($form, $success, $error) {
	$.ajax({
		type : "POST",
		url : this.url,
		data : JSON.stringify($form),
		contentType : "application/json",
		success : $success,
		error : $error,
		beforeSend : function(xhr) {
			xhr.setRequestHeader("Authorization", AuthProxy.getCredentials());
		}
	});
};

BookmarkProxy.update = function($id, $form, $success, $error) {
	$.ajax({
		type : "PUT",
		url : this.url + "/" + $id,
		data : JSON.stringify($form),
		contentType : "application/json",
		success : $success,
		error : $error,
		beforeSend : function(xhr) {
			xhr.setRequestHeader("Authorization", AuthProxy.getCredentials());
		}
	});
};

BookmarkProxy.remove = function($ids, $success, $error) {
	$.ajax({
		type : "DELETE",
		url : this.url,
		data : JSON.stringify($ids),
		contentType : "application/json",
		success : $success,
		error : $error,
		beforeSend : function(xhr) {
			xhr.setRequestHeader("Authorization", AuthProxy.getCredentials());
		}
	});
};
