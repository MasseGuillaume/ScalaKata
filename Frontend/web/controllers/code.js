CodeMirror.hack = {};
app.controller('code',["$scope", "$timeout", "LANGUAGE", "VERSION", "scalaEval", "katas", "insightRenderer", "errorsRenderer", "wrap", "webcam",
         function code( $scope ,  $timeout ,  LANGUAGE ,  VERSION ,  scalaEval ,  katas,   insightRenderer ,  errorsRenderer ,  wrap ,  webcam){

  var cmCode,
      state = {},
      ctrl = CodeMirror.keyMap["default"] == CodeMirror.keyMap.pcDefault ? "Ctrl-" : "Cmd-";

  state.configEditing = false;

  $scope.state = 'idle';

  if(angular.isDefined(window.localStorage['codemirror_' + VERSION])) {
    $scope.cmOptions = JSON.parse(window.localStorage['codemirror_' + VERSION]);
  } else {

    var keys = {}
    keys[ctrl + "Space"] = "autocomplete";
    keys['.'] = "autocompleteDot";
    keys[ctrl + "Enter"] = "run";
    keys[ctrl + ","] = "config";
    keys[ctrl + "."] = "typeAt";
    keys["F11"] = "fullscreen";

    $scope.cmOptions = {
      "_to config codemirror see_": "http://codemirror.net/doc/manual.html#config",
      extraKeys: keys,
      coverGutterNextToScrollbar: true,
      firstLineNumber: 0,
      lineNumbers: false,
      lineWrapping: true,
      tabSize: 2,
      theme: 'solarized light',
      "_supported_themes": [ "solarized dark", "solarized light", "mdn-like"],
      smartIndent: false,
      multiLineStrings: true,
      matchTags: {bothTags: true},
      autoCloseBrackets: true,
      styleActiveLine: false,
      scrollPastEnd: true,
      keyMap: "sublime",
      mode: 'text/x-' + LANGUAGE,
      highlightSelectionMatches: { showToken: false },
      video: false
    }
  }

  $scope.theme = function(){
    return _.map($scope.cmOptions.theme.split(" "), function(v){
      return "cm-s-" + v;
    }).join(" ");
  }

  function clear(){
    $timeout(function(){
      $scope.state = 'idle';
    });
    insightRenderer.clear();
    errorsRenderer.clear();
  }
  $scope.clear = clear;

  function setMode(edit){
    if(edit) {
      state.code = $scope.code;
      clear();
      $timeout(function(){
        $scope.cmOptions.mode = 'application/json';
        $scope.code = JSON.stringify($scope.cmOptions, null, '\t');
      });
    } else {
      $scope.cmOptions.onLoad = function(cm_) {
        cm_.refresh();

        cmCode = cm_;
        CodeMirror.hack.code = cm_;
        cmCode.focus();
        cmCode.on('changes', function(){
          clear();
        });
        cmCode.on('dblclick', function(){
          clear();
        });
      };

      $scope.cmOptions.mode = 'text/x-' + LANGUAGE;
      
      window.localStorage['codemirror_' + VERSION] = JSON.stringify($scope.cmOptions);

      this.videoSet = this.videoSet || false;
      if($scope.cmOptions.video && !this.videoSet) {
        this.videoSet = true;
        webcam($scope.cmOptions.videoMapping).then(function(newMapping){
          $scope.cmOptions.videoMapping = newMapping;
          window.localStorage['codemirror_' + VERSION] = JSON.stringify($scope.cmOptions);
        })
      }
      $scope.code = state.code;
      $timeout(function(){
        $scope.code = state.code;
      });
    }
  }
  function setResource(){
    function load(path){
      setMode(false, false);
      katas(path).then(function(r){
        state.code = r.data;
        setMode(false, false);
        window.history.replaceState({"code": r.data}, null, path);
      });
    }
    if(window.location.pathname !== "/") {
      load(window.location.pathname);
    } else {
      if(angular.isDefined(window.localStorage['code_' + VERSION])){
        state.code = window.localStorage['code_' + VERSION];
        window.history.replaceState({"code": state.code}, null, "/");
      } else {
        state.code = "";
        window.history.replaceState({"code": state.code}, null, "/");
      }
      setMode(false, false);
    }
  }
  setResource();

  $scope.toogleEdit = function(){
    state.configEditing = !state.configEditing;
    setMode(state.configEditing, false);
  };

  CodeMirror.hack.wrap = wrap;

  window.onpopstate = function(event) {
    if(event.state) {
      $scope.code = event.state.code;
      $scope.$digest();
      run();
    }
  };

  function run(){
    if(state.configEditing) return;
    if(!angular.isDefined($scope.code)) return;

    $scope.state = 'running';

    var w = wrap($scope.code, true);
    scalaEval.insight(w.full).then(function(r){
      var data = r.data;
      var code = $scope.code.split("\n");

      $scope.state = 'viewing';

      insightRenderer.render(cmCode, w, $scope.cmOptions, data.insight, setResource, $scope.code);
      errorsRenderer.render(cmCode, w, data.infos, data.runtimeError, code);
    });
  }

  CodeMirror.commands.run = run;
  CodeMirror.commands.save = run;
  CodeMirror.commands.config = $scope.toogleEdit;

  CodeMirror.commands.fullscreen = function(){
    if(screenfull.enabled) {
      screenfull.toggle();
    }
  }
  
  $scope.$watch('code', function(){
    if(state.configEditing) {
      try {
        $scope.cmOptions = JSON.parse($scope.code);
      } catch(e){}
    } else {
      clear();
      window.localStorage['code_' + VERSION] = $scope.code;
    }
  });

  $scope.run = run;
}]);
