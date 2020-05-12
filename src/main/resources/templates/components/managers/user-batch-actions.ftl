
<#-- batch actions for users -->

<#macro render>
    <template id="user-batch-actions">
        <div style="margin-top: 60px; display: flex; align-items: center; justify-content: space-evenly">
            <div class="batch-btn card" @click="showUpdatePassword">
                <i class="material-icons">vpn_key</i>
                <span>Standard-Passwort</span>
            </div>
            <div class="batch-btn card" @click="showUpdateQuotas">
                <i class="material-icons">storage</i>
                <span>Quotas ändern</span>
            </div>
            <div class="batch-btn card" @click="showDeleteUsers">
                <i class="material-icons">delete</i>
                <span>Nutzer löschen</span>
            </div>

            <!-- update password modal -->
            <div id="modal-update-password" class="modal" style="width: 500px">
                <div class="modal-content">
                    <h4>Passwort festlegen</h4>
                    <br>
                    Alle Accounts einer bestimmten Rolle erhalten einen neuen Passwort.
                    <br>
                    Die alten Passwörter gehen dabei verloren.
                    <br><br>
                    <div class="input-field">
                        <i class="material-icons prefix">vpn_key</i>
                        <label for="input-update-password">neues Passwort</label>
                        <input v-model:value="password" type="password" id="input-update-password" autocomplete="off"/>
                    </div>
                    <br>
                    <div class="input-field">
                        <i class="material-icons prefix">person</i>
                        <select v-model:value="role">
                            <option value="0" selected>Schüler</option>
                            <option value="1">Lehrer</option>
                            <option value="2">Gast</option>
                        </select>
                        <label>Rolle</label>
                    </div>
                    <br>
                </div>
                <div class="modal-footer">
                    <a href="#!"
                       class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
                    <button @click="updatePassword" type="button" class="btn waves-effect waves-light green darken-3">
                        <i class="material-icons left">save</i>
                        Ändern
                    </button>
                </div>
            </div>

            <!-- update quotas modal -->
            <div id="modal-update-quotas" class="modal" style="width: 500px">
                <div class="modal-content">
                    <h4>Quotas festlegen</h4>
                    <br>
                    Alle Accounts einer bestimmten Klassenstufe erhalten einen neuen Speicherplatz.
                    <br>
                    Es werden dabei keine Dateien gelöscht.
                    <br><br>
                    <div class="input-field">
                        <i class="material-icons prefix">storage</i>
                        <label for="input-quotas-storage">neuer Speicherplatz</label>
                        <input v-model:value="storage" type="text" id="input-quotas-storage" autocomplete="off"/>
                        <span class="helper-text">in Megabytes</span>
                    </div>
                    <br>
                    <div class="input-field">
                        <i class="material-icons prefix">school</i>
                        <label for="input-quotas-grade">Klassenstufe</label>
                        <input v-model:value="grade" type="text" id="input-quotas-grade" autocomplete="off"/>
                    </div>
                    <br>
                </div>
                <div class="modal-footer">
                    <a href="#!"
                       class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
                    <button @click="updateQuotas" type="button" class="btn waves-effect waves-light green darken-3">
                        <i class="material-icons left">save</i>
                        Ändern
                    </button>
                </div>
            </div>

            <!-- update password modal -->
            <div id="modal-delete-users" class="modal" style="width: 500px">
                <div class="modal-content">
                    <h4>Nutzer löschen</h4>
                    <br>
                    Alle Accounts einer bestimmten Rolle werden gelöscht.
                    <br>
                    Veröffentlichte Beiträge bleiben erhalten.
                    <br>
                    Unveröffentlichte Beiträge werden gelöscht.
                    <br>
                    Alle Gruppen, wo der Nutzer Gruppenleiter ist, werden ebenfalls gelöscht.
                    <br><br>
                    <div class="input-field">
                        <i class="material-icons prefix">vpn_key</i>
                        <label for="input-delete-users-pw">Passwort zur Bestätigung</label>
                        <input v-model:value="password" type="password" id="input-delete-users-pw" autocomplete="off"/>
                    </div>
                    <br>
                    <div class="input-field">
                        <i class="material-icons prefix">person</i>
                        <select v-model:value="role">
                            <option value="0" selected>Schüler</option>
                            <option value="1">Lehrer</option>
                            <option value="2">Gast</option>
                        </select>
                        <label>Rolle</label>
                    </div>
                    <br>
                </div>
                <div class="modal-footer">
                    <a href="#!"
                       class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
                    <button @click="deleteUsers" type="button" class="btn waves-effect waves-light red darken-3">
                        <i class="material-icons left">delete</i>
                        Löschen
                    </button>
                </div>
            </div>
        </div>
    </template>

    <script type="text/javascript">
        Vue.component('user-batch-actions', {
            props: [],
            data: function () {
                return {
                    password: null,
                    role: 0,
                    storage: null,
                    grade: 5
                }
            },
            methods: {
                showUpdatePassword: function() {
                    this.password = null;
                    this.role = 0;
                    M.Modal.getInstance(document.getElementById('modal-update-password')).open();
                    this.$nextTick(() => {
                        M.updateTextFields();
                        M.FormSelect.init(document.querySelectorAll('select'), {});
                    });
                },
                updatePassword: async function() {

                    if(!this.password) {
                        M.toast({html: 'Leeres Passwort.'});
                        return;
                    }

                    showLoading('Passwort setzen...')
                    try {
                        await axios.post('/api/users/batch/password', { password: this.password, role: this.role });
                        hideLoading();
                        M.toast({html: 'Passwörter aktualisiert'});
                        M.Modal.getInstance(document.getElementById('modal-update-password')).close();
                    } catch (e) {
                        M.toast({html: 'Ein Fehler ist aufgetreten.'});
                        hideLoading();
                    }
                },
                showUpdateQuotas: function() {
                    this.storage = null;
                    this.grade = 5;
                    M.Modal.getInstance(document.getElementById('modal-update-quotas')).open();
                    this.$nextTick(() => {
                        M.updateTextFields();
                    });
                },
                updateQuotas: async function() {

                    if(isNaN(this.grade)) {
                        M.toast({html: 'Klassenstufe angeben.'});
                        hideLoading();
                        return;
                    }

                    if(isNaN(this.storage)) {
                        M.toast({html: 'Speicherplatz angeben.'});
                        hideLoading();
                        return;
                    }

                    showLoading('Quotas setzen...')
                    try {
                        const grade = parseInt(this.grade);
                        const storage = 1024 * 1024 * parseInt(this.storage); // MB to B
                        await axios.post('/api/users/batch/quotas', { storage, grade });
                        hideLoading();
                        M.toast({html: 'Speicherplatz aktualisiert'});
                        M.Modal.getInstance(document.getElementById('modal-update-quotas')).close();
                    } catch (e) {
                        M.toast({html: 'Ein Fehler ist aufgetreten.'});
                        hideLoading();
                    }
                },
                showDeleteUsers: function() {
                    this.password = null;
                    this.role = 0;
                    M.Modal.getInstance(document.getElementById('modal-delete-users')).open();
                    this.$nextTick(() => {
                        M.updateTextFields();
                        M.FormSelect.init(document.querySelectorAll('select'), {});
                    });
                },
                deleteUsers: async function() {

                    if(!this.password) {
                        M.toast({html: 'Leeres Passwort.'});
                        return;
                    }

                    showLoading('Nutzer löschen...')
                    try {
                        await axios.post('/api/users/batch/delete', { password: this.password, role: this.role });
                        hideLoading();
                        M.toast({html: 'Nutzer gelöscht.'});
                        M.Modal.getInstance(document.getElementById('modal-delete-users')).close();
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
            },
            mounted: function() {
                this.$nextTick(() => {
                    M.Modal.init(document.querySelectorAll('.modal'), {});
                });
            },
            template: '#user-batch-actions'
        });
    </script>

    <style>
        .batch-btn {
            background-color: #1b5e20;
            color: #ffffff;
            min-width: 210px;
            cursor: pointer;
            padding: 15px 15px 15px 15px;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
        }

        .batch-btn:hover {
            background-color: #388e3c !important;
        }

        .batch-btn i {
            font-size: 3rem;
        }

        .batch-btn span {
            font-size: 1.4rem;
            margin-top: 5px;
        }
    </style>
</#macro>