
<#-- search users -->

<#macro render>
    <template id="user-search">
        <div style="display: flex">
            <div style="flex: 1; margin: 0" class="row">
                <div class="input-field col s6">
                    <label for="user-search-firstName">Vorname</label>
                    <input @keyup.enter="search" v-model:value="firstName" type="text" id="user-search-firstName"/>
                </div>
                <div class="input-field col s6">
                    <label for="user-search-lastName">Nachname</label>
                    <input @keyup.enter="search" v-model:value="lastName" type="text" id="user-search-lastName"/>
                </div>
            </div>
            <div style="flex: 0 0 150px; display: flex; align-items: center; justify-content: center">
                <a @click="search" class="waves-effect waves-light btn green darken-3">
                    <i class="material-icons left">search</i>Suchen
                </a>
            </div>
        </div>
    </template>

    <script type="text/javascript">
        Vue.component('user-search', {
            props: [],
            data: function () {
                return {
                    firstName: null,
                    lastName: null,
                }
            },
            methods: {
                search: async function() {
                    try {
                        showLoadingInvisible();
                        const response = await axios.post('/api/search/user', { firstName: this.firstName, lastName: this.lastName });
                        this.$emit('result', response.data);
                    } catch (e) {
                        M.toast({html: 'Ein Fehler ist aufgetreten.'});
                    } finally {
                        hideLoading();
                    }
                }
            },
            template: '#user-search'
        });
    </script>
</#macro>