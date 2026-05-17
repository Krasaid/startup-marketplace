<template>
  <div>
    <!-- ── HERO ────────────────────────────────────────────────── -->
    <div class="feed-hero">
      <h1>Descubre Emprendedores Locales</h1>
      <p>Explora stands únicos de Santa Ana y conecta directo con los dueños</p>
    </div>

    <!-- ── RESULTADOS ──────────────────────────────────────────── -->
    <div class="resultados-header" v-if="!loading">
      <span class="resultados-count">
        {{ standsPaginados.length === 0
            ? 'Sin resultados'
            : `${totalFiltrados} stand${totalFiltrados !== 1 ? 's' : ''}` }}
        <template v-if="feed.busqueda || feed.categoriaActiva">
          encontrados
        </template>
      </span>
      <button v-if="feed.busqueda || feed.categoriaActiva"
              class="limpiar-filtros" @click="feed.limpiarTodo()">
        <i class="pi pi-filter-slash" /> Limpiar filtros
      </button>
    </div>


    <!-- ── SKELETONS ──────────────────────────────────────────── -->
    <div v-if="loading" class="feed-grid">
      <Skeleton v-for="n in 6" :key="n" height="280px" border-radius="12px" />
    </div>

    <!-- ── GRID ──────────────────────────────────────────────── -->
    <div v-else-if="standsPaginados.length" class="feed-grid">
      <RouterLink v-for="stand in standsPaginados" :key="stand.id"
                  :to="`/stand/${stand.slug}`" class="stand-card">

        <div class="card-banner"
             :style="stand.bannerUrl
               ? `background-image: url(${stand.bannerUrl})`
               : `background: ${colorPorCategoria(stand.categoria)}`">
          <div class="card-logo">
            <img v-if="stand.logoUrl" :src="stand.logoUrl" :alt="stand.nombre" />
            <span v-else class="logo-inicial">{{ stand.nombre?.charAt(0) }}</span>
          </div>
          <div class="card-categoria-badge">{{ stand.categoria }}</div>
        </div>

        <div class="card-body">
          <h3 class="card-nombre">{{ stand.nombre }}</h3>
          <p class="card-desc">{{ truncate(stand.descripcion, 85) }}</p>

          <div class="card-footer">
            <span class="card-ciudad">
              <i class="pi pi-map-marker" /> {{ stand.ciudad }}
            </span>
            <span class="card-score">
              <i class="pi pi-star-fill" style="color:#f59e0b" />
              {{ stand.scoreSemanal }}
            </span>
          </div>
        </div>
      </RouterLink>
    </div>

    <!-- ── EMPTY STATE ────────────────────────────────────────── -->
    <div v-else class="empty-state">
      <i class="pi pi-search" />
      <h3>Sin resultados para "{{ feed.busqueda || feed.categoriaActiva }}"</h3>
      <p>Intenta con otro término o categoría</p>
      <button class="btn-limpiar" @click="feed.limpiarTodo()">
        Ver todos los stands
      </button>
    </div>

    <!-- ── PAGINACIÓN ─────────────────────────────────────────── -->
    <div v-if="totalPaginas > 1" class="paginacion">
      <button class="pag-btn"
              :disabled="feed.paginaActual === 1"
              @click="feed.paginaActual--">
        <i class="pi pi-chevron-left" />
      </button>

      <button v-for="p in totalPaginas" :key="p"
              class="pag-btn"
              :class="{ activo: p === feed.paginaActual }"
              @click="feed.paginaActual = p">
        {{ p }}
      </button>

      <button class="pag-btn"
              :disabled="feed.paginaActual === totalPaginas"
              @click="feed.paginaActual++">
        <i class="pi pi-chevron-right" />
      </button>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { standsApi } from '../service/api'
import { useFeedStore } from '../stores/feed'
import Skeleton from 'primevue/skeleton'

const feed       = useFeedStore()
const stands     = ref([])
const loading    = ref(true)
const POR_PAGINA = 9

