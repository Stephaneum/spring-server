<template>
  <div v-if="allowed" class="internal-container" style="max-width: 900px">

    <InternalHeader title="Vertretungsplan" icon="description"></InternalHeader>

    <div class="card-panel" style="display: flex; padding: 30px 0 30px 30px">
      <div style="flex: 50%">
        <div class="round-area">
          <i class="material-icons">description</i>
          <div>
            <h5 style="margin-bottom: 10px">PDF-Datei</h5>
            <file-upload url="/api/plan/upload" @upload="uploaded" @error="uploadError" v-slot:default="slot">
              <a @click="slot.upload" class="waves-effect waves-light tooltipped green darken-3 btn"
                 data-tooltip="Hochladen" data-position="bottom">
                <i class="material-icons">cloud_upload</i>
              </a>
            </file-upload>
            <a class="waves-effect waves-light tooltipped red darken-3 btn" style="margin-left: 10px"
               @click="showDelete" :disabled="!info.plan.exists" data-tooltip="Löschen" data-position="bottom">
              <i class="material-icons">delete</i>
            </a>
            <br>
            <span v-if="lastModified" class="info-text">Stand: {{ lastModified }}</span>
            <span v-else class="info-text">(keine Datei bereitgestellt)</span>
          </div>
        </div>
        <div class="round-area" style="margin-top: 30px">
          <i class="material-icons">info</i>
          <div>
            <h5>Zusatzinformation</h5>
            <div style="display: flex; align-items: center;">
              <div class="input-field" style="width: 200px; margin-bottom: 0">
                <input v-model="planInfo" type="text" />
              </div>
              <a v-show="planInfo !== info.plan.info" style="margin-left: 20px" class="waves-effect waves-light tooltipped green darken-3 btn"
                 @click="updateText" data-tooltip="Speichern" data-position="bottom">
                <i class="material-icons">save</i>
              </a>
            </div>
          </div>
        </div>
        <div class="round-area" style="margin-top: 30px">
          <i class="material-icons">vpn_key</i>
          <div>
            <h5>Passwort</h5>
            <div style="display: flex; align-items: center;">
              <div class="input-field" style="width: 200px; margin-bottom: 0">
                <input v-model="planPassword" type="text" />
              </div>
              <a v-show="planPassword !== originalPlanPassword" style="margin-left: 20px" class="waves-effect waves-light tooltipped green darken-3 btn"
                 @click="updatePassword" data-tooltip="Speichern" data-position="bottom">
                <i class="material-icons">save</i>
              </a>
            </div>
          </div>
        </div>
      </div>
      <div style="flex: 50%; display: flex; align-items: center; justify-content: center; flex-direction: column">
        <h5 style="text-align: center; margin-bottom: 30px">Vorschau</h5>
        <div v-if="info.plan.exists" style="width: 330px">
          <a href="vertretungsplan.pdf" target="_blank">
            <div class="quick-button card">
              <div class="card-content white-text">
                <div class="row" style="margin-bottom:0">
                  <div class="col s12 m12 l8">
                    <span class="card-title">Vertretungsplan</span>
                    <p>{{ planInfo }}</p>
                  </div>
                  <div class="col l4 right-align hide-on-med-and-down">
                    <i id="quick-icon" class="material-icons" style="font-size:50pt">description</i>
                  </div>
                </div>
              </div>
            </div>
          </a>
        </div>
        <span class="green-badge-light" style="font-size: 1em; margin-top: 20px" v-else>ausgeblendet</span>
      </div>
    </div>

    <!-- delete modal -->
    <div id="modal-delete" class="modal">
      <div class="modal-content">
        <h4>PDF-Datei wirklich löschen?</h4>
        <p>Dieser Vorgang kann nicht rückgangig gemacht werden.</p>
      </div>
      <div class="modal-footer">
        <a @click="closeDelete" href="#!" class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
        <a @click="doDelete" href="#!" class="modal-close waves-effect waves-red btn red darken-4">
          <i class="material-icons left">delete</i>
          Löschen
        </a>
      </div>
    </div>
  </div>
