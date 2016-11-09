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
	},
	getmodels: function(){
		cordova.exec(
			function(winParam) {
				alert(winParam + " got models");
			},
			function(error) {
				alert(error + " models failed");
			},
			"ScioCordova",
			"getmodels",
			[]
		);
	},
	setmodel: function(name){
		cordova.exec(
			function(winParam) {
				console.log(winParam);
			},
			function(error) {
				alert(error + " models failed");
			},
			"ScioCordova",
			"setmodel",
			[name]
		);
	},
	calibrate: function(){
		cordova.exec(
			function(winParam) {
				alert(winParam + "calibrated");
			},
			function(error) {
				alert(error + " calibrate failed");
			},
			"ScioCordova",
			"calibrate",
			[]
		);
	},
	login: function(){
		cordova.exec(
			function(winParam) {
				alert(winParam + " Logged in");
			},
			function(error) {
				alert(error + " login failed");
			},
			"ScioCordova",
			"login",
			[]
		);
	},
	logout: function(){
		cordova.exec(
			function(winParam) {
				alert(winParam + " logout");
			},
			function(error) {
				alert(error + " logout failed");
			},
			"ScioCordova",
			"logout",
			[]
		);
	}
};
