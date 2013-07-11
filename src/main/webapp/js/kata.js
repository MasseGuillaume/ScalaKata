$.fn.openkata = function(kataOptions,codeMirrorOptions){
    'use strict';

    var actionToMode, codeMirrorDefaults, form;

    form = this;
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
    codeMirrorOptions = $.extend(codeMirrorDefaults,codeMirrorOptions)

    // Show once
    if($(this).hasClass("kataifyed")) return
    $(this).addClass("kataifyed")

    $(this).find(".kata-code").each(function(){
        var options, mirror;
        options = $.extend(codeMirrorDefaults,{
            mode: actionToMode[$(form).attr("action")]
        });
        mirror = CodeMirror.fromTextArea(this,options);

        function runCode(){
            var $console, $result, $run, code;

            // disable until response from server
            $run = $(form).find("[name='run']");
            if($run.prop("disabled")) { return; }
            $run.prop("disabled",true);

            $console = $(form).find(".kata-console");
            $result = $(form).find(".kata-result")

            $console.empty();
            $result.empty();

            function pushState(code,result,success){
                if (result.id !== undefined && kataOptions.pushState) {
                    window.history.pushState($.extend(result,{code:code,status:success}), null,"/" + result.id);
                }
            }

            function renderEval(data){
                if (data.errors !== undefined ) {
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
                    $result[0].innerHTML = data.result;
                }
            }
            function renderFail(data) {
                $console.text("");
                $result.text(data.error);
            }
            function renderAlways() {
               $(form).find(".kata-code-wrap").addClass("with-results");
               $(form).find(".kata-result-window").removeClass("hidden");
               $run.prop("disabled",false);
            }

            // replay history
            if (kataOptions.pushState) {
                window.onpopstate = function(event) {
                    var data;
                    data = event.state;
                    if (null !== data) {
                        mirror.setValue(data.code);
                        if (true === data.status) {
                         renderEval(data);
                        } else {
                         renderFail(data);
                        }
                        renderAlways(data);
                    } else {
                        // intial state
                        mirror.setValue("");
                        $(form).find(".kata-code-wrap").removeClass("with-results");
                        $(form).find(".kata-result-window").addClass("hidden");
                    }
                };
            }

            code = mirror.getValue();
            $.ajax({
                url: form[0].action,
                type: "POST",
                data: JSON.stringify({code:code}),
                contentType: "application/json; charset=utf-8",
                dataType: "json"
            }).
            done(function(result){
                renderEval(result);
                pushState(code,result,true);
            }).
            fail(function(data){
                var result;
                result = data.responseJSON;
                renderFail(result);
                pushState(code,result,false);
            }).
            always(renderAlways);

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

};