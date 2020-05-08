<#-- @ftlvariable name="title" type="java.lang.String" -->

<#import "/spring.ftl" as spring/>
<#import "components/loading.ftl" as loading/>
<#import "components/vue-loader.ftl" as vueLoader/>
<#import "components/menu.ftl" as menu/>
<#import "components/footer.ftl" as footer/>
<#import "components/utils.ftl" as utils/>
<#import "components/cloud/cloud-view.ftl" as cloudView/>

<!DOCTYPE HTML>
<html lang="de">
<head>
    <title>Nutzerspeicher - Stephaneum</title>
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
    <div v-if="allowed" id="main-row" style="margin: 50px auto 0 auto;">

        <div class="row">
            <div class="col s10 offset-s2">
                <h4 style="margin: 20px 0 20px 0">Deine persönliche Cloud</h4>
            </div>
        </div>

        <cloud-view :my-id="info.user.id" :shared-mode="false" :modify-all="true" root-url="/api/cloud/view/user" upload-url="/api/cloud/upload/user" folder-url="/api/cloud/create-folder/user" :teacherchat="hasTeacherChat"></cloud-view>
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
<@cloudView.render/>
<script type="text/javascript">
    var app = new Vue({
        el: '#app',
        data: {
            info: { user: null, menu: null, plan: null, copyright: null, unapproved: null }
        },
        methods: {
            fetchData: function() {
                axios.get('/api/info')
                    .then((res) => {
                        if(res.data) {
                            this.info = res.data;
                        } else {
                            M.toast({html: 'Interner Fehler.'});
                        }
                    });
            }
        },
        computed: {
            allowed: function() {
                return this.info.user && this.info.user.code.role >= 0;
            },
            hasTeacherChat: function() {
                return this.allowed && (this.info.user.code.role === 1 || this.info.user.code.role === 100);
            }
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