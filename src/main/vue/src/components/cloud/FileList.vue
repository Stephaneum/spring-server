<template>
    <ul class="collection" style="margin: 0">
        <li v-for="f in files" :key="f.id" class="collection-item">
            <div style="display: flex; align-items: center;">
                    <span style="flex-grow: 1; display: flex; align-items: center; ">
                        <i v-if="f.isFolder" style="font-size: 1.5em; margin-right: 10px" class="material-icons">folder</i>
                        <span class="file-link" @click="select(f)">{{ f.isFolder ? f.name : f.fileName }}</span>
                    </span>

                <span style="text-align: right;" :style="{ 'flex' : sharedMode ? '0 0 400px' : '0 0 320px' }">
                        <span v-if="f.public" class="green-badge-light">öffentlich</span>
                        <span style="margin-left: 20px" class="green-badge-light">{{ f.sizeReadable }}</span>
                        <span v-if="f.time" style="margin-left: 20px" class="green-badge-light">{{ f.time }}</span>
                        <span v-if="sharedMode" style="margin-left: 20px; background-color: #cfd8dc" class="green-badge-light">{{ f.user.firstName }} {{ f.user.lastName }}</span>
                    </span>

                <span style="flex: 0 0 270px; text-align: right">
                        <a :href="f.link + '?download=true'" class="tooltipped waves-effect waves-light green darken-3 btn margin-1" :class="{ disabled: f.isFolder }" data-tooltip="Download" data-position="bottom">
                            <i class="material-icons">arrow_downward</i>
                        </a>

                        <a @click="onPublic(f)" class="tooltipped waves-effect waves-light teal btn margin-1" :class="{ disabled: f.isFolder, 'darken-2': !f.public, 'lighten-2': f.public }" href="#!" data-tooltip="Link" data-position="bottom">
                            <i class="material-icons">language</i>
                        </a>

                        <a @click="onEdit(f)" class="tooltipped waves-effect waves-light teal darken-2 btn margin-1" :class="{ disabled: !f.canModify }" href="#!" data-tooltip="Bearbeiten" data-position="bottom">
                            <i class="material-icons">edit</i>
                        </a>

                        <a @click="onDelete(f)" class="tooltipped waves-effect waves-light btn red darken-4 margin-1" :class="{ disabled: !f.canModify }" href="#!" data-tooltip="Löschen" data-position="bottom">
                            <i class="material-icons">delete</i>
                        </a>
                    </span>
            </div>
        </li>
    </ul>
</template>

<script>
export default {
    name: 'FileList',
    props: ['files', 'sharedMode', 'modifyAll'],
    methods: {
        select(f) {
            this.$emit('select', f);
        },
        onPublic(f) {
            this.$emit('public', f);
        },
        onEdit(f) {
            this.$emit('edit', f);
        },
        onDelete(f) {
            this.$emit('delete', f);
        }
    },
}
</script>

<style scoped>
    .file-link:hover {
        color: #2e7d32;
        text-decoration: underline;
        cursor: pointer;
    }
</style>