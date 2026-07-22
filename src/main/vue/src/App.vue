<template>
  <div id="app">
    <Menu v-if="!fetched || info.state === 'OK'" @update-info="fetchData" :menu="info.menu" :has-menu-write-access="info.hasMenuWriteAccess" :user="info.user" :plan="info.plan" :unapproved="info.unapproved"></Menu>
    <InternetExplorerNotice></InternetExplorerNotice>
    <div style="min-height: calc(100vh - 150px)">
      <router-view :info="info" @update-info="fetchData"/>
    </div>
    <div style="height: 50px"></div>
    <PrivacyPopup></PrivacyPopup>
    <Footer v-if="!fetched || info.state === 'OK'" :copyright="info.copyright"></Footer>
  </div>
</template>

<script>
// @ is an alias to /src
import Axios from "axios"
import M from "materialize-css"
import Menu from '@/components/Menu.vue'
import Footer from '@/components/Footer.vue'
import InternetExplorerNotice from "./components/InternetExplorerNotice";
import PrivacyPopup from "./components/PrivacyPopup";

export default {
  name: 'Home',
  components: {
    PrivacyPopup,
    InternetExplorerNotice,
    Menu,
    Footer
  },
  data: () => ({
    fetched: false,
    info: { user: null, hasMenuWriteAccess: false, menu: null, plan: null, copyright: null, unapproved: null }
  }),
  methods: {
    fetchData: async function () {
      try {
        const res = await Axios.get('/api/info');
        this.info = res.data;
      } catch(e) {
        M.toast({html: 'Ein Fehler ist aufgetreten.'});
      }
    },
  },
  mounted: async function() {
    await this.fetchData();

    switch(this.info.state) {
      case 'NEED_INIT':
        await this.$router.push('/init').catch(() => {});
        break;
      case 'BACKUP':
        await this.$router.push('/status/backup').catch(() => {});
        break;
      case 'RESTORE':
        await this.$router.push('/status/restoring').catch(() => {});
        break;
    }
    this.fetched = true;
  }
}
</script>

