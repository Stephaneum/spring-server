<#-- @ftlvariable name="title" type="java.lang.String" -->

<#import "/spring.ftl" as spring/>
<#import "components/loading.ftl" as loading/>
<#import "components/vue-loader.ftl" as vueLoader/>
<#import "components/menu.ftl" as menu/>
<#import "components/footer.ftl" as footer/>

<!DOCTYPE HTML>
<html lang="de">
<head>
    <title>Vertretungsplan - Stephaneum</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="<@spring.url '/static/img/favicon.png' />" />
    <link rel="apple-touch-icon" sizes="196x196" href="<@spring.url '/static/img/favicon.png' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/materialize.min.css' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/material-icons.css' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/style.css' />">
    <style>
        .info {
            margin-top: 20px;
            font-style: italic;
        }

        [v-cloak] {
            display: none;
        }
    </style>
</head>

<body>

<@vueLoader.blank/>
<div id="app" style="display: flex; align-items: center; flex-direction: column; min-height: 100vh" v-cloak>
    <nav-menu :menu="info.menu" :user="info.user" :plan="info.plan" :unapproved="info.unapproved"></nav-menu>
    <div v-if="allowed" style="flex: 1; display: flex; align-items: center; justify-content: center">
        <div style="width: 800px">
            <h5>Vertretungsplan</h5>
            <div class="card-panel">
                <h5>PDF-Datei:</h5>
                <h5>Zusatzinformation:</h5>
            </div>
        </div>
    </div>
    <div v-else style="flex: 1"></div>

    <stephaneum-footer :copyright="info.copyright"></stephaneum-footer>
</div>

<script src="/static/js/materialize.min.js" ></script>
<script src="/static/js/axios.min.js" ></script>
<script src="/static/js/vue.js" ></script>
<@loading.render/>
<@menu.render/>
<@footer.render/>
<script type="text/javascript">
    M.AutoInit();

    var app = new Vue({
        el: '#app',
        data: {
            info: { user: null, menu: null, plan: null, copyright: null, unapproved: null },
        },
        methods: {

        },
        computed: {
            allowed: function() {
                return this.info.user && (this.info.user.code.role === 100 || this.info.user.managePlans)
            }
        },
        mounted: function() {
            axios.get('./api/info')
                .then((res) => {
                    if(res.data) {
                        this.info = res.data;
                    } else {
                        M.toast({html: 'Interner Fehler.'});
                    }
                });
        }
    });
</script>
</body>
</html>