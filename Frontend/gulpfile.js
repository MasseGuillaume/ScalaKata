var gulp = require('gulp'),
    gulpUtil = require('gulp-util'),
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
    request = require('request'),
    usemin = require('gulp-usemin'),
    uglify = require('gulp-uglify'),
    minifyHtml = require('gulp-minify-html'),
    minifyCss = require('gulp-minify-css'),
    rev = require('gulp-rev');

var livereloadport = 35729,
    serverport = 5000;

gulp.task('styles', function() {
    gulp.src('styles/main.less')
        .pipe(less())
        .pipe(gulp.dest('tmp/styles'))
        .pipe(refresh(lrserver));
});

gulp.task('html', function(){
    gulp.src('web/**/*.html')
        .pipe(refresh(lrserver));
});

gulp.task('js2', function(){
    gulp.src('bower_components/codemirror/**/*.js')
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
    // 'install'
    gulp.start('styles', 'serve', 'watch', 'browser');
});

function serveF(assets){
    var server = express(),
        apiUrl = "http://localhost:8080";

    server.use(livereload({port: livereloadport}));
    ['/eval', '/completion', '/typeAt'].forEach(function(u){
        server.use(u, function(req, res) {
            req.pipe(request(apiUrl + u)).pipe(res);
        });
    });

    assets.forEach(function(a){
        server.use(express.static(a));
    });

    server.listen(serverport);
    lrserver.listen(livereloadport);
}

gulp.task('serve', function(){
    serveF(['web', 'bower_components', 'tmp']);
});

gulp.task('watch', function() {
    gulp.watch('bower_components/codemirror/**/*.js', ['js2']);
    gulp.watch('styles/**/*.less', ['styles']);
    gulp.watch('web/**/*.html', ['html']);
    gulp.watch('web/**/*.js', ['js']);
    gulp.watch('bower.json', ['bower']);
    gulp.watch('package.json', ['npm']);
});

gulp.task('build', ['usemin', 'font', 'fav']);
gulp.task('buildServe', ['build', 'serveDist', 'browser']);

gulp.task('serveDist', function(){
    serveF(['dist']);
});

gulp.task('font', function(){
    gulp.src('bower_components/fontawesome/fonts/fontawesome-webfont.woff')
    .pipe(gulp.dest('dist/fonts/'));
})

gulp.task('fav', function(){
    gulp.src('web/favicon.ico')
    .pipe(gulp.dest('dist/'));
})

gulp.task('usemin', function() {
  gulp.src('web/**/*.html')
    .pipe(usemin({
      css: [minifyCss(), 'concat'],
      html: [minifyHtml({empty: true, quotes: true})],
      js: [uglify(), rev()]
    }))
    .pipe(gulp.dest('dist/'));
});