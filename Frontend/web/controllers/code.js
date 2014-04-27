app.controller('code', function code(
	$scope, $timeout, 
	LANGUAGE, scalaEval, insightRenderer, errorsRenderer, throttle){

	var cm, 
		code = "",
		configEditing = false;

	$scope.cmOptions = {
		"to config codemirror see": "http://codemirror.net/doc/manual.html#config",
		extraKeys: {"Ctrl-Space": "autocomplete"},
		fixedGutter: true,
		coverGutterNextToScrollbar: true,
		lineNumbers: true,
		theme: 'solarized dark',
		smartIndent: false,
		autoCloseBrackets: true,
		styleActiveLine: true,
		keyMap: "sublime",
		highlightSelectionMatches: { showToken: false },
		onLoad: function(cm_) { 
			cm = cm_;
			cm.focus();
		}
	};

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
			$timeout(function(){
				$scope.code = code;
				$scope.cmOptions.mode = 'text/x-' + LANGUAGE;
			});
		}
	}
	setMode(false);

	$scope.toogleEdit = function(){
		configEditing = !configEditing;
		setMode(configEditing);
	};
	
	$scope.$watch('code', function(){
		if(configEditing) {
			$scope.cmOptions = JSON.parse($scope.code);	
		} else {
			insightRenderer.clear();
			errorsRenderer.clear();
			throttle.event(function() {
				scalaEval.insight($scope.code).then(function(data){
					var code = $scope.code.split("\n");
					insightRenderer.render(cm, $scope.cmOptions.mode, code, data.insight);
					errorsRenderer.render(cm, data, code);
				});
			});
		}
	});
});