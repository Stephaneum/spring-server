<template>
  <div>
    <div class="card" style="padding: 5px 20px 40px 20px">
      <h5 class="center-align" style="padding: 10px">Login</h5>
      <div class="card-content">
        <p>Man kann sich entweder über den Office365-Account oder über den manuell vergebenen Account an der Stephaneum-Cloud anmelden.</p>
      </div>

      <div style="text-align: center">
        <a href="/oauth2/authorization/azure" class="btn waves-effect waves-light green darken-3" :class="{ disabled: loggingIn }">
          Login mit Office 365
        </a>
        <div style="margin-top: 20px">
          oder
        </div>
      </div>

      <div>
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
    <div style="text-align: center">
      <p class="login-info" v-show="!loggingIn && !loginFailed" style="visibility: hidden">placeholder</p>
      <p class="login-info" v-show="loggingIn" style="display: none">Authentifizierung</p>
      <p class="login-info red-text" v-show="!loggingIn && loginFailed" style="display: none">Login fehlgeschlagen</p>
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
  .login-info {
    margin-top: 20px;
    font-style: italic;
  }
</style>