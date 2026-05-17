<template>
  <div class="perfil-page">

    <!-- ENCABEZADO -->
    <div class="page-header">
      <h1>Mi Perfil</h1>
      <p>Administra tu información personal</p>
    </div>

    <div class="perfil-layout">

      <!-- COLUMNA IZQUIERDA: Datos personales -->
      <div class="col-left">
        <Card>
          <template #title>Información personal</template>
          <template #content>
            <div v-if="loadingPerfil" class="loading">
              <Skeleton height="200px" />
            </div>
            <div v-else class="form">

              <!-- Foto de perfil -->
              <div class="foto-section">
                <div class="avatar-grande">
                  <img v-if="form.fotoPerfil" :src="form.fotoPerfil" alt="foto" />
                  <span v-else class="avatar-inicial">
                    {{ (form.nombre || auth.user.email).charAt(0).toUpperCase() }}
                  </span>
                </div>
                <ImageUploader v-model="form.fotoPerfil"
                               label="Cambiar foto"
                               tipo="logo" />
              </div>

              <div class="field-row">
                <div class="field">
                  <label>Nombre *</label>
                  <InputText v-model="form.nombre"
                             placeholder="Tu nombre"
                             class="w-full" />
                </div>
                <div class="field">
                  <label>Apellido *</label>
                  <InputText v-model="form.apellido"
                             placeholder="Tu apellido"
                             class="w-full" />
                </div>
              </div>

              <div class="field">
                <label>Email</label>
                <InputText :value="auth.user.email"
                           disabled class="w-full" />
                <small class="hint">El email no se puede cambiar</small>
              </div>

              <div class="field">
                <label>Rol</label>
                <div class="rol-badge">
                  <i :class="auth.isEmprendedor
                      ? 'pi pi-shop' : 'pi pi-user'" />
                  {{ auth.user.rol }}
                </div>
              </div>

              <div v-if="perfil?.tieneStand" class="stand-link-card">
                <i class="pi pi-shop" />
                <div>
                  <strong>Tienes un stand activo</strong>
                  <p>Administra tus productos y métricas</p>
                </div>
                <RouterLink to="/mi-stand">
                  <Button label="Ir a Mi Stand"
                          severity="secondary" size="small" />
                </RouterLink>
              </div>

              <Message v-if="errorPerfil" severity="error" :closable="false">
                {{ errorPerfil }}
              </Message>

              <Button label="Guardar cambios"
                      icon="pi pi-save"
                      :loading="guardando"
                      class="w-full"
                      @click="guardarPerfil" />
            </div>
          </template>
        </Card>
      </div>

      <!-- COLUMNA DERECHA: Seguridad + Eliminar cuenta -->
      <div class="col-right">

        <!-- Cambiar contraseña -->
        <Card class="mb-card">
          <template #title>Cambiar contraseña</template>
          <template #content>
            <div class="form">
              <div class="field">
                <label>Contraseña actual</label>
                <Password v-model="passForm.actual"
                          :feedback="false"
                          placeholder="••••••"
                          class="w-full" />
              </div>
              <div class="field">
                <label>Nueva contraseña</label>
                <Password v-model="passForm.nueva"
                          placeholder="Mínimo 6 caracteres"
                          class="w-full" />
              </div>
              <div class="field">
                <label>Confirmar nueva contraseña</label>
                <Password v-model="passForm.confirmar"
                          :feedback="false"
                          placeholder="Repite la nueva contraseña"
                          class="w-full" />
              </div>

              <Message v-if="errorPass" severity="error" :closable="false">
                {{ errorPass }}
              </Message>
              <Message v-if="successPass" severity="success" :closable="false">
                {{ successPass }}
              </Message>

              <Button label="Cambiar contraseña"
                      icon="pi pi-lock"
                      :loading="cambiandoPass"
                      class="w-full"
                      @click="cambiarPassword" />
            </div>
          </template>
        </Card>

        <!-- Zona de peligro -->
        <Card class="danger-card">
          <template #title>
            <span style="color:#dc2626">Zona de peligro</span>
          </template>
          <template #content>
            <p class="danger-desc">
              Eliminar tu cuenta es una acción permanente. 
              Se desactivará tu stand (si tienes uno) y no 
              podrás recuperar tu información.
            </p>

            <div v-if="!confirmandoEliminar">
              <Button label="Eliminar mi cuenta"
                      icon="pi pi-trash"
                      severity="danger"
                      outlined
                      class="w-full"
                      @click="confirmandoEliminar = true" />
            </div>

            <div v-else class="confirmar-eliminar">
              <p class="confirmar-texto">
                ¿Estás seguro? Escribe tu email para confirmar:
              </p>
              <InputText v-model="emailConfirmacion"
                         :placeholder="auth.user.email"
                         class="w-full" />
              <div class="confirmar-btns">
                <Button label="Cancelar"
                        severity="secondary"
                        @click="cancelarEliminar" />
                <Button label="Sí, eliminar"
                        severity="danger"
                        :loading="eliminando"
                        :disabled="emailConfirmacion !== auth.user.email"
                        @click="eliminarCuenta" />
              </div>
            </div>
          </template>
        </Card>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { useAuthStore } from '../stores/auth'
