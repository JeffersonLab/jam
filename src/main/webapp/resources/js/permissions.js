var jlab = jlab || {};
jlab.save = function () {
    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    jlab.requestStart();

    var leaveSpinning = false,
            $actionButton = $("#save-button"),
            success = false,
            newLogId = null;

    $actionButton.html("<span class=\"button-indicator\"></span>");
    $actionButton.attr("disabled", "disabled");

    var request = jQuery.ajax({
        url: jlab.contextPath + "/permissions",
        type: "POST",
        data: $("#authorization-form").serialize(),
        dataType: "json"
    });

    request.done(function (data) {
        if (data.error) {
            alert('Unable to save: ' + data.error);
        } else {
            /* Success */
            success = true;
            newLogId = data.logId;
        }

    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to save: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to verify; server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
        if (!leaveSpinning) {
            $actionButton.html("Save");
            $actionButton.removeAttr("disabled");
        }

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
$(document).on("change", ".mode-select", function(){
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

$(document).on("click", "#edit-button", function () {
    $(".readonly-field").hide();
    $(".editable-field").show();
});
$(document).on("click", "#cancel-button", function () {
    $(".editable-field").hide();
    $(".readonly-field").show();
    return false;
});
$(document).on("click", "#save-button", function () {
    jlab.save();
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
});
