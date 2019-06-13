var refreshDelayBlackboard = 1500; // every sec
var currentTimestamp;
var url;
var requestFetchBlackboard = true;


function fetchBlackboard() {

    $.ajax({
            method: 'GET',
            url: url,
            timeout: 10000, // timeout 10s
            success: function (data) {
                if (currentTimestamp !== data.timestamp) {
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
        updateBlackboard = false;
        fetchBlackboard();
    }
}

function initBlackboard(initURL, timestamp) {
    url = initURL;
    currentTimestamp = timestamp;
    setInterval(updateBlackboard, refreshDelayBlackboard);
}

