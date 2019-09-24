<#import "/spring.ftl" as spring/>
<#import "../components/loading.ftl" as loading/>
<#import "../components/toaster.ftl" as toaster/>
<#setting locale="de_DE">
<#setting number_format="computer">

<!DOCTYPE HTML>
<html lang="de">
<head>
    <title>Blackboard</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="<@spring.url '/static/img/favicon.png' />"/>
    <link rel="apple-touch-icon" sizes="196x196" href="<@spring.url '/static/img/favicon.png' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/materialize.min.css' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/material-icons.css' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/style.css' />">
</head>

<body>

<div id="app">
    <div class="valign-wrapper" style="height: 100vh">
        <div style="margin: auto">
            <div class="center-align" style="max-width: 400px; margin: auto; margin-top: 50px">
                <img src="<@spring.url '/static/img/logo-banner-green.png' />" style="width: 100%"/>
            </div>

            <div class="row">
                <div class="col m6 left-align">
                    <a class="waves-effect waves-light btn green darken-3" href="<@spring.url './' />" target="_blank">
                        <i class="material-icons right">star</i>Live-Version</a>
                </div>
                <div class="col m6 right-align">
                    <a @click="logout" class="waves-effect waves-light btn green darken-3">
                        <i class="material-icons right">exit_to_app</i>Abmelden</a>
                </div>
            </div>
            <div class="card" style="width: 1250px; min-height: 500px; margin: 15px 0 15px 0; padding: 5px 20px 20px 20px">
                <h4 style="margin-bottom: 20px">Blackboard</h4>

                <ul class="collection">
                    <li v-for="b in boards" class="collection-item" :class="b.visible ? [] : ['grey', 'lighten-3']">
                        <div class="row" style="margin: 0">
                            <div class="col m6" style="font-size: 1.4em; overflow: hidden;padding: 10px">
                                <div style="display: flex; justify-content: space-between">
                                    <div style="display:inline-block; flex-shrink: 0; width: 60px">
                                    <span class="board-sec-class green-badge"
                                          :style="{ visibility: activeID === b.id ? 'visible' : 'hidden' }">{{ activeSeconds }}s</span>
                                    </div>
                                    <div style="display:inline-block; flex-shrink: 0; width: 80px">
                                    <span class="text-hover" style="display: inline-block"
                                          @click="showDuration(b.id, typeReadable(b.type), b.duration)">({{ b.duration }}s)</span>
                                    </div>
                                    <div style="flex: 1; display:inline-block;">
                                        <span v-if="b.type === 'PLAN'" style="margin-left: 10px">[ Vertretungsplan ]</span>
                                        <span v-else-if="b.type === 'TEXT'" class="text-hover" style="white-space: nowrap;" @click="showRename(b.id, typeReadable(b.type), b.value)">{{ b.valueWithoutBreaks }}</span>
                                        <div v-else style="display: inline-block">
                                            <span v-if="b.uploaded" style="margin-left: 10px">[ {{ b.fileName }} ]</span>
                                            <span v-else style="margin-left: 10px">[ leer ]</span>
                                            <form method="POST" enctype="multipart/form-data" style="display: inline-block">
                                                <input name="file" type="file" :id="'upload-file-'+ b.id" @change="upload($event.currentTarget.files[0], b.id)" style="display: none">
                                                <a class="waves-effect waves-light btn-small green darken-3 margin-1"
                                                   @click="showUpload(b.id)"><i class="material-icons left">arrow_upward</i>Hochladen</a>
                                            </form>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="col m3 right-align">
                                <select name="type" @change="updateType(b.id, $event.currentTarget.value)" class="browser-default">
                                    <option v-for="t in types" :value="t" :selected="b.type === t" style="z-index: 9999">{{ typeReadable(t) }}</option>
                                </select>
                            </div>
                            <div class="col m3 right-align">

                                <a class="tooltipped waves-effect waves-light btn darken-4 margin-1"
                                   @click="moveUp(b.id)" href="#!" data-tooltip="Reihenfolge: nach oben" data-position="top">
                                    <i class="material-icons">arrow_upward</i></a>

                                <a class="tooltipped waves-effect waves-light btn darken-4 margin-1"
                                   @click="moveDown(b.id)" href="#!" data-tooltip="Reihenfolge: nach unten" data-position="top">
                                    <i class="material-icons">arrow_downward</i></a>

                                <a class="tooltipped waves-effect waves-light btn green darken-4 margin-1"
                                   @click="toggleVisibility(b.id)" href="#!" data-tooltip="Sichtbar: ja/nein" data-position="top">
                                    <i class="material-icons">{{ b.visible ? 'visibility' : 'visibility_off' }}</i></a>

                                <a class="tooltipped waves-effect waves-light btn red darken-4 margin-1"
                                   @click="showDelete(b.id, typeReadable(b.type))" data-tooltip="Löschen" data-position="top">
                                    <i class="material-icons">delete</i></a>

                            </div>
                        </div>
                    </li>
                </ul>

                <div class="center-align">
                    <a style="margin-top: 25px" class="waves-effect waves-light btn green darken-3"
                       @click="newEntry">
                        <i class="material-icons right">add</i>Neuer Eintrag</a>
                </div>

            </div>
            <div id="active-counter" style="text-align: center; color: #808080">aktive Blackboards: {{ activeClients }}</div>
        </div>
    </div>

    <!-- rename modal -->
    <div id="modal-rename" class="modal">
        <div class="modal-content">
            <h4>{{ boardRename.boardType }} bearbeiten</h4>
            <br/>

            <div class="input-field">
                <i class="material-icons prefix">edit</i>
                <label for="modal-rename-input">Textinhalt</label>
                <textarea v-model:value="boardValue" id="modal-rename-input" name="value" class="materialize-textarea">
                </textarea>
            </div>
        </div>
        <div class="modal-footer">
            <a @click="closeRename" href="#!" class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
            <a @click="updateValue" href="#!" class="btn waves-effect waves-light green darken-3">
                <i class="material-icons left">save</i>
                Speichern
            </a>
        </div>
    </div>

    <!-- duration modal -->
    <div id="modal-duration" class="modal">
        <div class="modal-content">
            <h4>Dauer bearbeiten ({{ boardDuration.boardType }})</h4>
            <br/>

            <div class="input-field">
                <i class="material-icons prefix">timer</i>
                <label for="modal-duration-input">Dauer in Sekunden</label>
                <input v-model:value="duration" id="modal-duration-input" type="number" min="1" max="3600"/>
            </div>
        </div>
        <div class="modal-footer">
            <a @click="closeDuration" href="#!" class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
            <a @click="updateDuration" href="#!" class="btn waves-effect waves-light green darken-3">
                <i class="material-icons left">save</i>
                Speichern
            </a>
        </div>
    </div>

    <!-- delete modal -->
    <div id="modal-delete" class="modal">
        <div class="modal-content">
            <h4>{{ boardDelete.boardType }} wirklich löschen?</h4>
            <p>{{ boardDelete.boardType }} wird gelöscht. Dieser Vorgang kann nicht rückgangig gemacht werden.</p>
        </div>
        <div class="modal-footer">
            <a @click="closeDelete" href="#!" class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
            <a @click="deleteBoard" href="#!" class="modal-close waves-effect waves-red btn red darken-4">Löschen</a>
        </div>
    </div>
