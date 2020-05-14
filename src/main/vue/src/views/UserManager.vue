<template>
  <div>
    <div v-if="allowed" class="internal-container">

      <InternalHeader title="Nutzerverwaltung" icon="people"></InternalHeader>

      <UserBatchActions></UserBatchActions>

      <UserSearchAndManage></UserSearchAndManage>

      <UserImport></UserImport>

    </div>
  </div>
</template>

<script>
  import M from "materialize-css"
  import UserBatchActions from '@/components/managers/UserBatchActions.vue'
  import UserSearchAndManage from '@/components/managers/UserSearchAndManage.vue'
  import UserImport from '@/components/managers/UserImport.vue'
  import InternalHeader from "../components/InternalHeader";

  export default {
    name: 'UserManager',
    props: ['info'],
    components: {
      InternalHeader,
      UserBatchActions,
      UserSearchAndManage,
      UserImport
    },
    computed: {
      allowed: function() {
        return this.info.user && this.info.user.code.role === 100;
      }
    },
    mounted: function() {
      M.AutoInit();
    }
  }
</script>