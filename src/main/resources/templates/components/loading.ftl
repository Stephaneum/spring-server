<#macro render>
    <div id="modal-loading" style="display: none; position: fixed; z-index: 999; left: 0; top: 0;width: 100%; height: 100%; overflow: auto; background-color: rgb(0,0,0); background-color: rgba(0,0,0,0.4);">
        <div style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center">
            <div style="text-align: center; padding: 25px; border-radius: 10px; background-color: white">
                <h4 id="modal-loading-text" style="margin: 0">Verarbeitung...</h4>
                <div class="progress" style="margin-top: 25px">
                    <div id="modal-loading-bar" class="indeterminate"></div>
                </div>
            </div>
        </div>
    </div>
    <script type="text/javascript">
        function showLoading(text = 'Verarbeitung...', percent = null) {
            document.getElementById("modal-loading-text").innerHTML = text;
            document.getElementById("modal-loading").style.display = "block";

            if(percent) {
                document.getElementById("modal-loading-bar").className = "determinate";
                document.getElementById("modal-loading-bar").style.width = percent+"%";
            } else {
                document.getElementById("modal-loading-bar").className = "indeterminate";
                document.getElementById("modal-loading-bar").style.width = "auto";
            }
        }

        function hideLoading() {
            document.getElementById("modal-loading").style.display = "none";
        }
    </script>
</#macro>