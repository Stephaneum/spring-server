<#macro render>
    <template id="post-list">
        <div>
            <h5 style="margin-bottom: 20px">{{ name }}:</h5>
            <ul class="collection">
                <li v-for="p in postsLimited" class="collection-item" :style="p.id === selected ? { background: '#e8f5e9' } : null">
                    <div style="display: flex; align-items: center;">
                        <span style="flex-grow: 1; white-space: nowrap; overflow-x: hidden; text-overflow: ellipsis;">
                            <span style="font-weight: bold; margin-right: 10px"># {{ p.number }}</span>
                            <i v-if="p.password" style="font-size: 1em; margin-right: 10px" class="material-icons">lock</i>
                            <span v-if="p.id === selected" style="color: green; font-weight: bold; margin-right: 10px">AUSGEWÄHLT</span>
                            {{ p.title }}
                        </span>
                        <span style="flex: 0 0 320px; text-align: right;">
                            <span class="green-badge-light">{{ p.user.firstName }} {{ p.user.lastName }}</span>
                            <span style="margin-left: 20px" class="green-badge-light">{{ p.time }}</span>

                        </span>
                        <span style="text-align: right" :style="{ flex: hide_password ? '0 0 140px' : '0 0 200px' }">
                            <a v-if="!hide_password" @click="showPassword(p)" class="tooltipped waves-effect waves-light blue-grey btn margin-1" href="#!" data-tooltip="Passwort" data-position="bottom">
                                <i class="material-icons">{{ p.password ? 'lock_open' : 'lock' }}</i>
                            </a>

                            <a @click="emitSelected(p)" class="tooltipped waves-effect waves-light green darken-3 btn margin-1" href="#!" data-tooltip="Bearbeiten" data-position="bottom">
                                <i class="material-icons">edit</i>
                            </a>

                            <a @click="showDelete(p)" class="tooltipped waves-effect waves-light btn red darken-4 margin-1" href="#!" data-tooltip="Löschen" data-position="bottom">
                                <i class="material-icons">delete</i>
                            </a>
                        </span>
                    </div>
                </li>
            </ul>
            <div v-if="postsLimited.length < posts.length" style="text-align: center; margin: 50px">
                <a @click="increasePostLimit" class="waves-effect waves-light green darken-3 btn">
                    <i class="material-icons left">add</i>
                    mehr Beiträge
                </a>
            </div>
            <!-- set password modal -->
            <div v-if="!hide_password" id="modal-password" class="modal" style="width: 500px">
                <div class="modal-content">
                    <h4>Beitrag-Passwort</h4>
                    <br>
                    <p>Nutzer müssen dieses Passwort eingeben, um den Inhalt des Beitrages zu betrachten.</p>
                    <br>
                    <div class="input-field">
                        <i class="material-icons prefix">vpn_key</i>
                        <label for="modal-password-input">Passwort</label>
                        <input @keyup.enter="updatePassword" v-model:value="password" id="modal-password-input" name="password" type="password">
                    </div>
                </div>
                <div class="modal-footer">
                    <a @click="closePassword" href="#!"
                       class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
                    <button @click="updatePassword" type="button" class="btn waves-effect waves-light green darken-3">
                        <i class="material-icons left">save</i>
                        Speichern
                    </button>
                </div>
            </div>
            <!-- delete modal -->
            <div id="modal-delete" class="modal">
                <div class="modal-content">
                    <h4>Beitrag wirklich löschen?</h4>
                    <br>
                    <p><b>{{ selectedPost.title }}</b> ({{ selectedPost.time }}) wird gelöscht.</p>
                    <p>Dieser Vorgang kann nicht rückgangig gemacht werden.</p>
                </div>
                <div class="modal-footer">
                    <a @click="closeDelete" href="#!" class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
                    <a @click="deletePost" href="#!" class="modal-close waves-effect waves-red btn red darken-4">
                        <i class="material-icons left">delete</i>
                        Löschen
                    </a>
                </div>
            </div>
        </div>
    </template>

    <script type="text/javascript">
        Vue.component('post-list', {
            props: ['name', 'posts', 'selected', 'hide_password'],
            data: function () {
                return {
                    postsLimited: [],
                    selectedPost: {},
                    password: null
                }
            },
            methods: {
                emitSelected: function(post) {
                    this.$emit('selected', post);
                },
                emitUpdated: function() {
                    this.$emit('updated');
                },
                increasePostLimit: function() {
                    this.postsLimited = this.posts.slice(0, this.postsLimited.length + 10);
                },
                showDelete: function(p) {
                    this.selectedPost = p;
                    M.Modal.getInstance(document.getElementById('modal-delete')).open();
                },
                closeDelete: function() {
                    M.Modal.getInstance(document.getElementById('modal-delete')).close();
                },
                showPassword: function(p) {

                    this.selectedPost = p;
                    this.password = null;
                    if(p.password) {
                        // if there is a password, remove it
                        this.updatePassword();
                        return;
                    }
                    M.Modal.getInstance(document.getElementById('modal-password')).open();
                },
                closePassword: function() {
                    M.Modal.getInstance(document.getElementById('modal-password')).close();
                },
                updatePassword: function() {
                    M.Modal.getInstance(document.getElementById('modal-password')).close();
                    showLoading('Passwort setzen...');
                    axios.post( './api/post/update-password/', { postID: this.selectedPost.id, password: this.password })
                        .then((response) => {
                            if(response.data.success) {
                                M.toast({html: 'Passwort geändert.<br>'+this.selectedPost.title});
                                this.emitUpdated();
                                // hideLoading will be called from parent
                            } else if(response.data.message) {
                                M.toast({html: 'Speichern fehlgeschlagen.<br>'+response.data.message});
                                hideLoading();
                            } else {
                                M.toast({html: 'Speichern fehlgeschlagen.<br>'+this.selectedPost.title});
                                hideLoading();
                            }
                        });
                },
                deletePost: function() {
                    M.Modal.getInstance(document.getElementById('modal-delete')).close();
                    showLoading('Beitrag löschen...');
                    axios.post( './api/post/delete/'+this.selectedPost.id)
                        .then((response) => {
                            if(response.data.success) {
                                M.toast({html: 'Beitrag gelöscht.<br>'+this.selectedPost.title});
                                this.emitUpdated();
                                // hideLoading will be called from parent
                            } else if(response.data.message) {
                                M.toast({html: 'Löschen fehlgeschlagen.<br>'+response.data.message});
                                hideLoading();
                            } else {
                                M.toast({html: 'Löschen fehlgeschlagen.<br>'+this.selectedPost.title});
                                hideLoading();
                            }
                        });
                }
            },
            watch: {
                posts: function(newVal, oldVal) {
                    console.log(newVal);
                    this.postsLimited = newVal.slice(0, 10);
                }
            },
            mounted: function () {
                this.postsLimited = this.posts.slice(0, 10);
            },
            template: '#post-list'
        });
    </script>

    <style>
    </style>
</#macro>