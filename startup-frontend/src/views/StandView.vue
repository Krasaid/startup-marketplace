<template>
  <div v-if="loading" class="loading-page">
    <Skeleton height="320px" style="border-radius:0" />
    <div class="loading-body">
      <Skeleton height="32px" width="260px" />
      <Skeleton height="16px" width="180px" />
      <Skeleton height="80px" />
    </div>
  </div>

  <div v-else-if="stand" class="vitrina">

    <!-- ── HERO ─────────────────────────────────────────────────── -->
    <div class="hero"
         :style="stand.bannerUrl
           ? `background-image: url(${stand.bannerUrl})`
           : 'background: linear-gradient(135deg, #788A70 0%, #A9CFA1 100%)'">
      <div class="hero-overlay" />

      <div class="hero-content">
        <div class="hero-logo">
          <img v-if="stand.logoUrl" :src="stand.logoUrl" :alt="stand.nombre" />
          <span v-else class="logo-inicial">
            {{ stand.nombre?.charAt(0) }}
          </span>
        </div>
        <div class="hero-info">
          <span class="hero-categoria">{{ stand.categoria }}</span>
          <h1 class="hero-nombre">{{ stand.nombre }}</h1>
          <span class="hero-ciudad">
            <i class="pi pi-map-marker" /> {{ stand.ciudad }}
          </span>
        </div>
        <div class="hero-score">
          <i class="pi pi-star-fill" />
          <span>{{ stand.scoreSemanal }}</span>
          <small>esta semana</small>
        </div>
      </div>
    </div>

    <!-- ── CUERPO ────────────────────────────────────────────────── -->
    <div class="vitrina-body">

      <!-- Columna principal -->
      <div class="col-main">

        <!-- Historia -->
        <section class="seccion">
          <h2 class="seccion-titulo">
            <i class="pi pi-heart" /> Nuestra historia
          </h2>
          <p class="historia-texto">{{ stand.descripcion }}</p>
        </section>

        <!-- Productos -->
        <section v-if="stand.productos && stand.productos.length" class="seccion">
          <h2 class="seccion-titulo">
            <i class="pi pi-shopping-bag" />
            Lo que ofrecemos
            <span class="badge">{{ stand.productos.length }}</span>
          </h2>

          <div class="productos-grid">
            <div v-for="(p, i) in productosDisponibles" :key="i"
                 class="producto-card">
              <div class="producto-img"
                   :style="p.imagenUrl
                     ? `background-image: url(${p.imagenUrl})`
                     : 'background: linear-gradient(135deg,#f4f8f3,#BDDDBB)'">
                <Tag v-if="!p.disponible" value="Agotado"
                     severity="danger" class="tag-agotado" />
              </div>
              <div class="producto-info">
                <span class="producto-nombre">{{ p.nombre }}</span>
                <span v-if="p.descripcion" class="producto-desc">
                  {{ p.descripcion }}
                </span>
                <span class="producto-precio">${{ p.precio }}</span>
              </div>
            </div>
          </div>

          <!-- Agotados colapsados -->
          <div v-if="productosAgotados.length" class="agotados-toggle"
               @click="verAgotados = !verAgotados">
            <i :class="verAgotados ? 'pi pi-chevron-up' : 'pi pi-chevron-down'" />
            {{ verAgotados ? 'Ocultar' : 'Ver' }}
            {{ productosAgotados.length }} producto(s) agotado(s)
          </div>
          <div v-if="verAgotados" class="productos-grid agotados">
            <div v-for="(p, i) in productosAgotados" :key="i"
                 class="producto-card opaco">
              <div class="producto-img"
                   :style="p.imagenUrl
                     ? `background-image: url(${p.imagenUrl})`
                     : 'background: #f0f0f0'">
                <Tag value="Agotado" severity="danger" class="tag-agotado" />
              </div>
              <div class="producto-info">
                <span class="producto-nombre">{{ p.nombre }}</span>
                <span class="producto-precio">${{ p.precio }}</span>
              </div>
            </div>
          </div>
        </section>

        <section v-else class="seccion sin-productos">
          <i class="pi pi-box" />
          <p>Este stand aún no tiene productos publicados.</p>
        </section>

      </div>

      <!-- Columna contacto (sticky) -->
      <aside class="col-aside">
        <div class="contacto-card">
          <h3>¿Te interesa?</h3>
          <p>Contacta directamente con el emprendedor</p>

          <div class="contacto-btns">
            <button v-if="stand.tieneWhatsapp"
                    class="btn-contacto btn-whatsapp"
                    @click="contactar('whatsapp')">
              <i class="pi pi-whatsapp" />
              Escribir por WhatsApp
            </button>

            <button v-if="stand.tieneInstagram"
                    class="btn-contacto btn-instagram"
                    @click="contactar('instagram')">
              <i class="pi pi-instagram" />
              Ver en Instagram
            </button>

            <p v-if="!stand.tieneWhatsapp && !stand.tieneInstagram"
               class="sin-contacto">
              Sin contacto configurado aún.
            </p>
          </div>

          <div class="contacto-hint">
            <i class="pi pi-shield" />
            Contacto seguro vía StartUP!
          </div>
        </div>

        <!-- Compartir -->
        <div class="compartir-card">
          <h4>Compartir este stand</h4>
          <button class="btn-compartir" @click="compartir">
            <i class="pi pi-copy" />
            {{ copiado ? '¡Enlace copiado!' : 'Copiar enlace' }}
          </button>
        </div>
      </aside>

    </div>
  </div>

  <div v-else class="no-encontrado">
    <i class="pi pi-search" style="font-size:3rem;color:#ccc" />
    <h2>Stand no encontrado</h2>
    <p>El emprendimiento que buscas no existe o fue desactivado.</p>
    <RouterLink to="/">
      <Button label="Volver al feed" icon="pi pi-arrow-left" />
    </RouterLink>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { standsApi } from '../service/api'
