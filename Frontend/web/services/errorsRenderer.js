app.factory('errorsRenderer', function() {
  var insightWidget = [];
  var errorMessages = [];
  var errorUnderlines = [];

  function errorUnderline(cmCode, wrap, severity, value) {
    var start;
    function render(from, to){
      return cmCode.markText(from, to, {className: severity} );
    }
    if(angular.isDefined(value.start) && angular.isDefined(value.end) && value.start != value.end) {
      return render(
        wrap.fixRange(value.start),
        wrap.fixRange(value.end)
      )
    } else {
      var l = angular.isDefined(value.line) ?
          wrap.fixLine(value.line)
        : cmCode.getDoc().posFromIndex(wrap.fixRange(value.start)).line;

      return render(
        {line: l, ch: 0},
        {line: l, ch: Infinity}
      );
    }
  }

  function errorMessage(cmCode, wrap, severity, value){
    var offset;
    function render(line){
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

      return cmCode.addLineWidget(line, msg);
    }

    if(angular.isDefined(value.start)) {
      offset = value.start;

      console.log(wrap.fixRange(offset).line);

      if(value.start == -1) offset = Infinity;
      return render(cmCode.getDoc().posFromIndex(wrap.fixRange(offset)).line);

    } else {
      if (value.line !== -1) {
        return render(
          render(wrap.fixLine(value.line) -1)
        );
      } else {
        return render(Infinity);
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
    render: function(cmCode, wrap, infos, runtimeError){
      clearFun();
      ["error", "warning", "info"].forEach(function(severity){
        if (infos[severity]){
          infos[severity].forEach(function(value) {
            errorMessages.push(errorMessage(cmCode, wrap, severity, value));
            errorUnderlines.push(errorUnderline(cmCode, wrap, severity, value));
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
