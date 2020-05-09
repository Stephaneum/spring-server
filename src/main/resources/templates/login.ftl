<#-- @ftlvariable name="title" type="java.lang.String" -->

<#import "/spring.ftl" as spring/>
<#import "components/vue-loader.ftl" as vueLoader/>
<#import "components/menu.ftl" as menu/>
<#import "components/footer.ftl" as footer/>

<!DOCTYPE HTML>
<html lang="de">
<head>
    <title>Anmelden - Stephaneum</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="/static/img/favicon.png" />
    <link rel="apple-touch-icon" sizes="196x196" href="/static/img/favicon.png">
    <link rel="stylesheet" type="text/css" href="/static/css/materialize.min.css">
    <link rel="stylesheet" type="text/css" href="/static/css/material-icons.css">
    <link rel="stylesheet" type="text/css" href="/static/css/style.css">
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
<div id="app" style="display: flex; align-items: center; flex-direction: column; min-height: 100vh" v-cloak>
    <nav-menu :menu="info.menu" :has-menu-write-access="info.hasMenuWriteAccess" :user="info.user" :plan="info.plan" :unapproved="info.unapproved"></nav-menu>
    <div style="flex: 1; display: flex; align-items: center; justify-content: center">
        <div style="max-width: 500px">
            <div class="card" style="margin-top: 100px; padding: 5px 20px 40px 20px">
                <h5 class="center-align" style="padding: 10px; margin-bottom: 30px">Login</h5>
                <div class="input-field">
                    <i class="material-icons prefix">person</i>
                    <label for="email">E-Mail</label>
                    <input @keyup.enter="login" v-model:value="email" :disabled="loggingIn" type="text" id="email"/>
                </div>
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

    <stephaneum-footer :copyright="info.copyright"></stephaneum-footer>
</div>

<script src="/static/js/materialize.min.js" ></script>
<script src="/static/js/axios.min.js" ></script>
<script src="/static/js/vue.min.js" ></script>
<@menu.render/>
<@footer.render/>
<script type="text/javascript">
    M.AutoInit();

    var app = new Vue({
        el: '#app',
        data: {
            info: { user: null, menu: null, plan: null, copyright: null, unapproved: null },
            email: null,
            password: null,
            loginFailed: false,
            loggingIn: false
        },
        methods: {
            login: function() {
                if(!this.email) {
                    document.getElementById("email").focus();
                    return;
                } else if(!this.password) {
                    document.getElementById("password").focus();
                    return;
                }

                this.loggingIn = true;
                axios.post('/api/login', { email: this.email, password: this.password })
                    .then((response) => {
                    if(response.data.success) {
                        this.loginFailed = false;
                        window.location = '/admin-config';
                    } else {
                        this.loginFailed = true;
                        this.loggingIn = false;
                        M.toast({html: 'Login fehlgeschlagen.'});
                    }
                });
            }
        },
        mounted: function() {
            axios.get('/api/info')
                .then((res) => {
                    if(res.data) {
                        this.info = res.data;
                    } else {
                        M.toast({html: 'Interner Fehler.'});
                    }
                });
        }
    });
</script>
</body>
</html>