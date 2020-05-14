<template>
  <CenterLayout title="Hier investiert Europa in die Zukunft unseres Landes" :plan="info.plan" :history="info.history" :eu-sa="info.euSa">
    <span v-html="info.euSa"></span>
  </CenterLayout>
</template>

<script>
  import CenterLayout from "../components/CenterLayout";

  export default {
    name: 'EuSa',
    components: { CenterLayout },
    props: ['info'],
  }
</script>