var refreshDelayLogs = 1000; // every 1 sec
var url;
var logsTimer;

function fetchBlackboardAdmin() {

    $.ajax({
            method: 'GET',
            url: url,
            timeout: 10000, // timeout 10s
            success: function (data) {
                if(!data.running) {
                    if (data.error) {
                        document.getElementById('log-container').style.backgroundColor = '#ffcdd2';
                    } else {
                        location.reload();
                    }
                    document.getElementById('log-back-btn').style.display = 'inline-block';
                    clearInterval(logsTimer);
                }
                document.getElementById('log').innerHTML = data.log;


            }
        }
    );
}

function initLogs(initURL) {
    url = initURL;
    logsTimer = setInterval(fetchBlackboardAdmin, refreshDelayLogs);
}