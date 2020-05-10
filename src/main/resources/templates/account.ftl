<#import "/spring.ftl" as spring/>
<#import "components/loading.ftl" as loading/>
<#import "components/vue-loader.ftl" as vueLoader/>
<#import "components/menu.ftl" as menu/>
<#import "components/footer.ftl" as footer/>
<#import "components/utils.ftl" as utils/>

<!DOCTYPE HTML>
<html lang="de">
<head>
    <title>Account - Stephaneum</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="/static/img/favicon.png" />
    <link rel="apple-touch-icon" sizes="196x196" href="/static/img/favicon.png">
    <link rel="stylesheet" type="text/css" href="/static/css/materialize.min.css">
    <link rel="stylesheet" type="text/css" href="/static/css/material-icons.css">
    <link rel="stylesheet" type="text/css" href="/static/css/style.css">
    <style>
        [v-cloak] {
            display: none;
        }

        .account-section-header {
            display: flex;
            align-items: center;
            margin-bottom: 20px;
        }

        .account-section-header span {
            margin-left: 10px;
            font-size: 1.5rem;
        }

        .account-key {
            font-weight: bold;
        }
    </style>
</head>

<body>

<@vueLoader.blank/>
<div id="app" v-cloak>
    <nav-menu :menu="info.menu" :has-menu-write-access="info.hasMenuWriteAccess" :user="info.user" :plan="info.plan" :unapproved="info.unapproved"></nav-menu>
    <div v-if="allowed" style="margin: 50px auto 0 auto; max-width: 1200px; min-height: calc(100vh - 350px)">

        <div style="text-align: center; margin: 60px 0 60px 0">
            <i class="material-icons" style="font-size: 4em">account_circle</i>
            <h4 style="margin: 0">Account</h4>
        </div>

        <div class="row">
            <div class="col s4" style="padding: 20px">
                <div class="card-panel">
                    <div class="account-section-header">
                        <i class="material-icons">info</i>
                        <span>Informationen</span>
                    </div>
                    <div style="display: grid; column-gap: 10px; row-gap: 10px; grid-template-columns: min-content auto; grid-template-rows: auto auto auto auto auto auto">
                        <div style="grid-area: 1 / 1 / 1 / 1;" class="account-key">Vorname:</div>
                        <div style="grid-area: 1 / 2 / 1 / 2;">{{ info.user.firstName }}</div>
                        <div style="grid-area: 2 / 1 / 2 / 1;" class="account-key">Nachname:</div>
                        <div style="grid-area: 2 / 2 / 2 / 2;">{{ info.user.lastName }}</div>
                        <div style="grid-area: 3 / 1 / 3 / 1;" class="account-key">E-Mail:</div>
                        <div style="grid-area: 3 / 2 / 3 / 2;">{{ info.user.email }}</div>
                        <div style="grid-area: 4 / 1 / 4 / 1;" class="account-key">Rolle:</div>
                        <div style="grid-area: 4 / 2 / 4 / 2;">{{ info.user.code.roleString }}</div>
                        <div style="grid-area: 5 / 1 / 5 / 1;" class="account-key">Klasse:</div>
                        <div style="grid-area: 5 / 2 / 5 / 2;">{{ additionalInfo.schoolClass }}</div>
                        <div style="grid-area: 6 / 1 / 6 / 1;" class="account-key">Speicherplatz:</div>
                        <div style="grid-area: 6 / 2 / 6 / 2;">{{ additionalInfo.storage }}</div>
                    </div>
                </div>
            </div>

            <div class="col s4" style="padding: 20px">
                <div class="card-panel">
                    <div class="account-section-header">
                        <i class="material-icons">email</i>
                        <span>E-Mail ändern</span>
                    </div>
                    <div class="input-field">
                        <label for="account-email">E-Mail</label>
                        <input v-model:value="email" type="text" id="account-email"/>
                    </div>

                    <div style="text-align: right">
                        <a class="waves-effect waves-light btn green darken-3" :class="email ? [] : ['disabled']"
                           @click="updateEmail">
                            <i class="material-icons left">save</i>
                            Speichern
                        </a>
                    </div>
                </div>
            </div>

            <div class="col s4" style="padding: 20px">
                <div class="card-panel">
                    <div class="account-section-header">
                        <i class="material-icons">vpn_key</i>
                        <span>Passwort ändern</span>
                    </div>
                    <div class="input-field">
                        <label for="account-old-password">altes Passwort</label>
                        <input v-model:value="oldPassword" type="password" id="account-old-password"/>
                    </div>
                    <div class="input-field">
                        <label for="account-new-password">neues Passwort</label>
                        <input v-model:value="newPassword" type="password" id="account-new-password"/>
                    </div>
                    <div class="input-field">
                        <label for="account-new-password-repeat">neues Passwort wiederholen</label>
                        <input v-model:value="newPasswordRepeat" type="password" id="account-new-password-repeat"/>
                    </div>

                    <div style="text-align: right">
                        <a class="waves-effect waves-light btn green darken-3" :class="oldPassword && newPassword && newPasswordRepeat ? [] : ['disabled']"
                           @click="updatePassword">
                            <i class="material-icons left">save</i>
                            Speichern
                        </a>
                    </div>
                </div>
            </div>
        </div>

    </div>
    <div v-else style="flex: 1; min-height: calc(100vh - 100px); display: flex; flex-direction: column; align-items: center; justify-content: center"></div>

    <div style="height: 100px"></div>

    <stephaneum-footer :copyright="info.copyright"></stephaneum-footer>
