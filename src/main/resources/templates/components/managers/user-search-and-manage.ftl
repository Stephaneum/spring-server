
<#-- batch actions for users -->

<#macro render>
    <template id="user-search-and-manage">
        <div class="card-panel" style="margin-top: 60px;">
            <div style="display: flex">
                <span style="font-size: 2rem">Nutzersuche</span>

                <div style="flex: 1;">
                    <div style="display: flex; align-items: center; justify-content: space-evenly">
                        Inputs
                    </div>
                </div>
            </div>
        </div>
    </template>

    <script type="text/javascript">
        Vue.component('user-search-and-manage', {
            props: [],
            data: function () {
                return {
                }
            },
            methods: {
            },
            mounted: function() {
                this.$nextTick(() => {
                    M.Modal.init(document.querySelectorAll('.modal'), {});
                });
            },
            template: '#user-search-and-manage'
        });
    </script>
</#macro>