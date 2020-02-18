<#-- @ftlvariable name="title" type="java.lang.String" -->

<#import "/spring.ftl" as spring/>
<#import "components/loading.ftl" as loading/>
<#import "components/vue-loader.ftl" as vueLoader/>
<#import "components/menu.ftl" as menu/>
<#import "components/footer.ftl" as footer/>

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
    <nav-menu :menu="info.menu" :user="info.user" :plan="info.plan" :unapproved="info.unapproved"></nav-menu>
    <div v-if="allowed" style="flex: 1; display: flex; align-items: center; justify-content: center; min-height: calc(100vh - 100px)">
    </div>
    <div v-else style="flex: 1; min-height: calc(100vh - 100px)"></div>

    <stephaneum-footer :copyright="info.copyright"></stephaneum-footer>
</div>

<script src="/static/js/materialize.min.js" ></script>
<script src="/static/js/axios.min.js" ></script>
<script src="/static/js/vue.js" ></script>
<@loading.render/>
<@menu.render/>
<@footer.render/>
<script type="text/javascript">
    var app = new Vue({
        el: '#app',
        data: {
            info: { user: null, menu: null, plan: null, copyright: null, unapproved: null }
        },
        methods: {
            fetchData: function() {
                axios.get('./api/info')
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
                return this.info.user && (this.info.user.code.role >= 10)
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