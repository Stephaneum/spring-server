<#import "/spring.ftl" as spring/>
<#import "components/loading.ftl" as loading/>
<#import "components/vue-loader.ftl" as vueLoader/>
<#import "components/menu.ftl" as menu/>
<#import "components/footer.ftl" as footer/>
<#import "components/utils.ftl" as utils/>

<!DOCTYPE HTML>
<html lang="de">
<head>
    <title>Gruppen - Stephaneum</title>
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
    <nav-menu :menu="info.menu" :user="info.user" :plan="info.plan" :unapproved="info.unapproved"></nav-menu>
    <div v-if="allowed" style="margin: 50px auto 0 auto; max-width: 1200px; min-height: calc(100vh - 350px)">

        <div style="text-align: center; margin: 60px 0 40px 0">
            <i class="material-icons" style="font-size: 4em">people</i>
            <h4 style="margin: 0">Gruppen</h4>
        </div>

        <div class="card-panel">
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px">
                <span style="font-size: 2em">Meine Gruppen</span>
                <a @click="onDelete(f)" style="margin-right: 20px" class="tooltipped waves-effect waves-light btn-floating green darken-4"
                   data-tooltip="Neue Gruppe" data-position="top" href="#!">
                    <i class="material-icons">add</i>
                </a>
            </div>

            <div style="display: flex; flex-wrap: wrap; justify-content: space-evenly; margin-bottom: 30px">
                <a v-for="g in groups" :href="'/groups/' + g.id" class="group-rect card white-text">
                    <i class="material-icons" style="font-size: 50px">people</i>
                    <span style="font-weight: bold; font-size: 20px; text-align: center; line-height: 20px; margin-bottom: 5px">{{ g.name }}</span>
                    <span>{{ g.leader.firstName }} {{ g.leader.lastName }}</span>
                </a>
            </div>

            <div v-if="admin">
                <span style="font-size: 2em;">Alle Gruppen</span>
                <ul class="collection" style="margin: 20px 0 0 0">
                    <li v-for="g in allGroups" class="collection-item">
                        <div style="display: flex; align-items: center;">
                        <span style="flex-grow: 1; display: flex; align-items: center; ">
                            <span class="group-link" @click="select(f)">{{ g.name }}</span>
                        </span>

                            <span style="flex: 0 0 320px; text-align: right;">
                            <span class="green-badge-light">{{ g.members }} Mitglieder</span>
                            <span style="margin-left: 20px" class="green-badge-light">{{ g.leader.firstName }} {{ g.leader.lastName }}</span>
                        </span>

                            <span style="flex: 0 0 150px; text-align: right">
                            <a @click="onEdit(f)" class="tooltipped waves-effect waves-light teal darken-2 btn margin-1" href="#!" data-tooltip="Chat" data-position="bottom">
                                <i class="material-icons">chat</i>
                            </a>

                            <a @click="onDelete(f)" class="tooltipped waves-effect waves-light btn red darken-4 margin-1" href="#!" data-tooltip="Löschen" data-position="bottom">
                                <i class="material-icons">delete</i>
                            </a>
                        </span>
                        </div>
                    </li>
                </ul>
            </div>
        </div>

    </div>
    <div v-else style="flex: 1; min-height: calc(100vh - 100px)"></div>

    <div style="height: 100px"></div>

    <stephaneum-footer :copyright="info.copyright"></stephaneum-footer>
</div>

<script src="/static/js/moment.min.js" ></script>
<script src="/static/js/moment.de.js" ></script>
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
            info: { user: null, menu: null, plan: null, copyright: null, unapproved: null },
            groups: [],
            allGroups: []
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

                const groups = await axios.get('/api/groups');
                this.groups = groups.data;

                if(this.admin) {
                    const allGroups = await axios.get('/api/groups/all');
                    this.allGroups = allGroups.data;
                }

                hideLoading();
                this.$nextTick(() => M.Tooltip.init(document.querySelectorAll('.tooltipped'), {}));
            }
        },
        computed: {
            allowed: function() {
                return this.info.user && this.info.user.code.role >= 0;
            },
            admin: function() {
                return this.info.user && this.info.user.code.role == 100;
            },
        },
        mounted: function() {
            M.AutoInit();
            moment.locale('de');
            this.fetchData();
        }
    });
</script>
</body>
</html>