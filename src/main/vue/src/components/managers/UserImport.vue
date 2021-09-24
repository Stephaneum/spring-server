<template>
  <div class="import-panel card-panel" style="margin-top: 60px;">
    <div style="display: flex; align-items: center; justify-content: space-between">
      <span style="font-size: 2rem">Nutzerimport</span>

      <a @click="importUsers" class="btn-large waves-effect waves-light green darken-4">
        <i class="material-icons left">arrow_forward</i>
        Importieren
      </a>
    </div>

    <p>Erfahrungsgemäß müssen beim Schüler-Import nur Klasse, Vorname und Nachname ausgewählt werden.</p>

    <div class="row">
      <div class="input-field col s2" style="padding-left: 0">
        <i class="material-icons prefix">people</i>
        <select v-model="role">
          <option value="0">Schüler</option>
          <option value="1">Lehrer</option>
          <option value="2">Gast</option>
        </select>
        <label>Rolle</label>
      </div>

      <div class="input-field col s3">
        <select v-model="separator">
          <option value=";">Semikolon [ ; ] (empfohlen)</option>
          <option value="/">Schrägstrich [ / ]</option>
          <option value="tab">Tab [&emsp;]</option>
        </select>
        <label>Trennzeichen</label>
      </div>

      <div class="input-field col s7">
        <select v-model="format">
          <option value="0">Vorname | Nachname | Anmeldename | Passwort | Klasse</option>
          <option value="1">Anmeldename | Anrede | Nachname | Vorname</option>
          <option value="2">Klasse | Nachname | Vorname</option>
          <option value="3">Klasse | Vorname | Nachname</option>
          <option value="4">Vorname | Nachname | Klasse</option>
          <option value="5">Nachname | Vorname | Klasse</option>
        </select>
        <label>Format</label>
      </div>
    </div>

    <div class="input-field" :style="parseInt(format) === 0 ? {'display': 'none'} : {}">
      <i class="material-icons prefix">vpn_key</i>
      <label for="input-import-pw">Standard-Passwort</label>
      <input v-model="password" type="text" id="input-import-pw" autocomplete="off" placeholder="z.B. schule"/>
    </div>

    <div class="input-field">
      <i class="material-icons prefix">person</i>
      <label for="import-example">Beispiel</label>
      <input :value="example" type="text" id="import-example" autocomplete="off"/>
    </div>

    <textarea v-model="data" style="height: 600px; padding: 10px; border:solid 1px #c9c9c9; resize: none; font-family: Consolas, monospace" :placeholder="example + '\n' + example2 + '\n' + example3 + '\n[...]'" ></textarea>
  </div>
</template>

<script>
  // @ is an alias to /src
  import Axios from "axios"
  import M from "materialize-css"
  import { showLoading, hideLoading } from '@/helper/utils.js';

  export default {
    name: 'UserManager',
    data: () => ({
      role: 0,
      format: 0,
      separator: ';',
      password: null,
      data: null
    }),
    methods: {
      importUsers: async function() {

        if(!this.data) {
          M.toast({html: 'Keine Daten'});
          return;
        }

        let separator = this.separator;
        if(separator === 'tab') {
          separator = '\t';
        }

        showLoading('Nutzer importieren...')
        try {
          await Axios.post('/api/users/import', {
            data: this.data,
            format: this.format,
            separator: separator,
            password: this.password,
            role: this.role
          });
          hideLoading();
          M.toast({html: 'Nutzer importiert'});
        } catch (e) {
          switch (e.response.status) {
            case 409:
              M.toast({html: `E-Mails sind vergeben.<br>${e.response.data?.message}`});
              break;
            case 410:
              M.toast({html: 'Syntax fehlerhaft.'});
              break;
            case 412:
              M.toast({html: 'Format nicht für Schüler verfügbar.'});
              break;
            case 417:
              M.toast({html: 'Standard-Passwort fehlt.'});
              break;
            case 418:
              M.toast({html: `Syntax der Klassen fehlerhaft.<br>${e.response.data?.message}`});
              break;
            default:
              M.toast({html: 'Ein Fehler ist aufgetreten.'});
              break;
          }
          hideLoading();
        }
      },
      generateExample: function(firstName, lastName, login, password, clazz, salutation) {
        let s = this.separator;
        if(s === 'tab') {
          s = '   ';
        }
        switch(parseInt(this.format)) {
          case 0: return firstName + s + lastName + s + login + s + password + s + clazz;
          case 1: return login + s + salutation + s + lastName + s + firstName;
          case 2: return clazz + s + lastName + s + firstName;
          case 3: return clazz + s + firstName + s + lastName;
          case 4: return firstName + s + lastName + s + clazz;
          case 5: return lastName + s + firstName + s + clazz;
          default: return 'Error';
        }
      }
    },
    computed: {
      example: function() {
        return this.generateExample('Max', 'Mustermann', 'm.mustermann', 'meinPasswort', '7c', 'Herr');
      },
      example2: function() {
        return this.generateExample('Sabine', 'Meier', 's.meier', 'eule', '11-2', 'Frau');
      },
      example3: function() {
        return this.generateExample('Tom', 'Müller', 't.müller', 'schule', '9a', 'Herr');
      }
    },
    mounted: function() {
      this.$nextTick(() => {
        M.updateTextFields();
      });
    },
  }
</script>

<style scoped>
  .import-panel .input-field {
    margin-top: 40px;
  }
</style>