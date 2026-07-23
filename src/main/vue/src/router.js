import Vue from "vue";
import VueRouter from "vue-router";
import Index from "./views/Index.vue";
import Login from "./views/Login.vue";
import Imprint from "./views/Imprint.vue";
import Contact from "./views/Contact.vue";
import Sitemap from "./views/Sitemap";
import EuSa from "./views/EuSa";
import HistoryView from "./views/HistoryView";
import PostView from "./views/PostView";
import Section from "./views/Section";
import Home from "./views/internal/Home";
import Account from "./views/internal/Account";
import CodeManager from "./views/internal/CodeManager";
import PlanManager from "./views/internal/PlanManager";
import StaticManager from "./views/internal/StaticManager";
import Cloud from "./views/internal/Cloud";
import Logs from "./views/internal/Logs";
import GroupList from "./views/internal/GroupList";
import StaticView from "./views/StaticView";
import Init from "./views/special/Init";
import Restoring from "./views/special/Restoring";
import Backup from "./views/special/Backup";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    name: "index",
    component: Index,
  },
  {
    path: "/init",
    name: "init",
    component: Init,
  },
  {
    path: "/status/restoring",
    name: "status-restoring",
    component: Restoring,
  },
  {
    path: "/status/backup",
    name: "status-backup",
    component: Backup,
  },
  {
    path: "/m/:id/:page?",
    name: "section",
    component: Section,
  },
  {
    path: "/beitrag/:id",
    name: "post-view",
    component: PostView,
  },
  {
    path: "/statistiken",
    name: "stats",
    component: () => import(/* webpackChunkName: "stats" */ "./views/Stats.vue"),
  },
  {
    path: "/termine",
    name: "events",
    component: () => import(/* webpackChunkName: "events" */ "./views/Events.vue"),
  },
  {
    path: "/geschichte",
    name: "history",
    component: HistoryView,
  },
  {
    path: "/eu-sa",
    name: "eu-sa",
    component: EuSa,
  },
  {
    path: "/s/*",
    name: "static-view",
    component: StaticView,
  },
  {
    path: "/kontakt",
    name: "contact",
    component: Contact,
  },
  {
    path: "/impressum",
    name: "imprint",
    component: Imprint,
  },
  {
    path: "/sitemap",
    name: "sitemap",
    component: Sitemap,
  },
  {
    path: "/login",
    name: "login",
    component: Login,
  },
  {
    path: "/home",
    name: "home",
    component: Home,
  },
  {
    path: "/config-manager",
    name: "config-manager",
    meta: { title: 'Konfiguration - Begabungen leben' },
    component: () => import(/* webpackChunkName: "config-manager" */ "./views/internal/ConfigManager.vue"),
  },
  {
    path: "/code-manager",
    name: "code-manager",
    meta: { title: 'Zugangscodes - Begabungen leben' },
    component: CodeManager,
  },
  {
    path: "/user-manager",
    name: "user-manager",
    meta: { title: 'Nutzerverwaltung - Begabungen leben' },
    component: () => import(/* webpackChunkName: "user-manager" */ "./views/internal/UserManager.vue"),
  },
  {
    path: "/logs",
    name: "logs",
    meta: { title: 'Logdaten - Begabungen leben' },
    component: Logs,
  },
  {
    path: "/menu-manager",
    name: "menu-manager",
    meta: { title: 'Menüverwaltung - Begabungen leben' },
    component: () => import(/* webpackChunkName: "menu-manager" */ "./views/internal/MenuManager.vue"),
  },
  {
    path: "/plan-manager",
    name: "plan-manager",
    meta: { title: 'Vertretungsplan - Begabungen leben' },
    component: PlanManager,
  },
  {
    path: "/static-manager",
    name: "static-manager",
    meta: { title: 'Seiten - Begabungen leben' },
    component: StaticManager,
  },
  {
    path: "/post-manager",
    name: "post-manager",
    meta: { title: 'Beiträge - Begabungen leben' },
    component: () => import(/* webpackChunkName: "post-manager" */ "./views/internal/PostManager.vue"),
  },
  {
    path: "/groups",
    name: "group-list",
    meta: { title: 'Gruppen - Begabungen leben' },
    component: GroupList,
  },
  {
    path: "/groups/:id",
    name: "group-view",
    meta: { title: 'Gruppen - Begabungen leben' },
    component: () => import(/* webpackChunkName: "group-view" */ "./views/internal/GroupView.vue"),
  },
  {
    path: "/cloud",
    name: "cloud",
    meta: { title: 'Cloud - Begabungen leben' },
    component: Cloud,
  },
  {
    path: "/account",
    name: "account",
    meta: { title: 'Account - Begabungen leben' },
    component: Account,
  },
];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  scrollBehavior() {
    return { x: 0, y: 0 };
  },
  routes,
});

router.afterEach((to) => {
  // Use next tick to handle router history correctly
  // see: https://github.com/vuejs/vue-router/issues/914#issuecomment-384477609
  Vue.nextTick(() => {
      document.title = to.meta.title || 'Begabungen leben';
  });
});

export default router;
