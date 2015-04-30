var MetadataProxy = {

	url : "api/metadata",

	getVersion : function() {
		return $.ajax({
			type : "GET",
			url : this.url + "/version"
		});
	},

	getMessage : function(key) {
		return $.ajax({
			type : "GET",
			url : this.url + "/message/" + key
		});
	},

	getDemoiselleVersion : function() {
		return $.ajax({
			type : "GET",
			url : this.url + "/demoiselle/version"
		});
	}
};
