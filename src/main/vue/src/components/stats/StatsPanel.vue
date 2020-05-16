<template>
    <div class="card-panel white" style="margin-top: 50px">
        <span style="display: flex; align-items: center">
            <i class="material-icons">{{ icon }}</i>
            <h5 style="margin: 0 0 0 10px">{{ title }}</h5>
        </span>
        <slot></slot>
    </div>
</template>

<script>
export default {
    name: 'StatsPanel',
    props: ['title', 'icon']
}
</script>