if (window.location.pathname.endsWith('/index.html')) {
    history.replaceState(null, '', './');
}

Auth.redirectIfLoggedIn();

function openModal(id) {
    document.getElementById(id).classList.add('open');
}
function closeModal(id) {
    document.getElementById(id).classList.remove('open');
}

document.querySelectorAll('[data-close]').forEach(btn => {
    btn.addEventListener('click', () => closeModal(btn.dataset.close));
});
document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
        if (e.target === overlay) closeModal(overlay.id);
    });
});

document.getElementById('btn-ingresar').addEventListener('click', () => {
    limpiarLogin();
    openModal('modal-login');
});

function limpiarLogin() {
    document.getElementById('login-username').value = '';
    document.getElementById('login-password').value = '';
    ['err-username', 'err-password'].forEach(id => {
        document.getElementById(id).classList.remove('visible');
    });
    ['login-username', 'login-password'].forEach(id => {
        document.getElementById(id).classList.remove('error');
    });
}

function validarLogin() {
    let ok = true;
    const username = document.getElementById('login-username').value.trim();
    const password = document.getElementById('login-password').value.trim();

    if (!username) {
        document.getElementById('err-username').classList.add('visible');
        document.getElementById('login-username').classList.add('error');
        ok = false;
    } else {
        document.getElementById('err-username').classList.remove('visible');
        document.getElementById('login-username').classList.remove('error');
    }
    if (!password) {
        document.getElementById('err-password').classList.add('visible');
        document.getElementById('login-password').classList.add('error');
        ok = false;
    } else {
        document.getElementById('err-password').classList.remove('visible');
        document.getElementById('login-password').classList.remove('error');
    }
    return ok;
}

document.getElementById('btn-login').addEventListener('click', async () => {
    if (!validarLogin()) return;

    const btn = document.getElementById('btn-login');
    btn.disabled = true;
    btn.textContent = 'Verificando...';

    try {
        const data = await API.publicPost('/usuarios/login', {
            username: document.getElementById('login-username').value.trim(),
            password: document.getElementById('login-password').value.trim()
        });

        if (data._ok) {
            Auth.save(data);
            Toast.success('Bienvenido, ' + data.username);
            setTimeout(() => window.location.href = 'dashboard.html', 800);
        } else {
            Toast.error(data.mensaje || 'Credenciales inválidas');
            btn.disabled = false;
            btn.textContent = 'Iniciar sesión';
        }
    } catch {
        Toast.error('Error de conexión con el servidor');
        btn.disabled = false;
        btn.textContent = 'Iniciar sesión';
    }
});

['login-username', 'login-password'].forEach(id => {
    document.getElementById(id).addEventListener('keydown', (e) => {
        if (e.key === 'Enter') document.getElementById('btn-login').click();
    });
});

const tipoLabels = {
    ESTUDIANTE:     'Postulación como Estudiante',
    PROFESOR:       'Postulación como Profesor',
    ADMINISTRATIVO: 'Postulación como Administrativo'
};

const LABEL_CATALOGO = {
    ESTUDIANTE:     'Programa académico al que aspira *',
    PROFESOR:       'Área o departamento al que aspira *',
    ADMINISTRATIVO: 'Cargo al que aspira *'
};

const PLACEHOLDER_CATALOGO = {
    ESTUDIANTE:     'Selecciona un programa académico...',
    PROFESOR:       'Selecciona un área o departamento...',
    ADMINISTRATIVO: 'Selecciona un cargo...'
};

const ENDPOINT_CATALOGO = {
    ESTUDIANTE:     '/catalogos/programas',
    PROFESOR:       '/catalogos/areas',
    ADMINISTRATIVO: '/catalogos/cargos'
};

const ID_KEY_CATALOGO = {
    ESTUDIANTE:     'programaId',
    PROFESOR:       'areaId',
    ADMINISTRATIVO: 'cargoId'
};

document.querySelectorAll('.post-card').forEach(card => {
    card.addEventListener('click', () => {
        const tipo = card.dataset.tipo;
        document.getElementById('post-tipo').value = tipo;
        document.getElementById('modal-post-titulo').textContent = tipoLabels[tipo];
        limpiarFormulario();
        cargarSelectorCatalogo(tipo);
        openModal('modal-postulacion');
    });
});

async function cargarSelectorCatalogo(tipo) {
    const select = document.getElementById('selector-catalogo');
    const label  = document.getElementById('label-selector-catalogo');

    label.textContent = LABEL_CATALOGO[tipo];
    select.innerHTML  = '<option value="">Cargando...</option>';
    select.classList.remove('error');
    document.getElementById('err-selector-catalogo').classList.remove('visible');

    try {
        const res  = await fetch(`${CONFIG.API_BASE}${ENDPOINT_CATALOGO[tipo]}?soloActivos=true`);
        const data = await res.json();

        if (!Array.isArray(data) || data.length === 0) {
            select.innerHTML = '<option value="">No hay opciones disponibles — contacta al administrador</option>';
            return;
        }

        const idKey = ID_KEY_CATALOGO[tipo];
        select.innerHTML = `<option value="">${PLACEHOLDER_CATALOGO[tipo]}</option>` +
            data.map(item =>
                `<option value="${item[idKey]}">${item.nombre}</option>`
            ).join('');

    } catch {
        select.innerHTML = '<option value="">Error al cargar opciones</option>';
    }
}

