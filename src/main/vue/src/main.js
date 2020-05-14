import Vue from 'vue'
import jQuery from 'jquery'
import moment from 'moment'
import App from './App.vue'
import router from './router'
import 'materialize-css/dist/css/materialize.min.css'
import 'material-icons/iconfont/material-icons.css'

Vue.config.productionTip = false;
moment.locale('de');
window.jQuery = window.$ = jQuery;

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