import Button  from 'primevue/button'
import Skeleton from 'primevue/skeleton'
import Tag     from 'primevue/tag'

const route   = useRoute()
const stand   = ref(null)
const loading = ref(true)
const verAgotados = ref(false)
const copiado     = ref(false)

onMounted(async () => {
  try {
    stand.value = await standsApi.getBySlug(route.params.slug)
  } catch {
    stand.value = null
  } finally {
    loading.value = false
  }
})

const productosDisponibles = computed(() =>
  stand.value?.productos?.filter(p => p.disponible !== false) ?? []
)
const productosAgotados = computed(() =>
  stand.value?.productos?.filter(p => p.disponible === false) ?? []
)

function contactar(platform) {
  window.open(standsApi.getRedirectUrl(stand.value.id, platform), '_blank')
}

async function compartir() {
  const url = window.location.href
  await navigator.clipboard.writeText(url)
  copiado.value = true
  setTimeout(() => copiado.value = false, 2500)
}
</script>

<style scoped>
/* ── HERO ─────────────────────────────────────────────────────────── */
.hero {
  width: 100%;
  height: 320px;
  background-size: cover;
  background-position: center;
  position: relative;
  margin: -2rem -2rem 0;
  width: calc(100% + 4rem);
}
.hero-overlay {
  position: absolute; inset: 0;
  background: linear-gradient(to top,
    rgba(10,8,30,0.85) 0%,
    rgba(10,8,30,0.3) 60%,
    transparent 100%);
}
.hero-content {
  position: absolute;
  bottom: 0; left: 0; right: 0;
  display: flex;
  align-items: flex-end;
  gap: 1.25rem;
  padding: 1.75rem 2rem;
  color: #fff;
}
.hero-logo {
  width: 80px; height: 80px;
  border-radius: 50%;
  border: 3px solid var(--color-card);
  overflow: hidden;
  background: var(--color-deep);
  flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
}
.hero-logo img { width: 100%; height: 100%; object-fit: cover; }
.logo-inicial  { font-size: 2rem; font-weight: 700; color: var(--color-text); }

.hero-info     { flex: 1; }
.hero-categoria {
  font-size: 0.72rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .1em;
  color: var(--color-secondary);
  display: block;
  margin-bottom: 4px;
}
.hero-nombre { font-size: 1.9rem; font-weight: 800; line-height: 1.1; }
.hero-ciudad { font-size: 0.875rem; opacity: .8; }

.hero-score {
  display: flex; flex-direction: column;
  align-items: center; gap: 2px;
  background: rgba(255,255,255,.12);
  border: 1px solid rgba(255,255,255,.2);
  border-radius: 12px;
  padding: 0.6rem 1rem;
  backdrop-filter: blur(8px);
}
.hero-score i    { color: var(--color-accent); font-size: 1.25rem; }
.hero-score span { font-size: 1.4rem; font-weight: 700; color: var(--color-text); }
.hero-score small { font-size: 0.7rem; color: rgba(255,255,255,.7); }

/* ── LAYOUT ──────────────────────────────────────────────────────── */
.vitrina-body {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 2rem;
  padding-top: 2rem;
  align-items: start;
}
@media (max-width: 768px) {
  .vitrina-body { grid-template-columns: 1fr; }
  .hero { margin: -2rem -2rem 0; width: calc(100% + 4rem); height: 240px; }
}