import { profileApi } from '../service/api'
import Card        from 'primevue/card'
import Button      from 'primevue/button'
import InputText   from 'primevue/inputtext'
import Password    from 'primevue/password'
import Message     from 'primevue/message'
import Skeleton    from 'primevue/skeleton'
import ImageUploader from '../components/ImageUploader.vue'

const toast  = useToast()
const router = useRouter()
const auth   = useAuthStore()

// ── Estado del perfil ─────────────────────────────────────────────
const perfil       = ref(null)
const loadingPerfil = ref(true)
const guardando    = ref(false)
const errorPerfil  = ref('')

const form = ref({
    nombre:     '',
    apellido:   '',
    fotoPerfil: ''
})

// ── Estado contraseña ─────────────────────────────────────────────
const passForm = ref({ actual: '', nueva: '', confirmar: '' })
const cambiandoPass = ref(false)
const errorPass     = ref('')
const successPass   = ref('')

// ── Estado eliminar cuenta ────────────────────────────────────────
const confirmandoEliminar = ref(false)
const emailConfirmacion   = ref('')
const eliminando          = ref(false)

// ── Carga inicial ─────────────────────────────────────────────────
onMounted(async () => {
    try {
        perfil.value       = await profileApi.get()
        form.value.nombre  = perfil.value.nombre     || ''
        form.value.apellido = perfil.value.apellido  || ''
        form.value.fotoPerfil = perfil.value.fotoPerfil || ''
    } catch (e) {
        errorPerfil.value = e.message
    } finally {
        loadingPerfil.value = false
    }
})

// ── Guardar perfil ────────────────────────────────────────────────
async function guardarPerfil() {
    if (!form.value.nombre || !form.value.apellido) {
        errorPerfil.value = 'Nombre y apellido son obligatorios.'
        return
    }
    errorPerfil.value = ''
    guardando.value   = true
    try {
        const resultado = await profileApi.update(form.value)
        auth.updateUser({
            nombre:     resultado.nombre,
            apellido:   resultado.apellido,
            fotoPerfil: resultado.fotoPerfil
        })
        toast.add({ severity: 'success', summary: '¡Perfil actualizado!', life: 3000 })
    } catch (e) {
        errorPerfil.value = e.message
    } finally {
        guardando.value = false
    }
}

