app.factory("webcam", function() {
  var options = {
    audio: false,
    video: true,
    el: "webcam",
    extern: null,
    append: true,
    width: 0,
    height: 0,
    mode: "callback",
    onCapture: function () {
        window.webcam.save(); // ???
    }
  };
  return function(){
    getUserMedia(options, function(stream){
      var video = options.videoEl;

      var vendorURL = window.URL || window.webkitURL;
      video.src = vendorURL ? vendorURL.createObjectURL(stream) : stream;

      video.onerror = function () {
        stream.stop();
        streamError();
      };
    }, function(){
      console.log("error");
    });
  };
});
