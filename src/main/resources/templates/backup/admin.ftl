<#import "/spring.ftl" as spring/>
<#import "../components/loading.ftl" as loading/>
<#import "../components/toaster.ftl" as toaster/>
<#setting locale="de_DE">
<#setting number_format="computer">

<!DOCTYPE HTML>
<html lang="de">
<head>
    <title>Backup</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="<@spring.url '/static/img/favicon.png' />"/>
    <link rel="apple-touch-icon" sizes="196x196" href="<@spring.url '/static/img/favicon.png' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/materialize.min.css' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/material-icons.css' />">
    <link rel="stylesheet" type="text/css" href="<@spring.url '/static/css/style.css' />">
    <style>
        .backup-card {
            flex-basis: 400px;
            min-height: 600px;
            text-align: center;

            display: flex;
            flex-direction: column;
            justify-content: space-between;
        }

        .info-card {
            text-align: center;
            background-color: #dcedc8;
            color: black;
            border-radius: 20px;
            padding: 10px 10px 10px 10px;
            margin: 0 20px 0 20px;
            font-size: 1.2em;
            flex-shrink: 0;
        }

        .action-btn {
            margin: 0 10px 0 10px;
        }

        .collapsible li.active .collapsible-header {
            background-color: #f1f8e9;
        }
    </style>
</head>

<body>

