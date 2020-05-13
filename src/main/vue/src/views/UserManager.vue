<template>
  <div>
    <div v-if="allowed" style="margin: 50px auto 0 auto; max-width: 1200px;">

      <div style="text-align: center; margin: 60px 0 60px 0">
        <i class="material-icons" style="font-size: 4em">people</i>
        <h4 style="margin: 0">Nutzerverwaltung</h4>
      </div>

      <UserBatchActions></UserBatchActions>

      <UserSearchAndManage></UserSearchAndManage>

      <UserImport></UserImport>

    </div>

    <div style="height: 100px"></div>
  </div>
</template>

<script>
  import M from "materialize-css"
  import UserBatchActions from '@/components/managers/UserBatchActions.vue'
  import UserSearchAndManage from '@/components/managers/UserSearchAndManage.vue'
  import UserImport from '@/components/managers/UserImport.vue'

  export default {
    name: 'UserManager',
    props: ['info'],
    components: {
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