// ── Carga inicial ─────────────────────────────────────────────────
onMounted(async () => {
  try {
    stands.value = await standsApi.getFeed()
    // Llenar categorías en el store para que el navbar las muestre
    const cats = stands.value.map(s => s.categoria).filter(Boolean)
    feed.categoriasDisponibles = [...new Set(cats)].sort()
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})

// ── Filtrado combinado (búsqueda + categoría) ─────────────────────
const standsFiltrados = computed(() => {
  let result = stands.value

  if (feed.categoriaActiva) {
    result = result.filter(s => s.categoria === feed.categoriaActiva)
  }

  if (feed.busqueda.trim()) {
    const q = feed.busqueda.toLowerCase().trim()
    result = result.filter(s =>
      s.nombre?.toLowerCase().includes(q)      ||
      s.descripcion?.toLowerCase().includes(q) ||
      s.categoria?.toLowerCase().includes(q)   ||
      s.ciudad?.toLowerCase().includes(q)
    )
  }

  return result
})

const totalFiltrados = computed(() => standsFiltrados.value.length)

// ── Paginación ────────────────────────────────────────────────────
const totalPaginas = computed(() =>
  Math.ceil(standsFiltrados.value.length / POR_PAGINA)
)

const standsPaginados = computed(() => {
  const inicio = (feed.paginaActual - 1) * POR_PAGINA
  return standsFiltrados.value.slice(inicio, inicio + POR_PAGINA)
})

// Resetear página al cambiar filtros
watch([() => feed.busqueda, () => feed.categoriaActiva], () => {
  feed.paginaActual = 1
})

// ── Helpers ───────────────────────────────────────────────────────
const truncate = (text, max) =>
  text && text.length > max ? text.slice(0, max) + '…' : text

const COLORES = {
  'Gastronomía':      'linear-gradient(135deg,#f97316,#fb923c)',
  'Moda':             'linear-gradient(135deg,#ec4899,#f472b6)',
  'Moda Alternativa': 'linear-gradient(135deg,#1e1b4b,#4c1d95)',
  'Artesanías':       'linear-gradient(135deg,#92400e,#d97706)',
  'Belleza':          'linear-gradient(135deg,#be185d,#ec4899)',
  'Tecnología':       'linear-gradient(135deg,#1d4ed8,#3b82f6)',
  'Mascotas':         'linear-gradient(135deg,#15803d,#4ade80)',
  'Hogar':            'linear-gradient(135deg,#0891b2,#22d3ee)',
}
const colorPorCategoria = (cat) =>
  COLORES[cat] || 'var(--color-secondary)'
</script>

<style scoped>
/* ── HERO ─────────────────────────────────────────────────────── */
.feed-hero {
  text-align: center;
  padding: 1.5rem 1rem 1rem;
}
.feed-hero h1 {
  font-size: 2rem; font-weight: 800;
  color: var(--color-text); margin-bottom: 0.4rem;
}
.feed-hero p { color: var(--color-muted); margin-bottom: 0; }



/* ── RESULTADOS HEADER ────────────────────────────────────────── */
.resultados-header {
  display: flex; align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}
.resultados-count { font-size: 0.85rem; color: var(--color-muted); }
.limpiar-filtros {
  background: none; border: none;
  font-size: 0.82rem; color: var(--color-primary);
  cursor: pointer; display: flex;
  align-items: center; gap: 4px;
  font-weight: 500;
}
.limpiar-filtros:hover { text-decoration: underline; }

/* ── GRID ─────────────────────────────────────────────────────── */
.feed-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(290px, 1fr));
  gap: 1.5rem;
}
.stand-card {
  background: var(--color-card); border-radius: 14px;
  overflow: hidden; text-decoration: none;
  color: inherit;
  box-shadow: 0 2px 12px rgba(0,0,0,0.2);
  transition: transform .2s, box-shadow .2s;
  display: block;
}
.stand-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 28px rgba(0,0,0,0.5);
}

.card-banner {
  height: 150px; background-size: cover;
  background-position: center; position: relative;
}
.card-logo {
  position: absolute; bottom: -20px; left: 1rem;
  width: 44px; height: 44px; border-radius: 50%;
  overflow: hidden; border: 3px solid var(--color-card);
  background: var(--color-deep);
  display: flex; align-items: center; justify-content: center;
}
.card-logo img { width: 100%; height: 100%; object-fit: cover; }
.logo-inicial  { font-size: 1.1rem; font-weight: 700; color: var(--color-text); }

.card-categoria-badge {
  position: absolute; top: 0.6rem; right: 0.6rem;
  background: rgba(0,0,0,0.45);
  backdrop-filter: blur(4px);
  color: var(--color-text); font-size: 0.7rem;
  font-weight: 600; padding: 3px 8px;
  border-radius: 99px;
  text-transform: uppercase; letter-spacing: .05em;
}

.card-body    { padding: 1.6rem 1rem 1rem; }
.card-nombre  { font-size: 1.05rem; font-weight: 700; margin-bottom: 0.3rem; }
.card-desc    { font-size: 0.85rem; color: var(--color-muted); line-height: 1.55; }
.card-footer  {
  display: flex; justify-content: space-between;
  align-items: center; margin-top: 0.85rem;
  font-size: 0.8rem; color: var(--color-muted);
}
.card-score   { display: flex; align-items: center; gap: 4px; }

/* ── EMPTY ────────────────────────────────────────────────────── */
.empty-state {
  text-align: center; padding: 4rem 2rem;
  display: flex; flex-direction: column;
  align-items: center; gap: 0.75rem;
}
.empty-state i  { font-size: 2.5rem; color: var(--color-muted); }
.empty-state h3 { font-size: 1.1rem; font-weight: 600; color: var(--color-text); }
.empty-state p  { color: var(--color-muted); font-size: 0.9rem; }
.btn-limpiar {
  margin-top: 0.5rem; padding: 0.6rem 1.5rem;
  background: var(--color-primary); color: var(--color-background); border: none;
  border-radius: 99px; cursor: pointer;
  font-size: 0.875rem; font-weight: 600;
  transition: background .15s;
}
.btn-limpiar:hover { background: var(--color-accent); }

/* ── PAGINACIÓN ───────────────────────────────────────────────── */
.paginacion {
  display: flex; justify-content: center;
  align-items: center; gap: 0.4rem;
  margin-top: 2rem; padding-bottom: 1rem;
}
.pag-btn {
  width: 36px; height: 36px;
  border-radius: 8px; border: 1.5px solid var(--color-border);
  background: var(--color-card); cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  font-size: 0.875rem; color: var(--color-muted);
  transition: all .15s;
}
.pag-btn:hover:not(:disabled) { border-color: var(--color-deep); color: var(--color-text); }
.pag-btn.activo { background: var(--color-deep); border-color: var(--color-deep); color: var(--color-text); font-weight: 700; }
.pag-btn:disabled { opacity: 0.35; cursor: not-allowed; }
</style>