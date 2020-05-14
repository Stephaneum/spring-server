<template>
  <CenterLayout title="Kontakt" :plan="info.plan" :history="info.history" :eu-sa="info.euSa">
    <span v-html="contact"></span>
  </CenterLayout>
</template>

<script>
  import Axios from "axios"
  import CenterLayout from "../components/CenterLayout";

  export default {
    name: 'Contact',
    components: { CenterLayout },
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