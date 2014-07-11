var spawn = require('child_process').spawn,
    gutil = require('gulp-util');

module.exports = function (command, args){
    var child = spawn(command, args, {cwd: process.cwd()});
    
    child.stdout.setEncoding('utf8');
    child.stdout.on('data', function (data) {
        gutil.log(data);
    });

    child.stderr.setEncoding('utf8');
    child.stderr.on('data', function (data) {
        gutil.log(gutil.colors.red(data));
        gutil.beep();
    });

    child.on('close', function(code) {
        gutil.log("Done with exit code", code);
    });
}