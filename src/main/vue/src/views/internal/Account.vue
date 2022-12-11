<template>
  <div v-if="allowed" class="internal-container">

    <InternalHeader title="Account" icon="account_circle"></InternalHeader>

    <div class="row">
      <div class="col s4" style="padding: 20px">
        <div class="card-panel">
          <div class="account-section-header">
            <i class="material-icons">info</i>
            <span>Informationen</span>
          </div>
          <div style="display: grid; column-gap: 10px; row-gap: 10px; grid-template-columns: min-content auto; grid-template-rows: auto auto auto auto auto auto">
            <div style="grid-area: 1 / 1 / 1 / 1;" class="account-key">Vorname:</div>
            <div style="grid-area: 1 / 2 / 1 / 2;">{{ info.user.firstName }}</div>
            <div style="grid-area: 2 / 1 / 2 / 1;" class="account-key">Nachname:</div>
            <div style="grid-area: 2 / 2 / 2 / 2;">{{ info.user.lastName }}</div>
            <div style="grid-area: 3 / 1 / 3 / 1;" class="account-key">E-Mail:</div>
            <div style="grid-area: 3 / 2 / 3 / 2;">{{ info.user.email }}</div>
            <div style="grid-area: 4 / 1 / 4 / 1;" class="account-key">Rolle:</div>
            <div style="grid-area: 4 / 2 / 4 / 2;">{{ info.user.code.roleString }}</div>
            <div style="grid-area: 5 / 1 / 5 / 1;" class="account-key">Klasse:</div>
            <div style="grid-area: 5 / 2 / 5 / 2;">{{ additionalInfo.schoolClass }}</div>
            <div style="grid-area: 6 / 1 / 6 / 1;" class="account-key">Speicherplatz:</div>
            <div style="grid-area: 6 / 2 / 6 / 2;">{{ additionalInfo.storage }}</div>
            <div style="grid-area: 7 / 1 / 7 / 1;" class="account-key">Typ:</div>
            <div style="grid-area: 7 / 2 / 7 / 2;">{{ info.user.openId ? 'OpenID Connect' : 'Manuell' }}</div>
          </div>
        </div>
      </div>

      <div class="col s4" style="padding: 20px" v-if="!info.user.openId">
        <div class="card-panel">
          <div class="account-section-header">
            <i class="material-icons">email</i>
            <span>E-Mail ändern</span>
          </div>
          <div class="input-field">
            <label for="account-email">E-Mail</label>
            <input v-model="email" type="text" id="account-email"/>
          </div>

          <div style="text-align: right">
            <a class="waves-effect waves-light btn green darken-3" :class="email ? [] : ['disabled']"
               @click="updateEmail">
              <i class="material-icons left">save</i>
              Speichern
            </a>
          </div>
        </div>
      </div>

      <div class="col s4" style="padding: 20px" v-if="!info.user.openId">
        <div class="card-panel">
          <div class="account-section-header">
            <i class="material-icons">vpn_key</i>
            <span>Passwort ändern</span>
          </div>
          <div class="input-field">
            <label for="account-old-password">altes Passwort</label>
            <input v-model="oldPassword" type="password" id="account-old-password"/>
          </div>
          <div class="input-field">
            <label for="account-new-password">neues Passwort</label>
            <input v-model="newPassword" type="password" id="account-new-password"/>
          </div>
          <div class="input-field">
            <label for="account-new-password-repeat">neues Passwort wiederholen</label>
            <input v-model="newPasswordRepeat" type="password" id="account-new-password-repeat"/>
          </div>

          <div style="text-align: right">
            <a class="waves-effect waves-light btn green darken-3" :class="oldPassword && newPassword && newPasswordRepeat ? [] : ['disabled']"
               @click="updatePassword">
              <i class="material-icons left">save</i>
              Speichern
            </a>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
  import Axios from "axios"
  import M from "materialize-css"
  import { storageReadable, showLoadingInvisible, hideLoading } from '@/helper/utils.js';
  import InternalHeader from "../../components/InternalHeader";

  export default {
    name: 'Account',
    components: {InternalHeader},
    props: ['info'],
    data: () => ({
      additionalInfo: { schoolClass: null, used: 1, total: 1, storage: null },
      email: null,
      oldPassword: null,
      newPassword: null,
      newPasswordRepeat: null
    }),
    methods: {
      fetchData: async function() {
        try {
          const additional = await Axios.get('/api/account/info');
          this.additionalInfo = additional.data;
          this.additionalInfo.storage = storageReadable(additional.data.used) + ' / ' + storageReadable(additional.data.total);
        } catch (e) {
          M.toast({html: 'Ein Fehler ist aufgetreten.'});
        }

        hideLoading();
      },
      updateEmail: async function() {

        if(!this.email) {
          M.toast({html: 'Leere E-Mail'});
          return;
        }

        showLoadingInvisible();
        try {
          await Axios.post('/api/account/email', { email: this.email });
          await this.fetchData();
          await this.$emit('update-info');
          M.toast({html: 'E-Mail geändert.'});
          this.email = null;
          this.$nextTick(() => M.updateTextFields());
        } catch (e) {
          switch (e.response.status) {
            case 409:
              M.toast({html: 'E-Mail bereits in Verwendung.'});
              break;
            case 410:
              M.toast({html: 'E-Mail ist nicht gültig.'});
              break;
            default:
              M.toast({html: 'Ein Fehler ist aufgetreten.'});
              break;
          }
          hideLoading();
        }
      },
      updatePassword: async function() {

        if(this.newPassword !== this.newPasswordRepeat) {
          M.toast({html: 'Passwörter stimmen nicht überein.'});
          return;
        }

        showLoadingInvisible();
        try {
          await Axios.post('/api/account/password', { oldPassword: this.oldPassword, newPassword: this.newPassword });
          await this.fetchData();
          M.toast({html: 'Passwort geändert.'});
          this.oldPassword = null;
          this.newPassword = null;
          this.newPasswordRepeat = null;
          this.$nextTick(() => M.updateTextFields());
        } catch (e) {
          switch (e.response.status) {
            case 403:
              M.toast({html: 'Falsches Passwort.'});
              break;
            default:
              M.toast({html: 'Ein Fehler ist aufgetreten.'});
              break;
          }
          hideLoading();
        }
      }
    },
    computed: {
      allowed: function() {
        return this.info.user && this.info.user.code.role >= 0;
      }
    },
    mounted: function() {
      M.AutoInit();
      this.fetchData();
    }
  }
</script>

<style scoped>
  .account-section-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
  }

  .account-section-header span {
    margin-left: 10px;
    font-size: 1.5rem;
  }

  .account-key {
    font-weight: bold;
  }
</style>