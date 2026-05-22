if (window.location.pathname.endsWith('/dashboard.html')) {
    history.replaceState(null, '', './dashboard');
}

function sanitizar(texto) {
    if (texto === null || texto === undefined) return '';
    const div = document.createElement('div');
    div.textContent = String(texto);
    return div.innerHTML;
}

const paginacion = {
    postulaciones: { page: 0, totalPages: 1 },
    terceros:      { page: 0, totalPages: 1 },
    usuarios:      { page: 0, totalPages: 1 },
    historial:     { page: 0, totalPages: 1 }
};

async function cargarComponente(url, contenedorId) {
    const res  = await fetch(url);
    const html = await res.text();
    document.getElementById(contenedorId).innerHTML = html;
}

async function iniciarDashboard() {
    await Promise.all([
        cargarComponente('components/sidebar.html',    'sidebar-container'),
        cargarComponente('modules/dashboard.html',     'main-content'),
    ]);

    const main = document.getElementById('main-content');
    const modulos = ['postulaciones', 'terceros', 'historial', 'catalogos', 'usuarios'];

    for (const mod of modulos) {
        const res  = await fetch(`modules/${mod}.html`);
        const html = await res.text();
        const div  = document.createElement('div');
        div.innerHTML = html;
        main.appendChild(div.firstElementChild);
    }

    inicializarApp();
}

function inicializarApp() {
    if (!Auth.requireAuth()) return;

    const usuario = Auth.getUser();
    if (usuario) {
        document.getElementById('sidebar-username').textContent = usuario.username || 'Admin';
        const rolEl = document.getElementById('sidebar-rol');
        if (rolEl) rolEl.textContent = usuario.rol || '';
    }

    aplicarPermisosSidebar();

    const btnNuevoUsuario = document.getElementById('btn-nuevo-usuario');
    if (btnNuevoUsuario && !Auth.puede('crear_usuario')) {
        btnNuevoUsuario.style.display = 'none';
    }

    if (!Auth.puede('crear_catalogo')) {
        ['btn-nuevo-programa', 'btn-nueva-area', 'btn-nuevo-cargo'].forEach(id => {
            const btn = document.getElementById(id);
            if (btn) btn.style.display = 'none';
        });
    }

    document.getElementById('btn-logout').addEventListener('click', () => {
        Auth.logout();
    });

    registrarEventosNavegacion();
    registrarEventosModales();
    registrarEventosPostulaciones();
    registrarEventosTerceros();
    registrarEventosUsuarios();
    registrarEventosCatalogos();
    registrarEventosHistorial();

    activarModulo('dashboard');
}

function aplicarPermisosSidebar() {
    document.querySelectorAll('.sidebar-nav a').forEach(link => {
        const modulo = link.dataset.module;
        let visible  = true;

        if (modulo === 'usuarios'  && !Auth.puede('ver_usuarios'))  visible = false;
        if (modulo === 'historial' && !Auth.puede('ver_historial')) visible = false;
        if (modulo === 'catalogos' && !Auth.puede('ver_catalogos')) visible = false;

        link.style.display = visible ? 'block' : 'none';
    });
}

function activarModulo(nombre) {
    if (nombre === 'usuarios'  && !Auth.puede('ver_usuarios'))  { Toast.error('No tienes permisos para acceder a este módulo'); return; }
    if (nombre === 'historial' && !Auth.puede('ver_historial')) { Toast.error('No tienes permisos para acceder a este módulo'); return; }
    if (nombre === 'catalogos' && !Auth.puede('ver_catalogos')) { Toast.error('No tienes permisos para acceder a este módulo'); return; }

    document.querySelectorAll('.sidebar-nav a').forEach(a => {
        a.classList.toggle('active', a.dataset.module === nombre);
    });
    document.querySelectorAll('.module').forEach(m => {
        m.classList.toggle('active', m.id === 'module-' + nombre);
    });

    const titulos = {
        dashboard:     'Dashboard — Sistema de Postulaciones',
        postulaciones: 'Postulaciones — Sistema de Postulaciones',
        terceros:      'Terceros — Sistema de Postulaciones',
        historial:     'Historial — Sistema de Postulaciones',
        usuarios:      'Usuarios — Sistema de Postulaciones',
        catalogos:     'Catálogos — Sistema de Postulaciones'
    };
    document.title = titulos[nombre] || 'Panel Admin';

    switch (nombre) {
        case 'dashboard':     cargarStats();        break;
        case 'postulaciones': cargarPostulaciones(); break;
        case 'terceros':      cargarTerceros();      break;
        case 'historial':     cargarHistorial();     break;
        case 'usuarios':      cargarUsuarios();      break;
        case 'catalogos':     iniciarTabs(); cargarTabActiva(); break;
    }
}

function registrarEventosNavegacion() {
    document.querySelectorAll('.sidebar-nav a').forEach(a => {
        a.addEventListener('click', (e) => {
            e.preventDefault();
            activarModulo(a.dataset.module);
        });
    });
}

function openModal(id)  { document.getElementById(id).classList.add('open'); }
function closeModal(id) { document.getElementById(id).classList.remove('open'); }

function registrarEventosModales() {
    document.querySelectorAll('[data-close]').forEach(btn => {
        btn.addEventListener('click', () => closeModal(btn.dataset.close));
    });
    document.querySelectorAll('.modal-overlay').forEach(overlay => {
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) closeModal(overlay.id);
        });
    });
}

