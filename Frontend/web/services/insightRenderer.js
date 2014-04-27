app.factory('insightRenderer', function() {
	var widgets = [];
	return {
		clear: function(){
			// clear insight
			widgets.forEach(function(w){ 
				w.clear();
			});
			widgets = [];
		},
		render: function(cm, cmOptions, code, insights){
			if(insights == "") return;
			
			widgets = insights.split('\n').map(function(insight, i){
				var currentLine = code[i];
				var pre = document.createElement("pre");
				pre.className = "insight"; //theme
				CodeMirror.runMode(insight, cmOptions.mode, pre);
				cm.addWidget({line: i, ch: currentLine.length}, pre, false, "over");
				return {
					clear: function(){ pre.parentElement.removeChild(pre); }
				}
			});
		}
	}
});