function limpiarFormulario() {
    ['post-tipo-doc', 'post-nro-doc', 'post-nombres',
     'post-apellidos', 'post-email', 'post-telefono', 'post-comentarios']
        .forEach(id => {
            const el = document.getElementById(id);
            if (el) { el.value = ''; el.classList.remove('error'); }
        });
    document.querySelectorAll('#modal-postulacion .form-error')
        .forEach(el => el.classList.remove('visible'));
    const selectCatalogo = document.getElementById('selector-catalogo');
    if (selectCatalogo) {
        selectCatalogo.value = '';
        selectCatalogo.classList.remove('error');
    }
    document.getElementById('err-selector-catalogo')?.classList.remove('visible');
}

function validarFormulario() {
    let ok = true;

    const campos = [
        { id: 'post-tipo-doc',  errId: 'err-tipo-doc' },
        { id: 'post-nro-doc',   errId: 'err-nro-doc' },
        { id: 'post-nombres',   errId: 'err-nombres' },
        { id: 'post-apellidos', errId: 'err-apellidos' },
        { id: 'post-email',     errId: 'err-email' }
    ];

    campos.forEach(({ id, errId }) => {
        const el    = document.getElementById(id);
        const errEl = document.getElementById(errId);
        if (!el.value.trim()) {
            errEl.classList.add('visible');
            el.classList.add('error');
            ok = false;
        } else {
            errEl.classList.remove('visible');
            el.classList.remove('error');
        }
    });

    const emailEl    = document.getElementById('post-email');
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (emailEl.value.trim() && !emailRegex.test(emailEl.value.trim())) {
        document.getElementById('err-email').classList.add('visible');
        emailEl.classList.add('error');
        ok = false;
    }

    const selectCat = document.getElementById('selector-catalogo');
    if (!selectCat.value) {
        document.getElementById('err-selector-catalogo').classList.add('visible');
        selectCat.classList.add('error');
        ok = false;
    } else {
        document.getElementById('err-selector-catalogo').classList.remove('visible');
        selectCat.classList.remove('error');
    }

    return ok;
}

document.getElementById('btn-postular').addEventListener('click', async () => {
    if (!validarFormulario()) return;

    const btn = document.getElementById('btn-postular');
    btn.disabled    = true;
    btn.textContent = 'Enviando...';

    const tipo       = document.getElementById('post-tipo').value;
    const catalogoId = document.getElementById('selector-catalogo').value;

    const body = {
        tipoDoc:         document.getElementById('post-tipo-doc').value,
        nroDoc:          document.getElementById('post-nro-doc').value.trim(),
        nombres:         document.getElementById('post-nombres').value.trim().toUpperCase(),
        apellidos:       document.getElementById('post-apellidos').value.trim().toUpperCase(),
        email:           document.getElementById('post-email').value.trim().toLowerCase(),
        telefono:        document.getElementById('post-telefono').value.trim(),
        comentarios:     document.getElementById('post-comentarios').value.trim(),
        tipoPostulacion: tipo
    };

    if (tipo === 'ESTUDIANTE')     body.programaAcademicoId = parseInt(catalogoId);
    if (tipo === 'PROFESOR')       body.areaDepartamentoId  = parseInt(catalogoId);
    if (tipo === 'ADMINISTRATIVO') body.cargoId             = parseInt(catalogoId);

    try {
        const data = await API.publicPost('/postulaciones', body);

        if (data._status === 201 || data._ok) {
            Toast.success('¡Postulación enviada exitosamente! Pronto recibirás respuesta.');
            closeModal('modal-postulacion');
            limpiarFormulario();
        } else if (data._status === 409) {
            Toast.error('Ya tienes una postulación activa para este tipo. No puedes postularte dos veces al mismo cargo.');
        } else if (data._status === 400 && data.errores) {
            const mapaErrores = {
                nombres:         'err-nombres',
                apellidos:       'err-apellidos',
                tipoDoc:         'err-tipo-doc',
                nroDoc:          'err-nro-doc',
                email:           'err-email',
                tipoPostulacion: null
            };
            Object.entries(data.errores).forEach(([campo, mensaje]) => {
                const errId = mapaErrores[campo];
                if (errId) {
                    const errEl = document.getElementById(errId);
                    if (errEl) {
                        errEl.textContent = mensaje;
                        errEl.classList.add('visible');
                    }
                }
            });
            Toast.error('Revisa los campos marcados en rojo');
        } else {
            Toast.error(data.mensaje || 'Error al enviar la postulación. Intenta nuevamente.');
        }
    } catch {
        Toast.error('Error de conexión con el servidor');
    } finally {
        btn.disabled    = false;
        btn.textContent = 'Enviar postulación';
    }
});