function badgeEstado(estado) {
    const clases = {
        APROBADA:  'badge badge-aprobada',
        RECHAZADA: 'badge badge-rechazada',
        PENDIENTE: 'badge badge-pendiente'
    };
    return `<span class="${clases[estado] || 'badge badge-pendiente'}">${estado}</span>`;
}

function badgeTipoPost(tipo) {
    return `<span class="badge-tipo">${tipo}</span>`;
}

const TIPO_TERC_LABEL = { '0': 'Estudiante', '1': 'Profesor', '2': 'Administrativo' };

function formatFecha(iso) {
    if (!iso) return '—';
    return new Date(iso).toLocaleDateString('es-CO', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

function formatFechaHora(iso) {
    if (!iso) return '—';
    const d = new Date(iso);
    return d.toLocaleDateString('es-CO', { day: '2-digit', month: '2-digit', year: 'numeric' })
         + ' ' + d.toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' });
}

function actualizarPaginacion(modulo, pageData) {
    const page       = pageData.number       ?? 0;
    const totalPages = pageData.totalPages   ?? 1;
    const total      = pageData.totalElements ?? 0;
    const desde      = total === 0 ? 0 : page * 20 + 1;
    const hasta      = Math.min(page * 20 + (pageData.numberOfElements ?? 0), total);

    paginacion[modulo].page       = page;
    paginacion[modulo].totalPages = totalPages;

    const ids = {
        postulaciones: { info: 'info-postulaciones', page: 'page-post',  prev: 'btn-prev-post', next: 'btn-next-post' },
        terceros:      { info: 'info-terceros',       page: 'page-terc', prev: 'btn-prev-terc', next: 'btn-next-terc' },
        usuarios:      { info: 'info-usuarios',       page: 'page-user', prev: 'btn-prev-user', next: 'btn-next-user' },
        historial:     { info: 'info-historial',      page: 'page-hist', prev: 'btn-prev-hist', next: 'btn-next-hist' }
    }[modulo];

    document.getElementById(ids.info).textContent = total === 0
        ? 'Sin resultados'
        : `Mostrando ${desde}–${hasta} de ${total}`;
    document.getElementById(ids.page).textContent = `Página ${page + 1} de ${totalPages}`;
    document.getElementById(ids.prev).disabled    = pageData.first ?? (page === 0);
    document.getElementById(ids.next).disabled    = pageData.last  ?? (page >= totalPages - 1);
}

async function cargarStats() {
    ['stat-total', 'stat-pendientes', 'stat-aprobadas', 'stat-rechazadas',
     'stat-estudiantes', 'stat-profesores', 'stat-administrativos']
        .forEach(id => { document.getElementById(id).textContent = '…'; });

    const data = await API.get('/dashboard/stats');
    if (!data || data.error) { Toast.error('No se pudieron cargar las estadísticas'); return; }

    document.getElementById('stat-total').textContent           = data.total                    ?? 0;
    document.getElementById('stat-pendientes').textContent      = data.pendientes               ?? 0;
    document.getElementById('stat-aprobadas').textContent       = data.aprobadas                ?? 0;
    document.getElementById('stat-rechazadas').textContent      = data.rechazadas               ?? 0;
    document.getElementById('stat-estudiantes').textContent     = data.porTipo?.ESTUDIANTE      ?? 0;
    document.getElementById('stat-profesores').textContent      = data.porTipo?.PROFESOR        ?? 0;
    document.getElementById('stat-administrativos').textContent = data.porTipo?.ADMINISTRATIVO  ?? 0;
}

function registrarEventosDashboard() {
    document.getElementById('btn-refresh-stats').addEventListener('click', cargarStats);
}

async function cargarPostulaciones(page = 0) {
    const tbody = document.getElementById('tbody-postulaciones');
    tbody.innerHTML = '<tr><td colspan="7" class="table-empty"><span class="spinner"></span> Cargando...</td></tr>';

    const estado = document.getElementById('filtro-estado').value;
    const tipo   = document.getElementById('filtro-tipo-post').value;
    const texto  = document.getElementById('filtro-texto-post').value.trim();

    const params = [`page=${page}`, 'size=20'];
    if (texto) {
        params.push(`texto=${encodeURIComponent(texto)}`);
    } else {
        if (estado) params.push(`estado=${estado}`);
        if (tipo)   params.push(`tipo=${tipo}`);
    }

    const data = await API.get('/postulaciones?' + params.join('&'));

    if (!data || data.error || !Array.isArray(data.content)) {
        tbody.innerHTML = '<tr><td colspan="7" class="table-empty">Error al cargar postulaciones</td></tr>';
        return;
    }
    if (data.content.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="table-empty">No hay postulaciones que coincidan con los filtros aplicados</td></tr>';
        actualizarPaginacion('postulaciones', data);
        return;
    }

    tbody.innerHTML = data.content.map(p => {
        const pendiente = p.estado === 'PENDIENTE';
        const rechazada = p.estado === 'RECHAZADA';

        let acciones = `<button class="btn btn-outline btn-sm" data-action="ver-post" data-id="${p.id}">Ver</button>`;

        if (pendiente) {
            if (Auth.puede('aprobar_postulacion'))  acciones += `<button class="btn btn-success btn-sm" data-action="aprobar-post" data-id="${p.id}">Aprobar</button>`;
            if (Auth.puede('rechazar_postulacion')) acciones += `<button class="btn btn-danger btn-sm"  data-action="rechazar-post" data-id="${p.id}">Rechazar</button>`;
            if (Auth.puede('eliminar_postulacion')) acciones += `<button class="btn btn-danger btn-sm"  data-action="eliminar-post" data-id="${p.id}">Eliminar</button>`;
        } else if (rechazada) {
            if (Auth.puede('aprobar_postulacion'))  acciones += `<button class="btn btn-success btn-sm" data-action="aprobar-post" data-id="${p.id}">Aprobar</button>`;
            if (Auth.puede('eliminar_postulacion')) acciones += `<button class="btn btn-danger btn-sm"  data-action="eliminar-post" data-id="${p.id}">Eliminar</button>`;
        }

        return `
        <tr>
            <td>${sanitizar(p.apellidos)}, ${sanitizar(p.nombres)}</td>
            <td>${sanitizar(p.tipoDoc)} ${sanitizar(p.nroDoc)}</td>
            <td>${sanitizar(p.email) || '—'}</td>
            <td>${badgeTipoPost(sanitizar(p.tipoPostulacion))}</td>
            <td>${badgeEstado(sanitizar(p.estado))}</td>
            <td>${formatFecha(p.fechaCreacion)}</td>
            <td><div class="actions-cell">${acciones}</div></td>
        </tr>`;
    }).join('');

    actualizarPaginacion('postulaciones', data);
}

async function verDetallePost(id) {
    const data = await API.get(`/postulaciones/${id}`);
    if (!data || data.error) { Toast.error('No se pudo cargar el detalle'); return; }

    document.getElementById('detalle-post-body').innerHTML = `
        <div class="detail-row"><span class="detail-label">Nombres</span>
            <span class="detail-value">${sanitizar(data.nombres)} ${sanitizar(data.apellidos)}</span></div>
        <div class="detail-row"><span class="detail-label">Documento</span>
            <span class="detail-value">${sanitizar(data.tipoDoc)} ${sanitizar(data.nroDoc)}</span></div>
        <div class="detail-row"><span class="detail-label">Email</span>
            <span class="detail-value">${sanitizar(data.email) || '—'}</span></div>
        <div class="detail-row"><span class="detail-label">Teléfono</span>
            <span class="detail-value">${sanitizar(data.telefono) || '—'}</span></div>
        <div class="detail-row"><span class="detail-label">Tipo postulación</span>
            <span class="detail-value">${badgeTipoPost(sanitizar(data.tipoPostulacion))}</span></div>
        <div class="detail-row"><span class="detail-label">Estado</span>
            <span class="detail-value">${badgeEstado(sanitizar(data.estado))}</span></div>
        <div class="detail-row"><span class="detail-label">Fecha postulación</span>
            <span class="detail-value">${formatFecha(data.fechaCreacion)}</span></div>
        <div class="detail-row"><span class="detail-label">ID Tercero</span>
            <span class="detail-value">${data.tercId ? '#' + sanitizar(String(data.tercId)) : '—'}</span></div>
        ${data.comentarios
            ? `<div class="detail-row"><span class="detail-label">Comentarios</span>
               <span class="detail-value">${sanitizar(data.comentarios)}</span></div>`
            : ''}
    `;
    openModal('modal-detalle-post');
}

async function aprobarPost(id) {
    if (!confirm('¿Confirmas que deseas APROBAR esta postulación? Se creará automáticamente el registro en Terceros.')) return;
    const data = await API.put(`/postulaciones/${id}/aprobar`, {});
    if (data && data._ok) {
        Toast.success('Postulación aprobada. Tercero creado con ID #' + data.tercId);
        cargarPostulaciones(paginacion.postulaciones.page);
    } else {
        Toast.error(data?.mensaje || 'No se pudo aprobar la postulación');
    }
}

async function rechazarPost(id) {
    if (!confirm('¿Confirmas que deseas RECHAZAR esta postulación?')) return;
    const data = await API.put(`/postulaciones/${id}/rechazar`, {});
    if (data && data._ok) {
        Toast.success('Postulación rechazada');
        cargarPostulaciones(paginacion.postulaciones.page);
    } else {
        Toast.error(data?.mensaje || 'No se pudo rechazar la postulación');
    }
}

async function eliminarPost(id) {
    if (!confirm('¿Confirmas que deseas ELIMINAR esta postulación? Esta acción no se puede deshacer.')) return;
    const data = await API.delete(`/postulaciones/${id}`);
    if (data && !data.error) {
        Toast.success('Postulación eliminada');
        cargarPostulaciones(paginacion.postulaciones.page);
    } else {
        Toast.error(data?.mensaje || 'No se pudo eliminar la postulación');
    }
}

function registrarEventosPostulaciones() {
    document.getElementById('tbody-postulaciones').addEventListener('click', (e) => {
        const btn = e.target.closest('button[data-action]');
        if (!btn) return;
        const id = parseInt(btn.dataset.id);
        switch (btn.dataset.action) {
            case 'ver-post':      verDetallePost(id); break;
            case 'aprobar-post':  aprobarPost(id);    break;
            case 'rechazar-post': rechazarPost(id);   break;
            case 'eliminar-post': eliminarPost(id);   break;
        }
    });
    document.getElementById('btn-buscar-post').addEventListener('click', () => cargarPostulaciones(0));
    document.getElementById('btn-limpiar-post').addEventListener('click', () => {
        document.getElementById('filtro-estado').value     = '';
        document.getElementById('filtro-tipo-post').value  = '';
        document.getElementById('filtro-texto-post').value = '';
        cargarPostulaciones(0);
    });
    document.getElementById('filtro-texto-post').addEventListener('keydown', (e) => {
        if (e.key === 'Enter') cargarPostulaciones(0);
    });
    document.getElementById('filtro-estado').addEventListener('change',    () => cargarPostulaciones(0));
    document.getElementById('filtro-tipo-post').addEventListener('change', () => cargarPostulaciones(0));
    document.getElementById('btn-prev-post').addEventListener('click', () => {
        if (paginacion.postulaciones.page > 0) cargarPostulaciones(paginacion.postulaciones.page - 1);
    });
    document.getElementById('btn-next-post').addEventListener('click', () => {
        if (paginacion.postulaciones.page < paginacion.postulaciones.totalPages - 1)
            cargarPostulaciones(paginacion.postulaciones.page + 1);
    });
}

async function cargarTerceros(page = 0) {
    const tbody = document.getElementById('tbody-terceros');
    tbody.innerHTML = '<tr><td colspan="7" class="table-empty"><span class="spinner"></span> Cargando...</td></tr>';

    const tipo   = document.getElementById('filtro-tipo-terc').value;
    const nombre = document.getElementById('filtro-nombre-terc').value.trim();

    const params = [`page=${page}`, 'size=20'];
    if (nombre)    params.push(`nombre=${encodeURIComponent(nombre)}`);
    else if (tipo) params.push(`tipo=${tipo}`);

    const data = await API.get('/terceros?' + params.join('&'));

    if (!data || data.error || !Array.isArray(data.content)) {
        tbody.innerHTML = '<tr><td colspan="7" class="table-empty">Error al cargar terceros</td></tr>';
        return;
    }
    if (data.content.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="table-empty">No hay terceros registrados. Aprueba postulaciones para que aparezcan aquí.</td></tr>';
        actualizarPaginacion('terceros', data);
        return;
    }

    tbody.innerHTML = data.content.map(t => {
        let acciones = '';
        if (Auth.puede('editar_tercero'))   acciones += `<button class="btn btn-outline btn-sm" data-action="editar-terc" data-id="${t.tercId}">Editar</button>`;
        if (Auth.puede('eliminar_tercero')) acciones += `<button class="btn btn-danger btn-sm"  data-action="eliminar-terc" data-id="${t.tercId}">Eliminar</button>`;
        if (!acciones) acciones = '<span style="color:var(--text-secondary);font-size:12px">Sin acciones</span>';

        return `
        <tr>
            <td>#${sanitizar(String(t.tercId))}</td>
            <td>${sanitizar(t.tercApellidos)}, ${sanitizar(t.tercNombres)}</td>
            <td>${sanitizar(t.tercTipoDoc)} ${sanitizar(t.tercNroDoc)}</td>
            <td>${sanitizar(t.tercEmail) || '—'}</td>
            <td>${sanitizar(t.tercTelefono) || '—'}</td>
            <td><span class="badge-tipo">${sanitizar(TIPO_TERC_LABEL[String(t.tercTipo)] || t.tercTipo)}</span></td>
            <td><div class="actions-cell">${acciones}</div></td>
        </tr>`;
    }).join('');

    actualizarPaginacion('terceros', data);
}

async function abrirEditarTerc(id) {
    const data = await API.get(`/terceros/${id}`);
    if (!data || data.error) { Toast.error('No se pudo cargar el tercero'); return; }

    document.getElementById('edit-terc-id').value        = data.tercId;
    document.getElementById('edit-terc-nombres').value   = data.tercNombres   || '';
    document.getElementById('edit-terc-apellidos').value = data.tercApellidos || '';
    document.getElementById('edit-terc-tipo-doc').value  = data.tercTipoDoc   || 'CC';
    document.getElementById('edit-terc-nro-doc').value   = data.tercNroDoc    || '';
    document.getElementById('edit-terc-email').value     = data.tercEmail     || '';
    document.getElementById('edit-terc-telefono').value  = data.tercTelefono  || '';
    document.getElementById('edit-terc-direccion').value = data.tercDireccion || '';
    openModal('modal-editar-terc');
}

async function eliminarTerc(id) {
    if (!confirm('⚠️ Al eliminar este tercero, la postulación vinculada volverá automáticamente a estado PENDIENTE.\n\n¿Deseas continuar?')) return;
    const data = await API.delete(`/terceros/${id}`);
    if (data && !data.error) {
        Toast.success('Tercero eliminado. La postulación volvió a estado PENDIENTE.');
        cargarTerceros(paginacion.terceros.page);
    } else {
        Toast.error(data?.mensaje || 'No se pudo eliminar el tercero');
    }
}

function registrarEventosTerceros() {
    document.getElementById('tbody-terceros').addEventListener('click', (e) => {
        const btn = e.target.closest('button[data-action]');
        if (!btn) return;
        const id = parseInt(btn.dataset.id);
        if (btn.dataset.action === 'editar-terc')   abrirEditarTerc(id);
        else if (btn.dataset.action === 'eliminar-terc') eliminarTerc(id);
    });
    document.getElementById('btn-buscar-terc').addEventListener('click', () => cargarTerceros(0));
    document.getElementById('btn-limpiar-terc').addEventListener('click', () => {
        document.getElementById('filtro-tipo-terc').value   = '';
        document.getElementById('filtro-nombre-terc').value = '';
        cargarTerceros(0);
    });
    document.getElementById('filtro-nombre-terc').addEventListener('keydown', (e) => {
        if (e.key === 'Enter') cargarTerceros(0);
    });
    document.getElementById('filtro-tipo-terc').addEventListener('change', () => cargarTerceros(0));
    document.getElementById('btn-prev-terc').addEventListener('click', () => {
        if (paginacion.terceros.page > 0) cargarTerceros(paginacion.terceros.page - 1);
    });
    document.getElementById('btn-next-terc').addEventListener('click', () => {
        if (paginacion.terceros.page < paginacion.terceros.totalPages - 1)
            cargarTerceros(paginacion.terceros.page + 1);
    });
    document.getElementById('btn-guardar-terc').addEventListener('click', async () => {
        const id        = document.getElementById('edit-terc-id').value;
        const nombres   = document.getElementById('edit-terc-nombres').value.trim();
        const apellidos = document.getElementById('edit-terc-apellidos').value.trim();
        if (!nombres || !apellidos) { Toast.error('Nombres y apellidos son requeridos'); return; }

        const body = {
            tercNombres:   nombres.toUpperCase(),
            tercApellidos: apellidos.toUpperCase(),
            tercTipoDoc:   document.getElementById('edit-terc-tipo-doc').value,
            tercNroDoc:    document.getElementById('edit-terc-nro-doc').value.trim(),
            tercEmail:     document.getElementById('edit-terc-email').value.trim().toLowerCase(),
            tercTelefono:  document.getElementById('edit-terc-telefono').value.trim(),
            tercDireccion: document.getElementById('edit-terc-direccion').value.trim()
        };

        const btn = document.getElementById('btn-guardar-terc');
        btn.disabled = true; btn.textContent = 'Guardando...';

        const data = await API.put(`/terceros/${id}`, body);
        btn.disabled = false; btn.textContent = 'Guardar cambios';

        if (data && data._ok) {
            Toast.success('Tercero actualizado correctamente');
            closeModal('modal-editar-terc');
            cargarTerceros(paginacion.terceros.page);
        } else {
            Toast.error(data?.mensaje || 'No se pudo actualizar el tercero');
        }
    });
}

async function cargarUsuarios(page = 0) {
    const tbody = document.getElementById('tbody-usuarios');
    tbody.innerHTML = '<tr><td colspan="5" class="table-empty"><span class="spinner"></span> Cargando...</td></tr>';

    const data = await API.get(`/usuarios?page=${page}&size=20`);

    if (!data || data.error || !Array.isArray(data.content)) {
        tbody.innerHTML = '<tr><td colspan="5" class="table-empty">Error al cargar usuarios</td></tr>';
        return;
    }
    if (data.content.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="table-empty">No hay usuarios registrados en el sistema</td></tr>';
        actualizarPaginacion('usuarios', data);
        return;
    }

    tbody.innerHTML = data.content.map(u => {
        let acciones = '';
        if (Auth.puede('editar_usuario'))   acciones += `<button class="btn btn-outline btn-sm" data-action="editar-usuario" data-id="${u.userId}" data-username="${sanitizar(u.userUsername)}" data-rol="${sanitizar(u.userRol)}" data-activo="${u.userActivo}">Editar</button>`;
        if (Auth.puede('eliminar_usuario')) acciones += `<button class="btn btn-danger btn-sm"  data-action="eliminar-usuario" data-id="${u.userId}" data-username="${sanitizar(u.userUsername)}">Eliminar</button>`;
        if (!acciones) acciones = '<span style="color:var(--text-secondary);font-size:12px">Sin acciones</span>';

        return `
        <tr>
            <td>#${sanitizar(String(u.userId))}</td>
            <td>${sanitizar(u.userUsername)}</td>
            <td><span class="badge-tipo">${sanitizar(u.userRol)}</span></td>
            <td>${u.userActivo
                ? '<span class="badge badge-aprobada">Activo</span>'
                : '<span class="badge badge-rechazada">Inactivo</span>'}</td>
            <td><div class="actions-cell">${acciones}</div></td>
        </tr>`;
    }).join('');

    actualizarPaginacion('usuarios', data);
}

function abrirEditarUsuario(id, username, rol, activo) {
    document.getElementById('edit-user-id').value       = id;
    document.getElementById('edit-user-username').value = username;
    document.getElementById('edit-user-password').value = '';
    document.getElementById('edit-user-rol').value      = rol;
    document.getElementById('edit-user-activo').value   = activo ? 'true' : 'false';
    document.getElementById('modal-usuario-titulo').textContent = 'Editar usuario';
    document.getElementById('label-password').textContent      = 'Nueva contraseña (dejar vacío para no cambiar)';
    ['err-user-username', 'err-user-password'].forEach(id => {
        document.getElementById(id).classList.remove('visible');
    });
    openModal('modal-usuario');
}

async function eliminarUsuario(id, username) {
    if (!confirm(`¿Confirmas que deseas eliminar el usuario "${username}"? Esta acción no se puede deshacer.`)) return;
    const data = await API.delete(`/usuarios/${id}`);
    if (data && !data.error) {
        Toast.success('Usuario eliminado');
        cargarUsuarios(paginacion.usuarios.page);
    } else {
        Toast.error(data?.mensaje || 'No se pudo eliminar el usuario');
    }
}

function registrarEventosUsuarios() {
    document.getElementById('tbody-usuarios').addEventListener('click', (e) => {
        const btn = e.target.closest('button[data-action]');
        if (!btn) return;
        if (btn.dataset.action === 'editar-usuario') {
            abrirEditarUsuario(
                parseInt(btn.dataset.id),
                btn.dataset.username,
                btn.dataset.rol,
                btn.dataset.activo === 'true'
            );
        } else if (btn.dataset.action === 'eliminar-usuario') {
            eliminarUsuario(parseInt(btn.dataset.id), btn.dataset.username);
        }
    });
    document.getElementById('btn-prev-user').addEventListener('click', () => {
        if (paginacion.usuarios.page > 0) cargarUsuarios(paginacion.usuarios.page - 1);
    });
    document.getElementById('btn-next-user').addEventListener('click', () => {
        if (paginacion.usuarios.page < paginacion.usuarios.totalPages - 1)
            cargarUsuarios(paginacion.usuarios.page + 1);
    });
    document.getElementById('btn-nuevo-usuario').addEventListener('click', () => {
        document.getElementById('edit-user-id').value       = '';
        document.getElementById('edit-user-username').value = '';
        document.getElementById('edit-user-password').value = '';
        document.getElementById('edit-user-rol').value      = 'ADMIN';
        document.getElementById('edit-user-activo').value   = 'true';
        document.getElementById('modal-usuario-titulo').textContent = 'Nuevo usuario';
        document.getElementById('label-password').textContent      = 'Contraseña *';
        ['err-user-username', 'err-user-password'].forEach(id => {
            document.getElementById(id).classList.remove('visible');
        });
        openModal('modal-usuario');
    });
    document.getElementById('btn-guardar-usuario').addEventListener('click', async () => {
        const id       = document.getElementById('edit-user-id').value;
        const username = document.getElementById('edit-user-username').value.trim();
        const password = document.getElementById('edit-user-password').value.trim();
        const esNuevo  = !id;

        let ok = true;
        if (!username) { document.getElementById('err-user-username').classList.add('visible'); ok = false; }
        else           { document.getElementById('err-user-username').classList.remove('visible'); }
        if (esNuevo && !password) { document.getElementById('err-user-password').classList.add('visible'); ok = false; }
        else                      { document.getElementById('err-user-password').classList.remove('visible'); }
        if (!ok) return;

        const body = {
            userUsername: username,
            userRol:      document.getElementById('edit-user-rol').value,
            userActivo:   document.getElementById('edit-user-activo').value === 'true'
        };
        if (password) body.userPassword = password;

        const btn = document.getElementById('btn-guardar-usuario');
        btn.disabled = true; btn.textContent = 'Guardando...';

        const data = esNuevo
            ? await API.post('/usuarios', body)
            : await API.put(`/usuarios/${id}`, body);

        btn.disabled = false; btn.textContent = 'Guardar';

        if (data && (data._ok || data.userId)) {
            Toast.success(esNuevo ? 'Usuario creado correctamente' : 'Usuario actualizado correctamente');
            closeModal('modal-usuario');
            cargarUsuarios(paginacion.usuarios.page);
        } else if (data?._status === 409) {
            Toast.error('Ya existe un usuario con ese username');
        } else {
            Toast.error(data?.mensaje || 'No se pudo guardar el usuario');
        }
    });
}

let tabActivaCatalogo = 'programas';

function iniciarTabs() {
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
            btn.classList.add('active');
            const tab = btn.dataset.tab;
            document.getElementById('tab-' + tab).classList.add('active');
            tabActivaCatalogo = tab;
            cargarTabActiva();
        });
    });
}

