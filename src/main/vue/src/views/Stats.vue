<template>
  <CenterLayout title="Statistiken" custom-card="true" hide-logos="true" :plan="info.plan" :history="info.history" :eu-sa="info.euSa">
    <div class="card-panel white">
      <div class="row" style="margin: 20px 0 20px 0">
        <StatsNumber text="Schüler/innen*" icon="people" :number="studentCount"></StatsNumber>
        <StatsNumber text="Lehrer/innen*" icon="people" :number="teacherCount"></StatsNumber>
        <StatsNumber text="Beiträge" icon="edit" :number="postCount"></StatsNumber>
        <StatsNumber text="Aufrufe (30 Tage)" icon="star" :number="visitCount"></StatsNumber>
      </div>
    </div>

    <StatsChartPanel v-if="fetched" :stats-day="statsDay" :stats-hour="statsHour" :stats-browser="statsBrowser" :stats-o-s="statsOS"></StatsChartPanel>

    <StatsPanel title="Technologien" icon="settings">
      <div style="margin: 30px 0 20px 0; display: flex; align-items: end; justify-content: space-evenly">
        <Tech title="Frontend-Framework" name="Vue" img="vue.png" width="200" info="Blablabla" link="https://vuejs.org"></Tech>
        <Tech title="Backend-Framework" name="Spring" img="spring.png" width="130" info="Blablabla" link="https://spring.io"></Tech>
        <Tech title="Datenbanksystem" name="MySQL" img="mysql.png" width="130" info="Blablabla" link="https://www.mysql.com"></Tech>
        <Tech title="Betriebssystem" name="Ubuntu" img="ubuntu.png" width="200" info="Blablabla" link="https://ubuntu.com"></Tech>
      </div>
    </StatsPanel>

    <StatsPanel title="Laufzeit" icon="schedule">
      <div style="margin-top: 20px; font-size: 1.2rem">
        Der Server läuft seit <LiveTimer :seconds="upTime" style="font-size: 1.2rem; font-weight: bold"></LiveTimer>.
      </div>

      <div style="margin-top: 10px; font-size: 1.2rem">
        <span>Serverstart war am </span><span style="font-size: 1.2rem; font-weight: bold">{{ startTime }}</span>.
      </div>
    </StatsPanel>

    <StatsPanel title="Über die Entwicklung" icon="build">
      <span v-html="dev"></span>
    </StatsPanel>

    <p style="color: grey; text-align: right">*momentan im System registriert</p>
  </CenterLayout>
</template>

<script>
  import Axios from "axios"
  import moment from "moment"
  import CenterLayout from "../components/CenterLayout";
  import StatsNumber from "../components/stats/StatsNumber";
  import Tech from "../components/stats/Tech";
  import LiveTimer from "../components/stats/LiveTimer";
  import StatsPanel from "../components/stats/StatsPanel";
  import StatsChartPanel from "../components/stats/StatsChartPanel";

  export default {
    name: 'Stats',
    components: {StatsChartPanel, StatsPanel, LiveTimer, Tech, StatsNumber, CenterLayout },
    props: ['info'],
    data: () => ({
      fetched: false,
      studentCount: null,
      teacherCount: null,
      postCount: null,
      visitCount: null,

      statsDay: null,
      statsHour: null,
      statsBrowser: null,
      statsOS: null,

      upTime: null,
      startTime: null,
      dev: null
    }),
    async mounted() {
      const stats = (await Axios.get('/api/stats')).data;
      this.studentCount = stats.studentCount;
      this.teacherCount = stats.teacherCount;
      this.postCount = stats.postCount;
      this.visitCount = stats.visitCount;

      this.statsDay = stats.statsDay;
      this.statsHour = stats.statsHour;
      this.statsBrowser = stats.statsBrowser;
      this.statsOS = stats.statsOS;

      this.upTime = stats.upTime;
      this.startTime = moment(stats.startTime).format('dddd, [den] DD.MMMM yyyy');
      this.dev = stats.dev;

      this.fetched = true;
    }
  }
</script>