
function ScioCordova(){
	var self = this;
	
	this.cp_models = null;
	this.models = null;
	this.devices = [];
	this.selected_model = "";
	this.last_result = "";
	this.collections = {};
	
	this.connect = function(cb, err){
		cordova.exec(
			function(winParam) {
				if(typeof cb != "undefined")
				if(cb)
					cb(winParam);
			},
			function(error) {
				if(typeof err != "undefined")
				if(err)
					err(error);
			},
			"ScioCordova",
			"connect",
			[]
		);
	},
	this.scanBLE = function(cb, err){
		cordova.exec(
			function(winParam) {
				alert(winParam + "Scio Scanned");
				self.devices = winParam;
				
				if(typeof cb != "undefined")
				if(cb)
					cb(winParam);
			},
			function(error) {
				if(typeof err != "undefined")
				if(err)
					err(error);
			},
			"ScioCordova",
			"scanble",
			[]
		);
	},
	this.scan = function(cb, err){
		cordova.exec(
			function(winParam) {
				console.log(winParam);
				
				self.last_result = winParam;
				if(typeof cb != "undefined")
				if(cb)
					cb(winParam);
			},
			function(error) {
				if(typeof err != "undefined")
				if(err)
					err(error);
			},
			"ScioCordova",
			"scan",
			[]
		);
	},
	this.getmodels = function(cb, err){
		cordova.exec(
			function(winParam) {
				console.log(winParam);
				self.models = JSON.parse(winParam);
				
				//Group models into "collections"
				for(var m in self.models){
					var model = self.models[m];
					var split = model.name.split("_");
					
					if(typeof self.collections[split[0]] == "undefined")
						self.collections[split[0]] = {
							models: [model.index],
							name: split[0]
						};
					else
						self.collections[split[0]].models.push(model.index);
				}
				
				if(typeof cb != "undefined")
				if(cb)
					cb(winParam);
			},
			function(error) {
				if(typeof err != "undefined")
				if(err)
					err(error);
			},
			"ScioCordova",
			"getmodels",
			[]
		);
	},
	this.getcpmodels = function(cb, err){
		cordova.exec(
			function(winParam) {
				console.log(winParam);
				self.cp_models = JSON.parse(winParam);
				
				//Group models into "collections"
				for(var m in self.cp_models){
					var model = self.cp_models[m];
					var split = model.name.split("_");
					
					if(typeof self.collections[split[0]] == "undefined")
						self.collections[split[0]] = {
							models: [model.index],
							name: split[0]
						};
					else
						self.collections[split[0]].models.push(model.index);
				}
				
				if(typeof cb != "undefined")
				if(cb)
					cb(winParam);
			},
			function(error) {
				if(typeof err != "undefined")
				if(err)
					err(error);
			},
			"ScioCordova",
			"getcpmodels",
			[]
		);
	},
	this.setmodel = function(name, err){
		cordova.exec(
			function(winParam) {
				//self.selected_model = name;
				
				console.log(winParam);
			},
			function(error) {
				if(typeof err != "undefined")
				if(err)
					err(error);
			},
			"ScioCordova",
			"setmodel",
			[name]
		);
	},
	this.setmodels = function(models, cb, err){
		cordova.exec(
			function(winParam) {
				//self.selected_model = name;
				console.log("Set models to "+models);
				if(typeof cb != "undefined")
				if(cb)
					cb(winParam);
			},
			function(error) {
				if(typeof err != "undefined")
				if(err)
					err(error);
			},
			"ScioCordova",
			"setmodels",
			[models]
		);
	},
	this.calibrate = function(cb, err){
		cordova.exec(
			function(winParam) {
				if(typeof cb != "undefined")
				if(cb)
					cb(winParam);
			},
			function(error) {
				if(typeof err != "undefined")
				if(err)
					err(error);
			},
			"ScioCordova",
			"calibrate",
			[]
		);
	},
	this.login = function(cb, err){
		cordova.exec(
			function(winParam) {
				if(typeof cb != "undefined")
				if(cb)
					cb(winParam);
			},
			function(error) {
				if(typeof err != "undefined")
				if(err)
					err(error);
			},
			"ScioCordova",
			"login",
			[]
		);
	},
	this.logout = function(cb, err){
		cordova.exec(
			function(winParam) {
				if(typeof cb != "undefined")
				if(cb)
					cb(winParam);
			},
			function(error) {
				if(typeof err != "undefined")
				if(err)
					err(error);
			},
			"ScioCordova",
			"logout",
			[]
		);
	},
	this.isConnected = function(cb, err){
		cordova.exec(
			function(winParam) {
				if(typeof cb != "undefined")
				if(cb)
					cb(winParam);
			},
			function(error) {
				if(typeof err != "undefined")
				if(err)
					err(error);
			},
			"ScioCordova",
			"isconnected",
			[]
		);
	}
};

module.exports = (new ScioCordova());
