<template>
  <div>
    <div class="card" style="padding: 5px 20px 40px 20px">
      <h5 class="center-align" style="padding: 10px; margin-bottom: 30px">Login</h5>
      <div class="input-field">
        <i class="material-icons prefix">person</i>
        <label for="login-email">E-Mail</label>
        <input @keyup.enter="login" v-model="email" :disabled="loggingIn" type="text" id="login-email"/>
      </div>
      <div class="input-field">
        <i class="material-icons prefix">vpn_key</i>
        <label for="login-password">Passwort</label>
        <input @keyup.enter="login" v-model="password" :disabled="loggingIn" type="password" id="login-password"/>
      </div>
      <div style="text-align: right">
        <button @click="login" type="button" value="Login" class="btn waves-effect waves-light green darken-3" :class="{ disabled: loggingIn }">
          Login
          <i class="material-icons right">send</i>
        </button>
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
      loginFailed: false,
      loggingIn: false
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
  .info {
    margin-top: 20px;
    font-style: italic;
  }
</style>