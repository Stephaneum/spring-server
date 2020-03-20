<#import "/spring.ftl" as spring/>
<#import "components/loading.ftl" as loading/>
<#import "components/vue-loader.ftl" as vueLoader/>
<#import "components/menu.ftl" as menu/>
<#import "components/footer.ftl" as footer/>
<#import "components/utils.ftl" as utils/>
<#import "components/groups/chat-view.ftl" as chatView/>
<#import "components/groups/member-list.ftl" as memberList/>
<#import "components/cloud/cloud-view.ftl" as cloudView/>

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
    </style>
</head>

<body>

<@vueLoader.blank/>
<div id="app" v-cloak>
    <nav-menu :menu="info.menu" :user="info.user" :plan="info.plan" :unapproved="info.unapproved"></nav-menu>
    <div v-if="group" style="margin: 50px auto 0 auto; max-width: 1600px; min-height: calc(100vh - 350px)">

        <div class="row">
            <div class="col s10 offset-s2">
                <h4 style="margin: 20px 0 20px 0">{{ group.name }}</h4>
            </div>
        </div>

        <div class="row">
            <div class="col s2">
                <div @click="showCreateFolder" class="action-btn z-depth-1">
                    <i style="font-size: 3em" class="material-icons">add</i>
                    <span style="font-size: 1.5em">Chatraum</span>
                </div>

                <div @click="showCreateFolder" class="action-btn z-depth-1">
                    <i style="font-size: 3em" class="material-icons">settings</i>
                    <span style="font-size: 1.5em">Optionen</span>
                </div>

                <div @click="showCreateFolder" class="action-btn z-depth-1">
                    <i style="font-size: 3em" class="material-icons">delete</i>
                    <span style="font-size: 1.5em">Löschen</span>
                </div>
            </div>

            <div class="col s10" style="display: flex; min-height: 500px">
                <div style="flex: 1; padding-right: 10px">
                    <chat-view></chat-view>
                </div>

                <div style="flex: 0 0 400px; padding-left: 10px">
                    <member-list :members="group.members" :leader="group.leader" :teachers="group.teachers"></member-list>
                </div>
            </div>
        </div>

        <div class="row" style="margin-top: 50px">
            <div class="col s10 offset-s2">
                <h4 style="margin: 20px 0 20px 0">Gemeinsame Cloud</h4>
            </div>
        </div>

        <cloud-view :my-id="info.user.id" shared-mode="true" :modify-all="modifyAll" :root-url="'/api/cloud/view/group/' + group.id" :upload-url="'/api/cloud/upload/group/' + group.id" :folder-url="'/api/cloud/create-folder/group/' + group.id" :teacherchat="hasTeacherChat"></cloud-view>

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
<@chatView.render/>
<@memberList.render/>
<@cloudView.render/>
<script type="text/javascript">
    var app = new Vue({
        el: '#app',
        data: {
            info: { user: null, menu: null, plan: null, copyright: null, unapproved: null },
            group: null
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

                const id = window.location.pathname.split('/').pop();
                const group = await axios.get('/api/groups/' + id);
                this.group = group.data;

                hideLoading();
                this.$nextTick(() => M.Tooltip.init(document.querySelectorAll('.tooltipped'), {}));
            }
        },
        computed: {
            allowed: function() {
                return this.info.user && this.info.user.code.role >= 0;
            },
            admin: function() {
                return this.info.user && this.info.user.code.role === 100;
            },
            hasTeacherChat: function() {
                return this.allowed && (this.info.user.code.role === 1 || this.info.user.code.role === 100);
            },
            modifyAll: function() {
                return this.admin || // admin
                    (this.group && this.group.leader && this.group.leader.id === this.info.user.id) || // group-admin
                    (this.group && this.group.teachers && this.group.teachers.some((t) => t.id === this.info.user.id)); // group-teacher
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