
<#-- view list of all members with actions -->

<#macro render>
    <template id="member-list">
        <div class="card" style="margin: 0; width: 100%; height: 100%; padding: 10px">
            <div style="display: flex; justify-content: space-between; margin-bottom: 10px">
                <h5 style="margin-top: 5px">Mitglieder</h5>
                <a @click="onPublic(f)" class="tooltipped waves-effect waves-light green darken-4 btn-floating" href="#!" data-tooltip="Mitglied hinzufügen" data-position="top">
                    <i class="material-icons">add</i>
                </a>
            </div>

            <div style="display: flex; align-items: center; height: 30px">
                <i class="material-icons" style="margin-right: 5px">star</i>
                <span>{{ leader.firstName }} {{ leader.lastName }}</span>
            </div>

            <div v-for="u in teachers" style="display: flex; align-items: center; height: 30px">
                <i class="material-icons" style="margin-right: 5px">remove_red_eye</i>
                <span>{{ u.firstName }} {{ u.lastName }}</span>
            </div>

            <div v-for="u in normalMembers" style="display: flex; align-items: center; justify-content: space-between; height: 30px">
                <span style="display: flex; align-items: center">
                    <i class="material-icons" style="margin-right: 5px">person</i>
                    <span>{{ u.firstName }} {{ u.lastName }}</span>
                </span>
                <div style="display: flex; align-items: center">
                    <i class="material-icons" style="margin-right: 10px; font-size: 1.2em">chat</i>
                    <i class="material-icons" style="font-size: 1.2em">delete</i>
                </div>
            </div>
        </div>
    </template>

    <script type="text/javascript">
        Vue.component('member-list', {
            props: ['members', 'leader', 'teachers'],
            methods: {
            },
            computed: {
                normalMembers: function() {
                    return this.members.filter((u) => u.id !== this.leader.id && this.teachers.every((t) => t.id !== u.id));
                }
            },
            template: '#member-list'
        });
    </script>
</#macro>