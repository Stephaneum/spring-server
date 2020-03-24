
<#-- view chat -->

<#macro render>
    <template id="chat-view">
        <div class="card" style="margin: 0; width: 100%; height: 100%; display: flex; flex-direction: column">
            <div id="chat-scroll-panel" style="height: 500px; overflow-y: scroll;">
                <div v-for="m in messages">
                    <div class="chat-message-head">
                        <div class="chat-message-user">
                            {{ m.user.firstName }} {{ m.user.lastName }}
                        </div>
                    </div>
                    <div class="chat-message-body" @mouseover="selectedMessage = m" @mouseleave="selectedMessage = {}">
                        {{ m.text }}
                        <div class="chat-message-time">
                            <span v-if="selectedMessage.id === m.id">{{ m.date }} / </span>{{ m.time }}
                        </div>
                    </div>
                </div>
            </div>

            <div style="display: flex; align-items: center; margin: 10px 20px 0 20px">
                <div style="flex: 1;" class="input-field">
                    <i class="material-icons prefix">chat</i>
                    <label for="add-message-text">Nachricht</label>
                    <input @keyup.enter="addMessage" v-model:value="message" type="text" id="add-message-text"/>
                </div>
                <a @click="addMessage" href="#!" style="margin-left: 20px" class="modal-close waves-effect waves-light btn green darken-4">
                    <i class="material-icons left">send</i>
                    Senden
                </a>
            </div>
        </div>
    </template>

    <script type="text/javascript">
        Vue.component('chat-view', {
            props: ['messageCountUrl', 'messagesUrl', 'addMessageUrl', 'clearUrl', 'deleteUrl'],
            data: function () {
                return {
                    messages: [],
                    message: null,
                    selectedMessage: {},
                }
            },
            methods: {
                fetchData: async function() {
                    try {
                        const count = await axios.get(this.messageCountUrl);
                        if(count.data.count !== this.messages.length) {
                            const messages = await axios.get(this.messagesUrl);
                            messages.data.forEach((m) => {
                                m.date = moment(m.timestamp).format('DD.MM.YYYY');
                                m.time = moment(m.timestamp).format('HH:mm');
                            });
                            this.messages = messages.data;
                        }
                    } catch (e) {
                        M.toast({html: 'Interner Fehler.'});
                    }

                    hideLoading();
                },
                addMessage: async function() {

                    if(!this.message)
                        return;

                    try {
                        await axios.post(this.addMessageUrl, { message: this.message });
                        await this.fetchData();
                        this.message = null;
                        const scrollPanel = document.getElementById("chat-scroll-panel");
                        scrollPanel.scrollTop = scrollPanel.scrollHeight;
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

        .chat-message-head {
            display: flex;
            align-items: end;
        }

        .chat-message-user {
            margin: 20px 0 5px 20px;
            font-weight: bold;
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