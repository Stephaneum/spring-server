<#macro render>
    <div id="modal-loading" style="display: none; position: fixed; z-index: 1; left: 0; top: 0;width: 100%; height: 100%; overflow: auto; background-color: rgb(0,0,0); background-color: rgba(0,0,0,0.4);">
        <div style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center">
            <div style="text-align: center; padding: 25px; border-radius: 10px; background-color: white">
                <h4 style="margin: 0">Verarbeitung...</h4>
                <div class="progress" style="margin-top: 25px">
                    <div class="indeterminate"></div>
                </div>
            </div>
        </div>
    </div>
    <script type="text/javascript">
        function loading() {
            document.getElementById("modal-loading").style.display = "block";
        }
    </script>
</#macro>