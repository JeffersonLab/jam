var jlab = jlab || {};
$(document).on("click", "#expired-link", function () {
    $("#expired-dialog").dialog("open");
    return false;
});
$(document).on("click", "#expiring-link", function () {
    $("#expiring-dialog").dialog("open");
    return false;
});
$(document).on("click", ".default-clear-panel", function () {
    $("#facility-select").val('');
    return false;
});