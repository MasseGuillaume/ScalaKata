var run = require('./run.js'),
	es = require('event-stream');

exports.bower = function(){
	return es.map(function(){
    	return run("bower", ["install"]);
    });
}
exports.npm = function(){
	return es.map(function(){
    	return run("npm", ["install"]);
    });
}