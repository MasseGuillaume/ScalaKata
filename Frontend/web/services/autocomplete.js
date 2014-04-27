app.run(function(scalaEval){
	CodeMirror.commands.autocomplete = function(cm) {
		scalaEval.autocomplete(
			cm.getDoc().getValue(), 
			cm.getDoc().indexFromPos(cm.getCursor())).then(function(data){

			// unavailable
			if(angular.isString(data.completions)) {
				CodeMirror.showHint(cm, function(){
					return {from: cm.getCursor(), to: cm.getCursor(), list: [ " /*" + data.completions + "*/ "] };
				});
				return;
			}

			// ok
			CodeMirror.showHint(cm, function(cm, options){
				var i;
				var cur= cm.getCursor();
				var curTo = {"ch" : cur.ch, "line" : cur.line};
				var curFrom = {"ch" : cur.ch, "line" : cur.line};

				var currentLine = cm.getDoc().getValue().split("\n")[cur.line];

				function delimiter(c){
					return  /^[a-zA-Z0-9\_]$/.test(c);
				}


				for (i = cur.ch-1; i >= 0 && delimiter(currentLine[i]); i--){
					curFrom.ch = i;
				}
				for (i = cur.ch; i < currentLine.length && delimiter(currentLine[i]); i++){
					curTo.ch = i+1;
				}

				var term = currentLine.substr(curFrom.ch, curTo.ch - curFrom.ch);


				var completions = data.completions.filter(function(c){

					return c.name.toLowerCase().indexOf(term.toLowerCase()) != -1;

				}).map(function(c){ return {
					text: c.name,
					completion: c,
					alignWithWord: true,
					render: function(el, _, _1){
						el.innerHTML = "<span class=\"autocomplete-result-name\">" + c.name + "</span> <span class=\"autocomplete-result-signature\">" + c.signature +"</span>";
					},
				}});

				return {from: curFrom, to: curTo, list: completions};
			});
		})
	};
});