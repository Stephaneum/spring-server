
<#-- batch actions for users -->

<#macro render>
    <template id="user-search-and-manage">
        <div class="card-panel" style="margin-top: 60px;">
            <div style="display: flex; align-items: center; justify-content: space-evenly">
                <div class="input-field" style="margin-top: 40px">
                    <label for="input-search-firstname">Vorname</label>
                    <input v-model:value="firstName" type="text" id="input-search-firstname" autocomplete="off" placeholder="z.B. Max"/>
                </div>

                <div class="input-field" style="margin-top: 40px">
                    <label for="input-search-lastname">Nachname</label>
                    <input v-model:value="lastName" type="text" id="input-search-lastname" autocomplete="off" placeholder="z.B. Mustermann"/>
                </div>

                <div class="input-field" style="margin-top: 40px">
                    <i class="material-icons prefix">people</i>
                    <select v-model:value="role">
                        <option value="null" selected>Alle</option>
                        <option value="0">Schüler</option>
                        <option value="1">Lehrer</option>
                        <option value="2">Gast</option>
                    </select>
                    <label>Rolle</label>
                </div>

                <a class="btn-large waves-effect waves-light green darken-4">
                    <i class="material-icons left">search</i>
                    Suchen
                </a>
            </div>
            <div style="margin-top: 20px; height: 500px; border: #e0e0e0 solid 1px; overflow-y: scroll">
                <h1>Hallo</h1>
                <h1>Hallo</h1>
                <h1>Hallo</h1>
                <h1>Hallo</h1>
                <h1>Hallo</h1>
            </div>
        </div>
    </template>

    <script type="text/javascript">
        Vue.component('user-search-and-manage', {
            props: [],
            data: function () {
                return {
                    firstName: null,
                    lastName: null,
                    role: null
                }
            },
            methods: {
            },
            mounted: function() {
                this.$nextTick(() => {
                    M.updateTextFields();
                    M.Modal.init(document.querySelectorAll('.modal'), {});
                    M.FormSelect.init(document.querySelectorAll('select'), {});
                });
            },
            template: '#user-search-and-manage'
        });
    </script>
</#macro>