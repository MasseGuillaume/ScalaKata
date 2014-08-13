CodeMirror.hack = {};
app.controller('code',["$scope", "$timeout", "LANGUAGE", "scalaEval", "insightRenderer", "errorsRenderer",
				 function code( $scope ,  $timeout ,  LANGUAGE ,  scalaEval ,  insightRenderer ,  errorsRenderer){

	var cmCode,
			cmPrelude,
			state = {},
			ctrl = CodeMirror.keyMap["default"] == CodeMirror.keyMap.pcDefault ? "Ctrl-" : "Cmd-";

	state.configEditing = false;

	if(angular.isDefined(window.localStorage['code'])){
		state.code = window.localStorage['code'];
	}
	if(angular.isDefined(window.localStorage['prelude'])){
		$scope.prelude = window.localStorage['prelude'];
	}
	// if(angular.isDefined(window.localStorage['codemirror'])) {
	// 	$scope.cmOptions = JSON.parse(window.localStorage['codemirror']);
	// } else {

		var keys = {}
		keys[ctrl + "Space"] = "autocomplete";
		keys['.'] = "autocompleteDot";
		keys[ctrl + "Enter"] = "run";
		keys[ctrl + ","] = "config";
		keys[ctrl + "."] = "typeAt";
		keys["F11"] = "fullscreen";
		keys[ctrl + "Up"] = "focusPrelude";
		keys[ctrl + "Down"] = "focusCode";

		CodeMirror.commands.focusPrelude = function(){
			cmPrelude.focus();
		};
		CodeMirror.commands.focusCode = function(){
			cmCode.focus();
		}

		$scope.cmOptions = {
			"_to config codemirror see_": "http://codemirror.net/doc/manual.html#config",
			extraKeys: keys,
			coverGutterNextToScrollbar: true,
			firstLineNumber: 0,
			lineNumbers: false,
			theme: 'mdn-like',
			"_themes": [ "solarized dark", "solarized light", "monokai", "ambiance", "eclipse", "mdn-like"],
			smartIndent: false,
			multiLineStrings: true,
			autoCloseBrackets: true,
			styleActiveLine: false,
			keyMap: "sublime",
			highlightSelectionMatches: { showToken: false }
		}
		$scope.cmOptionsPrelude = angular.copy($scope.cmOptions);
		$scope.cmOptionsPrelude.onLoad = function(cm_){
			cmPrelude = cm_;
			CodeMirror.hack.prelude = cm_;
		};
	// }

	function clear(){
		insightRenderer.clear();
		errorsRenderer.clear();
	}

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
				cmCode = cm_;
				CodeMirror.hack.code = cm_;
				cmCode.focus();
				cmCode.on('changes', function(){
					clear();
				});
			};

			$scope.cmOptions.mode = 'text/x-' + LANGUAGE;
			window.localStorage['codemirror'] = JSON.stringify($scope.cmOptions);

			$timeout(function(){
				$scope.code = state.code;
			});
		}
	}
	setMode(false);

	$scope.toogleEdit = function(){
		state.configEditing = !state.configEditing;
		setMode(state.configEditing);
	};

	function wrap(prelude_, code_){
		var import_ = "import com.scalakata.eval._",
				macroBegin = "@ScalaKata object A {",
				macroClose = "}",
				nl = "\n",
				prelude = prelude_.split(nl),
				beforeCode = prelude.concat([import_, macroBegin]),
				beforeCodeLength = beforeCode.join(nl).length + 1,
				code = code_.split(nl);

		return {
			split: function(full){

			},
			codeOffset: function(){
				return beforeCodeLength;
			},
			fixRange: function(range, cmPrelude, cmCode, apply) {
				if(range < prelude_.length) return apply(range, cmPrelude);
				else return apply(range - beforeCodeLength, cmCode);
			},
			fixLine: function(line, cmPrelude, cmCode, apply) {
				if(line < prelude.length) return apply(line, cmPrelude);
				else return apply(line - beforeCode.length, cmCode);
			},
			full: beforeCode.concat([
				code_,
				macroClose
			]).join(nl)
		}
	}
	CodeMirror.hack.wrap = wrap;

	function run(){
		if(state.configEditing) return;

		var w = wrap($scope.prelude, $scope.code);
		scalaEval.insight(w.full).then(function(r){
			var data = r.data;
			var code = $scope.code.split("\n");
			insightRenderer.render(cmCode, w, $scope.cmOptions.mode, data.insight, code);
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

	$scope.theme = function(){
		return cmCode.options.theme.split(" ").map(function(v){
			return "cm-s-" + v;
		}).join(" ");
	}

	$scope.$watch('prelude', function(){
		clear();
		window.localStorage['prelude'] = $scope.prelude;
	});
	$scope.$watch('code', function(){
		if(state.configEditing) {
			try { $scope.cmOptions = JSON.parse($scope.code); } catch(e){}
		} else {
			clear();
			window.localStorage['code'] = $scope.code;
		}
	});

	$scope.run = run;
}]);
