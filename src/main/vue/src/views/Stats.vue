<template>
  <CenterLayout title="Statistiken" custom-card="true" hide-logos="true" :plan="info.plan" :history="info.history" :eu-sa="info.euSa">
    <div class="card-panel white">

      <div class="row" style="margin: 20px 0 20px 0">
        <StatsNumber text="Schüler/innen*" icon="people" number="293"></StatsNumber>
        <StatsNumber text="Lehrer/innen*" icon="people" number="293"></StatsNumber>
        <StatsNumber text="Beiträge" icon="edit" number="293"></StatsNumber>
        <StatsNumber text="Aufrufe (30 Tage)" icon="star" number="293"></StatsNumber>
      </div>

    </div>
  </CenterLayout>
</template>

<script>
  import Axios from "axios"
  import CenterLayout from "../components/CenterLayout";
  import StatsNumber from "../components/stats/StatsNumber";

  export default {
    name: 'Stats',
    components: {StatsNumber, CenterLayout },
    props: ['info'],
    data: () => ({
      contact: null
    }),
    async mounted() {
      const contact = await Axios.get('/api/contact');
      this.contact = contact.data.text;
    }
  }
</script>