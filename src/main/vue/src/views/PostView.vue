<template>
  <CenterLayout title="Beitrag" :custom-card="true" :plan="info.plan" :history="info.history" :eu-sa="info.euSa">
    <div v-if="fetching" class="card-panel white" style="height: 500px">
      <div class="empty-hint">
        Lade Beitrag...
      </div>
    </div>

    <div v-else-if="notFound" class="card-panel white" style="height: 500px">
      <div class="empty-hint">
        Error 404
        <br>
        Beitrag nicht gefunden
      </div>
    </div>

    <div v-else-if="locked" class="card-panel white" style="height: 500px; display: flex; align-items: center; justify-content: center">
      <div>
        <p style="font-size: 1.4rem; margin-bottom: 50px; text-align: center">Dieser Bereich ist passwortgeschützt.</p>
        <div>
          <div class="input-field" style="display: inline-block">
            <i class="material-icons prefix">vpn_key</i>
            <label for="input-section-password">Passwort</label>
            <input v-model="password" type="password" id="input-section-password" autocomplete="off"/>
          </div>

          <a @click="unlock" class="btn waves-effect green darken-4" style="margin-left: 40px">
            <i class="material-icons left">lock_open</i>
            Entsperren
          </a>
        </div>
      </div>
    </div>

    <Post v-else :menu="post.menu" :date="post.date" :title="post.title" :text="post.content" :layout="post.layout" :images="post.images"></Post>
  </CenterLayout>
</template>

<script>
  import Axios from "axios"
  import moment from "moment"
  import CenterLayout from "../components/CenterLayout";
  import Post from "../components/cms/Post";
  import M from "materialize-css";
  import {hideLoading, showLoadingInvisible} from "../helper/utils";

  export default {
    name: 'PostView',
    components: { Post, CenterLayout },
    props: ['info'],
    data: () => ({
      fetching: true,
      password: null, // to unlock
      post: {
          date: null,
          title: null,
          content: null,
          layout: 0,
          images: []
      },
      notFound: false,
      locked: false,
      unknownError: false
    }),
    methods: {
      async fetchData() {
        try {
          const id = this.$route.params.id;
          const post = await Axios.get('/api/post?postID='+id);
          this.post = post.data;
          this.post.layout = this.post.layoutPost;
          this.post.date = moment(this.post.timestamp).format('DD.MM.YYYY');
          this.post.images.forEach((image) => {
            image.fileNameNoExtension = image.fileName.substring(0, image.fileName.lastIndexOf('.'))
          });
          this.locked = false;
          hideLoading();
        } catch (e) {
          switch(e.response.status) {
            case 403:
              this.locked = true;
              break;
            case 404:
              this.notFound = true;
              break;
            default:
              this.unknownError = true;
          }
        } finally {
          this.fetching = false;
        }
      },
      async unlock() {

        if(!this.password) {
          M.toast({html: 'Bitte Passwort eingeben'});
          return;
        }

        const id = this.$route.params.id;

        showLoadingInvisible();
        try {
          await Axios.post('/api/unlock/post',{ post: id, password: this.password });
          await this.fetchData();
          M.toast({html: 'Beitrag freigeschaltet'});
        } catch (e) {
          switch(e.response.status) {
            case 403:
              M.toast({html: 'Falsches Passwort'});
              break;
            default:
              M.toast({html: 'Ein Fehler ist aufgetreten'});
          }
          hideLoading();
        }
      }
    },
    async mounted() {
        await this.fetchData();
    }
  }
</script>