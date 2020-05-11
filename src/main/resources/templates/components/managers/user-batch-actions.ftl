
<#-- batch actions for users -->

<#macro render>
    <template id="user-batch-actions">
        <div style="margin-top: 60px; display: flex; align-items: center; justify-content: space-evenly">
            <div class="batch-btn card" @click="showUpdatePassword">
                <i class="material-icons">vpn_key</i>
                <span>Standard-Passwort</span>
            </div>
            <div class="batch-btn card">
                <i class="material-icons">storage</i>
                <span>Quotas ändern</span>
            </div>
            <div class="batch-btn card">
                <i class="material-icons">delete</i>
                <span>Nutzer löschen</span>
            </div>

            <!-- update password modal -->
            <div id="modal-update-password" class="modal" style="width: 500px">
                <div class="modal-content">
                    <h4>Passwort festlegen</h4>
                    <br>
                    Alle Accounts mit einer bestimmten Rolle erhalten einen neuen Passwort.
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
        </div>
    </template>

    <script type="text/javascript">
        Vue.component('user-batch-actions', {
            props: [],
            data: function () {
                return {
                    password: null,
                    role: 0
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
            padding: 10px 15px 10px 15px;
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
        }
    </style>
</#macro>