function cargarTabActiva() {
    switch (tabActivaCatalogo) {
        case 'programas': cargarProgramas(); break;
        case 'areas':     cargarAreas();     break;
        case 'cargos':    cargarCargos();    break;
    }
}

async function cargarCatalogo(endpoint, tbodyId, tipo) {
    const tbody = document.getElementById(tbodyId);
    tbody.innerHTML = `<tr><td colspan="5" class="table-empty"><span class="spinner"></span> Cargando...</td></tr>`;

    const data = await API.get(endpoint);

    if (!data || data.error || !Array.isArray(data)) {
        tbody.innerHTML = `<tr><td colspan="5" class="table-empty">Error al cargar los datos</td></tr>`;
        return;
    }
    if (data.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="table-empty">No hay registros. Crea el primero con el botón de arriba.</td></tr>`;
        return;
    }

    const idKey = tipo === 'programas' ? 'programaId' : tipo === 'areas' ? 'areaId' : 'cargoId';

    tbody.innerHTML = data.map(item => {
        let acciones = '';
        if (Auth.puede('editar_catalogo')) {
            acciones += `<button class="btn btn-outline btn-sm" data-action="editar-catalogo"
                data-tipo="${tipo}" data-id="${item[idKey]}"
                data-nombre="${sanitizar(item.nombre || '')}"
                data-descripcion="${sanitizar(item.descripcion || '')}"
                data-activo="${item.activo}">Editar</button>`;
        }
        if (Auth.puede('eliminar_catalogo')) {
            acciones += `<button class="btn btn-danger btn-sm" data-action="eliminar-catalogo"
                data-tipo="${tipo}" data-id="${item[idKey]}"
                data-nombre="${sanitizar(item.nombre || '')}">Eliminar</button>`;
        }
        if (!acciones) acciones = '<span style="color:var(--text-secondary);font-size:12px">Sin acciones</span>';

        return `
        <tr>
            <td>#${sanitizar(String(item[idKey]))}</td>
            <td style="font-weight:600">${sanitizar(item.nombre)}</td>
            <td style="color:var(--text-secondary)">${sanitizar(item.descripcion) || '—'}</td>
            <td>${item.activo
                ? '<span class="badge-activo">Activo</span>'
                : '<span class="badge-inactivo">Inactivo</span>'}</td>
            <td><div class="actions-cell">${acciones}</div></td>
        </tr>`;
    }).join('');
}

async function cargarProgramas() { await cargarCatalogo('/catalogos/programas', 'tbody-programas', 'programas'); }
async function cargarAreas()     { await cargarCatalogo('/catalogos/areas',     'tbody-areas',     'areas');     }
async function cargarCargos()    { await cargarCatalogo('/catalogos/cargos',    'tbody-cargos',    'cargos');    }

function abrirNuevoCatalogo(tipo) {
    const titulos = { programas: 'Nuevo programa académico', areas: 'Nueva área / departamento', cargos: 'Nuevo cargo' };
    document.getElementById('catalogo-id').value          = '';
    document.getElementById('catalogo-tipo').value        = tipo;
    document.getElementById('catalogo-nombre').value      = '';
    document.getElementById('catalogo-descripcion').value = '';
    document.getElementById('catalogo-activo').value      = 'true';
    document.getElementById('modal-catalogo-titulo').textContent = titulos[tipo];
    document.getElementById('err-catalogo-nombre').classList.remove('visible');
    document.getElementById('catalogo-nombre').classList.remove('error');
    openModal('modal-catalogo');
}

function abrirEditarCatalogo(tipo, id, nombre, descripcion, activo) {
    const titulos = { programas: 'Editar programa académico', areas: 'Editar área / departamento', cargos: 'Editar cargo' };
    document.getElementById('catalogo-id').value          = id;
    document.getElementById('catalogo-tipo').value        = tipo;
    document.getElementById('catalogo-nombre').value      = nombre;
    document.getElementById('catalogo-descripcion').value = descripcion;
    document.getElementById('catalogo-activo').value      = activo ? 'true' : 'false';
    document.getElementById('modal-catalogo-titulo').textContent = titulos[tipo];
    document.getElementById('err-catalogo-nombre').classList.remove('visible');
    document.getElementById('catalogo-nombre').classList.remove('error');
    openModal('modal-catalogo');
}

async function eliminarCatalogo(tipo, id, nombre) {
    const labels    = { programas: 'programa académico', areas: 'área / departamento', cargos: 'cargo' };
    const endpoints = { programas: `/catalogos/programas/${id}`, areas: `/catalogos/areas/${id}`, cargos: `/catalogos/cargos/${id}` };

    if (!confirm(`¿Confirmas que deseas eliminar el ${labels[tipo]} "${nombre}"?\n\nEsta acción no se puede deshacer.`)) return;

    const data = await API.delete(endpoints[tipo]);
    if (data && !data.error) {
        Toast.success(`${labels[tipo].charAt(0).toUpperCase() + labels[tipo].slice(1)} eliminado correctamente`);
        cargarTabActiva();
    } else {
        Toast.error(data?.mensaje || `No se pudo eliminar el ${labels[tipo]}`);
    }
}

function registrarEventosCatalogos() {
    ['tbody-programas', 'tbody-areas', 'tbody-cargos'].forEach(tbodyId => {
        document.getElementById(tbodyId).addEventListener('click', (e) => {
            const btn = e.target.closest('button[data-action]');
            if (!btn) return;
            if (btn.dataset.action === 'editar-catalogo') {
                abrirEditarCatalogo(
                    btn.dataset.tipo,
                    parseInt(btn.dataset.id),
                    btn.dataset.nombre,
                    btn.dataset.descripcion,
                    btn.dataset.activo === 'true'
                );
            } else if (btn.dataset.action === 'eliminar-catalogo') {
                eliminarCatalogo(btn.dataset.tipo, parseInt(btn.dataset.id), btn.dataset.nombre);
            }
        });
    });
    document.getElementById('btn-nuevo-programa').addEventListener('click', () => abrirNuevoCatalogo('programas'));
    document.getElementById('btn-nueva-area').addEventListener('click',    () => abrirNuevoCatalogo('areas'));
    document.getElementById('btn-nuevo-cargo').addEventListener('click',   () => abrirNuevoCatalogo('cargos'));

    document.getElementById('btn-guardar-catalogo').addEventListener('click', async () => {
        const id      = document.getElementById('catalogo-id').value;
        const tipo    = document.getElementById('catalogo-tipo').value;
        const nombre  = document.getElementById('catalogo-nombre').value.trim();
        const esNuevo = !id;

        if (!nombre) {
            document.getElementById('err-catalogo-nombre').classList.add('visible');
            document.getElementById('catalogo-nombre').classList.add('error');
            return;
        }
        document.getElementById('err-catalogo-nombre').classList.remove('visible');
        document.getElementById('catalogo-nombre').classList.remove('error');

        const body = {
            nombre,
            descripcion: document.getElementById('catalogo-descripcion').value.trim(),
            activo: document.getElementById('catalogo-activo').value === 'true'
        };

        const endpoints = { programas: '/catalogos/programas', areas: '/catalogos/areas', cargos: '/catalogos/cargos' };

        const btn = document.getElementById('btn-guardar-catalogo');
        btn.disabled = true; btn.textContent = 'Guardando...';

        const data = esNuevo
            ? await API.post(endpoints[tipo], body)
            : await API.put(`${endpoints[tipo]}/${id}`, body);

        btn.disabled = false; btn.textContent = 'Guardar';

        if (data && (data._ok || data.programaId || data.areaId || data.cargoId)) {
            Toast.success(esNuevo ? 'Creado correctamente' : 'Actualizado correctamente');
            closeModal('modal-catalogo');
            cargarTabActiva();
        } else if (data?._status === 409) {
            Toast.error('Ya existe un elemento con ese nombre');
        } else {
            Toast.error(data?.mensaje || 'No se pudo guardar');
        }
    });
}

async function cargarHistorial(page = 0) {
    const tbody = document.getElementById('tbody-historial');
    tbody.innerHTML = '<tr><td colspan="7" class="table-empty"><span class="spinner"></span> Cargando...</td></tr>';

    const tipo     = document.getElementById('filtro-tipo-hist').value;
    const username = document.getElementById('filtro-username-hist').value.trim();
    const desde    = document.getElementById('filtro-desde-hist').value;
    const hasta    = document.getElementById('filtro-hasta-hist').value;

    let endpoint = `/historial?page=${page}&size=20`;
    if (tipo)     endpoint += `&tipo=${tipo}`;
    if (username) endpoint += `&username=${encodeURIComponent(username)}`;
    if (desde)    endpoint += `&desde=${desde}`;
    if (hasta)    endpoint += `&hasta=${hasta}`;

    const data = await API.get(endpoint);

    if (!data || data.error) {
        tbody.innerHTML = '<tr><td colspan="7" class="table-empty">Error al cargar el historial</td></tr>';
        return;
    }

    const lista = data.content || [];
    actualizarPaginacion('historial', data);

    if (lista.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="table-empty">No hay registros en el historial</td></tr>';
        return;
    }

    tbody.innerHTML = lista.map(h => {
        const badgeAccion    = getBadgeAccion(h.accion);
        const estadoAnterior = h.estadoAnterior
            ? `<span class="badge ${getBadgeEstadoClass(h.estadoAnterior)}">${sanitizar(h.estadoAnterior)}</span>`
            : '<span style="color:var(--text-secondary)">—</span>';
        const estadoNuevo = h.estadoNuevo
            ? `<span class="badge ${getBadgeEstadoClass(h.estadoNuevo)}">${sanitizar(h.estadoNuevo)}</span>`
            : '<span style="color:var(--text-secondary)">—</span>';

        return `
        <tr>
            <td style="white-space:nowrap">${formatFechaHora(h.fecha)}</td>
            <td><span class="badge-tipo">${sanitizar(h.tipo)}</span></td>
            <td style="max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap"
                title="${sanitizar(h.descripcion || '')}">${sanitizar(h.descripcion) || '—'}</td>
            <td>${badgeAccion}</td>
            <td>${estadoAnterior}</td>
            <td>${estadoNuevo}</td>
            <td style="font-weight:600">${sanitizar(h.username) || '—'}</td>
        </tr>`;
    }).join('');
}

function getBadgeAccion(accion) {
    const mapa = {
        'APROBADA':              '<span class="badge-accion badge-accion-aprobada">Aprobada</span>',
        'RECHAZADA':             '<span class="badge-accion badge-accion-rechazada">Rechazada</span>',
        'ELIMINADA':             '<span class="badge-accion badge-accion-eliminada">Eliminada</span>',
        'TERCERO_ELIMINADO':     '<span class="badge-accion badge-accion-tercero">Tercero eliminado</span>',
        'POSTULACION_RESETEADA': '<span class="badge-accion badge-accion-reseteada">Reseteada</span>'
    };
    return mapa[accion] || `<span class="badge-accion">${accion}</span>`;
}

function getBadgeEstadoClass(estado) {
    const mapa = { 'APROBADA': 'badge-aprobada', 'RECHAZADA': 'badge-rechazada', 'PENDIENTE': 'badge-pendiente' };
    return mapa[estado] || 'badge-pendiente';
}

function registrarEventosHistorial() {
    document.getElementById('btn-buscar-hist').addEventListener('click', () => cargarHistorial(0));
    document.getElementById('btn-limpiar-hist').addEventListener('click', () => {
        document.getElementById('filtro-tipo-hist').value     = '';
        document.getElementById('filtro-username-hist').value = '';
        document.getElementById('filtro-desde-hist').value    = '';
        document.getElementById('filtro-hasta-hist').value    = '';
        cargarHistorial(0);
    });
    document.getElementById('filtro-tipo-hist').addEventListener('change', () => cargarHistorial(0));
    document.getElementById('btn-prev-hist').addEventListener('click', () => {
        if (paginacion.historial.page > 0) cargarHistorial(paginacion.historial.page - 1);
    });
    document.getElementById('btn-next-hist').addEventListener('click', () => {
        if (paginacion.historial.page < paginacion.historial.totalPages - 1)
            cargarHistorial(paginacion.historial.page + 1);
    });
}

iniciarDashboard();
