app.factory('insightRenderer', function() {
	var widgets = [];

	function apply(cm, cmOptions, insight, code){
		var elem,
			start = cm.getDoc().posFromIndex(insight.start),
			end = cm.getDoc().posFromIndex(insight.end);		// TODO: use range

		start.ch = Infinity;

		switch (insight.renderType) {
			case "html":
				elem = document.createElement("div");
				elem.innerHTML = insight.result;
				break;
			case "latex":
				// TODO
				elem = document.createElement("div");
				elem.innerText = insight.result;
				break;
			case "markdown":
				elem = document.createElement("pre");
				elem.innerHTML = marked.parse(insight.result, {ghf: true});
				break;
			case "string":
				elem = document.createElement("div");
				elem.innerText = insight.result;
				break;
			case "other":
				elem = document.createElement("pre");
				CodeMirror.runMode(insight.result, cmOptions, elem);
				break;
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
