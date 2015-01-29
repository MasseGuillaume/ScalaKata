app.factory('errorsRenderer', function() {
	var insightWidget = [];
	var errorMessages = [];
	var errorUnderlines = [];

	function errorUnderline(cmCode, cmPrelude, wrap, severity, value) {
		var start;
		function render(cm, from, to){
			return cm.markText(from, to, {className: severity} );
		}
		if(angular.isDefined(value.start) && angular.isDefined(value.end)) {
			start = wrap.fixRange(value.start, cmPrelude, cmCode, function(range, cm){
				return cm.getDoc().posFromIndex(range);
			});

			return wrap.fixRange(value.end, cmPrelude, cmCode, function(range, cm){
				var to = cm.getDoc().posFromIndex(range);
				return render(cm, start, to);
			});
		} else {
			return wrap.fixLine(value.line, cmPrelude, cmCode, function(l, cm){
				from = {line: l - 1, ch: 0};
				to = {line: l - 1, ch: Infinity};
				return render(cm, from, to);
			});
		}
	}

	function errorMessage(cmCode, cmPrelude, wrap, severity, value){
		var offset;
		function render(line, cm){
			var msg = document.createElement("div"),
					icon = msg.appendChild(document.createElement("i"));

			icon.className = "fa ";
			if(severity == "error") {
				icon.className += "fa-times-circle";
			} else if(severity == "warning") {
				icon.className += "fa-exclamation-triangle";
			} else if(severity == "info") {
				icon.className += "fa-info-circle";
			}
			msg.appendChild(document.createTextNode(value.message));
			msg.className = "error-message";

			return cm.addLineWidget(line, msg);
		}

		if(angular.isDefined(value.start)) {
			offset = value.start;
			// To avoid displaying general error on the prelude(top) mirror
			if(value.start == -1) offset = Infinity;

			return wrap.fixRange(offset, cmPrelude, cmCode, function(range, cm){
				var line = cm.getDoc().posFromIndex(range).line;
				return render(line, cm);
			});
		} else {
			if (value.line !== -1) {
				return wrap.fixLine(value.line, cmPrelude, cmCode, function(line, cm){
					return render(line - 1, cm);
				});
			} else {
				return render(Infinity, cmCode);
			}
			
		}
	}

	function clearFun(){
		// clear line errors
		errorMessages.forEach(function (value){
			value.clear();
		});
		errorMessages = [];

		errorUnderlines.forEach(function (value){
			value.clear();
		});
		errorUnderlines = [];
	}

	return {
		clear: clearFun,
		render: function(cmCode, cmPrelude, wrap, infos, runtimeError){
			clearFun();
			["error", "warning", "info"].forEach(function(severity){
				if (infos[severity]){
					infos[severity].forEach(function(value) {
						errorMessages.push(errorMessage(cmCode, cmPrelude, wrap, severity, value));
						errorUnderlines.push(errorUnderline(cmCode, cmPrelude, wrap, severity, value));
					});
				}
			});
			if(angular.isDefined(runtimeError)) {
				var value = runtimeError;
				var severity = "runtime-error";

				errorMessages.push(errorMessage(cmCode, cmPrelude, wrap, severity, value));
				errorUnderlines.push(errorUnderline(cmCode, cmPrelude, wrap, severity, value));
			}
		}
	}
});
