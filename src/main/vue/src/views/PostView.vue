<template>
  <CenterLayout :title="post.title" :custom-card="true" :plan="info.plan" :history="info.history" :eu-sa="info.euSa">
    <Post :menu="post.menu" :date="post.date" :title="post.title" :text="post.content" :layout="post.layout" :images="post.images"></Post>
  </CenterLayout>
</template>

<script>
  import Axios from "axios"
  import moment from "moment"
  import CenterLayout from "../components/CenterLayout";
  import Post from "../components/cms/Post";

  export default {
    name: 'PostView',
    components: {Post, CenterLayout },
    props: ['info'],
    data: () => ({
      post: {
          date: null,
          title: null,
          content: null,
          layout: 0,
          images: []
      },
      notFound: false,
      unknownError: false
    }),
    async mounted() {
        try {
            const id = this.$route.params.id;
            const post = await Axios.get('/api/post?postID='+id);
            this.post = post.data;
            this.post.layout = this.post.layoutPost;
            this.post.date = moment(this.post.timestamp).format('DD.MM.YYYY');
        } catch (e) {
            switch(e) {
                case 404:
                    this.notFound = true;
                    break;
                default:
                    this.unknownError = true;
            }
        }

    }
  }
</script>