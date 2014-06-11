var AuthProxy = {};

AuthProxy.url = "api/auth";

AuthProxy.login = function($credentials, $success, $error) {
	$.ajax({
		url : this.url,
		type : "POST",
		data : JSON.stringify($credentials),
		contentType : "application/json",
		success : $success,
		error : $error
	});
};

AuthProxy.logout = function($success, $error) {
	$.ajax({
		url : this.url,
		type : "DELETE",
		success : $success,
		error : $error
	});
};

AuthProxy.getUser = function($success, $error) {
	$.ajax({
		url : this.url,
		type : "GET",
		success : $success,
		error : $error
	});
};

AuthProxy.getCredential = function(){
	return sessionStorage.getItem('credential');
}