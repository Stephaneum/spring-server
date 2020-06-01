<template>
  <div style="min-height: inherit;">

    <InternalHeader title="Autorisierung" icon="vpn_key"></InternalHeader>

    <div class="row">
      <RegisterPanel @update-info="updateInfo" class="col s12 m5 l4 offset-l1 xl3 offset-xl2"></RegisterPanel>
      <div class="col m2 l2 xl2 hide-on-small-only"></div>
      <LoginPanel @update-info="updateInfo" class="col s12 m5 l4 xl3"></LoginPanel>
    </div>
  </div>

</template>

<script>
  import LoginPanel from "../components/auth/LoginPanel";
  import RegisterPanel from "../components/auth/RegisterPanel";
  import InternalHeader from "../components/InternalHeader";

  export default {
    name: 'Login',
    components: {InternalHeader, RegisterPanel, LoginPanel},
    methods: {
      async updateInfo() {
          await this.$emit('update-info');
      }
    }
  }
</script>