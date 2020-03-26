<#import "/spring.ftl" as spring/>
<#import "../components/vue-loader.ftl" as vueLoader/>
<#setting locale="de_DE">
<#setting number_format="computer">

<!DOCTYPE HTML>
<html lang="de">
<head>
    <title>Backup</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="/static/img/favicon.png"/>
    <link rel="apple-touch-icon" sizes="196x196" href="/static/img/favicon.png">
    <link rel="stylesheet" type="text/css" href="/static/css/materialize.min.css">
    <link rel="stylesheet" type="text/css" href="/static/css/material-icons.css">
    <link rel="stylesheet" type="text/css" href="/static/css/style.css">
    <style>
        [v-cloak] {
            display: none;
        }
    </style>
</head>

<body>

<@vueLoader.blank/>
<div id="app" style="display: flex; justify-content: center" v-cloak>
    <div style="width: 1300px; margin-bottom: 100px">
        <!-- title -->
        <div style="display: flex; justify-content: space-between; align-items: center; margin: 30px 50px 100px 50px">
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <img src="/static/img/favicon.png" style="width: 50px"/>
                <h4 style="color: #396e3a; margin-left: 10px; padding-bottom: 5px">Backup-System</h4>
            </div>

            <a @click="logout" class="waves-effect waves-light btn teal darken-3">
                <i class="material-icons right">exit_to_app</i>Abmelden</a>
        </div>

        <div class="card" style="min-height: 60vh; padding: 30px" :style="{ backgroundColor: error ? '#ffcdd2' : running ? 'white' : '#e8f5e9' }">
            <p style="margin: 0 0 20px 0; font-size: 2em">Konsolenausgabe</p>
            <div>
                <span v-html="logs"></span>
            </div>
            <a class="waves-effect waves-light btn green darken-3" style="margin-top: 30px" :style="{ display: running ? 'none' : 'inline-block' }" href="./admin">
                <i class="material-icons left">arrow_back</i>Zurück</a>
        </div>
    </div>

</div>
<script src="/static/js/materialize.min.js" ></script>
<script src="/static/js/axios.min.js" ></script>
<script src="/static/js/vue.min.js" ></script>
<script type="text/javascript">

    M.AutoInit();
    var instance;
    var app = new Vue({
        el: '#app',
        data: {
            logs: "",
            running: true,
            error: false
        },
        methods: {
            logout: function() {
                axios.post('./api/logout')
                    .then((response) => {
                        if(response.data.success) {
                            window.location = 'login';
                        } else {
                            M.toast({html: 'Logout fehlgeschlagen.'});
                        }
                    });
            }
        },
        mounted: function () {
            this.$nextTick(() => {
                instance = this;
                fetchLogs();
            });
        }
    });

    function fetchLogs() {
        axios.get('./api/log-data')
            .then((response) => {
                if(response.data) {
                    instance.logs = response.data.logs;
                    instance.running = response.data.running;
                    instance.error = response.data.error;

                    if(instance.running)
                        setTimeout(fetchLogs, 1000);
                    else if(!instance.error)
                        setTimeout(() => window.location = 'admin', 3000);
                }
            });
    }
</script>
</body>
</html>