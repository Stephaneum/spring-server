<template>
  <CenterLayout title="Kontakt" :plan="info.plan" :history="info.history" :eu-sa="info.euSa">
    <p>Du hast eine Frage, eine Idee oder möchtest etwas zu Begabungsförderung erzählen?</p>
    <p>Dann schreib mir gern.</p>

    <!-- honeypot against simple bots -->
    <input v-model="website" type="text" tabindex="-1" autocomplete="off" aria-hidden="true" style="position: absolute; left: -10000px"/>

    <div class="input-field">
      <label for="contact-name">Name</label>
      <input v-model="name" type="text" id="contact-name" maxlength="100" autocomplete="name"/>
    </div>
    <div class="input-field">
      <label for="contact-email">E-Mail-Adresse</label>
      <input v-model="email" type="email" id="contact-email" maxlength="150" autocomplete="email"/>
    </div>
    <div class="input-field">
      <label for="contact-subject">Betreff</label>
      <input v-model="subject" type="text" id="contact-subject" maxlength="150"/>
    </div>
    <div class="input-field">
      <label for="contact-message">Nachricht</label>
      <textarea v-model="message" id="contact-message" class="materialize-textarea" maxlength="5000"></textarea>
    </div>

    <p>
      <label>
        <input v-model="privacy" type="checkbox" class="filled-in"/>
        <span>Ich stimme zu, dass meine Angaben zur Bearbeitung meiner Anfrage verwendet werden.</span>
      </label>
    </p>

    <div style="text-align: right">
      <a class="waves-effect waves-light btn green darken-3" :class="name && email && subject && message && privacy && !sending ? [] : ['disabled']"
         @click="send">
        <i class="material-icons left">send</i>
        Senden
      </a>
    </div>
  </CenterLayout>
</template>

<script>
  import Axios from "axios"
  import M from "materialize-css"
  import CenterLayout from "../components/CenterLayout";

  export default {
    name: 'Contact',
    components: { CenterLayout },
    props: ['info'],
    data: () => ({
      name: '',
      email: '',
      subject: '',
      message: '',
      privacy: false,
      website: '',
      sending: false
    }),
    methods: {
      async send() {
        this.sending = true;
        try {
          await Axios.post('/api/contact/message', {
            name: this.name,
            email: this.email,
            subject: this.subject,
            message: this.message,
            privacy: this.privacy,
            website: this.website
          });
          M.toast({html: 'Danke für deine Nachricht!'});
          this.name = this.email = this.subject = this.message = '';
          this.privacy = false;
          this.$nextTick(() => {
            M.updateTextFields();
            M.textareaAutoResize(document.getElementById('contact-message'));
          });
        } catch (e) {
          const error = e.response && e.response.data && e.response.data.error;
          M.toast({html: error || 'Die Nachricht konnte nicht gesendet werden.'});
        } finally {
          this.sending = false;
        }
      }
    }
  }
</script>
