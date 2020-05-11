
<#-- batch actions for users -->

<#macro render>
    <template id="user-import">
        <div class="card-panel" style="margin-top: 60px;">
            <span style="font-size: 2rem">Nutzerimport</span>
        </div>
    </template>

    <script type="text/javascript">
        Vue.component('user-import', {
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
            template: '#user-import'
        });
    </script>
</#macro>