<template>
  <div>
    <div class="slider">
      <ul class="slides">
        <li v-for="i in slider" :key="i.id">
          <img :src="sliderURL(i)" />
          <div class="caption" :class="[ i.direction ]">
            <h3>{{ i.title }}</h3>
            <h5 class="light grey-text text-lighten-3">{{ i.subTitle }}</h5>
          </div>
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
  import Axios from 'axios'
  import M from "materialize-css";

export default {
  name: 'Index',
  data: () => ({
    slider: []
  }),
  computed: {
    sliderURL: function() {
      return (slider) => '/files/slider/'+slider.name;
    },
  },
  async mounted() {
    const response = (await Axios.get('/api/home')).data;
    this.slider = response.slider;

    this.$nextTick(() => {
      M.Slider.init(document.querySelectorAll('.slider'), {});
    });
  }
}
</script>
