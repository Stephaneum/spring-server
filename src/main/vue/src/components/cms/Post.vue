<template>
    <div class="card-panel white">
        <h5>{{ title }}</h5>
        <p v-if="menu">
            {{ date }}
            <span> / </span>
            <router-link :to="'/m/'+menu.id" v-slot="{ href, navigate }">
                <a @click="navigate" :href="href" class="green-text">{{ menu.name }}</a>
            </router-link>
        </p>
        <p v-else>{{ date }}</p>
        <br/>
        <div v-if="!hasImages">
            <span v-html="text" style="word-wrap: break-word"></span>
        </div>
        <div v-else>
            <div v-if="layout === 0" class="row">
                <div class="col m8">
                    <span v-html="text" style="word-wrap: break-word"></span>
                </div>
                <div class="col m4" style="text-align: right; vertical-align: top; overflow: hidden">
                        <span v-for="i in images" :key="i.id">
                            <img class="materialboxed" :data-caption="i.fileNameNoExtension" width="100%" :src="imageURL(i)"/>
                            <div align="right">
                                {{ i.fileNameNoExtension }}
                                <br />
                                <br />
                            </div>
                        </span>
                </div>
            </div>
            <div v-else-if="layout === 1">
                <span v-html="text" style="word-wrap: break-word"></span>
                <br/>
                <div class="row">
                    <div class="img-wall" style="text-align: center">
                        <img v-for="i in images" :key="i.id" class="materialboxed"
                             :data-caption="i.fileNameNoExtension" :src="imageURL(i)"/>
                    </div>
                </div>
            </div>
            <div v-else>
                <div v-if="images.length === 1" style="margin: auto; text-align: center">
                    <div style="display: inline-block; width: 600px">
                        <img :src="imageURL(images[0])" :alt="images[0].fileNameNoExtension" style="width: 100%; border-radius: 10px" />
                        <div style="text-align: right">
                            <p style="margin: 0 20px 0 0">{{ images[0].fileNameNoExtension }}</p>
                        </div>
                    </div>
                </div>
                <div v-else>
                    <div class="slider">
                        <ul class="slides">
                            <li v-for="i in images" :key="i.id">
                                <img :src="imageURL(i)">
                                <div class="caption right-align">
                                    <h5 class="light grey-text text-lighten-3">{{ i.fileNameNoExtension }}</h5>
                                </div>
                            </li>
                        </ul>
                    </div>
                </div>
                <br/><br/>
                <span v-html="text" style="word-wrap: break-word"></span>
            </div>
        </div>
    </div>
</template>

<script>
    import M from "materialize-css";

export default {
    name: 'Post',
    props: ['date', 'menu', 'title', 'text', 'layout', 'images'],
    computed: {
        hasImages: function () {
            return this.images.length !== 0;
        },
        imageURL: function () {
            return (image) => '/files/images/'+image.id+'_'+image.fileName;
        }
    },
    watch: {
        images: {
            immediate: true,
            handler() {
                this.$nextTick(() => {
                    M.Slider.init(document.querySelectorAll('.slider'), {});
                    M.Materialbox.init(document.querySelectorAll('.materialboxed'), {});
                });
            }
        }
    }
}
</script>

<style scoped>
    .img-wall .material-placeholder {
        display: inline-block !important;
        width: 33%;
    }

    .img-wall img {
        width: 100%;
        padding: 10px;
    }
</style>