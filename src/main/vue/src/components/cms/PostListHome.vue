<template>
    <div>
        <template v-for="p in digestedPosts">
            <PostPreview :key="p.id" :post-id="p.id"
                         :title="p.title" :text="p.content" :date="p.date" :images="p.images" :layout="p.layoutPreview" :preview="p.preview"></PostPreview>
            <br :key="'br'+p.id">
        </template>
    </div>
</template>

<script>
    import moment from "moment"
    import PostPreview from "./PostPreview";

export default {
    name: 'PostListHome',
    components: {PostPreview},
    props: ['posts'],
    data: () => ({
        digestedPosts: []
    }),
    methods: {
        digestPosts(posts) {
            return posts.map((p) => {
               p.date = moment(p.timestamp).format('DD.MM.yyyy');
               return p;
            });
        }
    },
    watch: {
        posts: {
            immediate: true,
            handler: function (newVal) {
                if(newVal)
                    this.digestedPosts = this.digestPosts(newVal);
            }
        }
    }
}
</script>