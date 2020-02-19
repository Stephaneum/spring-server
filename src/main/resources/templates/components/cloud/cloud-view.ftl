<#import "file-grid.ftl" as fileGrid/>
<#import "file-list.ftl" as fileList/>

<#-- cloud view -->

<#macro render>
    <@fileGrid.render/>
    <@fileList.render/>
    <template id="cloud-view">
        <div class="row">
            <!-- PATH -->
            <div class="col s10 offset-s2" style="display: flex; justify-content: space-between; align-items: center; padding-bottom: 15px">
                <div style="display: flex;">
                    <div style="margin-left: 10px" class="path-item" @click="toHomeFolder">
                        Home
                    </div>

                    <div v-for="f in folderStack" style="display: flex; align-items: center">
                        <i style="font-size: 2em; margin: 0" class="material-icons">chevron_right</i>
                        <span class="path-item" @click="openFolder(f)">
                        {{ f.name }}
                    </span>
                    </div>
                </div>

                <div style="padding-right: 20px">
                    <i @click="setGridView(true)" style="font-size: 2em; margin: 0; padding: 5px; border-radius: 5px; user-select: none; cursor: pointer" class="material-icons" :class="gridView ? ['green', 'darken-2', 'white-text'] : []">apps</i>
                    <i @click="setGridView(false)" style="font-size: 2em; margin: 0; padding: 5px; border-radius: 5px; user-select: none; cursor: pointer" class="material-icons" :class="!gridView ? ['green', 'darken-2', 'white-text'] : []">menu</i>
                </div>
            </div>

            <!-- ACTIONS -->
            <div class="col s2">
                <div v-for="a in actions" @click="action(a)" class="action-btn z-depth-1">
                    <template v-if="a.icon">
                        <i style="font-size: 3em" class="material-icons">{{ a.icon }}</i>
                        <span style="font-size: 1.5em">{{ a.name }}</span>
                    </template>
                    <template v-else>
                        <div id="storage-bar" style="width: 70%; height: 20px">
                            <div :style="{ width: (storage.percentage*100)+'%' }"></div>
                        </div>
                        <span style="font-size: 1.5em">{{ storage.used }} / {{ storage.total }}</span>
                    </template>
                </div>
            </div>

            <!-- MAIN CONTENT -->
            <div class="col s10">
                <div class="tab-panel white z-depth-1" style="margin: 0; min-height: 450px;padding: 10px">
                    <file-grid v-if="gridView" :files="files" @onselect="openFolder"></file-grid>
                    <file-list v-else :files="files" @onselect="openFolder" @onpublic="showPublic" @onedit="showEdit" @ondelete="showDelete"></file-list>
                </div>
            </div>

            <div class="col s12" style="text-align: right; padding: 15px 25px 0 0">
                {{ folderCount }} Ordner / {{ fileCount }} Dateien
            </div>

            <!-- public modal -->
            <div id="modal-public" class="modal">
                <div class="modal-content">
                    <h4>{{ selected.fileName }}</h4>
                    <br>
                    <a @click="updatePublic(false)" href="#!" class="waves-effect waves-light teal btn margin-1" :class="selected.public ? ['darken-3'] : []">
                        <i class="material-icons left">lock</i>
                        Privat
                    </a>

                    <a @click="updatePublic(true)" href="#!" class="waves-effect waves-light teal btn margin-1" :class="selected.public ? [] : ['darken-3']">
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
                    <p><b>{{ selected.fileName }}</b> wird gelöscht.</p>
                    <p>Dieser Vorgang kann nicht rückgangig gemacht werden.</p>
                </div>
                <div class="modal-footer">
                    <a @click="closeDelete" href="#!" class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
                    <a @click="deleteFile" href="#!" class="modal-close waves-effect waves-red btn red darken-4">
                        <i class="material-icons left">delete</i>
                        Löschen
                    </a>
                </div>
            </div>
        </div>
    </template>

    <script type="text/javascript">
        var actions = {
            upload: {
                id: 0,
                name: "Hochladen",
                icon: "cloud_upload"
            },
            createFolder: {
                id: 1,
                name: "Neuer Ordner",
                icon: "create_new_folder"
            },
            stats: {
                id: 2
            },
        };
        Vue.component('cloud-view', {
            props: ['mode', 'id'],
            data: function () {
                return {
                    gridView: true,
                    folderID: null,
                    actions: actions,
                    files: [],
                    folderStack: [],
                    fileCount: 0,
                    folderCount: 0,
                    storage: {
                        used: '-',
                        total: '-',
                        percentage: 0
                    },
                    selected: {
                        fileName: null,
                        name: null, // for folders
                        public: false
                    }
                }
            },
            methods: {
                setGridView: function(grid) {
                    this.gridView = grid;
                    M.Tooltip.init(document.querySelectorAll('.tooltipped'), {});
                },
                action: function(action) {
                    console.log(action);
                },
                openFolder: function(folder) {
                    if(!folder.isFolder)
                        return;

                    showLoadingInvisible();
                    var index = this.folderStack.findIndex(f => f.id === folder.id);
                    if(index === -1) {
                        this.folderStack.push(folder);
                    } else {
                        this.folderStack = this.folderStack.slice(0, index + 1);
                    }
                    this.folderID = folder.id;
                    this.fetchData();
                },
                toHomeFolder: function() {
                    showLoadingInvisible();
                    this.folderStack = [];
                    this.folderID = null;
                    this.fetchData();
                },
                digestFiles: function(files) {
                  files.forEach((f) => {
                      if(f.timestamp)
                        f.time = moment(f.timestamp).format('DD.MM.YYYY');

                      f.sizeReadable = storageReadable(f.size);

                      if(f.isFolder)
                          f.link = './api/cloud/download/folder/'+f.id;
                      else
                          f.link = './api/cloud/download/file/'+f.id;
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
                    axios.post( './api/cloud/update-public-file', { fileID: this.selected.id, isPublic })
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
                deleteFile: function() {
                    M.Modal.getInstance(document.getElementById('modal-delete')).close();
                    showLoading('Datei löschen...');
                    axios.post( './api/cloud/delete-file/'+this.selected.id)
                        .then((response) => {
                            if(response.data.success) {
                                M.toast({html: 'Datei gelöscht.<br>'+this.selected.fileName});
                            } else if(response.data.message) {
                                M.toast({html: 'Löschen fehlgeschlagen.<br>'+response.data.message});
                            } else {
                                M.toast({html: 'Löschen fehlgeschlagen.<br>'+this.selected.fileName});
                            }
                            this.fetchData();
                        });
                },
                fetchData: async function () {
                    var url = this.folderID ? './api/cloud/user/' + this.folderID : './api/cloud/user';
                    var res = await axios.get(url);
                    this.files = this.digestFiles(res.data);
                    this.count();
                    this.storage = (await axios.get('./api/cloud/info')).data;
                    this.storage.percentage = this.storage.used / (this.storage.total ? this.storage.total : 1);
                    this.storage.percentage = Math.min(Math.max(this.storage.percentage, 0.05), 0.95);
                    this.storage.used = storageReadable(this.storage.used);
                    this.storage.total = storageReadable(this.storage.total);
                    M.Tooltip.init(document.querySelectorAll('.tooltipped'), {});
                    hideLoading();
                },
            },
            computed: {
                publicLink: function () {
                    return (file) => {
                        return window.location.origin + '/public/?file='+ file.id +'_' + encodeURI(file.fileName)
                    };
                }
            },
            mounted: function() {
                M.AutoInit();
                this.fetchData();
            },
            template: '#cloud-view'
        });
    </script>

    <style>
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

        .folder-link:hover {
            color: #2e7d32;
            text-decoration: underline;
            cursor: pointer;
        }

        #storage-bar {
            background-color: rgba(255,255,255,0.2) !important;
            border: 2px solid white;
            border-radius: 20px;
            position: relative;
            margin: 10px 0 10px 0;
        }

        #storage-bar > div {
            position: absolute;
            left: -1px;
            top: 0;
            height: 100%;
            background-color: white;
            border-radius: 20px 0 0 20px;
        }
    </style>
</#macro>