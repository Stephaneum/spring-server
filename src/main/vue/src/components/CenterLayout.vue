<template>
    <div style="min-height: inherit; margin: auto; max-width: 1600px">

        <div class="row" style="margin: 40px 0 0 0">
            <div class="col m4 l2 hide-on-small-only"></div>
            <div class="col s12 m8">
                <div style="display: flex; align-items: center;">
                    <router-link to="/" v-slot="{ href, navigate }">
                        <a @click="navigate" :href="href" class="green-text" style="margin-left: 20px">Startseite</a>
                    </router-link>
                    <i class="material-icons">chevron_right</i>
                    <a href="#" class="green-text">{{ title }}</a>
                </div>
            </div>
            <div class="col m2 hide-on-med-and-down"></div>
        </div>

        <div class="row">
            <div class="col l2 hide-on-med-and-down">
                <br/><br/>
                <div>
                    <span v-if="plan && plan.exists">
                        <a href="/vertretungsplan.pdf" target="_blank">
                            <div class="quick-button card" style="padding:20px 5px 20px 10px">
                                <div class="white-text">
                                    <h6 style="margin:0"><i class="material-icons left" style="font-size:15pt">description</i>Vertretungsplan</h6>
                                </div>
                            </div>
                        </a>
                    </span>

                    <router-link to="/termine" v-slot="{ href, navigate }">
                        <a @click="navigate" :href="href">
                            <div class="quick-button card" style="padding:20px 5px 20px 10px;margin-top:30px">
                                <div class="white-text">
                                    <h6 style="margin:0"><i class="material-icons left" style="font-size:15pt">date_range</i>Termine</h6>
                                </div>
                            </div>
                        </a>
                    </router-link>

                </div>
            </div>

            <div class="col" :class="hideLogos ? ['s12', 'm12', 'l10'] : ['s12', 'm8']">
                <slot v-if="customCard"></slot>
                <div v-else class="card-panel white" style="min-height: 800px">
                    <h5 v-if="!hideTitle" style="margin-bottom: 30px">{{ title }}</h5>
                    <slot></slot>
                </div>
                <br>
                <router-link to="/" v-slot="{ href, navigate }">
                    <a @click="navigate" :href="href" class="waves-effect waves-light btn green darken-4" style="margin-left:30px;">
                        <i class="material-icons left">arrow_back</i>
                        zurück zur Startseite
                    </a>
                </router-link>
            </div>

            <div v-if="!hideLogos" class="col m2 hide-on-med-and-down">
                <br/><br/>
                <Logos :history="history" :eu-sa="euSa"></Logos>
            </div>
        </div>
    </div>
</template>

<script>
import Logos from "./Logos";
export default {
    name: 'CenterLayout',
    components: { Logos },
    props: ['title', 'hideTitle', 'hideLogos', 'customCard', 'plan', 'history', 'euSa']
}
</script>

<style scoped>
    .quick-button {
        background: #1b5e20;
    }

    .quick-button:hover {
        background: #2e7d32;
    }
</style>