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

	function apply(cmCode, wrap, cmOptions, insight, cmOriginal, blockOffset, updateF, prelude, code){
		var nl = "\n", elem, start, end, clearF, joined;

    start = wrap.fixRange(insight[0][0], null, cmOriginal, function(range, cm){
      return cm.getDoc().posFromIndex(range);
    });
    start.line -= blockOffset;

    end = wrap.fixRange(insight[0][1], null, cmOriginal, function(range, cm){
      return cm.getDoc().posFromIndex(range);
    });
    end.line -= blockOffset;

    function captureClick(el){
      $("a", el).map(function(i, e){
        var href = $(e).attr("href");
        if(!angular.isDefined(href)) return;

        function domain(url) {
            return url.replace('http://','').replace('https://','').split('/')[0];
        };
        if(domain(href) === domain(window.location.origin) ||
          (href[0] === '/' && href[1] !== '/')) {
          $(e).on('click', function(ev){
            var path = href.replace(window.location.origin, ""),
                state = {"prelude": prelude, "code": code};
            ev.preventDefault();
            window.history.pushState(state, null, path);
            updateF(path);
          })
        } else {
            $(e).attr('target', '_blank');
        }
      })
    }

    function joined(sep){
      return _.map(insight[1], function(v){ return v.value; }).join(sep);
    }

    function addClass(two, e){
      angular.element(e)
            .addClass("insight")
            .addClass(two);
    }

    function fold(e){
      addClass("fold", e);
      var range = cmCode.markText({ch: 0, line: start.line}, end, {
        replacedWith: e
      });
      clearF = function(){
        range.clear();
      };
    }

    function inline(e){
      addClass("inline", e);
      var widget = cmCode.addLineWidget(end.line, e);
      clearF = function(){ widget.clear() };
    }

    function html(){
      var time = new Date().getTime(),
          code = insight[1][0].value[0],
          div = $('<div/>'),
          form = $('<form action="/echo" target="iframe'+time+'" method="post"></form>'),
          iframe = $('<iframe class="html" name="iframe'+time+'" allow-top-navigation allow-popups allowTransparency></iframe>');

      $("<input type='hidden' />")
       .attr("name", "code")
       .attr("value", code)
       .appendTo(form);

      iframe.css("height", insight[1][0].value[1]);

      elem = iframe.contents();
      fold(iframe[0]);
      $timeout(function(){
        form.submit();
        iframe.load(function(){
          captureClick(iframe.contents());
        });
      }, 500);
    }

    function markdown(){
      elem = document.createElement("div");
      elem.innerHTML = marked.parse(joined(nl), {
        ghf: true,
        highlight: function (code, lang) {
          var elem = document.createElement("div"),
              option = angular.copy(cmOptions),
              langs = {
                "scala": "text/x-scala",
                "json": "application/json"
              },
              mode = langs[lang] || "";


          CodeMirror.runMode(code, mode, elem, option);
          return elem.innerHTML;
        }
      });

      elem.className = "markdown";
      captureClick(elem);
    }

		switch (insight[1][0].type) {
			case "html":
				html();
				break;

      case "html2":
        html();
        inline(elem);
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
				markdown();
        fold(elem);
				break;

      case "markdown2":
        markdown();
        inline(elem);
        break;

      case "block":
        var $element = angular.element("<div>"),
            ta = document.createElement("textarea"),
            clip = angular.element("<i class='fa fa-clipboard clip'>"),
            cmOptions2 = angular.copy(cmOptions),
            content = cmCode.getRange(start, end);
        ta.textContent = content;
        $element.append(ta);
        cmOptions2.readOnly = true;
        cmOptions2.lineNumbers = false;
        cmOptions2.scrollPastEnd = false;
        var cm = CodeMirror.fromTextArea(ta, cmOptions2);

        var client = new ZeroClipboard(clip);

        client.on("ready", function (event){
          client.on("copy", function (event){
            event.clipboardData.setData("text/plain", content);
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
          apply(cm, wrap, cmOptions, it, cmOriginal, start.line);
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
		render: function(cmCode, wrap, cmOptions, insights, updateF, prelude, code){
      clearFun();
			widgets = _.map(insights, function(insight){
				return apply(cmCode, wrap, cmOptions, insight, cmCode, 0, updateF, prelude, code);
			});
      $timeout(function(){
        cmCode.refresh();
      });
      // focus on cursor
      // cmCode.focus();
      // cmCode.scrollIntoView(cmCode.getCursor());
      // cmCode.setCursor(cmCode.getCursor(), null, { focus: true});
		}
	}
}]);
