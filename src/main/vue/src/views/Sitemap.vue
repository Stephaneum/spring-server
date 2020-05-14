<template>
  <CenterLayout title="Sitemap" :plan="info.plan" :history="info.history" :eu-sa="info.euSa">
    <span v-for="m in info.menu" :key="m.id">
      <router-link :to="'/home?id='+m.id" v-slot="{ href, navigate }">
          <a @click="navigate" :href="href" class="item">- {{ m.name }}</a>
      </router-link>
      <br>
      <span v-for="m2 in m.children" :key="m2.id" style="margin-left: 30px">
        <router-link :to="'/home?id='+m2.id" v-slot="{ href, navigate }">
            <a @click="navigate" :href="href" class="item">- {{ m2.name }}</a>
        </router-link>
        <br>
        <span v-for="m3 in m2.children" :key="m3.id" style="margin-left: 60px">
          <router-link :to="'/home?id='+m3.id" v-slot="{ href, navigate }">
              <a @click="navigate" :href="href" class="item">- {{ m3.name }}</a>
          </router-link>
          <br>
          <span v-for="m4 in m3.children" :key="m4.id" style="margin-left: 90px">
            <router-link :to="'/home?id='+m4.id" v-slot="{ href, navigate }">
                <a @click="navigate" :href="href" class="item">- {{ m4.name }}</a>
            </router-link>
            <br>
          </span>
        </span>
      </span>
    </span>
  </CenterLayout>
</template>

<script>
  import CenterLayout from "../components/CenterLayout";

  export default {
    name: 'Sitemap',
    components: { CenterLayout },
    props: ['info']
  }
</script>

<style scoped>
  .item {
    color: black !important;
  }

  .item:hover {
    color: #4caf50 !important;
  }
</style>