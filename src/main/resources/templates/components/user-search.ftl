
<#-- search users -->

<#macro render>
    <template id="user-search">
        <div>
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
            <ul class="collection">
                <li v-for="u in results" class="collection-item">
                    <div style="display: flex; align-items: center;">
                        <span style="flex-grow: 1; white-space: nowrap; overflow-x: hidden; text-overflow: ellipsis;">
                            {{ u.firstName }} {{ u.lastName }} {{ u.schoolClass? '('+u.schoolClass+')' : '' }}
                        </span>
                        <span style="flex: 0 0 200px; text-align: right">
                            <a v-if="u.excluded" class="btn-flat">
                                <i class="material-icons left">check</i>{{ excludedString }}
                            </a>
                            <a v-else @click="select(u)" class="waves-effect waves-light btn green darken-4">
                                <i class="material-icons left">check</i>{{ actionString }}
                            </a>
                        </span>
                    </div>
                </li>
            </ul>
        </div>
    </template>

    <script type="text/javascript">
        Vue.component('user-search', {
            props: ['excluded', 'actionString', 'excludedString'],
            data: function () {
                return {
                    firstName: null,
                    lastName: null,
                    results: []
                }
            },
            methods: {
                reset: function() {
                    this.firstName = null;
                    this.lastName = null;
                    this.results = [];
                },
                search: async function() {
                    try {
                        showLoadingInvisible();
                        const response = await axios.post('/api/search/user', { firstName: this.firstName, lastName: this.lastName });
                        response.data.forEach((u) => {
                           u.excluded = this.excluded.some((e) => u.id === e.id);
                        });
                        this.results = response.data;
                        hideLoading();
                    } catch (e) {
                        M.toast({html: 'Ein Fehler ist aufgetreten.'});
                    }
                },
                select: function(u) {
                    this.$emit('onselect', u);
                }
            },
            computed: {
            },
            template: '#user-search'
        });
    </script>
</#macro>