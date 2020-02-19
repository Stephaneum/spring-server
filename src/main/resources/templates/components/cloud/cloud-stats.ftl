
<#-- cloud statistics of the user -->

<#macro render>
    <template id="cloud-stats">
        <div>
            <div style="text-align: right">
                <a @click="exit" class="waves-effect btn-flat" href="#!" style="margin: 10px 10px 0 0">
                    <i class="material-icons">close</i>
                </a>
            </div>

            <h5 style="text-align: center">Speicherverbrauch</h5>

            <div class="stats-storage-bar" style="margin: 70px 20px 20px 20px; height: 20px">
                <div style="background-color: #a5d6a7; left: 0" :style="{ width: (info.percentage*100)+'%' }"></div>
            </div>

            <div style="display: flex; justify-content: space-evenly">
                <span><span class="color-block" style="background-color: #a5d6a7"></span>Beansprucht: {{ info.used }}</span>
                <span>Verfügbar: {{ info.free }}</span>
                <span>Insgesamt: {{ info.total }}</span>
            </div>

            <div class="stats-storage-bar" style="margin: 100px 20px 20px 20px; height: 20px">
                <div style="background-color: #a5d6a7; left: 0" :style="{ width: (info.privatePercentage*100)+'%' }"></div>
                <div style="background-color: #81d4fa;" :style="{ left: (info.privatePercentage*100)+'%', width: (info.projectPercentage*100)+'%' }"></div>
                <div style="background-color: #ffcc80;" :style="{ left: ((info.privatePercentage+info.projectPercentage)*100)+'%', width: (info.classPercentage*100)+'%' }"></div>
                <div style="background-color: #80cbc4;" :style="{ left: ((info.privatePercentage+info.projectPercentage+info.classPercentage)*100)+'%', width: (info.teacherPercentage*100)+'%' }"></div>
            </div>

            <div style="display: flex; justify-content: space-evenly">
                <span><span class="color-block" style="background-color: #a5d6a7"></span>Nutzerspeicher: {{ info.private }}</span>
                <span><span class="color-block" style="background-color: #81d4fa"></span>Projektspeicher: {{ info.project }}</span>
                <span><span class="color-block" style="background-color: #ffcc80"></span>Klassenspeicher: {{ info.schoolClass }}</span>
                <span v-if="teacherchat"><span class="color-block" style="background-color: #80cbc4"></span>Lehrerchat: {{ info.teacherChat }}</span>
            </div>

            <div style="margin: 70px 0 50px 0; text-align: center">
                Insgesamt {{ info.count }} Dateien wurden hochgeladen.
            </div>
        </div>
    </template>

    <script type="text/javascript">
        Vue.component('cloud-stats', {
            props: ['info', 'teacherchat'],
            methods: {
                exit: function() {
                    this.$emit('onexit');
                }
            },
            template: '#cloud-stats'
        });
    </script>

    <style>
        .stats-storage-bar {
            border: 2px solid #eeeeee;
            position: relative;
            margin: 10px 0 10px 0;
        }

        .stats-storage-bar > div {
            position: absolute;
            top: 0;
            height: 100%;
        }

        .color-block {
            display: inline-block;
            width: 20px;
            height: 10px;
            margin-right: 10px;
        }
    </style>
</#macro>