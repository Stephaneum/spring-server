<#macro render>
    <template id="post-preview">
        <div class="card">
            <div class="card-content">
                <span class="card-title">
                    <a :href="postURL" style="color: black">{{ title }}</a>
                </span>

                <p v-if="!hasImages">{{ textPreview }}</p>

                <div v-else>
                    <div v-if="layout === 0" class="row">
                        <div class="col m8">
                            <span>{{ textPreview }}</span>
                        </div>
                        <div class="col m4" style="text-align: right; vertical-align: top; overflow: hidden">
                            <a :href="postURL">
                                <img :src="imageURL(images[0])" width="150"/>
                            </a>
                        </div>
                    </div>
                    <div v-else-if="layout === 1" style="text-align: center">
                        <a v-for="i in imagesSliced" :href="postURL">
                            <img :src="imageURL(i)" width="150" style="margin-left: 10px; margin-right: 10px"/>
                        </a>
                    </div>
                    <div v-else>
                        <div style="text-align: center">
                            <a v-for="i in imagesSliced" :href="postURL">
                                <img :src="imageURL(i)" width="150" style="margin-left: 10px; margin-right: 10px"/>
                            </a>
                        </div>
                        <br />
                        <span>{{ textPreview }}</span>
                    </div>
                </div>
            </div>
            <div class="card-action">
                <span>{{ date }}</span>
                <span style="float: right"><a :href="postURL" style="color: black">weiterlesen</a></span>
            </div>
        </div>
    </template>

    <script type="text/javascript">
        Vue.component('post-preview', {
            props: ['post_id', 'date' , 'title', 'text', 'preview', 'layout', 'images'],
            methods: {
            },
            computed: {
                textPreview: function() {
                    return this.text ? this.text.replace(/<[^>]*>?/gm, '').slice(0, this.preview) + '...' : null;
                },
                imagesSliced: function() {
                    return this.images.slice(0, 5);
                },
                hasImages: function () {
                    return this.images.length !== 0;
                },
                imageURL: function () {
                    return (image) => './api/images/'+image.fileNameWithID;
                },
                postURL: function () {
                    if(this.post_id > 1)
                        return 'beitrag.xhtml?id=' + this.post_id;
                    else
                        return null;
                }
            },
            template: '#post-preview'
        });
    </script>

    <style>
    </style>
</#macro>