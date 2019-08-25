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
                });

                var active = $('#board-sec-' + data.activeID);
                if(active) {
                    active.html(data.activeSeconds+'s');
                }

                $('#active-counter').html('aktive Blackboards: '+data.activeClients);
            }
        }
    );
}

function initBlackboardAdmin(initURL) {
    url = initURL;
    setInterval(fetchBlackboardAdmin, refreshDelayBlackboardAdmin);
}