<div id="app">
    <div style="display: flex; justify-content: center">
        <div style="width: 1300px; margin-bottom: 100px">
            <!-- title -->
            <div style="display: flex; justify-content: space-between; align-items: center; margin: 30px 50px 0 50px">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <img src="<@spring.url '/static/img/favicon.png' />" style="width: 50px"/>
                    <h4 style="color: #396e3a; margin-left: 10px; padding-bottom: 5px">Backup-System</h4>
                </div>

                <a @click="logout" class="waves-effect waves-light btn teal darken-3">
                    <i class="material-icons right">exit_to_app</i>Abmelden</a>
            </div>

            <!-- main card -->

            <div class="card" style="background-color: #f1f8e9; margin-top: 50px; padding: 20px;display: flex; justify-content: center; align-items: center;">
                <div class="info-card">
                    <b>Nächste automatische Sicherung:</b>
                    <br>
                    {{ nextBackup }}
                </div>

                <div class="info-card">
                    <b>Sicherungspfad:</b>
                    <br>
                    {{ backupLocation }}
                </div>

                <div class="info-card">
                    <b>Gesamtgröße:</b>
                    <br>
                    {{ totalSize }}
                </div>

                <div style="text-align: center; flex-basis: 500px">
                    <a @click="backupAll" class="backup-btn waves-effect waves-light btn-large green darken-3" style="margin: 5px 0 0 0;font-size: 1.3em;">
                        <i class="material-icons left">photo_camera</i>Backup erstellen
                    </a>
                    <p style="margin: 10px 0 0 0">Hierbei werden Backups von <b>allen</b> Modulen erstellt.</p>
                </div>
            </div>
            <!-- cards -->
            <div style="display: flex; justify-content: space-between; margin-top: 50px">

                <div v-for="m in modules" class="card backup-card">
                    <div>
                        <h4 style="text-align: center">{{ m.title }}</h4>
                        <br>
                        <ul v-if="m.backups.length > 0" class="collapsible" style="box-shadow: none !important;">
                            <li v-for="b in m.backups">
                                <div class="collapsible-header">
                                    <div style="display: flex; justify-content: space-between; width: 100%;">
                                        <span>{{ b.name }}</span>
                                        <span class="green-badge-light">{{ b.size }}</span>
                                    </div>
                                </div>
                                <div class="collapsible-body light-green lighten-5">
                                    <div style="display: flex; justify-content: center; align-items: center">
                                        <i class="material-icons" style="display: inline-block; margin: 0 10px 10px 0; font-size: 2em;">subdirectory_arrow_right</i>
                                        <span style="margin-right: 10px">Aktionen:</span>
                                        <a class="action-btn tooltipped waves-effect waves-light btn green darken-3" data-tooltip="Download" data-position="bottom"
                                           :href="downloadURL(m.code,b.name)">
                                            <i class="material-icons">arrow_downward</i></a>
                                        <a class="action-btn tooltipped waves-effect waves-light btn yellow darken-3" :class="{ disabled: m.passwordNeeded }" data-tooltip="Wiederherstellen" data-position="bottom"
                                           @click="restore(m.code, b.name)">
                                            <i class="material-icons">restore</i></a>
                                        <a class="action-btn tooltipped waves-effect waves-light btn red darken-3" data-tooltip="Löschen" data-position="bottom"
                                           @click="showDelete(m.code, b.name, b.size)">
                                            <i class="material-icons">delete</i></a>
                                    </div>

                                </div>
                            </li>
                        </ul>
                        <div v-else>
                            <p class="green-badge-light" style="display: inline-block; font-size: 1em">Keine Backups vorhanden</p>
                        </div>
                        <div v-if="m.passwordNeeded">
                            <a class="waves-effect waves-light btn red darken-3 modal-trigger" style="margin-top: 30px" href="#modal-password">
                                <i class="material-icons left">vpn_key</i>sudo-Passwort eingeben
                            </a>
                        </div>
                    </div>

                    <div>
                        <form method="POST" enctype="multipart/form-data" style="display: inline-block">
                            <input name="file" type="file" :id="'upload-'+ m.code" @change="upload($event.currentTarget.files[0], m.code)" style="display: none">
                            <a class="waves-effect waves-light btn teal darken-3" style="margin-top: 30px"
                                @click="showUpload(m.code)">
                                <i class="material-icons left">cloud_upload</i>Backup hochladen
                            </a>
                        </form>

                        <a class="waves-effect waves-light btn-large green darken-3"
                            :class="{ disabled: m.passwordNeeded }"
                           style="margin: 30px 50px 50px 50px;font-size: 1.3em;"
                           @click="backup(m.code)">
                            <i class="material-icons left">photo_camera</i>Backup erstellen
                        </a>

                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="modal-delete" class="modal">
        <div class="modal-content">
            <h4>Backup wirklich löschen?</h4>
            <br>
            <p><b>{{ backupDelete.name }}</b> ({{ backupDelete.size }}) wird gelöscht.</p>
            <p>Dieser Vorgang kann nicht rückgangig gemacht werden.</p>
        </div>
        <div class="modal-footer">
            <a @click="closeDelete" href="#!" class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
            <a @click="deleteBackup" href="#!" class="modal-close waves-effect waves-red btn red darken-4">Löschen</a>
        </div>
    </div>
    <div id="modal-password" class="modal" style="width: 500px">
        <div class="modal-content">
            <h4>sudo-Passwort</h4>
            <br>
            <p>Bitte geben Sie unten das sudo-Passwort des Servers ein.</p>
            <p>Das Passwort bleibt im RAM und wird nicht persistent gespeichert.</p>
            <br>
            <div class="input-field">
                <i class="material-icons prefix">vpn_key</i>
                <label for="modal-password-input">Passwort</label>
                <input @keyup.enter="setSudoPassword" v-model:value="sudoPassword" id="modal-password-input" name="password" type="password">
            </div>
        </div>
        <div class="modal-footer">
            <a @click="closePassword" href="#!"
               class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
            <button @click="setSudoPassword" type="button" class="btn waves-effect waves-light green darken-3">
                Speichern
                <i class="material-icons left">save</i>
            </button>
        </div>
    </div>
</div>

