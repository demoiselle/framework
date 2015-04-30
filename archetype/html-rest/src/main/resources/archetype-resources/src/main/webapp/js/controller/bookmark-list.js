$(function() {
	$("#new").focus();
	BookmarkProxy.findAll().done(findAllOk);

	MetadataProxy.getDemoiselleVersion().done(function(data) {
		$("#demoiselle-version").html(data);
	});

	$("form").submit(function(event) {
		event.preventDefault();
	});

	$("#new").click(function() {
		location.href = "bookmark-edit.html";
	});

	$("#delete").click(function() {
		var ids = [];

		$("input:checked").each(function(index, value) {
			ids.push($(value).val());
		});

		if (ids.length == 0) {
			bootbox.alert({
				message : "Nenhum registro selecionado"
			});
		} else {
			bootbox.confirm("Tem certeza que deseja apagar?", function(result) {
				if (result) {
					BookmarkProxy.remove(ids).done(removeOk);
				}
			});
		}
	});
});

function findAllOk(data) {
	$('#resultList').dataTable({
		"aoColumns" : [ {
			"aTargets" : [ 0 ],
			"mDataProp" : "id",
			"mRender" : function(id) {
				return '<input id="remove-' + id + '" type="checkbox" value="' + id + '">';
			}
		}, {
			"aTargets" : [ 1 ],
			"mDataProp" : "description",
			"mRender" : function(data, type, full) {
				return '<a href="bookmark-edit.html?id=' + full.id + '">' + full.description + '</a>';
			}
		}, {
			"aTargets" : [ 2 ],
			"mDataProp" : "link",
			"mRender" : function(link) {
				return '<a href="' + link + '" target="_blank">' + link + '</a>';
			}
		} ],
		"oLanguage" : {
			"sInfo" : "Mostrando _START_ a _END_ de _TOTAL_ registros",
			"sEmptyTable" : "Não há dados disponíveis na tabela",
			"sLengthMenu" : "Mostrar _MENU_ registros",
			"sInfoThousands" : "",
			"oPaginate" : {
				"sFirst" : "Primeiro",
				"sLast" : "Último",
				"sNext" : "Próximo",
				"sPrevious" : "Anterior"
			}
		},
		"bFilter" : false,
		"bDestroy" : true,
		"sPaginationType" : "bs_full",
		"aaData" : data,
		"bSort" : true
	});
}

function removeOk() {
	BookmarkProxy.findAll().done(findAllOk);
}
