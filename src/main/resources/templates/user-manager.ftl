<#import "/spring.ftl" as spring/>
<#import "components/loading.ftl" as loading/>
<#import "components/vue-loader.ftl" as vueLoader/>
<#import "components/menu.ftl" as menu/>
<#import "components/footer.ftl" as footer/>
<#import "components/utils.ftl" as utils/>
<#import "components/managers/user-batch-actions.ftl" as userBatchActions/>
<#import "components/managers/user-search-and-manage.ftl" as userSearchAndManage/>
<#import "components/managers/user-import.ftl" as userImport/>

<!DOCTYPE HTML>
<html lang="de">
<head>
    <title>Nutzerverwaltung - Stephaneum</title>
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
    <div v-if="allowed" style="margin: 50px auto 0 auto; max-width: 1200px; min-height: calc(100vh - 350px)">

        <div style="text-align: center; margin: 60px 0 60px 0">
            <i class="material-icons" style="font-size: 4em">people</i>
            <h4 style="margin: 0">Nutzerverwaltung</h4>
        </div>

        <user-batch-actions></user-batch-actions>

        <user-search-and-manage></user-search-and-manage>

        <user-import></user-import>

    </div>
    <div v-else style="flex: 1; min-height: calc(100vh - 100px); display: flex; flex-direction: column; align-items: center; justify-content: center"></div>

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
<@userBatchActions.render/>
<@userSearchAndManage.render/>
<@userImport.render/>
<script type="text/javascript">
    var app = new Vue({
        el: '#app',
        data: {
            info: { user: null, hasMenuWriteAccess: false, menu: null, plan: null, copyright: null, unapproved: null },
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

                hideLoading();
            }
        },
        computed: {
            allowed: function() {
                return this.info.user && this.info.user.code.role === 100;
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