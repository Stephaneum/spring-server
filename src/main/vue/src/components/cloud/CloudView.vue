<template>
    <div class="row">
        <!-- PATH -->
        <div class="col s10 offset-s2" style="display: flex; justify-content: space-between; align-items: center; padding-bottom: 15px">
            <div style="display: flex;">
                <div style="margin-left: 10px" class="path-item" @click="toHomeFolder" :style="{ 'background-color': statsMode ? 'grey !important' : null }">
                    Home
                </div>

                <div v-for="f in folderStack" :key="f.id" style="display: flex; align-items: center">
                    <i style="font-size: 2em; margin: 0" class="material-icons">chevron_right</i>
                    <span class="path-item" @click="openFolder(f)" :style="{ 'background-color': statsMode ? 'grey !important' : null }">
                        {{ f.name }}
                    </span>
                </div>
            </div>

            <div style="padding-right: 20px">
                <i @click="setGridView(true)" style="font-size: 2em; margin: 0; padding: 5px; border-radius: 5px; user-select: none; cursor: pointer" class="material-icons" :class="!statsMode && gridView ? ['green', 'darken-2', 'white-text'] : []">apps</i>
                <i @click="setGridView(false)" style="font-size: 2em; margin: 0; padding: 5px; border-radius: 5px; user-select: none; cursor: pointer" class="material-icons" :class="!statsMode && !gridView ? ['green', 'darken-2', 'white-text'] : []">menu</i>
            </div>
        </div>

        <!-- ACTIONS -->
        <div class="col s2">

            <form method="POST" enctype="multipart/form-data" style="display: none">
                <input name="file" type="file" id="upload-files" @change="uploadFiles" multiple>
            </form>

            <div @click="showUpload" class="action-btn z-depth-1">
                <i style="font-size: 3em" class="material-icons">cloud_upload</i>
                <span style="font-size: 1.5em">Hochladen</span>
            </div>

            <div @click="showCreateFolder" class="action-btn z-depth-1">
                <i style="font-size: 3em" class="material-icons">create_new_folder</i>
                <span style="font-size: 1.5em">Neuer Ordner</span>
            </div>

            <div @click="toggleStatsMode" class="action-btn z-depth-1">
                <div class="storage-bar" style="width: 70%; height: 20px">
                    <div :style="{ width: (storage.percentage*100)+'%' }"></div>
                </div>
                <span style="font-size: 1.5em">{{ storage.used }} / {{ storage.total }}</span>
            </div>
        </div>

        <!-- MAIN CONTENT -->
        <div class="col s10">
            <div class="tab-panel white z-depth-1" style="margin: 0; min-height: 530px;padding: 10px">
                <cloud-stats v-if="statsMode" :info="storage" :teacherchat="teacherchat" @onexit="toggleStatsMode"></cloud-stats>
                <template v-else>
                    <template v-if="files.length !== 0">
                        <file-grid v-if="gridView" :files="files" :shared-mode="sharedMode" @onselect="select"></file-grid>
                        <file-list v-else :files="files" :shared-mode="sharedMode" :modify-all="modifyAll" @onselect="select" @onpublic="showPublic" @onedit="showEdit" @ondelete="showDelete"></file-list>
                    </template>
                    <div v-else style="height: 400px;" class="empty-hint">
                        {{ fetching ? 'Lade Dateien...' : 'Dieser Ordner ist leer.' }}
                    </div>
                </template>
            </div>
        </div>

        <div v-if="!statsMode" class="col s12" style="text-align: right; padding: 15px 25px 0 0">
            {{ folderCount }} Ordner / {{ fileCount }} Dateien
        </div>

        <!-- create folder modal -->
        <div id="modal-folder" class="modal">
            <div class="modal-content">
                <h4>Neuer Ordner</h4>
                <br>
                <p>Es sind alle Zeichen erlaubt außer "/" und "\".</p>
                <p>Wird erstellt in: <b>{{ folderStack.length != 0 ? folderStack.slice(-1)[0].name : 'Homeverzeichnis' }}</b></p>
                <br>
                <div class="input-field">
                    <i class="material-icons prefix">folder</i>
                    <label for="create-folder-name">Name</label>
                    <input v-model="createFolderName" type="text" id="create-folder-name"/>
                </div>
            </div>
            <div class="modal-footer">
                <a @click="closeCreateFolder" href="#!"
                   class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
                <button @click="createFolder" type="button" class="btn waves-effect waves-light green darken-3">
                    <i class="material-icons left">save</i>
                    Erstellen
                </button>
            </div>
        </div>

        <!-- public modal -->
        <div id="modal-public" class="modal">
            <div class="modal-content">
                <h4>{{ selected.fileName }}</h4>
                <br>
                <a @click="updatePublic(false)" href="#!" class="waves-effect waves-light teal btn margin-1" :class="{ 'darken-3': selected.public, 'disabled': !selected.canModify }">
                    <i class="material-icons left">lock</i>
                    Privat
                </a>

                <a @click="updatePublic(true)" href="#!" class="waves-effect waves-light teal btn margin-1" :class="{ 'darken-3': !selected.public, 'disabled': !selected.canModify }">
                    <i class="material-icons left">lock_open</i>
                    Öffentlich
                </a>

                <div v-if="selected.public" style="margin-top: 40px">
                    Link:
                    <div class="green lighten-4" style="margin-top: 10px; padding: 10px">
                        {{ publicLink(selected) }}
                    </div>
                </div>

            </div>
            <div class="modal-footer">
                <a @click="closePublic" href="#!" class="modal-close waves-effect waves-green btn-flat">Schließen</a>
            </div>
        </div>

        <!-- edit modal -->
        <div id="modal-edit" class="modal">
            <div class="modal-content">
                <h4>{{ selected.fileName || selected.name }}</h4>
                <br>
                In Arbeit

            </div>
            <div class="modal-footer">
                <a @click="closeEdit" href="#!" class="modal-close waves-effect waves-green btn-flat">Schließen</a>
            </div>
        </div>

        <!-- delete modal -->
        <div id="modal-delete" class="modal">
            <div class="modal-content">
                <h4>Löschen fortfahren?</h4>
                <br>
                <p><b>{{ selected.name || selected.fileName }}</b> wird gelöscht.</p>
                <p>Dieser Vorgang kann nicht rückgangig gemacht werden.</p>
            </div>
            <div class="modal-footer">
                <a @click="closeDelete" href="#!" class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
                <a @click="deleteItem" href="#!" class="modal-close waves-effect waves-red btn red darken-4">
                    <i class="material-icons left">delete</i>
                    Löschen
                </a>
            </div>
        </div>

        <file-popup v-if="file" :file="file" @onexit="closeFilePopup" @onpublic="showPublic(file)" @onedit="showEdit(file)" @ondelete="showDelete(file)"></file-popup>
    </div>
