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

        <!-- fetching -->
        <div v-if="fetching" style="height: 600px; text-align: center; font-size: 2rem;">Lade Beiträge..</div>

        <!-- password protected -->
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

        <!-- empty -->
        <template v-else-if="posts.length === 0">

          <!-- empty and no children -->
          <div v-if="menu.children.length === 0" style="padding-top: 100px; text-align: center">
            <span style="font-size: 1.4rem">Noch keine Beiträge in diesem Bereich veröffentlicht.</span>
          </div>

          <!-- empty and children (show them) -->
          <div v-else style="padding-top: 50px; display: flex; flex-direction: column; align-items: center">
            <div>
              <div v-for="m in menu.children" :key="m.id" style="margin-bottom: 10px">
                <a v-if="m.link" :href="m.link" target="_blank" class="btn-flat waves-effect">
                  <i class="material-icons left">arrow_forward</i>{{ m.name }}
                </a>

                <router-link v-else :to="'/m/' + m.id" v-slot="{ href, navigate }">
                  <a @click="navigate" :href="href" class="btn-flat waves-effect">
                    <i class="material-icons left">arrow_forward</i>{{ m.name }}
                  </a>
                </router-link>
              </div>
            </div>
          </div>
        </template>

        <!-- normal -->
        <PostListHome v-else :posts="posts"></PostListHome>

        <ul v-if="!fetching && !locked && posts.length !== 0" class="pagination center-align">
          <router-link v-if="page !== 1" :to="{path:'/m/'+menu.id+'/'+(page-1)}" v-slot="{ href, navigate }">
            <li class="waves-effect">
              <a @click="navigate" :href="href">
                <i class="material-icons">chevron_left</i>
              </a>
            </li>
          </router-link>
          <li v-else class="disabled"><a><i class="material-icons">chevron_left</i></a></li>

          <router-link v-for="i in pages" :key="i" :to="{path:'/m/'+menu.id+'/'+i}" v-slot="{ href, navigate }">
            <li class="waves-effect" :class="i === page ? ['active', 'green', 'darken-1'] : []">
              <a @click="navigate" :href="href">
                {{ i }}
              </a>
            </li>
          </router-link>

          <router-link v-if="page !== lastPage" :to="{path:'/m/'+menu.id+'/'+(page+1)}" v-slot="{ href, navigate }">
            <li class="waves-effect">
              <a @click="navigate" :href="href">
                <i class="material-icons">chevron_right</i>
              </a>
            </li>
          </router-link>
          <li v-else class="disabled"><a><i class="material-icons">chevron_right</i></a></li>
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

    // page management
    page: 0,
    pages: [],
    lastPage: 0,
  }),
  methods: {
    async fetchData() {
      this.fetching = true;
      const id = this.$route.params.id;
      this.page = Math.max(parseInt(this.$route.params.page || 1), 1);
      const response = (await Axios.get('/api/section/'+id+'?page='+this.page)).data;
      this.slider = response.slider;
      this.menu = response.menu;
      this.locked = response.locked;
      this.posts = response.posts;
      this.events= response.events;

      // page
      this.lastPage = Math.ceil(response.postCount / 5);
      if (this.page < 5) {
        // in the beginning
        this.pages = [];
        for (let i = 1; i <= 10 && i <= this.lastPage; i++) {
          this.pages.push(i);
        }
      } else if(this.page >= this.lastPage - 5) {
        // in the end
        this.pages = [];
        for (let i = this.lastPage; i >= this.lastPage - 9 && i >= 1; i--) {
          this.pages.unshift(i);
        }
      } else {
        // in the middle
        this.pages = [];
        for (let i = this.page - 4; i <= this.page + 5; i++) {
          this.pages.push(i);
        }
      }

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
    '$route.params.page': async function() {
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