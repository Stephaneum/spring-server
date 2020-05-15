<template>
    <div class="col s6 m3 center-align">
        <i class="material-icons" style="font-size: 4rem">{{ icon }}</i>
        <br>
        <span style="font-size: 3rem">{{ number }}</span>
        <br>
        <span style="font-size: 1.2rem">{{ text }}</span>
    </div>
</template>

<script>
export default {
    name: 'StatsNumber',
    props: ['number', 'icon', 'text']
}
</script>