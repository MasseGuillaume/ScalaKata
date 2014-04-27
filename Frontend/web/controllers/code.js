app.controller('code', function code($scope, $timeout, LANGUAGE){
	var cm, 
		code = "",
		editing = false;

	$scope.cmOptions = {
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
		editing = !editing;
		setMode(editing);
	};
	
	$scope.$watch('code', function(){
		if(editing) {
			$scope.cmOptions = JSON.parse($scope.code);	
		}
	});
});