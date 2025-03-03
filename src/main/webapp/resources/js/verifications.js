var jlab = jlab || {};
$(document).on("click", "#expiring-link", function () {
    $("#expiring-dialog").dialog("open");
    return false;
});
$(document).on("click", ".default-clear-panel", function () {
    $("#facility-select").val('');
    $("#team-select").val('');
    return false;
});
$( ".accordion" ).accordion({
    collapsible: true,
    heightStyle: "content",
    active: 0
});
$(function() {
   $("#expiring-dialog").dialog({
       width: 900,
       height: 800
   });
});