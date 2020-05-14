<template>
  <div v-if="allowed" class="internal-container">

    <InternalHeader title="Benutzerdefinierte Seiten" icon="note_add"></InternalHeader>

    <div class="card-panel">
      <div style="display: flex; align-items: center; justify-content: space-evenly; margin-top: 10px; margin-bottom: 40px">
        <div style="background-color: #f8f8f8; display: flex; align-items: center; border-radius: 20px; padding: 10px 30px 10px 20px">
          <i class="material-icons" style="font-size: 3rem">info</i>
          <div style="margin-left: 20px;">
            <p style="margin: 0">https://stephaneum.de/s/<b>hallo-welt.html</b></p>
            <p style="margin: 0" class="grey-text">greift auf folgende Datei zu:</p>
            <p style="margin: 0">{{staticPath}}/<b>hallo-welt.html</b></p>
            <p style="margin: 0" class="grey-text">Dieser Ordner wird periodisch gescannt. Neue Dateien werden automatisch hinzugefügt.</p>
          </div>
        </div>

        <div style="text-align: center">
          <p style="margin: 0;font-size: 3em; font-weight: bold">{{ pages.length }}</p>
          <p style="margin: 0;font-size: 1.5em">Seiten aktiv</p>
        </div>

        <file-upload url="/api/static/upload" @upload="fetchData" @error="uploadError" v-slot:default="slot">
          <a @click="slot.upload" class="tooltipped waves-effect waves-light btn-floating green darken-4"
             data-tooltip="HTML-Datei hochladen" data-position="top" href="#!">
            <i class="material-icons">arrow_upward</i>
          </a>
        </file-upload>
      </div>

      <ul v-if="pages.length !== 0" class="collection">
        <li v-for="p in pages" :key="p.id" class="collection-item">
          <div style="display: flex; align-items: center; justify-content: space-between">
            <span style="display: flex; align-items: center">
                <i class="material-icons grey-text text-darken-2">description</i>
                <span style="margin-left: 10px">{{ p.path }}</span>
                <span class="green-badge-light" style="margin-left: 20px">{{ mode(p.mode) }}</span>
            </span>

            <span>
              <a class="waves-effect waves-light btn blue-grey"
                 @click="toggleMode(p.id)" :class="{ disabled: waitingForData }">
                  <i class="material-icons left">brush</i>Modus ändern
              </a>
              <a class="tooltipped waves-effect waves-light btn green darken-3" data-tooltip="Herunterladen" data-position="top" :href="'/s/'+p.path+'?download=true'" style="margin-left: 10px">
                  <i class="material-icons">arrow_downward</i>
              </a>
              <a class="tooltipped waves-effect waves-light btn green darken-3" data-tooltip="Öffnen" data-position="top" :href="'/s/'+p.path" target="_blank" style="margin-left: 10px">
                  <i class="material-icons">open_in_new</i>
              </a>
              <a class="tooltipped waves-effect waves-light btn red darken-3" data-tooltip="Löschen" data-position="top" style="margin-left: 10px"
                 @click="deletePage(p.id)" :class="{ disabled: waitingForData }">
                  <i class="material-icons">delete</i>
              </a>
              </span>
          </div>
        </li>
      </ul>
      <div v-if="pages.length === 0" style="height: 300px; display: flex; align-items: center; justify-content: center">
        <span class="green-badge-light">Keine Einträge</span>
      </div>
    </div>
    <div style="height: 100px"></div>
  </div>
</template>

<script>
  import Axios from "axios"
  import M from "materialize-css"
  import FileUpload from '@/components/FileUpload.vue'
  import InternalHeader from "../components/InternalHeader";

  export default {
    name: 'PlanManager',
    components: {
      InternalHeader,
      FileUpload
    },
    props: ['info'],
    data: () => ({
      staticPath: null,
      pages: [],
      waitingForData: true,
    }),
    methods: {
      toggleMode: async function(id) {
        this.waitingForData = true;
        try {
          await Axios.post('/api/static/toggle-mode/'+id);
          await this.fetchData();
        } catch (e) {
          M.toast({ html: 'Ein Fehler ist aufgetreten.' });
          this.waitingForData = false;
        }
      },
      deletePage: async function(id) {
        this.waitingForData = true;
        try {
          await Axios.post('/api/static/delete/'+id);
          M.toast({ html: 'Gelöscht.' });
          await this.fetchData();
        } catch (e) {
          M.toast({ html: 'Ein Fehler ist aufgetreten.' });
          this.waitingForData = false;
        }
      },
      uploadError: function(status) {
        switch (status) {
          case 409:
            M.toast({html: 'Nur HTML-Dateien erlaubt.'});
            break;
          case 410:
            M.toast({html: 'Dateinamen vergeben.'});
            break;
          default:
            M.toast({html: 'Ein Fehler ist aufgetreten.'});
            break;
        }
      },
      fetchData: async function() {
        try {
          const response = await Axios.get('/api/static');
          this.staticPath = response.data.staticPath;
          this.pages = response.data.pages;
          this.waitingForData = false;
          this.$nextTick(() => M.Tooltip.init(document.querySelectorAll('.tooltipped'), {}));
        } catch (e) {
          M.toast({ html: 'Ein Fehler ist aufgetreten.' });
        }
      }
    },
    computed: {
      allowed: function() {
        return this.info.user && this.info.user.code.role === 100
      },
      mode: function () {
        return (mode) => {
          switch(mode) {
            case 'MIDDLE': return 'eingebettet, mittig';
            case 'FULL_WIDTH': return 'eingebettet, voll';
            case 'FULL_SCREEN': return 'original';
          }
        };
      },
    },
    mounted: async function() {
      await this.fetchData();
      M.AutoInit();
    }
  }
</script>