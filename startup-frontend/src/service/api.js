const BASE_URL = 'http://localhost:8080/api'

// Interceptor central: inyecta JWT en cada request automáticamente
async function request(method, endpoint, body = null) {
    const headers = { 'Content-Type': 'application/json' }

    const token = localStorage.getItem('token')
    if (token) {
        headers['Authorization'] = `Bearer ${token}`
    }

    const config = { method, headers }
    if (body) config.body = JSON.stringify(body)

    const response = await fetch(`${BASE_URL}${endpoint}`, config)

    // 401 → limpiar sesión y redirigir al login
    if (response.status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        window.location.href = '/login'
        return
    }

    // 302 no aplica aquí — los redirects van por URL directa del navegador
    if (!response.ok) {
        const error = await response.json().catch(() => ({}))
        throw new Error(error.error || 'Error del servidor')
    }

    // 204 No Content (ej. deletes) → no parsear JSON
    if (response.status === 204) return null

    return response.json()
}

// ── Upload ───────────────────────────────────────────────────────────
// Usa FormData — NO pasar Content-Type, el navegador lo setea con el boundary
export const uploadApi = {
    upload: async (file) => {
        const headers = {}
        const token = localStorage.getItem('token')
        if (token) headers['Authorization'] = `Bearer ${token}`

        const formData = new FormData()
        formData.append('file', file)

        const response = await fetch(`${BASE_URL}/upload`, {
            method: 'POST',
            headers,
            body: formData
        })

        if (response.status === 401) {
            localStorage.removeItem('token')
            localStorage.removeItem('user')
            window.location.href = '/login'
            return
        }

        if (!response.ok) {
            const error = await response.json().catch(() => ({}))
            throw new Error(error.error || 'Error al subir la imagen')
        }

        return response.json()  // { url: "http://localhost:8080/uploads/uuid.ext" }
    }
}

// ── Auth ─────────────────────────────────────────────────────────────
export const authApi = {
    register: (data) => request('POST', '/auth/register', data),
    login: (data) => request('POST', '/auth/login', data)
}

// ── Stands ───────────────────────────────────────────────────────────
export const standsApi = {
    getFeed: () => request('GET', '/stands'),
    getMio: () => request('GET', '/stands/mio'),
    getBySlug: (slug) => request('GET', `/stands/${slug}`),
    create: (data) => request('POST', '/stands', data),
    update: (id, data) => request('PUT', `/stands/${id}`, data),
    addProducto: (id, data) => request('POST', `/stands/${id}/productos`, data),
    removeProducto: (id, index) => request('DELETE', `/stands/${id}/productos/${index}`),
    updateProducto: (id, index, data) =>
        request('PATCH', `/stands/${id}/productos/${index}`, data),

    // Este sí va por URL nativa del navegador — el backend retorna 302
    // y el navegador sigue la redirección automáticamente
    getRedirectUrl: (id, platform) =>
        `http://localhost:8080/api/stands/${id}/redirect/${platform}`
}