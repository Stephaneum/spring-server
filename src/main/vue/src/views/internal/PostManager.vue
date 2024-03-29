<template>
  <div v-if="currMode" class="internal-container row" style="max-width: 1600px">

    <InternalHeader :title="currMode.description" :icon="currMode.icon" left="true"></InternalHeader>
    <div id="post-manager-title"></div>

    <!-- TABS -->
    <div class="col s10 offset-s2">
      <tab-bar :tabs="currTabs" :curr-tab="currTab" @selected="setTab"></tab-bar>
    </div>
    <!-- MODES -->
    <div class="col s2">
      <div v-for="m in modes" :key="'mode'+m.id" @click="setMode(m)" class="mode-btn z-depth-1" :class="{ 'mode-btn-active': m.id === currMode.id }">
        <i style="font-size: 3em" class="material-icons">{{ m.icon }}</i>
        <span style="font-size: 1.5em">{{ m.name }}</span>
      </div>
    </div>
    <div class="col s10">

      <!-- SELECT-EDIT -->
      <div v-show="currTab.id === tabs.selectEdit.id" class="tab-panel white z-depth-1">
        <div v-if="admin || info.hasMenuWriteAccess">
          <h5 style="margin-bottom: 20px">Gruppe auswählen:</h5>
          <div class="grey-round-border">
            <Menu :menu="writableMenu" unreal="true" @select="fetchPosts"></Menu>
          </div>
        </div>

        <div v-if="currTab.id === tabs.selectEdit.id && currSelection.menu && currSelection.posts.length !== 0" style="margin-top: 50px">
          <!-- post list for a menu -->
          <post-list :name="currSelection.menu.name" :posts="currSelection.posts" :selected="currPost.id"
                     @selected="selectPost" @updated="fetchPosts(currSelection.menu)"></post-list>
        </div>
        <div v-else-if="currTab.id === tabs.selectEdit.id && !currSelection.menu && currSelection.posts.length !== 0" style="margin-top: 50px">
          <!-- post list of unapproved posts-->
          <post-list name="Noch nicht genehmigte Beiträge" :posts="currSelection.posts" :selected="currPost.id"
                     :hide_password="true"
                     @selected="selectPost" @updated="fetchPosts"></post-list>
        </div>
        <div v-else style="text-align: center; margin-top: 100px">
          <h5>Keine Beiträge verfügbar.</h5>
        </div>
      </div>

      <!-- SELECT-APPROVE -->
      <div v-show="currTab.id === tabs.selectApprove.id" class="tab-panel white z-depth-1">
        <div v-if="currTab.id === tabs.selectApprove.id && currSelection.posts.length !== 0" style="margin-top: 30px">
          <post-list name="Noch nicht genehmigte Beiträge" :posts="currSelection.posts" :selected="currPost.id"
                     :hide_password="true"
                     @selected="selectPost" @updated="fetchPosts"></post-list>
        </div>
        <div v-else style="text-align: center; margin-top: 100px">
          <h5>Keine Beiträge verfügbar.</h5>
        </div>
      </div>

      <!-- SELECT-SPECIAL -->
      <div v-show="currTab.id === tabs.selectSpecial.id" class="tab-panel white z-depth-1" style="padding: 100px 0 0 0">

        <div style="display: flex; align-items: center; justify-content: space-evenly;">

          <div class="special-container">
            <i class="material-icons">code</i>
            <h5>Daten</h5>
            <div v-for="s in specialData" :key="s.id">
              <a @click="selectSpecial(s)" :class="s.id === currPost.id ? ['special-btn-active'] : []" class="special-btn waves-effect waves-light btn-large">
                {{ s.name }}
              </a>
            </div>
          </div>

          <div class="special-container">
            <i class="material-icons">dashboard</i>
            <h5>Fragmente</h5>
            <div v-for="s in specialFragments" :key="s.id">
              <a @click="selectSpecial(s)" :class="s.id === currPost.id ? ['special-btn-active'] : []" class="special-btn waves-effect waves-light btn-large">
                {{ s.name }}
              </a>
            </div>
          </div>

          <div class="special-container">
            <i class="material-icons">description</i>
            <h5>Seiten</h5>
            <div v-for="s in specialSites" :key="s.id">
              <a @click="selectSpecial(s)" :class="s.id === currPost.id ? ['special-btn-active'] : []" class="special-btn waves-effect waves-light btn-large">
                {{ s.name }}
              </a>
            </div>
          </div>

        </div>
      </div>

      <!-- TEXT SPECIAL -->
      <div v-show="currTab.id === tabs.textSpecial.id " class="tab-panel white z-depth-1">
        <div style="display: flex; align-items: center; justify-content: space-between">
          <div>
            <h5>{{ specialObj.name }}</h5>
            <span v-html="specialObj.info"></span>
          </div>
          <a class="waves-effect waves-light btn-large" style="font-size: 1.5em;background-color: #1b5e20;"
             @click="sendSpecial">
            <i class="material-icons left">save</i>
            Speichern
          </a>
        </div>

        <div v-show="!specialObj.plain">
          <trumbowyg v-model="currPost.text" :config="TEXT_EDITOR_CONFIG" style="height: 600px"></trumbowyg>
        </div>

        <div v-show="specialObj.plain">
          <br>
          <textarea v-model="currPost.text" style="height: 600px; padding: 10px; border:solid 1px #c9c9c9; resize: none; font-family: Consolas, monospace" placeholder="(leer)" ></textarea>
        </div>
      </div>

      <!-- TEXT -->
      <div v-show="currTab.id === tabs.text.id " class="tab-panel white z-depth-1">
        <div class="input-field" style="max-width: 700px">
          <label for="post-title">Titel</label>
          <input v-model="currPost.title" type="text" id="post-title" style="font-weight: bold"/>
        </div>
        <trumbowyg v-model="currPost.text" :config="TEXT_EDITOR_CONFIG" style="height: 600px"></trumbowyg>
      </div>

      <!-- IMAGES -->
      <div v-show="currTab.id === tabs.images.id " class="tab-panel white z-depth-1" style="display: flex; align-items: center;">
        <div style="width: 100%">
          <div @drop="uploadImages" @dragover="imageDragEnter" @dragleave="imageDragExit" style="display: flex; align-items: center; background-color: #e8f5e9; border-radius: 20px; padding: 10px" :style="imageDragging ? {'border': '5px dashed #4caf50' } : {}">
            <div v-show="!imageDragging" style="flex: 0 0 180px; padding-right: 10px; text-align: center">
              <p style="font-size: 3em; font-weight: bold; margin: 0">{{ currPost.imagesAdded.length }}</p>
              <h5 style="margin: 0;">
                Ausgewählt
              </h5>
              <form method="POST" enctype="multipart/form-data" style="margin-top: 30px;">
                <input name="file" type="file" id="upload-image" @change="uploadImages" style="display: none" multiple>

                <a class="waves-effect waves-light tooltipped btn" style="background-color: #607d8b"
                   @click="deselectAllImages" :disabled="currPost.imagesAdded.length === 0" data-tooltip="Zurücksetzen" data-position="bottom">
                  <i class="material-icons">block</i>
                </a>
                <a class="waves-effect waves-light tooltipped btn" style="background-color: #1b5e20; margin-left: 10px"
                   @click="showUpload" data-tooltip="Hochladen" data-position="bottom">
                  <i class="material-icons">cloud_upload</i>
                </a>
              </form>
            </div>
            <div v-if="!imageDragging" id="container-images-added" class="container-images">
              <div v-for="(i, index) in currPost.imagesAdded" :key="index" @click="deselectImage(i)" class="container-image z-depth-1">
                <span class="image-number">{{ index+1 }}</span>
                <img :src="imageURL(i)" height="150"/>
                <p style="margin: 0">{{i.fileName}}</p>
                <p class="image-time">{{i.time}} ~ {{i.sizeReadable}}</p>
              </div>
              <div v-show="currPost.imagesAdded.length === 0" style="height: 100%; display: flex; align-items: center; justify-content: center">
                <p class="green-badge-light" style="display: inline-block; font-size: 1em">Keine Bilder ausgewählt.</p>
              </div>
            </div>
            <div v-else style="flex: 1; height: 240px; display: flex; align-items: center; justify-content: center; font-size: 2em; pointer-events: none;">
              Hier Dateien ablegen
            </div>
          </div>
          <div style="display: flex; align-items: center; margin-top: 50px">
            <h5 style="flex: 0 0 180px; padding-right: 10px; margin: 0; text-align: center;">
              Verfügbar
            </h5>
            <div id="container-images-available" class="container-images">
              <div v-for="i in currPost.imagesAvailable" :key="i.id" @click="selectImage(i)" class="container-image z-depth-1">
                <img :src="imageURL(i)" height="150"/>
                <p style="margin: 0">{{i.fileName}}</p>
                <p class="image-time">{{i.time}} ~ {{i.sizeReadable}}</p>
              </div>
              <div v-show="currPost.imagesAvailable.length === 0" style="height: 100%; display: flex; align-items: center; justify-content: center">
                <p class="green-badge-light" style="display: inline-block; font-size: 1em">Keine Bilder verfügbar.</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- LAYOUT -->
      <div v-show="currTab.id === tabs.layout.id " class="tab-panel white z-depth-1" style="display: flex; align-items: center; justify-content: center">
        <div>
          <h5>Beitrag</h5>
          <div v-for="l in postLayouts" :key="'post'+l" @click="setLayoutPost(l)" style="height: 200px" class="layout-btn z-depth-1" :class="{ 'layout-btn-active': l === currPost.layoutPost }">
            <img :src="require('../../assets/img/layout-post-'+l+'.png')">
          </div>
          <h5>Vorschau</h5>
          <div v-for="l in previewLayouts" :key="'preview'+l" @click="setLayoutPreview(l)" style="height: 100px" class="layout-btn z-depth-1" :class="{ 'layout-btn-active': l === currPost.layoutPreview }">
            <img :src="require('../../assets/img/layout-preview-'+l+'.png')">
          </div>
          <div style="margin-top: 30px" class="input-field">
            <i class="material-icons prefix">crop</i>
            <label for="post-preview-count">Zeichenlänge der Vorschau</label>
            <input v-model="currPost.preview" type="number" id="post-preview-count" min="0" max="1000"/>
          </div>
        </div>
      </div>

      <!-- ASSIGN -->
      <div v-show="currTab.id === tabs.assign.id " class="tab-panel white z-depth-1" style="display: flex; align-items: center; justify-content: center">
        <div style="width: 100%">
          <div style="text-align: center; font-size: 1.5em;">
            <span :style="{ background: !currPost.menu ? '#ffcdd2' : '#e8f5e9' }" style="padding: 20px; border-radius: 20px">
                Zuordnung:
                <span v-html="menuAssigned" style="color: #808080; margin-left: 20px"></span>
            </span>
          </div>
          <div v-if="!admin && !menuAdmin" style="text-align: center; margin-top: 40px">
            <span>Die Zuordnung ist optional. Der Admin ordnet dann diesen Beitrag zu, falls keine Auswahl getätigt wurde.</span>
          </div>
          <div style="height: 60px"></div>
          <div class="grey-round-border">
            <Menu :menu="writableMenu" unreal="true" @select="assignMenu"></Menu>
          </div>

          <div style="height: 300px"></div>
        </div>
      </div>

      <!-- FINALIZE -->
      <div v-show="currTab.id === tabs.finalize.id " class="tab-panel white z-depth-1">
        <div style="display: flex;">
          <div style="flex: 1">
            <div style="border-radius: 20px; margin: 50px; padding: 30px; height: 100px;display: flex;align-items: center" :style="{ background: validationInfoBox.background }">
              <i style="font-size: 3em" class="material-icons">{{ validationInfoBox.icon }}</i>
              <span style="margin-left: 20px; font-size: 1.5em">{{ validationInfoBox.text }}</span>
              <ul v-if="this.currPost.error.error" style="flex: 0 0 130px;margin-left: 20px">
                <li v-if="this.currPost.error.titleEmpty"> - Kein Titel</li>
                <li v-if="this.currPost.error.missingAssignment"> - Zuordnung fehlt</li>
              </ul>
              <ul v-else-if="this.currPost.warning.warning" style="flex: 0 0 200px;margin-left: 20px">
                <li v-if="this.currPost.warning.compressImages"> - Bilder werden komprimiert</li>
              </ul>
            </div>
          </div>
          <div style="flex: 0 0 350px;display: flex; align-items: center; justify-content: center">
            <a class="waves-effect waves-light btn-large" style="font-size: 1.5em;background-color: #1b5e20;"
               @click="sendPost" :disabled="this.currPost.error.error">
              <i class="material-icons left">check_circle</i>
              {{ finalButtonText }}
            </a>
          </div>
        </div>
        <div v-if="currPost.error.titleEmpty" style="margin-top: 100px; text-align: center">
          <h5>Vorschau ist nicht verfügbar.</h5>
        </div>
        <div v-else>
          <div style="margin: auto; max-width: 1050px">
            <h5 style="margin: 0 0 30px 50px">Vorschau (Startseite):</h5>
            <div class="grey-round-border">
              <post-preview postID="-1" :date="currentDate" :title="currPost.title" :text="currPost.text" :preview="parseInt(currPost.preview)" :layout="currPost.layoutPreview" :images="currPost.imagesAdded"></post-preview>
            </div>
          </div>
          <div style="margin: auto; max-width: 1050px">
            <h5 style="margin: 50px 0 30px 50px">Vorschau (Beitrag geöffnet):</h5>
            <div class="grey-round-border">
              <post :date="currentDate" :title="currPost.title" :text="currPost.text" :layout="currPost.layoutPost" :images="currPost.imagesAdded"></post>
            </div>
          </div>
          <div style="height: 50px"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import Axios from "axios"
  import M from "materialize-css"
  import moment from 'moment'
  import Trumbowyg from 'vue-trumbowyg';
  import 'trumbowyg/dist/ui/trumbowyg.css';
  import "../../assets/trumbowyg/trumbowyg.fontsize.min.js";
  import "../../assets/trumbowyg/trumbowyg.colors.min.js";
  import "../../assets/trumbowyg/trumbowyg.colors.min.css";
  import Menu from '@/components/Menu.vue'
  import TabBar from '@/components/TabBar.vue'
  import Post from '@/components/cms/Post.vue'
  import PostList from '@/components/cms/PostList.vue'
  import PostPreview from '@/components/cms/PostPreview.vue'
  import { TEXT_EDITOR_CONFIG, storageReadable, uploadMultipleFiles, showLoading, showLoadingInvisible, hideLoading } from '@/helper/utils.js';
  import { modes, tabs, postLayouts, previewLayouts, specialData, specialFragments, specialSites,  } from '@/helper/postManagerConstants.js';
  import InternalHeader from "../../components/InternalHeader";

  export default {
    name: 'PostManager',
    props: ['info'],
    components: {InternalHeader, Trumbowyg, Menu, TabBar, Post, PostList, PostPreview },
    data: () => ({
      initialized: false,
      currentDate: moment().format('DD.MM.YYYY'),
      maxPictureSize: 0, // will be fetched, image will be compressed if larger than this number
      menuAdmin: false, // just like admin
      writableMenu: [], // will be fetched (like menu but only a part of it)
      modes: modes,
      tabs: tabs,
      postLayouts: postLayouts,
      previewLayouts: previewLayouts,
      specialData: specialData,
      specialFragments: specialFragments,
      specialSites: specialSites,
      specialAll: { ...specialData, ...specialFragments, ...specialSites },
      TEXT_EDITOR_CONFIG: TEXT_EDITOR_CONFIG,
      imagesAvailable: [], // will be fetched
      imagesAvailableLimit: 10,
      imageDragging: 0, // whether or not the drag-and-drop file is over the div
      currMode: null,
      currTabs: [],
      currTab: null,
      currSelection: {
        menu: null,
        posts: []
      },
      currPost: {
        id: null,
        title: null,
        text: '', // editor somehow only clears on '' not null
        imagesAdded: [],
        imagesAvailable: [],
        layoutPost: 0,
        layoutPreview: 0,
        preview: 300,
        menu: null,
        error: {
          error: false,
          titleEmpty: false,
          missingAssignment: false
        },
        warning: {
          warning: false,
          compressImages: false
        }
      }
    }),
    methods: {
      setMode: function(mode) {
        this.resetData();

        switch(mode.id) {
          case modes.create.id:
            this.currTabs = this.admin || this.info.hasMenuWriteAccess ?
                    [tabs.text, tabs.images, tabs.layout, tabs.assign, tabs.finalize] :
                    [tabs.text, tabs.images, tabs.layout, tabs.finalize];
            this.currTab = tabs.text;
            break;
          case modes.edit.id:
            this.currTabs = [tabs.selectEdit];
            this.currTab = tabs.selectEdit;
            if(!this.admin && !this.menuAdmin)
              this.fetchPosts(); // load unapproved posts for non-admins
            break;
          case modes.approve.id:
            this.currTabs = [tabs.selectApprove];
            this.currTab = tabs.selectApprove;
            this.fetchPosts(); // load unapproved posts
            break;
          case modes.special.id:
            this.currTabs = [tabs.selectSpecial];
            this.currTab = tabs.selectSpecial;
            break;
        }
        this.currMode = mode;
        if(!this.initialized) {
          this.postInit();
          this.initialized = true;
        }
      },
      setTab: function(tab) {

        // update Materialize
        if(tab.id === tabs.selectEdit.id || tab.id === tabs.selectApprove.id || tab.id === tabs.selectSpecial.id) {
          this.$nextTick(() => {
            this.$nextTick(() => {
              M.Tooltip.init(document.querySelectorAll('.tooltipped'), {});
              M.Modal.init(document.querySelectorAll('.modal'), {});
              console.log('Materialize update')
            });
          });
        }

        // init scroller and fetch images if not initialized yet
        if (tab.id === tabs.images.id && this.imagesAvailable.length === 0) {
          var instance = this;
          this.fetchImages();

          var containerImagesAdded = document.getElementById('container-images-added');
          containerImagesAdded.addEventListener('wheel', function(e) {
            if (e.deltaY > 0) {
              this.scrollLeft += 50;

              if(this.scrollLeft < this.scrollLeftMax) e.preventDefault();
            } else {
              this.scrollLeft -= 50;

              if(this.scrollLeft > 0) e.preventDefault();
            }
          });

          var containerImagesAvailable = document.getElementById('container-images-available');
          containerImagesAvailable.addEventListener('wheel', function(e) {
            if(instance.currPost.imagesAvailable.length === 0)
              return; // this user has no images

            if (e.deltaY > 0) this.scrollLeft += 50;
            else this.scrollLeft -= 50;

            if(this.scrollLeft >= this.scrollLeftMax) {
              instance.increaseImageLimit();
            }

            if(this.scrollLeft > 0) e.preventDefault();
          });
        }

        // validate, update materialboxed and sliders
        if(tab.id === tabs.finalize.id) {
          var error = this.currPost.error;
          var warning = this.currPost.warning;
          error.error = false;
          error.titleEmpty = false;
          error.missingAssignment = false;
          warning.warning = false;
          warning.compressImages = false;
          if(!this.currPost.title || !this.currPost.title.trim()) {
            error.titleEmpty = true;
            error.error = true;
          }

          if((this.admin || this.menuAdmin) && !this.currPost.menu) {
            error.missingAssignment = true;
            error.error = true;
          }

          if(this.currPost.imagesAdded.some((i) => i.size >= this.maxPictureSize)) {
            warning.compressImages = true;
            warning.warning = true;
          }

          M.Materialbox.init(document.querySelectorAll('.materialboxed'), {});
          M.Slider.init(document.querySelectorAll('.slider'), {});
          console.log('update preview');
        }
        this.currTab = tab;
      },
      resetData: function() {
        this.currPost.id = null;
        this.currPost.title = null;
        this.currPost.text = '';
        this.currPost.imagesAdded = [];
        this.currPost.layoutPost = 0;
        this.currPost.layoutPreview = 0;
        this.currPost.preview = 300;
        this.currPost.menu = null;

        this.currSelection.menu = null;
        this.currSelection.posts = [];
      },
      fetchPosts: function (menu) {
        // if menu is null, then fetch unapproved posts
        showLoading('Lade Beiträge...');
        Axios.get(menu ? '/api/post?noContent=true&menuID='+menu.id : '/api/post?unapproved=true')
                .then((res) => {
                  if(Array.isArray(res.data)) {
                    this.resetData();
                    this.currTabs = [this.currTabs[0]]; // hide the other tabs and only show the first tab

                    res.data.forEach((p, index) => {
                      p.time = moment(p.timestamp).format('DD.MM.YYYY'); // add time
                      p.number = menu ? res.data.length - index : index + 1; // add index
                    });
                    this.currSelection.posts = res.data;
                    this.currSelection.menu = menu;

                    this.$nextTick(() => {
                      M.Tooltip.init(document.querySelectorAll('.tooltipped'), {});
                      M.Modal.init(document.querySelectorAll('.modal'), {});
                    });

                    if(this.admin || this.menuAdmin) {
                      // for admin and menu admin
                      if(this.currMode.id === this.modes.approve.id) {
                        this.info.unapproved = res.data.length;
                        this.modes.approve.name = this.approvedModeText(this.info.unapproved); // update button text
                      }
                    } else {
                      // for everybody else
                      if(this.currMode.id === this.modes.edit.id) {
                        this.info.unapproved = res.data.length;
                        this.modes.edit.name = this.editModeText(this.info.unapproved); // update button text
                      }
                    }
                    console.log('posts fetched ('+res.data.length+')');
                  } else {
                    M.toast({html: 'Interner Fehler.'});
                  }
                  hideLoading();
                });
      },
      selectPost: function(post) {
        showLoading('Lade Beitrag...');
        Axios.get('/api/post?postID='+post.id)
                .then((res) => {
                  if(res.data.id) {
                    this.currPost.id = res.data.id;
                    this.currPost.title = res.data.title;
                    this.currPost.text = res.data.content;
                    this.currPost.imagesAdded = res.data.images;
                    this.currPost.layoutPost = res.data.layoutPost;
                    this.currPost.layoutPreview = res.data.layoutPreview;
                    this.currPost.preview = res.data.preview;
                    this.currPost.menu = res.data.menu;

                    // date, size, filenames
                    this.currPost.imagesAdded.forEach(i => {
                      this.addImageData(i);
                    });

                    var selectTab = this.currMode.id === modes.edit.id ? tabs.selectEdit : this.currMode.id === modes.approve.id ? tabs.selectApprove : tabs.selectSpecial;
                    this.currTabs = this.admin || this.info.hasMenuWriteAccess ?
                            [selectTab, tabs.text, tabs.images, tabs.layout, tabs.assign, tabs.finalize] : [selectTab, tabs.text, tabs.images, tabs.layout, tabs.finalize];
                    this.currTab = tabs.text;

                    this.$nextTick(() => {
                      M.updateTextFields();
                      window.scrollTo({ top: document.getElementById('post-manager-title').getBoundingClientRect().top + window.scrollY, behavior: 'smooth' });
                    });

                    console.log('post fetched');
                  } else {
                    M.toast({html: 'Interner Fehler.'});
                  }
                  hideLoading();
                });
      },
      selectSpecial: function(type) {
        showLoadingInvisible();
        Axios.get('/api/post/special?type='+this.specialCode(type.id))
                .then((res) => {
                  if(res.data) {
                    this.currPost.id = type.id; // just for front end calculations
                    this.currPost.text = res.data.text;
                    this.currTabs = [tabs.selectSpecial, tabs.textSpecial];
                    this.currTab = tabs.textSpecial;
                    this.$nextTick(() => {
                      window.scrollTo({ top: document.getElementById('post-manager-title').getBoundingClientRect().top + window.scrollY, behavior: 'smooth' });
                    });
                    console.log('text fetched');
                  } else {
                    M.toast({html: 'Interner Fehler.'});
                  }
                  hideLoading();
                });
      },
      setLayoutPost: function(layout) {
        this.currPost.layoutPost = layout;
        M.toast({html: 'Beitrag-Layout '+ (layout+1) + ' ausgewählt'});
      },
      setLayoutPreview: function(layout) {
        this.currPost.layoutPreview = layout;
        M.toast({html: 'Vorschau-Layout '+ (layout+1) + ' ausgewählt'});
      },
      selectImage: function(image) {
        this.currPost.imagesAdded.push(image);
        if(this.currPost.imagesAvailable.length < 8)
          this.increaseImageLimit(false);
        this.updateImagesAvailable();
      },
      deselectImage: function(image) {
        this.currPost.imagesAdded = this.currPost.imagesAdded.filter(i => i.id !== image.id);
        this.updateImagesAvailable();
      },
      deselectAllImages: function() {
        this.currPost.imagesAdded = [];
        this.updateImagesAvailable();
      },
      showUpload: function() {
        document.getElementById('upload-image').click();
      },
      imageDragEnter: function(event) {
        event.preventDefault();
        this.imageDragging = true;
      },
      imageDragExit: function() {
        this.imageDragging = false;
      },
      uploadImages: function(event) {
        event.preventDefault();
        this.imageDragging = false;
        var files = event.dataTransfer ? event.dataTransfer.files : event.currentTarget.files;
        uploadMultipleFiles('/api/post/upload-image', files, {
          params: {},
          uploaded: (file) => {
            this.addImageData(file);
            this.imagesAvailable.unshift(file);
            this.currPost.imagesAdded.push(file);
          },
          finished: () => {}
        });
      },
      addImageData: function(image) {
        // time
        image.time = moment(image.timestamp).format('DD.MM.YYYY');

        // size
        image.sizeReadable = storageReadable(image.size);

        // filenames
        image.fileNameNoExtension = image.fileName.substring(0, image.fileName.lastIndexOf('.'))
      },
      increaseImageLimit: function(update=true) {
        this.imagesAvailableLimit += 10;
        if(update)
          this.updateImagesAvailable();
        console.log("increased limit to " + this.imagesAvailableLimit);
      },
      fetchImages: function() {
        Axios.get('/api/post/images-available')
                .then((res) => {
                  if(Array.isArray(res.data)) {
                    this.imagesAvailable = res.data;
                    this.imagesAvailable.forEach(i => {
                      this.addImageData(i);
                    });
                    this.updateImagesAvailable();
                    console.log('images fetched ('+res.data.length+')');
                  } else {
                    M.toast({html: 'Interner Fehler.'});
                  }
                });
      },
      updateImagesAvailable: function() {
        this.currPost.imagesAvailable = this.imagesAvailable.slice(0, this.imagesAvailableLimit).filter(i => !this.currPost.imagesAdded.includes(i));
      },
      assignMenu: function (menu) {
        if(menu.link) {
          M.toast({html: 'Keine Links erlaubt'});
        } else {
          this.currPost.menu = menu;
          M.toast({html: 'Gruppe ausgewählt'});
        }
      },
      sendPost: async function () {
        showLoading("Verarbeitung...");
        try {
          const res = await Axios.post('/api/post', {
            id: this.currPost.id,
            title: this.currPost.title,
            text: this.currPost.text,
            images: this.currPost.imagesAdded,
            layoutPost: this.currPost.layoutPost,
            layoutPreview: this.currPost.layoutPreview,
            preview: this.currPost.preview,
            menuID: this.currPost.menu ? this.currPost.menu.id : null
          });

          if(res.data.id) {
            if(res.data.menu)
              await this.$router.push('/beitrag/'+res.data.id);
            else {
              // this post is unapproved
              if(this.currMode.id === this.modes.create.id) {
                // user has created a new unapproved post
                this.info.unapproved++;
                this.modes.edit.name = this.editModeText(this.info.unapproved);
              }
              hideLoading();
              M.toast({ html: 'Fertig!' });
              this.setMode(this.currMode);
            }
          }
        } catch (e) {
          if(e.response.data.message) {
            M.toast({ html: e.response.data.message });
          } else {
            M.toast({ html: 'Ein Fehler ist aufgetreten.' });
          }
          hideLoading();
        }
      },
      sendSpecial: function () {
        showLoading("Verarbeitung...");
        var text = this.currPost.text;
        Axios.post('/api/post/special', {
          type: this.specialCode(this.currPost.id),
          text: text
        })
                .then((res) => {
                  if(res.data.success) {
                    M.toast({ html: 'Änderungen gespeichert<br>'+this.specialObj.name });
                    this.setMode(this.currMode);
                  } else if(res.data.message) {
                    M.toast({ html: res.data.message });
                  }
                  hideLoading();
                });
      },
      postInit: function() {
        this.$nextTick(() => {
          M.AutoInit();
          M.updateTextFields();
          console.log('post init finished')
        });
      }
    },
    computed: {
      admin: function () {
        return this.info.user && this.info.user.code.role === 100;
      },
      imageURL: function() {
        return (image) => '/files/images/'+image.id+'_'+image.fileName;
      },
      editModeText: function() {
        return (unapproved) => unapproved ? 'Bearbeiten (' + unapproved + ')' : 'Bearbeiten';
      },
      approvedModeText: function() {
        return (unapproved) => unapproved ? 'Genehmigen (' + unapproved + ')' : 'Genehmigen';
      },
      specialObj: function() {
        for(var prop in this.specialAll) {
          if(Object.prototype.hasOwnProperty.call(this.specialAll, prop) && this.specialAll[prop].id === this.currPost.id) {
            return this.specialAll[prop];
          }
        }
        return {};
      },
      specialCode: function() {
        return (id) => {
          for(var prop in this.specialAll) {
            if(Object.prototype.hasOwnProperty.call(this.specialAll, prop) && this.specialAll[prop].id === id) {
              return prop
            }
          }
          return null;
        };
      },
      finalButtonText: function() {
        switch(this.currMode.id) {
          case modes.create.id:
            return 'Veröffentlichen';
          case modes.approve.id:
            return 'Genehmigen';
          default:
            return 'Speichern';
        }
      },
      menuAssigned: function() {
        if(this.currPost.menu) {
          // build menu string with all its parents
          var s = '<span style="color: black; font-weight: bold;">'+this.currPost.menu.name+'</span>';
          var currMenu = this.currPost.menu;
          var findParent = (curr, parent, target) => {
            if(curr.id === target.id) {
              return parent;
            } else {
              return curr.children.reduce((result, c) => result ? result : findParent(c, curr, target), null);
            }
          };
          do {
            var parent = this.info.menu.reduce((result, curr) => result ? result : findParent(curr, null, currMenu), null);
            if(parent) {
              s = parent.name + ' / ' + s;
              currMenu = parent;
            }
          } while(parent);
          return s;
        } else {
          return 'keine Auswahl';
        }
      },
      validationInfoBox: function() {
        if(this.currPost.error.error)
          return {
            text: 'gefundene Fehler:',
            background: '#ffcdd2',
            icon: 'warning'
          };
        else if(this.currPost.warning.warning)
          return {
            text: 'Hinweise:',
            background: '#fff9c4',
            icon: 'info'
          };
        else
          return {
            text: 'Alles in Ordnung.',
            background: '#e8f5e9',
            icon: 'check'
          };
      }
    },
    watch: {
      'currPost.imagesAdded': function () {
        this.$nextTick(function () {
          tabs.images.number = this.currPost.imagesAdded.length;
        })
      },
    },
    mounted: function () {
      this.$nextTick(async () => {
        const infoPostManager = await Axios.get('/api/post/info-post-manager')
        this.maxPictureSize = infoPostManager.data.maxPictureSize;
        this.menuAdmin = infoPostManager.data.menuAdmin;
        this.writableMenu = infoPostManager.data.writableMenu;

        // update button text
        if(this.admin || this.menuAdmin)
          modes.approve.name = this.approvedModeText(this.info.unapproved);
        else
          modes.edit.name = this.editModeText(this.info.unapproved);

        if(this.info.user && this.info.user.code.role >= 0) {
          // set one time the modes available
          if(this.info.user.code.role === 100 || this.menuAdmin)
            this.modes = { create: modes.create, edit: modes.edit, approve: modes.approve, special: modes.special };
          else
            this.modes = { create: modes.create, edit: modes.edit };
          this.setMode(modes.create);
        }
      })
    }
  }
