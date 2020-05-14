<template>
  <CenterLayout title="Termine" :plan="info.plan" :history="info.history" :eu-sa="info.euSa" hide-title="true">
    <FullCalendar
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
        }"/>
  </CenterLayout>
</template>

<script>
  import Axios from "axios"
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
        calendarPlugins: [ dayGridPlugin ]
    }),
    async mounted() {
      const events = await Axios.get('/api/events');
      this.events = events.data;
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