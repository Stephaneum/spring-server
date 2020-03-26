<#import "/spring.ftl" as spring/>
<#import "components/loading.ftl" as loading/>
<#import "components/vue-loader.ftl" as vueLoader/>
<#import "components/menu.ftl" as menu/>
<#import "components/footer.ftl" as footer/>
<#import "components/tab-bar.ftl" as tabBar/>
<#import "components/user-add-list.ftl" as userAddList/>
<#import "components/user-search.ftl" as userSearch/>
<#import "components/utils.ftl" as utils/>
<#import "components/groups/chat-view.ftl" as chatView/>
<#import "components/groups/member-list.ftl" as memberList/>
<#import "components/cloud/cloud-view.ftl" as cloudView/>

<!DOCTYPE HTML>
<html lang="de">
<head>
    <title>Gruppen - Stephaneum</title>
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
    <nav-menu :menu="info.menu" :user="info.user" :plan="info.plan" :unapproved="info.unapproved"></nav-menu>
    <div v-if="group" style="margin: 50px auto 0 auto; max-width: 1600px; min-height: calc(100vh - 350px)">

        <div class="row">
            <div class="col s10 offset-s2">
                <h4 style="margin: 20px 0 20px 0">{{ group.name }}</h4>
            </div>
        </div>

        <div class="row">

            <!-- TABS -->
            <div class="col s10 offset-s2">
                <tab-bar :tabs="groups" :curr-tab="currTab" @selected="setTab"></tab-bar>
            </div>

            <div class="col s2">
                <a href="../groups">
                    <div class="action-btn z-depth-1">
                        <i style="font-size: 3em" class="material-icons">arrow_back</i>
                        <span style="font-size: 1.5em">Zurück</span>
                    </div>
                </a>

                <div v-if="modifyAll" @click="showEditGroup" class="action-btn z-depth-1">
                    <i style="font-size: 3em" class="material-icons">settings</i>
                    <span style="font-size: 1.5em">Optionen</span>
                </div>

                <div v-if="modifyAll" @click="showDeleteGroup" class="action-btn z-depth-1">
                    <i style="font-size: 3em" class="material-icons">delete</i>
                    <span style="font-size: 1.5em">Löschen</span>
                </div>

                <div v-else @click="showLeaveGroup" class="action-btn z-depth-1">
                    <i style="font-size: 3em" class="material-icons">exit_to_app</i>
                    <span style="font-size: 1.5em">Verlassen</span>
                </div>
            </div>

            <div class="col s10" style="display: flex; min-height: 500px">
                <div style="flex: 1; padding-right: 10px">
                    <chat-view :disabled-all="!currGroup.chat" :disabled-me="!canChat" :modify-all="modifyAll" :height="memberListHeight"
                               :message-count-url="'/api/chat/group/'+currGroup.id+'/count'" :messages-url="'/api/chat/group/'+currGroup.id"
                               :add-message-url="'/api/chat/group/'+currGroup.id" :clear-url="'/api/chat/group/'+currGroup.id+'/clear'"></chat-view>
                </div>

                <div style="flex: 0 0 400px; padding-left: 10px">
                    <member-list :members="currGroup.members" :leader="currGroup.leader" :modify-all="modifyAll" :show-add-user="currGroup.id === group.id" @adduser="showAddUser" @togglechat="toggleChatUser" @kick="showKickUser" @height="setMemberListHeight"></member-list>
                </div>
            </div>
        </div>

        <div class="row" style="margin-top: 50px">
            <div class="col s10 offset-s2">
                <h4 style="margin: 20px 0 10px 0">Gemeinsame Cloud</h4>
                <p v-if="currGroup.id !== group.id" style="margin: 0">des Chatraums <b>{{ currGroup.name }}</b></p>
            </div>
        </div>

        <cloud-view ref="cloudView" :my-id="info.user.id" :shared-mode="true" :modify-all="modifyAll" :teacherchat="hasTeacherChat"
                    :root-url="'/api/cloud/view/group/' + currGroup.id" :upload-url="'/api/cloud/upload/group/' + currGroup.id" :folder-url="'/api/cloud/create-folder/group/' + currGroup.id"></cloud-view>

    </div>
    <div v-else style="flex: 1; min-height: calc(100vh - 100px)"></div>

    <div style="height: 100px"></div>

    <stephaneum-footer :copyright="info.copyright"></stephaneum-footer>

    <!-- add chatroom modal -->
    <div id="modal-add-subgroup" class="modal">
        <div class="modal-content">
            <h4>Neuer Chatraum</h4>
            <p>Chaträume bieten die Möglichkeit, Untergruppen zu bilden und dort zu chatten oder Dateien auszutauschen. Chaträume können nur von Gruppenadmins oder Betreuer erstellt werden.</p>
            <div class="input-field">
                <i class="material-icons prefix">edit</i>
                <label for="subgroup-name-text">Name</label>
                <input v-model:value="groupName" type="text" id="subgroup-name-text"/>
            </div>
            <user-add-list @select="addUserSubGroup" :users="((group || {}).members || [])" :excluded="usersSubGroup" excluded-string="hinzugefügt" action-string="Hinzufügen"></user-add-list>
        </div>
        <div class="modal-footer">
            <a href="#!" class="modal-close waves-effect waves-green btn-flat">Schließen</a>
            <button @click="addSubGroup" type="button" class="btn waves-effect waves-light green darken-3">
                <i class="material-icons left">add</i>
                Erstellen
            </button>
        </div>
    </div>

    <!-- edit modal -->
    <div id="modal-edit-group" class="modal">
        <div class="modal-content">
            <h4>Gruppe bearbeiten</h4>
            <br>
            <div style="display: flex; align-items: center; padding: 10px 20px 0 20px;">
                <div style="flex: 1;" class="input-field">
                    <i class="material-icons prefix">edit</i>
                    <label for="group-name-text">Name</label>
                    <input @keyup.enter="changeGroupName" v-model:value="groupName" type="text" id="group-name-text"/>
                </div>
                <a @click="changeGroupName" style="margin-left: 20px" class="waves-effect waves-light btn green darken-4">
                    <i class="material-icons left">save</i>
                    Speichern
                </a>
            </div>

            <div style="display: flex; align-items: center; padding: 10px 20px 0 20px;">
                Chat
                <a @click="updateChat(true)" href="#!" style="margin-left: 20px" class="waves-effect waves-light btn green" :class="currGroup && currGroup.chat ? [] : ['darken-4']">
                    <i class="material-icons left">check</i>
                    Ja
                </a>
                <a @click="updateChat(false)" href="#!" style="margin-left: 20px" class="waves-effect waves-light btn green" :class="currGroup && currGroup.chat ? ['darken-4'] : []">
                    <i class="material-icons left">close</i>
                    Nein
                </a>
            </div>
        </div>
        <div class="modal-footer">
            <a href="#!" class="modal-close waves-effect waves-green btn-flat">Schließen</a>
        </div>
    </div>

    <!-- add user modal -->
    <div id="modal-add-user" class="modal">
        <div class="modal-content">
            <div style="display: flex; align-items: center; justify-content: space-between">
                <span style="display: flex; align-items: center">
                    <i style="font-size: 2em" class="material-icons">person</i>
                    <span style="font-size: 1.5em; margin-left: 15px">Nutzer hinzufügen</span>
                </span>
                <a class="modal-close waves-effect btn-flat" href="#!" style="margin: 0">
                    <i class="material-icons">close</i>
                </a>
            </div>

            <br>
            <user-search ref="userSearch" @result="setSearchResult"></user-search>
            <user-add-list @select="addUser" :users="searchResult" :excluded="group ? group.members : []" excluded-string="hinzugefügt" action-string="Hinzufügen"></user-add-list>
        </div>
    </div>

    <!-- kick modal -->
    <div id="modal-kick-user" class="modal">
        <div class="modal-content">
            <h4>Nutzer entfernen?</h4>
            <br>
            <p><b>{{ selectedUser.firstName }} {{ selectedUser.lastName }}</b> wird gelöscht.</p>
            <p>Alle Dateien des Nutzers in dieser Gruppe werden ebenfalls gelöscht.</p>
            <p>Dieser Vorgang kann nicht rückgangig gemacht werden.</p>
        </div>
        <div class="modal-footer">
            <a href="#!" class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
            <a @click="kickUser" href="#!" class="modal-close waves-effect waves-red btn red darken-4">
                <i class="material-icons left">delete</i>
                Löschen
            </a>
        </div>
    </div>

    <!-- delete modal -->
    <div id="modal-delete-group" class="modal">
        <div class="modal-content">
            <h4>{{ (currGroup || {}).name }} löschen?</h4>
            <br>
            <p>Da du Gruppenleiter/in bzw. Betreuer/in bist, wird die ganze Gruppe beim Verlassen gelöscht.</p>
            <p>Alle Dateien in dieser Gruppe werden ebenfalls gelöscht.</p>
            <p>Dieser Vorgang kann nicht rückgangig gemacht werden.</p>
        </div>
        <div class="modal-footer">
            <a href="#!" class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
            <a @click="deleteGroup" href="#!" class="modal-close waves-effect waves-red btn red darken-4">
                <i class="material-icons left">delete</i>
                Löschen
            </a>
        </div>
    </div>

    <!-- leave modal -->
    <div id="modal-leave-group" class="modal">
        <div class="modal-content">
            <h4>{{ (currGroup || {}).name }} verlassen?</h4>
            <br>
            <p>Alle Dateien in dieser Gruppe werden ebenfalls gelöscht.</p>
            <p>Dieser Vorgang kann nicht rückgangig gemacht werden.</p>
        </div>
        <div class="modal-footer">
            <a href="#!" class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
            <a @click="leaveGroup" href="#!" class="modal-close waves-effect waves-red btn red darken-4">
                <i class="material-icons left">delete</i>
                Löschen
            </a>
        </div>
    </div>
