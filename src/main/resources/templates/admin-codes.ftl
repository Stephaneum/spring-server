<#-- @ftlvariable name="title" type="java.lang.String" -->

<#import "/spring.ftl" as spring/>
<#import "components/loading.ftl" as loading/>
<#import "components/vue-loader.ftl" as vueLoader/>
<#import "components/menu.ftl" as menu/>
<#import "components/footer.ftl" as footer/>

<!DOCTYPE HTML>
<html lang="de">
<head>
    <title>Zugangscodes - Stephaneum</title>
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

        .code-panel {
            flex-basis: 350px;
        }
    </style>
</head>

<body>

<@vueLoader.blank/>
<div id="app" v-cloak>
    <nav-menu :menu="info.menu" :user="info.user" :plan="info.plan" :unapproved="info.unapproved"></nav-menu>
    <div v-if="allowed" style="margin: auto; min-height: calc(100vh - 200px); max-width: 1200px">
        <div style="text-align: center; margin: 60px 0 40px 0">
            <i class="material-icons" style="font-size: 4em">vpn_key</i>
            <h4 style="margin: 0">Zugangscodes</h4>
        </div>

        <div style="display: flex; justify-content: space-between">
            <div v-for="r in roles" class="card-panel code-panel">
                <h5 style="text-align: center; margin: 0 0 30px 0">{{ r.name }} ({{ r.codes.length }})</h5>
                <ul v-if="r.codes.length !== 0" class="collection">
                    <li v-for="c in r.codes" class="collection-item">
                        <div style="display: flex; align-items: center; justify-content: space-between">
                            <span>
                                <span>{{ c.code }}</span>
                                <input type="hidden" :id="'code-field-'+c.id" :value="c.code">
                            </span>

                            <span>
                                <a class="waves-effect waves-light tooltipped blue-grey darken-3 btn" style="margin-left: 10px"
                                   @click="copy(c.id, c.code)" data-tooltip="Kopieren" data-position="bottom">
                                    <i class="material-icons">save</i>
                                </a>
                                <a class="waves-effect waves-light tooltipped red darken-3 btn" style="margin-left: 10px"
                                   @click="doDelete(c.id)" data-tooltip="Löschen" data-position="bottom">
                                    <i class="material-icons">delete</i>
                                </a>
                            </span>
                        </div>
                    </li>
                </ul>
                <div style="text-align: center">
                    <a @click="create(r.id)" class="waves-effect waves-light teal darken-3 btn" style="margin: 20px 0 20px 0">
                        <i class="material-icons left">add</i>
                        Neuer Code
                    </a>
                </div>
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

    var roles = {
        student: {
            id: 0,
            name: 'Schüler',
            codes: []
        },
        teacher: {
            id: 1,
            name: 'Lehrer',
            codes: []
        },
        guest: {
            id: 2,
            name: 'Gast',
            codes: []
        }
    };

    var app = new Vue({
        el: '#app',
        data: {
            info: { user: null, menu: null, plan: null, copyright: null, unapproved: null },
            roles: roles,
            codes: []
        },
        methods: {
            create: function(role) {
                showLoadingInvisible();
                axios.post('/api/codes/add/'+role)
                    .then((res) => {
                        if(res.data.success) {
                            M.toast({ html: 'Code generiert.' });
                            this.fetchData();
                        } else if(res.data.message) {
                            M.toast({ html: res.data.message });
                            hideLoading();
                        } else {
                            M.toast({ html: 'Ein Fehler ist aufgetreten.' });
                            hideLoading();
                        }
                    });
            },
            copy: function(id, code) {
                let input = document.getElementById('code-field-'+id);
                input.setAttribute('type', 'text');
                input.select();

                document.execCommand('copy');
                M.toast({ html: 'Kopiert:<br>'+code });

                // unselect the range
                input.setAttribute('type', 'hidden');
                window.getSelection().removeAllRanges();
            },
            doDelete: function(id) {
                showLoadingInvisible();
                axios.post('/api/codes/delete/'+id)
                    .then((res) => {
                        if(res.data.success) {
                            M.toast({ html: 'Gelöscht.' });
                            this.fetchData();
                        } else if(res.data.message) {
                            M.toast({ html: res.data.message });
                            hideLoading();
                        } else {
                            M.toast({ html: 'Ein Fehler ist aufgetreten.' });
                            hideLoading();
                        }
                    });
            },
            fetchData: function() {
                axios.get('/api/codes')
                    .then((res) => {
                        if(Array.isArray(res.data)) {
                            this.codes = res.data;
                            this.roles.student.codes = res.data.filter(d => d.role === this.roles.student.id);
                            this.roles.teacher.codes = res.data.filter(d => d.role === this.roles.teacher.id);
                            this.roles.guest.codes = res.data.filter(d => d.role === this.roles.guest.id);
                            this.$nextTick(() => {
                                this.$nextTick(() => M.Tooltip.init(document.querySelectorAll('.tooltipped'), {}));
                            });
                            hideLoading();
                        }
                    });
            }
        },
        computed: {
            allowed: function() {
                return this.info.user && this.info.user.code.role === 100
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