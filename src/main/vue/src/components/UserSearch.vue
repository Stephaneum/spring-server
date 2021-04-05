<template>
    <div style="display: flex">
        <div style="flex: 1; margin: 0" class="row">
            <div class="input-field col s4">
                <label for="user-search-firstName">Vorname</label>
                <input @keyup.enter="search" v-model="firstName" type="text" id="user-search-firstName"/>
            </div>
            <div class="input-field col s4">
                <label for="user-search-lastName">Nachname</label>
                <input @keyup.enter="search" v-model="lastName" type="text" id="user-search-lastName"/>
            </div>
            <div class="input-field col s4">
              <label for="user-search-class">Klasse</label>
              <input @keyup.enter="search" v-model="className" type="text" id="user-search-class"/>
            </div>
        </div>
        <div style="flex: 0 0 150px; display: flex; align-items: center; justify-content: center">
            <a @click="search" class="waves-effect waves-light btn green darken-3">
                <i class="material-icons left">search</i>Suchen
            </a>
        </div>
    </div>
</template>

<script>
    import Axios from "axios"
    import M from "materialize-css"
    import { showLoadingInvisible, hideLoading } from '@/helper/utils.js';

    export default {
        name: 'UserSearch',
        data: () => ({
            firstName: null,
            lastName: null,
            className: null
        }),
        methods: {
            search: async function() {
                try {
                    showLoadingInvisible();

                    let classGrade;
                    let classSuffix;
                    if (this.className) {
                      const match = this.className.match(/(\d+)(\D+\d*)?/);
                      classGrade = parseInt(match[1]);
                      classSuffix = match[2];
                    }

                    const response = await Axios.post('/api/search/user', {
                      firstName: this.firstName,
                      lastName: this.lastName,
                      classGrade,
                      classSuffix
                    });
                    this.$emit('result', response.data);
                } catch (e) {
                    console.error(e);
                    M.toast({html: 'Ein Fehler ist aufgetreten.'});
                } finally {
                    hideLoading();
                }
            }
        }
    }
</script>

<style>

</style>