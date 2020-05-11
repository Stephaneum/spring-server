
<#-- manages variables of the system -->

<#macro render>
    <template id="variable-manager">
        <div class="card-panel" style="margin-top: 60px">
            <span style="font-size: 2rem">Variablen</span>
            <br>
            <span class="section-title">Pfade</span>
            <div class="section-box">
                <span class="section-info">Änderungen werden nur in der Datenbank vorgenommen. Dateien werden nicht auf der Festplatte verschoben.</span>
                <div class="variable-input">
                    <span class="variable-label">Dateien:</span>
                    <div class="input-field" style="width: 400px">
                        <input v-model:value="variables.fileLocation" type="text"/>
                    </div>
                    <a v-if="variables.fileLocation !== savedVariables.fileLocation" class="waves-effect waves-light btn green darken-3" style="margin-left: 30px"
                       @click="saveVariable('fileLocation', variables.fileLocation)">
                        <i class="material-icons left">save</i>
                        Speichern
                    </a>
                </div>
                <div class="variable-input">
                    <span class="variable-label">Backup:</span>
                    <div class="input-field" style="width: 400px">
                        <input v-model:value="variables.backupLocation" type="text"/>
                    </div>
                    <a v-if="variables.backupLocation !== savedVariables.backupLocation" class="waves-effect waves-light btn green darken-3" style="margin-left: 30px"
                       @click="saveVariable('backupLocation', variables.backupLocation)">
                        <i class="material-icons left">save</i>
                        Speichern
                    </a>
                </div>
            </div>

            <span class="section-title">Speicherplatz</span>
            <div class="section-box">
                <span class="section-info">Diese Regeln gelten nur für zukünftige neue Nutzer.</span>
                <div class="variable-input">
                    <span class="variable-label">Lehrer:</span>
                    <div class="input-field" style="width: 60px">
                        <input v-model:value="variables.storageTeacher" type="text"/>
                    </div>
                    MB
                    <a v-if="variables.storageTeacher !== savedVariables.storageTeacher" class="waves-effect waves-light btn green darken-3" style="margin-left: 30px"
                       @click="saveVariable('storageTeacher', variables.storageTeacher)">
                        <i class="material-icons left">save</i>
                        Speichern
                    </a>
                </div>
                <div class="variable-input">
                    <span class="variable-label">Schüler:</span>
                    <div class="input-field" style="width: 60px">
                        <input v-model:value="variables.storageStudent" type="text"/>
                    </div>
                    MB
                    <a v-if="variables.storageStudent !== savedVariables.storageStudent" class="waves-effect waves-light btn green darken-3" style="margin-left: 30px"
                       @click="saveVariable('storageStudent', variables.storageStudent)">
                        <i class="material-icons left">save</i>
                        Speichern
                    </a>
                </div>
            </div>

            <span class="section-title">Andere</span>
            <div class="section-box">
                <span class="section-info">Bilder werden komprimiert, falls die Grenze überschritten wird.</span>
                <div class="variable-input">
                    <span class="variable-label">Max. Bildgröße in Beiträgen:</span>
                    <div class="input-field" style="width: 60px">
                        <input v-model:value="variables.maxPictureSize" type="text"/>
                    </div>
                    KB
                    <a v-if="variables.maxPictureSize !== savedVariables.maxPictureSize" class="waves-effect waves-light btn green darken-3" style="margin-left: 30px"
                       @click="saveVariable('maxPictureSize', variables.maxPictureSize)">
                        <i class="material-icons left">save</i>
                        Speichern
                    </a>
                </div>
                <div class="variable-input">
                    <span class="variable-label">Passwort-Reset-Timeout:</span>
                    <div class="input-field" style="width: 60px">
                        <input v-model:value="variables.passwordResetTimeout" type="text"/>
                    </div>
                    h
                    <a v-if="variables.passwordResetTimeout !== savedVariables.passwordResetTimeout" class="waves-effect waves-light btn green darken-3" style="margin-left: 30px"
                       @click="saveVariable('passwordResetTimeout', variables.passwordResetTimeout)">
                        <i class="material-icons left">save</i>
                        Speichern
                    </a>
                </div>
            </div>
        </div>
    </template>

    <script type="text/javascript">
        Vue.component('variable-manager', {
            data: function () {
                return {
                    savedVariables: {
                        storageTeacher: null,
                        storageStudent: null,
                        fileLocation: null,
                        backupLocation: null,
                        maxPictureSize: null,
                        passwordResetTimeout: null
                    },
                    variables: {
                        storageTeacher: null,
                        storageStudent: null,
                        fileLocation: null,
                        backupLocation: null,
                        maxPictureSize: null,
                        passwordResetTimeout: null
                    },
                }
            },
            methods: {
                fetchData: async function() {
                    const variables = await axios.get('/api/variable');
                    this.variables = {
                        storageTeacher: variables.data.storageTeacher / (1024 * 1024), // B -> MB
                        storageStudent: variables.data.storageStudent / (1024 * 1024), // B -> MB
                        fileLocation: variables.data.fileLocation,
                        backupLocation: variables.data.backupLocation,
                        maxPictureSize: variables.data.maxPictureSize / 1024, // B -> KB
                        passwordResetTimeout: variables.data.passwordResetTimeout / (60 * 60 * 1000) // ms -> h
                    };
                    for (key in this.variables) {
                        this.variables[key] = this.variables[key].toString()
                    }
                    this.savedVariables = { ...this.variables };
                    hideLoading();
                },
                saveVariable: async function(key, value) {
                    switch(key) {
                        case 'storageTeacher':
                        case 'storageStudent':
                            value = 1024 * 1024 * parseInt(value); // MB to B
                            break;
                        case 'fileLocation':
                        case 'backupLocation':
                            break;
                        case 'maxPictureSize':
                            value = 1024 * parseInt(value); // KB to B
                            break;
                        case 'passwordResetTimeout':
                            value = parseInt(value) * (60 * 60 * 1000);
                            break;
                    }
                    await axios.post('/api/variable/update', { key, value });
                    await this.fetchData();
                    M.toast({html: 'Änderungen gespeichert.'});
                },
            },
            mounted: async function() {
                await this.fetchData();
            },
            template: '#variable-manager'
        });
    </script>

    <style>
        .section-title {
            display: inline-block;
            margin-top: 30px;
            font-size: 1.2rem;
            font-weight: bold;
        }

        .section-box {
            padding-left: 40px;
            padding-top: 20px;
        }

        .section-info {
            font-style: italic;
        }

        .variable-input {
            display: flex;
            align-items: center;
        }

        .variable-label {
        }

        .variable-input .input-field {
            margin-left: 20px;
        }

        .variable-input a {
            margin-left: 30px;
        }
    </style>
</#macro>