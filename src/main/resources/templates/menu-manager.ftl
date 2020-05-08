<#import "/spring.ftl" as spring/>
<#import "components/loading.ftl" as loading/>
<#import "components/vue-loader.ftl" as vueLoader/>
<#import "components/menu.ftl" as menu/>
<#import "components/footer.ftl" as footer/>
<#import "components/utils.ftl" as utils/>

<!DOCTYPE HTML>
<html lang="de">
<head>
    <title>Menü - Stephaneum</title>
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

        .group-rect {
            flex-basis: calc(25% - 30px);
            background-color: #1b5e20;
            cursor: pointer;
            padding: 15px 5px 15px 5px;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
        }

        .group-rect:hover {
            background-color: #388e3c !important;
        }

        @media screen and (min-width: 901px) and (max-width: 1100px) {
            .group-rect {
                flex-basis: calc(33.3% - 30px);
            }
        }

        @media screen and (min-width: 601px) and (max-width: 900px) {
            .group-rect {
                flex-basis: calc(50% - 30px);
            }
        }

        @media screen and (max-width: 600px) {
            .group-rect {
                flex-basis: 100%;
            }
        }

        .group-link {
            color: black;
        }

        .group-link:hover {
            color: #2e7d32;
            text-decoration: underline;
            cursor: pointer;
        }
    </style>
</head>

<body>

<@vueLoader.blank/>
<div id="app" v-cloak>
    <nav-menu :menu="info.menu" :has-menu-write-access="info.hasMenuWriteAccess" :user="info.user" :plan="info.plan" :unapproved="info.unapproved"></nav-menu>
    <div v-if="allowed" style="margin: 50px auto 0 auto; max-width: 1200px; min-height: calc(100vh - 350px)">

        <div style="text-align: center; margin: 60px 0 60px 0">
            <i class="material-icons" style="font-size: 4em">list</i>
            <h4 style="margin: 0">Menü konfigurieren</h4>
        </div>

        <nav-menu :menu="menu" unreal="true" edit-mode="true" :edit-root-level="menuAdmin" @selected="selectMenu"></nav-menu>

        <div class="card-panel" style="margin-top: 60px">
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px">
                <span style="font-size: 2em">Schreibrechte</span>
                <a @click="showCreateRule" style="margin-right: 20px" class="tooltipped waves-effect waves-light btn-floating green darken-4"
                   data-tooltip="Neue Regel" data-position="top" href="#!">
                    <i class="material-icons">add</i>
                </a>
            </div>

            <p style="margin: 0">Nutzer können in diesen Menüpunkten (inkl. Untermenüs) <b>Beiträge</b> erstellen, bearbeiten und genehmigen - sowie das <b>Menü</b> selber bearbeiten.</p>
            <p style="margin: 0">Regeln sind <b>kombinierbar</b>, z.B. kann Nutzer A für die Menüpunkte M1 und M2 Schreibrechte zugewiesen werden.</p>
            <br>

            <ul v-if="rules.length !== 0" class="collection">
                <li v-for="r in rules" class="collection-item">
                    <div style="display: flex; align-items: center; justify-content: space-between">
                        <span style="display: flex; align-items: center">
                            <i class="material-icons grey-text text-darken-2">person</i>
                            <span style="margin-left: 10px">{{ r.user.firstName }} {{ r.user.lastName }} ({{ r.user.code.roleString }})</span>
                        </span>

                        <span>
                            <span v-if="r.menu">
                                <span style="font-size: 1.2rem; color: grey">{{ menuPath(r.menu) }}</span>
                                <span style="font-size: 1.2rem; font-weight: bold">{{ r.menu.name }}</span>
                            </span>
                            <span v-else style="font-size: 1.4rem; font-weight: bold">
                                ALLES
                            </span>
                        </span>

                        <span>
                            <a class="tooltipped waves-effect waves-light btn green darken-3" data-tooltip="Regel bearbeiten" data-position="top">
                                <i class="material-icons">edit</i>
                            </a>
                            <a class="tooltipped waves-effect waves-light btn red darken-3" data-tooltip="Regel löschen" data-position="top" style="margin-left: 10px"
                               @click="deletePage(p.id)">
                                <i class="material-icons">delete</i>
                            </a>
                        </span>
                    </div>
                </li>
            </ul>
        </div>

    </div>
    <div v-else style="flex: 1; min-height: calc(100vh - 100px)"></div>

    <div style="height: 100px"></div>

    <stephaneum-footer :copyright="info.copyright"></stephaneum-footer>
</div>

<script src="/static/js/materialize.min.js" ></script>
<script src="/static/js/axios.min.js" ></script>
<script src="/static/js/vue.js" ></script>
<@utils.render/>
<@loading.render/>
<@menu.render/>
<@footer.render/>
<script type="text/javascript">
    var app = new Vue({
        el: '#app',
        data: {
            info: { user: null, hasMenuWriteAccess: false, menu: null, plan: null, copyright: null, unapproved: null },
            menu: [],
            menuAdmin: false,
            rules: [],
            selected: {},
        },
        methods: {
            fetchData: async function() {
                const info = await axios.get('/api/info');
                if(info.data) {
                    this.info = info.data;
                } else {
                    M.toast({html: 'Interner Fehler.'});
                    return;
                }

                const menu = await axios.get('/api/menu/writable');
                this.menu = menu.data.menu;
                this.menuAdmin = menu.data.menuAdmin;

                if(this.admin) {
                    const rules = await axios.get('/api/menu/rules');
                    this.rules = rules.data;
                }

                hideLoading();
                this.$nextTick(() => M.Tooltip.init(document.querySelectorAll('.tooltipped'), {}));
            },
            selectMenu: function(menu) {

            },
            showCreateRule: function(menu) {

            }
        },
        computed: {
            allowed: function() {
                return this.info.hasMenuWriteAccess;
            },
            admin: function() {
                return this.info.user && this.info.user.code.role === 100;
            },
            menuPath: function() {
                return (menu) => {
                    let s = '';
                    let curr = menu;
                    while (curr.parent) {
                        curr = curr.parent;
                        s = curr.name + ' / ' + s;
                    }
                    return s;
                }
            }
        },
        mounted: function() {
            M.AutoInit();
            this.fetchData();
        }
    });
</script>
</body>
</html>