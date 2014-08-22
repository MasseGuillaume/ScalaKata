app.factory("wrap", function() {
  return function (prelude_, code_){
    var import_ = "import com.scalakata.eval._",
        macroBegin = "@ScalaKata object $Playground {",
        macroClose = "}",
        nl = "\n",
        prelude = prelude_.split(nl),
        beforeCode = prelude.concat([import_, macroBegin]),
        beforeCodeLength = beforeCode.join(nl).length + 1,
        code = code_.split(nl);

    return {
      split: function(full){
        var exclude = [import_, macroBegin].join(nl),
            start = full.indexOf(exclude),
            full_ = full.split(nl),
            endPrelude = 0,
            startCode = 0,
            end = 0;

        // find where is the instrumented object
        _.find(full_, function(v, i){
          if(v == import_ && _.contains(full_[i+1], "@ScalaKata")) {
            endPrelude = i;
            startCode = i + 2;
            return true;
          }
        });
        // find last closing bracket
        _.findLast(full_,function(s, i){
          if(_.contains(s, "}")){
            end = i;
            return true;
          }
        });
        function removeIndent(xs){
          function identLength(x){
            return x.length - x.trimLeft().length;
          }
          var indent = identLength(_.min(_.filter(xs, function(v){
            return v !== "";
          }), identLength));
          return _.map(xs, function(x){
            return x.slice(indent, x.length);
          });
        }

        return [
          full_.slice(0, endPrelude).join(nl),
          removeIndent(full_.slice(startCode, end	)).join(nl)
        ];
      },
      codeOffset: function(){
        return beforeCodeLength;
      },
      fixRange: function(range, cmPrelude, cmCode, apply) {
        if(range <= prelude_.length) return apply(range, cmPrelude);
        else return apply(range - beforeCodeLength, cmCode);
      },
      fixLine: function(line, cmPrelude, cmCode, apply) {
        if(line <= prelude.length) return apply(line, cmPrelude);
        else return apply(line - beforeCode.length, cmCode);
      },
      full: beforeCode.concat([
        code_,
        macroClose
      ]).join(nl)
    }
  };
});
