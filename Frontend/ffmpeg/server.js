var express = require('express')
var http = require('http')
var net = require('net');
var child = require('child_process');

var app = express();
var httpServer = http.createServer(app);

app.get('/', function(req, res) {
  var date = new Date();

  res.writeHead(200, {
    'Date':date.toUTCString(),
    'Connection':'close',
    'Cache-Control':'private',
    'Content-Type':'video/webm',
    'Server':'CustomStreamer/0.0.1',
  });

  var tcpServer = net.createServer(function (socket) {
    socket.on('data', function (data) {
      res.write(data);
    });
    socket.on('close', function(had_error) {
      res.end();
    });
  });

  tcpServer.maxConnections = 1;


  tcpServer.listen(function() {
    var cmd = 'gst-launch-0.10';
    var options = null;
    // var args =
    //   [
    //   'v4l2src', 'device=/dev/video0',
    //   '!', 'video/x-raw-yuv,framerate=40/1,width=640,height=480',
    //   '!', 'ffmpegcolorspace',
    //   '!', 'vp8enc,speed=2,max-latency=2,quality=1.0,max-keyframe-distance=3,threads=5',
    //   '!', 'queue2',
    //   '!', 'm.', 'webmmux', 'name=m', 'streamable=true',
    //   '!', 'tcpclientsink', 'host=localhost',
    //   'port='+tcpServer.address().port
    //   ];

var args =
  [
  'v4l2src', 'device=/dev/video0',
  '!', 'video/x-raw-yuv',
  '!', 'ffmpegcolorspace',
  '!', 'tcpclientsink', 'host=localhost',
  'port='+tcpServer.address().port
  ];

    var gstMuxer = child.spawn(cmd, args, options);

    gstMuxer.stderr.on('data', onSpawnError);
    gstMuxer.on('exit', onSpawnExit);

    res.connection.on('close', function() {
      gstMuxer.kill();
    });
  });
});

httpServer.listen(9001);

function onSpawnError(data) {
  console.log(data.toString());
}

function onSpawnExit(code) {
  if (code != null) {
    console.error('GStreamer error, exit code ' + code);
  }
}

process.on('uncaughtException', function(err) {
  console.debug(err);
});
