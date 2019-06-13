var refreshDelayDoc = 1000*60*10; // every 10min
var initDelay = 2000;
var waitTopDuration = 1500;
var waitBottomDuration = 1500;
var scrollSpeed = 0.15;
var bugfixSwitch = false;

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
    if(!bugfixSwitch) {
        bugfixSwitch = true;
        return;
    }
    console.log('wait bottom');
    setTimeout(waitTop, waitBottomDuration);
    bugfixSwitch = false;
}

function waitTop() {
    $(window).scrollTop(0);
    setTimeout(scroller, waitTopDuration);
}

function initScoller() {
    setTimeout(location.reload, refreshDelayDoc);
    scroller();
}

setTimeout(initScoller, initDelay);

