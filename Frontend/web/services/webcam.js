app.factory("webcam", ["$q", function($q) {
  // mapping: cameraId -> webcam | background
  return function(mapping){
    var newMapping = $q.defer();

    MediaStreamTrack.getSources(function(sources){
      var camIds,
          requests,
          options = {
            audio: false,
            video: true,
            el: "_",
            extern: null,
            append: true,
            width: 0,
            height: 0,
            mode: "callback",
            onCapture: function () {
                window.webcam.save(); // ???
            }
          };

      camIds =
        _.chain(sources).filter(function(e){
          return e.kind === 'video';
        }).map(function(e){
          return e.id;
        }).value();

      if(!angular.isDefined(mapping)){
        mapping = mapping || _.zipObject(camIds, ["webcam", "background"]);
      }
      newMapping.resolve(mapping);

      requests = _.map(mapping, function(element, camId){
        var ops = angular.copy(options);
        ops.video = {
          mandatory: {
            sourceId: camId
          }
        };
        ops.el = element;

        return ops;
      });

      _.forEach(requests, function(op){
        getUserMedia(op, function(stream){
          var video = op.videoEl;

          var vendorURL = window.URL || window.webkitURL;
          video.src = vendorURL ? vendorURL.createObjectURL(stream) : stream;

          video.onerror = function () {
            stream.stop();
            streamError();
          };

        }, function(){
          console.log("error");
        });
      });
    });

    return newMapping.promise;
  };
}]);
