var refreshDelayBlackboard = 1500; // every 1.5 sec
var currentTimestamp;
var url;
var requestFetchBlackboard = true;


function fetchBlackboard() {

    $.ajax({
            method: 'GET',
            url: url,
            timeout: 10000, // timeout 10s
            success: function (data) {
                if (data.timestamp && currentTimestamp !== data.timestamp) {
                    location.reload();
                    return;
                }

                requestFetchBlackboard = true;
            },
            error: function () {
                // try again
                requestFetchBlackboard = true;
            }
        }
    );
}

function updateBlackboard() {
    if(requestFetchBlackboard) {
        requestFetchBlackboard = false;
        fetchBlackboard();
    }
}

function initBlackboard(initURL, timestamp) {
    url = initURL;
    currentTimestamp = timestamp;
    setInterval(updateBlackboard, refreshDelayBlackboard);
}

