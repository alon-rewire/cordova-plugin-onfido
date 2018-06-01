
var exec = require('cordova/exec');

var PLUGIN_NAME = 'OnFido';

var onFido = {
  scan: function(cb, applicantId) {
    exec(cb, null, PLUGIN_NAME, 'scan', [applicantId]);
  }
};

module.exports = onFido;
