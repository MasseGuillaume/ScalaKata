app.factory('errorsRenderer', function() {
	var insightWidget = [];
	var errorMessages = [];
	var errorUnderlines = [];

	function errorUnderline(cm, severity, value, code) {
		var cur = cm.getDoc().posFromIndex(value.position);
		var currentLine = code[cur.line];
		return cm.markText(
			{line: cur.line, ch: cur.ch}, 
			{line: cur.line, ch: currentLine.length},
			{className: severity}
		);
	}

	function errorMessage(cm, severity, value){
		var msg = document.createElement("div");
		var cur = cm.getDoc().posFromIndex(value.position);
		var icon = msg.appendChild(document.createElement("i"));
		if(severity == "errors") {
			icon.className += "ion-close-circled";
		} else if(severity == "warnings") {
			icon.className += "ion-alert-circled";
		} else if(severity == "infos") {
			icon.className += "ion-information-circled";
		}
		msg.appendChild(document.createTextNode(value.message));
		msg.className = "error-message";

		return cm.addLineWidget(cur.line, msg);
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
		render: function(cm, data, code){
			["errors", "warnings", "infos"].forEach(function(severity){
				if (data[severity]){
					data[severity].forEach(function(value) {	
						errorMessages.push(errorMessage(cm, severity, value));							
						errorUnderlines.push(errorUnderline(cm, severity, value, code));
					});
				}
			});
		}
	}
});