</div>

<script src="/static/js/moment.min.js" ></script>
<script src="/static/js/moment.de.js" ></script>
<script src="/static/js/materialize.min.js" ></script>
<script src="/static/js/axios.min.js" ></script>
<script src="/static/js/vue.js" ></script>
<@utils.render/>
<@loading.render/>
<@menu.render/>
<@footer.render/>
<@tabBar.render/>
<@userAddList.render/>
<@userSearch.render/>
<@chatView.render/>
<@memberList.render/>
<@cloudView.render/>
<script type="text/javascript">

    const parentGroup = { id: -1, special: true, name: 'Allgemein' };
    const addSubGroup = { id: -2, icon: 'add', name: 'Chatraum' };

    var app = new Vue({
        el: '#app',
        data: {
            info: { user: null, menu: null, plan: null, copyright: null, unapproved: null },
            group: null, // main group
            currTab: parentGroup, // curr tab
            currGroup: null,
            selectedUser: {}, // e.g. from member list
            groupName: null, // used for edit / new sub group
            memberListHeight: 0, // applied to chat view
            searchResult: [],
            usersSubGroup: [],
        },
        methods: {
            setTab: function(tab) {
                if(tab.id === this.currTab.id)
                    return;

                switch(tab.id) {
                    case parentGroup.id:
                        this.currGroup = this.group;
                        this.currTab = parentGroup;
                        break;
                    case addSubGroup.id:
                        this.showAddSubGroup();
                        break;
                    default:
                        // select sub group
                        this.currGroup = tab;
                        this.currTab = tab;
                        break;
                }

                this.$nextTick(async () => {
                    await this.$refs.cloudView.reset(); // also update the cloud view
                });
            },
            showAddSubGroup: function() {
                this.groupName = null;
                this.usersSubGroup = [ this.info.user ];
                M.Modal.getInstance(document.getElementById('modal-add-subgroup')).open();
                this.$nextTick(() => {
                    M.updateTextFields();
                });
            },
            addUserSubGroup: function(u) {
                this.usersSubGroup.push(u);
            },
            addSubGroup: async function() {

                if(!this.groupName || this.groupName.trim().length === 0)
                    return;

                M.Modal.getInstance(document.getElementById('modal-add-subgroup')).close();
                try {
                    await axios.post( '/api/groups/create', { name: this.groupName, parent: this.group.id, members: this.usersSubGroup.filter((u) => u.id !== this.info.user.id).map((u) => u.id) });
                    await this.fetchData();
                    M.toast({html: 'Chatraum erstellt.<br>'+this.groupName});
                } catch (e) {
                    hideLoading();
                    console.log(e);
                    M.toast({html: 'Fehlgeschlagen.'});
                }
            },
            showEditGroup: function() {
                this.groupName = this.currGroup.name;
                M.Modal.getInstance(document.getElementById('modal-edit-group')).open();
                this.$nextTick(() => {
                    M.updateTextFields();
                });
            },
            closeEditGroup: function() {
                M.Modal.getInstance(document.getElementById('modal-edit-group')).close();
            },
            changeGroupName: async function() {
                try {
                    showLoadingInvisible();
                    await axios.post('/api/groups/' + this.currGroup.id + '/update', { name: this.groupName });
                    await this.fetchData();
                } catch (e) {
                    M.toast({html: 'Ein Fehler ist aufgetreten.'});
                }
            },
            updateChat: async function(chat) {
                try {
                    showLoadingInvisible();
                    await axios.post('/api/groups/' + this.currGroup.id + '/chat/' + (chat ? '1' : '0'));
                    await this.fetchData();
                } catch (e) {
                    M.toast({html: 'Ein Fehler ist aufgetreten.'});
                }
            },
            showDeleteGroup: function() {
                M.Modal.getInstance(document.getElementById('modal-delete-group')).open();
            },
            deleteGroup: async function() {
                M.Modal.getInstance(document.getElementById('modal-delete-group')).close();
                try {
                    showLoadingInvisible();
                    await axios.post('/api/groups/' + this.currGroup.id + '/delete');
                    if(this.currGroup.id === this.group.id)
                        window.location.replace("../groups");
                    else {
                        this.setTab(parentGroup);
                        await this.fetchData();
                    }
                } catch (e) {
                    M.toast({html: 'Ein Fehler ist aufgetreten.'});
                }
            },
            showLeaveGroup: function() {
                M.Modal.getInstance(document.getElementById('modal-leave-group')).open();
            },
            leaveGroup: async function() {
                M.Modal.getInstance(document.getElementById('modal-leave-group')).close();
                try {
                    showLoadingInvisible();
                    await axios.post('/api/groups/' + this.currGroup.id + '/leave');
                    if(this.currGroup.id === this.group.id)
                        window.location.replace("../groups");
                    else {
                        this.setTab(parentGroup);
                        await this.fetchData();
                    }
                } catch (e) {
                    M.toast({html: 'Ein Fehler ist aufgetreten.'});
                }
            },
            showAddUser: function() {
                this.searchResult = [];
                M.Modal.getInstance(document.getElementById('modal-add-user')).open();
                this.$nextTick(() => {
                    M.updateTextFields();
                });
            },
            setSearchResult: function(users) {
                this.searchResult = users;
            },
            addUser: async function(user) {
                try {
                    showLoadingInvisible();
                    await axios.post('/api/groups/' + this.group.id + '/add-user/' + user.id);
                } catch (e) {
                    M.toast({html: 'Ein Fehler ist aufgetreten.'});
                }
                await this.fetchData();
                this.$nextTick(() => {
                    this.$refs.userSearch.search();
                });
            },
            toggleChatUser: async function(user) {
                try {
                    showLoadingInvisible();
                    await axios.post('/api/groups/' + this.currGroup.id + '/toggle-chat/' + user.id);
                } catch (e) {
                    M.toast({html: 'Ein Fehler ist aufgetreten.'});
                }
                await this.fetchData();
            },
            showKickUser: function(user) {
                this.selectedUser = user;
                M.Modal.getInstance(document.getElementById('modal-kick-user')).open();
            },
            kickUser: async function() {
                M.Modal.getInstance(document.getElementById('modal-kick-user')).close();
                showLoading('Nutzer entfernen...');
                try {
                    await axios.post('/api/groups/' + this.currGroup.id + '/kick/' + this.selectedUser.id);
                    M.toast({html: 'Nutzer gelöscht.<br>'+this.selectedUser.firstName});
                    await this.fetchData();
                    await this.$refs.cloudView.fetchData(); // also update the cloud view because files may be deleted
                } catch (e) {
                    hideLoading();
                    M.toast({html: 'Löschen fehlgeschlagen.<br>'+this.selectedUser.firstName});
                }
            },
            setMemberListHeight: function(h) {
                this.memberListHeight = h;
            },
            fetchData: async function() {
                const info = await axios.get('/api/info');
                if(info.data) {
                    this.info = info.data;
                } else {
                    M.toast({html: 'Interner Fehler.'});
                    return;
                }

                const id = window.location.pathname.split('/').pop();
                const group = await axios.get('/api/groups/' + id);
                this.group = group.data;
                if(!this.currGroup) {
                    // init
                    this.currGroup = this.group;
                } else {
                    // update
                    if(this.currTab === parentGroup) {
                        this.currGroup = this.group;
                    } else {
                        this.currGroup = this.group.children.find((g) => g.id === this.currGroup.id);
                    }
                }

                hideLoading();
                this.$nextTick(() => M.Tooltip.init(document.querySelectorAll('.tooltipped'), {}));
            }
        },
        computed: {
            allowed: function() {
                return this.info.user && this.info.user.code.role >= 0;
            },
            admin: function() {
                return this.info.user && this.info.user.code.role === 100;
            },
            hasTeacherChat: function() {
                return this.allowed && (this.info.user.code.role === 1 || this.info.user.code.role === 100);
            },
            modifyAll: function() {
                return this.admin || // admin
                    (this.group && this.group.leader && this.group.leader.id === this.info.user.id) || // group-admin
                    (this.group && this.group.members && this.group.members.some((t) => t.teacher && t.id === this.info.user.id)); // group-teacher
            },
            canChat: function() {
                return this.admin || (this.info.user && this.currGroup.members && this.currGroup.members.some((m) => m.id === this.info.user.id && m.chat));
            },
            groups: function() {
                if(!this.group)
                    return [];
                if(this.modifyAll)
                    return [ parentGroup, ...this.group.children, addSubGroup ];
                else {
                    if(this.group.children.length !== 0)
                        return [ parentGroup, ...this.group.children ];
                    else
                        return [];
                }
            }
        },
        mounted: function() {
            M.AutoInit();
            moment.locale('de');
            this.fetchData();
        }
    });
</script>
</body>
</html>