<#-- @ftlvariable name="title" type="java.lang.String" -->

<#import "/spring.ftl" as spring/>
<#import "components/loading.ftl" as loading/>
<#import "components/vue-loader.ftl" as vueLoader/>
<#import "components/menu.ftl" as menu/>
<#import "components/footer.ftl" as footer/>
<#import "components/upload.ftl" as upload/>

<!DOCTYPE HTML>
<html lang="de">
<head>
    <title>Benutzerdefinierte Seiten - Stephaneum</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="/static/img/favicon.png" />
    <link rel="apple-touch-icon" sizes="196x196" href="/static/img/favicon.png">
    <link rel="stylesheet" type="text/css" href="/static/css/materialize.min.css">
    <link rel="stylesheet" type="text/css" href="/static/css/material-icons.css">
    <link rel="stylesheet" type="text/css" href="/static/css/style.css">
    <style>
        [v-cloak] {
            display: none;
        }
    </style>
</head>

<body>

<@vueLoader.blank/>
<div id="app" v-cloak>
    <nav-menu :menu="info.menu" :has-menu-write-access="info.hasMenuWriteAccess" :user="info.user" :plan="info.plan" :unapproved="info.unapproved"></nav-menu>
    <div v-if="allowed" style="margin: auto; min-height: calc(100vh - 200px); max-width: 1200px">
        <div style="text-align: center; margin: 60px 0 40px 0">
            <i class="material-icons" style="font-size: 4em">note_add</i>
            <h4 style="margin: 0">Benutzerdefinierte Seiten</h4>
        </div>

        <div class="card-panel">
            <div style="display: flex; align-items: center; justify-content: space-evenly; margin-top: 10px; margin-bottom: 40px">
                <div class="grey lighten-5" style="display: flex; align-items: center; border-radius: 20px; padding: 10px 30px 10px 20px">
                    <i class="material-icons" style="font-size: 3rem">info</i>
                    <div style="margin-left: 20px;">
                        <p style="margin: 0">https://stephaneum.de/s/<b>hallo-welt.html</b></p>
                        <p style="margin: 0" class="grey-text">greift auf folgende Datei zu:</p>
                        <p style="margin: 0">{{staticPath}}/<b>hallo-welt.html</b></p>
                        <p style="margin: 0" class="grey-text">Dieser Ordner wird periodisch gescannt. Neue Dateien werden automatisch hinzugefügt.</p>
                    </div>
                </div>

                <div style="text-align: center">
                    <p style="margin: 0;font-size: 3em; font-weight: bold">{{ pages.length }}</p>
                    <p style="margin: 0;font-size: 1.5em">Seiten aktiv</p>
                </div>

                <file-upload url="/api/static/upload" @upload="fetchData" @error="uploadError" v-slot:default="slot">
                    <a @click="slot.upload" class="tooltipped waves-effect waves-light btn-floating green darken-4"
                       data-tooltip="HTML-Datei hochladen" data-position="top" href="#!">
                        <i class="material-icons">arrow_upward</i>
                    </a>
                </file-upload>
            </div>

            <ul v-if="pages.length !== 0" class="collection">
                <li v-for="p in pages" class="collection-item">
                    <div style="display: flex; align-items: center; justify-content: space-between">
                        <span style="display: flex; align-items: center">
                            <i class="material-icons grey-text text-darken-2">description</i>
                            <span style="margin-left: 10px">{{ p.path }}</span>
                            <span class="green-badge-light" style="margin-left: 20px">{{ mode(p.mode) }}</span>
                        </span>

                        <span>
                            <a class="waves-effect waves-light btn blue-grey"
                               @click="toggleMode(p.id)" :class="{ disabled: waitingForData }">
                                <i class="material-icons left">brush</i>Modus ändern
                            </a>
                            <a class="tooltipped waves-effect waves-light btn green darken-3" data-tooltip="Herunterladen" data-position="top" :href="'/s/'+p.path+'?download=true'" style="margin-left: 10px">
                                <i class="material-icons">arrow_downward</i>
                            </a>
                            <a class="tooltipped waves-effect waves-light btn green darken-3" data-tooltip="Öffnen" data-position="top" :href="'/s/'+p.path" target="_blank" style="margin-left: 10px">
                                <i class="material-icons">open_in_new</i>
                            </a>
                            <a class="tooltipped waves-effect waves-light btn red darken-3" data-tooltip="Löschen" data-position="top" style="margin-left: 10px"
                               @click="deletePage(p.id)" :class="{ disabled: waitingForData }">
                                <i class="material-icons">delete</i>
                            </a>
                        </span>
                    </div>
                </li>
            </ul>
            <div v-if="pages.length === 0" style="height: 300px; display: flex; align-items: center; justify-content: center">
                <span class="green-badge-light">Keine Einträge</span>
            </div>
        </div>
        <div style="height: 100px"></div>
    </div>
    <div v-else style="min-height: calc(100vh - 200px)"></div>

    <stephaneum-footer :copyright="info.copyright"></stephaneum-footer>
</div>

<script src="/static/js/materialize.min.js" ></script>
<script src="/static/js/axios.min.js" ></script>
<script src="/static/js/vue.js" ></script>
<@loading.render/>
<@menu.render/>
<@footer.render/>
<@upload.render/>
<script type="text/javascript">

    var amounts = [ 200, 1000, 5000 ];

    var app = new Vue({
        el: '#app',
        data: {
            info: { user: null, menu: null, plan: null, copyright: null, unapproved: null },
            staticPath: null,
            pages: [],
            waitingForData: true,
        },
        methods: {
            toggleMode: async function(id) {
                this.waitingForData = true;
                try {
                    await axios.post('/api/static/toggle-mode/'+id);
                    this.fetchData();
                } catch (e) {
                    M.toast({ html: 'Ein Fehler ist aufgetreten.' });
                    this.waitingForData = false;
                }
            },
            deletePage: async function(id) {
                this.waitingForData = true;
                try {
                    await axios.post('/api/static/delete/'+id);
                    M.toast({ html: 'Gelöscht.' });
                    this.fetchData();
                } catch (e) {
                    M.toast({ html: 'Ein Fehler ist aufgetreten.' });
                    this.waitingForData = false;
                }
            },
            uploadError: function(status) {
                switch (status) {
                    case 409:
                        M.toast({html: 'Nur HTML-Dateien erlaubt.'});
                        break;
                    case 410:
                        M.toast({html: 'Dateinamen vergeben.'});
                        break;
                    default:
                        M.toast({html: 'Ein Fehler ist aufgetreten.'});
                        break;
                }
            },
            fetchData: async function() {
                try {
                    const response = await axios.get('/api/static');
                    this.staticPath = response.data.staticPath;
                    this.pages = response.data.pages;
                    this.waitingForData = false;
                    this.$nextTick(() => M.Tooltip.init(document.querySelectorAll('.tooltipped'), {}));
                } catch (e) {
                    M.toast({ html: 'Ein Fehler ist aufgetreten.' });
                }
            }
        },
        computed: {
            allowed: function() {
                return this.info.user && this.info.user.code.role === 100
            },
            mode: function () {
                return (mode) => {
                    switch(mode) {
                        case 'MIDDLE': return 'eingebettet, mittig';
                        case 'FULL_WIDTH': return 'eingebettet, voll';
                        case 'FULL_SCREEN': return 'original';
                    }
                };
            },
        },
        mounted: async function() {
            try {
                const response = await axios.get('/api/info');
                this.info = response.data;
                await this.fetchData();
                M.AutoInit();
            } catch (e) {
                M.toast({html: 'Interner Fehler.'});
            }
        }
    });
</script>
</body>
</html>