import Vue from "vue";
import VueRouter from "vue-router";
import Index from "./views/Index.vue";
import Login from "./views/Login.vue";
import Imprint from "./views/Imprint.vue";
import Contact from "./views/Contact.vue";
import Sitemap from "./views/Sitemap";
import EuSa from "./views/EuSa";
import HistoryView from "./views/HistoryView";
import Events from "./views/Events";
import PostView from "./views/PostView";
import Stats from "./views/Stats";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    name: "index",
    component: Index,
  },
  {
    path: "/beitrag/:id",
    name: "post-view",
    component: PostView,
  },
  {
    path: "/statistiken",
    name: "stats",
    component: Stats,
  },
  {
    path: "/termine",
    name: "events",
    component: Events,
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
    component: () => import(/* webpackChunkName: "home" */ "./views/internal/Home.vue"),
  },
  {
    path: "/config-manager",
    name: "config-manager",
    meta: { title: 'Konfiguration - Stephaneum' },
    component: () => import(/* webpackChunkName: "config-manager" */ "./views/internal/ConfigManager.vue"),
  },
  {
    path: "/code-manager",
    name: "code-manager",
    meta: { title: 'Zugangscodes - Stephaneum' },
    component: () => import(/* webpackChunkName: "code-manager" */ "./views/internal/CodeManager.vue"),
  },
  {
    path: "/user-manager",
    name: "user-manager",
    meta: { title: 'Nutzerverwaltung - Stephaneum' },
    component: () => import(/* webpackChunkName: "user-manager" */ "./views/internal/UserManager.vue"),
  },
  {
    path: "/logs",
    name: "logs",
    meta: { title: 'Logdaten - Stephaneum' },
    component: () => import(/* webpackChunkName: "logs" */ "./views/internal/Logs.vue"),
  },
  {
    path: "/menu-manager",
    name: "menu-manager",
    meta: { title: 'Menüverwaltung - Stephaneum' },
    component: () => import(/* webpackChunkName: "menu-manager" */ "./views/internal/MenuManager.vue"),
  },
  {
    path: "/plan-manager",
    name: "plan-manager",
    meta: { title: 'Vertretungsplan - Stephaneum' },
    component: () => import(/* webpackChunkName: "plan-manager" */ "./views/internal/PlanManager.vue"),
  },
  {
    path: "/static-manager",
    name: "static-manager",
    meta: { title: 'Seiten - Stephaneum' },
    component: () => import(/* webpackChunkName: "static-manager" */ "./views/internal/StaticManager.vue"),
  },
  {
    path: "/post-manager",
    name: "post-manager",
    meta: { title: 'Beiträge - Stephaneum' },
    component: () => import(/* webpackChunkName: "post-manager" */ "./views/internal/PostManager.vue"),
  },
  {
    path: "/groups",
    name: "group-list",
    meta: { title: 'Gruppen - Stephaneum' },
    component: () => import(/* webpackChunkName: "group-list" */ "./views/internal/GroupList.vue"),
  },
  {
    path: "/groups/:id",
    name: "group-view",
    meta: { title: 'Gruppen - Stephaneum' },
    component: () => import(/* webpackChunkName: "group-view" */ "./views/internal/GroupView.vue"),
  },
  {
    path: "/cloud",
    name: "cloud",
    meta: { title: 'Cloud - Stephaneum' },
    component: () => import(/* webpackChunkName: "cloud" */ "./views/internal/Cloud.vue"),
  },
  {
    path: "/account",
    name: "account",
    meta: { title: 'Account - Stephaneum' },
    component: () => import(/* webpackChunkName: "account" */ "./views/internal/Account.vue"),
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
      document.title = to.meta.title || 'Stephaneum';
  });
});

export default router;
