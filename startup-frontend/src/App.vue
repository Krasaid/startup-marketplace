<template>
  <div class="app">
    <nav class="navbar">
      <!-- Marca -->
      <RouterLink to="/" class="brand">
        <i class="pi pi-shop" /> StartUP!
      </RouterLink>

      <!-- Búsqueda + filtros — solo visible en el feed -->
      <div v-if="route.name === 'feed'" class="nav-search">
        <div class="nav-search-bar">
          <i class="pi pi-search" />
          <input :value="feed.busqueda"
                 placeholder="Buscar..."
                 @input="feed.setBusqueda($event.target.value)" />
          <button v-if="feed.busqueda" class="nav-clear" @click="feed.setBusqueda('')">
            <i class="pi pi-times" />
          </button>
        </div>

        <div class="nav-chips">
          <button class="nav-chip"
                  :class="{ activo: feed.categoriaActiva === null }"
                  @click="feed.seleccionarCategoria(null)">
            Todos
          </button>
          <button v-for="cat in feed.categoriasDisponibles" :key="cat"
                  class="nav-chip"
                  :class="{ activo: feed.categoriaActiva === cat }"
                  @click="feed.seleccionarCategoria(cat)">
            {{ cat }}
          </button>
        </div>
      </div>

      <!-- Links -->
      <div class="nav-links">
        <a class="nav-link" :class="{ 'router-link-active': route.name === 'feed' }" href="#" @click.prevent="irAFeed">Explorar</a>
        <RouterLink v-if="auth.isEmprendedor || auth.isAdmin"
                    to="/mi-stand" class="nav-link">
          Mi Stand
        </RouterLink>
      </div>

      <!-- Acciones -->
      <div class="nav-actions">
        <template v-if="auth.isAuthenticated">
          <span class="nav-email">{{ auth.user.email }}</span>
          <RouterLink to="/perfil" class="nav-avatar" title="Mi perfil">
              <img v-if="auth.user.fotoPerfil" 
                   :src="auth.user.fotoPerfil" 
                   :alt="auth.user.nombre || auth.user.email" />
              <span v-else class="nav-inicial">
                  {{ (auth.user.nombre || auth.user.email).charAt(0).toUpperCase() }}
              </span>
          </RouterLink>
          <Button label="Salir" severity="secondary" size="small"
                  @click="handleLogout" />
        </template>
        <template v-else>
          <RouterLink to="/login">
            <Button label="Entrar" severity="secondary" size="small" />
          </RouterLink>
          <RouterLink to="/registro">
            <Button label="Registrarse" size="small" />
          </RouterLink>
        </template>
      </div>
    </nav>

    <main class="main-content">
      <RouterView />
    </main>

    <Toast />
  </div>
</template>

<script setup>
import { useAuthStore } from './stores/auth'
import { useFeedStore } from './stores/feed'
import { useRoute, useRouter } from 'vue-router'
import Button from 'primevue/button'
import Toast  from 'primevue/toast'

const auth   = useAuthStore()
const feed   = useFeedStore()
const route  = useRoute()
const router = useRouter()

function handleLogout() {
  auth.logout()
  router.push('/login')
}

function irAFeed() {
  if (route.name === 'feed') {
    // Ya estamos en el feed: limpiar filtros y volver al inicio
    feed.limpiarTodo()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  } else {
    router.push('/')
  }
}
</script>

<style>
* { box-sizing: border-box; margin: 0; padding: 0; }

body {
  font-family: 'Segoe UI', sans-serif;
  background: #f4f8f3;
  color: #2A4526;
}

.app { min-height: 100vh; display: flex; flex-direction: column; }

/* ── NAVBAR ─────────────────────────────────────────────────────── */
.navbar {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0 1rem;
  height: 56px;
  background: var(--color-forest, #2A4526);
  border-bottom: 1px solid rgba(255,255,255,0.1);
  position: sticky;
  top: 0;
  z-index: 100;
  overflow-x: auto;
  overflow-y: hidden;
}

.brand {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--color-text);
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 0.4rem;
  white-space: nowrap;
  flex-shrink: 0;
}

/* ── SEARCH EN NAVBAR ────────────────────────────────────────────── */
.nav-search {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  min-width: 0;
}

.nav-search-bar {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: var(--color-card);
  border: 1.5px solid var(--color-border);
  border-radius: 99px;
  padding: 0.4rem 0.9rem;
  transition: border-color .2s;
  width: 220px;
  flex-shrink: 0;
}
.nav-search-bar:focus-within {
  border-color: var(--color-deep);
  background: var(--color-card);
}
.nav-search-bar i    { color: var(--color-primary); font-size: 0.9rem; flex-shrink: 0; }
.nav-search-bar input {
  border: none; outline: none;
  font-size: 0.875rem; background: transparent;
  color: var(--color-text); width: 100%;
}
.nav-clear {
  background: none; border: none;
  cursor: pointer; color: var(--color-muted); padding: 0;
  display: flex; align-items: center; flex-shrink: 0;
}
.nav-clear:hover { color: var(--color-primary); }

.nav-chips {
  display: flex;
  gap: 0.35rem;
  overflow-x: auto;
  scrollbar-width: none;
  flex: 1;
  min-width: 0;
}
.nav-chips::-webkit-scrollbar { display: none; }

.nav-chip {
  padding: 0.3rem 0.8rem;
  border-radius: 99px;
  border: 1.5px solid var(--color-border);
  background: var(--color-card);
  font-size: 0.78rem; font-weight: 500;
  color: var(--color-muted); cursor: pointer;
  transition: all .15s;
  white-space: nowrap;
  flex-shrink: 0;
}
.nav-chip:hover  { border-color: var(--color-deep); color: var(--color-text); }
.nav-chip.activo {
  background: var(--color-deep); border-color: var(--color-deep);
  color: var(--color-text); font-weight: 600;
}

/* ── LINKS Y ACCIONES ───────────────────────────────────────────── */
.nav-links { display: flex; gap: 1.25rem; flex-shrink: 0; }

.nav-link {
  text-decoration: none;
  color: var(--color-muted);
  font-size: 0.9rem;
  font-weight: 500;
  white-space: nowrap;
}
.nav-link:hover, .router-link-active { color: var(--color-primary); }

.nav-actions { display: flex; align-items: center; gap: 0.6rem; flex-shrink: 0; }
.nav-email   { font-size: 0.8rem; color: var(--color-muted); white-space: nowrap; display: none; }

.nav-avatar {
  width: 34px; height: 34px;
  border-radius: 50%;
  border: 2px solid var(--color-primary);
  overflow: hidden;
  background: var(--color-deep);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; text-decoration: none;
  transition: opacity .15s;
}
.nav-avatar:hover { opacity: .85; }
.nav-avatar img   { width: 100%; height: 100%; object-fit: cover; }
.nav-inicial      { font-size: .85rem; font-weight: 700; color: var(--color-text); }

.main-content { flex: 1; padding: 2rem; max-width: 1200px;
                margin: 0 auto; width: 100%; }

@media (max-width: 768px) {
  .navbar {
    padding: 0 0.75rem;
    gap: 0.5rem;
  }
  .nav-links {
    gap: 0.75rem;
  }
  .main-content {
    padding: 1rem;
  }
}
</style>