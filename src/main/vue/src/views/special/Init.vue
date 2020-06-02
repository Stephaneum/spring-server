<template>
  <div style="min-height: 100vh; padding-top: 100px; display: flex; align-items: flex-start; justify-content: center">
    <div>
      <div style="display: flex; align-items: center; justify-content: center">
        <img src="../../assets/img/favicon.png" style="width: 60px"/>
        <span style="margin-left: 30px; font-size: 3rem; color: #396e3a">Erste Einrichtung</span>
      </div>

      <div style="margin-top: 100px; width: 80vw;">
        <TabBar :tabs="tabs" :curr-tab="currTab" @selected="setTab"></TabBar>
        <div style="width: 100%; margin-top: 0" class="card-panel">

          <!-- welcome -->
          <div v-show="currTab.id === 0" style="text-align: center">
            <h4>Willkommen!</h4>
            <br>
            <p style="font-size: 1.2rem">Anscheinend bist du gerade dabei, auf einem neuen Server die Stephaneum-Homepage zu installieren.</p>
            <p style="font-size: 1.2rem">Bitte wähle einer der beiden Modis aus, um fortzufahren:</p>
            <div style="margin: 50px 0 50px 0; display: flex; justify-content: space-evenly">

              <a class="waves-effect btn-large green darken-3"
                 @click="openBackupTab">
                <i class="material-icons left">history</i>
                Vom Backup laden
              </a>

              <a class="waves-effect btn-large green darken-3"
                 @click="openNewInstanceTab">
                <i class="material-icons left">star</i>
                Leere Instanz erstellen
              </a>
            </div>

          </div>

          <!-- backup -->
          <div v-show="currTab.id === 1">
            <h4>Backup laden</h4>
            <br>
            <p>Bitte gib die beiden Speicherorte an und lade das bestehende Backup hoch.</p>

            <div class="row" style="margin-top: 40px">
              <div class="col s6">
                <div class="input-field">
                  <i class="material-icons prefix">folder</i>
                  <label for="backup-file-location">Pfad zum Speicher</label>
                  <input v-model="fileLocation" type="text" id="backup-file-location" placeholder="/home/nutzer/dateien oder C:\Users\nutzer\dateien" autocomplete="off"/>
                </div>

                <div class="input-field" style="margin-top: 50px">
                  <i class="material-icons prefix">history</i>
                  <label for="backup-backup-location">Pfad zu den Backups</label>
                  <input v-model="backupLocation" type="text" id="backup-backup-location" placeholder="/home/nutzer/backups oder C:\Users\nutzer\backups" autocomplete="off"/>
                </div>
              </div>

              <div class="col s6" style="padding-top: 50px; text-align: center">
                <FileUpload url="/api/init/load-backup" @start="updateLocations" @upload="uploaded" @error="error" v-slot:default="slot">
                  <a @click="slot.upload" class="btn-large waves-effect green darken-3">
                    <i class="material-icons left">cloud_upload</i>
                    Backup hochladen
                  </a>
                </FileUpload>
              </div>
            </div>

          </div>

          <!-- new instance -->
          <div v-show="currTab.id === 2">
            <h4>Neue Instanz</h4>
            <br>
            <p>Bitte fülle die Textfelder aus, um eine neue leere Instanz zu starten.</p>

            <div class="row" style="margin-top: 40px">
              <div class="col s6">
                <div class="input-field">
                  <i class="material-icons prefix">folder</i>
                  <label for="new-file-location">Pfad zum Speicher</label>
                  <input v-model="fileLocation" type="text" id="new-file-location" placeholder="/home/nutzer/dateien oder C:\Users\nutzer\dateien" autocomplete="off"/>
                </div>

                <div class="input-field" style="margin-top: 50px">
                  <i class="material-icons prefix">history</i>
                  <label for="new-backup-location">Pfad zu den Backups</label>
                  <input v-model="backupLocation" type="text" id="new-backup-location" placeholder="/home/nutzer/backups oder C:\Users\nutzer\backups" autocomplete="off"/>
                </div>
                <br>
                <h5>Admin-Account</h5>

                <div class="row" style="margin-top: 30px">
                  <div class="input-field col s6">
                    <i class="material-icons prefix">person</i>
                    <label for="new-first-name">Vorname</label>
                    <input v-model="firstName" type="text" id="new-first-name" placeholder="Max" autocomplete="off"/>
                  </div>
                  <div class="input-field col s6">
                    <label for="new-last-name">Nachname</label>
                    <input v-model="lastName" type="text" id="new-last-name" placeholder="Mustermann" autocomplete="off"/>
                  </div>
                  <div class="input-field col s12">
                    <i class="material-icons prefix">email</i>
                    <label for="new-email">E-Mail</label>
                    <input v-model="email" type="text" id="new-email" placeholder="m.mustermann@email.de" autocomplete="off"/>
                  </div>
                  <div class="input-field col s6">
                    <i class="material-icons prefix">vpn_key</i>
                    <label for="new-password">Passwort</label>
                    <input v-model="password" type="password" id="new-password" placeholder="123" autocomplete="off"/>
                  </div>
                  <div class="input-field col s6">
                    <label for="new-password-repeat">Passwort wiederholen</label>
                    <input v-model="passwordRepeat" type="password" id="new-password-repeat" placeholder="123" autocomplete="off"/>
                  </div>
                  <div class="input-field col s12">
                    <i class="material-icons prefix">person</i>
                    <select v-model="gender">
                      <option value="0">männlich</option>
                      <option value="1">weiblich</option>
                      <option value="2">keine Angabe</option>
                    </select>
                    <label>Geschlecht</label>
                  </div>
                </div>

              </div>

              <div class="col s6" style="padding-top: 50px; text-align: center">
                <a @click="createNewInstance" class="btn-large waves-effect green darken-3">
                  <i class="material-icons left">add</i>
                  Instanz erstellen
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script>

  import Axios from "axios";
  import TabBar from "../../components/TabBar";
  import M from "materialize-css";
  import FileUpload from "../../components/FileUpload";

  const tabs = [
    { id: 0, icon: 'home', special: true, name: 'Willkommen' },
    { id: 1, icon: 'history', name: 'Backup laden' },
    { id: 2, icon: 'star', name: 'Neue Instanz' }
  ];

  export default {
    name: 'Init',
    components: {FileUpload, TabBar},
    data() {
      return {
        tabs: tabs,
        currTab: tabs[0],

        fileLocation: null,
        backupLocation: null,
        firstName: null,
        lastName: null,
        email: null,
        password: null,
        passwordRepeat: null,
        gender: 2
      }
    },
    methods: {
      setTab(tab) {
        this.currTab = tab;
        this.$nextTick(() => {
          M.updateTextFields();
        });
      },
      openBackupTab() {
        this.setTab(tabs[1]);
      },
      openNewInstanceTab() {
        this.setTab(tabs[2]);
      },
      async updateLocations() {
        try {
          await Axios.post('/api/init/locations', {
            fileLocation: this.fileLocation,
            backupLocation: this.backupLocation
          });
        } catch (e) {
          M.toast({ html: 'Pfade konnten nicht gesetzt werden.' });
        }
      },
      async uploaded() {
        await this.$router.push('/status/restoring').catch(() => {});
      },
      error() {
        M.toast({ html: 'Datei konnte nicht hochgeladen werden.' });
      },
      async createNewInstance() {

        if(this.password !== this.passwordRepeat) {
          M.toast({ html: 'Passwörter stimmen nicht überein.' });
          return;
        }

        try {
          await Axios.post('/api/init/new-instance', {
            fileLocation: this.fileLocation,
            backupLocation: this.backupLocation,
            firstName: this.firstName,
            lastName: this.lastName,
            email: this.email,
            password: this.password,
            gender: this.gender
          });
          await this.$emit('update-info');
          await this.$router.push('/');
        } catch (e) {
          M.toast({ html: 'Ein Fehler ist aufgetreten.' });
        }
      }
    },
    mounted() {
      this.$nextTick(() => {
        M.AutoInit();
      });
    }
  }
</script>