// ── Cambiar contraseña ────────────────────────────────────────────
async function cambiarPassword() {
    errorPass.value   = ''
    successPass.value = ''

    if (passForm.value.nueva !== passForm.value.confirmar) {
        errorPass.value = 'Las contraseñas nuevas no coinciden.'
        return
    }
    cambiandoPass.value = true
    try {
        await profileApi.changePassword({
            passwordActual: passForm.value.actual,
            passwordNueva:  passForm.value.nueva
        })
        successPass.value = '¡Contraseña actualizada correctamente!'
        passForm.value    = { actual: '', nueva: '', confirmar: '' }
    } catch (e) {
        errorPass.value = e.message
    } finally {
        cambiandoPass.value = false
    }
}

// ── Eliminar cuenta ───────────────────────────────────────────────
function cancelarEliminar() {
    confirmandoEliminar.value = false
    emailConfirmacion.value   = ''
}

async function eliminarCuenta() {
    eliminando.value = true
    try {
        await profileApi.delete()
        auth.logout()
        router.push('/')
        toast.add({
            severity: 'info',
            summary: 'Cuenta eliminada',
            detail: 'Tu cuenta ha sido eliminada.',
            life: 4000
        })
    } catch (e) {
        toast.add({ severity: 'error', summary: 'Error', detail: e.message, life: 3000 })
    } finally {
        eliminando.value = false
    }
}
</script>

<style scoped>
.perfil-page     { max-width: 900px; margin: 0 auto; }
.page-header     { margin-bottom: 1.5rem; }
.page-header h1  { font-size: 1.75rem; font-weight: 800; color: var(--color-text); }
.page-header p   { color: var(--color-muted); margin-top: .25rem; }

.perfil-layout {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1.5rem;
    align-items: start;
}
@media (max-width: 768px) {
    .perfil-layout { grid-template-columns: 1fr; }
}

.form       { display: flex; flex-direction: column; gap: 1rem; }
.field      { display: flex; flex-direction: column; gap: .35rem; }
.field label { font-weight: 600; font-size: .875rem; color: var(--color-text); }
.field-row  { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
.hint       { color: var(--color-muted); font-size: .78rem; }
.w-full     { width: 100%; }
.mb-card    { margin-bottom: 1.5rem; }

.foto-section {
    display: flex;
    align-items: center;
    gap: 1rem;
    padding-bottom: .75rem;
    border-bottom: 1px solid var(--color-border);
}
.avatar-grande {
    width: 72px; height: 72px; border-radius: 50%;
    background: var(--color-deep);
    border: 3px solid var(--color-card);
    overflow: hidden; flex-shrink: 0;
    display: flex; align-items: center; justify-content: center;
}
.avatar-grande img  { width: 100%; height: 100%; object-fit: cover; }
.avatar-inicial { font-size: 1.75rem; font-weight: 800; color: var(--color-text); }

.rol-badge {
    display: inline-flex; align-items: center; gap: .5rem;
    background: var(--color-deep); color: var(--color-text);
    border-radius: 99px; padding: .4rem 1rem;
    font-size: .875rem; font-weight: 600;
}

.stand-link-card {
    display: flex; align-items: center; gap: .75rem;
    background: var(--color-card); border: 1px solid var(--color-border);
    border-radius: 10px; padding: .75rem;
}
.stand-link-card i  { font-size: 1.5rem; color: var(--color-primary); flex-shrink: 0; }
.stand-link-card div { flex: 1; }
.stand-link-card strong { font-size: .875rem; color: var(--color-text); display: block; }
.stand-link-card p  { font-size: .78rem; color: var(--color-muted); margin: 0; }

.danger-card { border-color: #fee2e2 !important; }
.danger-desc {
    font-size: .875rem; color: var(--color-muted);
    line-height: 1.6; margin-bottom: 1rem;
}

.confirmar-eliminar { display: flex; flex-direction: column; gap: .75rem; }
.confirmar-texto    { font-size: .875rem; color: #dc2626; font-weight: 500; }
.confirmar-btns     { display: flex; justify-content: flex-end; gap: .75rem; }
</style>
