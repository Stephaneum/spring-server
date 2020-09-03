<template>
    <div class="card">
        <div class="card-content">
            <span class="card-title">
                <router-link v-if="postId" :to="postLink" v-slot="{ href, navigate }">
                    <a @click="navigate" :href="href" style="color: black">{{ title }}</a>
                </router-link>
                <span v-else>{{ title }}</span>
            </span>

            <br>

            <p v-if="!hasImages">{{ textPreview }}</p>

            <div v-else>
                <div v-if="layout === 0" class="row">
                    <div class="col m8">
                        <span>{{ textPreview }}</span>
                    </div>
                    <div class="col m4" style="text-align: right; vertical-align: top; overflow: hidden">
                        <router-link v-if="postId" :to="postLink" v-slot="{ href, navigate }">
                            <a @click="navigate" :href="href">
                                <img :src="imageURL(images[0])" width="150"/>
                            </a>
                        </router-link>
                        <img v-else :src="imageURL(images[0])" width="150"/>
                    </div>
                </div>
                <div v-else-if="layout === 1" style="text-align: center">
                    <template v-for="i in imagesSliced">
                        <router-link :key="i.id" v-if="postId" :to="postLink" v-slot="{ href, navigate }">
                            <a @click="navigate" :href="href">
                                <img :src="imageURL(i)" width="150" style="margin-left: 10px; margin-right: 10px"/>
                            </a>
                        </router-link>
                        <img v-else :key="i.id" :src="imageURL(i)" width="150" style="margin-left: 10px; margin-right: 10px"/>
                    </template>
                </div>
                <div v-else>
                    <div style="text-align: center">
                        <template v-for="i in imagesSliced">
                            <router-link :key="i.id" v-if="postId" :to="postLink" v-slot="{ href, navigate }">
                                <a @click="navigate" :href="href">
                                    <img :src="imageURL(i)" width="150" style="margin-left: 10px; margin-right: 10px" />
                                </a>
                            </router-link>
                            <img v-else :key="i.id" :src="imageURL(i)" width="150" style="margin-left: 10px; margin-right: 10px" />
                        </template>
                    </div>
                    <br />
                    <span>{{ textPreview }}</span>
                </div>
            </div>
        </div>
        <div class="card-action">
            <span>{{ date }}</span>
            <span style="float: right">
                <router-link v-if="postId" :to="postLink" v-slot="{ href, navigate }">
                    <a @click="navigate" :href="href" style="color: black">weiterlesen</a>
                </router-link>
                <span v-else>WEITERLESEN</span>
            </span>
        </div>
    </div>
</template>

<script>
export default {
    name: 'PostPreview',
    props: ['postId', 'date' , 'title', 'text', 'preview', 'layout', 'images'],
    computed: {
        textPreview: function() {
            if (!this.text) return null;
            const span = document.createElement('span');
            span.innerHTML = this.text;
            return (span.textContent || span.innerText).slice(0, this.preview).replace(/\s\s+/g, ' ') + '...';
        },
        imagesSliced: function() {
            return this.images.slice(0, 5);
        },
        hasImages: function () {
            return this.images.length !== 0;
        },
        imageURL: function () {
            return (image) => '/files/images/'+image.id+'_'+image.fileName;
        },
        postLink() {
            return '/beitrag/'+this.postId;
        }
    },
}
</script>