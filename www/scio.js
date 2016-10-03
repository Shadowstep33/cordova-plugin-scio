/**************** Scio Device JS Interface ***********************

- pair
- connect
- scan
- calibrate
- checkCalibration

**********************************************************/
function scioDevice(){

}
scioDevice.prototype.constructor = scioDevice;

scioDevice.prototype.connect = function(){
	cordova.exec(
		function(winParam) {
			alert("Scio Connected");
		},
		function(error) {
			alert(error + " Scio not connected");
		},
		"ScioDevice",
		"connect",
		[]
	);
};


/**************** Scio Cloud JS Interface ***********************

- analyze

**********************************************************/

module.exports = {
	connect: scioDevice.prototype.connect
};