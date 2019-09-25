<#macro render>
    <div id="vue-loader-init" style="position: fixed; z-index: 999; left: 0; top: 0;width: 100%; height: 100%; display: flex; background-color: #f0f0f0; align-items: center; justify-content: center">
        <h3>Applikation wird geladen...</h3>
    </div>
    <script type="text/javascript">
        window.onload = function(){
            var show = function() {
                document.getElementById("vue-loader-init").style.display = "none";
            };
            setTimeout(show, 500);
        };

    </script>
</#macro>