<template id="nav-menu">
    <div style="width: 100%">
        <nav>
            <div class="nav-wrapper" :style="{ 'z-index': unreal ? 'auto' : 100 }" style="background-color: #163B63">
                <a href="#" data-target="sidenav" class="sidenav-trigger hide-on-large-only">
                    <i class="material-icons" style="color: #FFFFFF">menu</i>
                </a>
                <router-link v-if="!editRootLevel" :to="home" v-slot="{ href, navigate }">
                    <a @click="navigate" :href="href" class="brand-logo" style="display:flex;align-items:center" :style="unreal ? { 'opacity': 0.05 } : {}">
                        <img src="../assets/img/logo-beg.webp" style="height:50px;margin-top:5px;margin-left:10px"/>
                        <span style="margin-left:10px;color:#FFFFFF;font-size:1.5rem;font-weight:500">Begabungenleben.de</span>
                    </a>
                </router-link>
                <ul class="right hide-on-med-and-down">
                    <li v-for="m1 in menu" :key="m1.id">
                        <router-link v-if="!unreal && !m1.link" :to="menuUrl(m1)" v-slot="{ href, navigate }">
                            <a @click="navigate" :href="href" style="color: #FFFFFF">
                                {{ m1.name }}
                            </a>
                        </router-link>
                        <a v-else v-text="m1.name" @click="emit(m1)" :href="link(m1)" target="_blank" style="color: #FFFFFF">{{ m1.name }}</a>
                        <MenuItem :parent="m1" :edit-mode="editMode" :unreal="unreal" @select="emit" @group="emitNewGroup" @link="emitNewLink"></MenuItem>
                    </li>
                    <li v-if="!unreal && loggedIn">
                        <a id="internal-btn">Intern</a>
                        <ul id="internal-menu" class="z-depth-1" style="z-index: 200">
                            <!-- admin -->
                            <li v-if="admin">
                                <router-link to="/config-manager" v-slot="{ href, navigate }">
                                    <a @click="navigate" :href="href"><span><i class="material-icons">build</i>Konfiguration</span></a>
                                </router-link>
                            </li>
                            <li v-if="admin">
                                <router-link to="/code-manager" v-slot="{ href, navigate }">
                                    <a @click="navigate" :href="href"><span><i class="material-icons">vpn_key</i>Zugangscodes</span></a>
                                </router-link>
                            </li>
                            <li v-if="admin">
                                <router-link to="/user-manager" v-slot="{ href, navigate }">
                                    <a @click="navigate" :href="href"><span><i class="material-icons">people</i>Nutzer</span></a>
                                </router-link>
                            </li>
                            <li v-if="admin">
                                <router-link to="/logs" v-slot="{ href, navigate }">
                                    <a @click="navigate" :href="href"><span><i class="material-icons">history</i>Logdaten</span></a>
                                </router-link>
                            </li>
                            <li class="internal-divider"></li>

                            <!-- cms -->
                            <li v-if="hasMenuWriteAccess">
                                <router-link to="/menu-manager" v-slot="{ href, navigate }">
                                    <a @click="navigate" :href="href"><span><i class="material-icons">device_hub</i>Menü</span></a>
                                </router-link>
                            </li>
