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
	}
};
