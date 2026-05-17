<template>
  <div class="auth-page">
    <Card class="auth-card">
      <template #title>Crear cuenta</template>
      <template #content>
        <div class="form">
          <div class="field">
            <label>Email</label>
            <InputText v-model="email" type="email"
                       placeholder="tu@email.com" class="w-full" />
          </div>
          <div class="field">
            <label>Contraseña</label>
            <Password v-model="password" placeholder="Mínimo 6 caracteres"
                      class="w-full" />
          </div>
          <div class="field">
            <label>¿Eres emprendedor?</label>
            <div class="rol-toggle">
              <Button :label="rol === 'COMPRADOR' ? '🛍 Solo explorar' : '🏪 Tengo un stand'"
                      :severity="rol === 'EMPRENDEDOR' ? 'success' : 'secondary'"
                      @click="rol = rol === 'COMPRADOR' ? 'EMPRENDEDOR' : 'COMPRADOR'" />
            </div>
          </div>

          <Message v-if="error" severity="error" :closable="false">
            {{ error }}
          </Message>

          <Button label="Crear cuenta" icon="pi pi-user-plus"
                  :loading="loading" class="w-full"
                  @click="handleRegistro" />

          <p class="switch-link">
            ¿Ya tienes cuenta?
            <RouterLink to="/login">Inicia sesión</RouterLink>
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
const rol      = ref('COMPRADOR')
const loading  = ref(false)
const error    = ref('')

async function handleRegistro() {
  error.value   = ''
  loading.value = true
  try {
    await auth.register(email.value, password.value, rol.value)
    router.push('/')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page  { display: flex; justify-content: center; align-items: center; min-height: 60vh; }
.auth-card  { width: 100%; max-width: 420px; }
.form       { display: flex; flex-direction: column; gap: 1rem; }
.field      { display: flex; flex-direction: column; gap: 0.4rem; }
.field label { font-weight: 500; font-size: 0.9rem; }
.w-full     { width: 100%; }
.rol-toggle { display: flex; }
.switch-link { text-align: center; font-size: 0.9rem; color: var(--color-muted); }
.switch-link a { color: var(--color-primary); font-weight: 600; }
</style>