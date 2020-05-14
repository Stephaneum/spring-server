<template>
    <div style="text-align: center">
        <!-- history -->
        <a :href="historyLink">
            <img src="../assets/img/logo-steph.png" style="width: 150px" alt="logo"/>
        </a>

        <img src="../assets/img/europaschule.png" style="margin-top: 50px;" alt="logo"/>

        <a href="http://www.unesco.de/bildung/ups.html" target="_blank">
            <img src="../assets/img/unesco.png" style="margin-top: 50px;" alt="logo"/>
        </a>

        <img src="../assets/img/sor.png" style="margin-top: 50px;" alt="logo"/>

        <a :href="euSaLink">
            <img src="../assets/img/investor-scl.png" style="margin-top: 50px;" alt="logo"/>
        </a>
    </div>
</template>

<script>
export default {
    name: 'Logos',
    props: ['history', 'euSa'],
    computed: {
        historyLink: function () {
            if(this.history) {
                if(this.history.startsWith('http'))
                    return this.history;
                else
                    return '/geschichte.xhtml';
            } else {
                return '#!'
            }
        },
        euSaLink: function () {
            if(this.euSa) {
                if(this.euSa.startsWith('http'))
                    return this.euSa;
                else
                    return '/eu_sa.xhtml';
            } else {
                return '#!'
            }
        },
    }
}
</script>

<style>

</style>