let todasLasPresentaciones = [];


// ✅ Cargar tareas al iniciar la página
document.addEventListener('DOMContentLoaded', function() {
    cargarPresentaciones();
});

// ✅ Obtener todas las tareas y llenar la tabla
async function cargarPresentaciones() {
    try {
        const response = await fetch('/productos/tabla_presentaciones');
        todasLasPresentaciones = await response.json();
        
        aplicarFiltros(); 
    } catch (error) {
        console.error('Error al cargar presentaciones:', error);
    }
}

// ✅ Función para aplicar filtros de búsqueda y estado
function aplicarFiltros() {
    const textoBusqueda = document.getElementById('search-input').value.toLowerCase().trim();
    const filtroEstado = document.getElementById('filtroEstado').value;
    
    let resultado = todasLasPresentaciones.filter(presentacion => {
        const coincideNombre = presentacion.nombre.toLowerCase().includes(textoBusqueda);
        return coincideNombre;
    });
    
    resultado = resultado.filter(presentacion => {
        if (filtroEstado === 'true') return presentacion.estado === true;
        if (filtroEstado === 'false') return presentacion.estado === false;
        return true;
    });
    
    llenarPresentaciones(resultado);
}


// ✅ Llenar la tabla con los datos
function llenarPresentaciones(presentaciones) {
    const cuerpoTabla = document.getElementById('cuerpoTabla');
    const filtroValor = document.getElementById('filtroEstado').value;
    cuerpoTabla.innerHTML = ''; // Limpiar tabla
    
    presentaciones.forEach(presentacion => {
        // ✅ CONDICIÓN CORREGIDA - Manejar "todos", "activos", "inactivos"
        let debeMostrar = true;
        
        if (filtroValor === 'true') {
            debeMostrar = presentacion.estado === true;
        } else if (filtroValor === 'false') {
            debeMostrar = presentacion.estado === false;
        }
        // Si filtroValor está vacío ("") → mostrar todos
        
        if (debeMostrar) {
            const fila = document.createElement('tr');
            
            // ✅ CORREGIR: usar "estado" en lugar de "completada"
            const claseFila = presentacion.estado ? 'fila-activa' : 'fila-inactiva';
            fila.className = claseFila;
            
            fila.innerHTML = `
                <td data-label="Código">${presentacion.id}</td>
                <td data-label="Presentacion">${presentacion.nombre}</td>
                <td>
                    <span class='${presentacion.estado ? "activo" : "inactivo"}'>
                    ${presentacion.estado ? '✓ Activo' : '⊗ Inactivo'}
                    </span>
                </td>
                <td data-label="Acciones">
                    <div class="acciones-group">
                        <button class="btn-editar" onclick="editarPresentacion(
                                ${presentacion.id}, 
                                '${presentacion.nombre.replace(/'/g, "\\'")}', 
                                ${presentacion.estado}
                            )">✏ Editar</button>
                    </div>
                </td>
            `;
            
            cuerpoTabla.appendChild(fila);
        }
    });
}


// ✅ Agregar nueva tarea
async function agregarPresentacion() {

    const nombrePresentacion = document.getElementById('nombrePresentacion').value;
    const estado = document.getElementById('estado').value;
    
    const nuevaPresentacion = {
        nombre: nombrePresentacion,
        estado: estado === "true" // Convertir a booleano
    }
    
    try {
        const response = await fetch('/productos/add_presentacion', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(nuevaPresentacion)
        });
        
        if (response.ok) {
            // ✅ LIMPIAR TODOS LOS CAMPOS
            document.getElementById('nombrePresentacion').value = '';
            document.getElementById('estado').value = 'true'; // Resetear a activo
            
            // ✅ CERRAR MODAL
            document.getElementById('modalPresentacion').close();
            
            // ✅ RECARGAR TABLA
            await cargarPresentaciones();
            
            alert('Presentación agregada exitosamente!');
        } else {
            const error = await response.text();
            alert('Error: ' + error);
        }
    } catch (error) {
        console.error('Error al agregar Presentación:', error);
        alert('Error de conexión');
    }
}

// ✅ Función para editar una marca (por implementar)
// ✅ FUNCIÓN PARA RELLENAR EL FORMULARIO CON DATOS DE LA FILA
function editarPresentacion(id, nombre, estado) {    
    // 1. RELLENAR LOS CAMPOS DEL FORMULARIO
    document.getElementById('nombrePresentacion').value = nombre;
    document.getElementById('estado').value = estado.toString();
    
    // 2. GUARDAR EL ID DE LA MARCA QUE SE ESTÁ EDITANDO
    window.presentacionEditandoId = id;
    
    // 3. CAMBIAR EL BOTÓN "REGISTRAR" POR "ACTUALIZAR"
    const btnRegistrar = document.querySelector('.btn-registrar');
    btnRegistrar.textContent = 'Actualizar';
    btnRegistrar.onclick = actualizarPresentacion; // Cambiar la función que ejecuta
    
    // 4. CAMBIAR EL TÍTULO DEL MODAL
    const tituloModal = document.querySelector('#modalPresentacion h2');
    if (tituloModal) {
        tituloModal.textContent = 'Editar Presentación';
    }
    
    // 5. ABRIR EL MODAL
    document.getElementById('modalPresentacion').showModal();
}

// ✅ FUNCIÓN PARA ACTUALIZAR (cuando se edita)
async function actualizarPresentacion() {
    const id = window.presentacionEditandoId;
    const nombrePresentacion = document.getElementById('nombrePresentacion').value;
    const estado = document.getElementById('estado').value === 'true';
    
    // Validar
    if (!nombrePresentacion.trim()) {
        alert('El nombre de la presentacion es requerido');
        return;
    }
    
    const presentacionActualizada = {
        id: parseInt(id),
        nombre: nombrePresentacion,
        estado: estado
    }
    
    try {
        const response = await fetch('/productos/update_presentacion', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(presentacionActualizada)
        });
        
        if (response.ok) {
            // Cerrar modal, recargar tabla y resetear formulario
            document.getElementById('modalPresentacion').close();
            await cargarPresentaciones();
            alert('Presentación actualizada exitosamente!');
            resetearFormulario();
        } else {
            const error = await response.text();
            alert('Error: ' + error);
        }
    } catch (error) {
        console.error('Error al actualizar marca:', error);
        alert('Error de conexión');
    }
}

// ✅ FUNCIÓN PARA RESETEAR FORMULARIO A MODO "REGISTRAR"
function resetearFormulario() {
    // Limpiar campos
    document.getElementById('nombrePresentacion').value = '';
    document.getElementById('estado').value = 'true';
    
    // Restaurar botón "Registrar"
    const btnRegistrar = document.querySelector('.btn-registrar');
    btnRegistrar.textContent = 'Registrar';
    btnRegistrar.onclick = agregarPresentacion;
    
    // Restaurar título
    const tituloModal = document.querySelector('#modalPresentacion h2');
    if (tituloModal) {
        tituloModal.textContent = 'Registrar Presentación';
    }
    
    // Limpiar ID
    window.presentacionEditandoId = null;
}
