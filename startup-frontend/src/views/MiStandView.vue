<template>
  <div class="mi-stand-page">

    <!-- ── ENCABEZADO ─────────────────────────────────────────── -->
    <div class="page-header">
      <h1>Mi Stand</h1>
      <p>Administra tu presencia en StartUP!</p>
    </div>

    <div v-if="loadingInicial" class="loading">
      <Skeleton height="300px" border-radius="12px" />
    </div>

    <div v-else class="layout">

      <!-- ── COLUMNA IZQUIERDA: Datos del stand ─────────────── -->
      <div class="col-left">
        <Card>
          <template #title>
            {{ stand.id ? 'Editar stand' : 'Crear mi stand' }}
          </template>
          <template #content>
            <div class="form">

              <div class="field">
                <label>Nombre del stand *</label>
                <InputText v-model="stand.nombre" placeholder="Ej: Postres Abuela Rosa"
                           class="w-full" />
              </div>

              <div class="field">
                <label>Slug (URL única) *</label>
                <InputText v-model="stand.slug"
                           :disabled="!!stand.id"
                           placeholder="postres-abuela-rosa"
                           class="w-full" />
                <small v-if="!stand.id" class="hint">
                  Solo letras minúsculas y guiones. No se puede cambiar después.
                </small>
              </div>

              <div class="field">
                <label>Historia / Descripción *</label>
                <Textarea v-model="stand.descripcion"
                          placeholder="Cuenta tu historia como emprendedor..."
                          rows="4" class="w-full" />
              </div>

              <div class="field-row">
                <div class="field">
                  <label>Categoría</label>
                  <Select v-model="stand.categoria"
                          :options="categorias"
                          placeholder="Selecciona..."
                          class="w-full" />
                </div>
                <div class="field">
                  <label>Ciudad</label>
                  <InputText v-model="stand.ciudad"
                             placeholder="Santa Ana"
                             class="w-full" />
                </div>
              </div>

              <div class="field">
                <label>WhatsApp URL</label>
                <InputText v-model="stand.whatsappUrl"
                           placeholder="https://wa.me/50312345678"
                           class="w-full" />
              </div>

              <div class="field">
                <label>Instagram URL</label>
                <InputText v-model="stand.instagramUrl"
                           placeholder="https://instagram.com/tu_stand"
                           class="w-full" />
              </div>

              <div class="field">
                <label>Banner del stand</label>
                <ImageUploader v-model="stand.bannerUrl"
                               label="Subir banner"
                               tipo="banner" />
              </div>

              <div class="field" style="align-items: flex-start">
                <label>Logo del stand</label>
                <ImageUploader v-model="stand.logoUrl"
                               label="Subir logo"
                               tipo="logo" />
              </div>

              <Message v-if="errorStand" severity="error" :closable="false">
                {{ errorStand }}
              </Message>

              <Button :label="stand.id ? 'Guardar cambios' : 'Crear stand'"
                      :icon="stand.id ? 'pi pi-save' : 'pi pi-plus'"
                      :loading="guardandoStand"
                      class="w-full"
                      @click="guardarStand" />
            </div>
          </template>
        </Card>

        <!-- Métricas solo si el stand ya existe -->
        <Card v-if="stand.id" class="metricas-card">
          <template #title>Métricas esta semana</template>
          <template #content>
            <div class="metricas">
              <div class="metrica">
                <span class="metrica-n">{{ stand.scoreVisitas }}</span>
                <span class="metrica-l">Visitas</span>
              </div>
              <div class="metrica">
                <span class="metrica-n">{{ stand.scoreClics }}</span>
                <span class="metrica-l">Clics contacto</span>
              </div>
              <div class="metrica destacada">
                <span class="metrica-n">{{ stand.scoreSemanal }}</span>
                <span class="metrica-l">Score semanal</span>
              </div>
            </div>
          </template>
        </Card>
      </div>

      <!-- ── COLUMNA DERECHA: Productos ─────────────────────── -->
      <div class="col-right">
        <Card>
          <template #title>
            Productos
            <span class="productos-count">
              {{ stand.productos ? stand.productos.length : 0 }} / 20
            </span>
          </template>
          <template #content>

            <div v-if="!stand.id" class="empty-productos">
              <i class="pi pi-info-circle" />
              <p>Primero crea tu stand para agregar productos.</p>
            </div>

            <div v-else>
              <!-- Formulario nuevo producto -->
              <div class="nuevo-producto">
                <h4>Agregar producto</h4>
                <div class="form">
                  <div class="field">
                    <label>Nombre *</label>
                    <InputText v-model="nuevoProducto.nombre"
                               placeholder="Ej: Tres Leches"
                               class="w-full" />
                  </div>
                  <div class="field">
                    <label>Descripción</label>
                    <InputText v-model="nuevoProducto.descripcion"
                               placeholder="Descripción breve"
                               class="w-full" />
                  </div>
                  <div class="field-row">
                    <div class="field">
                      <label>Precio (USD) *</label>
                      <InputNumber v-model="nuevoProducto.precio"
                                   mode="currency" currency="USD"
                                   locale="en-US" class="w-full" />
                    </div>
                    <div class="field">
                      <label>Imagen URL</label>
                      <InputText v-model="nuevoProducto.imagenUrl"
                                 placeholder="https://..."
                                 class="w-full" />
                    </div>
                  </div>

                  <Message v-if="errorProducto" severity="error" :closable="false">
                    {{ errorProducto }}
                  </Message>

                  <Button label="Agregar producto"
                          icon="pi pi-plus"
                          severity="success"
                          :loading="agregandoProducto"
                          :disabled="stand.productos && stand.productos.length >= 20"
                          class="w-full"
                          @click="agregarProducto" />
                </div>
              </div>

              <!-- Lista de productos existentes -->
              <div v-if="stand.productos && stand.productos.length"
                   class="lista-productos">
                <h4>Productos actuales</h4>

                <div v-for="(p, i) in stand.productos" :key="i" class="producto-item">

                  <!-- MODO LECTURA -->
                  <template v-if="editandoIndex !== i">
                    <img v-if="p.imagenUrl" :src="p.imagenUrl" :alt="p.nombre" />
                    <div class="producto-info">
                      <span class="producto-nombre">{{ p.nombre }}</span>
                      <span class="producto-precio">${{ p.precio }}</span>
                      <Tag :value="p.disponible !== false ? 'Disponible' : 'Agotado'"
                           :severity="p.disponible !== false ? 'success' : 'danger'" />
                    </div>
                    <div class="producto-acciones">
                      <Button icon="pi pi-pencil"
                              severity="secondary" text rounded
                              title="Editar"
                              @click="iniciarEdicion(i)" />
                      <Button icon="pi pi-trash"
                              severity="danger" text rounded
                              title="Eliminar"
                              :loading="eliminandoIndex === i"
                              @click="eliminarProducto(i)" />
                    </div>
                  </template>

                  <!-- MODO EDICIÓN INLINE -->
                  <template v-else>
                    <div class="edicion-inline">
                      <div class="edicion-fila">
                        <div class="field">
                          <label>Nombre</label>
                          <InputText v-model="productoEditado.nombre" class="w-full" />
                        </div>
                        <div class="field">
                          <label>Precio (USD)</label>
                          <InputNumber v-model="productoEditado.precio"
                                       mode="currency" currency="USD"
                                       locale="en-US" class="w-full" />
                        </div>
                      </div>

                      <div class="field">
                        <label>Descripción</label>
                        <InputText v-model="productoEditado.descripcion" class="w-full" />
                      </div>

                      <div class="field">
                        <label>Imagen URL</label>
                        <InputText v-model="productoEditado.imagenUrl" class="w-full" />
                      </div>

                      <div class="edicion-disponible">
                        <label class="toggle-label">
                          <input type="checkbox" v-model="productoEditado.disponible" />
                          <span>Disponible</span>
                        </label>
                      </div>

                      <Message v-if="errorEdicion" severity="error" :closable="false">
                        {{ errorEdicion }}
                      </Message>

                      <div class="edicion-btns">
                        <Button label="Cancelar" severity="secondary" text
                                @click="cancelarEdicion" />
                        <Button label="Guardar" icon="pi pi-check"
                                severity="success"
                                :loading="guardandoEdicion"
                                @click="guardarEdicion(i)" />
                      </div>
                    </div>
                  </template>

                </div>
              </div>

              <p v-else class="no-productos">
                Aún no tienes productos. ¡Agrega el primero!
              </p>
            </div>

          </template>
        </Card>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useAuthStore } from '../stores/auth'