<style>
  :root {
    --bg: #F6FAFD;
    --nav: #163B63;
    --nav-dark: #0F2C4A;
    --blue-1: #1E4D7B;
    --blue-2: #256E96;
    --accent: #2FA8CC;
    --accent-dark: #2489AB;
    --accent-light: #5FBFDB;
    --text: #2E3A46;
  }

  body {
    background-color: #F6FAFD;
    color: #2E3A46;
    overflow-y: scroll;
  }

  /* ---- Palette remap of Materialize color utility classes ---- */
  /* `body` prefix raises specificity so these win over materialize.min.css,
     which is imported after this component's styles and also uses !important. */
  body .green { background-color: #2FA8CC !important; }
  body .green.lighten-4 { background-color: #E8F4FA !important; }
  body .green.lighten-3 { background-color: #CDE9F4 !important; }
  body .green.lighten-2 { background-color: #A9D9EA !important; }
  body .green.lighten-1 { background-color: #7FCBE3 !important; }
  body .green.darken-1 { background-color: #2B93BE !important; }
  body .green.darken-2 { background-color: #256E96 !important; }
  body .green.darken-3 { background-color: #1E4D7B !important; }
  body .green.darken-4 { background-color: #163B63 !important; }

  body .light-green { background-color: #5FBFDB !important; }
  body .light-green.lighten-4 { background-color: #E8F4FA !important; }

  body .teal { background-color: #2FA8CC !important; }
  body .teal.darken-1 { background-color: #2B93BE !important; }
  body .teal.darken-2 { background-color: #2489AB !important; }
  body .teal.darken-3 { background-color: #1E7A9B !important; }

  body .green-text { color: #2FA8CC !important; }
  body .green-text.text-lighten-3 { color: #A9D9EA !important; }
  body .green-text.text-darken-3 { color: #1E4D7B !important; }
  body .green-text.text-darken-4 { color: #163B63 !important; }
  body .teal-text { color: #2FA8CC !important; }

  /* carousel / slider indicators */
  body .slider .indicators .indicator-item.active { background-color: #2FA8CC !important; }

  /* progress bars */
  body .progress { background-color: #CDE9F4; }
  body .progress .determinate,
  body .progress .indeterminate { background-color: #2FA8CC; }

  /* pagination + active collection items */
  body .pagination li.active { background-color: #163B63; }
  body .collection .collection-item.active {
    background-color: #2FA8CC;
    color: #fff;
  }
  body .collection .collection-item.active .secondary-content { color: #fff; }

  /* ---- Global polish ---- */
  .btn, .btn-large, .btn-small {
    border-radius: 8px;
    text-transform: none;
    letter-spacing: 0.2px;
    box-shadow: 0 2px 6px rgba(22, 59, 99, 0.18);
  }
  .btn:hover, .btn-large:hover, .btn-small:hover {
    box-shadow: 0 4px 12px rgba(22, 59, 99, 0.25);
  }

  .card, .card-panel {
    border-radius: 12px;
  }

  body .btn-floating.green,
  body .btn-floating.teal { background-color: #2FA8CC !important; }

  body input:not([type]):not(.browser-default):focus:not([readonly]),
  body input[type=text]:not(.browser-default):focus:not([readonly]),
  body input[type=password]:not(.browser-default):focus:not([readonly]),
  body input[type=email]:not(.browser-default):focus:not([readonly]),
  body input[type=number]:not(.browser-default):focus:not([readonly]),
  body input[type=search]:not(.browser-default):focus:not([readonly]),
  body textarea.materialize-textarea:focus:not([readonly]) {
    border-bottom: 1px solid #2FA8CC !important;
    box-shadow: 0 1px 0 0 #2FA8CC !important;
  }
  body input:not([type]):not(.browser-default):focus:not([readonly]) + label,
  body input[type=text]:not(.browser-default):focus:not([readonly]) + label,
  body input[type=password]:not(.browser-default):focus:not([readonly]) + label,
  body input[type=email]:not(.browser-default):focus:not([readonly]) + label,
  body input[type=number]:not(.browser-default):focus:not([readonly]) + label,
  body textarea.materialize-textarea:focus:not([readonly]) + label {
    color: #2489AB !important;
  }

  body [type="checkbox"].filled-in:checked + span:not(.lever)::after {
    border-color: #2FA8CC !important;
    background-color: #2FA8CC !important;
  }
  body [type="radio"]:checked + span::after,
  body [type="radio"].with-gap:checked + span::after {
    border-color: #2FA8CC !important;
    background-color: #2FA8CC !important;
  }
  body [type="radio"].with-gap:checked + span::before {
    border-color: #2FA8CC !important;
  }
  body .switch label input[type=checkbox]:checked + .lever { background-color: #A9D9EA !important; }
  body .switch label input[type=checkbox]:checked + .lever::after { background-color: #2FA8CC !important; }

  .tabs .tab a { color: #256E96; }
  .tabs .tab a:hover, .tabs .tab a.active { color: #163B63; }
  body .tabs .indicator { background-color: #2FA8CC !important; }

  .dropdown-content li > a, .dropdown-content li > span { color: #163B63; }

  .modal {
    max-width: 600px;
    border-radius: 12px;
  }

  .internal-container {
    margin: auto;
    min-height: inherit;
    max-width: 1200px;
  }

  #main-row {
    max-width: 1600px;
  }

  .margin-1 {
    margin-left: 10px;
  }

  .text-hover {
    cursor: pointer;
    padding: 0 10px 0 10px;
  }

  .text-hover:hover {
    background-color: #2FA8CC;
    border-radius: 20px;
  }

  .green-badge {
    background-color: #2FA8CC;
    border-radius: 10px;
    padding: 0 10px 0 10px;
    font-size: 0.8em;
    display: inline-block;
    color: white;
  }

  .green-badge-light {
    background-color: #D3ECF5;
    border-radius: 10px;
    padding: 0 10px 0 10px;
    font-size: 0.8em;
    display: inline-block;
    color: black;
  }

  .empty-hint {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    text-align: center;
    font-size: 2em;
    color: #e0e0e0;
    font-weight: bold;
  }

  /* move toasts to bottom right */
  #toast-container {
    top: auto !important;
    left: auto !important;
    right: 50px !important;
    bottom: 50px !important;
  }

  @media screen and (max-width: 900px) {
    #toast-container {
      right: 0 !important;
      bottom: 0 !important;
    }
  }
</style>
