// KONSTANTEN
var scrollInterval = 16; // 1 tick = 16ms (60 FPS)
var distancePerTick = 1.3; // pro tick: 1 pixel nach unten scrollen
var docHeight; // höhe des dokuments, wird automatisch initialisiert
var waitTicks = 200; // ca. 3 sekunden(16*200) warten, wenn man unten/oben ist

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

function initScroller() {
    docHeight = getDocHeight();
    setInterval(scroller, scrollInterval);
}