</template>

<script>
    import Axios from "axios"
    import M from "materialize-css"
    import moment from 'moment'
    import CloudStats from '@/components/cloud/CloudStats.vue'
    import FileGrid from '@/components/cloud/FileGrid.vue'
    import FileList from '@/components/cloud/FileList.vue'
    import FilePopup from '@/components/cloud/FilePopup.vue'
    import { storageReadable, uploadMultipleFiles, showLoading, showLoadingInvisible, hideLoading } from '@/helper/utils.js';

    export default {
        name: 'CloudView',
        components: {
            CloudStats, FileGrid, FileList, FilePopup
        },
        props: ['myId', 'sharedMode', 'modifyAll', 'rootUrl', 'uploadUrl', 'folderUrl', 'teacherchat'],
        data: function () {
            return {
                fetching: true,
                statsMode: false,
                gridView: true,
                folderID: null,
                files: [],
                folderStack: [],
                fileCount: 0,
                folderCount: 0,
                storage: {
                    used: '-',
                    total: '-',
                    percentage: 0,
                    count: 0,
                    privatePercentage: 0,
                    projectPercentage: 0,
                    classPercentage: 0,
                    teacherPercentage: 0
                },
                selected: {
                    fileName: null,
                    name: null, // for folders
                    public: false
                },
                createFolderName: null,
                file: null // for the file popup
            }
        },
        methods: {
            reset: async function() {
                this.statsMode = false;
                this.fetching = true;
                await this.toHomeFolder();
            },
            toggleStatsMode: function() {
                this.statsMode = !this.statsMode;
            },
            setGridView: function(grid) {

                if(this.statsMode)
                    return;

                this.gridView = grid;
                this.$nextTick(() => {
                    M.Tooltip.init(document.querySelectorAll('.tooltipped'), {});
                });
            },
            action: function(action) {
                console.log(action);
            },
            select: async function(file) {
                if(file.isFolder) {
                    await this.openFolder(file);
                } else {
                    this.file = file;
                }
            },
            closeFilePopup: function() {
                this.file = null;
            },
            openFolder: async function(folder) {

                if(this.statsMode)
                    return;

                showLoadingInvisible();
                var index = this.folderStack.findIndex(f => f.id === folder.id);
                if(index === -1) {
                    this.folderStack.push(folder);
                } else {
                    this.folderStack = this.folderStack.slice(0, index + 1);
                }
                this.folderID = folder.id;
                await this.fetchData();
            },
            toHomeFolder: async function() {
                if(this.statsMode)
                    return;

                showLoadingInvisible();
                this.folderStack = [];
                this.folderID = null;
                await this.fetchData();
            },
            digestFiles: function(files) {
                files.forEach((f) => {
                    if(f.timestamp)
                        f.time = moment(f.timestamp).format('DD.MM.YYYY');

                    f.sizeReadable = storageReadable(f.size);

                    if(f.isFolder)
                        f.link = '/api/cloud/download/folder/'+f.id;
                    else
                        f.link = '/api/cloud/download/file/'+f.id;

                    f.canModify = !f.locked && (!this.sharedMode || this.modifyAll || f.user.id === this.myId);
                });
                return files;
            },
            count: function() {
                this.fileCount = 0;
                this.folderCount = 0;
                this.files.forEach((f) => {
                    if(f.isFolder)
                        this.folderCount++;
                    else
                        this.fileCount++;
                });
            },
            showUpload: function() {
                document.getElementById('upload-files').click();
            },
            uploadFiles: function(event) {
                event.preventDefault();
                this.filesDragging = false; // TODO: drag and drop
                var files = event.dataTransfer ? event.dataTransfer.files : event.currentTarget.files;
                uploadMultipleFiles(this.uploadUrl, files, {
                    params: { 'folder': this.folderID },
                    uploaded: () => {},
                    finished: () => {
                        this.fetchData();
                    }
                });
            },
            showCreateFolder: function() {
                this.createFolderName = null;
                M.Modal.getInstance(document.getElementById('modal-folder')).open();
            },
            closeCreateFolder: function() {
                M.Modal.getInstance(document.getElementById('modal-folder')).close();
            },
            createFolder: async function() {
                if(!this.createFolderName) {
                    M.toast({html: 'Fehler<br>Bitte gib dem Ordner einen Namen.'});
                    return;
                }

                showLoadingInvisible();
                M.Modal.getInstance(document.getElementById('modal-folder')).close();
                try {
                    await Axios.post(this.folderUrl, { name: this.createFolderName, parentID: this.folderID })
                    M.toast({html: 'Ordner erstellt<br>'+this.createFolderName});
                } catch (e) {
                    M.toast({html: 'Fehlgeschlagen.<br>'+this.selected.fileName});
                }
                this.fetchData();
            },
            showPublic: function(f) {
                this.selected = f;
                M.Modal.getInstance(document.getElementById('modal-public')).open();
            },
            closePublic: function() {
                M.Modal.getInstance(document.getElementById('modal-public')).close();
                showLoadingInvisible();
                this.fetchData();
            },
            updatePublic: function(isPublic) {
                showLoadingInvisible();
                Axios.post( '/api/cloud/update-public-file', { fileID: this.selected.id, isPublic })
                    .then((response) => {
                        if(response.data.success) {
                            this.selected.public = isPublic;
                        } else if(response.data.message) {
                            M.toast({html: 'Fehlgeschlagen.<br>'+response.data.message});
                        } else {
                            M.toast({html: 'Fehlgeschlagen.<br>'+this.selected.fileName});
                        }
                        this.fetchData();
                    });
            },
            showEdit: function(f) {
                this.selected = f;
                M.Modal.getInstance(document.getElementById('modal-edit')).open();
            },
            closeEdit: function() {
                M.Modal.getInstance(document.getElementById('modal-edit')).close();
                showLoadingInvisible();
                this.fetchData();
            },
            showDelete: function(f) {
                this.selected = f;
                M.Modal.getInstance(document.getElementById('modal-delete')).open();
            },
            closeDelete: function() {
                M.Modal.getInstance(document.getElementById('modal-delete')).close();
            },
            deleteItem: function() {
                M.Modal.getInstance(document.getElementById('modal-delete')).close();
                var isFolder = this.selected.isFolder;
                var typeString = isFolder ? 'Ordner' : 'Datei';
                var name = this.selected.fileName || this.selected.name;
                var route = isFolder ? '/api/cloud/delete-folder/' : '/api/cloud/delete-file/';
                showLoading(typeString + ' löschen...');
                Axios.post( route + this.selected.id)
                    .then((response) => {
                        if(response.data.success) {
                            M.toast({html: typeString + ' gelöscht.<br>'+name});
                        } else if(response.data.message) {
                            M.toast({html: 'Löschen fehlgeschlagen.<br>'+response.data.message});
                        } else {
                            M.toast({html: 'Löschen fehlgeschlagen.<br>'+name});
                        }
                        this.closeFilePopup();
                        this.fetchData();
                    });
            },
            fetchData: async function () {
                this.fetching = true;
                var url = this.folderID ? '/api/cloud/view/' + this.folderID : this.rootUrl;
                var res = await Axios.get(url);
                this.files = this.digestFiles(res.data);
                this.count();
                this.storage = (await Axios.get('/api/cloud/info')).data;
                this.storage.percentage = this.storage.used / (this.storage.total ? this.storage.total : 1);
                this.storage.percentage = Math.min(Math.max(this.storage.percentage, 0.05), 0.95);
                this.storage.privatePercentage = this.storage.private / this.storage.used;
                this.storage.projectPercentage = this.storage.project / this.storage.used;
                this.storage.classPercentage = this.storage.schoolClass / this.storage.used;
                this.storage.teacherPercentage = this.storage.teacherChat / this.storage.used;
                this.storage.free = storageReadable(this.storage.total - this.storage.used);
                this.storage.used = storageReadable(this.storage.used);
                this.storage.total = storageReadable(this.storage.total);
                this.storage.private = storageReadable(this.storage.private);
                this.storage.project = storageReadable(this.storage.project);
                this.storage.schoolClass = storageReadable(this.storage.schoolClass);
                this.storage.teacherChat = storageReadable(this.storage.teacherChat);
                M.Tooltip.init(document.querySelectorAll('.tooltipped'), {});
                hideLoading();
                this.fetching = false;
            },
        },
        computed: {
            publicLink: function () {
                return (file) => {
                    return window.location.origin + '/public/?file='+ file.id +'_' + encodeURI(file.fileName);
                };
            }
        },
        mounted: function() {
            M.AutoInit();
            this.fetchData();
        }
    }
</script>

<style scoped>
    .action-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        flex-direction: column;

        width: 100%;
        height: 120px;
        margin-bottom: 30px;
        background-color: #1b5e20;
        color: white;
    }

    .action-btn:hover {
        background-color: #2e7d32;
        cursor: pointer;
    }

    .path-item {
        padding: 5px 15px 5px 15px;
        border-radius: 5px;
        background-color: #558b2f;
        color: white;
    }

    .path-item:hover {
        background-color: #689f38;
        cursor: pointer;
    }

    .storage-bar {
        background-color: rgba(255,255,255,0.2) !important;
        border: 2px solid white;
        border-radius: 20px;
        position: relative;
        margin: 10px 0 10px 0;
    }

    .storage-bar > div {
        position: absolute;
        left: -1px;
        top: 0;
        height: 100%;
        background-color: white;
        border-radius: 20px 0 0 20px;
    }
</style>