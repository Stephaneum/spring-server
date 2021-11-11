<template>
  <div class="card-panel" style="margin-top: 60px;">
    <div style="display: flex; align-items: center; justify-content: space-evenly">
      <div class="input-field" style="margin-top: 40px">
        <label for="input-search-firstname">Vorname</label>
        <input @keyup.enter="search" v-model="firstName" type="text" id="input-search-firstname" autocomplete="off" placeholder="z.B. Max"/>
      </div>

      <div class="input-field" style="margin-top: 40px">
        <label for="input-search-lastname">Nachname</label>
        <input @keyup.enter="search" v-model="lastName" type="text" id="input-search-lastname" autocomplete="off" placeholder="z.B. Mustermann"/>
      </div>

      <div class="input-field" style="margin-top: 40px">
        <i class="material-icons prefix">people</i>
        <select v-model="role">
          <option value="null" selected>Alle</option>
          <option value="0">Schüler</option>
          <option value="1">Lehrer</option>
          <option value="2">Gast</option>
        </select>
        <label>Rolle</label>
      </div>

      <a @click="search" class="btn-large waves-effect waves-light green darken-4">
        <i class="material-icons left">search</i>
        Suchen
      </a>
    </div>
    <div v-show="result.length !== 0" style="margin-top: 20px; height: 500px; border: #e0e0e0 solid 1px; overflow-y: scroll">
      <table class="striped">
        <thead>
        <tr>
          <th style="padding-left: 20px">Vorname</th>
          <th>Nachname</th>
          <th>E-Mail</th>
          <th>Klasse</th>
          <th>Speicher</th>
          <th>Rolle</th>
          <th>Aktionen</th>
        </tr>
        </thead>

        <tbody>
        <tr v-for="u in result" :key="u.id">
          <td style="padding-left: 20px">{{ u.firstName }}</td>
          <td>{{ u.lastName }}</td>
          <td>{{ u.email }}</td>
          <td>{{ u.schoolClass }}</td>
          <td>{{ u.storageReadable }}</td>
          <td>{{ roleString(u.role) }}</td>
          <td>
            <a @click="showUpdateUser(u)" class="btn waves-effect waves-light green darken-4">
              <i class="material-icons">edit</i>
            </a>
            <a @click="showChangeAccount(u)" class="btn waves-effect waves-light teal darken-3" style="margin-left: 10px">
              <i class="material-icons">swap_horiz</i>
            </a>
            <a @click="showDeleteAccount(u)" class="btn waves-effect waves-light red darken-3" style="margin-left: 10px">
              <i class="material-icons">delete</i>
            </a>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
    <div v-show="result.length === 0" class="empty-hint" style="height: 200px; display: flex; align-items: center; justify-content: center">
      {{ hasSearched ? 'Keine Ergebnisse' : 'Noch keine Suchergebnisse' }}
    </div>

    <!-- update user data -->
    <div id="modal-search-update-user" class="modal" style="width: 500px">
      <div class="modal-content">
        <h4>{{ roleString(selected.role) }} verwalten</h4>
        <br>
        <div class="row">
          <div class="input-field col s6">
            <i class="material-icons prefix">person</i>
            <label for="input-user-firstname">Vorname</label>
            <input v-model="selected.firstName" type="text" id="input-user-firstname" autocomplete="off"/>
          </div>
          <div class="input-field col s6">
            <label for="input-user-lastname">Nachname</label>
            <input v-model="selected.lastName" type="text" id="input-user-lastname" autocomplete="off"/>
          </div>
        </div>
        <div class="row">
          <div class="input-field col s12">
            <i class="material-icons prefix">email</i>
            <label for="input-user-email">E-Mail</label>
            <input v-model="selected.email" type="text" id="input-user-email" autocomplete="off"/>
          </div>
        </div>
        <div class="row">
          <div class="input-field col s6">
            <i class="material-icons prefix">vpn_key</i>
            <label for="input-user-password">Passwort</label>
            <input v-model="selected.password" type="password" id="input-user-password" autocomplete="off" placeholder="(unverändert)"/>
          </div>
          <div class="input-field col s6">
            <i class="material-icons prefix">storage</i>
            <label for="input-user-storage">Speicherplatz (in MB)</label>
            <input v-model="selected.storage" type="text" id="input-user-storage" autocomplete="off"/>
          </div>
        </div>
        <div style="padding-left: 20px; display: flex; align-items: center">
          <i class="material-icons">security</i>
          <span style="margin-left: 10px; font-weight: bold">Berechtigungen</span>
        </div>
        <div style="margin-top: 20px; display: flex; align-items: center; justify-content: space-evenly">
          <a @click="selected.permissionLogin = !selected.permissionLogin" href="#!" class="waves-effect btn darken-2" :class="selected.permissionLogin ? ['green'] : ['grey']">Login</a>
          <a @click="selected.permissionPlan = !selected.permissionPlan" href="#!" class="waves-effect btn darken-2" :class="selected.permissionPlan ? ['green'] : ['grey']">Vertretungsplan ändern</a>
          <a href="/menu-manager" class="waves-effect waves-green btn teal">Menü & Beiträge</a>
        </div>
      </div>
      <div class="modal-footer">
        <a href="#!"
           class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>

        <button @click="updateUser" type="button" class="btn waves-effect waves-light green darken-3">
          <i class="material-icons left">save</i>
          Speichern
        </button>
      </div>
    </div>

    <!-- change account -->
    <div id="modal-change-account" class="modal" style="width: 500px">
      <div class="modal-content">
        <h4>Account wechseln</h4>
        <br>
        Sie wechseln zum Account von <b>{{ selected.firstName }} {{ selected.lastName }}</b>.
        <br><br>
        <b>Hinweis:</b> Nach dem Wechsel ist ein erneuter Login notwendig, um in den Ursprungsaccount zurückzukommen.
        <br>
      </div>
      <div class="modal-footer">
        <a href="#!" class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
        <button @click="changeAccount" type="button" class="btn waves-effect waves-light green darken-3">
          <i class="material-icons left">swap_horiz</i>
          Wechseln
        </button>
      </div>
    </div>

    <!-- delete account -->
    <div id="modal-delete-account" class="modal" style="width: 500px">
      <div class="modal-content">
        <h4>Account löschen</h4>
        <br>
        Wollen Sie den Account von <b>{{ selected.firstName }} {{ selected.lastName }}</b> löschen?
        <br><br>
        <b>Hinweis: Alle Dateien von dieser Personen werden ebenfalls gelöscht!</b>
        <br>
      </div>
      <div class="modal-footer">
        <a href="#!" class="modal-close waves-effect btn-flat">Abbrechen</a>
        <button @click="deleteAccount" type="button" class="btn waves-effect waves-light red darken-3">
          <i class="material-icons left">delete</i>
          Löschen
        </button>
      </div>
    </div>
  </div>