</div>

<script src="/static/js/materialize.min.js" ></script>
<script src="/static/js/axios.min.js" ></script>
<script src="/static/js/vue.js" ></script>
<script type="text/javascript">

    var instance;
    var app = new Vue({
        el: '#app',
        data: {
            types: [],
            boards: [],

            boardRename: { boardID: null, boardType: null },
            boardDuration: { boardID: null, boardType: null },
            boardDelete: { boardID: null, boardType: null },

            boardValue: null,
            duration: 0,

            activeID: null,
            activeSeconds: null,
            activeClients: null
        },
        methods: {
            showRename: function(boardID, boardType, boardValue) {
                this.boardRename = { boardID, boardType };
                this.boardValue = boardValue.replace(/<br\s*[\/]?>/gi, '\n')
                M.Modal.getInstance(document.getElementById('modal-rename')).open();
            },
            closeRename: function() {
                M.Modal.getInstance(document.getElementById('modal-rename')).close();
                            },
            updateValue: function() {
                M.Modal.getInstance(document.getElementById('modal-rename')).close();
                showLoading('Änderungen speichern...');
                axios.post( './rename/'+this.boardRename.boardID, { value: this.boardValue })
                    .then((response) => {
                        if(response.data.success) {
                            M.toast({html: 'Änderungen gespeichert.<br>'+this.boardRename.boardType});
                        } else {
                            M.toast({html: 'Speichern fehlgeschlagen.<br>'+this.boardRename.boardType});
                        }
                        fetchData(this);
                        hideLoading();
                    });
            },
            showDuration: function(boardID, boardType, boardDuration) {
                this.boardDuration = { boardID, boardType };
                this.duration = boardDuration
                M.Modal.getInstance(document.getElementById('modal-duration')).open();
            },
            closeDuration: function() {
                M.Modal.getInstance(document.getElementById('modal-duration')).close();
            },
            updateDuration: function() {
                M.Modal.getInstance(document.getElementById('modal-rename')).close();
                showLoading('Änderungen speichern...');
                axios.post( './duration/'+this.boardDuration.boardID, { duration: this.duration })
                    .then((response) => {
                        if(response.data.success) {
                            M.toast({html: 'Änderungen gespeichert.<br>'+this.boardDuration.boardType});
                        } else {
                            M.toast({html: 'Speichern fehlgeschlagen.<br>'+this.boardDuration.boardType});
                        }
                        fetchData(this);
                        hideLoading();
                    });
            },
            showDelete: function(boardID, boardType) {
                this.boardDelete = { boardID, boardType };
                M.Modal.getInstance(document.getElementById('modal-delete')).open();
            },
            closeDelete: function() {
                M.Modal.getInstance(document.getElementById('modal-delete')).close();
            },
            deleteBoard: function() {
                M.Modal.getInstance(document.getElementById('modal-delete')).close();
                showLoading('Element löschen...');
                axios.post( './delete/'+this.boardDelete.boardID)
                    .then((response) => {
                        if(response.data.success) {
                            M.toast({html: 'Element gelöscht.<br>'+this.boardDelete.boardType});
                        } else {
                            M.toast({html: 'Löschen fehlgeschlagen.<br>'+this.boardDelete.boardType});
                        }
                        fetchData(this);
                        hideLoading();
                    });
            },
            updateType: function(boardID, type) {
                showLoading('Typ ändern...');
                axios.post( './type/'+boardID, { type })
                    .then((response) => {
                        if(response.data.success) {
                            M.toast({ html: 'Typ geändert.<br>'+ type });
                        } else {
                            M.toast({ html: 'Änderung fehlgeschlagen.<br>'+ type });
                        }
                        fetchData(this);
                        hideLoading();
                    });
            },
            moveUp: function(boardID) {
                showLoading('Position ändern...');
                axios.post( './move-up/'+boardID)
                    .then((response) => {
                        if(response.data.success) {
                            M.toast({ html: 'Position geändert.' });
                        } else {
                            M.toast({ html: 'Änderung fehlgeschlagen.' });
                        }
                        fetchData(this);
                        hideLoading();
                    });
            },
            moveDown: function(boardID) {
                showLoading('Position ändern...');
                axios.post( './move-down/'+boardID)
                    .then((response) => {
                        if(response.data.success) {
                            M.toast({ html: 'Position geändert.' });
                        } else {
                            M.toast({ html: 'Änderung fehlgeschlagen.' });
                        }
                        fetchData(this);
                        hideLoading();
                    });
            },
            toggleVisibility: function(boardID) {
                showLoading('Sichtbarkeit ändern...');
                axios.post( './toggle-visibility/'+boardID)
                    .then((response) => {
                        if(response.data.success) {
                            M.toast({ html: 'Sichtbarkeit geändert.' });
                        } else {
                            M.toast({ html: 'Änderung fehlgeschlagen.' });
                        }
                        fetchData(this);
                        hideLoading();
                    });
            },
            newEntry: function() {
                showLoading('Neuer Eintrag erstellen...');
                axios.post( './add')
                    .then((response) => {
                        if(response.data.success) {
                            M.toast({ html: 'Neuer Eintrag erstellt.' });
                        } else {
                            M.toast({ html: 'Änderung fehlgeschlagen.' });
                        }
                        fetchData(this);
                        hideLoading();
                    });
            },
            showUpload: function(boardID) {
                document.getElementById('upload-file-'+boardID).click();
            },
            upload: function(file, boardID) {
                showLoading('Hochladen: 0 %');
                var data = new FormData();
                data.append('file', file);
                var config = {
                    onUploadProgress: function(progressEvent) {
                        var percentCompleted = Math.round( (progressEvent.loaded * 100) / progressEvent.total );
                        showLoading('Hochladen: '+ percentCompleted +' %');
                    }
                };
                var instance = this;
                axios.post('./upload/' + boardID, data, config)
                    .then(function (res) {
                        if(res.data.success) {
                            fetchData(instance);
                            M.toast({ html: 'Datei hochgeladen.' });
                        } else if(res.data.message) {
                            M.toast({ html: res.data.message });
                        }
                    })
                    .catch(function (err) {
                        M.toast({ html: 'Ein Fehler ist aufgetreten.' });
                        console.log(err);
                    })
                    .finally(function () {
                        hideLoading();
                    });
            },
            logout: function() {
                axios.post('./logout')
                    .then((response) => {
                        if(response.data.success) {
                            window.location = 'login';
                        } else {
                            M.toast({html: 'Logout fehlgeschlagen.'});
                        }
                    });
            }
        },
        mounted: function () {
            this.$nextTick(() => {
                instance = this;
                fetchData(this);
                fetchInfo();
            })
        },
        computed: {
            typeReadable() {
                return type => {
                    switch(type) {
                        case 'PLAN': return "Vertretungsplan";
                        case 'TEXT': return "Text";
                        case 'PDF': return "PDF";
                        case 'IMG': return "Bild";
                    }
                }
            }
        },
        watch: {
            boards: function (val, oldVal) {
                this.$nextTick(function () {
                    M.AutoInit();
                    console.log("Materialize auto init");
                })
            },
            boardValue: function (val, oldVal) {
                this.$nextTick(function () {
                    M.textareaAutoResize(document.getElementById("modal-rename-input")); // auto resize text area
                    M.updateTextFields();
                    console.log("fix text area");
                })
            },
        }
    });

    function fetchData(instance) {
        axios.get('data')
            .then((response) => {
                if(response.data) {

                    if(response.data.needLogin) {
                        window.location = 'login';
                        return;
                    }

                    instance.types = response.data.types;
                    instance.boards = response.data.boards;

                    console.log("Data fetched.");
                } else {
                    M.toast({ html: 'Daten konnten nicht geladen werden' });
                }
            });
    }

    function fetchInfo() {
        axios.get('./info')
            .then((response) => {
                if(response.data) {
                    instance.activeID = response.data.activeID;
                    instance.activeSeconds = response.data.activeSeconds;
                    instance.activeClients = response.data.activeClients;

                    if(response.data.timeToRefresh % 5 === 0) {
                        console.log('Time to refresh: ' + response.data.timeToRefresh+'s')
                    }

                    setTimeout(fetchInfo, 1000);
                }
            });
    }

    M.AutoInit();

</script>
<@loading.render/>
<@toaster.render/>
</body>
</html>