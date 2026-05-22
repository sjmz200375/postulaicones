const Toast = {

    _getContainer() {
        let container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            container.className = 'toast-container';
            document.body.appendChild(container);
        }
        return container;
    },

    show(mensaje, tipo = 'success', duracion = 3500) {
        const container = this._getContainer();
        const toast = document.createElement('div');
        toast.className = `toast toast-${tipo}`;

        const icono = tipo === 'success' ? '✓' : '✕';
        toast.innerHTML = `<span>${icono}</span><span>${mensaje}</span>`;

        container.appendChild(toast);

        setTimeout(() => {
            toast.style.animation = 'slideIn 0.25s ease reverse';
            setTimeout(() => toast.remove(), 250);
        }, duracion);
    },

    success(mensaje) { this.show(mensaje, 'success'); },
    error(mensaje)   { this.show(mensaje, 'error'); }
};
