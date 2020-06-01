<template>
  <div style="min-height: 100vh; display: flex; align-items: center; justify-content: center">
    <div>
      <div style="display: flex; align-items: center; justify-content: center">
        <img src="../assets/img/favicon.png" style="width: 60px"/>
        <span style="margin-left: 30px; font-size: 3rem; color: #396e3a">Erste Einrichtung</span>
      </div>

      <div style="margin-top: 100px; width: 80vw;">
        <TabBar :tabs="tabs" :curr-tab="currTab" @selected="setTab"></TabBar>
        <div style="width: 100%; margin-top: 0" class="card-panel">

          <!-- welcome -->
          <div v-if="currTab.id === 0" style="text-align: center">
            <h4>Willkommen!</h4>
            <br>
            <p>Anscheinend bist du gerade dabei, auf einem neuen Server die Stephaneum-Homepage zu installieren.</p>
            <p>Bitte wähle einer der beiden Modis aus, um fortzufahren:</p>
            <div style="margin: 50px 0 50px 0; display: flex; justify-content: space-evenly">

              <a class="waves-effect btn-large green darken-3"
                 @click="openBackupTab">
                <i class="material-icons left">save</i>
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
          <div v-if="currTab.id === 1">
            <h4>Backup laden</h4>
            <br>
            <p>Bitte gib die beiden Speicherorte an und lade das bestehende Backup hoch.</p>
          </div>

          <!-- new instance -->
          <div v-if="currTab.id === 2">
            <h4>Neue Instanz</h4>
            <br>
            <p>Bitte fülle die Textfelder aus, um eine neue leere Instanz zu starten.</p>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script>

  import TabBar from "../components/TabBar";

  const tabs = [
    { id: 0, icon: 'home', special: true, name: 'Willkommen' },
    { id: 1, icon: 'save', name: 'Backup laden' },
    { id: 2, icon: 'star', name: 'Neue Instanz' }
  ];

  export default {
    name: 'Init',
    components: {TabBar},
    data() {
      return {
        tabs: tabs,
        currTab: tabs[0]
      }
    },
    methods: {
      setTab(tab) {
        this.currTab = tab;
      },
      openBackupTab() {
        this.currTab = tabs[1];
      },
      openNewInstanceTab() {
        this.currTab = tabs[2];
      }
    }
  }
</script>