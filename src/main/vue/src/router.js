import Vue from "vue";
import VueRouter from "vue-router";
import Home from "./views/Home.vue";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    name: "home",
    component: Home,
  },
  {
    path: "/login",
    name: "login",
    component: () =>
      import(/* webpackChunkName: "login" */ "./views/Login.vue"),
  },
  {
    path: "/config-manager",
    name: "config-manager",
    component: () =>
        import(/* webpackChunkName: "config-manager" */ "./views/ConfigManager.vue"),
  },
  {
    path: "/code-manager",
    name: "code-manager",
    component: () =>
        import(/* webpackChunkName: "code-manager" */ "./views/CodeManager.vue"),
  },
  {
    path: "/user-manager",
    name: "user-manager",
    component: () =>
        import(/* webpackChunkName: "user-manager" */ "./views/UserManager.vue"),
  },
  {
    path: "/logs",
    name: "logs",
    component: () =>
        import(/* webpackChunkName: "logs" */ "./views/Logs.vue"),
  },
  {
    path: "/account",
    name: "account",
    component: () =>
        import(/* webpackChunkName: "account" */ "./views/Account.vue"),
  },
];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes,
});

export default router;
