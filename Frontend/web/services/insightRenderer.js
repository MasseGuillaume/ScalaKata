app.factory('insightRenderer', function() {
	var widgets = [];

	function apply(cm, cmOptions, insight, code){
		var elem,
			start = cm.getDoc().posFromIndex(insight.start),
			end = cm.getDoc().posFromIndex(insight.end);		// TODO: use range

		start.ch = Infinity;

		if(insight.xml) {
			elem = document.createElement("div");
			elem.innerHTML = insight.result;
		} else {
			// fix overlaping
			elem = document.createElement("pre");
			CodeMirror.runMode(insight.result, cmOptions, elem);	
		}

		elem.className = ["CodeMirror-activeline-background", "insight"].join(" ");
		cm.addWidget(start, elem, false, "over");

		return {
			clear: function(){ elem.parentElement.removeChild(elem); }
		}
	}
	function clearFun(){
		widgets.forEach(function(w){ 
			w.clear();
		});
		widgets = [];
	}
	return {
		clear: clearFun,
		render: function(cm, cmOptions, insights, code){
			widgets = insights.map(function(insight){
				return apply(cm, cmOptions, insight, code);
			});
		}
	}
});