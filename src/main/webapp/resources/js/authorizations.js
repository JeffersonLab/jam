var jlab = jlab || {};
jlab.pageDialog.width = 950;
jlab.rfSave = function () {

    var $actionButton = $("#rf-save-button"),
        success = false,
        newLogId = null,
        url = jlab.contextPath + "/ajax/edit-rf-auth",
        data = $("#rf-authorization-form").serialize();

    $actionButton
        .attr("disabled", "disabled")
        .height($actionButton.height())
        .width($actionButton.width())
        .empty().append('<div class="button-indicator"></div>');

    var request = jQuery.ajax({
        url: url,
        type: "POST",
        data: data,
        dataType: "json"
    });

    request.fail(function (jqXHR, textStatus, errorThrown) {
        console.log("fail jqXR:", jqXHR);
        console.log("fail textStatus:", textStatus);
        console.log("fail errorThrown:", errorThrown);
        console.log('fail Response JSON:', jqXHR.responseJSON);

        var error = textStatus;

        if(jqXHR.responseJSON) {
            error = jqXHR.responseJSON.error
        }

        alert("Unable to save: " + error);
    })

    request.done(function (data) {
        if (data.error) {
            alert('Unable to save: ' + data.error);
        } else {
            /* Success */
            success = true;
            newLogId = data.logId;
        }

    });

    request.always(function () {
        $actionButton.html("Save");
        $actionButton.removeAttr("disabled");

        if (success) {
            if (newLogId !== null && typeof(newLogId) !== 'undefined') {
                $("#new-entry-url").attr("href", jlab.logbookServerUrl + "/entry/" + newLogId);
                $("#new-entry-url").text(newLogId);
                $("#success-dialog").dialog("open");
            } else {
                document.location.reload();
            }
        }
    });
};
jlab.beamSave = function () {

    var $actionButton = $("#beam-save-button"),
        success = false,
        newLogId = null,
        url = jlab.contextPath + "/ajax/edit-beam-auth",
        data = $("#beam-authorization-form").serialize();

    $actionButton
        .attr("disabled", "disabled")
        .height($actionButton.height())
        .width($actionButton.width())
        .empty().append('<div class="button-indicator"></div>');

    var request = jQuery.ajax({
        url: url,
        type: "POST",
        data: data,
        dataType: "json"
    });

    request.fail(function (jqXHR, textStatus, errorThrown) {
        console.log("fail jqXR:", jqXHR);
        console.log("fail textStatus:", textStatus);
        console.log("fail errorThrown:", errorThrown);
        console.log('fail Response JSON:', jqXHR.responseJSON);

        var error = textStatus;

        if(jqXHR.responseJSON) {
            error = jqXHR.responseJSON.error
        }

        alert("Unable to save: " + error);
    })

    request.done(function (data) {
        if (data.error) {
            alert('Unable to save: ' + data.error);
        } else {
            /* Success */
            success = true;
            newLogId = data.logId;
        }

    });

    request.always(function () {
        $actionButton.html("Save");
        $actionButton.removeAttr("disabled");

        if (success) {
            if (newLogId !== null && typeof(newLogId) !== 'undefined') {
                $("#new-entry-url").attr("href", jlab.logbookServerUrl + "/entry/" + newLogId);
                $("#new-entry-url").text(newLogId);
                $("#success-dialog").dialog("open");
            } else {
                document.location.reload();
            }
        }
    });
};
$(document).on("change", ".rf-mode-select", function(){
    var $select = $(this),
        $tr = $select.closest("tr"),
        $comments = $tr.find(".comment-input"),
        $expiration = $tr.find(".expiration-input");

    if($select.val() === 'No') {
        $comments.val('');
        $expiration.val('');
        $comments.attr('readonly', 'readonly');
        $expiration.attr('readonly', 'readonly');
    } else {
        $comments.removeAttr('readonly');
        $expiration.removeAttr('readonly');
    }
});
$(document).on("change", ".beam-mode-select", function(){
    var $select = $(this),
        $tr = $select.closest("tr"),
        $limit = $tr.find(".limit-input"),
        $comments = $tr.find(".comment-input"),
        $expiration = $tr.find(".expiration-input");

    if($select.val() === 'RF Only') {
        $limit.attr('readonly', 'readonly');
        $comments.removeAttr('readonly');
        $expiration.removeAttr('readonly');
    } else if($select.val() === 'None') {
        $limit.val('');
        $comments.val('');
        $expiration.val('');
        $limit.attr('readonly', 'readonly');
        $comments.attr('readonly', 'readonly');
        $expiration.attr('readonly', 'readonly');
    } else {
        $limit.removeAttr('readonly');
        $comments.removeAttr('readonly');
        $expiration.removeAttr('readonly');
    }
});

