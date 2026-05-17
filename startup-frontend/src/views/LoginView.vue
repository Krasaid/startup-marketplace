<template>
  <div class="auth-page">
    <Card class="auth-card">
      <template #title>Bienvenido de vuelta</template>
      <template #content>
        <div class="form">
          <div class="field">
            <label>Email</label>
            <InputText v-model="email" type="email"
                       placeholder="tu@email.com" class="w-full" />
          </div>
          <div class="field">
            <label>Contraseña</label>
            <Password v-model="password" :feedback="false"
                      placeholder="••••••" class="w-full" />
          </div>

          <Message v-if="error" severity="error" :closable="false">
            {{ error }}
          </Message>

          <Button label="Entrar" icon="pi pi-sign-in"
                  :loading="loading" class="w-full"
                  @click="handleLogin" />

          <p class="switch-link">
            ¿No tienes cuenta?
            <RouterLink to="/registro">Regístrate</RouterLink>
          </p>
        </div>
      </template>
    </Card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import Card      from 'primevue/card'
import Button    from 'primevue/button'
import InputText from 'primevue/inputtext'
import Password  from 'primevue/password'
import Message   from 'primevue/message'

const router = useRouter()
const auth   = useAuthStore()

const email    = ref('')
const password = ref('')
const loading  = ref(false)
const error    = ref('')

async function handleLogin() {
  error.value   = ''
  loading.value = true
  try {
    await auth.login(email.value, password.value)
    router.push('/')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}
.auth-card { width: 100%; max-width: 420px; }
.form      { display: flex; flex-direction: column; gap: 1rem; }
.field     { display: flex; flex-direction: column; gap: 0.4rem; }
.field label { font-weight: 500; font-size: 0.9rem; }
.w-full    { width: 100%; }
.switch-link { text-align: center; font-size: 0.9rem; color: #888; }
.switch-link a { color: #788A70; font-weight: 600; }
</style>