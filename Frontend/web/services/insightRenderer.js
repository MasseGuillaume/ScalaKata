MathJax.Hub.Config({
    skipStartupTypeset: true,
    messageStyle: "none",
    "HTML-CSS": {
        showMathMenu: false
    }
});
MathJax.Hub.Configured();

app.directive("mathjaxBind", function() {
    return {
        restrict: "A",
        controller: ["$scope", "$element", "$attrs", function($scope, $element, $attrs) {
            $scope.$watch($attrs.mathjaxBind, function(value) {
                $element.text(value == undefined ? "" : value);
                MathJax.Hub.Queue(["Typeset", MathJax.Hub, $element[0]]);
            });
        }]
    };
});

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
				elem = document.createElement("span");
				elem.innerText = "\\(" + insight.result + "\\)";
				MathJax.Hub.Queue(["Typeset", MathJax.Hub, elem], function(){
					angular.element(elem).removeClass("processing");
				});
				angular.element(elem).addClass("processing");

				break;
			case "markdown":
				elem = document.createElement("pre");
				elem.innerHTML = marked.parse(insight.result, {ghf: true});
				document.body.append(elem);
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

		angular.element(elem).addClass("CodeMirror-activeline-background")
					.addClass("insight");

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
