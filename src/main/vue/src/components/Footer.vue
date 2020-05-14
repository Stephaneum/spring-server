<template>
    <footer class="page-footer" style="background-color: #2a4c2c; width: 100%">
        <div class="container center-align">
            <router-link to="/kontakt" v-slot="{ href, navigate }">
                <a @click="navigate" :href="href" class="green-text text-lighten-3">Kontakt</a>
            </router-link>
            <span class="white-text"> | </span>
            <router-link to="/impressum" v-slot="{ href, navigate }">
                <a @click="navigate" :href="href" class="green-text text-lighten-3">Impressum</a>
            </router-link>
            <span class="white-text"> | </span>
            <router-link to="/sitemap" v-slot="{ href, navigate }">
                <a @click="navigate" :href="href" class="green-text text-lighten-3">Sitemap</a>
            </router-link>
            <br/><br/>
            <span v-html="copyright" style="font-size:12pt;word-wrap: break-word;"></span>
            <br/><br/>
        </div>
    </footer>
</template>

<script>
export default {
    name: 'Footer',
    props: ['copyright']
}
</script>

<style>

</style>