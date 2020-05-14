<template>
    <div v-if="left">
        <div class="row" style="margin: 60px 0 60px 0">
            <div class="col s10 offset-s2" style="display: flex; align-items: center">
                <i class="material-icons" style="font-size: 2.5rem">{{ icon }}</i>
                <h4 style="margin: 0 0 0 20px">{{ title }}</h4>
            </div>
        </div>
    </div>
    <div v-else>
        <div style="text-align: center; margin: 60px 0 60px 0">
            <i class="material-icons" style="font-size: 4em">{{ icon }}</i>
            <h4 style="margin: 0">{{ title }}</h4>
        </div>
    </div>
</template>

<script>
    export default {
        name: 'InternalHeader',
        props: ['title', 'icon', 'left']
    }
</script>