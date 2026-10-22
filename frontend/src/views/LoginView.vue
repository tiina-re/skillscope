<script>
import AlertDanger from '@/components/AlertDanger.vue'
import AppNavbar from '@/navigation/AppNavbar.vue'
import LoginService from '@/services/LoginService.js'
import NavigationService from '@/services/NavigationService.js'

export default {
  name: 'LoginView',
  components: { AlertDanger, AppNavbar },

  data() {
    return {
      errorMessage: '',
      showSpinner: false,

      loginRequest: {
        email: '',
        password: '',
      },

      loginResponse: {
        userId: 0,
        roleName: '',
      },

      errorResponse: {
        message: '',
        errorCode: '',
      },
    }
  },
  methods: {
    login() {
      this.showSpinner = true
      if (this.loginRequest.email === '' || this.loginRequest.password === '') {
        this.errorMessage = 'Täida kõik väljad!'
        this.showSpinner = false
      } else {
        LoginService.postLoginRequest(this.loginRequest)
          .then((response) => this.handleLoginResponse(response))
          .catch((error) => this.handleLoginErrorResponse(error))
          .finally(() => this.showSpinner = false)
      }
    },
    handleLoginResponse(response) {
      this.loginResponse = response.data
      sessionStorage.setItem('userId', this.loginResponse.userId)
      sessionStorage.setItem('roleName', this.loginResponse.roleName)

      NavigationService.navigateToStartPage()
    },
    handleLoginErrorResponse(error) {
      console.log(error)

      this.errorResponse = error.response.data
      this.errorMessage = this.errorResponse.message
    },
  },
}
</script>

<template>
  <!-- Navbar on siin .layout-i sees, seega taust peab katma terve ekraani (mitte 100vh - navbar) -->
  <div class="layout min-vh-100">
    <AppNavbar :show-account-icon="false" />
    <div class="container text-start col-5 mt-5">
      <AlertDanger :error-message="errorMessage" />
    </div>
    <div class="container text-start col-3 mt-5">
      <div class="row">
        <h1>Logi sisse</h1>
        <p>Sisesta oma konto email ja salasõna.</p>
        <form @submit.prevent="login">
          <div class="mb-3">
            <input
              v-model="loginRequest.email"
              type="email"
              class="form-control"
              aria-describedby="emailHelp"
              placeholder="Email"
            />
            <div id="emailHelp" class="form-text">Su emaili ei jagata kolmandate osapooltega.</div>
          </div>
          <div class="mb-3">
            <input
              v-model="loginRequest.password"
              type="password"
              class="form-control"
              placeholder="Salasõna"
            />
          </div>
          <button v-if="showSpinner" class="btn btn-primary" type="button" disabled>
            <span class="spinner-border spinner-border-sm" aria-hidden="true"></span>
            <span role="status">Login sisse</span>
          </button>
          <button v-else @click="login" type="submit" class="btn btn-primary">Logi sisse</button>
        </form>
      </div>
    </div>
  </div>
</template>
