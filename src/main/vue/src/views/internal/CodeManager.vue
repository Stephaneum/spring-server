<template>
  <div v-if="allowed" class="internal-container">

    <InternalHeader title="Zugangscodes" icon="vpn_key"></InternalHeader>

    <div style="display: flex; justify-content: space-between">
      <div v-for="r in roles" :key="r.id" class="card-panel code-panel">
        <h5 style="text-align: center; margin: 0 0 30px 0">{{ r.name }} ({{ r.codes.length }})</h5>
        <ul v-if="r.codes.length !== 0" class="collection">
          <li v-for="c in r.codes" :key="c.id" class="collection-item">
            <div style="display: flex; align-items: center; justify-content: space-between">
              <span>
                  <span>{{ c.code }}</span>
                  <input type="hidden" :id="'code-field-'+c.id" :value="c.code">
              </span>

              <span>
                <a class="waves-effect waves-light tooltipped blue-grey darken-3 btn" style="margin-left: 10px"
                   @click="copy(c.id, c.code)" data-tooltip="Kopieren" data-position="bottom">
                    <i class="material-icons">save</i>
                </a>
                <a class="waves-effect waves-light tooltipped red darken-3 btn" style="margin-left: 10px"
                   @click="doDelete(c.id)" data-tooltip="Löschen" data-position="bottom">
                    <i class="material-icons">delete</i>
                </a>
              </span>
            </div>
          </li>
        </ul>
        <div style="text-align: center">
          <a @click="create(r.id)" class="waves-effect waves-light teal darken-3 btn" style="margin: 20px 0 20px 0">
            <i class="material-icons left">add</i>
            Neuer Code
          </a>
        </div>
      </div>
    </div>

    <div style="height: 100px"></div>
  </div>
</template>

<script>
  import Axios from "axios"
  import M from "materialize-css"
  import { showLoadingInvisible, hideLoading } from '@/helper/utils.js';
  import InternalHeader from "../../components/InternalHeader";

  const roles = {
    student: {
      id: 0,
      name: 'Schüler',
      codes: []
    },
    teacher: {
      id: 1,
      name: 'Lehrer',
      codes: []
    },
    guest: {
      id: 2,
      name: 'Gast',
      codes: []
    }
  };

  export default {
    name: 'CodeManager',
    components: {InternalHeader},
    props: ['info'],
    data: () => ({
      roles: roles,
      codes: []
    }),
    methods: {
      create: function(role) {
        showLoadingInvisible();
        Axios.post('/api/codes/add/'+role)
                .then((res) => {
                  if(res.data.success) {
                    M.toast({ html: 'Code generiert.' });
                    this.fetchData();
                  } else if(res.data.message) {
                    M.toast({ html: res.data.message });
                    hideLoading();
                  } else {
                    M.toast({ html: 'Ein Fehler ist aufgetreten.' });
                    hideLoading();
                  }
                });
      },
      copy: function(id, code) {
        let input = document.getElementById('code-field-'+id);
        input.setAttribute('type', 'text');
        input.select();

        document.execCommand('copy');
        M.toast({ html: 'Kopiert:<br>'+code });

        // unselect the range
        input.setAttribute('type', 'hidden');
        window.getSelection().removeAllRanges();
      },
      doDelete: function(id) {
        showLoadingInvisible();
        Axios.post('/api/codes/delete/'+id)
                .then((res) => {
                  if(res.data.success) {
                    M.toast({ html: 'Gelöscht.' });
                    this.fetchData();
                  } else if(res.data.message) {
                    M.toast({ html: res.data.message });
                    hideLoading();
                  } else {
                    M.toast({ html: 'Ein Fehler ist aufgetreten.' });
                    hideLoading();
                  }
                });
      },
      fetchData: function() {
        Axios.get('/api/codes')
                .then((res) => {
                  if(Array.isArray(res.data)) {
                    this.codes = res.data;
                    this.roles.student.codes = res.data.filter(d => d.role === this.roles.student.id);
                    this.roles.teacher.codes = res.data.filter(d => d.role === this.roles.teacher.id);
                    this.roles.guest.codes = res.data.filter(d => d.role === this.roles.guest.id);
                    this.$nextTick(() => {
                      this.$nextTick(() => M.Tooltip.init(document.querySelectorAll('.tooltipped'), {}));
                    });
                    hideLoading();
                  }
                });
      }
    },
    computed: {
      allowed: function() {
        return this.info.user && this.info.user.code.role === 100
      }
    },
    mounted: function() {
      M.AutoInit();
      this.fetchData();
    }
  }
</script>

<style scoped>
  .code-panel {
    flex-basis: 350px;
  }
</style>