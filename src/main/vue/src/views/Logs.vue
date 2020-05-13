<template>
  <div v-if="allowed" style="margin: auto; min-height: calc(100vh - 200px); max-width: 1200px">
    <div style="text-align: center; margin: 60px 0 40px 0">
      <i class="material-icons" style="font-size: 4em">history</i>
      <h4 style="margin: 0">Logdaten</h4>
    </div>

    <div class="card-panel">
      <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px">
        <div>
          <span style="font-size: 2em">{{ logInfo.amount }} Einträge</span>
          <br>
          <span style="font-style: italic">{{ logs.length }} davon angezeigt</span>
        </div>
        <div class="input-field" style="margin: 0">
          <i class="material-icons prefix">search</i>
          <label for="search-input">Suche</label>
          <input v-model="search" id="search-input" name="password" type="text" :disabled="waitingForData">
        </div>
        <div>
          <span>Limit:</span>
          <a v-for="a in amounts" :key="a" class="waves-effect waves-light btn blue-grey" style="margin-left: 10px"
             @click="setAmount(a)" :class="{ disabled: waitingForData, 'darken-3': a !== currAmount }">
            {{ a }}
          </a>
        </div>
      </div>

      <ul v-if="logs.length !== 0" class="collection">
        <li v-for="l in logs" :key="l.id" class="collection-item">
          <div style="display: flex; align-items: center;">
                        <span class="type-span">
                            <span class="green-badge-light" :class="l.className">{{ l.type }}</span>
                        </span>
            <span class="info-span">{{ l.info }}</span>
            <span class="date-span">
                            {{ l.date }}
                        </span>
          </div>
        </li>
      </ul>
      <div v-if="logs.length === 0" style="height: 300px; display: flex; align-items: center; justify-content: center">
        <span class="green-badge-light">Keine Einträge</span>
      </div>
    </div>
    <div style="height: 100px"></div>
  </div>
</template>

<script>
  import Axios from "axios"
  import M from "materialize-css"

  const amounts = [ 200, 1000, 5000 ];

  export default {
    name: 'Logs',
    props: ['info'],
    data: () => ({
      amounts: amounts,
      logInfo: { amount: null },
      logsRaw: [],
      logsRawLowerCase: [], // the same like logsRaw but all string are in lower case
      logs: [],
      logsLowerCase: [],
      search: null,
      currAmount: 200,
      waitingForData: true,
      firstFetch: true
    }),
    methods: {
      setAmount: function(amount) {
        this.waitingForData = true;
        this.currAmount = amount;
        this.search = null;
        this.fetchData();
      },
      fetchData: async function() {
        try {
          const info = await Axios.get('/api/logs/info');
          this.logInfo = info.data;
          const logs = await Axios.get('/api/logs/'+this.currAmount);
          this.logsRaw = logs.data;
          this.logs = [ ...logs.data ];
          this.logsRawLowerCase = logs.data.map(d => {
            return {
              id: d.id,
              type: d.type.toLowerCase(),
              info: d.info.toLowerCase(),
              date: d.date.toLowerCase()
            }
          });
          this.logsLowerCase = [ ...this.logsRawLowerCase ];
          this.waitingForData = false;
          if(!this.firstFetch)
            M.toast({ html: 'Logdaten geladen.<br>'+this.logs.length+' Einträge' });
          this.firstFetch = false;
        } catch (e) {
          M.toast({ html: 'Ein Fehler ist aufgetreten.' });
        }
      }
    },
    computed: {
      allowed: function() {
        return this.info.user && this.info.user.code.role === 100
      }
    },
    watch: {
      search: function(newVal, oldVal) {
        if(!newVal) {
          this.logs = [ ...this.logsRaw ];
          this.logsLowerCase = [ ...this.logsRawLowerCase ];
        } else {
          // if the next search key word is just a new letter, use the last results as basis
          let source = newVal.substring(0, newVal.length - 1) === oldVal ? this.logsLowerCase : this.logsRawLowerCase;
          let keyword = newVal.toLowerCase();
          this.logsLowerCase = source.filter(l => {
            return l.type.indexOf(keyword) !== -1 || l.info.indexOf(keyword) !== -1 || l.date.indexOf(keyword) !== -1
          });
          this.logs = this.logsLowerCase.map(l => this.logsRaw.find(f => f.id === l.id));
        }
      }
    },
    mounted: async function() {
      M.AutoInit();
      await this.fetchData();
    }
  }
</script>

<style scoped>
  .type-span {
    flex: 0 0 150px;
    text-align: right;
    padding-right: 20px;
  }

  .info-span {
    flex: 1;
    white-space: nowrap;
    overflow-x: hidden;
    text-overflow: ellipsis;
  }

  .date-span {
    flex: 0 0 250px;
    text-align: right;
    color: #808080;
  }

  .auth-badge {
    background-color: #bbdefb;
  }

  .file-badge {
    background-color: #c8e6c9;
  }

  .post-badge {
    background-color: #ffe0b2;
  }

  .group-badge {
    background-color: #e1bee7;
  }

  .menu-badge {
    background-color: #cfd8dc;
  }

  .other-badge {
    background-color: lightgrey;
  }
</style>