
/**
 * Returns the IE version. Tested for IE11 and earlier.
 */
function isIE () {
    var match = navigator.userAgent.match(/(?:MSIE |Trident\/.*; rv:)(\d+)/);
    return match ? parseInt(match[1]) : undefined;
}

/**
 * Converts plain text to HTML
 * @param text the plain text to convert
 * @returns the HTML version of the text
 */
function plain2html(text) {
    text = (text || "");
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/\t/g, "    ")
        .replace(/ /g, "&#8203;&nbsp;&#8203;")
        .replace(/\r\n|\r|\n/g, "<br />");
}

/**
 * formats the series identifier.
 * If the number is undefined, the blank string is returned
 * @param msg the series identifier
 */
function formatSeriesIdentifier(msg) {
    if (msg && msg.seriesIdentifier && msg.seriesIdentifier.number) {
        var id = msg.seriesIdentifier.fullId;
        if (msg.type == 'TEMPORARY_NOTICE') {
            id += '(T)';
        } else if (msg.type == 'PRELIMINARY_NOTICE') {
            id += '(P)';
        }
        id += '.';
        return id;
    }
    return '';
}