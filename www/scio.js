/*global cordova*/
module.exports = {
	connect: function(){
		cordova.exec(
			function(winParam) {
				alert("Scio Connected");
			},
			function(error) {
				alert(error + " Scio not connected");
			},
			"ScioCordova",
			"connect",
			[]
		);
	},
	scanBLE: function(){
		cordova.exec(
			function(winParam) {
				alert(winParam + "Scio Scanned");
			},
			function(error) {
				alert(error + " scan failed");
			},
			"ScioCordova",
			"scanble",
			[]
		);
	},
	scan: function(){
		cordova.exec(
			function(winParam) {
				alert(winParam + "Scio Material Scanned");
			},
			function(error) {
				alert(error + " material scan failed");
			},
			"ScioCordova",
			"scan",
			[]
		);
	}
};
