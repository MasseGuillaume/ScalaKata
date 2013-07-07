window.kataify  = function(){
    'use strict';

    var actionToMode, codeMirrorDefaults;
    actionToMode = {
        "/api/scala": "text/x-scala"
    };

    codeMirrorDefaults = {
        lineNumbers: true,
        matchBrackets: true,
        indentWithTabs: true,
        smartIndent: false,
        styleActiveLine: true,
        indentUnit: 3,
        tabSize: 3,
        autoClearEmptyLines: true,
        firstLineNumber: 0
    }

    $(".kata-form").each(function(){
        var form;
        form = this;

        // Show once
        if($(this).hasClass("kataifyed")) return
        $(this).addClass("kataifyed")

        $(this).find(".kata-code").each(function(){
            var options, mirror;
            options = {
                mode: actionToMode[$(form).attr("action")]
            };
            // Options from data-attributes
            $.each($(form).data(),function(d,v){
                var attr;
                attr = d.substring("codemirror".length,d.length)                // codemirrorTabSize
                attr = attr[0].toLowerCase() + attr.substring(1,attr.length);   // TabSize
                options[attr] = v                                               // tabSize
            });

            mirror = CodeMirror.fromTextArea(this,$.extend(codeMirrorDefaults,options));

            function runCode(){
                var $console, $result;
                $console = $(form).find(".kata-console");
                $result = $(form).find(".kata-result")

                $console.empty();
                $result.empty();

                $.ajax({
                    url: form.action,
                    type: "POST",
                    data: JSON.stringify({code: mirror.getValue()}),
                    contentType: "application/json; charset=utf-8",
                    dataType: "json"
                }).done(function (data) {
                    if(data.errors !== undefined ) {
                        var $errorList;
                        $console.text("Errors")
                        $errorList = $("<ol/>");
                        $result.append($errorList);
                        $.each(data.errors, function(i, error){
                            var $errorElement, $errorSeverity, $errorLine, $errorMessage;
                            error.column -= 1;
                            $errorElement = $("<li/>");
                            $errorElement.addClass("error");
                            $errorSeverity = $("<div/>")
                            $errorSeverity.text(error.severity);
                            $errorSeverity.addClass("severity");
                            $errorLine = $("<div/>");
                            $errorLine.text("L" + error.line + ":" + error.column);
                            $errorLine.addClass("line");
                            $errorLine.click(function(){
                                mirror.setSelection(
                                    CodeMirror.Pos(error.line,error.column),
                                    CodeMirror.Pos(error.line,Infinity)
                                );
                            });
                            $errorMessage = $("<pre/>");
                            $errorMessage.text(error.message);
                            $errorMessage.addClass("message")
                            $errorElement.append($errorSeverity);
                            $errorElement.append($errorLine);
                            $errorElement.append($errorMessage);
                            $errorList.append($errorElement);
                        })
                    } else {
                        $console.text(data.console);
                        $result.text(data.result);
                    }
                })
                .fail( function (data) {
                    var response;
                    response = $.parseJSON(data.responseText);
                    $console.text("");
                    $result.text(response.error);
                })
                .always( function () {
                    $(form).find(".kata-code-wrap").addClass("with-results");
                    $(form).find(".kata-result-window").removeClass("hidden");
                });

                return false;
            }
            $(form).keydown(function(e){
                if( ( e.ctrlKey || e.metaKey ) &&               // command or ctrl +
                    ( e.keyCode == 13 || e.keyCode == 83 ) ) {  // enter, (s)ave
                    e.preventDefault();
                    runCode();
                }
            })
            $(form).submit( function (e) {
                e.preventDefault();
                runCode();
            });
        });
    });
};