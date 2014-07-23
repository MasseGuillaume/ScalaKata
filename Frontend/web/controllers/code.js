app.controller('code',["$scope", "$timeout", "LANGUAGE", "scalaEval", "insightRenderer", "errorsRenderer",
				 function code( $scope ,  $timeout ,  LANGUAGE ,  scalaEval ,  insightRenderer ,  errorsRenderer){

	var cm,
		state = {},
		ctrl = CodeMirror.keyMap["default"] == CodeMirror.keyMap.pcDefault ? "Ctrl-" : "Cmd-";

	$scope.code = "";
	state.configEditing = false;

	scalaEval.initialCommands().then(function(r){
		if(r.data === "") {
			if(angular.isDefined(window.localStorage['code'])) {
				$scope.code = window.localStorage['code'];
			} else {
				$scope.code = "";
			}
		} else {
			$scope.code = r.data;
		}
	});

	if(angular.isDefined(window.localStorage['codemirror'])) {
		$scope.cmOptions = JSON.parse(window.localStorage['codemirror']);
	} else {
		var keys = {}
		keys[ctrl + "Space"] = "autocomplete";
		keys[ctrl + "Enter"] = "run";
		keys[ctrl + ","] = "config";
		keys[ctrl + "."] = "typeAt";

		$scope.cmOptions = {
			"_to config codemirror see_": "http://codemirror.net/doc/manual.html#config",
			extraKeys: keys,
			fixedGutter: true,
			coverGutterNextToScrollbar: true,
			lineNumbers: false,
			theme: 'solarized dark',
			themes: [ "solarized dark", "solarized light", "monokai", "ambiance", "eclipse", "mdn-like"],
			smartIndent: false,
			multiLineStrings: true,
			autoCloseBrackets: true,
			styleActiveLine: true,
			keyMap: "sublime",
			highlightSelectionMatches: { showToken: false }
		}
	}

	function clear(){
		insightRenderer.clear();
		errorsRenderer.clear();
	}

	function setMode(edit){
		if(edit) {
			state.code = $scope.code;
			insightRenderer.clear();
			errorsRenderer.clear();
			$timeout(function(){
				$scope.cmOptions.mode = 'application/json';
				$scope.code = JSON.stringify($scope.cmOptions, null, '\t');
			});
		} else {
			$scope.cmOptions.onLoad = function(cm_) {
				cm = cm_;
				cm.focus();
				cm.on('changes', function(){
					clear();
				})
			}
			$timeout(function(){
				$scope.cmOptions.mode = 'text/x-' + LANGUAGE;
				$scope.code = state.code;
				window.localStorage['codemirror'] = JSON.stringify($scope.cmOptions);
			});
		}
	}
	setMode(false);

	$scope.toogleEdit = function(){
		state.configEditing = !state.configEditing;
		setMode(state.configEditing);
	};

	function run(){
		if(state.configEditing) return;

		clear();

		scalaEval.insight($scope.code).then(function(r){
			var data = r.data;
			var code = $scope.code.split("\n");
			insightRenderer.render(cm, $scope.cmOptions.mode, data.insight, code);
			errorsRenderer.render(cm, data.infos, data.runtimeError, code);
		});
	}

	CodeMirror.commands.run = run;
	CodeMirror.commands.save = run;
	CodeMirror.commands.config = $scope.toogleEdit;

	$scope.$watch('code', function(){
		if(state.configEditing) {
			try {
				$scope.cmOptions = JSON.parse($scope.code);
			} catch(e){}
		} else {
			clear();
			window.localStorage['code'] = $scope.code;
		}
	});

	$scope.run = run;
}]);
