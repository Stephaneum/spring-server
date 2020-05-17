<template>
  <div>
    <Slider :slider="slider"></Slider>
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
        <PostListHome v-else :posts="posts"></PostListHome>

        <ul class="pagination center-align">
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

export default {
  name: 'Index',
  components: {Slider, PostListHome, Logos, QuickLinks},
  props: ['info'],
  data: () => ({
    fetching: true,
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
      this.posts = response.posts;
      this.events= response.events;
      this.lastPage = response.postCount / 5;
      this.fetching = false;

      this.$nextTick(() => {
        M.Tooltip.init(document.querySelectorAll('.tooltipped'), {});
      });
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