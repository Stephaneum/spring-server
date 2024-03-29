<template>
  <div class="card-panel" style="margin-top: 60px">
    <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px">
      <span style="font-size: 2rem">Diashow</span>
      <file-upload url="/api/slider/upload" @upload="fetchData" @error="uploadError" v-slot:default="slot">
        <a @click="slot.upload" style="margin-right: 20px" class="tooltipped waves-effect waves-light btn-floating green darken-4"
           data-tooltip="Bild hochladen" data-position="top" href="#!">
          <i class="material-icons">add</i>
        </a>
      </file-upload>
    </div>

    <ul v-if="sliders.length !== 0" class="collection">
      <li v-for="s in sliders" :key="s.id" class="collection-item">
        <div style="display: flex; align-items: center;">
          <span style="flex: 0 0 150px; display: flex; align-items: center">
            <img :src="imageURL(s)" style="width: 100%" />
          </span>

          <div v-show="!selectedSlider || s.id !== selectedSlider.id" style="flex: 1; display: flex; align-items: center">
            <span style="flex: 1; font-size: 1.1rem; display: flex; align-items: center">
              <template v-if="s.title || s.subTitle">
                  <a class="tooltipped waves-effect waves-light btn teal darken-3" data-tooltip="Richtung ändern" data-position="top" style="margin-left: 20px"
                     @click="toggleDirection(s)">
                      <i class="material-icons">{{ toArrow(s) }}</i>
                  </a>
              </template>

              <div style="padding-left: 15px">
                  <span style="font-weight: bold">{{ s.title }}</span>
                  <br>
                  <span>{{ s.subTitle }}</span>
              </div>
            </span>

            <a class="tooltipped waves-effect waves-light btn teal darken-3" data-tooltip="nach unten" data-position="top" style="margin-left: 10px"
               @click="down(s)">
              <i class="material-icons">arrow_downward</i>
            </a>
            <a class="tooltipped waves-effect waves-light btn teal darken-3" data-tooltip="nach oben" data-position="top" style="margin-left: 10px"
               @click="up(s)">
              <i class="material-icons">arrow_upward</i>
            </a>
            <a class="tooltipped waves-effect waves-light btn green darken-3" data-tooltip="Text bearbeiten" data-position="top" style="margin-left: 10px"
               @click="selectSlider(s)">
              <i class="material-icons">edit</i>
            </a>
            <a class="tooltipped waves-effect waves-light btn red darken-3" data-tooltip="Löschen" data-position="top" style="margin-left: 10px"
               @click="showDeleteSlider(s)">
              <i class="material-icons">delete</i>
            </a>
          </div>
          <div v-show="selectedSlider && s.id === selectedSlider.id" style="flex: 1; display: flex; align-items: center">
            <!-- selected -->
            <span style="flex: 1; font-size: 1.1rem; display: flex; align-items: center">
                <span style="margin-left: 20px; font-weight: bold">Titel:</span>
                <div class="input-field" style="margin: 0 0 0 10px;">
                    <input v-model="s.title" type="text"/>
                </div>

                <span style="margin-left: 30px; font-weight: bold">Info:</span>
                <div class="input-field" style="margin: 0 0 0 10px;">
                    <input v-model="s.subTitle" type="text"/>
                </div>
            </span>

            <a class="tooltipped waves-effect waves-light btn green darken-3" data-tooltip="Fertig" data-position="top" style="margin-left: 10px"
               @click="saveSlider">
              <i class="material-icons">check</i>
            </a>
          </div>

        </div>
      </li>
    </ul>

    <!-- delete slider modal -->
    <div id="modal-delete-slider" class="modal" style="width: 500px">
      <div class="modal-content">
        <h4>Bild löschen</h4>
        <br>
        Soll das Bild gelöscht werden?
        <br>
      </div>
      <div class="modal-footer">
        <a href="#!"
           class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
        <button @click="deleteSlider" type="button" class="btn waves-effect waves-light red darken-3">
          <i class="material-icons left">delete</i>
          Löschen
        </button>
      </div>
    </div>
  </div>
</template>

<script>
  import Axios from "axios"
  import M from "materialize-css"
  import FileUpload from '@/components/FileUpload.vue'
  import { showLoadingInvisible, hideLoading } from '@/helper/utils.js';

  export default {
    name: 'SliderManager',
    components: {
      FileUpload
    },
    data: () => ({
      sliders: [],
      selectedSlider: null
    }),
    methods: {
      fetchData: async function() {
        const sliders = await Axios.get('/api/slider');
        this.sliders = sliders.data;
        hideLoading();
        this.$nextTick(() => M.Tooltip.init(document.querySelectorAll('.tooltipped'), {}));
      },
      uploadError: function(status) {
        switch (status) {
          case 409:
            M.toast({html: 'Nur JPEG-Dateien erlaubt.'});
            break;
          default:
            M.toast({html: 'Ein Fehler ist aufgetreten.'});
            break;
        }
      },
      up: async function(slider) {
        showLoadingInvisible();
        await Axios.post('/api/slider/down/' + slider.id);
        await this.fetchData();
      },
      down: async function(slider) {
        showLoadingInvisible();
        await Axios.post('/api/slider/up/' + slider.id);
        await this.fetchData();
      },
      selectSlider: function(slider) {
        this.selectedSlider = slider;
        this.$nextTick(() => {
          M.updateTextFields();
        });
      },
      toggleDirection: async function(slider) {
        showLoadingInvisible();
        try {
          await Axios.post('/api/slider/direction/' + slider.id);
          await this.fetchData();
          M.toast({html: 'Richtung geändert.'});
        } catch (e) {
          hideLoading();
          M.toast({html: 'Ein Fehler ist aufgetreten.'});
        }
      },
      saveSlider: async function() {
        try {
          await Axios.post('/api/slider/update', {
            id: this.selectedSlider.id,
            title: this.selectedSlider.title,
            subTitle: this.selectedSlider.subTitle
          });
          await this.fetchData();
          this.selectedSlider = null;
          M.toast({html: 'Text aktualisiert.'});
        } catch (e) {
          hideLoading();
          M.toast({html: 'Ein Fehler ist aufgetreten.'});
        }
      },
      showDeleteSlider: function(slider) {
        this.selectedSlider = slider;
        M.Modal.getInstance(document.getElementById('modal-delete-slider')).open();
      },
      deleteSlider: async function() {
        showLoadingInvisible();
        try {
          await Axios.post('/api/slider/delete/' + this.selectedSlider.id);
          await this.fetchData();
          M.toast({html: 'Element gelöscht.'});
          M.Modal.getInstance(document.getElementById('modal-delete-slider')).close();
        } catch (e) {
          M.toast({html: 'Ein Fehler ist aufgetreten.'});
          hideLoading();
        }
      }
    },
    computed: {
      imageURL: function() {
        return (slider) => '/files/slider/'+slider.name;
      },
      toArrow: function() {
        return (slider) => {
          switch(slider.direction) {
            case 'center-align':
              return 'arrow_downward';
            case 'right-align':
              return 'arrow_back';
            case 'left-align':
              return 'arrow_forward';
            default:
              return null;
          }
        }
      }
    },
    mounted: async function() {
      await this.fetchData();
      this.$nextTick(() => {
        M.Modal.init(document.querySelectorAll('.modal'), {});
      });
    }
  }
</script>