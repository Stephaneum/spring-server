
<#-- group board -->

<#macro render>
    <template id="group-board">
        <div class="card" style="margin: 0; display: flex; flex-direction: column" :style="{ height: (height || 0)+'px' }">
            <div style="flex: 1; overflow-y: auto;">
                <div v-if="textMode" style="height: 100%">
                    <div id="board-text-editor" style="height: auto"></div>
                </div>
                <template v-else>
                    <div v-if="lastUpdate && areas.length === 0" class="empty-hint">
                        Die Tafel ist leer.
                    </div>
                    <div v-else style="height: 100%; display: flex; justify-content: center; align-items: center">
                        <template v-for="a in areas">
                            <div v-if="a.type === 'TEXT'" v-html="a.text"></div>
                            <div v-else-if="a.type === 'IMAGE'" style="padding: 20px">
                                <img :src="downloadLink(a.file)" style="height: 100%; width: 100%"/>
                            </div>
                            <div v-else-if="a.type === 'PDF'" style="width: 100%; height: 100%; padding: 20px">
                                <object :data="downloadLink(a.file)" type="application/pdf" style="width: 100%; height: 100%">
                                    Ihr Browser unterstützt kein PDF.
                                </object>
                            </div>
                        </template>
                    </div>
                </template>
            </div>
            <div v-if="writeBoard" style="display: flex; align-items: center; flex-direction: row-reverse; padding: 20px; background-color: #fafafa; border-top: 1px solid #e0e0e0">
                <form method="POST" enctype="multipart/form-data" style="display: none">
                    <input name="file" type="file" id="upload-board-file" @change="uploadBoardFile" multiple>
                </form>
                <template v-if="textMode">
                    <a @click="updateText((activeArea||{}).id)" href="#!" class="waves-effect waves-light btn green darken-4">
                        <i class="material-icons left">save</i>
                        Speichern
                    </a>
                    <a @click="toggleTextMode" href="#!" style="margin-right: 20px" class="waves-effect waves-light btn-flat">
                        <i class="material-icons left">close</i>
                        Abbrechen
                    </a>
                </template>
                <template v-else-if="activeArea">
                    <template v-if="activeArea.type === 'TEXT'">
                        <a @click="toggleTextMode" href="#!" class="waves-effect waves-light btn green darken-4">
                            <i class="material-icons left">edit</i>
                            Bearbeiten
                        </a>
                        <a @click="deleteArea(activeArea.id)" href="#!" style="margin-right: 20px" class="waves-effect waves-light btn-flat">
                            <i class="material-icons left">delete</i>
                            Löschen
                        </a>
                    </template>
                    <template v-if="activeArea.type === 'IMAGE' || activeArea.type === 'PDF'">
                        <a :href="downloadLink(activeArea.file) + '?download=true'"  class="waves-effect waves-light btn green darken-4">
                            <i class="material-icons left">arrow_downward</i>
                            Download
                        </a>
                        <a @click="deleteArea(activeArea.id)" href="#!" style="margin-right: 20px" class="waves-effect waves-light btn red darken-4">
                            <i class="material-icons left">delete</i>
                            Löschen
                        </a>
                    </template>
                </template>
                <template v-else>
                    <a @click="toggleTextMode" href="#!" class="waves-effect waves-light btn green darken-4">
                        <i class="material-icons left">format_align_left</i>
                        Text
                    </a>
                    <a @click="showUpload" href="#!" style="margin-right: 20px" class="waves-effect waves-light btn green darken-4">
                        <i class="material-icons left">cloud_upload</i>
                        Bild / PDF
                    </a>
                </template>

            </div>
        </div>
    </template>

    <script type="text/javascript">
        Vue.component('group-board', {
            props: ['writeBoard', 'visible', 'timestampUrl', 'boardUrl', 'addTextUrl', 'updateTextUrl', 'addFileUrl', 'deleteAreaUrl', 'height'],
            data: function () {
                return {
                    lastUpdate: null,
                    areas: [],
                    textMode: false
                }
            },
            methods: {
                fetchData: async function(force = false) {
                    try {
                        const timestamp = await axios.get(this.timestampUrl);
                        if(force || !this.lastUpdate || timestamp.data.timestamp !== this.lastUpdate) {
                            const areas = await axios.get(this.boardUrl);
                            this.areas = areas.data;
                            this.lastUpdate = timestamp.data.timestamp;
                        }
                    } catch (e) {
                        M.toast({html: 'Interner Fehler.'});
                    }

                    hideLoading();
                },
                toggleTextMode: function() {
                    this.textMode = !this.textMode;
                    if(this.textMode) {
                        this.$nextTick(() => {
                            $('#board-text-editor').trumbowyg(TEXT_EDITOR_CONFIG);
                            if(this.activeArea) {
                                $('#board-text-editor').trumbowyg('html', this.activeArea.text);
                            }
                        });
                    } else {
                        $('#board-text-editor').trumbowyg('destroy');
                    }
                },
                updateText: async function(id) {
                    showLoadingInvisible();
                    try {
                        const text = $('#board-text-editor').trumbowyg('html');
                        await axios.post(id ? this.updateTextUrl : this.addTextUrl, { id, text });
                        M.toast({html: 'Text gespeichert.'});
                        await this.fetchData(true);
                        this.textMode = false;
                        $('#board-text-editor').trumbowyg('destroy');
                    } catch (e) {
                        M.toast({html: 'Interner Fehler.'});
                    }
                },
                showUpload: function() {
                    document.getElementById('upload-board-file').click();
                },
                uploadBoardFile: function(event) {
                    event.preventDefault();
                    this.filesDragging = false; // TODO: drag and drop
                    var files = event.dataTransfer ? event.dataTransfer.files : event.currentTarget.files;
                    if(files.length !== 1) {
                        M.toast({html: 'Nur eine Datei erlaubt.'});
                        return;
                    }
                    uploadMultipleFiles(this.addFileUrl, files, {
                        params: {},
                        uploaded: (file) => {},
                        finished: () => {
                            this.fetchData(true);
                        }
                    });
                },
                deleteArea: async function(id) {
                    showLoadingInvisible();
                    try {
                        await axios.post(this.deleteAreaUrl+id);
                        M.toast({html: 'Element gelöscht.'});
                        await this.fetchData(true);
                        this.textMode = false;
                        $('#board-text-editor').trumbowyg('destroy');
                    } catch (e) {
                        M.toast({html: 'Interner Fehler.'});
                    }
                },
            },
            computed: {
                activeArea: function() {
                    if(this.areas.length === 0)
                        return null;
                    else
                        return this.areas[0];
                },
                downloadLink: function(file) {
                    return (file) => '/api/cloud/download/file/'+file.id;
                }
            },
            watch: {
                visible: function() {
                    this.textMode = false;
                    $('#board-text-editor').trumbowyg('destroy');
                },
            },
            mounted: async function() {
                await this.fetchData();
                setInterval(this.fetchData, 5000);
            },
            template: '#group-board'
        });
    </script>

    <style>
        .trumbowyg-box, .trumbowyg-editor {
            margin: 0 !important;
        }
    </style>
</#macro>