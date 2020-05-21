<template>
    <div style="margin-top: 50px">
        <TabBar :tabs="tabs" :curr-tab="currTab" @selected="selectTab"></TabBar>
        <div class="card-panel white" style="margin-top: 0">
            <BarChart v-if="currTab.id === tabs.days.id" :chart-data="dayData" :options="dayOptions" :height="300"></BarChart>
            <BarChart v-if="currTab.id === tabs.hour.id" :chart-data="hourData" :options="hourOptions" :height="300"></BarChart>

            <div v-if="currTab.id === tabs.system.id" class="row">
                <div class="col s6">
                    <PieChart :chart-data="browserData" :options="browserOptions" :height="200" />
                </div>
                <div class="col s6">
                    <PieChart :chart-data="osData" :options="osOptions" :height="200" />
                </div>
            </div>

            <LineChart v-if="currTab.id === tabs.cloud.id" :chart-data="lineData" :options="lineOptions" :height="300"></LineChart>
        </div>
    </div>

</template>

<script>
    import TabBar from "../TabBar";
    import LineChart from "./charts/LineChart";
    import BarChart from "./charts/BarChart";
    import PieChart from "./charts/PieChart";

    const tabs = {
      days: { id: 0, name: 'Aufrufe (30 Tagen)', icon: 'today' },
      hour: { id: 1, name: 'Aufrufe (Uhrzeit)', icon: 'schedule' },
      system: { id: 2, name: 'Systeme', icon: 'devices' },
      cloud: { id: 3, name: 'Cloud', icon: 'cloud' }
    };

    const stdColor = '#85b56e'

    const stdOptions = {
        responsive: true,
        maintainAspectRatio: false,
    };

export default {
    name: 'StatsChartPanel',
    components: { PieChart, BarChart, LineChart, TabBar },
    props: ['statsDay', 'statsHour', 'statsBrowser', 'statsOS', 'statsCloud'],
    data: () => ({
        currTab: tabs.days,
        tabs: tabs,
        dayData: {},
        dayOptions: {
            ...stdOptions,
            legend: { display: false },
            scales: {
                yAxes: [{ scaleLabel: { display: true, labelString: 'Aufrufe' }}]
            }
        },
        hourData: {},
        hourOptions: {
            ...stdOptions,
            legend: { display: false },
            scales: {
                yAxes: [{ scaleLabel: { display: true, labelString: 'Aufrufe' }}]
            }
        },
        browserData: {},
        browserOptions: {
            ...stdOptions,
            title: { display: true, text: 'Browser'},
            legend: { position: 'right' }
        },
        osData: {},
        osOptions: {
            ...stdOptions,
            title: { display: true, text: 'Betriebssystem'},
            legend: { position: 'right' }
        },
        lineData: {},
        lineOptions: {
            ...stdOptions,
            legend: { display: false },
            scales: {
                xAxes: [{type: 'time'}],
                yAxes: [{ scaleLabel: { display: true, labelString: 'Speicher in MB' }}]
            }
        }
    }),
    methods: {
        selectTab(t) {
            this.currTab = t;
            this.updateChart();
        },
        updateChart() {
            switch(this.currTab.id) {
                case tabs.days.id:
                    this.dayData = {
                        labels: this.statsDay.map((s) => s.day),
                        datasets: [{
                            data: this.statsDay.map((s) => s.count),
                            backgroundColor: stdColor
                        }]
                    };
                    break;
                case tabs.hour.id:
                    this.hourData = {
                        labels: this.statsHour.map((s) => s.hour),
                        datasets: [{
                            data: this.statsHour.map((s) => s.count),
                            backgroundColor: stdColor
                        }]
                    };
                    break;
                case tabs.system.id:
                    this.browserData = {
                        labels: this.statsBrowser.map((s) => s.browser),
                        datasets: [{
                            data: this.statsBrowser.map((s) => s.count),
                            backgroundColor: ['#ff9800', '#ffeb3b', '#2196f3', '#0d47a1', '#607d8b', '#673ab7']
                        }]
                    };
                    this.osData = {
                        labels: this.statsOS.map((s) => s.os),
                        datasets: [{
                            data: this.statsOS.map((s) => s.count),
                            backgroundColor: ['#ff9800', '#2196f3', '#0d47a1', '#607d8b', '#263238', '#cfd8dc', '#4caf50', '#673ab7']
                        }]
                    };
                    break;
                case tabs.cloud.id:
                    this.lineData = {
                        labels: this.statsCloud.map((s) => new Date(s.date)),
                        datasets: [{
                            data: this.statsCloud.map((s) => s.size),
                            borderColor: stdColor,
                            pointRadius: 1,
                            fill: false
                        }]
                    };
                    break;
            }
        }
    },
    watch: {
        statsDay: {
            immediate: true,
            handler() {
                if(this.statsDay)
                    this.updateChart();
            }
        }
    }
}
</script>