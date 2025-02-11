var jlab = jlab || {};
jlab.editableRowTable = jlab.editableRowTable || {};
jlab.editableRowTable.entity = 'Authorizer';
jlab.editableRowTable.dialog.width = 400;
jlab.editableRowTable.dialog.height = 300;
jlab.addRow = function() {
    var username = $("#row-username").val(),
        type = $("#row-type").val(),
        facilityId = $("#row-facility").val(),
        reloading = false;

    $(".dialog-submit-button")
        .height($(".dialog-submit-button").height())
        .width($(".dialog-submit-button").width())
        .empty().append('<div class="button-indicator"></div>');
    $(".dialog-close-button").attr("disabled", "disabled");
    $(".ui-dialog-titlebar button").attr("disabled", "disabled");

    var request = jQuery.ajax({
        url: jlab.contextPath + "/ajax/add-authorizer",
        type: "POST",
        data: {
            username: username,
            type: type,
            facilityId: facilityId
        },
        dataType: "json"
    });

    request.done(function(json) {
        if (json.stat === 'ok') {
            reloading = true;
            window.location.reload();
        } else {
            alert(json.error);
        }
    });

    request.fail(function(xhr, textStatus) {
        window.console && console.log('Unable to add authorizer; Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to Save: Server unavailable or unresponsive');
    });

    request.always(function() {
        if (!reloading) {
            $(".dialog-submit-button").empty().text("Save");
            $(".dialog-close-button").removeAttr("disabled");
            $(".ui-dialog-titlebar button").removeAttr("disabled");
        }
    });
};
jlab.removeRow = function() {
    var type = $(".editable-row-table tr.selected-row td:nth-child(2)").text(),
        facilityId = $(".editable-row-table tr.selected-row").attr("data-facility-id"),
        username = $(".editable-row-table tr.selected-row").attr("data-username");

        reloading = false;

    $("#remove-row-button")
        .height($("#remove-row-button").height())
        .width($("#remove-row-button").width())
        .empty().append('<div class="button-indicator"></div>');

    var request = jQuery.ajax({
        url: jlab.contextPath + "/ajax/remove-authorizer",
        type: "POST",
        data: {
            facilityId: facilityId,
            username: username,
            type: type
        },
        dataType: "json"
    });

    request.done(function(json) {
        if (json.stat === 'ok') {
            reloading = true;
            window.location.reload();
        } else {
            alert(json.error);
        }
    });

    request.fail(function(xhr, textStatus) {
        window.console && console.log('Unable to remove authorizer; Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to Remove Server unavailable or unresponsive');
    });

    request.always(function() {
        if (!reloading) {
            $("#remove-row-button").empty().text("Remove");
        }
    });
};
$(document).on("dialogclose", "#table-row-dialog", function() {
    $("#row-form")[0].reset();
});
$(document).on("table-row-add", function() {
    jlab.addRow();
});
$(document).on("click", "#remove-row-button", function() {
    var facility = $(".editable-row-table tr.selected-row td:first-child").text(),
        type = $(".editable-row-table tr.selected-row td:nth-child(2)").text(),
        name = $(".editable-row-table tr.selected-row td:nth-child(3)").text();
    if (confirm('Are you sure you want to remove ' + name + ' from ' + facility + ' authorizer of type ' + type + '?')) {
        jlab.removeRow();
    }
});
$(document).on("click", ".default-clear-panel", function () {
    $("#facility-select").val('');
    $("#type-select").val('');
    return false;
});