$(document).on("click", "#beam-edit-button", function () {
    $("#beam-authorization-form .readonly-field").hide();
    $("#beam-authorization-form .editable-field").show();
});
$(document).on("click", "#beam-cancel-button", function () {
    $("#beam-authorization-form .editable-field").hide();
    $("#beam-authorization-form .readonly-field").show();
    return false;
});
$(document).on("click", "#beam-save-button", function () {
    jlab.beamSave();
});
$(document).on("click", "#rf-edit-button", function () {
    $("#rf-authorization-form .readonly-field").hide();
    $("#rf-authorization-form .editable-field").show();
});
$(document).on("click", "#rf-cancel-button", function () {
    $("#rf-authorization-form .editable-field").hide();
    $("#rf-authorization-form .readonly-field").show();
    return false;
});
$(document).on("click", "#rf-save-button", function () {
    jlab.rfSave();
});
$("#success-dialog").on("dialogclose", function () {
    document.location.reload(true);
});
$(function () {
    /*Custom time picker*/
    var myControl = {
        create: function (tp_inst, obj, unit, val, min, max, step) {
            $('<input class="ui-timepicker-input" value="' + val + '" style="width:50%">')
                    .appendTo(obj)
                    .spinner({
                        min: min,
                        max: max,
                        step: step,
                        change: function (e, ui) { // key events
                            // don't call if api was used and not key press
                            if (e.originalEvent !== undefined)
                                tp_inst._onTimeChange();
                            tp_inst._onSelectHandler();
                        },
                        spin: function (e, ui) { // spin events
                            tp_inst.control.value(tp_inst, obj, unit, ui.value);
                            tp_inst._onTimeChange();
                            tp_inst._onSelectHandler();
                        }
                    });
            return obj;
        },
        options: function (tp_inst, obj, unit, opts, val) {
            if (typeof (opts) === 'string' && val !== undefined)
                return obj.find('.ui-timepicker-input').spinner(opts, val);
            return obj.find('.ui-timepicker-input').spinner(opts);
        },
        value: function (tp_inst, obj, unit, val) {
            if (val !== undefined)
                return obj.find('.ui-timepicker-input').spinner('value', val);
            return obj.find('.ui-timepicker-input').spinner('value');
        }
    };

    $(".date-time-field").datetimepicker({
        dateFormat: 'dd-M-yy',
        controlType: myControl,
        timeFormat: 'HH:mm',
        hour: 8,
        beforeShow: function(i) { if ($(i).attr('readonly')) { return false; } }
    }).mask("99-aaa-9999 99:99", {placeholder: " "});

    $( ".accordion" ).accordion({
        collapsible: true,
        heightStyle: "content",
        active: 0
    });

    const urlParams = new URLSearchParams(window.location.search);
    const focus = urlParams.get('focus');

    if(focus === 'RF') {
        $("#beam-accordion").accordion("option","active",false);
    } else if(focus === 'BEAM') {
        $("#rf-accordion").accordion("option","active",false);
    }
});