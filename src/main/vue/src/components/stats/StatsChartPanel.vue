<template>
    <div style="margin-top: 50px">
        <TabBar :tabs="tabs" :curr-tab="currTab" @selected="selectTab"></TabBar>
        <div class="card-panel white" style="margin-top: 0">
            <GChart v-show="currTab.id === tabs.days.id || currTab.id === tabs.hour.id" type="ColumnChart" :data="barData" :options="barOptions" />

            <div v-show="currTab.id === tabs.system.id" class="row">
                <div class="col s6">
                    <GChart type="PieChart" :data="pieData1" :options="pieOptions1" />
                </div>
                <div class="col s6">
                    <GChart type="PieChart" :data="pieData2" :options="pieOptions2" />
                </div>
            </div>
        </div>
    </div>

</template>

<script>
    import { GChart } from 'vue-google-charts'
    import TabBar from "../TabBar";

    const tabs = {
      days: { id: 0, name: 'Aufrufe (30 Tagen)', icon: 'today' },
      hour: { id: 1, name: 'Aufrufe (Uhrzeit)', icon: 'schedule' },
      system: { id: 2, name: 'Systeme', icon: 'devices' },
      // cloud: { id: 3, name: 'Cloud', icon: 'cloud' } TODO
    };

    const standardOptions = {
        height: 250,
        legend: 'none',
        colors: ['#85b56e'],
        chartArea: {
            top: 10,
            right: 0,
            bottom: 20,
            left: 50
        }
    };

export default {
    name: 'StatsChartPanel',
    components: { TabBar, GChart },
    props: ['statsDay', 'statsHour', 'statsBrowser', 'statsOS'],
    data: () => ({
        currTab: tabs.days,
        tabs: tabs,
        barData: [
            ['Tag', 'Aufrufe'],
            ['0', 0]
        ],
        barOptions: { ...standardOptions },
        pieData1: null,
        pieOptions1: null,
        pieData2: null,
        pieOptions2: null
    }),
    methods: {
        selectTab(t) {
            this.currTab = t;
            this.updateChart();
        },
        updateChart() {
            switch(this.currTab.id) {
                case tabs.days.id:
                    this.barData = [
                        ['Tag', 'Aufrufe'],
                        ...this.statsDay.map((s) => [s.day, s.count])
                    ];
                    break;
                case tabs.hour.id:
                    this.barData = [
                        ['Uhrzeit', 'Aufrufe'],
                        ...this.statsHour.map((s) => [s.hour, s.count])
                    ];
                    break;
                case tabs.system.id:
                    this.pieData1 = [
                        ['Browser', 'Aufrufe'],
                        ...this.statsBrowser.map((s) => [s.browser, s.count])
                    ];
                    this.pieData2 = [
                        ['OS', 'Aufrufe'],
                        ...this.statsOS.map((s) => [s.os, s.count])
                    ];
                    break;
            }
        }
    },
    watch: {
        statsDay: {
            immediate: true,
            handler() {
                this.updateChart();
            }
        }
    }
}
</script>

<style>
    svg > g > g:last-child { pointer-events: none }
</style>