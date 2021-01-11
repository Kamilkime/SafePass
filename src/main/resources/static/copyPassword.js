// https://stackoverflow.com/questions/400212/how-do-i-copy-to-the-clipboard-in-javascript
// https://stackoverflow.com/questions/247483/http-get-request-in-javascript

function copyPassword(entryID) {
    let textArea = document.createElement("textarea");

    textArea.style.position = 'fixed';
    textArea.style.top = 0;
    textArea.style.left = 0;
    textArea.style.width = '2em';
    textArea.style.height = '2em';
    textArea.style.padding = 0;
    textArea.style.border = 'none';
    textArea.style.outline = 'none';
    textArea.style.boxShadow = 'none';
    textArea.style.background = 'transparent';

    let xmlHttp = new XMLHttpRequest();
    xmlHttp.open("GET", "/getPassword?entryID=" + entryID, false);
    xmlHttp.send(null);

    textArea.value = xmlHttp.responseText;
    document.body.appendChild(textArea);

    textArea.focus();
    textArea.select();

    document.execCommand('copy');
    document.body.removeChild(textArea);
}