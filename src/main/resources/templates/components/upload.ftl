
<#-- logos on the right -->

<#macro render>
    <template id="file-upload">
        <div>
            <slot :upload="showUpload"></slot>
            <form method="POST" enctype="multipart/form-data">
                <input name="file" type="file" id="input-upload" @change="upload" style="display: none">
            </form>
        </div>
    </template>

    <script type="text/javascript">
        Vue.component('file-upload', {
            props: ['url'],
            methods: {
                showUpload: function() {
                    document.getElementById('input-upload').click();
                },
                upload: function(event) {
                    event.preventDefault();
                    var files = event.dataTransfer ? event.dataTransfer.files : event.currentTarget.files;
                    if(files.length !== 1) {
                        M.toast({html: 'Nur eine Datei erlaubt.'});
                        return;
                    }
                    this.uploadHelper(this.url, files, {
                        params: {},
                        uploaded: (file) => {},
                        finished: () => {
                            this.$emit('upload');
                        }
                    });
                },
                uploadHelper: async function(url, files, { params, uploaded, finished }, index = 0) {

                    var infoStart = files.length === 1 ? 'Hochladen (0%)' : '[' + (index+1) + '/' + files.length + '] [0%]' + files[index].name;
                    showLoading(infoStart);
                    var data = new FormData();
                    data.append('file', files[index]);
                    for(var key in params) {
                        data.append(key, params[key])
                    }
                    var config = {
                        onUploadProgress: function(progressEvent) {
                            var percentCompleted = Math.round( (progressEvent.loaded * 100) / progressEvent.total );
                            var infoProcess = files.length === 1 ?
                                'Hochladen ('+ percentCompleted +'%)' :
                                '[' + (index+1) + '/' + files.length + '] [' + percentCompleted + '%] ' + files[index].name;
                            showLoading(infoProcess, percentCompleted);
                        }
                    };

                    try {
                        const res = await axios.post(url, data, config);
                        uploaded(res.data);
                        if(index < files.length-1)
                            uploadMultipleFiles(url, files, { params, uploaded, finished }, index+1);
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
            },
            template: '#file-upload'
        });
    </script>
</#macro>