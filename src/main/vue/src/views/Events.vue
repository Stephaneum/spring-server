<template>
  <CenterLayout title="Termine" :plan="info.plan" :history="info.history" :eu-sa="info.euSa" hide-title="true">
    <FullCalendar
        ref="fullCalendar"
        defaultView="dayGridMonth"
        locale="de"
        event-color="rgb(220, 237, 200)"
        event-text-color="black"
        :first-day="1"
        :fixed-week-count="false"
        :events="events"
        :plugins="calendarPlugins"
        :header="{
            left: 'prev',
            center: 'title',
            right: 'next'
        }"
        :custom-buttons="{
          prev: { click: prev },
          next: { click: next }
        }"
    />

    <h5 style="margin: 40px 0 20px 0">Alle Ereignisse aufgelistet:</h5>
    <div v-for="(e, index) in monthEvents" :key="'e'+index">
      <b>{{ e.timeString }}.</b> {{ e.title }}
    </div>
  </CenterLayout>
</template>

<script>
  import Axios from "axios";
  import moment from "moment";
  import CenterLayout from "../components/CenterLayout";
  import FullCalendar from '@fullcalendar/vue'
  import dayGridPlugin from '@fullcalendar/daygrid'
  import '@fullcalendar/core/main.css';
  import '@fullcalendar/daygrid/main.css';

  export default {
    name: 'Events',
    components: { FullCalendar, CenterLayout },
    props: ['info'],
    data: () => ({
      events: null,
      calendarPlugins: [ dayGridPlugin ],
      currYear: new Date().getFullYear(),
      currMonth: new Date().getMonth() // 0 - 11
    }),
    methods: {
      prev: function() {
        this.$refs.fullCalendar.getApi().prev();

        this.currMonth--;
        if (this.currMonth === -1) {
          this.currYear--;
          this.currMonth = 11;
        }
      },
      next: function() {
        this.$refs.fullCalendar.getApi().next();

        this.currMonth++;
        if (this.currMonth === 12) {
          this.currYear++;
          this.currMonth = 0;
        }
      }
    },
    computed: {
      monthEvents: function() {
        if(!this.events)
          return [];
        return this.events.filter(e => e.startMoment.month() === this.currMonth && e.startMoment.year() === this.currYear);
      }
    },
    async mounted() {
      const events = await Axios.get('/api/events');
      this.events = events.data;
      this.events.forEach(e => {
        e.startMoment = moment(e.start);
        if (e.end) {
          e.endMoment = moment(e.end);
          e.timeString = e.startMoment.format('DD.MM') + ' bis ' + e.endMoment.subtract(1, "days").format('DD.MM');
        } else {
          // start only
          if (e.allDay)
            e.timeString = e.startMoment.format('DD.MM');
          else
            e.timeString = e.startMoment.format('DD.MM, HH:mm');
        }
      });
    }
  }
</script>

<style>
  .fc-button-primary {
    color: #fff;
    background-color: #1b5e20;
    border-color: #1b5e20;
  }

  .fc-button-primary:hover {
    background-color: #388e3c !important;
    border-color: #388e3c !important;
  }
</style>