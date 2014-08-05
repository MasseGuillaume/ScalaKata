MathJax.Hub.Config({
    skipStartupTypeset: true,
    messageStyle: "none",
    "HTML-CSS": {
        showMathMenu: false
    }
});
MathJax.Hub.Configured();

app.factory('insightRenderer', function() {
	var widgets = [];

	function apply(cm, cmOptions, insight, code){
		var sep,
        nl = "\n",
        elem,
			  start = cm.getDoc().posFromIndex(insight.start),
			  end = cm.getDoc().posFromIndex(insight.end),
        clearF;

    function addClass(two){
      angular.element(elem)
            .addClass("insight")
            .addClass(two);
    }

    function fold(){
      addClass("fold");
      var widget = cm.foldCode(start, {
        widget: elem,
        rangeFinder: function(){
          return {
            // from: start,
            from: {ch: 0, line: start.line},
            to: end
          };
        }
      });
      clearF = function(){};
    }

    function inline(){
      addClass("inline");
      var widget = cm.addLineWidget(start.line, elem);
      clearF = function(){ widget.clear() };
    }

		switch (insight.renderType) {
			case "html":
				elem = document.createElement("div");
				elem.innerHTML = insight.result;
        fold();
				break;
			case "latex":

        var $script = angular.element("<script type='math/tex'>")
            .html(insight.result);
        var $element = angular.element("<div>");

        $element.append($script);
        elem = $element[0];
        MathJax.Hub.Queue(["Reprocess", MathJax.Hub, elem]);
        fold();

				break;
			case "markdown":
				elem = document.createElement("pre");
				elem.innerHTML = marked.parse(insight.result, {ghf: true});
        fold();
				break;
			case "string":
				elem = document.createElement("span");
        if(insight.result.split(nl).length > 1) {
          sep = '"""';
        } else {
          sep = '"';
        }
				elem.innerText = sep + insight.result + sep;
        inline();
				break;
			case "other":
				elem = document.createElement("span");
				CodeMirror.runMode(insight.result, cmOptions, elem);
        inline();
				break;
		}
    return clearF;
	}
	function clearFun(){
		widgets.forEach(function(w){
			w();
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
