app.factory('insightRenderer', function() {
	var widgets = [];

	function apply(cm, cmOptions, insight, code){
		var start = cm.getDoc().posFromIndex(insight.start),
			end = cm.getDoc().posFromIndex(insight.end);		// TODO: use it

		start.ch = Infinity;

		var pre = document.createElement("pre");
		pre.className = "cm-s-solarized insight";
		pre.attributes["ng-class"] = "cm-s-{snippets.getThemeShort()}";
	  	CodeMirror.runMode(insight.result, cmOptions, pre);
		cm.addWidget(start, pre, false, "over");
		return {
			clear: function(){ pre.parentElement.removeChild(pre); }
		}
	}
	return {
		clear: function(){
			widgets.forEach(function(w){ 
				w.clear();
			});
			widgets = [];
		},
		render: function(cm, cmOptions, insights, code){
			widgets = insights.map(function(insight){
				return apply(cm, cmOptions, insight, code);
			});
		}
	}
});