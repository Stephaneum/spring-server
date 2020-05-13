<template>
  <div id="app">
    <Menu :menu="info.menu" :has-menu-write-access="info.hasMenuWriteAccess" :user="info.user" :plan="info.plan" :unapproved="info.unapproved"></Menu>
    <div style="min-height: calc(100vh - 150px)">
      <router-view :info="info"/>
    </div>
    <Footer :copyright="info.copyright"></Footer>
  </div>
</template>

<script>
// @ is an alias to /src
import Axios from "axios"
import M from "materialize-css"
import Menu from '@/components/Menu.vue'
import Footer from '@/components/Footer.vue'

export default {
  name: 'Home',
  components: {
    Menu,
    Footer
  },
  data: () => ({
      info: { user: null, hasMenuWriteAccess: false, menu: null, plan: null, copyright: null, unapproved: null }
  }),
  mounted: async function() {
    try {
      const res = await Axios.get('/api/info');
      this.info = res.data;
    } catch(e) {
      M.toast({html: 'Ein Fehler ist aufgetreten.'});
    }
  }
}
</script>

<style>
  body {
    background-color: #f0f0f0;
    overflow-y: scroll;
  }

  .modal {
    max-width: 600px;
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
    background-color: #00C853;
    border-radius: 20px;
  }

  .green-badge {
    background-color: #4caf50;
    border-radius: 10px;
    padding: 0 10px 0 10px;
    font-size: 0.8em;
    display: inline-block;
    color: white;
  }

  .green-badge-light {
    background-color: #dcedc8;
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
</style>