<!--                            <li v-if="admin || managePlans">-->
<!--                                <router-link to="/plan-manager" v-slot="{ href, navigate }">-->
<!--                                    <a @click="navigate" :href="href"><span><i class="material-icons">description</i>Vertretungsplan</span></a>-->
<!--                                </router-link>-->
<!--                            </li>-->
                            <li v-if="admin">
                                <router-link to="/static-manager" v-slot="{ href, navigate }">
                                    <a @click="navigate" :href="href"><span><i class="material-icons">note_add</i>Seiten</span></a>
                                </router-link>
                            </li>
                            <li>
                                <router-link to="/post-manager" v-slot="{ href, navigate }">
                                    <a @click="navigate" :href="href"><span><i class="material-icons">edit</i>Beiträge{{ unapproved ? ' ('+unapproved+')' : null}}</span></a>
                                </router-link>
                            </li>
                            <li class="internal-divider"></li>

                            <!-- internal -->
                            <li>
                                <router-link to="/groups" v-slot="{ href, navigate }">
                                    <a @click="navigate" :href="href"><span><i class="material-icons">people</i>Gruppen</span></a>
                                </router-link>
                            </li>
                            <li>
                                <router-link to="/cloud" v-slot="{ href, navigate }">
                                    <a @click="navigate" :href="href"><span><i class="material-icons">folder</i>Dateien</span></a>
                                </router-link>
                            </li>
                            <li>
                                <router-link to="/account" v-slot="{ href, navigate }">
                                    <a @click="navigate" :href="href"><span><i class="material-icons">account_circle</i>Account</span></a>
                                </router-link>
                            </li>
                        </ul>
                        <div id="internal-menu-account" style="position: absolute; z-index: 1; top: 70px; left: -190px; display: none; overflow: hidden;text-align: center; width: 200px; height: 160px; background-color: #EAF6FC; color: #163B63; line-height: normal;" class="z-depth-1">
                            <i style="font-size: 4em; margin-top: 10px" class="material-icons">person</i>
                            <p style="white-space: nowrap">{{ user != null && user.firstName }} {{ user != null && user.lastName }}</p>
                            <p style="white-space: nowrap">({{ role }})</p>
                        </div>
                    </li>
                    <template v-if="editMode && editRootLevel">
                        <li style="margin-left: 10px; margin-right: 10px">
                            <a @click="emitNewGroup(null)" class="edit-btn" style="border: none">
                                <i class="material-icons left" style="margin-right: 5px">add</i>
                                Gruppe
                            </a>
                        </li>
                        <li>
                            <a @click="emitNewLink(null)" class="edit-btn" style="border: none">
                                <i class="material-icons left" style="margin-right: 5px">add</i>
                                Link
                            </a>
                        </li>
                    </template>
                    <li>
                        <a class="waves-effect waves-light btn" style="background-color: #2FA8CC" :style="unreal ? {'opacity': 0.1 } : {}" @click="toggleAuth">
                            {{ loggedIn && !unreal ? 'Abmelden' : 'Login' }}
                            <i class="material-icons right">exit_to_app</i>
                        </a>
                    </li>
                </ul>
            </div>
        </nav>
        <ul v-if="!unreal" id="sidenav" class="sidenav">
            <br />
            <li>
                <a href="/"><i class="material-icons">home</i>Startseite</a>
            </li>
            <li>
                <a :href="loggedIn ? 'logout' : 'login'"><i class="material-icons">exit_to_app</i>{{ loggedIn ? 'Abmelden' : 'Login' }}</a>
            </li>
            <li>
                <a href="/termine"><i class="material-icons">date_range</i>Termine</a>
            </li>

            <li v-if="plan && plan.exists"><div class="divider"></div></li>
            <li v-if="plan && plan.exists">
                <a class="subheader">Vertretungsplan</a>
            </li>
            <li v-if="plan && plan.exists">
                <a href="/vertretungsplan.pdf"><i class="material-icons">description</i>{{ plan.info }}</a>
            </li>

            <li><div class="divider"></div></li>
            <li><a class="subheader">weitere Links</a></li>
            <li v-for="m in menu" :key="m.id">
                <router-link v-if="!m.link" :to="menuUrl(m)" v-slot="{ href, navigate }">
                    <a @click="navigate" :href="href">
                        {{ m.name }}
                    </a>
                </router-link>
                <a v-else :href="link(m)" target="_blank">{{ m.name }}</a>
            </li>
        </ul>
    </div>
</template>


<script>
import Axios from "axios";
import M from "materialize-css";
import { showLoading, hideLoading } from '../helper/utils';
import MenuItem from "./MenuItem";

export default {
    name: 'Menu',
    components: {MenuItem},
    props: ['menu', 'user', 'hasMenuWriteAccess', 'plan', 'unapproved', 'unreal', 'editMode', 'editRootLevel'],
    methods: {
            emit: function(menu) {
                this.$emit('select', menu);
            },
            emitNewGroup: function(parent) {
                this.$emit('group', parent);
            },
            emitNewLink: function(parent) {
                this.$emit('link', parent);
            },
            toggleAuth: async function() {

                if(this.unreal)
                    return;

                if(this.loggedIn) {
                    showLoading("Abmelden...");
                    await Axios.post('/api/logout');
                    await this.$emit('update-info');
                    await this.$router.push('/').catch(() => {});
                    hideLoading();
                    M.toast({html: 'Abgemeldet.'});
                } else {
                    await this.$router.push('/login');
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
            home: function() {
                if(this.unreal)
                    return '#!';
                return this.loggedIn ? '/home' : '/';
            },
            admin: function () {
                return this.user && this.user.code.role === 100;
            },
            managePlans: function () {
                return this.user && this.user.managePlans;
            },
            menuUrl: function () {
                return (menu) => '/m/'+menu.id;
            },
            link: function() {
                return (menu) => this.unreal ? null : menu.link
            }
        },
        mounted: function() {
            if(!this.unreal) {
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
        }
}
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
        color: #163B63;
        display: flex;
        justify-content: space-between;
        align-items: center;
        overflow: hidden; /* bugfix hover icons */
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
        background-color: #1E4D7B;
    }

    #internal-btn:hover {
        background-color: #2B93BE;
    }

    #internal-menu > li > a {
        background-color: #E8F4FA;
        padding-right: 20px;
    }

    #internal-menu > li > a:hover {
        background-color: #A9D9EA;
    }

    #internal-menu > li > a > span > i {
        font-size: 1.2em;
    }

    #internal-btn:hover ~ #internal-menu-account, #internal-menu:hover ~ #internal-menu-account {
        display: block !important;
    }

    .internal-divider {
        height: 1px;
        background-color: #2FA8CC;
    }

    /* edit mode */

    .edit-btn {
        background-color: #1E4D7B !important;
        color: white !important;
        border: white solid 2px;
    }

    .edit-btn:hover {
        background-color: #2B93BE !important;
    }

    /* bugfix height */
    nav li:hover li a span {
        height: 35px;
        display: flex;
        align-items: center;
    }
</style>