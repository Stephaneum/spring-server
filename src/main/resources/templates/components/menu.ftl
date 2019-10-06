<#macro render>
    <template id="nav-menu">
        <div style="width: 100%">
            <nav>
                <div class="nav-wrapper" :style="{ 'z-index': minimal ? 'auto' : 100 }" style="background-color: white">
                    <a href="#" data-target="sidenav" class="sidenav-trigger hide-on-large-only">
                        <i class="material-icons" style="color: #1b5e20">menu</i>
                    </a>
                    <a :href="minimal ? '#!' : './home.xhtml'" class="brand-logo" :style="minimal ? { 'opacity': 0.05 } : {}">
                        <img src="/static/img/logo-banner-green.png" style="height:50px;margin-top:5px;margin-left:10px"/>
                    </a>
                    <ul class="right hide-on-med-and-down">
                        <li v-for="m1 in menu">
                            <a v-text="m1.name" @click="emit(m1)" :href="url(m1)" :target="target(m1)"  style="color: #1b5e20"></a>
                            <ul v-if="m1.children.length != 0" class="z-depth-1" style="z-index: 200">
                                <li v-for="m2 in m1.children">
                                    <a @click="emit(m2)" :href="url(m2)" :target="target(m2)">
                                        <span>
                                            <i v-if="m2.link" class="material-icons">arrow_upward</i>
                                            <i v-else class="material-icons">stop</i>
                                            {{m2.name}}
                                        </span>
                                        <i v-if="m2.children.length != 0" class="material-icons">keyboard_arrow_right</i>
                                    </a>
                                    <ul v-if="m2.children.length != 0" class="z-depth-1" style="z-index: 300">
                                        <li v-for="m3 in m2.children">
                                            <a @click="emit(m3)" :href="url(m3)" :target="target(m3)">
                                                <span>
                                                    <i v-if="m3.link" class="material-icons">arrow_upward</i>
                                                    <i v-else class="material-icons">stop</i>
                                                    {{m3.name}}
                                                </span>
                                                <i v-if="m3.children.length != 0" class="material-icons">keyboard_arrow_right</i>
                                            </a>
                                            <ul v-if="m3.children.length != 0" class="z-depth-1" style="z-index: 400">
                                                <li v-for="m4 in m3.children">
                                                    <a @click="emit(m4)" :href="url(m4)" :target="target(m4)">
                                                        <span>
                                                            <i v-if="m4.link" class="material-icons">arrow_upward</i>
                                                            <i v-else class="material-icons">stop</i>
                                                            {{m4.name}}
                                                        </span>
                                                    </a>
                                                </li>
                                            </ul>
                                        </li>
                                    </ul>
                                </li>
                            </ul>
                        </li>
                        <li v-if="!minimal && loggedIn">
                            <a id="internal-btn">Intern</a>
                            <ul id="internal-menu" class="z-depth-1" style="z-index: 200">
                                <li v-if="admin"><a href="admin_konfig.xhtml"><span><i class="material-icons">build</i>Konfiguration</span></a></li>
                                <li v-if="admin"><a href="admin_static.xhtml"><span><i class="material-icons">brush</i>Benutzerdefinierte Seiten</span></a></li>
                                <li v-if="admin"><a href="admin_rubriken.xhtml"><span><i class="material-icons">bookmark</i>Rubriken</span></a></li>
                                <li v-if="admin"><a href="admin_backup.xhtml"><span><i class="material-icons">save</i>Backup</span></a></li>
                                <li v-if="admin"><a href="admin_zugangscode.xhtml"><span><i class="material-icons">vpn_key</i>Zugangscodes</span></a></li>
                                <li v-if="admin"><a href="admin_nutzer.xhtml"><span><i class="material-icons">people</i>Nutzer</span></a></li>
                                <li v-if="admin"><a href="admin_logs.xhtml"><span><i class="material-icons">history</i>Logbuch</span></a></li>
                                <li class="internal-divider"></li>
                                <li v-if="admin || managePlans"><a href="konfig_vertretung.xhtml"><span><i class="material-icons">description</i>Vertretungsplan</span></a></li>
                                <li v-if="createCategories"><a href="nutzer_rubrik.xhtml"><span><i class="material-icons">bookmark</i>Rubrik</span></a></li>
                                <li><a href="beitrag-manager"><span><i class="material-icons">edit</i>Beiträge{{ unapproved ? ' ('+unapproved+')' : null}}</span></a></li>
                                <li><a href="klasse.xhtml"><span><i class="material-icons">school</i>Schulklasse</span></a></li>
                                <li><a href="projekt_all.xhtml"><span><i class="material-icons">flag</i>Projekte</span></a></li>
                                <li class="internal-divider"></li>
                                <li><a href="nutzer_dateien.xhtml"><span><i class="material-icons">folder</i>Dateien</span></a></li>
                                <li><a href="nutzer_account.xhtml"><span><i class="material-icons">account_circle</i>Account</span></a></li>
                            </ul>
                            <div id="internal-menu-account" style="position: absolute; z-index: 1; top: 70px; left: -190px; display: none; overflow: hidden;text-align: center; width: 200px; height: 160px; background-color: #f1f8e9; color: #1b5e20; line-height: normal;" class="z-depth-1">
                                <i style="font-size: 4em; margin-top: 10px" class="material-icons">person</i>
                                <p style="white-space: nowrap">{{ user != null && user.firstName }} {{ user != null && user.lastName }}</p>
                                <p style="white-space: nowrap">({{ role }})</p>
                            </div>
                        </li>
                        <li>
                            <a class="waves-effect waves-dark btn" style="background-color: #1b5e20" :style="minimal ? {'opacity': 0.1 } : {}" @click="toggleAuth">
                                {{ loggedIn && !minimal ? 'Abmelden' : 'Login' }}
                                <i class="material-icons right">exit_to_app</i>
                            </a>
                        </li>
                    </ul>
                </div>
            </nav>
            <ul v-if="!minimal" id="sidenav" class="sidenav">
                <br />
                <li>
                    <a href="home.xhtml"><i class="material-icons">home</i>Startseite</a>
                </li>
                <li>
                    <a :href="loggedIn ? 'logout' : 'login'"><i class="material-icons">exit_to_app</i>{{ loggedIn ? 'Abmelden' : 'Login' }}</a>
                </li>
                <li>
                    <a href="termine.xhtml"><i class="material-icons">date_range</i>Termine</a>
                </li>

                <li v-if="plan && plan.exists"><div class="divider"></div></li>
                <li v-if="plan && plan.exists">
                    <a class="subheader">Vertretungsplan</a>
                </li>
                <li v-if="plan && plan.exists">
                    <a href="vertretungsplan.pdf"><i class="material-icons">description</i>{{ plan.info }}</a>
                </li>

                <li><div class="divider"></div></li>
                <li><a class="subheader">weitere Links</a></li>
                <li v-for="m in menu"><a :href="url(m)" :target="target(m)">{{ m.name }}</a></li>
            </ul>
        </div>
    </template>

    <script type="text/javascript">
        Vue.component('nav-menu', {
            props: ['menu', 'user', 'plan', 'unapproved', 'minimal'],
            methods: {
                emit: function(menu) {
                    this.$emit('selected', menu);
                },
                toggleAuth: function() {
                    if(this.loggedIn) {
                        showLoading("Abmelden...");
                        axios.post('./api/logout')
                            .then((response) => {
                                window.location = '/logout.xhtml'; // continue with jsf
                            });
                    } else {
                        window.location = 'login';
                    }
                }
            },
            computed: {
                loggedIn: function () {
                    return this.user && this.user.code.role >= 0;
                },
                role: function () {
                    if(this.user) {
                        switch(this.user.code.role) {
                            case 0: return "Schüler/in";
                            case 1: return "Lehrer/in";
                            case 2: return "Gast";
                            case 100: return "Admin";
                        }
                    }
                    return "?"
                },
                admin: function () {
                    return this.user && this.user.code.role === 100;
                },
                managePlans: function () {
                    return this.user && this.user.managePlans;
                },
                createCategories: function () {
                    return this.user && this.user.createCategories;
                },
                url: function () {
                    return (menu) => this.minimal ? null : menu.link ? menu.link : 'home.xhtml?id='+menu.id;
                },
                target: function () {
                    return (menu) => this.minimal ? null : menu.link ? '_blank' : '_self';
                }
            },
            mounted: function() {
                if(!this.minimal) {
                    var callback = function(){
                        // Handler when the DOM is fully loaded
                        M.Sidenav.init(document.querySelectorAll('.sidenav'), {});
                        console.log('menu init');
                    };

                    if (
                        document.readyState === "complete" ||
                        (document.readyState !== "loading" && !document.documentElement.doScroll)
                    ) {
                        callback();
                    } else {
                        document.addEventListener("DOMContentLoaded", callback);
                    }
                }
            },
            template: '#nav-menu'
        });
    </script>

    <style>

        /* css hacks to create dropdown menu */

        nav ul li {
            position: relative;
        }

        nav li ul {
            display: none;
        }

        nav li:hover > ul {
            display: block;
            position: absolute;
        }

        nav li:hover li {
            float: none;
        }

        nav ul ul ul {
            left: 100%;
            top: 0;
        }

        nav ul:before, ul:after {
            content: " ";
            display: table;
        }

        nav ul:after {
            clear: both;
        }

        /* all dropdowns */
        nav li:hover li a {
            background: white;
            white-space: nowrap;
            padding-right: 10px;
            height: 35px;
            line-height: 35px;
            color: #1b5e20;
            display: flex;
            justify-content: space-between;
            align-items: center;
            min-width: 150px;
        }

        /* hover one item in dropdown */
        nav li li:hover > a {
            background-color: #e0e0e0;
        }

        /* all icons in dropdowns */
        nav li:hover li a i {
            display: inline-block;
        }

        /* left icon in dropdowns */
        nav li:hover li a span i {
            display: inline-block;
            font-size: 0.8em;
            margin-right: 5px
        }

        /* internal */
        #internal-btn {
            background-color: #558b2f;
        }

        #internal-btn:hover {
            background-color: #689f38;
        }

        #internal-menu > li > a {
            background-color: #e8f5e9;
        }

        #internal-menu > li > a:hover {
            background-color: #a5d6a7;
        }

        #internal-menu > li > a > span > i {
            font-size: 1.2em;
        }

        #internal-btn:hover ~ #internal-menu-account, #internal-menu:hover ~ #internal-menu-account {
            display: block !important;
        }

        .internal-divider {
            height: 1px;
            background-color: #4caf50;
        }

        /* bugfix height */
        nav li:hover li a span {
            height: 35px;
            display: flex;
            align-items: center;
        }
    </style>
</#macro>