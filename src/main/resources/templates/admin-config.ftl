<#import "/spring.ftl" as spring/>
<#import "components/loading.ftl" as loading/>
<#import "components/vue-loader.ftl" as vueLoader/>
<#import "components/menu.ftl" as menu/>
<#import "components/footer.ftl" as footer/>
<#import "components/utils.ftl" as utils/>
<#import "components/user-add-list.ftl" as userAddList/>
<#import "components/user-search.ftl" as userSearch/>

<!DOCTYPE HTML>
<html lang="de">
<head>
    <title>Konfiguration - Stephaneum</title>
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
    </style>
</head>

<body>

<@vueLoader.blank/>
<div id="app" v-cloak>
    <nav-menu :menu="info.menu" :has-menu-write-access="info.hasMenuWriteAccess" :user="info.user" :plan="info.plan" :unapproved="info.unapproved"></nav-menu>
    <div v-if="allowed" style="margin: 50px auto 0 auto; max-width: 1200px; min-height: calc(100vh - 350px)">

        <div style="text-align: center; margin: 60px 0 60px 0">
            <i class="material-icons" style="font-size: 4em">build</i>
            <h4 style="margin: 0">Konfiguration</h4>
        </div>

        <nav-menu :menu="menu" unreal="true" edit-mode="true" :edit-root-level="menuAdmin" @select="selectMenu" @group="showCreateGroup" @link="showCreateLink"></nav-menu>

        <div v-if="menuAdmin" style="padding-top: 20px; padding-left: 50px; display: flex; justify-content: space-between">
            <span>Startseite: <b>{{ defaultMenu ? defaultMenu.name : '-' }}</b></span>
            <span>Je höher die Priorität, desto weiter oben/links ist das Element eingeordnet.</span>
        </div>

        <div v-else style="margin-top: 100px; text-align: center; font-size: 1.2em">
            Je höher die Priorität, desto weiter oben/links ist das Element eingeordnet.
        </div>

        <div v-if="admin" class="card-panel" style="margin-top: 60px">
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px">
                <span style="font-size: 2em">Schreibrechte</span>
                <a @click="showCreateRule" style="margin-right: 20px" class="tooltipped waves-effect waves-light btn-floating green darken-4"
                   data-tooltip="Neue Regel" data-position="top" href="#!">
                    <i class="material-icons">add</i>
                </a>
            </div>

            <p style="margin: 0">Nutzer können in diesen Menüpunkten (inkl. Untermenüs) <b>Beiträge</b> erstellen und bearbeiten - sowie das <b>Menü</b> selber bearbeiten.</p>
            <p style="margin: 0">Regeln sind <b>kombinierbar</b>, z.B. kann Nutzer A für die Menüpunkte M1 und M2 Schreibrechte zugewiesen werden.</p>
            <br>

            <ul v-if="rules.length !== 0" class="collection">
                <li v-for="r in rules" class="collection-item">
                    <div style="display: flex; align-items: center;">
                        <span style="flex: 0 0 400px; display: flex; align-items: center">
                            <i class="material-icons grey-text text-darken-2">person</i>
                            <span style="margin-left: 10px">{{ r.user.firstName }} {{ r.user.lastName }} ({{ r.user.code.roleString }})</span>
                        </span>

                        <span style="flex: 1">
                            <span v-if="r.menu">
                                <span style="font-size: 1.1rem; color: grey">{{ menuPath(r.menu) }}</span>
                                <span style="font-size: 1.1rem; font-weight: bold">{{ r.menu.name }}</span>
                            </span>
                            <span v-else style="font-size: 1.4rem; font-weight: bold">
                                <i class="material-icons left" style="margin-right: 5px">star</i>
                                ALLE
                            </span>
                        </span>

                        <a class="tooltipped waves-effect waves-light btn red darken-3" data-tooltip="Regel löschen" data-position="top" style="margin-left: 10px"
                           @click="showDeleteRule(r)">
                            <i class="material-icons">delete</i>
                        </a>
                    </div>
                </li>
            </ul>
        </div>

    </div>
    <div v-else style="flex: 1; min-height: calc(100vh - 100px); display: flex; flex-direction: column; align-items: center; justify-content: center">
        <span>Keine Berechtigung.</span>
        <span>Bei Fragen wende dich bitte an dem Administrator.</span>
    </div>

    <div style="height: 100px"></div>

    <stephaneum-footer :copyright="info.copyright"></stephaneum-footer>

    <!-- create menu modal -->
    <div id="modal-create-menu" class="modal" style="width: 500px">
        <div class="modal-content">
            <h4>{{ selectedMenu.linkMode ? 'Neuer Link' : 'Neue Gruppe' }}</h4>
            <p>Wird erstellt in <b>{{ selectedMenu.parent ? selectedMenu.parent.name : 'Hauptleiste' }}</b></p>
            <br>
            <div class="input-field">
                <i class="material-icons prefix">edit</i>
                <label for="create-menu-name">Name</label>
                <input v-model:value="selectedMenu.name" type="text" id="create-menu-name"/>
            </div>
            <div v-if="selectedMenu.linkMode" class="input-field">
                <i class="material-icons prefix">language</i>
                <label for="create-menu-link">Link</label>
                <input v-model:value="selectedMenu.link" type="text" id="create-menu-link"/>
            </div>
            <div v-else class="input-field">
                <i class="material-icons prefix">vpn_key</i>
                <label for="create-menu-password">Passwort</label>
                <input v-model:value="selectedMenu.password" type="password" id="create-menu-password"/>
            </div>
            <div class="input-field">
                <i class="material-icons prefix">low_priority</i>
                <label for="create-menu-priority">Priorität</label>
                <input v-model:value="selectedMenu.priority" type="text" id="create-menu-priority"/>
            </div>
        </div>
        <div class="modal-footer">
            <a href="#!"
               class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
            <button @click="createMenu" type="button" class="btn waves-effect waves-light green darken-3">
                <i class="material-icons left">add</i>
                Erstellen
            </button>
        </div>
    </div>

    <!-- update menu modal -->
    <div id="modal-update-menu" class="modal" style="width: 500px">
        <div class="modal-content">
            <h4>{{ selectedMenu.name }}</h4>
            <br>
            <div class="input-field">
                <i class="material-icons prefix">edit</i>
                <label for="update-menu-name">Name</label>
                <input v-model:value="selectedMenu.name" type="text" id="update-menu-name"/>
            </div>
            <div v-if="selectedMenu.linkMode" class="input-field">
                <i class="material-icons prefix">language</i>
                <label for="update-menu-link">Link</label>
                <input v-model:value="selectedMenu.link" type="text" id="update-menu-link"/>
            </div>
            <div v-else class="input-field">
                <i class="material-icons prefix">vpn_key</i>
                <label for="update-menu-password">Passwort</label>
                <input v-model:value="selectedMenu.password" type="password" id="update-menu-password"/>
            </div>
            <div class="input-field">
                <i class="material-icons prefix">low_priority</i>
                <label for="update-menu-priority">Priorität</label>
                <input v-model:value="selectedMenu.priority" type="text" id="update-menu-priority"/>
            </div>
            <div v-if="!selectedMenu.linkMode" style="display: flex; justify-content: space-evenly">
                <a v-if="menuAdmin" @click="setHome" class="waves-effect btn-flat" href="#!" style="margin: 10px 10px 0 0" :style="defaultMenu && selectedMenu.id === defaultMenu.id ? { color: 'green' } : {}">
                    <i class="material-icons left">home</i>
                    {{ defaultMenu && selectedMenu.id === defaultMenu.id ? 'Ist Startseite' : 'Startseite festlegen' }}
                </a>
                <a class="waves-effect btn-flat" :href="'/home.xhtml?id='+selectedMenu.id" target="_blank" style="margin: 10px 10px 0 0">
                    <i class="material-icons left">open_in_new</i>
                    Öffnen
                </a>
            </div>
        </div>
        <div class="modal-footer">
            <a href="#!"
               class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
            <button @click="showDeleteMenu" type="button" class="btn waves-effect waves-light red darken-3">
                <i class="material-icons left">delete</i>
                Löschen
            </button>
            <button @click="updateMenu" type="button" class="btn waves-effect waves-light green darken-3" style="margin-left: 10px;">
                <i class="material-icons left">save</i>
                Speichern
            </button>
        </div>
    </div>

    <!-- password delete menu modal -->
    <div id="modal-delete-menu" class="modal" style="width: 500px">
        <div class="modal-content">
            <h4>{{ selectedMenu.name }} löschen</h4>
            <br>
            <p>Für die Bestätigung das Nutzer-Passwort eingeben:</p>
            <br>
            <div class="input-field">
                <i class="material-icons prefix">vpn_key</i>
                <label for="menu-delete-password">Passwort</label>
                <input v-model:value="deletePassword" type="password" id="menu-delete-password"/>
            </div>
        </div>
        <div class="modal-footer">
            <a href="#!"
               class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
            <button @click="deleteMenu" type="button" class="btn waves-effect waves-light red darken-3">
                <i class="material-icons left">delete</i>
                Löschen
            </button>
        </div>
    </div>

    <!-- create rule modal -->
    <div id="modal-create-rule" class="modal" style="width: 1000px; max-width: 1000px">
        <div class="modal-content">
            <h4>Regel erstellen</h4>
            <br>
            <nav-menu :menu="info.menu" unreal="true" @select="setRuleMenu"></nav-menu>
            <br>
            <div class="row">
                <div class="col s8">
                    <user-search @result="setSearchResult"></user-search>
                    <user-add-list @select="setRuleUser" :users="selectedRule.searchResult" :excluded="selectedRule.user ? [selectedRule.user] : []" excluded-string="ausgewählt" action-string="Auswählen"></user-add-list>
                </div>
                <div class="col s4" style="padding: 30px 20px 0 20px;">
                    <div style="width: 100%; text-align: center; padding: 20px; border: 1px solid #e0e0e0; background-color: white; font-size: 1.2rem">
                        <span style="font-size: 1.8rem; font-weight: bold">Regel</span>
                        <br><br>
                        <span style="font-weight: bold">Nutzer</span>
                        <br>
                        <span>{{ selectedRule.user ? selectedRule.user.firstName + ' ' + selectedRule.user.lastName : 'Keine Auswahl' }}</span>
                        <br><br>
                        <span style="font-weight: bold">Menü</span>
                        <br>
                        <span>{{ selectedRule.menu ? selectedRule.menu.name : 'ALLE' }}</span>
                        <br><br>
                        <a v-if="selectedRule.menu" @click="setRuleMenu(null)" href="#!" style="font-size: 0.8rem" class="green-text">alles auswählen</a>
                    </div>
                </div>
            </div>
        </div>
        <div class="modal-footer">
            <a href="#!"
               class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
            <button @click="createRule" type="button" class="btn waves-effect waves-light green darken-3">
                <i class="material-icons left">add</i>
                Erstellen
            </button>
        </div>
    </div>

    <!-- delete rule modal -->
    <div id="modal-delete-rule" class="modal" style="width: 500px">
        <div class="modal-content">
            <h4>Regel löschen</h4>
            <br>
            Soll die Regel für {{ selectedRule.user ? selectedRule.user.firstName + ' ' + selectedRule.user.lastName : '' }} gelöscht werden?
            <br>
        </div>
        <div class="modal-footer">
            <a href="#!"
               class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
            <button @click="deleteRule" type="button" class="btn waves-effect waves-light red darken-3">
                <i class="material-icons left">delete</i>
                Löschen
            </button>
        </div>
    </div>
