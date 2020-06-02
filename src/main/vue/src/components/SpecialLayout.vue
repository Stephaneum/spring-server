<template>
    <div style="height: 100vh; display: flex; flex-direction: column; align-items: center; justify-content: center; font-size: 3rem">
        <slot></slot>
    </div>
</template>

<script>
    import Axios from "axios";

    export default {
        name: 'SpecialLayout',
        data() {
            return {
                timerId: null
            }
        },
        methods: {
            async tick() {
                try {
                    const res = await Axios.get('/api/info');

                    if(res.data.state === 'OK') {
                        await this.$emit('ok');
                    }
                } catch (e) {
                    console.log('error fetching info');
                }
            },
        },
        mounted() {
            this.intervalId = setInterval(this.tick, 1000);
        },
        destroyed() {
            if(this.intervalId)
                clearInterval(this.intervalId);
        }
    }
</script>