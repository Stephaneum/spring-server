<#-- @ftlvariable name="title" type="java.lang.String" -->

<#import "/spring.ftl" as spring/>
<#import "components/toaster.ftl" as toaster/>

<!DOCTYPE HTML>
<html lang="de">
<head>
    <title>${title}</title>
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
    </style>
</head>

<body>

<div id="app" class="valign-wrapper" style="height: 100vh">
    <div style="margin: auto; max-width: 400px">

        <div class="center-align">
            <img src="<@spring.url '/static/img/logo-banner-green.png' />" style="width: 100%"/>
        </div>

        <div class="card" style="margin-top: 100px; padding: 5px 20px 40px 20px">
            <h5 class="center-align" style="padding: 10px; margin-bottom: 30px">${title}</h5>
            <div class="input-field">
                <i class="material-icons prefix">vpn_key</i>
                <label for="password">Passwort</label>
                <input @keyup.enter="login" v-model:value="password" :disabled="loggingIn" type="password" id="password"/>
            </div>
            <div style="text-align: right">
                <button @click="login" type="button" value="Login" class="btn waves-effect waves-light green darken-3" :class="{ disabled: loggingIn }">
                    Login
                    <i class="material-icons right">send</i>
                </button>
            </div>
        </div>
        <div style="text-align: center">
            <p class="info" v-show="!loggingIn && !loginFailed" style="visibility: hidden">placeholder</p>
            <p class="info" v-show="loggingIn" style="display: none">Authentifizierung</p>
            <p class="info red-text" v-show="!loggingIn && loginFailed" style="display: none">Login fehlgeschlagen</p>
        </div>
    </div>
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
                axios({
                    method: 'post',
                    url: 'login',
                    data: { password: this.password }
                }).then((response) => {
                    if(response.data.success) {
                        this.loginFailed = false;
                        window.location = 'admin';
                    } else {
                        this.loginFailed = true;
                        M.toast({html: 'Login fehlgeschlagen.'});
                    }
                    this.loggingIn = false;
                });
            }
        }
    });
</script>
<@toaster.render/>
</body>
</html>