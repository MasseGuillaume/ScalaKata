app.controller('code', function code(
	$scope, $timeout, 
	LANGUAGE, scalaEval, insightRenderer, errorsRenderer){

	var cm, 
		code,
		configEditing = false,
		ctrl = CodeMirror.keyMap["default"] == CodeMirror.keyMap.pcDefault ? "Ctrl-" : "Cmd-";

	if(angular.isDefined(window.localStorage['code'])) {
		code = window.localStorage['code'];
	} else {
		code = "";
	}

	// if(angular.isDefined(window.localStorage['codemirror'])) {
	// 	$scope.cmOptions = JSON.parse(window.localStorage['codemirror']);
	// } else {
		var keys = {}
		keys[ctrl + "Space"] = "autocomplete";
		keys[ctrl + "Enter"] = "run";
		keys[ctrl + ","] = "config";

		$scope.cmOptions = {
			"_to config codemirror see_": "http://codemirror.net/doc/manual.html#config",
			extraKeys: keys,
			fixedGutter: true,
			coverGutterNextToScrollbar: true,
			lineNumbers: true,
			theme: 'solarized dark',
			themes: [ "solarized dark", "solarized light", "monokai", "ambiance", "eclipse", "mdn-like"],
			smartIndent: false,
			multiLineStrings: true,
			autoCloseBrackets: true,
			styleActiveLine: true,
			keyMap: "sublime",
			highlightSelectionMatches: { showToken: false }
		};
	// }
	$scope.theme = function(){
		return _.map($scope.cmOptions.theme.split(" "), function(t){
			return "cm-s-" + t;
		}).join(" ");
	}

	function setMode(edit){
		if(edit) {
			code = $scope.code;
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
			}
			$timeout(function(){
				$scope.code = code;
				$scope.cmOptions.mode = 'text/x-' + LANGUAGE;
				window.localStorage['codemirror'] = JSON.stringify($scope.cmOptions);
			});
		}
	}
	setMode(false);

	$scope.toogleEdit = function(){
		configEditing = !configEditing;
		setMode(configEditing);
	};
	
	function run(){
		if(!configEditing) {
			scalaEval.insight($scope.code).then(function(r){
				var data = r.data;
				var code = $scope.code.split("\n");
				insightRenderer.render(cm, $scope.cmOptions.mode, data.insight);
				errorsRenderer.render(cm, data.infos, data.runtimeError, code);
			});
		}
	}

	CodeMirror.commands.run = run;
	CodeMirror.commands.save = run;
	CodeMirror.commands.config = $scope.toogleEdit;

	$scope.$watch('code', function(){
		if(configEditing) {
			try {
				$scope.cmOptions = JSON.parse($scope.code);
			} catch(e){}
		} else {
			insightRenderer.clear();
			errorsRenderer.clear();
			window.localStorage['code'] = $scope.code;
		}
	});

	$scope.run = run;
});