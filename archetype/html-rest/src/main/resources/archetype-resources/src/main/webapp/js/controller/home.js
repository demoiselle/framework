$(function() {
	MetadataProxy.getDemoiselleVersion().done(getVersionOk);
});

function getVersionOk(data) {
	$("#demoiselle-version").html(data);
}
