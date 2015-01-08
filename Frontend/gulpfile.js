var gulp = require('gulp'),
    plumber = require('gulp-plumber'),
    gulpUtil = require('gulp-util'),
    clean = require('gulp-clean'),
    concat = require('gulp-concat'),
    express = require('express'),
    https = require('https'),
    http = require('http'),
    fs = require('fs'),
    install = require('./plugins/install.js'),
    run = require('./plugins/run.js'),
    less = require('gulp-less'),
    livereload = require('connect-livereload'),
    spawn = require("gulp-spawn"),
    refresh = require('gulp-livereload'),
    gulpFilter = require('gulp-filter'),
    rename = require('gulp-rename'),
    request = require('request'),
    usemin = require('gulp-usemin'),
    uglify = require('gulp-uglify'),
    minifyHtml = require('gulp-minify-html'),
    minifyCss = require('gulp-minify-css'),
    gutil = require('gulp-util'),
    rev = require('gulp-rev');

var i = 0;
var livereloadport = 35729 + i,
    serverport = 5443 + i,
    apiport = 7331 + i,
    certs = {
      // key: fs.readFileSync('key.pem'),
      // cert: fs.readFileSync('cert.pem'),
      // port: livereloadport
    },
    lrserver = require('tiny-lr')()//(certs);

gulp.task('styles', function() {
    gulp.src('styles/main.less')
        .pipe(plumber()) 
        .pipe(less())
        .pipe(gulp.dest('tmp/styles'))
        .pipe(refresh(lrserver));
});

gulp.task('html', function(){
    gulp.src('web/**/*.html')
        .pipe(refresh(lrserver));
});

// gulp.task('js2', function(){
//     gulp.src('bower_components/codemirror/**/*.js')
//         .pipe(refresh(lrserver));
// });

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

gulp.task('npm', function(){
    gulp.src('package.json')
        .pipe(install.npm())
        .pipe(refresh(lrserver));
});

gulp.task('default', function() {
    // 'install'
    gulp.start('styles', 'serve', 'watch');
});

function serveF(assets){
  var server = express();

  request.defaults({
      strictSSL: false, // allow us to use our self-signed cert for testing
      rejectUnauthorized: false
  });

  server.use(livereload({port: livereloadport}));

  assets.forEach(function(a){
      server.use(express.static(a));
  });

  // catch all to api
  server.use(function(req, res) {
    gutil.log(req.originalUrl);
    gutil.log(req.url);

    var isApi = [
      "eval",
      "completion",
      "typeAt",
      "echo"
    ].some(function(v){
      return req.originalUrl == "/" + v
    }) || req.originalUrl.indexOf(".scala") !== -1;

    if(isApi) {
      req.pipe(request("http://localhost:" + apiport + req.originalUrl)).pipe(res);
    } else {
      process.env.NODE_TLS_REJECT_UNAUTHORIZED = "0";
      req.pipe(request("http://localhost:" + serverport)).pipe(res);
    }
  });

  // https.createServer(certs, server).listen(serverport);
  http.createServer(server).listen(serverport);
  lrserver.listen(livereloadport);
}

gulp.task('serve', function(){
    serveF(['web', 'bower_components', 'tmp']);
});

gulp.task('watch', function() {
    // gulp.watch('bower_components/codemirror/**/*.js', ['js2']);
    gulp.watch('styles/**/*.less', ['styles']);
    gulp.watch('web/**/*.html', ['html']);
    gulp.watch('web/**/*.js', ['js']);
    gulp.watch('bower.json', ['bower']);
    gulp.watch('package.json', ['npm']);
});

gulp.task('build', ['styles', 'usemin', 'font', 'mathjax', 'fav', 'zeroclipboard']);
gulp.task('buildServe', ['build', 'serveDist', 'browser']);

gulp.task('serveDist', function(){
    serveF(['out']);
});

// gulp.task('clean', function(){
//   return gulp.src('out/', {read: false})
//     .pipe(clean());
// });

gulp.task('font', function(){
    gulp.src('bower_components/fontawesome/fonts/fontawesome-webfont.woff')
      .pipe(gulp.dest('out/assets/fonts/'));

    gulp.src('bower_components/octicons/octicons/octicons.woff')
      .pipe(gulp.dest('out/assets/styles/'));
});

gulp.task('zeroclipboard', function(){
  gulp.src('bower_components/zeroclipboard/dist/ZeroClipboard.swf')
    .pipe(gulp.dest('out/assets/scripts'));
});

gulp.task('mathjax', function(){

  function cp(from){
    var base = "bower_components/MathJax/";
    return gulp.src( base + from, { "base" : base }).pipe(
      gulp.dest("out/assets/MathJax/")
    );
  }

  cp("MathJax.js");
  cp("extensions/HTML-CSS/*");
  cp("extensions/TeX/*");
  cp("extensions/*.js");
  cp("fonts/HTML-CSS/TeX/woff/*");
  cp("jax/element/**");
  cp("jax/input/TeX/*");
  cp("jax/output/HTML-CSS/**");

});

gulp.task('fav', function(){
    gulp.src('web/assets/favicon.ico')
    .pipe(gulp.dest('out/assets/'));
})

gulp.task('usemin', function() {
  var index = gulpFilter(['**/index.html']);

  gulp.src('web/**/*.html')
    .pipe(usemin({
      css: [minifyCss(), 'concat'],
      html: [minifyHtml({empty: true, quotes: true})],
      js: [uglify(), rev()]
    }))
    .pipe(gulp.dest('out/'))
    .pipe(index)
    .pipe(gulp.dest('out/assets/'))

});
