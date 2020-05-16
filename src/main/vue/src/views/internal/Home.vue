<template>
  <div v-if="allowed" class="internal-container">

    <InternalHeader :title="info.user.firstName + ' ' + info.user.lastName" icon="account_circle"></InternalHeader>

    <div style="display: flex; flex-wrap: wrap; align-items: stretch; justify-content: space-evenly; margin-bottom: 30px">
        <router-link v-for="g in groups" :to="'/groups/'+g.id" :key="'g'+g.id" v-slot="{ href, navigate }">
          <a @click="navigate" :href="href" class="group-rect card white-text">
            <i class="material-icons" style="font-size: 50px">people</i>
            <span style="font-weight: bold; font-size: 20px; text-align: center; line-height: 20px; margin-bottom: 5px">{{ g.name }}</span>
            <span>{{ g.leader.firstName }} {{ g.leader.lastName }}</span>
          </a>
        </router-link>
    </div>
    <div v-if="fetched && groups.length === 0" style="text-align: center">
      Du bist keiner Gruppe zugeordnet.
    </div>

    <div style="text-align: center; margin-top: 100px">
      <router-link to="/" v-slot="{ href, navigate }">
        <a @click="navigate" :href="href" class="waves-effect waves-light btn-large teal darken-2">
          <i class="material-icons right">arrow_forward</i>öffentliche Startseite
        </a>
      </router-link>
    </div>
  </div>
</template>

<script>
  import Axios from "axios"
  import M from "materialize-css"
  import InternalHeader from "../../components/InternalHeader";

  export default {
    name: 'Home',
    components: { InternalHeader },
    props: ['info'],
    data: () => ({
      fetched: false,
      groups: []
    }),
    methods: {
      fetchData: async function() {
        const groups = await Axios.get('/api/groups?accepted=true');
        this.groups = groups.data;
        this.fetched = true;
      }
    },
    computed: {
      allowed: function() {
        return this.info.user && this.info.user.code.role >= 0;
      }
    },
    mounted: function() {
      M.AutoInit();
      this.fetchData();
    }
  }
</script>

<style scoped>
  .group-rect {
    flex-basis: calc(25% - 30px);
    background-color: #1b5e20;
    cursor: pointer;
    padding: 15px 5px 15px 5px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
  }

  .group-rect:hover {
    background-color: #388e3c !important;
  }
</style>