import { standsApi } from '../service/api'   // ← ruta corregida (singular)
import ImageUploader from '../components/ImageUploader.vue'
import Card        from 'primevue/card'
import Button      from 'primevue/button'
import InputText   from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Textarea    from 'primevue/textarea'
import Select      from 'primevue/select'
import Message     from 'primevue/message'
import Skeleton    from 'primevue/skeleton'
import Tag         from 'primevue/tag'

const toast = useToast()
const auth  = useAuthStore()

// ── Estado del stand ──────────────────────────────────────────────────
const stand = ref({
  id: null, nombre: '', slug: '', descripcion: '',
  categoria: '', ciudad: 'Santa Ana',
  whatsappUrl: '', instagramUrl: '',
  bannerUrl: '', logoUrl: '',
  productos: [],
  scoreVisitas: 0, scoreClics: 0, scoreSemanal: 0
})

const categorias = [
  'Gastronomía', 'Moda', 'Moda Alternativa', 'Artesanías',
  'Belleza', 'Tecnología', 'Mascotas', 'Hogar', 'Otro'
]

// ── Estado de producto nuevo ──────────────────────────────────────────
const nuevoProducto = ref({
  nombre: '', descripcion: '', precio: null, imagenUrl: ''
})

// ── Flags de UI ───────────────────────────────────────────────────────
const loadingInicial    = ref(true)
const guardandoStand    = ref(false)
const agregandoProducto = ref(false)
const eliminandoIndex   = ref(null)
const editandoIndex    = ref(null)
const productoEditado  = ref({})
const guardandoEdicion = ref(false)
const errorEdicion     = ref('')
const errorStand        = ref('')
const errorProducto     = ref('')

