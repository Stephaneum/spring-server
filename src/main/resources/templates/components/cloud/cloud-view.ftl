
<#-- cloud view -->

<#macro render>
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
                    {{ folderCount }} Ordner / {{ fileCount }} Dateien
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
                        <div class="progress" style="width: 70%; height: 20px">
                            <div class="determinate white" :style="{ width: (storage.percentage*100)+'%' }"></div>
                        </div>
                        <span style="font-size: 1.5em">{{ storage.used }} / {{ storage.total }}</span>
                    </template>
                </div>
            </div>

            <!-- MAIN CONTENT -->
            <div class="col s10">
                <div class="tab-panel white z-depth-1" style="margin: 0; min-height: 450px;padding: 10px">
                    <ul class="collection" style="margin: 0">
                        <li v-for="f in files" class="collection-item">
                            <div style="display: flex; align-items: center;">

                                <span style="flex-grow: 1; display: flex; align-items: center; ">
                                    <i v-if="f.isFolder" style="font-size: 1.5em; margin-right: 10px" class="material-icons">folder</i>
                                    <span :class="{ 'folder-link': f.isFolder }" @click="openFolder(f)">{{ f.isFolder ? f.name : f.fileName }}</span>
                                </span>

                                <span style="flex: 0 0 320px; text-align: right;">
                                    <span v-if="f.public" class="green-badge-light">öffentlich</span>
                                    <span style="margin-left: 20px" class="green-badge-light">{{ f.sizeReadable }}</span>
                                    <span v-if="f.time" style="margin-left: 20px" class="green-badge-light">{{ f.time }}</span>
                                </span>

                                <span style="flex: 0 0 270px; text-align: right">
                                    <a :href="downloadLink(f)" class="tooltipped waves-effect waves-light green darken-3 btn margin-1" data-tooltip="Download" data-position="bottom">
                                        <i class="material-icons">arrow_downward</i>
                                    </a>

                                    <a @click="emitSelected(p)" class="tooltipped waves-effect waves-light teal darken-2 btn margin-1" href="#!" data-tooltip="Link" data-position="bottom">
                                        <i class="material-icons">language</i>
                                    </a>

                                    <a @click="emitSelected(p)" class="tooltipped waves-effect waves-light teal darken-2 btn margin-1" href="#!" data-tooltip="Bearbeiten" data-position="bottom">
                                        <i class="material-icons">edit</i>
                                    </a>

                                    <a @click="showDelete(p)" class="tooltipped waves-effect waves-light btn red darken-4 margin-1" href="#!" data-tooltip="Löschen" data-position="bottom">
                                        <i class="material-icons">delete</i>
                                    </a>
                                </span>
                            </div>
                        </li>
                    </ul>
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
                    folderID: null,
                    actions: actions,
                    files: [],
                    folderStack: [],
                    fileCount: 0,
                    folderCount: 0,
                    storage: {
                        used: '',
                        total: '',
                        percentage: 0
                    }
                }
            },
            methods: {
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
                fetchData: async function () {
                    var url = this.folderID ? './api/cloud/user/' + this.folderID : './api/cloud/user';
                    var res = await axios.get(url);
                    this.files = this.digestFiles(res.data);
                    this.count();
                    this.storage = (await axios.get('./api/cloud/info')).data;
                    this.storage.percentage = this.storage.used / (this.storage.total ? this.storage.total : 1);
                    this.storage.used = storageReadable(this.storage.used);
                    this.storage.total = storageReadable(this.storage.total);
                    M.Tooltip.init(document.querySelectorAll('.tooltipped'), {});
                    hideLoading();
                }
            },
            computed: {
                downloadLink: function () {
                    return (file) => {
                        if(file.isFolder)
                            return './api/cloud/download/folder/'+file.id;
                        else
                            return './api/cloud/download/file/'+file.id;
                    };
                },
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

        .progress {
            background-color: transparent !important;
            border: 2px solid white;
        }
    </style>
</#macro>