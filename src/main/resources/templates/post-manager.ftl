
<#import "/spring.ftl" as spring/>
<#import "components/vue-loader.ftl" as vueLoader/>

<!DOCTYPE HTML>
<html lang="de">
<head>
    <title>Beiträge - Stephaneum</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="<@spring.url '/static/img/favicon.png' />" />
    <link rel="apple-touch-icon" sizes="196x196" href="<@spring.url '/static/img/favicon.png' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/materialize.min.css' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/material-icons.css' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/style.css' />">
    <style>
        .info {
            margin-top: 20px;
            font-style: italic;
        }

        [v-cloak] {
            display: none;
        }
    </style>
</head>

<body>

<@vueLoader.blank/>
<div id="app" style="height: 100vh" v-cloak>
</div>

<script src="/static/js/materialize.min.js" ></script>
<script src="/static/js/axios.min.js" ></script>
<script src="/static/js/vue.min.js" ></script>
<script type="text/javascript">
    M.AutoInit();

    var app = new Vue({
        el: '#app',
        data: {
            password: null,
            loginFailed: false,
            loggingIn: false
        },
        methods: {
            login: function() {
                this.loggingIn = true;
                axios.post('login', { password: this.password })
                    .then((response) => {
                        if(response.data.success) {
                            this.loginFailed = false;
                            window.location = 'admin';
                        } else {
                            this.loginFailed = true;
                            this.loggingIn = false;
                            M.toast({html: 'Login fehlgeschlagen.'});
                        }
                    });
            }
        }
    });
</script>
</body>
</html>