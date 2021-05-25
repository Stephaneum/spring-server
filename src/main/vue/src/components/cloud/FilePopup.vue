<template>
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
                    <object v-else-if="text(file)" :data="file.link + '?txt=true'" type="text/plain" style="width: 100%; height: 100%; background-color: white">
                        Ihr Browser unterstützt dies nicht.
                    </object>
                    <video v-else-if="video(file)" controls="controls" style="width: 100%; height: 100%">
                        <source :src="file.link" />
                    </video>
                    <div v-else-if="audio(file)" style="width: 100%; height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center">
                        <i style="font-size: 10vw; color: white" class="material-icons">audiotrack</i>
                        <div style="height: 100px"></div>
                        <audio controls="controls" style="width: 100%; max-width: 800px">
                            <source :src="file.link" />
                        </audio>
                    </div>

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
                            <div>Zeitstempel:</div>
                            <div style="text-align: right">{{ file.time }}</div>
                            <div>Dateigröße:</div>
                            <div style="text-align: right">{{ file.sizeReadable }}</div>
                            <div>Öffentlich:</div>
                            <div style="text-align: right">{{ file.public ? 'Ja' : 'Nein' }}</div>
                        </div>

                        <!-- actions -->
                        <p class="file-popup-sub-header">Aktionen</p>
                        <div class="file-popup-sub-panel" style="display: flex; justify-content: space-evenly">

                            <a @click="onPublic" class="tooltipped waves-effect waves-light teal darken-2 btn margin-1" href="#!" data-tooltip="Link" data-position="bottom">
                                <i class="material-icons">language</i>
                            </a>

                            <a @click="onEdit" class="tooltipped waves-effect waves-light teal darken-2 btn margin-1" :class="{ 'disabled': !file.canModify }" href="#!" data-tooltip="Bearbeiten" data-position="bottom">
                                <i class="material-icons">edit</i>
                            </a>

                            <a @click="onDelete" class="tooltipped waves-effect waves-light btn red darken-4 margin-1" :class="{ 'disabled': !file.canModify }" href="#!" data-tooltip="Löschen" data-position="bottom">
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

<script>
    import Axios from "axios"
    import M from "materialize-css"

    export default {
        name: 'FilePopup',
        props: ['file'],
        data() {
            return {
                key: null // used for office documents
            }
        },
        methods: {
            exit() {
                this.$emit('exit');
            },
            onPublic() {
                this.$emit('public');
            },
            onEdit() {
                this.$emit('edit');
            },
            onDelete() {
                this.$emit('delete');
            }
        },
        computed: {
            image() {
                return (file) => {
                    return file.isFile && file.mime.startsWith('image');
                }
            },
            pdf() {
                return (file) => {
                    return file.isFile && (file.mime === 'application/pdf' || file.fileName.toLowerCase().endsWith('.pdf'));
                }
            },
            docx() {
                return (file) => {
                    return file.isFile && (file.fileName.toLowerCase().endsWith('.docx') || file.fileName.toLowerCase().endsWith('.doc'));
                }
            },
            text() {
                return (file) => {
                    const lowerCase = file.fileName.toLowerCase();
                    return file.isFile && (file.mime.startsWith('text') || lowerCase.endsWith('.txt') || lowerCase.endsWith('.html') || lowerCase.endsWith('.htm') || lowerCase.endsWith('.js'));
                }
            },
            video() {
                return (file) => {
                    const lowerCase = file.fileName.toLowerCase();
                    return file.isFile && (file.mime.startsWith('video') || lowerCase.endsWith('.mpeg') || lowerCase.endsWith('.mp4') || lowerCase.endsWith('.avi') || lowerCase.endsWith('.webm'));
                }
            },
            audio() {
                return (file) => {
                    const lowerCase = file.fileName.toLowerCase();
                    return file.isFile && (file.mime.startsWith('audio') || lowerCase.endsWith('.mp3') || lowerCase.endsWith('.ogg') || lowerCase.endsWith('.wav'));
                }
            },
            officeLink() {
                return (file) => {
                    if(this.key)
                        return 'https://view.officeapps.live.com/op/embed.aspx?src=' + encodeURIComponent(window.location.origin + '/files/internal/'+ file.id+'?download=true&key='+this.key);
                    else
                        return '';
                }
            }
        },
        async mounted() {
            const instance = this;
            M.Tooltip.init(document.querySelectorAll('.tooltipped'), {});
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

            try {
                const response = await Axios.get('/api/cloud/key/' + this.file.id);
                this.key = response.data.key;
            } catch (e) {
                M.toast({html: 'Interner Fehler.'});
            }
        },
        destroyed() {
            document.onkeydown = null;
        }
    }
</script>

<style scoped>
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