<template>
  <div class="uploader">
    <!-- Preview si ya hay imagen -->
    <div v-if="modelValue" class="preview"
         :style="tipo === 'banner' ? 'height:140px' : 'height:100px;width:100px'">
      <img :src="modelValue" :alt="label" />
      <button class="remove-btn" @click="quitar" title="Quitar imagen">
        <i class="pi pi-times" />
      </button>
    </div>

    <!-- Zona de drop -->
    <div v-else
         class="drop-zone"
         :class="{ dragging, 'is-banner': tipo === 'banner', 'is-logo': tipo === 'logo' }"
         @dragover.prevent="dragging = true"
         @dragleave="dragging = false"
         @drop.prevent="onDrop"
         @click="triggerInput">

      <div v-if="uploading" class="upload-state">
        <i class="pi pi-spin pi-spinner" />
        <span>Subiendo...</span>
      </div>
      <div v-else class="upload-state">
        <i :class="tipo === 'banner' ? 'pi pi-image' : 'pi pi-user'" />
        <span>{{ label }}</span>
        <small>Arrastra o haz clic · JPG, PNG, WebP · máx 5MB</small>
      </div>

      <input ref="inputRef" type="file"
             accept="image/jpeg,image/png,image/webp"
             style="display:none"
             @change="onFileSelected" />
    </div>

    <small v-if="error" class="upload-error">{{ error }}</small>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  label:      { type: String, default: 'Subir imagen' },
  tipo:       { type: String, default: 'banner' }   // 'banner' | 'logo'
})

const emit      = defineEmits(['update:modelValue'])
const inputRef  = ref(null)
const dragging  = ref(false)
const uploading = ref(false)
const error     = ref('')

function triggerInput() {
  inputRef.value?.click()
}

function onDrop(e) {
  dragging.value = false
  const file = e.dataTransfer.files[0]
  if (file) subirArchivo(file)
}

function onFileSelected(e) {
  const file = e.target.files[0]
  if (file) subirArchivo(file)
}

async function subirArchivo(file) {
  error.value     = ''
  uploading.value = true

  const formData = new FormData()
  formData.append('file', file)

  const token = localStorage.getItem('token')

  try {
    const res = await fetch('http://localhost:8080/api/upload', {
      method: 'POST',
      headers: token ? { 'Authorization': `Bearer ${token}` } : {},
      body: formData
      // NO poner Content-Type — el browser lo setea solo con el boundary
    })

    if (!res.ok) {
      const data = await res.json().catch(() => ({}))
      throw new Error(data.error || 'Error al subir la imagen')
    }

    const data = await res.json()
    emit('update:modelValue', data.url)

  } catch (e) {
    error.value = e.message
  } finally {
    uploading.value = false
    // Limpiar input para permitir subir el mismo archivo de nuevo
    if (inputRef.value) inputRef.value.value = ''
  }
}

function quitar() {
  emit('update:modelValue', '')
}
</script>

<style scoped>
.uploader { width: 100%; }

.drop-zone {
  border: 2px dashed #BDDDBB;
  border-radius: 10px;
  background: #f4f8f3;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
}
.drop-zone:hover, .drop-zone.dragging {
  border-color: #788A70;
  background: #e8f2e6;
}
.drop-zone.is-banner { height: 140px; width: 100%; }
.drop-zone.is-logo   { height: 100px; width: 100px; border-radius: 50%; }

.upload-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  color: #A9CFA1;
  pointer-events: none;
}
.upload-state i    { font-size: 1.75rem; }
.upload-state span { font-size: 0.875rem; font-weight: 500; color: #788A70; }
.upload-state small { font-size: 0.75rem; color: #bbb; }

.preview {
  position: relative;
  border-radius: 10px;
  overflow: hidden;
  width: 100%;
}
.preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.remove-btn {
  position: absolute;
  top: 6px; right: 6px;
  background: rgba(0,0,0,0.55);
  border: none;
  border-radius: 50%;
  width: 28px; height: 28px;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  transition: background 0.15s;
}
.remove-btn:hover { background: rgba(220,50,50,0.8); }

.upload-error { color: #e53e3e; font-size: 0.78rem; margin-top: 4px; display: block; }
</style>
