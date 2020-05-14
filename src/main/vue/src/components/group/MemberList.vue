<template>
    <div id="member-list-card" class="card" style="min-height: 500px; margin: 0; padding: 10px">
        <div style="display: flex; justify-content: space-between; margin-bottom: 10px">
            <h5 style="margin-top: 5px">Mitglieder</h5>
            <a v-if="modifyAll && showAddUser" @click="addUser" class="tooltipped waves-effect waves-light green darken-4 btn-floating" href="#!" data-tooltip="Mitglied hinzufügen" data-position="top">
                <i class="material-icons">add</i>
            </a>
        </div>

        <div style="display: flex; align-items: center; height: 30px">
            <i class="material-icons" style="margin-right: 5px">star</i>
            <span>{{ leader.firstName }} {{ leader.lastName }}</span>
        </div>

        <template v-for="u in members">
            <div v-if="u.teacher" :key="u.id" style="display: flex; align-items: center; height: 30px">
                <i class="material-icons" style="margin-right: 5px">remove_red_eye</i>
                <span>{{ u.firstName }} {{ u.lastName }}</span>
            </div>
        </template>

        <template v-for="u in members">
            <div v-if="!u.teacher && u.id !== leader.id" :key="u.id" style="display: flex; align-items: center; justify-content: space-between; height: 30px">
                    <span style="display: flex; align-items: center">
                        <i class="material-icons" style="margin-right: 5px">person</i>
                        <span>{{ u.firstName }} {{ u.lastName }}</span>
                    </span>
                <div v-if="modifyAll" style="display: flex; align-items: center">
                    <i @click="toggleWriteBoard(u)" class="material-icons" style="cursor: pointer; user-select: none; margin-right: 10px; font-size: 1.2em">{{ u.writeBoard ? 'edit' : 'close' }}</i>
                    <i @click="toggleChat(u)" class="material-icons" style="cursor: pointer; user-select: none; margin-right: 10px; font-size: 1.2em">{{ u.chat ? 'chat' : 'close' }}</i>
                    <i @click="kick(u)" class="material-icons" style="cursor: pointer; user-select: none; font-size: 1.2em">delete</i>
                </div>
            </div>
        </template>
    </div>
</template>

<script>
export default {
    name: 'MemberList',
    props: ['members', 'leader', 'modifyAll', 'showAddUser'],
    methods: {
        addUser: function() {
            this.$emit('adduser');
        },
        toggleChat: function(u) {
            this.$emit('togglechat', u);
        },
        toggleWriteBoard: function(u) {
            this.$emit('togglewriteboard', u);
        },
        kick: function(u) {
            this.$emit('kick', u);
        },
    },
    watch: {
        members: {
            immediate: true,
            handler() {
                this.$nextTick(() => {
                    const height = document.getElementById("member-list-card").getBoundingClientRect().height;
                    this.$emit('height', height);
                });
            }
        }
    }
}
</script>