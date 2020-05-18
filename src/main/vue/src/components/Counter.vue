<template>
    <span>
      {{ curr }}
    </span>
</template>

<script>
  const counterDuration = 5000; // 5s
export default {
    name: 'Counter',
    props: ['immediately', 'number', 'start'],
    data: () => ({
        timerId: null,
        curr: 0,
        startTime: null, // millis
        startValue: null,
        delta: null
    }),
    methods: {
        startCounter() {
            if(this.intervalId)
                return;

            this.startTime = new Date().getTime();
            this.intervalId = setInterval(this.tick, 50);
        },
        tick() {
            if(!this.number && this.startValue === 0) {
                // smart detection that the real values has not been fetched yet
                // wait until it is fetched
                this.startTime = new Date().getTime();
                return;
            }

            const percentageTime = (new Date().getTime() - this.startTime) / counterDuration;
            if(this.intervalId && percentageTime >= 1) {
                clearInterval(this.intervalId);
            } else {
                this.curr = Math.round(this.startValue + (this.easeOutCubic(percentageTime) * this.delta));
            }
        },
        easeOutCubic(x) {
          return 1 - Math.pow(1 - x, 5);
        }
    },
    watch: {
        number: {
            immediate: true,
            handler() {
                this.startValue = this.startValue || 0; // custom start value different from 0
                this.delta = this.number - this.startValue;
            }
        }
    },
    mounted() {
        if(this.immediately)
            this.startCounter();
    },
    destroyed() {
        if(this.intervalId)
            clearInterval(this.intervalId);
    }
}
</script>

<style>

</style>