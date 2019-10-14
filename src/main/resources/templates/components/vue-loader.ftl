<#macro text string="App wird geladen...">
    <div id="vue-loader-init" style="position: fixed; z-index: 999; left: 0; top: 0;width: 100%; height: 100%; background-color: #f0f0f0; display: flex; align-items: center; justify-content: center; flex-direction: column">
        <h3 id="vue-loader-text-1">${string}</h3>
        <h4 id="vue-loader-text-2"></h4>
        <h4 id="vue-loader-text-3"></h4>
    </div>
    <script type="text/javascript">

        if(window.navigator.userAgent.indexOf('msie') !== -1 || window.navigator.userAgent.indexOf('trident') !== -1) {
            document.getElementById("vue-loader-text-1").innerHTML = '';
            document.getElementById("vue-loader-text-2").innerHTML = 'Diese Seite wird von Internet Explorer (noch) nicht unterstützt.';
            document.getElementById("vue-loader-text-3").innerHTML = 'Bitte wechseln Sie zu den Alternativen (Chrome, Firefox, Safari, Edge, ...).';
        } else {
            window.onload = function(){
                var show = function() {
                    document.getElementById("vue-loader-init").style.display = "none";
                };
                setTimeout(show, 350);
            };
        }


    </script>
</#macro>

<#macro blank>
    <div id="vue-loader-init" style="position: fixed; z-index: 999; left: 0; top: 0;width: 100%; height: 100%; background-color: #f0f0f0; display: flex; align-items: center; justify-content: center; flex-direction: column">
        <h3 id="vue-loader-text-1"></h3>
        <h4 id="vue-loader-text-2"></h4>
        <h4 id="vue-loader-text-3"></h4>
    </div>
    <script type="text/javascript">
        if(window.navigator.userAgent.indexOf('msie') !== -1 || window.navigator.userAgent.indexOf('trident') !== -1) {
            document.getElementById("vue-loader-text-2").innerHTML = 'Diese Seite wird von Internet Explorer (noch) nicht unterstützt.';
            document.getElementById("vue-loader-text-3").innerHTML = 'Bitte wechseln Sie zu den Alternativen (Chrome, Firefox, Safari, Edge, ...).';
        } else {
            window.onload = function(){
                document.getElementById("vue-loader-init").style.display = "none";
            };
        }
    </script>
</#macro>