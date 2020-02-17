
<#-- links on the left for plan and events -->

<#macro render>
    <template id="quick-links">
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

            <a href="/termine.xhtml">
                <div class="quick-button card" style="padding:20px 5px 20px 10px;margin-top:30px">
                    <div class="white-text">
                        <h6 style="margin:0"><i class="material-icons left" style="font-size:15pt">date_range</i>Termine</h6>
                    </div>
                </div>
            </a>
        </div>
    </template>

    <script type="text/javascript">
        Vue.component('quick-links', {
            props: ['plan'],
            mounted: function() {
                console.log(this);
            },
            template: '#quick-links'
        });
    </script>

    <style>
        .quick-button {
            background: #1b5e20;
        }

        .quick-button:hover {
            background: #2e7d32;
        }
    </style>
</#macro>