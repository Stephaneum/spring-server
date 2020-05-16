<template>
    <div class="card-panel white" style="margin-top: 50px">
        <GChart type="ColumnChart" :data="chartData" :options="chartOptions" style="height: 300px" />
    </div>

</template>

<script>
    import { GChart } from 'vue-google-charts'

export default {
    name: 'StatsChartPanel',
    components: { GChart },
    data: () => ({
        chartData: [
            ['Year', 'Sales', 'Expenses', 'Profit'],
            ['2014', 1000, 400, 200],
            ['2015', 1170, 460, 250],
            ['2016', 660, 1120, 300],
            ['2017', 1030, 540, 350]
        ],
        chartOptions: {
            chart: {
                title: 'Company Performance',
                subtitle: 'Sales, Expenses, and Profit: 2014-2017',
            }
        }
    })
}
</script>