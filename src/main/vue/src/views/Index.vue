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
        <LiveTicker v-if="liveticker" :text="liveticker" style="margin-bottom: 40px"></LiveTicker>

        <PostListHome v-if="posts.length !== 0" :posts="posts"></PostListHome>
        <div v-else style="height: 600px; text-align: center; font-size: 2rem;">Lade Beiträge..</div>

        <div style="text-align: center; margin-top: 20px">
          <router-link :to="'/m/'+menu.id" v-slot="{ href, navigate }">
            <a @click="navigate" :href="href" class="waves-effect waves-light btn green darken-4">
              <i class="material-icons right">arrow_forward</i>mehr Nachrichten
            </a>
          </router-link>
        </div>
        <Locations style="margin-top: 50px"></Locations>
      </div>
    </div>

    <div class="card-panel" style="margin-top: 70px">
      <div class="row">
        <div class="col s12 m9 center-align">
          <h5>Stephaneum in Zahlen...</h5>
          <br/>
        </div>
        <div class="col s3"><br/></div>
      </div>

      <div class="row" v-observe-visibility="{ callback: startNumberAnimation, once: true}">
        <div class="col s4 m3 center-align">
          <i style="font-size: 4rem" class="material-icons" aria-hidden="true">people</i><br/>
          <Counter ref="counterStudent" :number="studentCount" style="font-size: 3rem"></Counter>
          <p>Schüler/innen*</p>
        </div>

        <div class="col s4 m3 center-align">
          <i style="font-size: 4rem" class="material-icons" aria-hidden="true">people</i><br/>
          <Counter ref="counterTeacher" :number="teacherCount" style="font-size: 3rem"></Counter>
          <p>Lehrer/innen*</p>
        </div>

        <div class="col s4 m3 center-align">
          <i style="font-size: 4rem" class="material-icons" aria-hidden="true">schedule</i><br/>
          <Counter ref="counterYears" :number="years" style="font-size: 3rem"></Counter>
          <p>Jahre seit Gründung</p>
        </div>

        <div class="col s12 m3 center-align">
          <br/>
          <router-link to="/statistiken" v-slot="{ href, navigate }">
            <a @click="navigate" :href="href" class="waves-effect waves-light btn green darken-4">
              <i class="material-icons right">arrow_forward</i>mehr Statistiken
            </a>
          </router-link>
        </div>
      </div>
    </div>

    <br/><br/>

    <div class="center-align">
      <br/>
      <div v-if="coop.length !== 0">
        <h5>Wir kooperieren europaweit mit anderen Schulen</h5>
        <br/><br/><br/>
        <template v-for="(c, index) in coop">
          <a :key="index" v-if="c.link && c.tooltip" :href="c.link" target="_blank"><span class="country tooltipped" data-position="top" :data-tooltip="c.tooltip">{{ c.country }}</span></a>
          <span :key="index" v-else-if="c.tooltip" class="country tooltipped" data-position="top" :data-tooltip="c.tooltip">{{ c.country }}</span>
          <a :key="index" v-else-if="c.link" :href="c.link" target="_blank"><span class="country">{{ c.country }}</span></a>
          <span :key="index" v-else class="country">{{ c.country }}</span>
        </template>
        <br/><br/>
      </div>

      <a v-if="coopLink" :href="coopLink" target="_blank" class="waves-effect waves-light btn green darken-4">
        <i class="material-icons right">arrow_forward</i>Kooperationspartner
      </a>
    </div>
    <div class="center-align">
      <br/><br/><br/><br/><br/>
      <p style="color: grey">*momentan im System registriert</p>
    </div>

  </div>
</template>

<script>
  import Axios from 'axios'
  import M from "materialize-css";
  import { ObserveVisibility } from "vue-observe-visibility";
  import QuickLinks from "../components/QuickLinks";
  import Logos from "../components/Logos";
  import Locations from "../components/Locations";
  import PostListHome from "../components/cms/PostListHome";
  import Slider from "../components/Slider";
  import LiveTicker from "../components/LiveTicker";
  import Counter from "../components/Counter";

export default {
  name: 'Index',
  components: {Counter, LiveTicker, Slider, PostListHome, Locations, Logos, QuickLinks},
  props: ['info'],
  directives: {
      ObserveVisibility
  },
  data: () => ({
    slider: [],
    menu: {},
    liveticker: null,
    posts: [],
    events: [],
    studentCount: 0,
    teacherCount: 0,
    years: 0,
    coop: [],
    coopLink: null
  }),
  methods: {
      startNumberAnimation(visible) {
          if(visible) {
              this.$refs.counterStudent.startCounter();
              this.$refs.counterTeacher.startCounter();
              this.$refs.counterYears.startCounter();
          }
      }
  },
  async mounted() {
    const response = (await Axios.get('/api/home')).data;
    this.slider = response.slider;
    this.menu = response.menu;
    this.liveticker = response.liveticker;
    this.posts = response.posts;
    this.events = response.events;
    this.studentCount = response.studentCount;
    this.teacherCount = response.teacherCount;
    this.years = response.years;
    this.coop = response.coop;
    this.coopLink = response.coopLink;

    this.$nextTick(() => {
      M.Tooltip.init(document.querySelectorAll('.tooltipped'), {});
    });
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