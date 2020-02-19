
<#-- view the file and some info / functions -->

<#macro render>
    <template id="file-popup">
        <div style="position: fixed; z-index: 500; left: 0; top: 0;width: 100%; height: 100%; overflow: auto; background-color: rgb(0,0,0); background-color: rgba(0,0,0,0.6);">
            <div style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center">

                <div style="width: calc(100% - 50px); height: calc(100% - 50px); display: flex">
                    <div style="height: 100%; flex: 1; display: flex; align-items: center; justify-content: center;">
                        <img v-if="image(file)" :src="file.link" style="max-width: 100%; max-height: 100%"/>
                        <object v-else-if="pdf(file)" :data="file.link" type="application/pdf" style="width: 100%; height: 100%">
                            Ihr Browser unterstützt kein PDF.
                        </object>
                        <iframe v-else-if="docx(file)" :src="officeLink(file)" style="width: 100%; height: 100%; border:none">
                            This is an embedded <a target='_blank' href='http://office.com'>Microsoft Office</a> document, powered by
                            <a target='_blank' href='http://office.com/webapps'>Office Online</a>.
                        </iframe>
                        <div v-else style="background-color: white; border-radius: 20px; padding: 20px; text-align: center">
                            Keine Vorschau verfügbar.
                        </div>
                    </div>

                    <div style="flex:0 0 20px;"></div>

                    <div style="background-color: white; height: 100%; border-radius: 20px; flex: 1;max-width: 400px; display: flex; flex-direction: column; justify-content: space-between">

                        <!-- header -->
                        <div>
                            <div style="text-align: right">
                                <a @click="exit" class="waves-effect btn-flat" href="#!" style="margin: 10px 10px 0 0">
                                    <i class="material-icons">close</i>
                                </a>
                            </div>

                            <p style="font-size: 2em; font-weight: bold; text-align: center; padding: 0 20px 0 20px">{{ file.fileName }}</p>
                        </div>


                        <div>
                            <!-- info -->
                            <p class="file-popup-sub-header">Informationen</p>
                            <div class="file-popup-sub-panel" id="file-popup-info" style="display: flex; flex-wrap: wrap;">
                                <div>Hochgeladedatum:</div>
                                <div style="text-align: right">{{ file.time }}</div>
                                <div>Dateigröße:</div>
                                <div style="text-align: right">{{ file.sizeReadable }}</div>
                                <div>Öffentlich:</div>
                                <div style="text-align: right">{{ file.public ? 'Ja' : 'Nein' }}</div>
                            </div>

                            <div style="height: 30px"></div>

                            <!-- actions -->
                            <p class="file-popup-sub-header">Aktionen</p>
                            <div class="file-popup-sub-panel" style="display: flex; justify-content: space-evenly">

                                <a @click="onPublic" class="tooltipped waves-effect waves-light teal darken-2 btn margin-1" href="#!" data-tooltip="Link" data-position="bottom">
                                    <i class="material-icons">language</i>
                                </a>

                                <a @click="onEdit" class="tooltipped waves-effect waves-light teal darken-2 btn margin-1" href="#!" data-tooltip="Bearbeiten" data-position="bottom">
                                    <i class="material-icons">edit</i>
                                </a>

                                <a @click="onDelete" class="tooltipped waves-effect waves-light btn red darken-4 margin-1" href="#!" data-tooltip="Löschen" data-position="bottom">
                                    <i class="material-icons">delete</i>
                                </a>
                            </div>
                        </div>

                        <!-- download -->
                        <div style="text-align: center; padding-bottom: 40px">
                            <a class="waves-effect waves-light green darken-3 btn-large" :href="file.link + '?download=true'" style="">
                                <i class="material-icons left">arrow_downward</i>
                                Herunterladen
                            </a>
                        </div>
                    </div>
                </div>

            </div>
        </div>
    </template>

    <script type="text/javascript">
        Vue.component('file-popup', {
            props: ['file'],
            methods: {
                exit: function() {
                    this.$emit('onexit');
                },
                onPublic: function() {
                    this.$emit('onpublic');
                },
                onEdit: function() {
                    this.$emit('onedit');
                },
                onDelete: function() {
                    this.$emit('ondelete');
                }
            },
            computed: {
                image: function() {
                    return (file) => {
                        return !file.isFolder && file.mime.startsWith('image');
                    }
                },
                pdf: function() {
                    return (file) => {
                        return !file.isFolder && (file.mime === 'application/pdf' || file.fileName.toLowerCase().endsWith('.pdf'));
                    }
                },
                docx: function() {
                    return (file) => {
                        return !file.isFolder && (file.fileName.toLowerCase().endsWith('.docx') || file.fileName.toLowerCase().endsWith('.doc'));
                    }
                },
                officeLink: function() {
                    // TODO make it temporary
                    return (file) => {
                        return 'https://view.officeapps.live.com/op/embed.aspx?src=' + encodeURI(window.location.origin + '/api/cloud/download/file/'+ file.id);
                    }
                }
            },
            mounted: function() {
                var instance = this;
                initTooltips();
                document.onkeydown = function(evt) {
                    evt = evt || window.event;
                    var isEscape = false;
                    if ("key" in evt) {
                        isEscape = (evt.key === "Escape" || evt.key === "Esc");
                    } else {
                        isEscape = (evt.keyCode === 27);
                    }

                    if (isEscape) {
                        instance.exit();
                    }
                };
            },
            destroyed: function() {
                document.onkeydown = null;
            },
            template: '#file-popup'
        });
    </script>

    <style>
        #file-popup-info > div {
            flex: 50% !important;
            font-size: 1.2em;
        }

        .file-popup-sub-header {
            text-align: center;
            font-size: 1.5em;
        }

        .file-popup-sub-panel {
            background-color: #eeeeee;
            border-radius: 10px;
            margin: 20px;
            padding: 20px;
        }
    </style>
</#macro>