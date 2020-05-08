<#import "/spring.ftl" as spring/>
<#import "components/loading.ftl" as loading/>
<#import "components/vue-loader.ftl" as vueLoader/>
<#import "components/menu.ftl" as menu/>
<#import "components/footer.ftl" as footer/>
<#import "components/utils.ftl" as utils/>

<!DOCTYPE HTML>
<html lang="de">
<head>
    <title>Menü - Stephaneum</title>
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
            <i class="material-icons" style="font-size: 4em">list</i>
            <h4 style="margin: 0">Menü konfigurieren</h4>
        </div>

        <nav-menu :menu="menu" unreal="true" edit-mode="true" :edit-root-level="menuAdmin" @selected="selectMenu" @group="showCreateGroup" @link="showCreateLink"></nav-menu>

        <div class="card-panel" style="margin-top: 60px">
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px">
                <span style="font-size: 2em">Schreibrechte</span>
                <a @click="showCreateRule" style="margin-right: 20px" class="tooltipped waves-effect waves-light btn-floating green darken-4"
                   data-tooltip="Neue Regel" data-position="top" href="#!">
                    <i class="material-icons">add</i>
                </a>
            </div>

            <p style="margin: 0">Nutzer können in diesen Menüpunkten (inkl. Untermenüs) <b>Beiträge</b> erstellen, bearbeiten und genehmigen - sowie das <b>Menü</b> selber bearbeiten.</p>
            <p style="margin: 0">Regeln sind <b>kombinierbar</b>, z.B. kann Nutzer A für die Menüpunkte M1 und M2 Schreibrechte zugewiesen werden.</p>
            <br>

            <ul v-if="rules.length !== 0" class="collection">
                <li v-for="r in rules" class="collection-item">
                    <div style="display: flex; align-items: center; justify-content: space-between">
                        <span style="display: flex; align-items: center">
                            <i class="material-icons grey-text text-darken-2">person</i>
                            <span style="margin-left: 10px">{{ r.user.firstName }} {{ r.user.lastName }} ({{ r.user.code.roleString }})</span>
                        </span>

                        <span>
                            <span v-if="r.menu">
                                <span style="font-size: 1.2rem; color: grey">{{ menuPath(r.menu) }}</span>
                                <span style="font-size: 1.2rem; font-weight: bold">{{ r.menu.name }}</span>
                            </span>
                            <span v-else style="font-size: 1.4rem; font-weight: bold">
                                ALLES
                            </span>
                        </span>

                        <span>
                            <a class="tooltipped waves-effect waves-light btn green darken-3" data-tooltip="Regel bearbeiten" data-position="top">
                                <i class="material-icons">edit</i>
                            </a>
                            <a class="tooltipped waves-effect waves-light btn red darken-3" data-tooltip="Regel löschen" data-position="top" style="margin-left: 10px"
                               @click="deletePage(p.id)">
                                <i class="material-icons">delete</i>
                            </a>
                        </span>
                    </div>
                </li>
            </ul>
        </div>

    </div>
    <div v-else style="flex: 1; min-height: calc(100vh - 100px)"></div>

    <div style="height: 100px"></div>

    <stephaneum-footer :copyright="info.copyright"></stephaneum-footer>

    <!-- create menu modal -->
    <div id="modal-create-menu" class="modal" style="width: 500px">
        <div class="modal-content">
            <h4>{{ selected.linkMode ? 'Neuer Link' : 'Neue Gruppe' }}</h4>
            <p>Wird erstellt in <b>{{ selected.parent ? selected.parent.name : 'Hauptleiste' }}</b></p>
            <br>
            <div class="input-field">
                <i class="material-icons prefix">edit</i>
                <label for="create-menu-name">Name</label>
                <input v-model:value="selected.name" type="text" id="create-menu-name"/>
            </div>
            <div v-if="selected.linkMode" class="input-field">
                <i class="material-icons prefix">language</i>
                <label for="create-menu-link">Link</label>
                <input v-model:value="selected.link" type="text" id="create-menu-link"/>
            </div>
            <div v-else class="input-field">
                <i class="material-icons prefix">vpn_key</i>
                <label for="create-menu-password">Passwort</label>
                <input v-model:value="selected.password" type="password" id="create-menu-password"/>
            </div>
            <div class="input-field">
                <i class="material-icons prefix">low_priority</i>
                <label for="create-menu-priority">Priorität</label>
                <input v-model:value="selected.priority" type="text" id="create-menu-priority"/>
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
            <h4>{{ selected.name }}</h4>
            <br>
            <div class="input-field">
                <i class="material-icons prefix">edit</i>
                <label for="update-menu-name">Name</label>
                <input v-model:value="selected.name" type="text" id="update-menu-name"/>
            </div>
            <div v-if="selected.linkMode" class="input-field">
                <i class="material-icons prefix">language</i>
                <label for="update-menu-link">Link</label>
                <input v-model:value="selected.link" type="text" id="update-menu-link"/>
            </div>
            <div v-else class="input-field">
                <i class="material-icons prefix">vpn_key</i>
                <label for="update-menu-password">Passwort</label>
                <input v-model:value="selected.password" type="password" id="update-menu-password"/>
            </div>
            <div class="input-field">
                <i class="material-icons prefix">low_priority</i>
                <label for="update-menu-priority">Priorität</label>
                <input v-model:value="selected.priority" type="text" id="update-menu-priority"/>
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
            <h4>{{ selected.name }} löschen</h4>
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
            menu: [],
            menuAdmin: false,
            rules: [],
            deletePassword: null,
            selected: {
                linkMode: false,
                id: null, // only in update mode
                parent: null,
                name: null,
                priority: 0,
                link: null,
                password: null
            },
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

                const menu = await axios.get('/api/menu/writable');
                this.menu = menu.data.menu;
                this.menuAdmin = menu.data.menuAdmin;

                if(this.admin) {
                    const rules = await axios.get('/api/menu/rules');
                    this.rules = rules.data;
                }

                hideLoading();
                this.$nextTick(() => M.Tooltip.init(document.querySelectorAll('.tooltipped'), {}));
            },
            resetSelection: function() {
                this.selected = {
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
                this.selected.linkMode = link;
                this.selected.parent = parent;
                const priority = await axios.get('/api/menu/default-priority'+(parent ? '?id='+parent.id : ''));
                this.selected.priority = priority.data.priority;
                M.Modal.getInstance(document.getElementById('modal-create-menu')).open();
                this.$nextTick(() => {
                    M.updateTextFields();
                });
            },
            createMenu: async function() {
                showLoadingInvisible();
                try {
                    const data = {
                        name: this.selected.name,
                        priority: this.selected.priority,
                        link: this.selected.link,
                        password: this.selected.password
                    };
                    await axios.post('/api/menu/create' + (this.selected.parent ? '/' + this.selected.parent.id : ''), data);
                    await this.fetchData();
                    M.toast({html: 'Gruppe erstellt.<br>'+this.selected.name});
                    M.Modal.getInstance(document.getElementById('modal-create-menu')).close();
                } catch (e) {
                    M.toast({html: 'Ein Fehler ist aufgetreten.'});
                    hideLoading();
                }
            },
            selectMenu: function(menu) {
                this.resetSelection();
                this.selected.id = menu.id;
                this.selected.name = menu.name;
                this.selected.priority = menu.priority;
                this.selected.link = menu.link;
                this.selected.password = menu.password;
                this.selected.linkMode = !!menu.link;
                M.Modal.getInstance(document.getElementById('modal-update-menu')).open();
                this.$nextTick(() => {
                    M.updateTextFields();
                });
            },
            updateMenu: async function() {
                showLoadingInvisible();
                try {
                    const data = {
                        id: this.selected.id,
                        name: this.selected.name,
                        priority: this.selected.priority,
                        link: this.selected.link,
                        password: this.selected.password
                    };
                    await axios.post('/api/menu/update', data);
                    await this.fetchData();
                    M.toast({html: 'Eintrag aktualisiert.<br>'+this.selected.name});
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
                    await axios.post('/api/menu/delete/' + this.selected.id, { password: this.deletePassword });
                    await this.fetchData();
                    M.toast({html: 'Eintrag gelöscht.<br>'+this.selected.name});
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
            showCreateRule: function(menu) {

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