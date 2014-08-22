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

	function apply(cmCode, wrap, cmOptions, insight){
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
      var range = cmCode.markText({ch: 0, line: start.line}, end, {
        replacedWith: elem
      });
      clearF = function(){
        range.clear();
      };
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
        var markdown = marked.parse(insight.result, {
          ghf: true,
          highlight: function (code, lang) {
            var elem = document.createElement("div"),
                option = angular.copy(cmOptions),
                langs = {
                  "scala": "text/x-scala",
                  "json": "application/json"
                };
            option.mode = langs[lang] || "";

            CodeMirror.runMode(code, option, elem);
            return elem.innerHTML;
          }
        });

        // hack minify html
        // this way margins render correctly in pre
        elem.innerHTML = _.filter(markdown, function(v, i){
          return !(v == nl && markdown[i-1] == '>');
        }).join("");

        elem.className = "markdown";
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
		render: function(cmCode, wrap, cmOptions, insights){
      clearFun();
			widgets = insights.map(function(insight){
				return apply(cmCode, wrap, cmOptions, insight);
			});
      // focus on cursor
      // cmCode.scrollIntoView(cmCode.getCursor());
      // cmCode.setCursor(cmCode.getCursor(), null, { focus: true});
		}
	}
});
