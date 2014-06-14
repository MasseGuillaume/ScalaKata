app.factory('insightRenderer', function() {
	var widgets = [];

	function renderCode(cm, cmMode, code, insight){
		var currentLine = code[insight.line - 1];
		var pre = document.createElement("pre");
		pre.className = "cm-s-solarized insight";
		pre.attributes["ng-class"] = "cm-s-{snippets.getThemeShort()}";
	  	CodeMirror.runMode(insight.result, cmMode, pre);
		cm.addWidget({line: (insight.line - 1), ch: currentLine.length}, pre, false, "over");
		return {
			clear: function(){ pre.parentElement.removeChild(pre); }
		}
	}

	function apply(cm, cmOptions, code, insight){
		if(insight.type == "CODE") {
			return renderCode(cm, cmOptions, code, insight);
		}
	}
	return {
		clear: function(){
			// clear insight
			widgets.forEach(function(w){ 
				w.clear();
			});
			widgets = [];
		},
		render: function(cm, cmOptions, code, insights){
			widgets = insights.map(function(insight){
				return apply(cm, cmOptions, code, insight);
			});
		}
	}
});