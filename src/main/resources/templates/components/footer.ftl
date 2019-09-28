<#macro render>
    <template id="template-footer">
        <footer class="page-footer" style="background-color: #2a4c2c">
            <div class="container center-align">
                <a href="kontakt.xhtml" class="green-text text-lighten-3">Kontakt</a>
                <span class="white-text">|</span>
                <a href="impressum.xhtml" class="green-text text-lighten-3">Impressum</a>
                <span class="white-text">|</span>
                <a href="sitemap.xhtml" class="green-text text-lighten-3">Sitemap</a>
                <br/><br/>
                <span v-html="copyright" style="font-size:12pt;word-wrap: break-word;"></span>
                <br/><br/>
            </div>
        </footer>
    </template>

    <script type="text/javascript">
        Vue.component('stephaneum-footer', {
            props: ['copyright'],
            template: '#template-footer'
        });
    </script>

    <style>
    </style>
</#macro>