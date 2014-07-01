$(function() {
	AuthProxy.getUser(getUserOk, getUserFailed);
});

function getUserOk(data){
	$("#username").html(data.username);
}

function getUserFailed(request){
	switch (request.status) {
		case 401:
			location.href = "login.html";
			break;
		default:
			console.log(request.statusText);
			break;
	}
}