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

	function apply(cmCode, wrap, cmOptions, insight, code){
		var nl = "\n", elem, start, end, clearF;

    start = wrap.fixRange(insight.start, null, cmCode, function(range, cm){
      return cm.getDoc().posFromIndex(range);
    });

    end = wrap.fixRange(insight.end, null, cmCode, function(range, cm){
      return cm.getDoc().posFromIndex(range);
    });

    function addClass(two){
      angular.element(elem)
            .addClass("insight")
            .addClass(two);
    }

    function fold(){
      addClass("fold");
      var widget = cmCode.foldCode(start, {
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
      var widget = cmCode.addLineWidget(end.line, elem);
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
				elem = document.createElement("div");
				elem.innerHTML = marked.parse(insight.result, {ghf: true});
        fold();
				break;
			case "string":
				elem = document.createElement("pre");
				elem.innerText = insight.result;
        inline();
				break;
			case "other":
				elem = document.createElement("pre");
        elem.className = "code";
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
		render: function(cmCode, wrap, cmOptions, insights, code){
			widgets = insights.map(function(insight){
				return apply(cmCode, wrap, cmOptions, insight, code);
			});
		}
	}
});
