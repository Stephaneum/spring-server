<#macro render>
    <style>
        #internal {
            background-color: #558b2f;
		}
			
		#internal:hover {
		    background-color: #689f38;
		}

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

        li:hover a { background: white; white-space: nowrap; }
        li:hover li a:hover { background: #e0e0e0; }
    </style>
    <script type="text/javascript">
        Vue.component('nav-menu', {
            props: ['menu'],
            data: function () {
                return {
                    count: 0
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
                                    <a v-text="m1.name" style="color: #1b5e20"></a>
                                    <ul v-if="m1.children.length != 0">
                                        <li v-for="m2 in m1.children">
                                            <a v-text="m2.name" style="color: #1b5e20"></a>
                                            <ul v-if="m2.children.length != 0">
                                                <li v-for="m3 in m2.children">
                                                    <a v-text="m3.name" style="color: #1b5e20"></a>
                                                    <ul v-if="m3.children.length != 0">
                                                        <li v-for="m4 in m3.children">
                                                            <a v-text="m4.name" style="color: #1b5e20"></a>
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