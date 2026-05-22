const Auth = {

    save(userData) {
        localStorage.setItem(
            CONFIG.STORAGE_KEYS.USER,
            JSON.stringify(userData)
        );
        localStorage.setItem(
            CONFIG.STORAGE_KEYS.API_KEY,
            CONFIG.API_KEY_VALUE
        );
    },

    getUser() {
        const raw = localStorage.getItem(CONFIG.STORAGE_KEYS.USER);
        return raw ? JSON.parse(raw) : null;
    },

    isLoggedIn() {
        return this.getUser() !== null;
    },

    logout() {
        localStorage.removeItem(CONFIG.STORAGE_KEYS.USER);
        localStorage.removeItem(CONFIG.STORAGE_KEYS.API_KEY);
        window.location.href = 'index.html';
    },

    requireAuth() {
        if (!this.isLoggedIn()) {
            window.location.href = 'index.html';
            return false;
        }
        return true;
    },

    redirectIfLoggedIn() {
        if (this.isLoggedIn()) {
            window.location.href = 'dashboard.html';
        }
    },

    getRol() {
        const user = this.getUser();
        return user ? user.rol : null;
    },

    esAdmin() {
        return this.getRol() === 'ADMIN';
    },

    esCoordinador() {
        return this.getRol() === 'COORDINADOR';
    },

    esSecretaria() {
        return this.getRol() === 'SECRETARIA';
    },

    puede(accion) {
        const rol = this.getRol();
        const permisos = {
            'aprobar_postulacion':  ['ADMIN', 'COORDINADOR'],
            'rechazar_postulacion': ['ADMIN', 'COORDINADOR', 'SECRETARIA'],
            'eliminar_postulacion': ['ADMIN'],
            'editar_tercero':       ['ADMIN', 'COORDINADOR', 'SECRETARIA'],
            'eliminar_tercero':     ['ADMIN'],
            'ver_historial':        ['ADMIN', 'COORDINADOR'],
            'ver_usuarios':         ['ADMIN'],
            'crear_usuario':        ['ADMIN'],
            'editar_usuario':       ['ADMIN'],
            'eliminar_usuario':     ['ADMIN'],
            'ver_catalogos':        ['ADMIN', 'COORDINADOR'],
            'crear_catalogo':       ['ADMIN', 'COORDINADOR'],
            'editar_catalogo':      ['ADMIN', 'COORDINADOR'],
            'eliminar_catalogo':    ['ADMIN']
        };
        return permisos[accion] ? permisos[accion].includes(rol) : false;
    }
};
