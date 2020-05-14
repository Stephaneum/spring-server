<template>
  <div v-if="allowed" class="internal-container" style="max-width: 1600px">

    <InternalHeader title="Deine persönliche Cloud" icon="cloud" left="true"></InternalHeader>

    <cloud-view :my-id="info.user.id" :shared-mode="false" :modify-all="true" root-url="/api/cloud/view/user" upload-url="/api/cloud/upload/user" folder-url="/api/cloud/create-folder/user" :teacherchat="hasTeacherChat"></cloud-view>
  </div>
</template>

<script>
  import M from "materialize-css"
  import CloudView from '@/components/cloud/CloudView.vue'
  import InternalHeader from "../components/InternalHeader";

  export default {
    name: 'Account',
    props: ['info'],
    components: {InternalHeader, CloudView },
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