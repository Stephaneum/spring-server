var refreshDelayDoc = 1000*60*10; // every 10min
var initDelay = 1000;
var scrollInterval = 20;
var distancePerTick = 2;
var docHeight; // will be set after init
var waitTicks = 200; // wait 4s when reached top or bottom

var currentY = 0;
var waitTop = 0;
var waitBottom = 0;

function scroller() {

    if(waitBottom !== 0) {
        waitBottom--;

        if(waitBottom === 0) {
            currentY = 0;
            window.scrollTo(0, currentY);
            docHeight = getDocHeight(); // update in case window is resized
            waitTop = waitTicks;
        }
        return;
    }

    if(waitTop !== 0) {
        waitTop--;
        return;
    }

    window.scrollTo(0, currentY);

    currentY += distancePerTick;
    if(currentY + window.innerHeight >= docHeight) {
        waitBottom = waitTicks;
    }
}

function getDocHeight() {
    return Math.max(
        document.body.scrollHeight, document.documentElement.scrollHeight,
        document.body.offsetHeight, document.documentElement.offsetHeight,
        document.body.clientHeight, document.documentElement.clientHeight
    );
}

function reloadDoc() {
    console.log("reload");
    location.reload();
}

function init() {
    docHeight = getDocHeight();
    setInterval(scroller, scrollInterval);
    setInterval(reloadDoc, refreshDelayDoc);
}

setTimeout(init, initDelay);

