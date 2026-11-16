var jlab = jlab || {};
jlab.editableRowTable = jlab.editableRowTable || {};
jlab.editableRowTable.entity = 'Facility';
jlab.editableRowTable.dialog.width = 400;
jlab.editableRowTable.dialog.height = 250;
jlab.editRow = function() {
    var manager = $("#row-manager").val(),
        logbooks = $("#row-logbooks").val(),
        facilityId = $(".editable-row-table tr.selected-row").attr("data-id"),
        reloading = false;

    $(".dialog-submit-button")
        .height($(".dialog-submit-button").height())
        .width($(".dialog-submit-button").width())
        .empty().append('<div class="button-indicator"></div>');
    $(".dialog-close-button").attr("disabled", "disabled");
    $(".ui-dialog-titlebar button").attr("disabled", "disabled");

    var request = jQuery.ajax({
        url: jlab.contextPath + "/ajax/edit-facility",
        type: "POST",
        data: {
            facilityId: facilityId,
            manager: manager,
            logbooks: logbooks
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
        window.console && console.log('Unable to edit facility; Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        var json = xhr.responseJSON,
            msg = 'Unable to Save: Server unavailable or unresponsive';
        if(json && json.error) {
            msg = json.error;
        }
        alert(msg);
    });

    request.always(function() {
        if (!reloading) {
            $(".dialog-submit-button").empty().text("Save");
            $(".dialog-close-button").removeAttr("disabled");
            $(".ui-dialog-titlebar button").removeAttr("disabled");
        }
    });
};
$(document).on("click", "#open-edit-row-dialog-button", function() {
    var $selectedRow = $(".editable-row-table tr.selected-row");
    $("#row-name").text($selectedRow.find("td:first-child").text());
    $("#row-manager").val($selectedRow.attr("data-manager"));
    $("#row-logbooks").val($selectedRow.attr("data-logbooks"));
});
$(document).on("dialogclose", "#table-row-dialog", function() {
    $("#row-form")[0].reset();
    $("#row-name").empty();
});
$(document).on("table-row-edit", function() {
    jlab.editRow();
});
