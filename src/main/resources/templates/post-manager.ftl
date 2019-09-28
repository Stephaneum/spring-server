
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
    <link rel="icon" type="image/png" href="<@spring.url '/static/img/favicon.png' />" />
    <link rel="apple-touch-icon" sizes="196x196" href="<@spring.url '/static/img/favicon.png' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/materialize.min.css' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/material-icons.css' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/style.css' />">
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
    </style>
</head>

<body>

<@vueLoader.blank/>
<div id="app">
    <nav-menu :menu="menu" :user="user"></nav-menu>

    <div id="main-row" class="row" style="min-height: 100vh; margin-top: 50px">
        <div class="col s10 offset-s2">
            <h4>{{ mode.description }}</h4>
        </div>
        <div class="col s2">
            <mode-btn v-for="m in modes" v-bind:key="m.id" :mode="m" :active="m.id === mode.id" @click="setMode(m)"></mode-btn>
        </div>
        <div class="col s10">
            <div v-show="mode.id === modes.create.id" class="white z-depth-1">
                CREATE
            </div>
            <div v-show="mode.id === modes.edit.id" class="white z-depth-1">
                EDIT
            </div>
            <div v-show="mode.id === modes.approve.id" class="white z-depth-1">
                APPROVE
            </div>
            <div v-show="mode.id === modes.special.id" class="white z-depth-1">
                SPECIAL
            </div>
        </div>
    </div>

    <stephaneum-footer :copyright="copyright"></stephaneum-footer>
</div>

<template id="template-mode-btn">
    <div @click="$emit('click')" class="mode-btn z-depth-1" :class="{ 'mode-btn-active': active }">
        <i style="font-size: 3em" class="material-icons">{{ mode.icon }}</i>
        <span style="font-size: 1.5em">{{ mode.name }}</span>
    </div>
</template>

<script src="/static/js/materialize.min.js" ></script>
<script src="/static/js/axios.min.js" ></script>
<script src="/static/js/vue.js" ></script>
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

    Vue.component('mode-btn', {
        props: ['mode', 'active'],
        template: '#template-mode-btn'
    });

    var app = new Vue({
        el: '#app',
        data: {
            menu: [],
            user: null,
            copyright: null,
            modes: modes,
            mode: modes.create,
        },
        methods: {
            setMode: function(mode) {
                this.mode = mode;
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