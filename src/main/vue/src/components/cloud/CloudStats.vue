<template>
    <div>
        <div style="text-align: right">
            <a @click="exit" class="waves-effect btn-flat" href="#!" style="margin: 10px 10px 0 0">
                <i class="material-icons">close</i>
            </a>
        </div>

        <p style="text-align: center; font-size: 1.8em; font-weight: bold">Dein Speicherverbrauch</p>

        <div class="stats-storage-bar" style="margin: 70px 20px 20px 20px; height: 20px">
            <div style="background-color: #a5d6a7; left: 0" :style="{ width: (percentage*100)+'%' }"></div>
        </div>

        <div style="display: flex; justify-content: space-evenly">
            <span><span class="color-block" style="background-color: #a5d6a7"></span>Beansprucht: <b>{{ used }}</b></span>
            <span>Verfügbar: <b>{{ free }}</b></span>
            <span>Insgesamt: <b>{{ total }}</b></span>
        </div>

        <div class="stats-storage-bar" style="margin: 100px 20px 20px 20px; height: 20px">
            <div style="background-color: #a5d6a7; left: 0" :style="{ width: (privatePercentage*100)+'%' }"></div>
            <div style="background-color: #ffcc80;" :style="{ left: (privatePercentage*100)+'%', width: (groupPercentage*100)+'%' }"></div>
        </div>

        <div style="display: flex; justify-content: space-evenly">
            <span><span class="color-block" style="background-color: #a5d6a7"></span>Nutzerspeicher: <b>{{ privateUsed }}</b></span>
            <span><span class="color-block" style="background-color: #ffcc80"></span>Gruppenspeicher: <b>{{ groupUsed }}</b></span>
        </div>

        <div style="margin: 70px 0 50px 0; text-align: center">
            Insgesamt <b>{{ count }}</b> Dateien wurden hochgeladen.
        </div>
    </div>
</template>

<script>
export default {
    name: 'CloudStats',
    props: ['percentage', 'free', 'used', 'total', 'count', 'privateUsed', 'privatePercentage', 'groupUsed', 'groupPercentage'],
    methods: {
        exit() {
            this.$emit('onexit');
        }
    }
}
</script>

<style scoped>
    .stats-storage-bar {
        border: 2px solid #eeeeee;
        position: relative;
        margin: 10px 0 10px 0;
    }

    .stats-storage-bar > div {
        position: absolute;
        top: 0;
        height: 100%;
    }

    .color-block {
        display: inline-block;
        width: 20px;
        height: 10px;
        margin-right: 10px;
    }
</style>