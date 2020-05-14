import Vue from "vue";
import VueRouter from "vue-router";
import Home from "./views/Home.vue";
import Login from "./views/Login.vue";
import Imprint from "./views/Imprint.vue";
import Contact from "./views/Contact.vue";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    name: "home",
    component: Home,
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
    path: "/login",
    name: "login",
    component: Login,
  },
  {
    path: "/config-manager",
    name: "config-manager",
    meta: { title: 'Konfiguration - Stephaneum' },
    component: () =>
        import(/* webpackChunkName: "config-manager" */ "./views/ConfigManager.vue"),
  },
  {
    path: "/code-manager",
    name: "code-manager",
    meta: { title: 'Zugangscodes - Stephaneum' },
    component: () =>
        import(/* webpackChunkName: "code-manager" */ "./views/CodeManager.vue"),
  },
  {
    path: "/user-manager",
    name: "user-manager",
    meta: { title: 'Nutzerverwaltung - Stephaneum' },
    component: () =>
        import(/* webpackChunkName: "user-manager" */ "./views/UserManager.vue"),
  },
  {
    path: "/logs",
    name: "logs",
    meta: { title: 'Logdaten - Stephaneum' },
    component: () =>
        import(/* webpackChunkName: "logs" */ "./views/Logs.vue"),
  },
  {
    path: "/menu-manager",
    name: "menu-manager",
    meta: { title: 'Menüverwaltung - Stephaneum' },
    component: () =>
        import(/* webpackChunkName: "menu-manager" */ "./views/MenuManager.vue"),
  },
  {
    path: "/plan-manager",
    name: "plan-manager",
    meta: { title: 'Vertretungsplan - Stephaneum' },
    component: () =>
        import(/* webpackChunkName: "plan-manager" */ "./views/PlanManager.vue"),
  },
  {
    path: "/static-manager",
    name: "static-manager",
    meta: { title: 'Seiten - Stephaneum' },
    component: () =>
        import(/* webpackChunkName: "static-manager" */ "./views/StaticManager.vue"),
  },
  {
    path: "/post-manager",
    name: "post-manager",
    meta: { title: 'Beiträge - Stephaneum' },
    component: () =>
        import(/* webpackChunkName: "post-manager" */ "./views/PostManager.vue"),
  },
  {
    path: "/groups",
    name: "group-list",
    meta: { title: 'Gruppen - Stephaneum' },
    component: () =>
        import(/* webpackChunkName: "group-list" */ "./views/GroupList.vue"),
  },
  {
    path: "/groups/:id",
    name: "group-view",
    meta: { title: 'Gruppen - Stephaneum' },
    component: () =>
        import(/* webpackChunkName: "group-view" */ "./views/GroupView.vue"),
  },
  {
    path: "/cloud",
    name: "cloud",
    meta: { title: 'Cloud - Stephaneum' },
    component: () =>
        import(/* webpackChunkName: "cloud" */ "./views/Cloud.vue"),
  },
  {
    path: "/account",
    name: "account",
    meta: { title: 'Account - Stephaneum' },
    component: () =>
        import(/* webpackChunkName: "account" */ "./views/Account.vue"),
  },
];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
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
