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
                    var html = $(this).html();
                    var indexSlash = html.indexOf('/');
                    if(indexSlash >= 0) {
                        $(this).html('('+html.substr(indexSlash+1));
                        $(this).css({ color: 'black' });
                    }
                });

                var active = $('#board-sec-' + data.activeID);
                var value = active.html();
                if(value) {
                    var indexSlash = value.indexOf('/');
                    if(indexSlash >= 0) {
                        active.html('('+data.activeSeconds+'/'+value.substr(indexSlash+1));
                    } else {
                        active.html('('+data.activeSeconds+'/'+value.substr(1));
                    }
                    active.css({ color: 'green' });
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