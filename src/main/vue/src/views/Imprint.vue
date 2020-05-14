<template>
  <CenterLayout title="Impressum" :plan="info.plan" :history="info.history" :eu-sa="info.euSa">
    <span v-html="imprint"></span>
  </CenterLayout>
</template>

<script>
  import Axios from "axios"
  import CenterLayout from "../components/CenterLayout";

  export default {
    name: 'Imprint',
    components: { CenterLayout },
    props: ['info'],
    data: () => ({
      imprint: null
    }),
    async mounted() {
      const imprint = await Axios.get('/api/imprint');
      this.imprint = imprint.data.imprint;
    }
  }
</script>