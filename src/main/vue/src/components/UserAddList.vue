<template>
    <div>
        <ul class="collection">
            <li v-for="u in results" :key="u.id" class="collection-item">
                <div style="display: flex; align-items: center;">
                    <span style="flex-grow: 1; white-space: nowrap; overflow-x: hidden; text-overflow: ellipsis;">
                        {{ u.firstName }} {{ u.lastName }}
                    </span>
                    <span style="flex: 0 0 200px; text-align: right">
                        <a v-if="u.excluded" class="btn-flat">
                            <i class="material-icons left">check</i>{{ excludedString }}
                        </a>
                        <a v-else @click="select(u)" class="waves-effect waves-light btn green darken-4">
                            <i class="material-icons left">check</i>{{ actionString }}
                        </a>
                    </span>
                </div>
            </li>
        </ul>
    </div>
</template>

<script>
export default {
    name: 'UserAddList',
    props: ['users', 'excluded', 'excludedString', 'actionString'],
    data: () => ({
        results: [],
    }),
    methods: {
        select: function(u) {
            this.$emit('select', u);
        },
        update: function() {
            this.results = this.users.map((u) => {
                const copy = { ...u };
                copy.excluded = this.excluded.some((e) => u.id === e.id);
                return copy;
            });
        }
    },
    watch: {
        users: {
            immediate: true,
            handler() {
                this.update();
            }
        },
        excluded: {
            immediate: true,
            handler() {
                this.update();
            }
        },
    }
}
</script>

<style>

</style>