// ── Slug auto-generado desde el nombre (solo en modo creación) ────────
watch(() => stand.value.nombre, (nombre) => {
  if (!stand.value.id) {
    stand.value.slug = nombre
      .toLowerCase()
      .normalize('NFD')
      .replace(/[̀-ͯ]/g, '')
      .replace(/[^a-z0-9\s-]/g, '')
      .trim()
      .replace(/\s+/g, '-')
  }
})

// ── Al montar: cargar el stand propio vía endpoint autenticado ────────
onMounted(async () => {
  try {
    const resultado = await standsApi.getMio()
    // getMio() retorna null cuando el backend responde 204 (sin stand aún)
    if (resultado) {
      stand.value = resultado
    }
  } catch (e) {
    // Error de red u otro — quedamos en modo creación
  } finally {
    loadingInicial.value = false
  }
})

// ── Guardar stand (crear o editar) ────────────────────────────────────
async function guardarStand() {
  errorStand.value = ''

  // Validar que el slug sea limpio antes de enviar al backend
  const slugRegex = /^[a-z0-9]+(?:-[a-z0-9]+)*$/
  if (!slugRegex.test(stand.value.slug)) {
    errorStand.value = 'El slug solo puede tener letras minúsculas, números y guiones.'
    return
  }

  guardandoStand.value = true
  try {

    const payload = {
      nombre:       stand.value.nombre,
      slug:         stand.value.slug,
      descripcion:  stand.value.descripcion,
      categoria:    stand.value.categoria,
      ciudad:       stand.value.ciudad,
      whatsappUrl:  stand.value.whatsappUrl,
      instagramUrl: stand.value.instagramUrl,
      bannerUrl:    stand.value.bannerUrl,
      logoUrl:      stand.value.logoUrl
    }

    let resultado
    if (stand.value.id) {
      resultado = await standsApi.update(stand.value.id, payload)
      toast.add({ severity: 'success', summary: '¡Guardado!',
                  detail: 'Stand actualizado correctamente.', life: 3000 })
    } else {
      resultado = await standsApi.create(payload)
      toast.add({ severity: 'success', summary: '¡Stand creado!',
                  detail: 'Ya apareces en el feed.', life: 3000 })
    }
    stand.value = resultado

  } catch (e) {
    errorStand.value = e.message
  } finally {
    guardandoStand.value = false
  }
}

// ── Agregar producto ──────────────────────────────────────────────────
async function agregarProducto() {
  errorProducto.value = ''
  if (!nuevoProducto.value.nombre) {
    errorProducto.value = 'El nombre del producto es obligatorio.'
    return
  }
  agregandoProducto.value = true
  try {
    const resultado = await standsApi.addProducto(
      stand.value.id, nuevoProducto.value)
    stand.value = resultado
    nuevoProducto.value = { nombre: '', descripcion: '', precio: null, imagenUrl: '' }
    toast.add({ severity: 'success', summary: 'Producto agregado', life: 2000 })
  } catch (e) {
    errorProducto.value = e.message
  } finally {
    agregandoProducto.value = false
  }
}

