
var exec = require('cordova/exec');

var PLUGIN_NAME = 'OnFido';

var onFido = {
  echo: function(phrase, cb) {
    exec(cb, null, PLUGIN_NAME, 'echo', [phrase]);
  },
  getDate: function(cb) {
    exec(cb, null, PLUGIN_NAME, 'getDate', []);
  },
  startSdk: function(cb) {
    exec(cb, null, PLUGIN_NAME, 'startSdk', []);
  }
};

module.exports = onFido;
