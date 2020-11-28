<template>
    <div style="display: flex; flex-wrap: wrap">

        <div v-if="hasParent"
             @drop="drop" @dragover="(e) => dragOver(e, parent)" @dragleave="dragLeave(parent)"
             class="file-rect" :class="{ 'drag-over': draggingTarget && parent === draggingTarget }">

            <!-- Icon -->
            <i @click="openParent" style="color: rgb(125, 125, 125); transform: rotate(90deg)" class="file-icon material-icons">subdirectory_arrow_left</i>

            <!-- Name -->
            <span @click="openParent" class="file-text">Ebene hoch</span>
        </div>

        <div v-for="f in files" :key="f.id"
             @drop="drop" @dragstart="dragStart(f)" @dragend="dragEnd" @dragover="(e) => dragOver(e, f)" @dragleave="dragLeave(f)" draggable="true"
             class="file-rect" :class="{ 'transparent': draggingFile && f.isFile && f !== draggingFile, 'drag-over': draggingTarget && f === draggingTarget }">

            <!-- Icon -->
            <img v-if="image(f)" :src="f.link" @click="select(f)" class="file-image"/>
            <i v-else @click="select(f)" :style="{ color: f.isFile ? 'rgb(175, 175, 175)' : 'rgb(125, 125, 125)' }" class="file-icon material-icons">{{ icon(f) }}</i>

            <!-- File / Folder Name -->
            <span @click="select(f)" class="file-text">
              <i v-if="f.locked" class="material-icons file-prefix-icon">lock</i>
              <i v-if="f.hiddenFileContents" class="material-icons file-prefix-icon">how_to_vote</i>
              <i v-else-if="hiddenFileContents && !f.canModify" class="material-icons file-prefix-icon">visibility_off</i>

              {{ f.isFile ? f.fileName : f.name }}
            </span>

            <!-- Info -->
            <div style="margin-bottom: 10px; text-align: center">
                <span v-if="f.locked" class="grey-text lighten-2">System</span>
                <span v-else-if="sharedMode" class="grey-text lighten-2">{{ f.user.firstName }} {{ f.user.lastName }}</span>
            </div>
        </div>
    </div>
</template>

<script>
const parent = { isParent: true };

export default {
    name: 'FileGrid',
    props: {
      files: Array,
      hasParent: Boolean,
      sharedMode: Boolean,
      hiddenFileContents: Boolean // if true, then only show content if canModify is true
    },
    data() {
        return {
            parent: parent,
            draggingFile: null,
            draggingTarget: null
        };
    },
    methods: {
        select(f) {
          if (this.hiddenFileContents && !f.canModify)
            return;
          this.$emit('select', f);
        },
        openParent() {
            this.$emit('open-parent');
        },
        dragStart(f) {
            this.draggingFile = f;
        },
        dragEnd() {
            this.draggingFile = null;
        },
        dragOver(e, f) {
            e.preventDefault();
            if(this.draggingFile && this.draggingFile !== f && (f === parent || !f.isFile)) {
                this.draggingTarget = f;
            }
        },
        dragLeave(f) {
            if(this.draggingTarget && f === this.draggingTarget)
                this.draggingTarget = null;
        },
        drop(e) {
            e.preventDefault();
            if(this.draggingFile && this.draggingTarget) {
                if (this.draggingTarget === parent)
                    this.$emit('move-parent', this.draggingFile);
                else
                    this.$emit('move', this.draggingFile, this.draggingTarget);
                this.draggingFile = null;
                this.draggingTarget = null;
            }
        }
    },
    computed: {
        icon() {
            return (file) => {
                if(!file.isFile)
                    return 'folder';
                else {
                    switch (file.mime) {
                        case 'image/jpeg': return 'photo';
                        default: return 'description';
                    }
                }
            };
        },
        image() {
            return (file) => {
                return file.isFile && file.mime.startsWith('image');
            }
        }
    }
}
</script>

<style scoped>
    .file-rect {
        flex-basis: 14.285714%;
        padding: 15px 5px 15px 5px;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        border: transparent solid 2px !important; /* will be overwritten by drag-over */
    }

    .file-image {
        cursor: pointer;
        max-width: 75px;
        max-height: 50px;
        margin: 5px 0 5px 0;
        box-shadow: 2px 2px 4px #aaa;
    }

    .file-icon {
        cursor: pointer;
        font-size: 4em;
        text-shadow: 2px 2px 4px #aaa;
    }

    .file-prefix-icon {
        font-size: 0.8em;
        margin-right: 4px;
    }

    .file-text {
        margin-top: 5px;
        text-align: center;
        cursor: pointer;
    }

    .transparent {
        filter: opacity(0.2);
    }

    .drag-over {
        border: #808080 dashed 2px !important;
    }

    @media screen and (min-width: 1301px) and (max-width: 1600px) {
        .file-rect {
            flex-basis: 16.66%;
        }
    }

    @media screen and (min-width: 1101px) and (max-width: 1300px) {
        .file-rect {
            flex-basis: 20%;
        }
    }

    @media screen and (min-width: 600px) and (max-width: 1100px) {
        .file-rect {
            flex-basis: 25%;
        }
    }
</style>