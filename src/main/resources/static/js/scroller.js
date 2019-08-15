// KONSTANTEN
var initDelay = 1000; // 1 Sekunde initialisierung
var scrollInterval = 20; // 1 tick = 25ms
var distancePerTick = 1; // pro tick: 2 pixel nach unten scrollen
var docHeight; // höhe des dokuments, wird automatisch initialisiert
var waitTicks = 200; // 4 sekunden(20*200) warten, wenn man unten ist

// VARIABLEN
var currentY = 0; // momentane y koordinate
var waitTop = 0; // Anzahl ticks zum warten verbleibend (oben)
var waitBottom = 0; // Anzahl ticks zum warten verbleibend (unten)

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

function init() {
    docHeight = getDocHeight();
    setInterval(scroller, scrollInterval);
}

setTimeout(init, initDelay);
