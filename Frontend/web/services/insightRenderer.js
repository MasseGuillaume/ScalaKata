MathJax.Hub.Config({
    skipStartupTypeset: true,
    messageStyle: "none",
    "HTML-CSS": {
        showMathMenu: false
    }
});
MathJax.Hub.Configured();

app.factory('insightRenderer', ["$timeout", function($timeout) {
	var widgets = [];

	function apply(cmCode, wrap, cmOptions, insight){
		var nl = "\n", elem, start, end, clearF, joined;

    start = wrap.fixRange(insight[0][0], null, cmCode, function(range, cm){
      return cm.getDoc().posFromIndex(range);
    });

    end = wrap.fixRange(insight[0][1], null, cmCode, function(range, cm){
      return cm.getDoc().posFromIndex(range);
    });

    function joined(sep){
      return _.map(insight[1], function(v){ return v.value; }).join(sep);
    }

    function addClass(two){
      angular.element(elem)
            .addClass("insight")
            .addClass(two);
    }

    function fold(e){
      addClass("fold");
      var range = cmCode.markText({ch: 0, line: start.line}, end, {
        replacedWith: e
      });
      clearF = function(){
        range.clear();
      };
    }

    function inline(e){
      addClass("inline");
      var widget = cmCode.addLineWidget(end.line, e);
      clearF = function(){ widget.clear() };
    }

		switch (insight[1][0].type) {
			case "html":
				elem = document.createElement("div");
				elem.innerHTML = joined("");
        fold(elem);
				break;
			case "latex":

        var $script = angular.element("<script type='math/tex'>")
            .html(joined(nl));
        var $element = angular.element("<div>");

        $element.append($script);
        elem = $element[0];
        MathJax.Hub.Queue(["Reprocess", MathJax.Hub, elem]);
        fold(elem);

				break;
			case "markdown":
				elem = document.createElement("div");
        elem.innerHTML = marked.parse(joined(nl), {
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

        elem.className = "markdown";
        fold(elem);
				break;

      case "block":
        var $element = angular.element("<div>"),
            ta = document.createElement("textarea"),
            clip = angular.element("<i class='fa fa-clipboard clip'>"),
            cmOptions2 = angular.copy(cmOptions);
        ta.textContent = cmCode.getRange(start, end);
        $element.append(ta);
        cmOptions2.readOnly = true;
        cmOptions2.lineNumbers = false;
        var cm = CodeMirror.fromTextArea(ta, cmOptions2);

        var client = new ZeroClipboard(clip);

        client.on("ready", function (event){
          client.on("copy", function (event){
            event.clipboardData.setData("text/plain", cm.getDoc().getValue());
            clip.addClass("active");
            $timeout(function(){
              clip.removeClass("active");
            }, 400);
          });
        });

        elem = cm.display.wrapper;
        angular.element(elem).addClass("block").append(clip);
        $timeout(function(){
          cm.refresh();
        })

        _.forEach(insight[1][0].value, function(it){
          apply(cm, wrap, cmOptions, it);
        });

        fold(elem);

        break;
			case "string":
				elem = document.createElement("pre");
				elem.innerText = joined(nl);
        inline(elem);
				break;
			case "other":
				elem = document.createElement("pre");
        elem.className = "code";
				CodeMirror.runMode(joined(nl), cmOptions, elem);
        inline(elem);
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
			widgets = _.map(insights, function(insight){
				return apply(cmCode, wrap, cmOptions, insight);
			});
      // focus on cursor
      cmCode.scrollIntoView(cmCode.getCursor());
      // cmCode.setCursor(cmCode.getCursor(), null, { focus: true});
		}
	}
}]);