/* ── SECCIONES ───────────────────────────────────────────────────── */
.seccion {
  background: var(--color-card);
  border-radius: 14px;
  padding: 1.5rem;
  margin-bottom: 1.25rem;
  box-shadow: 0 2px 12px rgba(0,0,0,0.2);
}
.seccion-titulo {
  font-size: 1rem;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.seccion-titulo i { color: var(--color-primary); }
.badge {
  font-size: 0.72rem;
  background: var(--color-deep);
  color: var(--color-text);
  padding: 2px 8px;
  border-radius: 99px;
  font-weight: 600;
}

.historia-texto {
  color: var(--color-muted);
  line-height: 1.85;
  font-size: 0.95rem;
}

/* ── PRODUCTOS ───────────────────────────────────────────────────── */
.productos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 1rem;
}
.producto-card {
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid var(--color-border);
  background: var(--color-card);
  transition: transform .2s, box-shadow .2s;
}
.producto-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.5);
}
.producto-card.opaco { opacity: 0.55; }

.producto-img {
  height: 130px;
  background-size: cover;
  background-position: center;
  position: relative;
}
.tag-agotado { position: absolute; top: 6px; right: 6px; }

.producto-info  { padding: 0.75rem; }
.producto-nombre { font-weight: 600; font-size: 0.875rem; display: block; }
.producto-desc  {
  font-size: 0.78rem; color: var(--color-muted);
  display: block; margin-top: 2px;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.producto-precio {
  display: block;
  font-size: 1rem; font-weight: 700;
  color: var(--color-accent); margin-top: 6px;
}

.agotados-toggle {
  text-align: center;
  font-size: 0.82rem;
  color: var(--color-muted);
  cursor: pointer;
  padding: 0.75rem;
  margin-top: 0.5rem;
  display: flex; align-items: center;
  justify-content: center; gap: 6px;
}
.agotados-toggle:hover { color: var(--color-primary); }

.sin-productos {
  text-align: center; color: var(--color-muted);
  display: flex; flex-direction: column;
  align-items: center; gap: 0.5rem;
  padding: 2rem;
}
.sin-productos i { font-size: 2rem; }

/* ── ASIDE CONTACTO ──────────────────────────────────────────────── */
.col-aside { position: sticky; top: 80px; }

.contacto-card {
  background: var(--color-card);
  border-radius: 14px;
  padding: 1.5rem;
  box-shadow: 0 2px 12px rgba(0,0,0,0.2);
  margin-bottom: 1rem;
}
.contacto-card h3 { font-size: 1.1rem; font-weight: 700; margin-bottom: 4px; }
.contacto-card p  { font-size: 0.85rem; color: var(--color-muted); margin-bottom: 1.25rem; }

.contacto-btns { display: flex; flex-direction: column; gap: 0.75rem; }

.btn-contacto {
  display: flex; align-items: center;
  justify-content: center; gap: 0.6rem;
  width: 100%; padding: 0.85rem;
  border: none; border-radius: 10px;
  font-size: 0.95rem; font-weight: 600;
  cursor: pointer; transition: opacity .15s, transform .15s;
}
.btn-contacto:hover { opacity: .88; transform: translateY(-1px); }
.btn-contacto i { font-size: 1.1rem; }

.btn-whatsapp  { background: var(--color-primary); color: #fff; }
.btn-instagram {
  background: linear-gradient(135deg, var(--color-secondary), var(--color-deep));
  color: #fff;
}

.sin-contacto  { text-align: center; color: var(--color-muted); font-size: 0.85rem; }

.contacto-hint {
  margin-top: 1rem;
  font-size: 0.75rem;
  color: var(--color-muted);
  text-align: center;
  display: flex; align-items: center;
  justify-content: center; gap: 4px;
}

.compartir-card {
  background: var(--color-card);
  border-radius: 14px;
  padding: 1.25rem;
  box-shadow: 0 2px 12px rgba(0,0,0,0.2);
  text-align: center;
}
.compartir-card h4 { font-size: 0.875rem; font-weight: 600; margin-bottom: 0.75rem; color: var(--color-text); }
.btn-compartir {
  display: flex; align-items: center;
  justify-content: center; gap: 6px;
  width: 100%; padding: 0.7rem;
  background: var(--color-background); color: var(--color-primary);
  border: 1px solid var(--color-border);
  border-radius: 8px; cursor: pointer;
  font-size: 0.875rem; font-weight: 600;
  transition: background .15s;
}
.btn-compartir:hover { background: var(--color-card); }

/* ── ESTADOS ─────────────────────────────────────────────────────── */
.loading-page .loading-body {
  display: flex; flex-direction: column;
  gap: 1rem; padding: 2rem;
}
.no-encontrado {
  text-align: center; padding: 5rem 2rem;
  display: flex; flex-direction: column;
  align-items: center; gap: 1rem;
}
.no-encontrado h2 { font-size: 1.5rem; font-weight: 700; }
.no-encontrado p  { color: var(--color-muted); }
</style>