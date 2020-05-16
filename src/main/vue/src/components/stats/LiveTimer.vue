<template>
    <span>{{ d }} {{ dayString }}, {{ h }} {{ hourString }}, {{ m }} {{ minuteString }} und {{ s }} {{ secondString }}</span>
</template>

<script>
export default {
    name: 'LiveTimer',
    props: ['seconds'],
    data: () => ({
        intervalId: null,
        d: 0,
        h: 0,
        m: 0,
        s: 0
    }),
    methods: {
        tick() {
            this.s++;

            if (this.s === 60) {
                this.m++;
                this.s = 0;
            }

            if (this.m === 60) {
                this.h++;
                this.m = 0;
            }

            if (this.h === 24) {
                this.d++;
                this.h = 0;
            }
        }
    },
    computed: {
        dayString() {
            return this.d === 1 ? 'Tag' : 'Tagen';
        },
        hourString() {
            return this.h === 1 ? 'Stunde' : 'Stunden';
        },
        minuteString() {
            return this.m === 1 ? 'Minute' : 'Minuten';
        },
        secondString() {
            return this.s === 1 ? 'Sekunde' : 'Sekunden';
        }
    },
    watch: {
        seconds: {
            immediate: true,
            handler: function(newVal) {
                this.d = Math.floor(newVal / (60 * 60 * 24));
                this.h = Math.floor(newVal / (60 * 60)) % 24;
                this.m = Math.floor(newVal / 60) % 60;
                this.s = newVal % 60;
            }
        }
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