// ── Eliminar producto ─────────────────────────────────────────────────
async function eliminarProducto(index) {
  eliminandoIndex.value = index
  try {
    const resultado = await standsApi.removeProducto(stand.value.id, index)
    stand.value = resultado
    toast.add({ severity: 'info', summary: 'Producto eliminado', life: 2000 })
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Error', detail: e.message, life: 3000 })
  } finally {
    eliminandoIndex.value = null
  }
}

function iniciarEdicion(index) {
  editandoIndex.value   = index
  errorEdicion.value    = ''
  // Clonar para no mutar el original hasta confirmar
  productoEditado.value = { ...stand.value.productos[index] }
}

function cancelarEdicion() {
  editandoIndex.value   = null
  productoEditado.value = {}
  errorEdicion.value    = ''
}

async function guardarEdicion(index) {
  if (!productoEditado.value.nombre?.trim()) {
    errorEdicion.value = 'El nombre es obligatorio.'
    return
  }

  guardandoEdicion.value = true
  errorEdicion.value     = ''

  try {
    // PATCH atómico — un solo viaje a MongoDB
    const resultado = await standsApi.updateProducto(
      stand.value.id, index, productoEditado.value)

    stand.value           = resultado
    editandoIndex.value   = null
    productoEditado.value = {}

    toast.add({ severity: 'success', summary: 'Producto actualizado', life: 2000 })
  } catch (e) {
    errorEdicion.value = e.message
  } finally {
    guardandoEdicion.value = false
  }
}
</script>

<style scoped>
.mi-stand-page { max-width: 1100px; margin: 0 auto; }

.page-header        { margin-bottom: 1.5rem; }
.page-header h1     { font-size: 1.75rem; font-weight: 700; color: var(--color-text); }
.page-header p      { color: var(--color-muted); margin-top: 0.25rem; }

.layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  align-items: start;
}

@media (max-width: 768px) {
  .layout { grid-template-columns: 1fr; }
}

.form         { display: flex; flex-direction: column; gap: 1rem; }
.field        { display: flex; flex-direction: column; gap: 0.35rem; }
.field label  { font-weight: 500; font-size: 0.875rem; color: var(--color-text); }
.field-row    { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
.hint         { color: var(--color-muted); font-size: 0.78rem; }
.w-full       { width: 100%; }

.metricas-card  { margin-top: 1.5rem; }
.metricas       { display: grid; grid-template-columns: repeat(3,1fr); gap: 1rem; }
.metrica        { text-align: center; }
.metrica-n      { display: block; font-size: 2rem; font-weight: 700; color: var(--color-text); }
.metrica-l      { font-size: 0.8rem; color: var(--color-muted); }
.metrica.destacada .metrica-n { color: var(--color-accent); }

.productos-count {
  font-size: 0.8rem;
  font-weight: 400;
  color: var(--color-muted);
  margin-left: 0.5rem;
}

.nuevo-producto   { margin-bottom: 1.5rem; }
.nuevo-producto h4,
.lista-productos h4 { font-size: 0.95rem; font-weight: 600;
                       margin-bottom: 0.75rem; color: var(--color-text); }

.lista-productos  { border-top: 1px solid var(--color-border); padding-top: 1rem; }

.producto-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.6rem 0;
  border-bottom: 1px solid var(--color-border);
}
.producto-item img {
  width: 48px; height: 48px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
}
.producto-info  { flex: 1; display: flex; flex-direction: column; gap: 3px; }
.producto-nombre { font-weight: 600; font-size: 0.9rem; }
.producto-precio { font-size: 0.85rem; color: var(--color-accent); font-weight: 600; }

.empty-productos, .no-productos {
  text-align: center;
  color: var(--color-muted);
  padding: 2rem 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.loading { display: flex; flex-direction: column; gap: 1rem; }

.producto-acciones {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex-shrink: 0;
}

.edicion-inline {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding: 0.75rem;
  background: var(--color-card);
  border-radius: 10px;
  border: 1.5px solid var(--color-border);
}

.edicion-fila {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}

.edicion-disponible { display: flex; align-items: center; }

.toggle-label {
  display: flex; align-items: center;
  gap: 0.5rem; cursor: pointer;
  font-size: 0.875rem; font-weight: 500;
}
.toggle-label input { width: 16px; height: 16px; cursor: pointer; }

.edicion-btns {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}
</style>