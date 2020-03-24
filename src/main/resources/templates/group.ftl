<#import "/spring.ftl" as spring/>
<#import "components/loading.ftl" as loading/>
<#import "components/vue-loader.ftl" as vueLoader/>
<#import "components/menu.ftl" as menu/>
<#import "components/footer.ftl" as footer/>
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
                    <chat-view :disabled-all="!group.chat" :disabled-me="!canChat" :modify-all="modifyAll"
                               :message-count-url="'/api/chat/group/'+group.id+'/count'" :messages-url="'/api/chat/group/'+group.id"
                               :add-message-url="'/api/chat/group/'+group.id" :clear-url="'/api/chat/group/'+group.id+'/clear'"></chat-view>
                </div>

                <div style="flex: 0 0 400px; padding-left: 10px">
                    <member-list :members="group.members" :leader="group.leader" :modify-all="modifyAll" @adduser="showAddUser" @togglechat="toggleChatUser" @kick="showKickUser"></member-list>
                </div>
            </div>
        </div>

        <div class="row" style="margin-top: 50px">
            <div class="col s10 offset-s2">
                <h4 style="margin: 20px 0 20px 0">Gemeinsame Cloud</h4>
            </div>
        </div>

        <cloud-view ref="cloudView" :my-id="info.user.id" :shared-mode="true" :modify-all="modifyAll" :teacherchat="hasTeacherChat"
                    :root-url="'/api/cloud/view/group/' + group.id" :upload-url="'/api/cloud/upload/group/' + group.id" :folder-url="'/api/cloud/create-folder/group/' + group.id"></cloud-view>

    </div>
    <div v-else style="flex: 1; min-height: calc(100vh - 100px)"></div>

    <div style="height: 100px"></div>

    <stephaneum-footer :copyright="info.copyright"></stephaneum-footer>

    <!-- add chatroom modal -->
    <div id="modal-add-subgroup" class="modal">
        <div class="modal-content">
            <h4>Neuer Chatraum</h4>
            <br>
            In Arbeit

        </div>
        <div class="modal-footer">
            <a @click="closeAddSubGroup" href="#!" class="modal-close waves-effect waves-green btn-flat">Schließen</a>
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
                <a @click="changeGroupName" href="#!" style="margin-left: 20px" class="modal-close waves-effect waves-light btn green darken-4">
                    <i class="material-icons left">save</i>
                    Speichern
                </a>
            </div>

            <div style="display: flex; align-items: center; padding: 10px 20px 0 20px;">
                Chat
                <a @click="updateChat(true)" href="#!" style="margin-left: 20px" class="modal-close waves-effect waves-light btn green" :class="group && group.chat ? [] : ['darken-4']">
                    <i class="material-icons left">check</i>
                    Ja
                </a>
                <a @click="updateChat(false)" href="#!" style="margin-left: 20px" class="modal-close waves-effect waves-light btn green" :class="group && group.chat ? ['darken-4'] : []">
                    <i class="material-icons left">close</i>
                    Nein
                </a>
            </div>
        </div>
        <div class="modal-footer">
            <a @click="closeEditGroup" href="#!" class="modal-close waves-effect waves-green btn-flat">Schließen</a>
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
                <a @click="closeAddUser" class="waves-effect btn-flat" href="#!" style="margin: 0">
                    <i class="material-icons">close</i>
                </a>
            </div>

            <br>
            <user-search ref="userSearch" @onselect="addUser" :excluded="group ? group.members : []" excluded-string="hinzugefügt" action-string="Hinzufügen"></user-search>
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
            <a @click="closeKickUser" href="#!" class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
            <a @click="kickUser" href="#!" class="modal-close waves-effect waves-red btn red darken-4">
                <i class="material-icons left">delete</i>
                Löschen
            </a>
        </div>
    </div>

    <!-- delete modal -->
    <div id="modal-delete-group" class="modal">
        <div class="modal-content">
            <h4>Gruppe löschen?</h4>
            <br>
            <p>Da du Gruppenleiter/in bzw. Betreuer/in bist, wird die ganze Gruppe beim Verlassen gelöscht.</p>
            <p>Alle Dateien in dieser Gruppe werden ebenfalls gelöscht.</p>
            <p>Dieser Vorgang kann nicht rückgangig gemacht werden.</p>
        </div>
        <div class="modal-footer">
            <a @click="closeDeleteGroup" href="#!" class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
            <a @click="deleteGroup" href="#!" class="modal-close waves-effect waves-red btn red darken-4">
                <i class="material-icons left">delete</i>
                Löschen
            </a>
        </div>
    </div>

    <!-- leave modal -->
    <div id="modal-leave-group" class="modal">
        <div class="modal-content">
            <h4>Gruppe verlassen?</h4>
            <br>
            <p>Alle Dateien in dieser Gruppe werden ebenfalls gelöscht.</p>
            <p>Dieser Vorgang kann nicht rückgangig gemacht werden.</p>
        </div>
        <div class="modal-footer">
            <a @click="closeLeaveGroup" href="#!" class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
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
<@userSearch.render/>
<@chatView.render/>
<@memberList.render/>
<@cloudView.render/>
<script type="text/javascript">
    var app = new Vue({
        el: '#app',
        data: {
            info: { user: null, menu: null, plan: null, copyright: null, unapproved: null },
            group: null,
            selectedUser: {},
            groupName: null,
        },
        methods: {
            showAddSubGroup: function() {
                M.Modal.getInstance(document.getElementById('modal-add-subgroup')).open();
            },
            closeAddSubGroup: function() {
                M.Modal.getInstance(document.getElementById('modal-add-subgroup')).open();
            },
            addSubGroup: function() {
                M.Modal.getInstance(document.getElementById('modal-add-subgroup')).open();
            },
            showEditGroup: function() {
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
                    await axios.post('/api/groups/' + this.group.id + '/update', { name: this.groupName });
                    await this.fetchData();
                } catch (e) {
                    M.toast({html: 'Ein Fehler ist aufgetreten.'});
                }
            },
            updateChat: async function(chat) {
                try {
                    showLoadingInvisible();
                    await axios.post('/api/groups/' + this.group.id + '/chat/' + (chat ? '1' : '0'));
                    await this.fetchData();
                } catch (e) {
                    M.toast({html: 'Ein Fehler ist aufgetreten.'});
                }
            },
            showDeleteGroup: function() {
                M.Modal.getInstance(document.getElementById('modal-delete-group')).open();
            },
            closeDeleteGroup: function() {
                M.Modal.getInstance(document.getElementById('modal-delete-group')).close();
            },
            deleteGroup: async function() {
                M.Modal.getInstance(document.getElementById('modal-delete-group')).close();
                try {
                    showLoadingInvisible();
                    await axios.post('/api/groups/' + this.group.id + '/delete');
                    window.location.replace("../groups");
                } catch (e) {
                    M.toast({html: 'Ein Fehler ist aufgetreten.'});
                }
            },
            showLeaveGroup: function() {
                M.Modal.getInstance(document.getElementById('modal-leave-group')).open();
            },
            closeLeaveGroup: function() {
                M.Modal.getInstance(document.getElementById('modal-leave-group')).close();
            },
            leaveGroup: async function() {
                M.Modal.getInstance(document.getElementById('modal-leave-group')).close();
                try {
                    showLoadingInvisible();
                    await axios.post('/api/groups/' + this.group.id + '/leave');
                    window.location.replace("../groups");
                } catch (e) {
                    M.toast({html: 'Ein Fehler ist aufgetreten.'});
                }
            },
            showAddUser: function() {
                this.$refs.userSearch.reset();
                M.Modal.getInstance(document.getElementById('modal-add-user')).open();
                this.$nextTick(() => {
                    M.updateTextFields();
                });
            },
            closeAddUser: function() {
                M.Modal.getInstance(document.getElementById('modal-add-user')).close();
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
                    await axios.post('/api/groups/' + this.group.id + '/toggle-chat/' + user.id);
                } catch (e) {
                    M.toast({html: 'Ein Fehler ist aufgetreten.'});
                }
                await this.fetchData();
            },
            showKickUser: function(user) {
                this.selectedUser = user;
                M.Modal.getInstance(document.getElementById('modal-kick-user')).open();
            },
            closeKickUser: function() {
                M.Modal.getInstance(document.getElementById('modal-kick-user')).close();
            },
            kickUser: async function() {
                M.Modal.getInstance(document.getElementById('modal-kick-user')).close();
                showLoading('Nutzer entfernen...');
                try {
                    await axios.post('/api/groups/' + this.group.id + '/kick/' + this.selectedUser.id);
                    M.toast({html: 'Nutzer gelöscht.<br>'+this.selectedUser.firstName});
                    await this.fetchData();
                    await this.$refs.cloudView.fetchData(); // also update the cloud view because files may be deleted
                } catch (e) {
                    hideLoading();
                    M.toast({html: 'Löschen fehlgeschlagen.<br>'+this.selectedUser.firstName});
                }
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
                this.groupName = group.data.name;

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
                return this.admin || (this.info.user && this.group.members && this.group.members.some((m) => m.id === this.info.user.id && m.chat));
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