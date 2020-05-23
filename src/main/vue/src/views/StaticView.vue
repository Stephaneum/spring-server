<template>
  <div v-if="mode === 'FULL_WIDTH'" style="max-width: 1600px; padding-top: 50px; margin: auto" v-html="body"></div>
  <CenterLayout v-else :title="title" :custom-card="true" :plan="info.plan" :history="info.history" :eu-sa="info.euSa">
    <div class="card-panel" v-html="body"></div>
  </CenterLayout>
</template>

<script>
  import Axios from "axios"
  import CenterLayout from "../components/CenterLayout";

  export default {
    name: 'StaticView',
    components: { CenterLayout },
    props: ['info'],
    data: () => ({
      mode: 'FULL_WIDTH',
      title: null,
      body: null
    }),
    async mounted() {
      const route = this.$router.currentRoute.fullPath;
      const path = route.substring(3);
      const response = await Axios.get('/api/static', { params: { path } });
      this.mode = response.data.mode;
      this.title = response.data.title;
      this.body = response.data.body;
    }
  }
</script>