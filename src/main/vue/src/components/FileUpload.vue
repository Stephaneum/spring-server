<template>
    <div style="display: inline-block">
        <slot :upload="showUpload"></slot>
        <form method="POST" enctype="multipart/form-data">
            <input ref="inputUpload" name="file" type="file" @change="upload" style="display: none">
        </form>
    </div>
</template>

<script>
    import Axios from "axios"
    import M from "materialize-css"
    import { showLoading, hideLoading } from '@/helper/utils.js';

export default {
    name: 'FileUpload',
    props: ['url'],
    methods: {
        showUpload: function() {
            this.$refs.inputUpload.click();
        },
        upload: async function(event) {
            await this.$emit('start');
            event.preventDefault();
            const files = event.dataTransfer ? event.dataTransfer.files : event.currentTarget.files;
            if(files.length !== 1) {
                M.toast({html: 'Nur eine Datei erlaubt.'});
                return;
            }
            await this.uploadHelper(this.url, files, {
                params: {},
                uploaded: () => {
                },
                finished: () => {
                    this.$emit('upload');
                }
            });
        },
        uploadHelper: async function(url, files, { params, uploaded, finished }, index = 0) {

            const infoStart = files.length === 1 ? 'Hochladen (0%)' : '[' + (index + 1) + '/' + files.length + '] [0%]' + files[index].name;
            showLoading(infoStart);
            const data = new FormData();
            data.append('file', files[index]);
            for(var key in params) {
                data.append(key, params[key])
            }
            const config = {
                onUploadProgress: function (progressEvent) {
                    const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
                    const infoProcess = files.length === 1 ?
                        'Hochladen (' + percentCompleted + '%)' :
                        '[' + (index + 1) + '/' + files.length + '] [' + percentCompleted + '%] ' + files[index].name;
                    showLoading(infoProcess, percentCompleted);
                }
            };

            try {
                const res = await Axios.post(url, data, config);
                uploaded(res.data);
                if(index < files.length-1)
                    this.uploadMultipleFiles(url, files, { params, uploaded, finished }, index+1);
                else {
                    if(files.length === 1) M.toast({ html: 'Datei hochgeladen.' });
                    else M.toast({ html: 'Dateien hochgeladen.' });
                    hideLoading(); // finished successfully
                    finished();
                }
            } catch (e) {
                hideLoading(); // frontend error
                this.$emit('error', e.response.status);
            }
        }
    }
}
</script>

<style>

</style>