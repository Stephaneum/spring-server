<template>
    <div class="col s6 m3 center-align">
        <i class="material-icons" style="font-size: 4rem">{{ icon }}</i>
        <br>
        <Counter :number="number" :immediately="true" style="font-size: 3rem"></Counter>
        <br>
        <span style="font-size: 1.2rem">{{ text }}</span>
    </div>
</template>

<script>
import Counter from "../Counter";
export default {
    name: 'StatsNumber',
    components: {Counter},
    props: ['number', 'icon', 'text']
}
</script>