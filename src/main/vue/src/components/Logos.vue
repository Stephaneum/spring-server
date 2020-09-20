<template>
    <div style="text-align: center">
        <a v-if="historyLink" :href="history">
            <img src="../assets/img/logo-steph.png" style="width: 150px" alt="logo"/>
        </a>
        <router-link v-else to="/geschichte" v-slot="{ href, navigate }">
            <a @click="navigate" :href="href">
                <img src="../assets/img/logo-steph.png" style="width: 150px" alt="logo"/>
            </a>
        </router-link>

        <br>

        <img src="../assets/img/europaschule.png" style="margin-top: 50px;" alt="logo"/>

        <a href="http://www.unesco.de/bildung/ups.html" target="_blank">
            <img src="../assets/img/unesco.png" style="margin-top: 50px;" alt="logo"/>
        </a>

        <img src="../assets/img/sor.png" style="margin-top: 50px;" alt="logo"/>

        <img src="../assets/img/prime-gym.png" style="margin-top: 50px;" alt="logo"/>

        <a v-if="euSaLink" :href="euSa">
            <img src="../assets/img/investor-scl.png" style="margin-top: 50px;" alt="logo"/>
        </a>
        <router-link v-else to="/eu-sa" v-slot="{ href, navigate }">
            <a @click="navigate" :href="href">
                <img src="../assets/img/investor-scl.png" style="margin-top: 50px;" alt="logo"/>
            </a>
        </router-link>
    </div>
</template>

<script>
export default {
    name: 'Logos',
    props: ['history', 'euSa'],
    computed: {
        historyLink: function () {
            return this.history && this.history.startsWith('http');
        },
        euSaLink: function () {
            return this.euSa && this.euSa.startsWith('http');
        },
    }
}
</script>

<style>

</style>