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
		keys['.'] = "autocompleteDot";
		keys[ctrl + "Enter"] = "run";
		keys[ctrl + ","] = "config";
		keys[ctrl + "."] = "typeAt";

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
				var prelude, code, impr, instr1, instr2, readOnlyLines, instrumentationDelimiter, lines, cursor, nl = "\n";

				if(angular.isDefined(window.localStorage['code'])) {
					code = window.localStorage['code'];
					prelude = window.localStorage['prelude'];
				} else {
					prelude = r.data.prelude;
					code = r.data.code;
				}

				impr = [
					"import com.scalakata.eval._;",
					"",
				].join(nl);
				readOnlyLines = _.map(impr.split(nl), function(v, i){
					return i;
				});

				instrumentationDelimiter = _.last(readOnlyLines) + 1 + prelude.split(nl).length;

				instr1 = [
					"",
					"@ScalaKata object A{",
					""
				].join(nl);
				readOnlyLines = readOnlyLines.concat(_.map(instr1.split(nl), function(v, i){
					return i + instrumentationDelimiter;
				}));

				cursor = _.last(readOnlyLines) + 1;

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

				lines = state.code.split(nl);

				$scope.cmOptions.mode = 'text/x-' + LANGUAGE;
				window.localStorage['codemirror'] = JSON.stringify($scope.cmOptions);

				$scope.code = state.code;

				readOnlyLines = _.filter(readOnlyLines, function(v){ return lines[v] !== ""; });

				$timeout(function(){
					state.markers = _.map(readOnlyLines, function(i){
						return cm.markText(
								{ line: i, ch: 0},
								{ line: i, ch: lines[i].length},
								{
									readOnly: true,
									className: "macroAnnotation"
								}
							);
					});
					cm.setCursor({
						line: cursor,
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
			if(!angular.isDefined(state.markers)) return;

			var imports,
					instrBegin,
					instrEnd,
					codes,
					preludes,
					nl = "\n",
					lines = $scope.code.split(nl),
					pos = _.map(state.markers, function(m){ return m.find().from.line; });

			imports = pos[0];
			instrBegin = pos[1];
			instrEnd = pos[2];

			preludes = _.filter(lines, function(l, i){
				// [0, imports[ && ]imports, instrBegin[ && ]instrEnd, end]
				return i < imports ||
					 		 imports < i && i < instrBegin ||
					 		 instrEnd < i;
			}).join(nl);

			codes = _.filter(lines, function(l, i){
				return instrBegin < i && i < instrEnd;
			}).join(nl);

			window.localStorage['prelude'] = preludes;
			window.localStorage['code'] = codes;
		}
	});

	$scope.run = run;
}]);
