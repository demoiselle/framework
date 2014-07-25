$(function() {
	
	$("#menu").load("menu.html", function() {
		AuthProxy.getUser(getUserOk, getUserFailed);
		$("#logout").on("click", function() {
			sessionStorage.removeItem('credentials');
			location.href = "home.html";
		});
	});
	
	$("#delete").hide();
	$("#description").focus();

	$(document).ready(function() {
		if (id = $.url().param('id')) {
			BookmarkProxy.load(id, loadOk, loadFailed);
		}
	});

	$("form").submit(function(event) {
		event.preventDefault();
	});

	$("#save").click(function() {
		var form = {
			description : $("#description").val(),
			link : $("#link").val()
		};

		if (id = $("#id").val()) {
			BookmarkProxy.update(id, form, saveOk, saveFailed);
		} else {
			BookmarkProxy.insert(form, saveOk, saveFailed);
		}
	});

	$("#delete").click(function() {
		bootbox.confirm("Tem certeza que deseja apagar?", function(result) {
			if(result) {
				BookmarkProxy.remove([$("#id").val()], removeOk, removeFailed);
			}
		}); 
	});

	$("#back").click(function() {
		history.back();
	});
});

function loadOk(data) {
	$("#id-row").show();
	$("#id-text").html(data.id);
	$("#id").val(data.id);
	$("#description").val(data.description);
	$("#link").val(data.link);
	$("#delete").show();
}

function loadFailed(request) {
	switch (request.status) {
		case 404:
			alert('Você está tentando acessar um registro inexistente.\r\nVocê será redirecionado.')
			location.href = "bookmark-list.html";
			break;

		default:
			break;
	}
}

function saveOk(data) {
	location.href = 'bookmark-list.html';
}

function saveFailed(request) {
	switch (request.status) {
		case 401:
			alert('Você não está autenticado.');
			break;

		case 412:
			$($("form input").get().reverse()).each(function() {
				var id = $(this).attr('id');
				var message = null;

				$.each(request.responseJSON, function(index, value) {
					if (id == value.property) {
						message = value.message;
						return;
					}
				});

				if (message) {
					$("#" + id + "-message").html(message).show();
					$(this).focus();
				} else {
					$("#" + id + "-message").hide();
				}
			});
			break;

		default:
			break;
	}
}

function removeOk(data) {
	location.href = 'bookmark-list.html';
}

function removeFailed(request) {
	switch (request.status) {
		case 401:
			alert('Você não está autenticado.');
			break;

		default:
			break;
	}
}