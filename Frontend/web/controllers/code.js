app.controller('code', function code(
	$scope, $timeout, 
	LANGUAGE, scalaEval, insightRenderer, errorsRenderer){

	var cm, 
		code,
		configEditing = false;

	if(angular.isDefined(window.localStorage['code'])) {
		code = window.localStorage['code'];
	} else {
		code = "";
	}

	// if(angular.isDefined(window.localStorage['codemirror'])) {
	// 	$scope.cmOptions = JSON.parse(window.localStorage['codemirror']);
	// } else {
		$scope.cmOptions = {
			"to config codemirror see": "http://codemirror.net/doc/manual.html#config",
			extraKeys: {
				"Ctrl-Space": "autocomplete",
				"Ctrl-Enter": "run"
			},
			fixedGutter: true,
			coverGutterNextToScrollbar: true,
			lineNumbers: true,
			theme: 'solarized dark',
			themes: [ "solarized dark", "solarized light", "monokai", "ambiance", "eclipse", "mdn-like"],
			smartIndent: false,
			autoCloseBrackets: true,
			styleActiveLine: true,
			keyMap: "sublime",
			highlightSelectionMatches: { showToken: false }
		};
	// }

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
			scalaEval.insight($scope.code).then(function(data){
				var code = $scope.code.split("\n");
				insightRenderer.render(cm, $scope.cmOptions.mode, code, data.insight);
				errorsRenderer.render(cm, data, code);
			});
		}
	}

	CodeMirror.commands.run = run;

	$scope.$watch('code', function(){
		if(configEditing) {
			$scope.cmOptions = JSON.parse($scope.code);	
		} else {
			insightRenderer.clear();
			errorsRenderer.clear();
			window.localStorage['code'] = $scope.code;
		}
	});

	$scope.run = run;
});