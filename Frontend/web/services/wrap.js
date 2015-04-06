app.factory("wrap", function() {
  return function (code, insight){
    var import_ = "import com.scalakata.eval._",
        macroBegin = "@ScalaKata object $Playground {",
        macroEnd = "}",
        presentationBegin = "object $Playground {",
        presentationEnd = "}",
        nl = "\n",
        full = insight ?
            [import_, macroBegin, code, macroEnd].join(nl)
          : [import_, presentationBegin, code, presentationEnd].join(nl)

    return {
      codeOffset: ([import_, presentationBegin].join(nl).length),
      fixRange: function(range) {
        return insight ?
              range - ([import_, macroBegin].join(nl).length)
            : range - ([import_, presentationBegin].join(nl).length)
      },
      fixLine: function(line) {
        return line - 2
      },
      full: full 
    }
  };
});
