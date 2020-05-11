
<#-- batch actions for users -->

<#macro render>
    <template id="user-batch-actions">
        <div style="margin-top: 60px; display: flex; align-items: center; justify-content: space-evenly">
            <div class="batch-btn card">
                <i class="material-icons">vpn_key</i>
                <span>Standard-Passwort</span>
            </div>
            <div class="batch-btn card">
                <i class="material-icons">storage</i>
                <span>Quotas ändern</span>
            </div>
            <div class="batch-btn card">
                <i class="material-icons">delete</i>
                <span>Nutzer löschen</span>
            </div>
        </div>
    </template>

    <script type="text/javascript">
        Vue.component('user-batch-actions', {
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
            template: '#user-batch-actions'
        });
    </script>

    <style>
        .batch-btn {
            background-color: #1b5e20;
            color: #ffffff;
            min-width: 210px;
            cursor: pointer;
            padding: 10px 15px 10px 15px;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
        }

        .batch-btn:hover {
            background-color: #388e3c !important;
        }

        .batch-btn i {
            font-size: 3rem;
        }

        .batch-btn span {
            font-size: 1.4rem;
        }
    </style>
</#macro>