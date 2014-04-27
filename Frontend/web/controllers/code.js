app.controller('code', function code($scope, LANGUAGE){
	$scope.code = "1+1";
	$scope.cmOptions = {
		extraKeys: {"Ctrl-Space": "autocomplete"},
		fixedGutter: true,
		coverGutterNextToScrollbar: true,
		lineNumbers: true,
		mode: 'text/x-' + LANGUAGE,
		theme: 'solarized dark',
		smartIndent: false,
		autofocus: true,
		autoCloseBrackets: true,
		styleActiveLine: true,
		keyMap: "sublime",
		highlightSelectionMatches: { showToken: false },
		onLoad: function(cm_) { cm = cm_; }
	};
});