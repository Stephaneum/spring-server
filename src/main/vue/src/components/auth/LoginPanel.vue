<template>
  <div>
    <div class="card" style="padding: 5px 20px 40px 20px">
      <h5 class="center-align" style="padding: 10px; margin-bottom: 30px">Login</h5>
      <div class="card-content">
        <p>Du kannst dich entweder über deinen Office 365 Schulaccount anmelden (Standard) oder einen manuellen Account erhalten.</p>
      </div>
      <div class="card-tabs">
        <ul class="tabs tabs-fixed-width green-text text-darken-3">
          <li class="tab"><a class="active" href="#o365">Office 365</a></li>
          <li class="tab"><a href="#man">Manuell</a></li>
        </ul>
      </div>
      <div class="card-content">
        <div id="o365">
          <div style="display: flex; align-items: center; justify-content: space-between; width: 100%">
            <a href="/oauth2/authorization/azure" value="Login mit Office 365" class="btn waves-effect waves-light red darken-3" :class="{ disabled: loggingIn }">
              Login mit Office 365
              <i class="material-icons right">send</i>
            </a>
          </div>
        </div>

        <div id="man">
          <div class="input-field">
            <i class="material-icons prefix">person</i>
            <label for="login-email">E-Mail</label>
            <input @keyup.enter="login" v-model="email" :disabled="loggingIn" type="text" id="login-email"/>
          </div>
          <div class="input-field">
            <i class="material-icons prefix">vpn_key</i>
            <label for="login-password">Passwort</label>
            <input @keyup.enter="login" v-model="password" :disabled="loggingIn" :type="showPassword ? 'text' : 'password'" id="login-password"/>
          </div>
          <div style="display: flex; align-items: center; justify-content: space-between">
            <a v-if="password" @click="showPassword = !showPassword" class="waves-effect btn-flat">
              Passwort {{ showPassword ? 'verstecken' : 'anzeigen' }}
            </a>
            <span v-else></span>
            <button @click="login" type="button" value="Login" class="btn waves-effect waves-light green darken-3" :class="{ disabled: loggingIn }">
              Login
              <i class="material-icons right">send</i>
            </button>
          </div>
        </div>
      </div>


    </div>
    <div style="text-align: center">
      <p class="info" v-show="!loggingIn && !loginFailed" style="visibility: hidden">placeholder</p>
      <p class="info" v-show="loggingIn" style="display: none">Authentifizierung</p>
      <p class="info red-text" v-show="!loggingIn && loginFailed" style="display: none">Login fehlgeschlagen</p>
    </div>
  </div>
</template>

<script>
  import Axios from "axios"
  import M from "materialize-css"

  export default {
    name: 'LoginPanel',
    data: () => ({
      email: '',
      password: '',
      showPassword: false,
      loginFailed: false,
      loggingIn: false,
      o365Link: ''
    }),
    mounted: function() {
      M.AutoInit();
    },
    methods: {
      login: async function() {
        if(!this.email) {
          document.getElementById("login-email").focus();
          return;
        } else if(!this.password) {
          document.getElementById("login-password").focus();
          return;
        }

        this.loggingIn = true;

        try {
          await Axios.post('/api/login', { email: this.email, password: this.password });
          this.loginFailed = false;
          await this.$emit('update-info');

          const nextPage = this.$route.query.next;
          if (nextPage) {
            await this.$router.push(nextPage);
          } else {
            await this.$router.push('/home');
          }

          M.toast({html: 'Willkommen'});
        } catch (e) {
          this.loginFailed = true;
          this.loggingIn = false;
          M.toast({html: 'Login fehlgeschlagen.'});
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