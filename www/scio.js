/*global cordova*/

function ScioCordova(){
	var self = this;
	
	this.cp_models = null;
	this.models = null;
	this.devices = [];
	this.selected_model = "";
	this.last_result = "";
	
	this.connect = function(cb){
		cordova.exec(
			function(winParam) {
				if(typeof cb != "undefined")
				if(cb)
					cb(winParam);
			},
			function(error) {
				alert(error + " Scio not connected");
			},
			"ScioCordova",
			"connect",
			[]
		);
	},
	this.scanBLE = function(cb){
		cordova.exec(
			function(winParam) {
				alert(winParam + "Scio Scanned");
				self.devices = winParam;
				
				if(typeof cb != "undefined")
				if(cb)
					cb(winParam);
			},
			function(error) {
				alert(error + " scan failed");
			},
			"ScioCordova",
			"scanble",
			[]
		);
	},
	this.scan = function(cb){
		cordova.exec(
			function(winParam) {
				console.log(winParam);
				
				self.last_result = winParam;
				if(typeof cb != "undefined")
				if(cb)
					cb(winParam);
			},
			function(error) {
				alert(error + " material scan failed");
			},
			"ScioCordova",
			"scan",
			[]
		);
	},
	this.getmodels = function(){
		cordova.exec(
			function(winParam) {
				console.log(winParam);
				self.models = JSON.parse(winParam);
			},
			function(error) {
				alert(error + " models failed");
			},
			"ScioCordova",
			"getmodels",
			[]
		);
	},
	this.getcpmodels = function(){
		cordova.exec(
			function(winParam) {
				console.log(winParam);
				self.cp_models = JSON.parse(winParam);
			},
			function(error) {
				alert(error + " models failed");
			},
			"ScioCordova",
			"getcpmodels",
			[]
		);
	},
	this.setmodel = function(name){
		cordova.exec(
			function(winParam) {
				self.selected_model = name;
				
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
	this.calibrate = function(){
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
	this.login = function(){
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
	this.logout = function(){
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

module.exports = (new ScioCordova());
