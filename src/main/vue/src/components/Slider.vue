<template>
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
</template>

<script>
import M from "materialize-css";

export default {
    name: 'Slider',
    props: ['slider'],
    computed: {
        sliderURL: function() {
            return (slider) => '/files/slider/'+slider.name;
        },
    },
    watch: {
        slider: {
            immediate: true,
            handler() {
                this.$nextTick(() => {
                    M.Slider.init(document.querySelectorAll('.slider'), {});
                });
            }
        }
    }
}
</script>

<style>

</style>