CodeMirror.hack = {};
app.controller('code',["$scope", "$timeout", "LANGUAGE", "VERSION", "scalaEval", "katas", "insightRenderer", "errorsRenderer", "wrap", "webcam",
				 function code( $scope ,  $timeout ,  LANGUAGE ,  VERSION ,  scalaEval ,  katas,   insightRenderer ,  errorsRenderer ,  wrap ,  webcam){

	var cmCode,
			cmPrelude,
			state = {},
			ctrl = CodeMirror.keyMap["default"] == CodeMirror.keyMap.pcDefault ? "Ctrl-" : "Cmd-";

	state.configEditing = false;

	$scope.stateSaved = false;
	function vote(what){
		$scope.code = "md\"Not implemented. [Vote for " + what + "](https://github.com/MasseGuillaume/ScalaKata/issues/47)\"" + "\n" + $scope.code;
		$scope.run();
	}
	$scope.save = function(){
		vote("save");
		$scope.stateSaved = true;
	};
	$scope.fork = function(){
		vote("fork");
	}
	$scope.update = function(){
		vote("update");
	};

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
		keys[ctrl + "Up"] = "focusPrelude";
		keys[ctrl + "Down"] = "focusCode";

		$scope.cmOptions = {
			"_to config codemirror see_": "http://codemirror.net/doc/manual.html#config",
			extraKeys: keys,
			coverGutterNextToScrollbar: true,
			firstLineNumber: 0,
			lineNumbers: false,
			lineWrapping: true,
			tabSize: 2,
			theme: 'solarized dark',
			"_supported_themes": [ "solarized dark", "solarized light", "mdn-like"],
			"_other_themes": ["monokai", "ambiance", "eclipse"],
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
		insightRenderer.clear();
		errorsRenderer.clear();
	}

	function setMode(edit, eval){
		function refresh(cm_, next){
			var bar, parent = cm_.display.wrapper.parentElement;
			if(next) bar = parent.nextElementSibling;
			else bar = parent.previousElementSibling;

			angular.element(bar).on('mouseup', function(){
				cm_.refresh();
				if(!next) run();
			})
		}
		if(edit) {
			state.code = $scope.code;
			clear();
			$timeout(function(){
				$scope.cmOptions.mode = 'application/json';
				$scope.code = JSON.stringify($scope.cmOptions, null, '\t');
			});
		} else {
			$scope.cmOptions.onLoad = function(cm_) {
				refresh(cm_, false);

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
			$scope.cmOptionsPrelude = angular.copy($scope.cmOptions);
			$scope.cmOptionsPrelude.onLoad = function(cm_){
				refresh(cm_, true);

				cmPrelude = cm_;
				CodeMirror.hack.prelude = cm_;
			};

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

			if(eval) {
				$timeout(function(){
					$scope.code = state.code;
					run();
				});
			} else {
				$timeout(function(){
					$scope.code = state.code;
				});
			}
		}
	}
	function setResource(){
		function load(path){
			setMode(false, false);
			katas(path).then(function(r){
				var res = wrap("","").split(r.data);
				$scope.prelude = res[0];
				state.code = res[1];
				setMode(false, true);
				window.history.replaceState({"prelude": res[0], "code": res[1]}, null, path);
			});
		}
		if(window.location.pathname !== "/") {
			load(window.location.pathname);
		} else {
			if(angular.isDefined(window.localStorage['code_' + VERSION]) &&
			   angular.isDefined(window.localStorage['prelude_' + VERSION])){
				state.code = window.localStorage['code_' + VERSION];
				$scope.prelude = window.localStorage['prelude_' + VERSION];
				window.history.replaceState({"prelude": $scope.prelude, "code": state.code}, null, "/");
			} else {
				load("/index");
			}
			setMode(false, true);
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
			$scope.prelude = event.state.prelude;
			$scope.$digest();
			run();
		}
	};

	function run(){
		if(state.configEditing) return;
		if(!angular.isDefined($scope.prelude) && !angular.isDefined($scope.code)) return;

		var w = wrap($scope.prelude, $scope.code);
		scalaEval.insight(w.full).then(function(r){
			var data = r.data;
			var code = $scope.code.split("\n");
			insightRenderer.render(cmCode, w, $scope.cmOptions, data.insight, setResource, $scope.prelude, $scope.code);
			errorsRenderer.render(cmCode, cmPrelude, w, data.infos, data.runtimeError, code);
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
	CodeMirror.commands.focusPrelude = function(){
		cmPrelude.focus();
	};
	CodeMirror.commands.focusCode = function(){
		cmCode.focus();
	}

	$scope.$watch('prelude', function(){
		clear();
		window.localStorage['prelude_' + VERSION] = $scope.prelude;
	});
	$scope.$watch('code', function(){
		if(state.configEditing) {
			try {
				$scope.cmOptions = JSON.parse($scope.code);
				$scope.cmOptionsPrelude = angular.copy($scope.cmOptions);
			} catch(e){}
		} else {
			clear();
			window.localStorage['code_' + VERSION] = $scope.code;
		}
	});

	$scope.run = run;
}]);
