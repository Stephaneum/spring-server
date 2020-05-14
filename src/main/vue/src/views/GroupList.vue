<template>
  <div v-show="allowed" class="internal-container">

    <InternalHeader title="Gruppen" icon="people"></InternalHeader>

    <div class="card-panel">
      <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px">
        <span style="font-size: 2em">Meine Gruppen</span>
        <a @click="showCreateGroup" style="margin-right: 20px" class="tooltipped waves-effect waves-light btn-floating green darken-4"
           data-tooltip="Neue Gruppe" data-position="top" href="#!">
          <i class="material-icons">add</i>
        </a>
      </div>

      <div style="display: flex; flex-wrap: wrap; align-items: stretch; justify-content: space-evenly; margin-bottom: 30px">
        <template v-for="g in groups">
          <router-link v-if="g.accepted" :to="'/groups/'+g.id" :key="'g'+g.id" v-slot="{ href, navigate }">
            <a @click="navigate" :href="href" class="group-rect card white-text">
              <i class="material-icons" style="font-size: 50px">people</i>
              <span style="font-weight: bold; font-size: 20px; text-align: center; line-height: 20px; margin-bottom: 5px">{{ g.name }}</span>
              <span>{{ g.leader.firstName }} {{ g.leader.lastName }}</span>
            </a>
          </router-link>

          <div v-else :key="'u'+g.id" class="group-rect card white-text" style="cursor: default; background-color: #455a64 !important;">
            <span style="font-weight: bold; font-size: 20px; text-align: center; line-height: 20px; margin-bottom: 5px">{{ g.name }}</span>
            <span>{{ g.leader.firstName }} {{ g.leader.lastName }}</span>
            <div v-if="student" style="margin-top: 10px">
              Warten auf Genehmigung
            </div>
            <div v-else style="margin-top: 10px; width: 100%; display: flex; justify-content: space-evenly">
              <a @click="acceptGroup(g)" href="#!" class="tooltipped btn waves-effect waves-light green darken-3" data-tooltip="Akzeptieren" data-position="bottom">
                <i class="material-icons">check</i>
              </a>
              <a @click="rejectGroup(g)" href="#!" class="tooltipped btn waves-effect waves-light red darken-3" data-tooltip="Ablehnen" data-position="bottom">
                <i class="material-icons">close</i>
              </a>
            </div>
          </div>
        </template>
      </div>

      <div v-if="admin">
        <span style="font-size: 2em;">Alle Gruppen</span>
        <ul class="collection" style="margin: 20px 0 0 0">
          <li v-for="g in allGroups" :key="g.id" class="collection-item">
            <div style="display: flex; align-items: center;">
              <span style="flex-grow: 1; display: flex; align-items: center; ">
                <router-link v-if="g.accepted" :to="'/groups/'+g.id" v-slot="{ href, navigate }">
                  <a @click="navigate" :href="href" class="group-link">{{ g.name }}</a>
                </router-link>

                <i v-if="!g.accepted" class="material-icons grey-text text-darken-2" style="margin-left: 15px">report_problem</i>
                <span v-if="!g.accepted" class="grey-text text-darken-2" style="margin-left: 5px">Warten auf Genehmigung des Betreuers</span>
              </span>
              <span style="flex: 0 0 320px; text-align: right;">
                  <span class="green-badge-light">{{ g.members }} Mitglieder</span>
                  <span style="margin-left: 20px" class="green-badge-light">{{ g.leader.firstName }} {{ g.leader.lastName }}</span>
              </span>

              <span style="flex: 0 0 150px; text-align: right">
                  <a @click="toggleChat(g)" class="tooltipped waves-effect waves-light darken-2 btn margin-1" :class="g.chat ? ['teal'] : ['grey']" href="#!" data-tooltip="Chat" data-position="bottom">
                      <i class="material-icons">{{ g.chat ? 'chat' : 'close' }}</i>
                  </a>

                  <a @click="showDelete(g)" class="tooltipped waves-effect waves-light btn red darken-4 margin-1" href="#!" data-tooltip="Löschen" data-position="bottom">
                      <i class="material-icons">delete</i>
                  </a>
              </span>
            </div>
          </li>
        </ul>
      </div>
    </div>

    <!-- create modal -->
    <div id="modal-create-group" class="modal" style="width: 500px">
      <div class="modal-content">
        <h4>Neue Gruppe</h4>
        <br>
        <p>Bitte gib in dem Textfeld den Namen der Gruppe ein.</p>
        <p><b>Du</b> bist im Anschluss der Gruppen-Admin.</p>
        <div class="input-field">
          <i class="material-icons prefix">edit</i>
          <label for="create-group-name">Name</label>
          <input v-model="createGroupData.name" type="text" id="create-group-name"/>
        </div>
        <div v-if="student" style="height: 250px">
          <p>Da du noch Schüler bist, musst du mindestens einen Betreuer angeben.</p>
          <div class="input-field">
            <i class="material-icons prefix">person</i>
            <input type="text" id="group-teacher-input" class="autocomplete" v-model="createGroupData.teacherInput">
            <label for="group-teacher-input">Betreuer/in</label>
          </div>
          <div style="display: flex; align-items: center">
            <span style="margin-right: 10px">Angegeben:</span>
            <span v-if="createGroupData.teachersAdded.length === 0">Keine</span>
            <span v-for="t in createGroupData.teachersAdded" :key="t.id" class="green-badge-light" style="margin-right: 10px">{{ t.teacherName }}</span>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <a href="#!" class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
        <button @click="createGroup" type="button" class="btn waves-effect waves-light green darken-3">
          <i class="material-icons left">add</i>
          Erstellen
        </button>
      </div>
    </div>

    <!-- delete modal -->
    <div id="modal-delete-group" class="modal">
      <div class="modal-content">
        <h4>Löschen fortfahren?</h4>
        <br>
        <p><b>{{ selected.name }}</b> wird gelöscht.</p>
        <p>Dieser Vorgang kann nicht rückgangig gemacht werden.</p>
      </div>
      <div class="modal-footer">
        <a href="#!" class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
        <a @click="deleteItem" href="#!" class="modal-close waves-effect waves-red btn red darken-4">
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
  import { showLoading, showLoadingInvisible, hideLoading } from '@/helper/utils.js';
  import InternalHeader from "../components/InternalHeader";

  export default {
    name: 'GroupList',
    components: {InternalHeader},
    props: ['info'],
    data: () => ({
      groups: [],
      allGroups: [],
      selected: {},
      createGroupData: {
        name: null,
        teacherInput: null,
        teachersAdded: [],
        teachersAvailable: []
      }
    }),
    methods: {
      fetchData: async function() {
        const groups = await Axios.get('/api/groups');
        this.groups = groups.data;

        if(this.admin) {
          const allGroups = await Axios.get('/api/groups/all');
          this.allGroups = allGroups.data;
        }

        hideLoading();
        this.$nextTick(() => M.Tooltip.init(document.querySelectorAll('.tooltipped'), {}));
      },
      toggleChat: async function(group) {
        showLoadingInvisible();
        const nextState = group.chat ? 0 : 1;
        try {
          await Axios.post('/api/groups/' + group.id + '/chat/' + nextState);
          await this.fetchData();
        } catch (e) {
          M.toast({html: 'Interner Fehler.'});
          hideLoading();
        }
      },
      showCreateGroup: async function() {
        // reset
        this.createGroupData.name = null;
        this.createGroupData.teacherInput = null;
        this.createGroupData.teachersAdded = [];
        this.createGroupData.teachersAvailable = [];

        M.Modal.getInstance(document.getElementById('modal-create-group')).open();
        if(this.student) {
          try {
            const teachers = await Axios.post('/api/search/user', { role: 1 });
            teachers.data.forEach((t) => {
              t.teacherName = t.gender !== null ? (t.gender === 1 ? 'Frau ' + t.lastName : 'Herr ' + t.lastName) : t.firstName + ' ' + t.lastName;
            });
            this.createGroupData.teachersAvailable = teachers.data;
            const data = teachers.data.reduce((map, obj) => {
              map[obj.teacherName] = null;
              return map;
            }, {});

            const options = {
              data,
              limit: 3,
              onAutocomplete: (teacherName) => {
                const selected = this.createGroupData.teachersAvailable.find((t) => t.teacherName === teacherName);
                if(selected) {
                  this.createGroupData.teachersAdded.push(selected);
                  this.createGroupData.teachersAvailable = this.createGroupData.teachersAvailable.filter((t) => t.id !== selected.id);
                  this.createGroupData.teacherInput = null;
                }
              }
            };

            M.Autocomplete.init(document.querySelectorAll('.autocomplete'), options);
          } catch (e) {
            M.toast({html: 'Ein Fehler ist aufgetreten.'});
          }
        }
      },
      createGroup: async function() {
        M.Modal.getInstance(document.getElementById('modal-create-group')).close();
        showLoading('Gruppe erstellen...');
        try {
          await Axios.post( '/api/groups/create', { name: this.createGroupData.name, teachers: this.createGroupData.teachersAdded.map((t) => t.id) });
          await this.fetchData();
          M.toast({html: 'Gruppe erstellt.<br>'+this.createGroupData.name});
        } catch (e) {
          hideLoading();
          M.toast({html: 'Fehlgeschlagen.'});
        }
      },
      showDelete: function(f) {
        this.selected = f;
        M.Modal.getInstance(document.getElementById('modal-delete-group')).open();
      },
      deleteItem: async function() {
        M.Modal.getInstance(document.getElementById('modal-delete-group')).close();
        showLoading('Gruppe löschen...');
        try {
          await Axios.post('/api/groups/' + this.selected.id + '/delete');
          M.toast({html: 'Gruppe gelöscht.<br>'+this.selected.name});
          await this.fetchData();
        } catch (e) {
          hideLoading();
          M.toast({html: 'Löschen fehlgeschlagen.<br>'+this.selected.name});
        }
      },
      acceptGroup: async function(group) {
        showLoadingInvisible();
        try {
          await Axios.post('/api/groups/' + group.id + '/accept');
          await this.fetchData();
          M.toast({html: 'Gruppe akzeptiert.<br>'+group.name});
        } catch (e) {
          hideLoading();
          M.toast({html: 'Fehlgeschlagen.'});
        }
      },
      rejectGroup: async function(group) {
        showLoadingInvisible();
        try {
          await Axios.post('/api/groups/' + group.id + '/reject');
          await this.fetchData();
          M.toast({html: 'Gruppe abgelehnt.<br>'+group.name});
        } catch (e) {
          hideLoading();
          M.toast({html: 'Fehlgeschlagen.'});
        }
      }
    },
    computed: {
      allowed: function() {
        return this.info.user && this.info.user.code.role >= 0;
      },
      admin: function() {
        return this.info.user && this.info.user.code.role === 100;
      },
      student: function() {
        return this.info.user && this.info.user.code.role === 0;
      }
    },
    mounted: function() {
      M.AutoInit();
      this.fetchData();
    }
  }
</script>

<style scoped>
  .group-rect {
    flex-basis: calc(25% - 30px);
    background-color: #1b5e20;
    cursor: pointer;
    padding: 15px 5px 15px 5px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
  }

  .group-rect:hover {
    background-color: #388e3c !important;
  }

  @media screen and (min-width: 901px) and (max-width: 1100px) {
    .group-rect {
      flex-basis: calc(33.3% - 30px);
    }
  }

  @media screen and (min-width: 601px) and (max-width: 900px) {
    .group-rect {
      flex-basis: calc(50% - 30px);
    }
  }

  @media screen and (max-width: 600px) {
    .group-rect {
      flex-basis: 100%;
    }
  }

  .group-link {
    color: black;
  }

  .group-link:hover {
    color: #2e7d32;
    text-decoration: underline;
    cursor: pointer;
  }
</style>