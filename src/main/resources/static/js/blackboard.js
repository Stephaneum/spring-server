var refreshDelayBlackboard = 1000; // every sec
var currentTimestamp;
var url;


function updateBlackboard() {

    $.ajax({
            method: 'GET',
            url: url,
            timeout: 10000, // timeout 10s
            success: function (data) {
                if (currentTimestamp !== data.timestamp) {

                    currentTimestamp = data.timestamp;
                    location.reload();
                    return;
                }

                currentTimestamp = data.timestamp;
                setTimeout(updateBlackboard, refreshDelayBlackboard);
            },
            error: function () {
                // try again
                setTimeout(updateBlackboard, refreshDelayBlackboard);
            }
        }
    );
}

function initBlackboard(initURL, timestamp) {
    url = initURL;
    currentTimestamp = timestamp;
    setTimeout(updateBlackboard, refreshDelayBlackboard);
}

