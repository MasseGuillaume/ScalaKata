app.controller('code',["$scope", "$timeout", "LANGUAGE", "scalaEval", "insightRenderer", "errorsRenderer",
				 function code( $scope ,  $timeout ,  LANGUAGE ,  scalaEval ,  insightRenderer ,  errorsRenderer){

	var cm,
		state = {},
		ctrl = CodeMirror.keyMap["default"] == CodeMirror.keyMap.pcDefault ? "Ctrl-" : "Cmd-";

	state.configEditing = false;

	// if(angular.isDefined(window.localStorage['codemirror'])) {
	// 	$scope.cmOptions = JSON.parse(window.localStorage['codemirror']);
	// } else {

		var keys = {}
		keys[ctrl + "Space"] = "autocomplete";
		keys[ctrl + "Enter"] = "run";
		keys[ctrl + ","] = "config";
		keys[ctrl + "."] = "typeAt";
		keys['.'] = function (cm){
			cm.replaceSelection(".");
			cm.execCommand("autocomplete");
		};

		$scope.cmOptions = {
			"_to config codemirror see_": "http://codemirror.net/doc/manual.html#config",
			extraKeys: keys,
			fixedGutter: true,
			coverGutterNextToScrollbar: true,
			firstLineNumber: 0,
			lineNumbers: true,
			theme: 'solarized dark',
			themes: [ "solarized dark", "solarized light", "monokai", "ambiance", "eclipse", "mdn-like"],
			smartIndent: false,
			multiLineStrings: true,
			autoCloseBrackets: true,
			styleActiveLine: true,
			keyMap: "sublime",
			highlightSelectionMatches: { showToken: false }
		}
	// }

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
				});
			}

			scalaEval.initialCode().then(function(r){
				// if(angular.isDefined(window.localStorage['code'])) {
				// 	$scope.code = window.localStorage['code'];
				// }
				// or r.data

				var prelude, code, impr, instr1, instr2, readOnlyLines, nl = "\n";

				impr = [
					"import com.scalakata.eval._;",
					"",
				].join(nl);
				readOnlyLines = _.map(impr.split(nl), function(v, i){ return i; });

				prelude = [
					"class Meter(val v: Int) extends AnyVal {",
					"	def +(m: Meter) = new Meter(v + m.v)",
					"}"
				].join(nl);

				instr1 = [
					"",
					"@ScalaKata object A{",
					""
				].join(nl);
				readOnlyLines = readOnlyLines.concat(_.map(instr1.split(nl), function(v, i){
					return i + _.last(readOnlyLines) + prelude.split(nl).length + 1;
				}));

				code = [
					"List(1, 2)",
					"new Meter(1) + new Meter(2)"
				].join(nl);

				instr2 = [
					"",
					"}"
				].join(nl);
				readOnlyLines = readOnlyLines.concat(_.map(instr2.split(nl), function(v, i){
					return i + _.last(readOnlyLines) + code.split(nl).length + 1;
				}));

				state.code = [
					impr,
					prelude,
					instr1,
					code,
					instr2
				].join(nl);

				$scope.cmOptions.mode = 'text/x-' + LANGUAGE;
				window.localStorage['codemirror'] = JSON.stringify($scope.cmOptions);

				$scope.code = state.code;

				$timeout(function(){
					_.forEach(readOnlyLines, function(i){
							cm.markText(
								{ line: i, ch: 0},
								{ line: i, ch: Infinity},
								{
									readOnly: true,
									className: "macroAnnotation"
								}
							);
					});
					cm.setCursor({
						line: 3,
						ch: 0
					});
				});
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
			// TODO split prelud & code
			// window.localStorage['code'] = $scope.code;
		}
	});

	$scope.run = run;
}]);
