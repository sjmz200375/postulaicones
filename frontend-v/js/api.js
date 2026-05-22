const API = {

    getApiKey() {
        return localStorage.getItem(CONFIG.STORAGE_KEYS.API_KEY) || '';
    },

    publicHeaders() {
        return { 'Content-Type': 'application/json' };
    },

    authHeaders() {
        const user = JSON.parse(
            localStorage.getItem(CONFIG.STORAGE_KEYS.USER) || 'null'
        );
        return {
            'Content-Type': 'application/json',
            [CONFIG.API_KEY_HEADER]: this.getApiKey(),
            'X-User-Id':  user ? String(user.userId)  : '',
            'X-Username': user ? String(user.username) : ''
        };
    },

    async publicGet(endpoint) {
        try {
            const res = await fetch(`${CONFIG.API_BASE}${endpoint}`, {
                method: 'GET',
                headers: this.publicHeaders()
            });
            return this._handle(res);
        } catch {
            return {
                error: true,
                mensaje: 'Error de conexión con el servidor. Intenta nuevamente más tarde.',
                _ok: false
            };
        }
    },

    async get(endpoint) {
        try {
            const res = await fetch(`${CONFIG.API_BASE}${endpoint}`, {
                method: 'GET',
                headers: this.authHeaders()
            });
            return this._handle(res);
        } catch {
            return {
                error: true,
                mensaje: 'Error de conexión con el servidor. Intenta nuevamente más tarde.',
                _ok: false
            };
        }
    },

    async publicPost(endpoint, body) {
        try {
            const res = await fetch(`${CONFIG.API_BASE}${endpoint}`, {
                method: 'POST',
                headers: this.publicHeaders(),
                body: JSON.stringify(body)
            });
            return this._handle(res);
        } catch {
            return {
                error: true,
                mensaje: 'Error de conexión con el servidor. Intenta nuevamente más tarde.',
                _ok: false
            };
        }
    },

    async post(endpoint, body) {
        try {
            const res = await fetch(`${CONFIG.API_BASE}${endpoint}`, {
                method: 'POST',
                headers: this.authHeaders(),
                body: JSON.stringify(body)
            });
            return this._handle(res);
        } catch {
            return {
                error: true,
                mensaje: 'Error de conexión con el servidor. Intenta nuevamente más tarde.',
                _ok: false
            };
        }
    },

    async put(endpoint, body) {
        try {
            const res = await fetch(`${CONFIG.API_BASE}${endpoint}`, {
                method: 'PUT',
                headers: this.authHeaders(),
                body: JSON.stringify(body)
            });
            return this._handle(res);
        } catch {
            return {
                error: true,
                mensaje: 'Error de conexión con el servidor. Intenta nuevamente más tarde.',
                _ok: false
            };
        }
    },

    async delete(endpoint) {
        try {
            const res = await fetch(`${CONFIG.API_BASE}${endpoint}`, {
                method: 'DELETE',
                headers: this.authHeaders()
            });
            return this._handle(res);
        } catch {
            return {
                error: true,
                mensaje: 'Error de conexión con el servidor. Intenta nuevamente más tarde.',
                _ok: false
            };
        }
    },

    async _handle(res) {
        let data = {};
        try {
            data = await res.json();
        } catch {
            data = { mensaje: 'Respuesta inválida del servidor' };
        }

        if (res.status === 401 && Auth.isLoggedIn()) {
            Auth.logout();
            return { error: true, mensaje: 'Sesión expirada', _status: 401, _ok: false };
        }

        data._status = res.status;
        data._ok = res.ok;
        return data;
    }
};
