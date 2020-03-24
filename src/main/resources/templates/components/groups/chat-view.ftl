
<#-- view chat -->

<#macro render>
    <template id="chat-view">
        <div class="card" style="margin: 0; width: 100%; height: 100%; display: flex; flex-direction: column">
            <div v-if="disabledAll" class="chat-info">
                Chat für alle Mitglieder deaktiviert.
            </div>
            <template v-else>
                <div id="chat-scroll-panel" style="height: 500px; overflow-y: scroll;">
                    <div v-if="fetched && messages.length === 0" class="chat-info">
                        Noch keine Nachrichten
                    </div>
                    <template v-else>
                        <div v-for="(m, index) in messages" @mouseover="hoveringMessage = m" @mouseleave="hoveringMessage = {}">

                            <div v-if="index === 0 || messages[index - 1].date !== m.date" class="chat-date-wrapper">
                                <span class="chat-date">{{ m.date }}</span>
                            </div>

                            <div class="chat-message-user">
                                {{ m.user.firstName }} {{ m.user.lastName }}
                                <i v-if="modifyAll && hoveringMessage.id === m.id" @click="showDeleteMessage(m)" class="material-icons chat-delete-message">delete</i>
                            </div>
                            <div class="chat-message-body">
                                {{ m.text }}
                                <div class="chat-message-time">
                                    {{ m.time }}
                                </div>
                            </div>
                        </div>

                        <div style="height: 30px"></div>
                    </template>
                </div>

                <div style="display: flex; align-items: center; margin: 10px 20px 0 20px">
                    <div style="flex: 1;" class="input-field">
                        <i class="material-icons prefix">chat</i>
                        <label for="add-message-text">{{ disabledMe ? 'Chat gesperrt' : 'Nachricht' }}</label>
                        <input @keyup.enter="addMessage" v-model:value="message" type="text" id="add-message-text" :disabled="disabledMe"/>
                    </div>
                    <a @click="addMessage" href="#!" style="margin-left: 20px" class="modal-close waves-effect waves-light btn green darken-4" :class="{'disabled' : disabledMe}">
                        <i class="material-icons left">send</i>
                        Senden
                    </a>
                </div>
            </template>

            <!-- delete message modal -->
            <div id="modal-delete-message" class="modal">
                <div class="modal-content">
                    <h4>Nachricht entfernen?</h4>
                    <br>
                    <p>Folgende Nachricht wird gelöscht:</p>
                    <p style="font-style: italic" class="grey lighten-3">{{ selectedMessage.text }}</p>
                    <p>Dieser Vorgang kann nicht rückgangig gemacht werden.</p>
                </div>
                <div class="modal-footer">
                    <a @click="closeDeleteMessage" href="#!" class="modal-close waves-effect waves-green btn-flat">Abbrechen</a>
                    <a @click="deleteMessage" href="#!" class="modal-close waves-effect waves-red btn red darken-4">
                        <i class="material-icons left">delete</i>
                        Löschen
                    </a>
                </div>
            </div>
        </div>
    </template>

    <script type="text/javascript">
        Vue.component('chat-view', {
            props: ['disabledAll', 'disabledMe', 'modifyAll', 'messageCountUrl', 'messagesUrl', 'addMessageUrl', 'clearUrl'],
            data: function () {
                return {
                    messages: [],
                    message: null,
                    hoveringMessage: {},
                    selectedMessage: {},
                    fetched: false
                }
            },
            methods: {
                fetchData: async function() {
                    if(this.disabledAll)
                        return;
                    try {
                        const count = await axios.get(this.messageCountUrl);
                        if(!this.fetched || count.data.count !== this.messages.length) {
                            const messages = await axios.get(this.messagesUrl);
                            messages.data.forEach((m) => {
                                m.date = moment(m.timestamp).format('DD.MM.YYYY');
                                m.time = moment(m.timestamp).format('HH:mm');
                            });
                            this.messages = messages.data;
                            this.fetched = true;
                            this.$nextTick(() => {
                                const scrollPanel = document.getElementById("chat-scroll-panel");
                                scrollPanel.scrollTop = scrollPanel.scrollHeight;
                            });
                        }
                    } catch (e) {
                        M.toast({html: 'Interner Fehler.'});
                    }

                    hideLoading();
                },
                addMessage: async function() {

                    if(!this.message || this.disabledMe)
                        return;

                    try {
                        await axios.post(this.addMessageUrl, { message: this.message });
                        await this.fetchData();
                        this.message = null;
                    } catch (e) {
                        M.toast({html: 'Interner Fehler.'});
                    }
                },
                showDeleteMessage: function(message) {
                    this.selectedMessage = message;
                    M.Modal.getInstance(document.getElementById('modal-delete-message')).open();
                },
                closeDeleteMessage: function() {
                    M.Modal.getInstance(document.getElementById('modal-delete-message')).close();
                },
                deleteMessage: async function() {
                    M.Modal.getInstance(document.getElementById('modal-delete-message')).close();
                    try {
                        showLoadingInvisible();
                        await axios.post('/api/chat/delete/' + this.selectedMessage.id);
                        await this.fetchData();
                    } catch (e) {
                        M.toast({html: 'Interner Fehler.'});
                    }
                }
            },
            computed: {
            },
            mounted: function() {
                M.AutoInit();
                moment.locale('de');
                this.fetchData();
            },
            template: '#chat-view'
        });
    </script>

    <style>

        .chat-info {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center
        }

        .chat-date-wrapper {
            text-align: center;
        }

        .chat-date {
            display: inline-block;
            margin: 30px 0 10px 0;
            background-color: #eceff1;
            border-radius: 20px;
            padding: 0 10px 0 10px;
            font-size: 0.8em;
        }

        .chat-message-user {
            margin: 20px 0 5px 20px;
            font-weight: bold;
        }

        .chat-delete-message {
            margin-left: 5px;
            font-size: 1em;
            cursor: pointer;
        }

        .chat-message-time {
            text-align: right;
            margin: 0 0 0 0;
            font-size: 0.8em;
            color: #9e9e9e;
        }

        .chat-message-body {
            display: inline-block;
            min-width: 70px;
            background-color: #dcedc8;
            border-radius: 0 20px 20px 20px;
            margin: 0 20px 0 20px;
            padding: 10px;
        }
    </style>
</#macro>