</div>

<script src="/static/js/materialize.min.js" ></script>
<script src="/static/js/axios.min.js" ></script>
<script src="/static/js/vue.js" ></script>
<@utils.render/>
<@loading.render/>
<@menu.render/>
<@footer.render/>
<script type="text/javascript">
    var app = new Vue({
        el: '#app',
        data: {
            info: { user: null, hasMenuWriteAccess: false, menu: null, plan: null, copyright: null, unapproved: null },
            additionalInfo: { schoolClass: null, used: 1, total: 1, storage: null },
            email: null,
            oldPassword: null,
            newPassword: null,
            newPasswordRepeat: null
        },
        methods: {
            fetchData: async function() {
                try {
                    const info = await axios.get('/api/info');
                    this.info = info.data;

                    const additional = await axios.get('/api/account/info');
                    this.additionalInfo = additional.data;
                    this.additionalInfo.storage = storageReadable(additional.data.used) + ' / ' + storageReadable(additional.data.total);
                } catch (e) {
                    M.toast({html: 'Ein Fehler ist aufgetreten.'});
                }

                hideLoading();
            },
            updateEmail: async function() {

                if(!this.email) {
                    M.toast({html: 'Leere E-Mail'});
                    return;
                }

                showLoadingInvisible();
                try {
                    await axios.post('/api/account/email', { email: this.email });
                    await this.fetchData();
                    M.toast({html: 'E-Mail geändert.'});
                    this.email = null;
                    this.$nextTick(() => M.updateTextFields());
                } catch (e) {
                    switch (e.response.status) {
                        case 409:
                            M.toast({html: 'E-Mail bereits in Verwendung.'});
                            break;
                        case 410:
                            M.toast({html: 'E-Mail ist nicht gültig.'});
                            break;
                        default:
                            M.toast({html: 'Ein Fehler ist aufgetreten.'});
                            break;
                    }
                    hideLoading();
                }
            },
            updatePassword: async function() {

                if(this.newPassword !== this.newPasswordRepeat) {
                    M.toast({html: 'Passwörter stimmen nicht überein.'});
                    return;
                }

                showLoadingInvisible();
                try {
                    await axios.post('/api/account/password', { oldPassword: this.oldPassword, newPassword: this.newPassword });
                    await this.fetchData();
                    M.toast({html: 'Passwort geändert.'});
                    this.oldPassword = null;
                    this.newPassword = null;
                    this.newPasswordRepeat = null;
                    this.$nextTick(() => M.updateTextFields());
                } catch (e) {
                    switch (e.response.status) {
                        case 403:
                            M.toast({html: 'Falsches Passwort.'});
                            break;
                        default:
                            M.toast({html: 'Ein Fehler ist aufgetreten.'});
                            break;
                    }
                    hideLoading();
                }
            }
        },
        computed: {
            allowed: function() {
                return this.info.user && this.info.user.code.role >= 0;
            }
        },
        mounted: function() {
            M.AutoInit();
            this.fetchData();
        }
    });
</script>
</body>
</html>