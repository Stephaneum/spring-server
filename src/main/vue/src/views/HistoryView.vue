<template>
  <CenterLayout title="Geschichte" :plan="info.plan" :history="info.history" :eu-sa="info.euSa">
    <span v-html="info.history"></span>
  </CenterLayout>
</template>

<script>
  import CenterLayout from "../components/CenterLayout";

  export default {
    name: 'History',
    components: { CenterLayout },
    props: ['info'],
  }
</script>