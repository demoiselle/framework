$(function() {
	
	$("#delete").hide();

	$("#description").focus();

	$(document).ready(function() {
		if (id = $.url().param('id')) {
			BookmarkProxy.load(id).done(loadOk).fail(loadFailed);
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
			BookmarkProxy.update(id, form).done(saveOk).fail(saveFailed);
		} else {
			BookmarkProxy.insert(form).done(saveOk).fail(saveFailed);
		}
	});

	$("#delete").click(function() {
		bootbox.confirm("Tem certeza que deseja apagar?", function(result) {
			if(result) {
				BookmarkProxy.remove([$("#id").val()]).done(removeOk);
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
			bootbox.alert("Você está tentando acessar um registro inexistente!", function(){
				location.href = "bookmark-list.html";
			});
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
		case 422:
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