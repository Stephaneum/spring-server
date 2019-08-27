var refreshDelayBlackboardAdmin = 1000; // every 1 sec
var url;


function fetchBlackboardAdmin() {

    $.ajax({
            method: 'GET',
            url: url,
            timeout: 10000, // timeout 10s
            success: function (data) {

                // hide all counters
                $(".board-sec-class").each(function(i, obj) {
                    $(this).html(null);
                    $(this).css({ visibility: 'hidden'});
                });

                var active = $('#board-sec-' + data.activeID);
                if(active) {
                    active.html(data.activeSeconds+'s');
                    active.css({ visibility: 'visible'});
                }

                $('#active-counter').html('aktive Blackboards: '+data.activeClients);

                // dev: print time to refresh every 5s
                if(data.timeToRefresh % 5 == 0) {
                    console.log('Time to refresh: ' + data.timeToRefresh+'s')
                }
            }
        }
    );
}

function initBlackboardAdmin(initURL) {
    url = initURL;
    setInterval(fetchBlackboardAdmin, refreshDelayBlackboardAdmin);
}