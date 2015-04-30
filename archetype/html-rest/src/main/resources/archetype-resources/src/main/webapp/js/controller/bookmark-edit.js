$(function() {
	$("#delete").hide();
	$("#description").focus();

	if (id = App.getUrlParameterByName('id')) {
		BookmarkProxy.load(id).done(loadOk).fail(loadFail);
	}

	MetadataProxy.getDemoiselleVersion().done(function(data) {
		$("#demoiselle-version").html(data);
	});

	$("form").submit(function(event) {
		event.preventDefault();
	});

	$("#save").click(function() {
		var data = {
			description : $("#description").val(),
			link : $("#link").val()
		};

		if (id = $("#id").val()) {
			BookmarkProxy.update(id, data).done(saveOk).fail(saveFail);
		} else {
			BookmarkProxy.insert(data).done(saveOk).fail(saveFail);
		}
	});

	$("#delete").click(function() {
		bootbox.confirm("Tem certeza que deseja apagar?", function(result) {
			if (result) {
				BookmarkProxy.remove([ $("#id").val() ]).done(removeOk);
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

function loadFail(jqXHR) {
	switch (jqXHR.status) {
		case 404:
			bootbox.alert("Você está tentando acessar um registro inexistente!", function() {
				location.href = "bookmark-list.html";
			});
			break;

		default:
			break;
	}
}

function saveOk() {
	location.href = 'bookmark-list.html';
}

function saveFail(jqXHR) {
	switch (jqXHR.status) {
		case 422:
			$($("form input").get().reverse()).each(function() {
				var id = $(this).attr('id');
				var message = null;

				$.each(jqXHR.responseJSON, function(index, value) {
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

function removeOk() {
	location.href = 'bookmark-list.html';
}
