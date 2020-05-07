<#-- @ftlvariable name="title" type="java.lang.String" -->

<#import "/spring.ftl" as spring/>
<#import "components/loading.ftl" as loading/>
<#import "components/vue-loader.ftl" as vueLoader/>
<#import "components/menu.ftl" as menu/>
<#import "components/footer.ftl" as footer/>

<!DOCTYPE HTML>
<html lang="de">
<head>
    <title>Logdaten - Stephaneum</title>
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

        .type-span {
            flex: 0 0 150px;
            text-align: right;
            padding-right: 20px;
        }

        .info-span {
            flex: 1;
            white-space: nowrap;
            overflow-x: hidden;
            text-overflow: ellipsis;
        }

        .date-span {
            flex: 0 0 250px;
            text-align: right;
            color: #808080;
        }

        .auth-badge {
            background-color: #bbdefb;
        }

        .file-badge {
            background-color: #c8e6c9;
        }

        .post-badge {
            background-color: #ffe0b2;
        }

        .group-badge {
            background-color: #e1bee7;
        }

        .menu-badge {
            background-color: #cfd8dc;
        }

        .other-badge {
            background-color: lightgrey;
        }
    </style>
</head>

<body>

<@vueLoader.blank/>
<div id="app" v-cloak>
    <nav-menu :menu="info.menu" :user="info.user" :plan="info.plan" :unapproved="info.unapproved"></nav-menu>
    <div v-if="allowed" style="margin: auto; min-height: calc(100vh - 200px); max-width: 1200px">
        <div style="text-align: center; margin: 60px 0 40px 0">
            <i class="material-icons" style="font-size: 4em">history</i>
            <h4 style="margin: 0">Logdaten</h4>
        </div>

        <div class="card-panel">
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px">
                <div>
                    <span style="font-size: 2em">{{ logInfo.amount }} Einträge</span>
                    <br>
                    <span style="font-style: italic">{{ logs.length }} davon angezeigt</span>
                </div>
                <div class="input-field" style="margin: 0">
                    <i class="material-icons prefix">search</i>
                    <label for="search-input">Suche</label>
                    <input v-model:value="search" id="search-input" name="password" type="text" :disabled="waitingForData">
                </div>
                <div>
                    <span>Limit:</span>
                    <a v-for="a in amounts" class="waves-effect waves-light btn blue-grey" style="margin-left: 10px"
                       @click="setAmount(a)" :class="{ disabled: waitingForData, 'darken-3': a !== currAmount }">
                        {{ a }}
                    </a>
                </div>
            </div>

            <ul v-if="logs.length !== 0" class="collection">
                <li v-for="l in logs" class="collection-item">
                    <div style="display: flex; align-items: center;">
                        <span class="type-span">
                            <span class="green-badge-light" :class="l.className">{{ l.type }}</span>
                        </span>
                        <span class="info-span">{{ l.info }}</span>
                        <span class="date-span">
                            {{ l.date }}
                        </span>
                    </div>
                </li>
            </ul>
            <div v-if="logs.length === 0" style="height: 300px; display: flex; align-items: center; justify-content: center">
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
<script type="text/javascript">

    var amounts = [ 200, 1000, 5000 ];

    var app = new Vue({
        el: '#app',
        data: {
            info: { user: null, menu: null, plan: null, copyright: null, unapproved: null },
            amounts: amounts,
            logInfo: { amount: null },
            logsRaw: [],
            logsRawLowerCase: [], // the same like logsRaw but all string are in lower case
            logs: [],
            logsLowerCase: [],
            search: null,
            currAmount: 200,
            waitingForData: true,
            firstFetch: true,
        },
        methods: {
            setAmount: function(amount) {
                this.waitingForData = true;
                this.currAmount = amount;
                this.search = null;
                this.fetchData();
            },
            fetchData: function() {
                axios.get('/api/logs/info')
                    .then((res) => {
                        this.logInfo = res.data;
                        axios.get('/api/logs/'+this.currAmount)
                            .then((res) => {
                                if(Array.isArray(res.data)) {
                                    this.logsRaw = res.data;
                                    this.logs = [ ...res.data ];
                                    this.logsRawLowerCase = res.data.map(d => {
                                        return {
                                            id: d.id,
                                            type: d.type.toLowerCase(),
                                            info: d.info.toLowerCase(),
                                            date: d.date.toLowerCase()
                                        }
                                    });
                                    this.logsLowerCase = [ ...this.logsRawLowerCase ];
                                    this.waitingForData = false;
                                    if(!this.firstFetch)
                                        M.toast({ html: 'Logdaten geladen.<br>'+this.logs.length+' Einträge' });
                                    this.firstFetch = false;
                                }
                            });
                    });
            }
        },
        computed: {
            allowed: function() {
                return this.info.user && this.info.user.code.role === 100
            }
        },
        watch: {
            search: function(newVal, oldVal) {
                if(!newVal) {
                    this.logs = [ ...this.logsRaw ];
                    this.logsLowerCase = [ ...this.logsRawLowerCase ];
                } else {
                    // if the next search key word is just a new letter, use the last results as basis
                    var source = newVal.substring(0, newVal.length - 1) === oldVal ? this.logsLowerCase : this.logsRawLowerCase;
                    var keyword = newVal.toLowerCase();
                    this.logsLowerCase = source.filter(l => {
                        return l.type.indexOf(keyword) !== -1 || l.info.indexOf(keyword) !== -1 || l.date.indexOf(keyword) !== -1
                    });
                    this.logs = this.logsLowerCase.map(l => this.logsRaw.find(f => f.id === l.id));
                }
            }
        },
        mounted: function() {
            M.AutoInit();
            axios.get('/api/info')
                .then((res) => {
                    if(res.data) {
                        this.info = res.data;
                    } else {
                        M.toast({html: 'Interner Fehler.'});
                    }
                });
            this.fetchData();
        }
    });
</script>
</body>
</html>