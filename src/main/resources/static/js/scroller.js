var refreshDelayDoc = 1000*60*10; // every 10min
var initDelay = 2000;
var waitTopDuration = 2000;
var waitBottomDuration = 2000;
var scrollSpeed = 0.5;

function getDocHeight() {
    return Math.max(
        document.body.scrollHeight, document.documentElement.scrollHeight,
        document.body.offsetHeight, document.documentElement.offsetHeight,
        document.body.clientHeight, document.documentElement.clientHeight
    );
}

function scroller() {
    $("html, body").animate({ scrollTop: getDocHeight() }, getDocHeight() / scrollSpeed, 'linear', waitBottom);
}

function waitBottom() {
    console.log('wait bottom');
    setTimeout(waitTop, waitBottomDuration)
}

function waitTop() {
    $(window).scrollTop(0);
    setTimeout(scroller, waitTopDuration);
}

function init() {
    setTimeout(location.reload, refreshDelayDoc);
    scroller();
}

setTimeout(init, initDelay);

