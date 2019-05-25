var refreshDelayBlackboard = 1000; // every sec
var currentTimestamp;
var url;


function updateBlackboard() {

    $.ajax({
            method: 'GET',
            url: url,
            success: function (data) {

                if(currentTimestamp && currentTimestamp !== data.timestamp) {
                    currentTimestamp = data.timestamp;
                    location.reload();
                    return;
                }

                currentTimestamp = data.timestamp;
                setTimeout(updateBlackboard, refreshDelayBlackboard);
            }
        }
    );
}

function initBlackboard(initURL) {
    url = initURL;
    setTimeout(updateBlackboard, refreshDelayBlackboard);
}

