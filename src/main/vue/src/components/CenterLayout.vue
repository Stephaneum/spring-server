<template>
    <div style="min-height: inherit; margin: auto; max-width: 1600px">
        <br>
        <div class="row" style="margin: 0">
            <div class="col m4 l2 hide-on-small-only"></div>
            <div class="col s12 m8">
                <div style="display: flex; align-items: center;">
                    <a href="/" class="green-text" style="margin-left: 20px">Startseite</a>
                    <i class="material-icons">chevron_right</i>
                    <a href="#" class="green-text">{{ title }}</a>
                </div>
            </div>
            <div class="col m2 hide-on-med-and-down"></div>
        </div>

        <div class="row">
            <div class="col m4 l2 hide-on-small-only">
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

            <div class="col s12 m8">
                <div class="card-panel white" style="min-height: 800px">
                    <h5>{{ title }}</h5>
                    <br>
                    <slot></slot>
                </div>
            </div>

            <div class="col m2 hide-on-med-and-down">
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
    props: ['title', 'plan', 'history', 'euSa']
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