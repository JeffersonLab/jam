var jlab = jlab || {};
jlab.triCharMonthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
jlab.dateTimeToJLabString = function (x) {
    var year = x.getFullYear(),
            month = x.getMonth(),
            day = x.getDate(),
            hour = x.getHours(),
            minute = x.getMinutes();

    return jlab.pad(day, 2) + '-' + jlab.triCharMonthNames[month] + '-' + year + ' ' + jlab.pad(hour, 2) + ':' + jlab.pad(minute, 2);
};
jlab.addComponent = function () {
    var verificationId = $("#beam-content .verification-table tbody tr.selected-row").attr("data-control-verification-id"),
        componentId = $("#component").attr("data-component-id"),
        $dialog = $("#component-edit-dialog"),
        url = jlab.contextPath + "/ajax/add-component",
        $actionButton = $("#add-component-button"),
        data = {
            verificationId: verificationId,
            componentId: componentId
        };

    $actionButton
        .attr("disabled", "disabled")
        .height($actionButton.height())
        .width($actionButton.width())
        .empty().append('<div class="button-indicator"></div>');

    var promise = jlab.doAjaxJsonPostRequest(url, data, $dialog, true);

    promise.fail(function () {
        $actionButton.text("Add");
        $actionButton.removeAttr("disabled");
    });
};
jlab.removeComponent = function () {
    var verificationId = $("#beam-content .verification-table tbody tr.selected-row").attr("data-control-verification-id"),
        componentId = $("#selected-component-list").val(),
        $dialog = $("#component-edit-dialog"),
        url = jlab.contextPath + "/ajax/remove-component",
        $actionButton = $("#remove-component-button"),
        data = {
        verificationId: verificationId,
        componentId: componentId
    };

    $actionButton
        .attr("disabled", "disabled")
        .height($actionButton.height())
        .width($actionButton.width())
        .empty().append('<div class="button-indicator"></div>');

    var promise = jlab.doAjaxJsonPostRequest(url, data, $dialog, true);

    promise.fail(function () {
        $actionButton.text("Remove");
        $actionButton.removeAttr("disabled");
    });
};
jlab.verify = function () {
    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    jlab.requestStart();

    var leaveSpinning = false,
            $actionButton = $("#verifySaveButton"),
            verificationId = $("#verificationId").val(),
            verificationDate = $("#verificationDate").val(),
            verifiedBy = $("#verifiedBy").val(),
            expirationDate = $("#expirationDate").val(),
            comments = $("#comments").val(),
            verificationType = $("#verificationType").val(),
            success = false,
            newLogId = null;

    var verificationIdArray = [];

    $("#selected-verification-list li").each(function (index, value) {
        verificationIdArray.push($(this).attr("data-control-verification-id"));
    });

    $actionButton.html("<span class=\"button-indicator\"></span>");
    $actionButton.attr("disabled", "disabled");

    var request = jQuery.ajax({
        url: jlab.contextPath + "/ajax/edit-operations-verifications",
        type: "POST",
        data: {
            'verificationIdArray[]': verificationIdArray,
            verificationId: verificationId,
            verificationDate: verificationDate,
            verifiedBy: verifiedBy,
            expirationDate: expirationDate,
            comments: comments,
            verificationType: verificationType
        },
        dataType: "html"
    });

    request.done(function (data) {
        var status = $(".status", data).html();

        if (status === 'Error') {
            alert('Unable to verify: ' + $(".reason", data).html());
        } else {
            /* Success */
            success = true;
            newLogId = $(".logid", data).html();
        }

    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to verify: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to verify; server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
        if (!leaveSpinning) {
            $actionButton.html("Save");
            $actionButton.removeAttr("disabled");
        }

        if (success) {
            if (newLogId !== '') {
                $("#verify-dialog").dialog("close");
                $("#new-entry-url").attr("href", jlab.logbookServerUrl + "/entry/" + newLogId);
                $("#new-entry-url").text(newLogId);
                $("#success-dialog").dialog("open");
            } else {
                document.location.reload(true);
            }
        }
    });
};
$(document).on("click", ".verify-button", function () {

    var $panel = $(this).closest('.verification-panel');

    var verificationLabel = "Beam Destination",
        verificationType = "BEAM";

    if($panel.hasClass("rf")) {
        verificationLabel = "RF Segment";
        verificationType = "RF";
    }

    var $selectedList = $("#selected-verification-list");

    $selectedList.empty();

    var $verificationList = $panel.find(".verification-table tbody tr");

    var statusArray = [],
            verificationDateArray = [],
            verifiedByArray = [],
            expirationDateArray = [],
            commentsArray = [];


    $verificationList.each(function (index, value) {
        if ($(".destination-checkbox", value).is(":checked")) {
            var id = $(this).attr("data-control-verification-id"),
                label = $("td:nth-child(2)", value).text(),
                $notReadyComponents = $("td:nth-child(4) .not-verified-icon", value);

            if($notReadyComponents.length > 0) {
                alert("You cannot verify " + label + " because there are associated Components Not Ready");
                return;
            }

            $selectedList.append('<li data-control-verification-id="' + String(id).encodeXml()  + '">' + String(label).encodeXml() + '</li>');
            statusArray.push($(this).attr("data-status-id"));
            verificationDateArray.push($(".verified-date", value).text());
            verifiedByArray.push($(this).attr("data-verified-username"));
            expirationDateArray.push($("td:nth-child(6)", value).text());
            commentsArray.push($("td:nth-child(5)", value).text());
        }
    });

    if (statusArray.length < 1) {
        window.console && console.log('No rows selected');
        return;
    }

    var statusId = '',
            verificationDate = '',
            verifiedBy = '',
            expirationDate = '',
            comments = '';

    var rowsDiffer = false;

    for (var i = 1; i < statusArray.length; i++) {
        if (statusArray[0] !== statusArray[i] ||
                verificationDateArray[0] !== verificationDateArray[i] ||
                verifiedByArray[0] !== verifiedByArray[i] ||
                expirationDateArray[0] !== expirationDateArray[i] ||
                comments[0] !== comments[i]) {
            rowsDiffer = true;
            break;
        }
    }

    if (rowsDiffer) {
        $("#rows-differ-message").show();
    } else {
        $("#rows-differ-message").hide();
        statusId = statusArray[0];
        verificationDate = verificationDateArray[0];
        verifiedBy = verifiedByArray[0];
        expirationDate = expirationDateArray[0];
        comments = commentsArray[0];
    }

    var count = $("#selected-verification-list li").length;
    var text = count === 1 ? verificationLabel : verificationLabel + 's';

    $("#edit-dialog-verification-label").text(text);
    $("#edit-dialog-verification-count").text(count + ' ' + text);

    $("#verificationId").val(statusId);
    $("#verificationDate").val(verificationDate);
    $("#verifiedBy").val(verifiedBy);
    $("#expirationDate").val(expirationDate);
    $("#comments").val(comments);
    $("#verificationType").val(verificationType);

    $("#verify-dialog").dialog("open");
});
$(document).on("click", "#verifySaveButton", function () {
    jlab.verify();
});
$(document).on("change", ".check-select", function () {
    var $panel = $(this).closest('.verification-panel');

    if ($(this).val() === 'all') {
        $panel.find(".editable-row-table tbody tr").addClass("selected-row");
        $panel.find(".destination-checkbox").each(function (index, value) {
            $(this).prop("checked", true);
        });
        $panel.find(".edit-selected-button").prop("disabled", false);

        var numSelected = jlab.editableRowTable.updateSelectionCount($panel);
        if(numSelected === 1) {
            $panel.find(".component-edit-button").prop("disabled", false);
        } else {
            $panel.find(".component-edit-button").prop("disabled", true);
        }
    } else if ($(this).val() === 'none') {
        $panel.find(".editable-row-table tbody tr").removeClass("selected-row");
        $panel.find(".destination-checkbox").each(function (index, value) {
            $(this).prop("checked", false);
        });
        $panel.find(".edit-selected-button").prop("disabled", true);
        $panel.find(".component-edit-button").prop("disabled", true);
    }
    $(this).val('');
});
$(document).on("click", ".component-edit-button", function() {
    var $panel = $(this).closest('.verification-panel');

    var $componentList = $("#selected-component-list");

    $componentList.empty();

    var $rowList = $panel.find(".verification-table tbody tr.selected-row");

    if($rowList.length !== 1) {
        alert("select only one row");
        return;
    }

    $rowList.find(".component-status").each(function (index, value) {
        var label = $(this).find("a").text(),
            id = $(this).attr("data-id");

        $componentList.append('<option value="' + String(id).encodeXml() + '">' + String(label).encodeXml() + '</option>');
    });

    var destination = $rowList.find("td:nth-child(2)").text();

    $("#component-edit-dialog").dialog({"title": destination + " Components"});

    $("#component-edit-dialog").dialog("open");
    return false;
});
$(document).on("click", ".multicheck-table tbody tr", function (e) {
    var checkClicked = e.target.classList.contains('destination-checkbox');

    var $panel = $(this).closest('.verification-panel');

    var lastSelectedRow = $panel.attr("data-last-selected-row");

    if (e.ctrlKey || checkClicked) {
        $(this).toggleClass("selected-row");
        if ($(this).hasClass("selected-row")) {
            $(this).find(".destination-checkbox").prop("checked", true);
        } else {
            $(this).find(".destination-checkbox").prop("checked", false);
        }
    } else if (e.shiftKey) {
        /*console.log('shift held');*/
        if (lastSelectedRow === undefined || lastSelectedRow === null) { // Regular click if no previous click
            /*console.log('no last selected');*/
            $(this).find(".destination-checkbox").prop("checked", true);
            $(this).addClass("selected-row").siblings().removeClass("selected-row").find(".destination-checkbox").prop("checked", false);
        } else { // Select a range
            var first = parseInt(lastSelectedRow),
                    second = $(this).index(),
                    start = Math.min(first, second),
                    end = Math.max(first, second);

            /*console.log('start: ' + start);
             console.log('end: ' + end);*/

            $panel.find(".multicheck-table tbody tr").slice(start, end + 1).addClass("selected-row").find(".destination-checkbox").prop("checked", true);
        }
    } else {
        $(this).find(".destination-checkbox").prop("checked", true);
        $(this).addClass("selected-row").siblings().removeClass("selected-row").find(".destination-checkbox").prop("checked", false);
    }

    if ($(this).hasClass("selected-row")) {
        lastSelectedRow = $(this).index();
        $panel.attr("data-last-selected-row", lastSelectedRow);
    } else {
        lastSelectedRow = null; /*If we are unselecting then reset shift select*/
        $panel.removeAttr("data-last-selected-row");
    }

    var numSelected = jlab.editableRowTable.updateSelectionCount($panel);

    if (numSelected > 0) {
        $panel.find(".no-selection-row-action").prop("disabled", true);
        $panel.find(".selected-row-action").prop("disabled", false);
    } else {
        $panel.find(".no-selection-row-action").prop("disabled", false);
        $panel.find(".selected-row-action").prop("disabled", true);
    }

    if(numSelected === 1) {
        $panel.find(".single-select-row-action").prop("disabled", false);
    } else {
        $panel.find(".single-select-row-action").prop("disabled", true);
    }
});
jlab.editableRowTable.updateSelectionCount = function ($panel) {
    var numSelected = $panel.find(".multicheck-table").find(".selected-row").length;
    //$panel.find(".selected-count").text(numSelected);
    return numSelected;
};
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
        hour: 8
    }).mask("99-aaa-9999 99:99", {placeholder: " "});

    $("#verify-dialog").dialog({
        width: 700,
        height: 600,
        resizable: false
    });

    $("#component-edit-dialog").dialog({
        width: 800,
        height: 300,
        resizable: false
    });

    $(".username-autocomplete").autocomplete({
        minLength: 2,
        source: function (request, response) {
            $.ajax({
                data: {
                    q: request.term
                },
                dataType: 'json',
                url: jlab.contextPath + '/data/users',
                success: function (json) {
                    response($.map(json.records, function (item) {
                        return {
                            id: item.id,
                            label: item.label,
                            value: item.value
                        }
                    }));

                    if (json.total_records > 10) {
                        $(".ui-autocomplete").append($("<li class=\"plus-more\">Plus " + jlab.integerWithCommas(json.total_records - 10) + " more...</li>"));
                    }
                }
            });
        },
        select: function (event, ui) {
            $(".username-autocomplete").attr("data-user-id", ui.item.id);
        }
    });

    /*Now Button Support 2 of 2*/
    $('<span> </span><button class="now-button" type="button">Now</button>').insertAfter(".nowable-field");

    $("#component").autocomplete({
        minLength: 2,
        source: function (request, response) {
            var params = {
                q: request.term
            };

            //jQuery.ajaxSettings.traditional = true; /*array bracket serialization*/

            $.ajax({
                data: params,
                dataType: 'json',
                url: jlab.contextPath + '/data/components',
                success: function (json) {
                    response($.map(json.data, function (item) {
                        return {
                            label: item.name,
                            value: item.name,
                            id: item.id
                        };
                    }));

                    if (json.total_records > 10) {
                        $(".ui-autocomplete").append($("<li class=\"plus-more\">Plus " + jlab.integerWithCommas(json.total_records - 10) + " more...</li>"));
                    }
                }
            });
        },
        select: function (event, ui) {
            $("#component").attr("data-component-id", ui.item.id);
        }
    });

    $( ".accordion" ).accordion({
        collapsible: true,
        heightStyle: "content",
        active: 0
    });

});
/*Now Button Support 1 of 2*/
$(document).on("click", ".now-button", function () {
    $(this).prevAll(".date-time-field").first().val(jlab.dateTimeToJLabString(new Date()));
});
$(document).on("click", ".me-button", function () {
    var username = $("#username-container").text().trim();
    $(this).prevAll(".username-autocomplete").first().val(username);
});
$("#success-dialog").on("dialogclose", function () {
    document.location.reload(true);
});
$(document).on("click", "#add-component-button", function () {
    jlab.addComponent();
});
$(document).on("click", "#remove-component-button", function () {
    jlab.removeComponent();
});