var refreshDelayLogs = 1000; // every 1 sec
var url;


function fetchBlackboardAdmin() {

    $.ajax({
            method: 'GET',
            url: url,
            timeout: 10000, // timeout 10s
            success: function (data) {
                if(!data.running) {
                    location.reload();
                    return;
                }
                document.getElementById('log').innerHTML = data.log;


            }
        }
    );
}

function initLogs(initURL) {
    url = initURL;
    setInterval(fetchBlackboardAdmin, refreshDelayLogs);
}