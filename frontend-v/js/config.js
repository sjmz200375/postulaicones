const _isLocal = ['localhost', '127.0.0.1'].includes(window.location.hostname);
const CONFIG = {
    API_BASE: _isLocal ? 'http://localhost:8080/api' : '/api',
    API_KEY_HEADER: 'X-API-Key',
    API_KEY_VALUE: 'postulaciones-2026-secret-key',
    STORAGE_KEYS: {
        USER:    'ps_usr',
        API_KEY: 'ps_key'
    }
};