</template>

<script>
  // @ is an alias to /src
  import Axios from "axios"
  import M from "materialize-css"
  import { showLoading, showLoadingInvisible, hideLoading, storageReadable } from '@/helper/utils.js';

  export default {
    name: 'UserSearchAndManage',
    data: () => ({
      firstName: null,
      lastName: null,
      role: null,
      result: [],
      selected: {},
      hasSearched: false
    }),
    methods: {
      search: async function() {
        showLoading('Daten laden...');
        try {
          const response = await Axios.post('/api/search/user?detailed=true', { firstName: this.firstName, lastName: this.lastName, role: this.role });
          this.result = response.data;
          this.result.forEach((r) => {
            r.storageReadable = storageReadable(r.storage);
          });
          hideLoading();
          this.hasSearched = true;
          M.toast({html: this.result.length +' Nutzer'});
        } catch (e) {
          M.toast({html: 'Ein Fehler ist aufgetreten.'});
          hideLoading();
        }
      },
      showUpdateUser: async function(user) {
        this.selected = user;
        M.Modal.getInstance(document.getElementById('modal-search-update-user')).open();

        const response = await Axios.get('/api/users/' + this.selected.id);
        this.selected = response.data;
        this.selected.storage /= (1024 * 1024); // B to MB
        this.selected.password = null;
        this.selected.id = user.id;
        this.$nextTick(() => {
          M.updateTextFields();
        });
      },
      updateUser: async function() {
        showLoadingInvisible();
        try {
          await Axios.post('/api/users/update', {
            user: this.selected.id,
            firstName: this.selected.firstName,
            lastName: this.selected.lastName,
            email: this.selected.email,
            password: this.selected.password,
            storage: parseInt(this.selected.storage) * 1024 * 1024,
            permissionLogin: this.selected.permissionLogin,
            permissionPlan: this.selected.permissionPlan
          });
          M.toast({html: 'Änderungen übernommen'});
          M.Modal.getInstance(document.getElementById('modal-search-update-user')).close();
          await this.search();
        } catch (e) {
          M.toast({html: 'Ein Fehler ist aufgetreten.'});
          hideLoading();
        }
      },
      showChangeAccount: async function(user) {
        this.selected = user;
        M.Modal.getInstance(document.getElementById('modal-change-account')).open();
      },
      changeAccount: async function() {
        showLoading('Account wechseln...');
        try {
          await Axios.post('/api/users/change-account/' + this.selected.id);
          window.location.href = '/home';
        } catch (e) {
          M.toast({html: 'Ein Fehler ist aufgetreten.'});
          hideLoading();
        }
      },
      showDeleteAccount: async function(user) {
        this.selected = user;
        M.Modal.getInstance(document.getElementById('modal-delete-account')).open();
      },
      deleteAccount: async function() {
        showLoading('Account löschen...');
        try {
          await Axios.post('/api/users/delete-account/' + this.selected.id);
          await this.search();
          M.toast({html: 'Account gelöscht.'});
          M.Modal.getInstance(document.getElementById('modal-delete-account')).close();
        } catch (e) {
          M.toast({html: 'Ein Fehler ist aufgetreten.'});
        } finally {
          hideLoading();
        }
      }
    },
    computed: {
      roleString: function () {
        return (role) => {
          switch(role) {
            case 0: return "Schüler/in";
            case 1: return "Lehrer/in";
            case 2: return "Gast";
            case 100: return "Admin";
            default: return null;
          }
        }
      }
    },
    mounted: function() {
      this.$nextTick(() => {
        M.updateTextFields();
        M.Modal.init(document.querySelectorAll('.modal'), {});
        M.FormSelect.init(document.querySelectorAll('select'), {});
      });
    }
  }
</script>

<style scoped>
  .import-panel .input-field {
    margin-top: 40px;
  }
</style>