</div>

<script src="/static/js/materialize.min.js" ></script>
<script src="/static/js/axios.min.js" ></script>
<script src="/static/js/vue.js" ></script>
<@utils.render/>
<@loading.render/>
<@menu.render/>
<@footer.render/>
<@userAddList.render/>
<@userSearch.render/>
<script type="text/javascript">
    var app = new Vue({
        el: '#app',
        data: {
            info: { user: null, hasMenuWriteAccess: false, menu: null, plan: null, copyright: null, unapproved: null },
            menu: [],
            menuAdmin: false,
            defaultMenu: null,
            rules: [],
            deletePassword: null,
            selectedMenu: {
                linkMode: false,
                id: null, // only in update mode
                parent: null,
                name: null,
                priority: 0,
                link: null,
                password: null
            },
            selectedRule: {
                searchResult: [],
                user: null,
                menu: null
            }
        },
        methods: {
            fetchData: async function() {
                const info = await axios.get('/api/info');
                if(info.data) {
                    this.info = info.data;
                } else {
                    M.toast({html: 'Interner Fehler.'});
                    return;
                }

                const menuInfo = await axios.get('/api/menu/info');
                this.menuAdmin = menuInfo.data.menuAdmin;
                this.defaultMenu = menuInfo.data.defaultMenu;

                const menu = await axios.get('/api/menu/writable');
                this.menu = menu.data;

                if(this.admin) {
                    const rules = await axios.get('/api/menu/rules');
                    this.rules = rules.data;
                }

                hideLoading();
                this.$nextTick(() => M.Tooltip.init(document.querySelectorAll('.tooltipped'), {}));
            },
            resetSelection: function() {
                this.selectedMenu = {
                    parent: null,
                    name: null,
                    priority: 0,
                    link: null,
                    password: null
                };
            },
            showCreateGroup: async function(parent) {
                await this.showCreateMenu(false, parent);
            },
            showCreateLink: async function(parent) {
                await this.showCreateMenu(true, parent);
            },
            showCreateMenu: async function(link, parent) {
                this.resetSelection();
                this.selectedMenu.linkMode = link;
                this.selectedMenu.parent = parent;
                const priority = await axios.get('/api/menu/default-priority'+(parent ? '?id='+parent.id : ''));
                this.selectedMenu.priority = priority.data.priority;
                M.Modal.getInstance(document.getElementById('modal-create-menu')).open();
                this.$nextTick(() => {
                    M.updateTextFields();
                });
            },
            createMenu: async function() {
                showLoadingInvisible();
                try {
                    const data = {
                        name: this.selectedMenu.name,
                        priority: this.selectedMenu.priority,
                        link: this.selectedMenu.link,
                        password: this.selectedMenu.password
                    };
                    await axios.post('/api/menu/create' + (this.selectedMenu.parent ? '/' + this.selectedMenu.parent.id : ''), data);
                    await this.fetchData();
                    M.toast({html: 'Gruppe erstellt.<br>'+this.selectedMenu.name});
                    M.Modal.getInstance(document.getElementById('modal-create-menu')).close();
                } catch (e) {
                    M.toast({html: 'Ein Fehler ist aufgetreten.'});
                    hideLoading();
                }
            },
            selectMenu: function(menu) {
                this.resetSelection();
                this.selectedMenu.id = menu.id;
                this.selectedMenu.name = menu.name;
                this.selectedMenu.priority = menu.priority;
                this.selectedMenu.link = menu.link;
                this.selectedMenu.password = menu.password;
                this.selectedMenu.linkMode = !!menu.link;
                M.Modal.getInstance(document.getElementById('modal-update-menu')).open();
                this.$nextTick(() => {
                    M.updateTextFields();
                });
            },
            setHome: async function() {

                if(this.selectedMenu && this.defaultMenu && this.selectedMenu.id === this.defaultMenu.id)
                    return;

                showLoadingInvisible();
                try {
                    await axios.post('/api/menu/default/' + this.selectedMenu.id);
                    await this.fetchData();
                    M.toast({html: 'Startseite festgelegt.<br>'+this.selectedMenu.name});
                } catch (e) {
                    M.toast({html: 'Ein Fehler ist aufgetreten.'});
                    hideLoading();
                }
            },
            updateMenu: async function() {
                showLoadingInvisible();
                try {
                    const data = {
                        id: this.selectedMenu.id,
                        name: this.selectedMenu.name,
                        priority: this.selectedMenu.priority,
                        link: this.selectedMenu.link,
                        password: this.selectedMenu.password
                    };
                    await axios.post('/api/menu/update', data);
                    await this.fetchData();
                    M.toast({html: 'Eintrag aktualisiert.<br>'+this.selectedMenu.name});
                    M.Modal.getInstance(document.getElementById('modal-update-menu')).close();
                } catch (e) {
                    M.toast({html: 'Ein Fehler ist aufgetreten.'});
                    hideLoading();
                }
            },
            showDeleteMenu: function() {
                this.deletePassword = null;
                M.Modal.getInstance(document.getElementById('modal-update-menu')).close();
                M.Modal.getInstance(document.getElementById('modal-delete-menu')).open();
                this.$nextTick(() => {
                    M.updateTextFields();
                });
            },
            deleteMenu: async function() {
                showLoadingInvisible();
                try {
                    await axios.post('/api/menu/delete/' + this.selectedMenu.id, { password: this.deletePassword });
                    await this.fetchData();
                    M.toast({html: 'Eintrag gelöscht.<br>'+this.selectedMenu.name});
                    M.Modal.getInstance(document.getElementById('modal-delete-menu')).close();
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
            },
            showCreateRule: function() {
                this.selectedRule.user = null;
                this.selectedRule.menu = null;
                M.Modal.getInstance(document.getElementById('modal-create-rule')).open();
            },
            setSearchResult: function(result) {
                this.selectedRule.searchResult = result;
            },
            setRuleUser: function(user) {
                this.selectedRule.user = user;
            },
            setRuleMenu: function(menu) {
                if(menu && menu.link)
                    return;
                this.selectedRule.menu = menu;
            },
            showDeleteRule: function(rule) {
                this.selectedRule.user = rule.user;
                this.selectedRule.menu = rule.menu;
                M.Modal.getInstance(document.getElementById('modal-delete-rule')).open();
            },
            createRule: async function() {
                if(!this.selectedRule.user) {
                    M.toast({html: 'Kein Nutzer ausgewählt.'});
                    return;
                }

                showLoadingInvisible();
                try {
                    await axios.post('/api/menu/rules/add', { user: this.selectedRule.user.id, menu: (this.selectedRule.menu ? this.selectedRule.menu.id : null) });
                    await this.fetchData();
                    M.toast({html: 'Regel erstellt.'});
                    M.Modal.getInstance(document.getElementById('modal-create-rule')).close();
                } catch (e) {
                    switch (e.response.status) {
                        case 409:
                            M.toast({html: 'Nutzer kann bereits alles verändern.'});
                            break;
                        case 410:
                            M.toast({html: 'Regel existiert bereits.'});
                            break;
                        default:
                            M.toast({html: 'Ein Fehler ist aufgetreten.'});
                            break;
                    }
                    hideLoading();
                }
            },
            deleteRule: async function() {
                showLoadingInvisible();
                try {
                    await axios.post('/api/menu/rules/delete', { user: this.selectedRule.user.id, menu: (this.selectedRule.menu ? this.selectedRule.menu.id : null) });
                    await this.fetchData();
                    M.toast({html: 'Regel gelöscht.'});
                    M.Modal.getInstance(document.getElementById('modal-delete-rule')).close();
                } catch (e) {
                    M.toast({html: 'Ein Fehler ist aufgetreten.'});
                    hideLoading();
                }
            }
        },
        computed: {
            allowed: function() {
                return this.info.hasMenuWriteAccess;
            },
            admin: function() {
                return this.info.user && this.info.user.code.role === 100;
            },
            menuPath: function() {
                return (menu) => {
                    if(!menu)
                        return null;
                    let s = '';
                    let curr = menu;
                    while (curr.parent) {
                        curr = curr.parent;
                        s = curr.name + ' / ' + s;
                    }
                    return s;
                }
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