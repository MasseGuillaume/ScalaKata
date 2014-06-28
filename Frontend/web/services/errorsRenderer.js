app.factory('errorsRenderer', function() {
	var insightWidget = [];
	var errorMessages = [];
	var errorUnderlines = [];

	function errorUnderline(cm, severity, value, code) {
		var from, to, cur, currentLine;
		if(angular.isDefined(value.position)) {
			cur = cm.getDoc().posFromIndex(value.position);
			currentLine = code[cur.line];
			from = {line: cur.line, ch: cur.ch};
			to = {line: cur.line, ch: currentLine.length};
		} else {
			from = {line: value.line - 1, ch: 0};
			to = {line: value.line - 1, ch: Infinity};
		}
		
		return cm.markText(from, to, {className: severity} );
	}

	function errorMessage(cm, severity, value){
		var msg = document.createElement("div"),
			line; 
		if(angular.isDefined(value.position)) {
			line = cm.getDoc().posFromIndex(value.position).line;
		} else {
			line = value.line - 1;
		}

		var icon = msg.appendChild(document.createElement("i"));
		icon.className = "fa ";
		if(severity == "errors") {
			icon.className += "fa-times-circle";
		} else if(severity == "warnings") {
			icon.className += "fa-exclamation-triangle";
		} else if(severity == "infos") {
			icon.className += "fa-info-circle";
		}
		msg.appendChild(document.createTextNode(value.message));
		msg.className = "error-message";

		return cm.addLineWidget(line, msg);
	}

	return {
		clear: function(){
			// clear line errors
			errorMessages.forEach(function (value){
				value.clear();
			});
			errorMessages = [];

			errorUnderlines.forEach(function (value){
				value.clear();
			});
			errorUnderlines = [];
		},
		render: function(cm, infos, runtimeError, code){
			["error", "warning", "info"].forEach(function(severity){
				if (infos[severity]){
					infos[severity].forEach(function(value) {	
						errorMessages.push(errorMessage(cm, severity, value));							
						errorUnderlines.push(errorUnderline(cm, severity, value, code));
					});
				}
			});
			if(angular.isDefined(runtimeError)) {
				var value = runtimeError;
				var severity = "runtime-error";

				errorMessages.push(errorMessage(cm, severity, value));							
				errorUnderlines.push(errorUnderline(cm, severity, value, code));
			}
		}
	}
});