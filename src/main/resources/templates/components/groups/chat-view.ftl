
<#-- view chat -->

<#macro render>
    <template id="chat-view">
        <div class="card" style="margin: 0; display: flex; flex-direction: column" :style="{ height: (height || 0)+'px' }">
            <div v-if="disabledAll" class="empty-hint">
                Chat für alle Mitglieder deaktiviert.
            </div>
            <template v-else>
                <div id="chat-scroll-panel" style="flex: 1; overflow-y: scroll;">
                    <div v-if="fetched && messages.length === 0" class="empty-hint">
                        Noch keine Nachrichten
                    </div>
                    <template v-else>
                        <div v-for="(m, index) in messages" @mouseover="hoveringMessage = m" @mouseleave="hoveringMessage = {}">

                            <div v-if="index === 0 || messages[index - 1].date !== m.date" class="chat-date-wrapper">
                                <span class="chat-date">{{ m.date }}</span>
                            </div>

                            <div class="chat-message-container">
                                <div class="chat-message-head">
                                    <div class="chat-message-user">
                                        {{ m.user.firstName }} {{ m.user.lastName }}
                                    </div>
                                    <i v-if="modifyAll && hoveringMessage.id === m.id" @click="showDeleteMessage(m)" class="material-icons chat-delete-message">delete</i>
                                </div>

                                {{ m.text }}
                                <div class="chat-message-time">
                                    {{ m.time }}
                                </div>
                            </div>
                        </div>

                        <div style="height: 30px"></div>
                    </template>
                </div>

                <div style="display: flex; align-items: center; padding: 10px 20px 0 20px; background-color: #fafafa; border-top: 1px solid #e0e0e0">
                    <div style="flex: 1;" class="input-field">
                        <i class="material-icons prefix">chat</i>
                        <label for="add-message-text">{{ disabledMe ? 'Chat gesperrt' : 'Nachricht' }}</label>
                        <input @keyup.enter="addMessage" v-model:value="message" type="text" id="add-message-text" :disabled="disabledMe || sending"/>
                    </div>
                    <a @click="addMessage" href="#!" style="margin-left: 20px" class="modal-close waves-effect waves-light btn green darken-4" :class="{'disabled' : disabledMe || sending}">
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
            props: ['disabledAll', 'disabledMe', 'modifyAll', 'messageCountUrl', 'messagesUrl', 'addMessageUrl', 'clearUrl', 'height'],
            data: function () {
                return {
                    messages: [],
                    message: null,
                    hoveringMessage: {},
                    selectedMessage: {},
                    fetched: false,
                    sending: false
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
                                const time = moment(m.timestamp);
                                m.date = time.format('DD.MM.YYYY');
                                m.time = time.format('HH:mm');
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

                    if(this.disabledMe || !this.message || this.message.trim().length === 0)
                        return;

                    try {
                        this.sending = true;
                        await axios.post(this.addMessageUrl, { message: this.message });
                        await this.fetchData();
                        this.message = null;
                        this.sending = false;
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
            watch: {
                messagesUrl: function() {
                    this.fetched = false;
                    this.fetchData();
                },
                disabledAll: function() {
                    this.fetched = false;
                    this.fetchData();
                    this.$nextTick(() => {
                        M.Modal.init(document.querySelectorAll('.modal'), {});
                    });
                }
            },
            mounted: async function() {
                M.AutoInit();
                moment.locale('de');

                if(!this.disabledAll) {
                    await this.fetchData();
                    setInterval(this.fetchData, 1500);
                }
            },
            template: '#chat-view'
        });
    </script>

    <style>

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

        .chat-message-head {
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .chat-message-user {
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

        .chat-message-container {
            display: inline-block;
            margin: 20px 20px 0 20px;
            min-width: 150px;
            background-color: #dcedc8;
            border-radius: 0 20px 20px 20px;
            padding: 10px;
        }
    </style>
</#macro>