<script src="/static/js/materialize.min.js" ></script>
<script src="/static/js/axios.min.js" ></script>
<script src="/static/js/vue.min.js" ></script>
<script type="text/javascript">

    var app = new Vue({
        el: '#app',
        data: {
            nextBackup: "",
            backupLocation: "",
            totalSize: "",
            modules: [],
            backupDelete: { module: null, name: null, size: null },
            sudoPassword: null
        },
        methods: {
            setSudoPassword: function() {
                M.Modal.getInstance(document.getElementById('modal-password')).close();
                showLoading('Passwort setzen...');
                axios.post( './set-password', { password: this.sudoPassword })
                    .then((response) => {
                        if(response.data.success) {
                            M.toast({html: 'Passwort im RAM hinterlegt.'});
                        } else {
                            M.toast({html: 'Falsches Passwort.'});
                        }
                        fetchData(this);
                        hideLoading();
                    });
            },
            showDelete: function(moduleCode, name, size) {
                this.backupDelete = { moduleCode, name, size};
                M.Modal.getInstance(document.getElementById('modal-delete')).open();
            },
            closeDelete: function() {
                M.Modal.getInstance(document.getElementById('modal-delete')).close();
            },
            deleteBackup: function() {
                showLoading('Backup löschen...');
                axios.post( './delete/'+this.backupDelete.moduleCode+'/'+this.backupDelete.name)
                .then((response) => {
                    if(response.data.success) {
                        M.toast({html: 'Backup gelöscht.<br>'+this.backupDelete.name});
                    } else {
                        M.toast({html: 'Löschen fehlgeschlagen.<br>'+this.backupDelete.name});
                    }
                    fetchData(this);
                    hideLoading();
                });
            },
            closePassword: function() {
                M.Modal.getInstance(document.getElementById('modal-password')).close();
            },
            showUpload: function(moduleCode) {
                document.getElementById('upload-'+moduleCode).click();
            },
            upload: function(file, moduleCode) {
                showLoading('Hochladen: 0 %');
                console.log('file: ' + file);
                console.log('module: ' + moduleCode);
                var data = new FormData();
                data.append('file', file);
                var config = {
                    onUploadProgress: function(progressEvent) {
                        var percentCompleted = Math.round( (progressEvent.loaded * 100) / progressEvent.total );
                        showLoading('Hochladen: '+ percentCompleted +' %');
                    }
                };
                var instance = this;
                axios.post('./upload-' + moduleCode, data, config)
                    .then(function (res) {
                        if(res.data.success) {
                            fetchData(instance);
                            M.toast({ html: 'Datei hochgeladen.' });
                        } else if(res.data.message) {
                            M.toast({ html: res.data.message });
                        }
                    })
                    .catch(function (err) {
                        M.toast({ html: 'Ein Fehler ist aufgetreten.' });
                        console.log(err);
                    })
                    .finally(function () {
                        hideLoading();
                    });
            },
            backupAll: function(moduleCode) {
                axios.post('./backup')
                    .then((response) => {
                        if(response.data.success) {
                            window.location = 'logs';
                        } else {
                            M.toast({html: 'Backup fehlgeschlagen.'});
                        }
                    });
            },
            backup: function(moduleCode) {
                axios.post('./backup-' + moduleCode)
                .then((response) => {
                    if(response.data.success) {
                        window.location = 'logs';
                    } else {
                        M.toast({html: 'Backup fehlgeschlagen.'});
                    }
                });
            },
            restore: function(moduleCode, name) {
                axios.post('./restore/' + moduleCode + '/' + name)
                    .then((response) => {
                        if(response.data.success) {
                            window.location = 'logs';
                        } else {
                            M.toast({html: 'Wiederherstellung fehlgeschlagen.'});
                        }
                    });
            },
            logout: function() {
                axios.post('./logout')
                    .then((response) => {
                        if(response.data.success) {
                            window.location = 'login';
                        } else {
                            M.toast({html: 'Logout fehlgeschlagen.'});
                        }
                    });
            }
        },
        mounted: function () {
            this.$nextTick(() => {
                fetchData(this);
            })
        },
        computed: {
            downloadURL() {
                return (module, name) => './download/'+module+'/'+name;
            },
            restoreURL() {
                return (module, name) => './restore/'+module+'/'+name;
            }
        },
        watch: {
            modules: function (val, oldVal) {
                this.$nextTick(function () {
                    M.AutoInit();
                    console.log("Materialize auto init")
                })
            },
        }
    });

    function fetchData(instance) {
        axios.get('data')
            .then((response) => {
            if(response.data) {

                if(response.data.needLogin) {
                    window.location = 'login';
                    return;
                }

                instance.nextBackup = response.data.nextBackup;
                instance.backupLocation = response.data.backupLocation;
                instance.totalSize = response.data.totalSize;
                instance.modules = response.data.modules;
                
                console.log("Data fetched.");
            } else {
                M.toast({ html: 'Backups konnten nicht geladen werden' });
            }
        });
    }

    M.AutoInit();

</script>
<@loading.render/>
<@toaster.render/>
</body>
</html>