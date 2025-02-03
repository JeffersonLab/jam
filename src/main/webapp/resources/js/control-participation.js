var jlab = jlab || {};
jlab.check = function($td) {
    $td.html("\u2714");
};
jlab.uncheck = function($td) {
    $td.empty();
};
jlab.restore = function($td, checked) {
    if (checked) {
        jlab.check($td);
    } else {
        jlab.uncheck($td);
    }
};
jlab.rfToggle = function() {
    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    jlab.requestStart();

    var $td = $(this),
        $tr = $td.closest("tr"),
        creditedControlId = $tr.attr("data-cc-id"),
        segmentId = $td.attr("data-segment-id"),
        checked = $td.html() !== null && ($.trim($td.html()) !== '');

    $td.html("<span class=\"button-indicator\"></span>");

    window.console && console.log("creditedControlId: " + creditedControlId + ", segmentId: " + segmentId);

    var request = jQuery.ajax({
        url: jlab.contextPath + "/ajax/toggle-rf-control-participation",
        type: "POST",
        data: {
            creditedControlId: creditedControlId,
            segmentId: segmentId
        },
        dataType: "html"
    });

    request.done(function(data) {
        if ($(".status", data).html() !== "Success") {
            jlab.restore($td, checked);
            alert('Unable to toggle: ' + $(".reason", data).html());
        } else {
            if (checked) {
                jlab.uncheck($td);
            } else {
                jlab.check($td);
            }
        }

    });

    request.fail(function(xhr, textStatus) {
        jlab.restore($td, checked);
        window.console && console.log('Unable to toggle: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to toggle');
    });

    request.always(function() {
        jlab.requestEnd();
    });
};
jlab.beamToggle = function() {
    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    jlab.requestStart();

    var $td = $(this),
            $tr = $td.closest("tr"),
            creditedControlId = $tr.attr("data-cc-id"),
            destinationId = $td.attr("data-destination-id"),
            checked = $td.html() !== null && ($.trim($td.html()) !== '');

    $td.html("<span class=\"button-indicator\"></span>");

    window.console && console.log("creditedControlId: " + creditedControlId + ", destinationId: " + destinationId);

    var request = jQuery.ajax({
        url: jlab.contextPath + "/ajax/toggle-beam-control-participation",
        type: "POST",
        data: {
            creditedControlId: creditedControlId,
            destinationId: destinationId
        },
        dataType: "html"
    });

    request.done(function(data) {
        if ($(".status", data).html() !== "Success") {
            jlab.restore($td, checked);
            alert('Unable to toggle: ' + $(".reason", data).html());
        } else {
            if (checked) {
                jlab.uncheck($td);
            } else {
                jlab.check($td);
            }
        }

    });

    request.fail(function(xhr, textStatus) {
        jlab.restore($td, checked);
        window.console && console.log('Unable to toggle: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to toggle');
    });

    request.always(function() {
        jlab.requestEnd();
    });
};
$(document).on("click", ".rf-content .control-participation-table.editable td", function() {
    jlab.rfToggle.call(this);
});
$(document).on("click", ".beam-content .control-participation-table.editable td", function() {
    jlab.beamToggle.call(this);
});
$(function() {
    $( ".accordion" ).accordion({
        collapsible: true,
        heightStyle: "content",
        active: 0
    });
});