var gulp = require('gulp'),
    clean = require('gulp-clean'),
    concat = require('gulp-concat'),
    express = require('express'),   
    install = require('./plugins/install.js'),
    run = require('./plugins/run.js'),
    less = require('gulp-less'),
    livereload = require('connect-livereload'),
    lrserver = require('tiny-lr')(),
    spawn = require("gulp-spawn"),
    refresh = require('gulp-livereload'),
    rename = require('gulp-rename'),
    request = require('request');

var livereloadport = 35729,
    serverport = 5000;

gulp.task('styles', function() {
    gulp.src('styles/main.less')
        .pipe(less())
        .pipe(gulp.dest('dist/styles'))
        .pipe(refresh(lrserver));
});

gulp.task('html', function(){
    gulp.src('web/**/*.html')
        .pipe(refresh(lrserver));
});

gulp.task('js', function(){
    gulp.src('web/**/*.js')
        .pipe(refresh(lrserver));
});

gulp.task('bower', function(){
    gulp.src('bower.json')
        .pipe(install.bower())
        .pipe(refresh(lrserver));
});

gulp.task('install', ['bower', 'npm']);

gulp.task('browser', function(){
    run("google-chrome", ["localhost:" + serverport]);
});

gulp.task('npm', function(){
    gulp.src('package.json')
        .pipe(install.npm())
        .pipe(refresh(lrserver));
});

gulp.task('default', function() {
    gulp.start('install', 'styles', 'serve', 'watch', 'browser');
});

gulp.task('serve', function(){
    var server = express(),
        apiUrl = "http://localhost:8080";

    server.use(livereload({port: livereloadport}));
    server.use(express.static('web'));
    server.use(express.static('bower_components'));
    server.use(express.static('dist'));

    ['/eval', '/completion'].forEach(function(u){
        server.use(u, function(req, res) {
            req.pipe(request(apiUrl + u)).pipe(res);
        });
    })

    server.listen(serverport);
    lrserver.listen(livereloadport);
});

gulp.task('watch', function() {
    gulp.watch('styles/**/*.less', ['styles']);
    gulp.watch('web/**/*.html', ['html']);
    gulp.watch('web/**/*.js', ['js']);
    gulp.watch('bower.json', ['bower']);
    gulp.watch('package.json', ['npm']);
});