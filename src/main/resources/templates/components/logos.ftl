<#macro render>
    <template id="logos">
        <div style="text-align: center">
            <!-- history -->
            <a :href="historyLink">
                <img src="/static/img/logo-steph.png" style="width: 150px" alt="logo"/>
            </a>

            <img src="/static/img/europaschule.png" style="margin-top: 50px;" alt="logo"/>

            <a href="http://www.unesco.de/bildung/ups.html" target="_blank">
                <img src="/static/img/unesco.png" style="margin-top: 50px;" alt="logo"/>
            </a>

            <img src="/static/img/sor.png" style="margin-top: 50px;" alt="logo"/>

            <a :href="euSaLink">
                <img src="/static/img/investor-scl.png" style="margin-top: 50px;" alt="logo"/>
            </a>
        </div>
    </template>

    <script type="text/javascript">
        Vue.component('logos', {
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
            },
            template: '#logos'
        });
    </script>
</#macro>