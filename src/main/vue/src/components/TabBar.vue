<template>
    <div>
        <div v-for="(t, index) in tabs" :key="index" @click="emit(t)" class="tab-btn" :class="{ 'tab-btn-special': t.special, 'tab-btn-active': !t.special && t.id === currTab.id, 'tab-btn-special-active': t.special && t.id === currTab.id }">
            <i style="font-size: 1em; margin-right: 7px" class="material-icons">{{ t.icon }}</i>
            <span style="vertical-align: middle">{{ t.name }}{{ t.number ? ' ('+t.number+')' : null }}</span>
        </div>
    </div>
</template>

<script>
export default {
    name: 'TabBar',
    props: ['tabs', 'currTab'],
    methods: {
        emit: function(tab) {
            this.$emit('selected', tab);
        },
    }
}
</script>

<style>
    .tab-btn {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        vertical-align: bottom;
        padding: 5px 15px 5px 10px;
        margin: 0 5px 0 5px;
        border-radius: 10px 10px 0 0;
        background-color: #558b2f;
        height: 40px;
        color: white;
        font-size: 1.2em;
    }

    .tab-btn:hover {
        background-color: #689f38;
        cursor: pointer;
    }

    .tab-btn-active {
        background-color: #43a047 !important;
        height: 45px !important;
    }

    .tab-btn-special {
        background-color: #009688;
    }

    .tab-btn-special:hover {
        background-color: #26a69a;
    }

    .tab-btn-special-active {
        background-color: #00897b !important;
        height: 45px !important;
    }
</style>