<template>
    <ul class="z-depth-1" style="z-index: 200">
        <li v-for="m in parent.children" :key="m.id">
            <router-link v-if="!unreal && !m.link" :to="menuUrl(m)" v-slot="{ href, navigate }">
                <a @click="navigate" :href="href">
                    <span>
                        <i class="material-icons">stop</i>
                        {{ m.name }}
                    </span>
                    <i v-if="m.children.length !== 0 || editMode" class="material-icons">keyboard_arrow_right</i>
                </a>
            </router-link>
            <a v-else @click="emit(m)" :href="link(m)" target="_blank">
                <span>
                    <i v-if="m.link" class="material-icons">arrow_upward</i>
                    <i v-else class="material-icons">stop</i>
                    {{m.name}}
                </span>
                <i v-if="m.children.length !== 0 || editMode" class="material-icons">keyboard_arrow_right</i>
            </a>
            <MenuItem v-if="m.children.length !== 0 || editMode"
                      :parent="m" :unreal="unreal" :edit-mode="editMode"
                      @select="emit" @group="emitNewGroup" @link="emitNewLink"></MenuItem>
        </li>
        <template v-if="editMode">
            <li>
                <a @click="emitNewGroup(parent)" class="edit-btn">
                    <span>
                        <i class="material-icons">add</i>
                        Gruppe
                    </span>
                </a>
            </li>
            <li>
                <a @click="emitNewLink(parent)" class="edit-btn">
                    <span>
                        <i class="material-icons">add</i>
                        Link
                    </span>
                </a>
            </li>
        </template>
    </ul>
</template>


<script>

export default {
    name: 'MenuItem',
    props: ['parent', 'unreal', 'editMode'],
    methods: {
        emit: function(menu) {
            this.$emit('select', menu);
        },
        emitNewGroup: function(parent) {
            this.$emit('group', parent);
        },
        emitNewLink: function(parent) {
            this.$emit('link', parent);
        },
    },
    computed: {
        menuUrl: function () {
            return (menu) => '/m/'+menu.id;
        },
        link: function() {
            return (menu) => this.unreal ? null : menu.link
        }
    }
}
</script>