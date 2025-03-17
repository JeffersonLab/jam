var jlab = jlab || {};
/* String Date format: YYYY-MM-DD hh:mm */
jlab.dateTimeFromIsoString = function(x) {
    var year = parseInt(x.substring(0,4)),
    month = parseInt(x.substring(5,7)),
    day = parseInt(x.substring(8,10)),
    hour = parseInt(x.substring(11,13)),
    minute = parseInt(x.substring(14,16));
        
    return new Date(year, month - 1, day, hour, minute);
};
jlab.dateTimeToIsoString = function(x) {
    var year = x.getFullYear(),
    month = x.getMonth() + 1,
    day = x.getDate(),
    hour = x.getHours(),
    minute = x.getMinutes();
        
    return year + '-' + jlab.pad(month, 2) + '-' + jlab.pad(day, 2) + ' ' + jlab.pad(hour, 2) + ':' + jlab.pad(minute, 2);
};
jlab.addS = function(x) {
    if(x !== 1) {
        return 's ';
    } else {
        return ' ';
    }
};
jlab.millisecondsToHumanReadable = function(milliseconds) {
    var days = Math.floor(milliseconds / 86400000),
    remainingMilliseconds = milliseconds % 86400000,
    hours = Math.floor((remainingMilliseconds) / 3600000),
    remainingMilliseconds2 = remainingMilliseconds % 3600000,
    minutes = Math.floor(remainingMilliseconds2 / 60000);
    
    return (days > 0 ? days + ' day' + jlab.addS(days) : '') + (hours > 0 ? hours + ' hour' + jlab.addS(hours) : '') + minutes + ' minute' + jlab.addS(minutes);
};
$(function() {
    $(".dialog").dialog({
        autoOpen: false,
        width: 640,
        height: 480,
        modal: true
    });    
});