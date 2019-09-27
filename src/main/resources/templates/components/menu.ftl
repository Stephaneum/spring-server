<#macro render>
    <style>
        #internal {
            background-color: #558b2f;
		}
			
		#internal:hover {
		    background-color: #689f38;
		}

        /* css hacks to create dropdown menu */

        ul li {
            position: relative;
        }

        li ul {
            display: none;
        }

        li:hover > ul {
            display: block;
            position: absolute;
        }

        li:hover li {
            float: none;
        }

        ul ul ul {
            left: 100%;
            top: 0;
        }

        ul:before, ul:after {
            content: " ";
            display: table;
        }

        ul:after {
            clear: both;
        }

        /* hover one item in dropdown */
        li:hover li a:hover {
            background: #e0e0e0;
        }

        /* all dropdowns */
        li:hover li a {
            background: white;
            white-space: nowrap;
            height: 35px;
            line-height: 35px;
            color: #1b5e20;
            display: flex;
            justify-content: space-between;
            align-items: center;
            min-width: 150px;
        }

        /* all icons in dropdowns */
        li:hover li a i {
            display: inline-block;
        }

        /* left icon in dropdowns */
        li:hover li a span i {
            display: inline-block;
            font-size: 0.8em;
            margin-right: 5px
        }

        /* bugfix height */
        li:hover li a span {
            height: 35px;
            display: flex;
            align-items: center;
        }
    </style>
    <script type="text/javascript">
        Vue.component('nav-menu', {
            props: ['menu'],
            data: function () {
                return {
                    count: 0
                }
            },
            computed: {
                url: function () {
                    return (menu) => menu.link ? menu.link : 'home.xhtml?id='+menu.id;
                },
                target: function () {
                    return (menu) => menu.link ? '_blank' : '_self';
                }
            },
            template: `
                <div>
                    <nav>
                        <div class="nav-wrapper" style="z-index: 100; background-color: white">
                            <a href="#" data-target="side-nav" class="sidenav-trigger hide-on-large-only">
                                <i class="material-icons" style="color: #1b5e20">menu</i>
                            </a>
                            <a href="./home.xhtml" class="brand-logo">
                                <img src="/static/img/logo-banner-green.png" style="height:50px;margin-top:5px;margin-left:10px"/>
                            </a>
                            <ul class="right hide-on-med-and-down">
                                <li v-for="m1 in menu">
                                    <a v-text="m1.name" :href="url(m1)" :target="target(m1)"  style="color: #1b5e20"></a>
                                    <ul v-if="m1.children.length != 0" class="z-depth-1" style="z-index: 200">
                                        <li v-for="m2 in m1.children">
                                            <a :href="url(m2)" :target="target(m2)">
                                                <span>
                                                    <i v-if="m2.link" class="material-icons">arrow_upward</i>
                                                    <i v-else class="material-icons">stop</i>
                                                    {{m2.name}}
                                                </span>
                                                <i v-if="m2.children.length != 0" class="material-icons">keyboard_arrow_right</i>
                                            </a>
                                            <ul v-if="m2.children.length != 0" class="z-depth-1" style="z-index: 300">
                                                <li v-for="m3 in m2.children">
                                                    <a :href="url(m3)" :target="target(m3)">
                                                        <span>
                                                            <i v-if="m3.link" class="material-icons">arrow_upward</i>
                                                            <i v-else class="material-icons">stop</i>
                                                            {{m3.name}}
                                                        </span>
                                                        <i v-if="m3.children.length != 0" class="material-icons">keyboard_arrow_right</i>
                                                    </a>
                                                    <ul v-if="m3.children.length != 0" class="z-depth-1" style="z-index: 400">
                                                        <li v-for="m4 in m3.children">
                                                            <a :href="url(m4)" :target="target(m4)">
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
                                <li>
                                    <a id="internal">Intern</a>
                                </li>
                                <li>
                                    <a class="waves-effect waves-dark btn" style="background-color: #1b5e20">
                                        Abmelden
                                        <i class="material-icons right">exit_to_app</i>
                                    </a>
                                </li>
                            </ul>
                        </div>
                    </nav>
                </div>`
        });
    </script>
</#macro>