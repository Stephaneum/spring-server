<template>
    <div style="display: none">
        <a href="/vertretungsplan.pdf" target="_blank">
            <div class="quick-button card">
                <div class="card-content white-text">
                    <div class="row" style="margin-bottom:0">
                        <div class="col s12 m12 l8">
                            <span class="card-title">Vertretungsplan</span>
                            <p>{{ (plan || {}).info }}</p>
                        </div>
                        <div class="col l4 right-align hide-on-med-and-down">
                            <i class="material-icons" style="font-size:50pt">description</i>
                        </div>
                    </div>
                </div>
            </div>
        </a>

        <br>

        <router-link to="/termine" v-slot="{ href, navigate }">
            <a @click="navigate" :href="href">
                <div class="quick-button card">
                    <div class="card-content white-text">
                        <div class="row" style="margin-bottom:0">
                            <div class="col s12 m12 l8">
                                <span class="card-title">Termine</span>
                                <p id="termine-date" style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;font-weight:bold">
                                    {{ eventTime }}</p>
                                <p id="termine-info" style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis">
                                    {{ eventName }}</p>
                            </div>
                            <div class="col l4 right-align hide-on-med-and-down">
                                <i id="quick-icon" class="material-icons" style="font-size:60pt">date_range</i>
                            </div>
                        </div>
                    </div>
                </div>
            </a>
        </router-link>

    </div>
</template>

<script>
    import moment from 'moment'

    const formatDateOnly = 'D. MMM';
    const formatDateTime = 'D. MMM [(]HH:mm[)]';

export default {
    name: 'QuickLinks',
    props: ['plan', 'events'],
    data: () => ({
        intervalId: null,
        currIndex: 0,
        eventName: null,
        eventTime: null
    }),
    methods: {
        tick() {
            if(this.events === null || this.events.length === 0)
                return;

            this.currIndex = (this.currIndex + 1) % this.events.length;
            const currEvent = this.events[this.currIndex];
            const start = moment(currEvent.start);
            const end = currEvent.end ? moment(currEvent.end) : null;

            if(start && end) {
                this.eventTime = this.formatTime(start) + ' - ' + this.formatTime(end);
            } else {
                this.eventTime = this.formatTime(start);
            }
            this.eventName = currEvent.title;
        },
        formatTime(time) {
            return time.hours() === 0 && time.minutes() === 0 && time.seconds() === 0 ? time.format(formatDateOnly) : time.format(formatDateTime);
        }
    },
    watch: {
        events() {
            this.tick();
        }
    },
    mounted() {
        this.intervalId = setInterval(this.tick, 4000);
    },
    destroyed() {
        if(this.intervalId)
            clearInterval(this.intervalId);
    }
}
</script>

<style scoped>
    .quick-button {
        background-color: #1b5e20;
    }

    .quick-button:hover {
        background-color: #388e3c;
    }
</style>