import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '../service/api'

export const useAuthStore = defineStore('auth', () => {

    const token = ref(localStorage.getItem('token') || null)
    const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

    const isAuthenticated = computed(() => !!token.value)
    const isEmprendedor = computed(() => user.value?.rol === 'EMPRENDEDOR')
    const isAdmin = computed(() => user.value?.rol === 'ADMIN')

    async function login(email, password) {
        const data = await authApi.login({ email, password })
        _setSession(data)
    }

    async function register(email, password, rol = 'COMPRADOR') {
        const data = await authApi.register({ email, password, rol })
        _setSession(data)
    }

    function logout() {
        token.value = null
        user.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('user')
    }

    function _setSession(data) {
        token.value = data.token
        user.value  = { 
            email:      data.email, 
            rol:        data.rol,
            nombre:     data.nombre     || null,
            apellido:   data.apellido   || null,
            fotoPerfil: data.fotoPerfil || null
        }
        localStorage.setItem('token', data.token)
        localStorage.setItem('user', JSON.stringify(user.value))
    }

    function updateUser(profileData) {
        user.value = { ...user.value, ...profileData }
        localStorage.setItem('user', JSON.stringify(user.value))
    }

    return {
        token, user, isAuthenticated, isEmprendedor, isAdmin,
        login, register, logout, updateUser
    }
})