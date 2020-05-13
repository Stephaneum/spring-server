<template>
  <div v-if="allowed" id="main-row" style="margin: 50px auto 0 auto;">

    <div class="row">
      <div class="col s10 offset-s2">
        <h4 style="margin: 20px 0 20px 0">Deine persönliche Cloud</h4>
      </div>
    </div>

    <cloud-view :my-id="info.user.id" :shared-mode="false" :modify-all="true" root-url="/api/cloud/view/user" upload-url="/api/cloud/upload/user" folder-url="/api/cloud/create-folder/user" :teacherchat="hasTeacherChat"></cloud-view>
  </div>
</template>

<script>
  import M from "materialize-css"
  import CloudView from '@/components/cloud/CloudView.vue'

  export default {
    name: 'Account',
    props: ['info'],
    components: { CloudView },
    computed: {
      allowed: function() {
        return this.info.user && this.info.user.code.role >= 0;
      },
      hasTeacherChat: function() {
        return this.allowed && (this.info.user.code.role === 1 || this.info.user.code.role === 100);
      }
    },
    mounted: function() {
      M.AutoInit();
    }
  }
</script>