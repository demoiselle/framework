function getUserOk(data) {
	$("#username").html(data.username);
}

function getUserFailed(request) {
	switch (request.status) {
		case 401:
			location.href = "login.html";
			break;
		default:
			console.log(request.statusText);
			break;
	}
}

function logoutOk(data) {
	sessionStorage.removeItem('credentials');
	location.href = "home.html";
}

function logoutFailed(data) {
	console.log('Falha no logout');
	console.log(data);
}