</script>

<style scoped>
  .mode-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column;

    width: 100%;
    height: 120px;
    margin-bottom: 30px;
    background-color: #1b5e20;
    color: white;
  }

  .mode-btn:hover {
    background-color: #2e7d32;
    cursor: pointer;
  }

  .mode-btn-active {
    background-color: #43a047 !important;
  }

  .tab-panel {
    min-height: 700px;
    padding: 20px;
  }

  .container-images {
    flex: 1;
    white-space: nowrap;
    overflow-x: hidden;
    overflow-y: hidden;
    height: 250px;
  }

  .container-image {
    position: relative;
    display: inline-block;
    text-align: center;
    margin: 20px;
    padding: 10px;
    cursor: pointer;
    background-color: #f1f8e9;
  }

  .image-number {
    position: absolute;
    right: -10px;
    top: -10px;
    width: 25px;
    height: 25px;

    background-color: #43a047;
    color: white;
    border-radius: 50%;
  }

  .image-time {
    margin: 0;
    color: #808080;
    font-style: italic;
    font-size: 0.8em;
  }

  .layout-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 200px;
    margin: 20px;
    background-color: #43a047;
    filter: grayscale(100%);
  }

  .layout-btn:hover {
    filter: none;
    cursor: pointer;
  }

  .layout-btn-active {
    filter: none !important;
  }

  .layout-btn > img {
    width: 150px;
  }

  .grey-round-border {
    padding: 10px;
    border-radius: 10px;
    background-color: #f5f5f5;
  }

  .grey-round-border > .card, .grey-round-border > .card-panel {
    margin: 0;
  }

  .special-container {
    text-align: center !important;
    background-color: #e8f5e9;
    padding: 30px 20px 20px 20px;
    border-radius: 20px
  }

  .special-container > i {
    font-size: 4em;
  }

  .special-container > h5 {
    margin-bottom: 20px;
  }

  .special-btn {
    width: 240px;
    margin: 10px;
    font-size: 1em;
    background-color: #2e7d32 !important;
  }

  .special-btn-active {
    background-color: #4caf50 !important;
  }
</style>