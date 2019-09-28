
<#import "/spring.ftl" as spring/>
<#import "components/vue-loader.ftl" as vueLoader/>
<#import "components/menu.ftl" as menu/>
<#import "components/footer.ftl" as footer/>

<!DOCTYPE HTML>
<html lang="de">
<head>
    <title>Beiträge - Stephaneum</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="/static/img/favicon.png" />
    <link rel="apple-touch-icon" sizes="196x196" href="/static/img/favicon.png">
    <link rel="stylesheet" type="text/css" href="/static/css/materialize.min.css">
    <link rel="stylesheet" type="text/css" href="/static/css/material-icons.css">
    <link rel="stylesheet" type="text/css" href="/static/css/style.css">
    <link rel="stylesheet" type="text/css" href="/static/trumbowyg/ui/trumbowyg.min.css">
    <link rel="stylesheet" type="text/css" href="/static/trumbowyg/plugins/table/ui/trumbowyg.table.min.css">
    <link rel="stylesheet" type="text/css" href="/static/trumbowyg/plugins/colors/ui/trumbowyg.colors.min.css">
    <style>
        [v-cloak] {
            display: none;
        }

        .mode-btn {
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

        .mode-btn:hover {
            background-color: #2e7d32;
            cursor: pointer;
        }

        .mode-btn-active {
            background-color: #43a047 !important;
        }

        .tab-btn {
            display: inline-block;
            vertical-align: bottom;
            padding: 5px 15px 5px 10px;
            margin: 0 5px 0 5px;
            border-radius: 10px 10px 0 0;
            background-color: #558b2f;
            height: 40px;
            color: white;
            font-size: 1.2em;
        }

        .tab-btn:hover {
            background-color: #689f38;
            cursor: pointer;
        }

        .tab-btn-active {
            background-color: #43a047 !important;
            height: 45px !important;
        }

        .tab-panel {
            padding: 20px;
            z-index: 100;
        }
    </style>
</head>

<body>

<@vueLoader.blank/>
<div id="app">
    <nav-menu :menu="menu" :user="user"></nav-menu>

    <div v-if="currMode" id="main-row" class="row" style="min-height: 100vh; margin-top: 50px">
        <div class="col s10 offset-s2">
            <h4 style="margin: 20px 0 40px 0">{{ currMode.description }}</h4>
        </div>
        <div class="col s10 offset-s2">
            <div v-for="(t, index) in currTabs" @click="setTab(t)" class="tab-btn" :class="{ 'tab-btn-active': t.id === currTab.id }">
                <span style="display: inline-flex; align-items: center; justify-content: center; margin: 0 5px 0 5px;font-size: 0.7em; background-color: #1b5e20; border-radius: 50%; width: 25px; height: 25px">{{index+1}}</span>
                <span style="vertical-align: middle">{{ t.name }}</span>
            </div>
        </div>
        <div class="col s2">
            <div v-for="m in modes" @click="setMode(m)" class="mode-btn z-depth-1" :class="{ 'mode-btn-active': m.id === currMode.id }">
                <i style="font-size: 3em" class="material-icons">{{ m.icon }}</i>
                <span style="font-size: 1.5em">{{ m.name }}</span>
            </div>
        </div>
        <div class="col s10">
            <div v-show="currTab.id === tabs.select.id " class="tab-panel white z-depth-1">
                AUSWAHL
            </div>
            <div v-show="currTab.id === tabs.text.id " class="tab-panel white z-depth-1">
                <div class="input-field" style="max-width: 600px">
                    <label for="post-title">Titel</label>
                    <input v-model:value="currPost.title" type="text" id="post-title" style="font-weight: bold"/>
                </div>
                <div id="post-text-editor" style="height: 600px"></div>
            </div>
            <div v-show="currTab.id === tabs.images.id " class="tab-panel white z-depth-1">
                BILDER
            </div>
            <div v-show="currTab.id === tabs.layout.id " class="tab-panel white z-depth-1">
                LAYOUT
            </div>
            <div v-show="currTab.id === tabs.assign.id " class="tab-panel white z-depth-1">
                ZUORDNUNG
            </div>
            <div v-show="currTab.id === tabs.finalize.id " class="tab-panel white z-depth-1">
                FERTIGSTELLUNG
            </div>
        </div>
    </div>
    <div v-else style="min-height: 100vh;">
    </div>

    <div style="height: 100px"></div>

    <stephaneum-footer :copyright="copyright"></stephaneum-footer>
</div>

<script src="/static/js/jquery.min.js" ></script>
<script src="/static/js/materialize.min.js" ></script>
<script src="/static/js/axios.min.js" ></script>
<script src="/static/js/vue.js" ></script>
<script src="/static/trumbowyg/trumbowyg.min.js" ></script>
<script src="/static/trumbowyg/plugins/table/trumbowyg.table.min.js"></script>
<script src="/static/trumbowyg/plugins/colors/trumbowyg.colors.min.js"></script>
<script src="/static/trumbowyg/plugins/fontSize/trumbowyg.fontsize.min.js"></script>
<script src="/static/trumbowyg/langs/de.min.js" ></script>
<@menu.render/>
<@footer.render/>
<script type="text/javascript">
    M.AutoInit();

    var modes = {
        create: {
            id: 0,
            name: "Erstellen",
            description: "Beitrag erstellen",
            icon: "add"
        },
        edit: {
            id: 1,
            name: "Bearbeiten",
            description: "Beitrag bearbeiten / löschen",
            icon: "edit"
        },
        approve: {
            id: 2,
            name: "Genehmigen",
            description: "Beitrag genehmigen",
            icon: "check"
        },
        special: {
            id: 3,
            name: "Spezielle Texte",
            description: "Spezielle Texte",
            icon: "build"
        }
    };

    var tabs = {
        select: {
            id: -1,
            name: "Auswahl"
        },
        text: {
            id: 0,
            name: "Titel und Text"
        },
        images: {
            id: 1,
            name: "Bilder"
        },
        layout: {
            id: 2,
            name: "Layout"
        },
        assign: {
            id: 3,
            name: "Zuordnung"
        },
        finalize: {
            id: 4,
            name: "Fertigstellung"
        }
    };

    var app = new Vue({
        el: '#app',
        data: {
            menu: [],
            user: null,
            copyright: null,
            initialized: false,
            modes: modes,
            tabs: tabs,
            currMode: null,
            currTabs: [],
            currTab: null,
            currPost: {
                title: null,
                text: null,
                images: [],
                layoutPost: 0,
                layoutPreview: 0,
                preview: 200
            }
        },
        methods: {
            setMode: function(mode) {
                switch(mode.id) {
                    case modes.create.id:
                        this.currTabs = [tabs.text, tabs.images, tabs.layout, tabs.assign, tabs.finalize];
                        this.currTab = tabs.text;
                        break;
                    case modes.edit.id:
                        this.currTabs = [tabs.select];
                        this.currTab = tabs.select;
                        break;
                    case modes.approve.id:
                        this.currTabs = [tabs.select];
                        this.currTab = tabs.select;
                        break;
                    case modes.special.id:
                        this.currTabs = [tabs.select];
                        this.currTab = tabs.select;
                        break;
                }
                this.currMode = mode;
                if(!this.initialized) {
                    this.$nextTick(() => {
                        M.AutoInit();
                        $('#post-text-editor').trumbowyg({
                            semantic: false,
                            lang: 'de',
                            btns: [
                                ['foreColor', 'backColor'],
                                ['strong', 'em', 'underline', 'del'],
                                ['formatting', 'fontsize'],
                                ['superscript', 'subscript'],
                                ['link'],
                                ['table'],
                                ['justifyLeft', 'justifyCenter', 'justifyRight', 'justifyFull'],
                                ['orderedList'],
                                ['horizontalRule'],
                                ['removeformat'],
                                ['viewHTML'],
                                ['fullscreen']],
                            plugins: {
                                fontsize: {
                                    sizeList: [
                                        '10pt',
                                        '11pt',
                                        '12pt',
                                        '14pt',
                                        '16pt',
                                        '18pt',
                                        '24pt'
                                    ],
                                    allowCustomSize: false
                                }
                            }
                        });
                        console.log('post init finished')
                    });
                }
            },
            setTab: function(tab) {
                if(this.currTab.id === tabs.text.id)
                    this.currPost.text = $('#post-text-editor').trumbowyg('html');
                this.currTab = tab;
            }
        },
        mounted: function () {
            this.$nextTick(() => {
                axios.get('./api/menu')
                    .then((response) => {
                        if(response.data) {
                            this.menu = response.data;
                        } else {
                            M.toast({html: 'Interner Fehler.'});
                        }
                    });

                axios.get('./api/info')
                    .then((response) => {
                        if(response.data) {
                            this.user = response.data.user;
                            this.copyright = response.data.copyright;
                            if(this.user) {
                                this.setMode(modes.create);
                            }
                        } else {
                            M.toast({html: 'Interner Fehler.'});
                        }
                    });
            })
        }
    });
</script>
</body>
</html>