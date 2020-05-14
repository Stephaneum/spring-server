<template>
  <div>
    <div v-if="allowed" class="internal-container">

      <InternalHeader title="Konfiguration" icon="build"></InternalHeader>

      <SliderManager></SliderManager>

      <VariableManager></VariableManager>

    </div>

    <div style="height: 100px"></div>
  </div>
</template>

<script>
  import M from "materialize-css"
  import SliderManager from '../components/managers/SliderManager.vue'
  import VariableManager from '../components/managers/VariableManager.vue'
  import InternalHeader from "../components/InternalHeader";

  export default {
    name: 'ConfigManager',
    props: ['info'],
    components: {
      InternalHeader,
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