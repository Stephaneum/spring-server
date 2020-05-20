<template>
  <div>
    <Slider :slider="slider" class="hide-on-small-only"></Slider>
    <br>
    <div class="row" style="max-width: 1500px">

      <div class="col s12 m9 offset-m3">
        <h3 v-if="menu.name" class="center-align">{{ menu.name }}</h3>
        <h3 v-else style="visibility: hidden">placeholder</h3>
      </div>

      <div class="col m3 hide-on-small-only">
        <QuickLinks :plan="info.plan" :events="events"></QuickLinks>
        <br>
        <Logos :history="info.history" :eu-sa="info.euSa" style="margin-top: 40px"></Logos>
      </div>

      <div class="col s12 m9">
        <div v-if="fetching" style="height: 600px; text-align: center; font-size: 2rem;">Lade Beiträge..</div>
        <div v-else-if="locked" style="padding-top: 100px; text-align: center">
          <p style="font-size: 1.4rem; margin-bottom: 50px">Dieser Bereich ist passwortgeschützt.</p>

          <div class="input-field" style="display: inline-block">
            <i class="material-icons prefix">vpn_key</i>
            <label for="input-section-password">Passwort</label>
            <input v-model="password" type="password" id="input-section-password" autocomplete="off"/>
          </div>

          <a @click="unlock" class="btn waves-effect green darken-4" style="margin-left: 40px">
            <i class="material-icons left">lock_open</i>
            Entsperren
          </a>

        </div>
        <PostListHome v-else :posts="posts"></PostListHome>

        <ul v-if="!fetching && !locked" class="pagination center-align">
          <router-link v-if="page !== 0" :to="{path:'/m/'+menu.id, query: { 'page': page-1 }}" v-slot="{ href, navigate }">
            <li class="waves-effect">
              <a @click="navigate" :href="href">
                <i class="material-icons">chevron_left</i>
              </a>
            </li>
          </router-link>
          <li v-else class="disabled"><a href="#!"><i class="material-icons">chevron_left</i></a></li>

          <router-link v-for="i in 10" :key="i" :to="{path:'/m/'+menu.id, query: { 'page': i-1 }}" v-slot="{ href, navigate }">
            <li class="waves-effect" :class="i-1 === page ? ['active', 'green', 'darken-1'] : []">
              <a @click="navigate" :href="href">
                {{ i }}
              </a>
            </li>
          </router-link>

          <router-link v-if="page !== lastPage" :to="{path:'/m/'+menu.id, query: { 'page': page+1 }}" v-slot="{ href, navigate }">
            <li class="waves-effect">
              <a @click="navigate" :href="href">
                <i class="material-icons">chevron_right</i>
              </a>
            </li>
          </router-link>
          <li v-else class="disabled"><a href="#!"><i class="material-icons">chevron_right</i></a></li>
        </ul>
      </div>
    </div>

  </div>
</template>

<script>
  import Axios from 'axios'
  import M from "materialize-css";
  import QuickLinks from "../components/QuickLinks";
  import Logos from "../components/Logos";
  import PostListHome from "../components/cms/PostListHome";
  import Slider from "../components/Slider";
  import { showLoadingInvisible, hideLoading } from '../helper/utils';

export default {
  name: 'Section',
  components: {Slider, PostListHome, Logos, QuickLinks},
  props: ['info'],
  data: () => ({
    fetching: true,
    locked: false,
    password: null, // input to unlock section
    slider: [],
    menu: {},
    posts: [],
    events: [],
    page: 0,
    lastPage: 0,
  }),
  methods: {
    async fetchData() {
      this.fetching = true;
      const id = this.$route.params.id;
      this.page = this.$route.query.page || 0;
      const response = (await Axios.get('/api/section/'+id+'?page='+this.page)).data;
      this.slider = response.slider;
      this.menu = response.menu;
      this.locked = response.locked;
      this.posts = response.posts;
      this.events= response.events;
      this.lastPage = response.postCount / 5;
      this.fetching = false;

      this.$nextTick(() => {
        M.Tooltip.init(document.querySelectorAll('.tooltipped'), {});
      });

      hideLoading();
    },
    async unlock() {

      if(!this.password) {
        M.toast({html: 'Bitte Passwort eingeben'});
        return;
      }

      showLoadingInvisible();
      try {
        await Axios.post('/api/unlock/section',{ menu: this.menu.id, password: this.password });
        await this.fetchData();
        M.toast({html: 'Bereich freigeschaltet'});
      } catch (e) {
        switch(e.response.status) {
          case 403:
            M.toast({html: 'Falsches Passwort'});
            break;
          default:
            M.toast({html: 'Ein Fehler ist aufgetreten'});
        }
        hideLoading();
      }
    }
  },
  watch: {
    '$route.params.id': async function() {
      await this.fetchData();
    },
    '$route.query.page': async function() {
      await this.fetchData();
    }
  },
  async mounted() {
    await this.fetchData();
  }
}
</script>

<style scoped>
  .country {
    display: inline-block;
    background: #2e7d32;
    color: white;
    font-size: 16pt;
    padding: 10px;
    margin-left: 20px;
    margin-bottom: 10px;
    border-radius: 15px;
  }
</style>