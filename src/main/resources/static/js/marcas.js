const API_URL = '/productos/tabla_marcas';

let todasLasMarcas = [];


// ✅ Cargar tareas al iniciar la página
document.addEventListener('DOMContentLoaded', function() {
    cargarMarcas();
});

// ✅ Obtener todas las tareas y llenar la tabla
async function cargarMarcas() {
    try {
        const response = await fetch(API_URL);
        todasLasMarcas = await response.json();
        
        aplicarFiltros(); 
    } catch (error) {
        console.error('Error al cargar marcas:', error);
    }
}

// ✅ Función para aplicar filtros de búsqueda y estado
function aplicarFiltros() {
    const textoBusqueda = document.getElementById('search-input').value.toLowerCase().trim();
    const filtroEstado = document.getElementById('filtroEstado').value;
    
    let resultado = todasLasMarcas.filter(marca => {
        const coincideNombre = marca.nombreMarca.toLowerCase().includes(textoBusqueda);
        const coincideDescripcion = (marca.descripcion || '').toLowerCase().includes(textoBusqueda);
        return coincideNombre || coincideDescripcion;
    });
    
    resultado = resultado.filter(marca => {
        if (filtroEstado === 'true') return marca.estado === true;
        if (filtroEstado === 'false') return marca.estado === false;
        return true;
    });
    
    llenarMarcas(resultado);
}


// ✅ Llenar la tabla con los datos
function llenarMarcas(marcas) {
    const cuerpoTabla = document.getElementById('cuerpoTabla');
    const filtroValor = document.getElementById('filtroEstado').value;
    cuerpoTabla.innerHTML = ''; // Limpiar tabla
    
    marcas.forEach(marca => {
        // ✅ CONDICIÓN CORREGIDA - Manejar "todos", "activos", "inactivos"
        let debeMostrar = true;
        
        if (filtroValor === 'true') {
            debeMostrar = marca.estado === true;
        } else if (filtroValor === 'false') {
            debeMostrar = marca.estado === false;
        }
        // Si filtroValor está vacío ("") → mostrar todos
        
        if (debeMostrar) {
            const fila = document.createElement('tr');
            
            // ✅ CORREGIR: usar "estado" en lugar de "completada"
            const claseFila = marca.estado ? 'fila-activa' : 'fila-inactiva';
            fila.className = claseFila;
            
            fila.innerHTML = `
                <td data-label="Código">${marca.idMarca}</td>
                <td data-label="Marca">${marca.nombreMarca}</td>
                <td data-label="Descripción">${marca.descripcion}</td>
                <td>
                    <span class='${marca.estado ? "activo" : "inactivo"}'>
                    ${marca.estado ? '✓ Activo' : '⊗ Inactivo'}
                    </span>
                </td>
                <td data-label="Acciones">
                    <div class="acciones-group">
                        <button class="btn-editar" onclick="editarMarca(
                                ${marca.idMarca}, 
                                '${marca.nombreMarca.replace(/'/g, "\\'")}', 
                                '${(marca.descripcion || '').replace(/'/g, "\\'")}', 
                                ${marca.estado}
                            )">✏ Editar</button>
                    </div>
                </td>
            `;
            
            cuerpoTabla.appendChild(fila);
        }
    });
}

// ✅ Función para filtrar por búsqueda

// ✅ Agregar nueva tarea
async function agregarMarca() {

    const nombreMarca = document.getElementById('nombreMarca').value;
    const descripcion = document.getElementById('descripcion').value;
    const estado = document.getElementById('estado').value;
    


    const nuevaMarca = {
        nombreMarca: nombreMarca,
        descripcion: descripcion,
        estado: estado === 'true' // Convertir a booleano
    }
    
    try {
        const response = await fetch('/productos/add_marca', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(nuevaMarca)
        });
        
        if (response.ok) {
            // ✅ LIMPIAR TODOS LOS CAMPOS
            document.getElementById('nombreMarca').value = '';
            document.getElementById('descripcion').value = '';
            document.getElementById('estado').value = 'true'; // Resetear a activo
            
            // ✅ CERRAR MODAL
            document.getElementById('modalMarca').close();
            
            // ✅ RECARGAR TABLA
            await cargarMarcas();
            
            alert('Marca agregada exitosamente!');
        } else {
            const error = await response.text();
            alert('Error: ' + error);
        }
    } catch (error) {
        console.error('Error al agregar Marca:', error);
        alert('Error de conexión');
    }
}

// ✅ Función para editar una marca (por implementar)
// ✅ FUNCIÓN PARA RELLENAR EL FORMULARIO CON DATOS DE LA FILA
function editarMarca(id, nombre, descripcion, estado) {    
    // 1. RELLENAR LOS CAMPOS DEL FORMULARIO
    document.getElementById('nombreMarca').value = nombre;
    document.getElementById('descripcion').value = descripcion;
    document.getElementById('estado').value = estado.toString();
    
    // 2. GUARDAR EL ID DE LA MARCA QUE SE ESTÁ EDITANDO
    window.marcaEditandoId = id;
    
    // 3. CAMBIAR EL BOTÓN "REGISTRAR" POR "ACTUALIZAR"
    const btnRegistrar = document.querySelector('.btn-registrar');
    btnRegistrar.textContent = 'Actualizar';
    btnRegistrar.onclick = actualizarMarca; // Cambiar la función que ejecuta
    
    // 4. CAMBIAR EL TÍTULO DEL MODAL
    const tituloModal = document.querySelector('#modalMarca h2');
    if (tituloModal) {
        tituloModal.textContent = 'Editar Marca';
    }
    
    // 5. ABRIR EL MODAL
    document.getElementById('modalMarca').showModal();
}

// ✅ FUNCIÓN PARA ACTUALIZAR (cuando se edita)
async function actualizarMarca() {
    const id = window.marcaEditandoId;
    const nombreMarca = document.getElementById('nombreMarca').value;
    const descripcion = document.getElementById('descripcion').value;
    const estado = document.getElementById('estado').value === 'true';
    
    // Validar
    if (!nombreMarca.trim()) {
        alert('El nombre de la marca es requerido');
        return;
    }
    
    const marcaActualizada = {
        idMarca: parseInt(id),
        nombreMarca: nombreMarca,
        descripcion: descripcion,
        estado: estado
    }
    
    try {
        const response = await fetch('/productos/update_marca', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(marcaActualizada)
        });
        
        if (response.ok) {
            // Cerrar modal, recargar tabla y resetear formulario
            document.getElementById('modalMarca').close();
            await cargarMarcas();
            alert('Marca actualizada exitosamente!');
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
    document.getElementById('nombreMarca').value = '';
    document.getElementById('descripcion').value = '';
    document.getElementById('estado').value = 'true';
    
    // Restaurar botón "Registrar"
    const btnRegistrar = document.querySelector('.btn-registrar');
    btnRegistrar.textContent = 'Registrar';
    btnRegistrar.onclick = agregarMarca;
    
    // Restaurar título
    const tituloModal = document.querySelector('#modalMarca h2');
    if (tituloModal) {
        tituloModal.textContent = 'Registrar Marca';
    }
    
    // Limpiar ID
    window.marcaEditandoId = null;
}
