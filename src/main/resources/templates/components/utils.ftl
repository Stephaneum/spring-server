<#macro render>
    <script type="text/javascript">

        var TEXT_EDITOR_CONFIG = {
            semantic: false,
            lang: 'de',
            btns: [
                ['strong', 'em', 'underline', 'del'],
                ['foreColor', 'backColor'],
                ['formatting', 'fontsize'],
                ['superscript', 'subscript'],
                ['link'],
                ['table'],
                ['justifyLeft', 'justifyCenter', 'justifyRight', 'justifyFull'],
                ['orderedList'],
                ['horizontalRule'],
                ['removeformat'],
                ['viewHTML'],
                ['fullscreen']],
            plugins: {
                fontsize: {
                    sizeList: [
                        '10pt',
                        '11pt',
                        '12pt',
                        '14pt',
                        '16pt',
                        '18pt',
                        '24pt'
                    ],
                    allowCustomSize: false
                }
            }
        };

        function initTooltips() {
            M.Tooltip.init(document.querySelectorAll('.tooltipped'), {});
        }

        function storageReadable(size) {
            if(size < 1024)
                return size + ' B';
            else if(size < 1024 *1024)
                return Math.round(size / 1024) + ' KB';
            else if(size < 1024 * 1024 * 1024)
                return Math.round(size / (1024 * 1024)) + ' MB';
            else
                return Math.round(size / (1024 * 1024 * 1024)) + ' GB';
        }

        function uploadMultipleFiles(url, files, { params, uploaded, finished }, index = 0) {

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
            axios.post(url, data, config)
                .then((res) => {
                    uploaded(res.data);
                    if(index < files.length-1)
                        uploadMultipleFiles(url, files, { params, uploaded, finished }, index+1);
                    else {
                        if(files.length === 1) M.toast({ html: 'Datei hochgeladen.' });
                        else M.toast({ html: 'Dateien hochgeladen.' });
                        hideLoading(); // finished successfully
                        finished();
                    }
                })
                .catch(function (err) {
                    M.toast({ html: 'Ein Fehler ist aufgetreten.' });
                    console.log(err);
                    hideLoading(); // frontend error
                });
        }
    </script>
</#macro>