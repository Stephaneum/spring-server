
<#-- display something before vue has been fully loaded -->

<#--
    <@vueLoader.text/> for displaying a text while loading
    <@vueLoader.blank/> for diplaying an empty screen while loading
-->

<#macro text string="App wird geladen...">
    <div id="vue-loader-init" style="position: fixed; z-index: 999; left: 0; top: 0;width: 100%; height: 100%; background-color: #f0f0f0; display: flex; align-items: center; justify-content: center;">
        <h3>${string}</h3>
    </div>
    <script type="text/javascript">
        window.onload = function(){
            var show = function() {
                document.getElementById("vue-loader-init").style.display = "none";
            };
            setTimeout(show, 350);
        };
    </script>
</#macro>

<#macro blank>
    <div id="vue-loader-init" style="position: fixed; z-index: 999; left: 0; top: 0;width: 100%; height: 100%; background-color: #f0f0f0;">
    </div>
    <script type="text/javascript">
        window.onload = function(){
            document.getElementById("vue-loader-init").style.display = "none";
        };
    </script>
</#macro>