import { defineStore } from 'pinia'
import { ref } from 'vue'

// Estado compartido de búsqueda entre App.vue (navbar) y FeedView
export const useFeedStore = defineStore('feed', () => {
  const busqueda        = ref('')
  const categoriaActiva = ref(null)
  const paginaActual    = ref(1)

  // Categorías disponibles — FeedView las llena cuando carga el feed
  const categoriasDisponibles = ref([])

  function setBusqueda(val) {
    busqueda.value     = val
    paginaActual.value = 1
  }

  function seleccionarCategoria(cat) {
    categoriaActiva.value = cat
    paginaActual.value    = 1
  }

  function limpiarTodo() {
    busqueda.value        = ''
    categoriaActiva.value = null
    paginaActual.value    = 1
  }

  return {
    busqueda, categoriaActiva, paginaActual, categoriasDisponibles,
    setBusqueda, seleccionarCategoria, limpiarTodo
  }
})
