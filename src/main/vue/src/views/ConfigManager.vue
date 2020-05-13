<template>
  <div>
    <div v-if="allowed" style="margin: 50px auto 0 auto; max-width: 1200px;">

      <div style="text-align: center; margin: 60px 0 60px 0">
        <i class="material-icons" style="font-size: 4em">build</i>
        <h4 style="margin: 0">Konfiguration</h4>
      </div>

      <SliderManager></SliderManager>

      <VariableManager></VariableManager>

    </div>

    <div style="height: 100px"></div>
  </div>
</template>

<script>
  import M from "materialize-css"
  import SliderManager from '@/components/managers/SliderManager.vue'
  import VariableManager from '@/components/managers/VariableManager.vue'

  export default {
    name: 'ConfigManager',
    props: ['info'],
    components: {
      SliderManager,
      VariableManager,
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