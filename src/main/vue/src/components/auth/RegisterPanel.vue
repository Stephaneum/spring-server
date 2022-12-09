<template>
  <div>
    <div class="card" style="padding: 5px 20px 40px 20px">
      <h5 class="center-align" style="padding: 10px; margin-bottom: 30px">Registrierung</h5>

      <template v-if="!codeResponse">
        <div class="input-field">
          <i class="material-icons prefix">vpn_key</i>
          <label for="register-code">Zugangscode</label>
          <input @keyup.enter="action" v-model="code" :disabled="fetching" type="text" id="register-code"/>
        </div>
      </template>

      <template v-else>
        <div class="input-field">
          <i class="material-icons prefix">people</i>
          <label for="register-role">Rolle</label>
          <input :value="codeResponse.roleString" type="text" id="register-role" disabled/>
        </div>

        <div class="row" style="margin-bottom: 0">
          <div class="input-field col s12 m6">
            <i class="material-icons prefix">person</i>
            <label for="register-first-name">Vorname</label>
            <input @keyup.enter="action" v-model="firstName" :disabled="fetching" type="text" id="register-first-name"/>
          </div>
          <div class="input-field col s12 m6">
            <label for="register-last-name">Nachname</label>
            <input @keyup.enter="action" v-model="lastName" :disabled="fetching" type="text" id="register-last-name"/>
          </div>
        </div>

        <div class="input-field">
          <i class="material-icons prefix">email</i>
          <label for="register-email">E-Mail</label>
          <input @keyup.enter="action" v-model="email" :disabled="fetching" type="text" id="register-email"/>
        </div>
        <div class="input-field">
          <i class="material-icons prefix">vpn_key</i>
          <label for="register-password">Passwort</label>
          <input @keyup.enter="action" v-model="password" :disabled="fetching" type="password" id="register-password"/>
        </div>
      </template>

      <div style="text-align: right">
        <button @click="action" type="button" value="Login" class="btn waves-effect waves-light green darken-3" :class="{ disabled: fetching }">
          {{ codeResponse ? 'Registrieren' : 'Code prüfen' }}
          <i class="material-icons right">send</i>
        </button>
      </div>
    </div>
    <div style="text-align: center">
      <p class="info" v-show="!fetching && !errorMessage" style="visibility: hidden">placeholder</p>
      <p class="info" v-show="fetching" style="display: none">Verarbeitung</p>
      <p class="info red-text" v-show="!fetching && errorMessage" style="display: none">{{ errorMessage }}</p>
    </div>
  </div>
</template>

<script>
  import Axios from "axios"
  import M from "materialize-css"

  export default {
    name: 'RegisterPanel',
    data: () => ({
      code: '',
      codeResponse: null,
      email: '',
      password: '',
      firstName: '',
      lastName: '',
      sex: '2',
      schoolClass: '',
      errorMessage: false,
      fetching: false
    }),
    methods: {
      async action() {
          this.fetching = true;
          if (this.codeResponse)
              await this.register();
          else
              await this.checkCode();
          this.fetching = false;
      },
      async checkCode() {

        if(!this.code) {
            this.errorMessage = 'Bitte Code eingeben';
            M.toast({html: this.errorMessage});
            return;
        }

        try {
            const response = await Axios.post('/api/check-code?code='+this.code);
            this.codeResponse = response.data;
            this.errorMessage = null;

            this.$nextTick(() => {
                this.$nextTick(() => {
                    M.updateTextFields();
                    M.FormSelect.init(document.querySelectorAll('select'), {});
                });
            });
        } catch (e) {
            this.errorMessage = 'Falscher Code';
            M.toast({html: this.errorMessage});
        }
      },
      async register() {
        try {
          await Axios.post('/api/register', {
              code: this.code,
              email: this.email,
              password: this.password,
              firstName: this.firstName,
              lastName: this.lastName,
          });
          await this.$emit('update-info');
          await this.$router.push('/home');
          M.toast({html: 'Willkommen'});
        } catch (e) {
          switch(e.response.status) {
              case 400:
                  this.errorMessage = 'Bitte alle Felder ausfüllen';
                  break;
              case 423:
                  this.errorMessage = 'Falsche E-Mail';
                  break;
              case 409:
                  this.errorMessage = 'E-Mail wird bereits verwendet';
                  break;
          }
          M.toast({html: this.errorMessage});
        }
      }
    }
  }
</script>

<style scoped>
  .info {
    margin-top: 20px;
    font-style: italic;
  }
</style>