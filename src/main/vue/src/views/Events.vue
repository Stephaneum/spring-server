<template>
  <CenterLayout title="Termine" :plan="info.plan" :history="info.history" :eu-sa="info.euSa" hide-title="true">
    <FullCalendar :options="calendarOptions" />

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
  import tippy from 'tippy.js';
  import 'tippy.js/dist/tippy.css';

  export default {
    name: 'Events',
    components: { FullCalendar, CenterLayout },
    props: ['info'],
    data: () => ({
      calendarPlugins: [ dayGridPlugin ],
      monthEvents: [],
      calendarOptions: {
        plugins: [dayGridPlugin],
        initialView: 'dayGridMonth',
        locale: 'de',
        weekends: true,
        events: [],
        headerToolbar: {
          left: 'prev',
          center: 'title',
          right: 'next',
        },
        firstDay: 1,
        fixedWeekCount: false,
        eventColor: 'rgb(220, 237, 200)',
        eventTextColor: 'black',
        eventDidMount: function(info) {
          let tooltipContent = info.event.extendedProps.tooltipContent;
          if (tooltipContent) {
            tippy(info.el, {
              content: tooltipContent,
              allowHTML: true, // allows for HTML content inside the tooltip
              animation: 'fade', // use 'scale' or other animations if you want
            });
          }
        },
      },
    }),
    methods: {
      setMonthEvents(year, month) {
        this.monthEvents = this.calendarOptions.events.filter(event => {
          const eventDate = new Date(event.start);
          return eventDate.getFullYear() === year && eventDate.getMonth() === month;
        });
      },
    },
    async mounted() {
      this.calendarOptions.datesSet = (arg) => {
        const start = arg.start;
        const end = arg.end;

        const middleDate = new Date(start.getTime() + (end.getTime() - start.getTime()) / 2);

        const year = middleDate.getFullYear();
        const month = middleDate.getMonth(); // Remember, months are zero-indexed. So, 0 = January, 11 = December.

        this.setMonthEvents(year, month);
      }
      const events = await Axios.get('/api/events');
      events.data.forEach(e => {
        e.extendedProps = {
          tooltipContent: e.title,
        };
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

      this.calendarOptions.events = events.data;

      const date = new Date();
      const currYear = date.getFullYear();
      const currMonth = date.getMonth();
      this.setMonthEvents(currYear, currMonth);
    }
  }
</script>

<style>
  /* fc means fullcalendar, we modify the default UI here */

  .fc-daygrid-day-number {
    color: black;
  }

  .fc-col-header-cell-cushion {
    color: black;
  }

  :root {
    --fc-button-bg-color: #1b5e20;
    --fc-button-active-bg-color: #388e3c;
    --fc-button-border-color: #1b5e20;
    --fc-button-text-color: #fff;
    --fc-button-hover-bg-color: #388e3c;
    --fc-button-hover-border-color: #388e3c;
  }
</style>