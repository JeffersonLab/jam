var jlab = jlab || {};
jlab.editableRowTable = jlab.editableRowTable || {};
jlab.editableRowTable.entity = 'Control';
jlab.editableRowTable.dialog.width = 400;
jlab.editableRowTable.dialog.height = 300;
jlab.addRow = function() {
    var name = $("#row-name").val(),
        description = $("#row-description").val(),
        doc = $("#row-doc").val(),
        teamId = $("#row-team").val(),
        frequency = $("#row-frequency").val(),
        reloading = false;

    $(".dialog-submit-button")
        .height($(".dialog-submit-button").height())
        .width($(".dialog-submit-button").width())
        .empty().append('<div class="button-indicator"></div>');
    $(".dialog-close-button").attr("disabled", "disabled");
    $(".ui-dialog-titlebar button").attr("disabled", "disabled");

    var request = jQuery.ajax({
        url: jlab.contextPath + "/ajax/add-control",
        type: "POST",
        data: {
            name: name,
            description: description,
            doc: doc,
            teamId: teamId,
            frequency: frequency
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
        window.console && console.log('Unable to add control; Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
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
     var controlId = $(".editable-row-table tr.selected-row").attr("data-id");

        reloading = false;

    $("#remove-row-button")
        .height($("#remove-row-button").height())
        .width($("#remove-row-button").width())
        .empty().append('<div class="button-indicator"></div>');

    var request = jQuery.ajax({
        url: jlab.contextPath + "/ajax/remove-control",
        type: "POST",
        data: {
            controlId: controlId
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
        window.console && console.log('Unable to remove control; Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
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
    var name = $(".editable-row-table tr.selected-row td:nth-child(1)").text();
    if (confirm('Are you sure you want to remove ' + name + '? \n WARNING: All history will be lost')) {
        jlab.removeRow();
    }
});