</template>

<script>
  import Axios from "axios"
  import M from "materialize-css"
  import FileUpload from '@/components/FileUpload.vue'
  import { showLoadingInvisible, hideLoading } from '@/helper/utils.js';
  import InternalHeader from "../../components/InternalHeader";

  export default {
    name: 'PlanManager',
    components: {
      InternalHeader,
      FileUpload
    },
    props: ['info'],
    data: () => ({
      planInfo: null,
      planPassword: null,
      originalPlanPassword: null,
      lastModified: null
    }),
    methods: {
      showUpload: function() {
        document.getElementById('upload-pdf').click();
      },
      uploaded: async function() {
        await this.$emit('update-info');
        await this.fetchData();
      },
      uploadError: function(e) {
        switch(e) {
          case 403:
            M.toast({ html: 'Keine Schreibrechte' });
            break;
          case 409:
            M.toast({ html: 'Nur PDF-Dateien erlaubt' });
            break;
          case 500:
            M.toast({ html: 'Interner Fehler' });
            break;
        }
      },
      showDelete: function(boardID, boardType) {
        this.boardDelete = { boardID, boardType };
        M.Modal.getInstance(document.getElementById('modal-delete')).open();
      },
      closeDelete: function() {
        M.Modal.getInstance(document.getElementById('modal-delete')).close();
      },
      doDelete: async function() {
        showLoadingInvisible();
        try {
          await Axios.post('/api/plan/delete');
          await this.$emit('update-info');
          await this.fetchData();
          M.toast({ html: 'PDF Gelöscht.' });
        } catch (e) {
          M.toast({ html: 'Ein Fehler ist aufgetreten.' });
        } finally {
          hideLoading();
        }
      },
      updateText: async function() {
        showLoadingInvisible();
        try {
          await Axios.post('/api/plan/text?text='+this.planInfo);
          await this.$emit('update-info');
          M.toast({ html: 'Text aktualisiert.' });
        } catch (e) {
          M.toast({ html: 'Ein Fehler ist aufgetreten.' });
        } finally {
          hideLoading();
        }
      },
      updatePassword: async function() {
        showLoadingInvisible();
        try {
          await Axios.post('/api/plan/password?password='+this.planPassword);
          M.toast({ html: 'Passwort aktualisiert.' });
          await this.fetchData();
        } catch (e) {
          M.toast({ html: 'Ein Fehler ist aufgetreten.' });
        } finally {
          hideLoading();
        }
      },
      fetchData: async function() {
        try {
          const lastModified = await Axios.get('/api/plan/last-modified');
          this.lastModified = lastModified.data.lastModified;

          const password = await Axios.get('/api/plan/password');
          this.planPassword = password.data.password;
          this.originalPlanPassword = this.planPassword;

          hideLoading();
          this.$nextTick(() => M.Tooltip.init(document.querySelectorAll('.tooltipped'), {}));
        } catch (e) {
          switch(e.response.status) {
            case 409:
              this.lastModified = null;
              break;
            default:
              M.toast({ html: 'Ein Fehler ist aufgetreten.' });
              break;
          }
        }
      }
    },
    computed: {
      allowed: function() {
        return this.info.user && (this.info.user.code.role === 100 || this.info.user.managePlans)
      }
    },
    watch: {
      info: {
        immediate: true,
        handler(newVal) {
          if(newVal && newVal.plan)
            this.planInfo = newVal.plan.info;
        },
      },
    },
    mounted: async function() {
      await this.fetchData();
      M.AutoInit();
    }
  }
</script>

<style scoped>
  .round-area {
    display: flex;
    align-items: center;
    background-color: #e8f5e9;
    border-radius: 20px;
    padding: 20px;
  }

  .round-area > div > h5 {
    margin: 0;
  }

  .round-area > i {
    font-size: 4em;
    margin-right: 20px;
  }

  .info-text {
    display: inline-block;
    font-style: italic;
    margin-top: 10px
  }

  .quick-button {
    background: #1b5e20;
  }

  .quick-button:hover {
    background: #2e7d32;
  }
</style>