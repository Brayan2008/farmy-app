// Reportes - Funcionalidades JavaScript
const Reportes = (function() {
    // Variables privadas
    let modalReporte;
    let modalConfirmacion;
    let reporteAEliminar = null;
    let accionConfirmacion = null;
    
    // Inicialización
    function init() {
        console.log('Sistema de Reportes inicializado');
        
        // Referencias a elementos DOM
        modalReporte = document.getElementById('modalReporte');
        modalConfirmacion = document.getElementById('modalConfirmacion');
        
        // Inicializar fechas
        inicializarFechas();
        
        // Configurar formulario
        configurarFormulario();
        
        // Configurar secciones plegables
        configurarSeccionesPlegables();
        
        // Calcular estadísticas iniciales
        calcularEstadisticas();
        
        // Configurar eventos
        configurarEventos();
    }
    
    // Configurar formulario
    function configurarFormulario() {
        const form = document.getElementById('formNuevoReporte');
        if (form) {
            // Prevenir envío por defecto
            form.onsubmit = function(e) {
                e.preventDefault();
                generarReporte();
            };
            
            // Configurar tipo de reporte inicial
            cambiarTipoReporte();
        }
    }
    
    // Inicializar fechas en el formulario
    function inicializarFechas() {
        const hoy = new Date();
        const inicioMes = new Date(hoy.getFullYear(), hoy.getMonth() - 1, 1);
        
        const fechaDesde = document.getElementById('fechaDesde');
        const fechaHasta = document.getElementById('fechaHasta');
        
        if (fechaDesde && !fechaDesde.value) {
            fechaDesde.value = formatoFechaInput(inicioMes);
        }
        
        if (fechaHasta && !fechaHasta.value) {
            fechaHasta.value = formatoFechaInput(hoy);
        }
    }
    
    // Formatear fecha para input type="date"
    function formatoFechaInput(fecha) {
        const year = fecha.getFullYear();
        const month = String(fecha.getMonth() + 1).padStart(2, '0');
        const day = String(fecha.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }
    
    // Configurar secciones plegables
    function configurarSeccionesPlegables() {
        const secciones = document.querySelectorAll('.section-content');
        secciones.forEach(seccion => {
            seccion.classList.add('hidden');
        });
    }
    
    // Mostrar modal de nuevo reporte
    function mostrarModalNuevoReporte() {
        if (modalReporte) {
            modalReporte.style.display = 'flex';
            document.body.style.overflow = 'hidden';
            
            // Resetear formulario
            const form = document.getElementById('formNuevoReporte');
            if (form) {
                form.reset();
                inicializarFechas();
                cambiarTipoReporte();
            }
        }
    }
    
    // Cerrar modal de nuevo reporte
    function cerrarModalNuevoReporte() {
        if (modalReporte) {
            modalReporte.style.display = 'none';
            document.body.style.overflow = 'auto';
        }
    }
    
    // Toggle secciones plegables
    function toggleSection(boton) {
        const seccion = boton.closest('.form-section');
        const contenido = seccion.querySelector('.section-content');
        const esVisible = !contenido.classList.contains('hidden');
        
        if (esVisible) {
            contenido.classList.add('hidden');
            boton.classList.remove('rotate');
        } else {
            contenido.classList.remove('hidden');
            boton.classList.add('rotate');
        }
    }
    
    // Cambiar período automáticamente
    function cambiarPeriodo() {
        const periodo = document.getElementById('periodo').value;
        const fechaDesde = document.getElementById('fechaDesde');
        const fechaHasta = document.getElementById('fechaHasta');
        const hoy = new Date();
        
        switch(periodo) {
            case 'hoy':
                fechaDesde.value = formatoFechaInput(hoy);
                fechaHasta.value = formatoFechaInput(hoy);
                break;
                
            case 'ayer':
                const ayer = new Date(hoy);
                ayer.setDate(hoy.getDate() - 1);
                fechaDesde.value = formatoFechaInput(ayer);
                fechaHasta.value = formatoFechaInput(ayer);
                break;
                
            case 'semana-actual':
                const inicioSemana = new Date(hoy);
                inicioSemana.setDate(hoy.getDate() - hoy.getDay());
                fechaDesde.value = formatoFechaInput(inicioSemana);
                fechaHasta.value = formatoFechaInput(hoy);
                break;
                
            case 'mes-actual':
                const inicioMes = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
                fechaDesde.value = formatoFechaInput(inicioMes);
                fechaHasta.value = formatoFechaInput(hoy);
                break;
                
            case 'trimestre':
                const trimestre = Math.floor(hoy.getMonth() / 3);
                const inicioTrimestre = new Date(hoy.getFullYear(), trimestre * 3, 1);
                fechaDesde.value = formatoFechaInput(inicioTrimestre);
                fechaHasta.value = formatoFechaInput(hoy);
                break;
                
            case 'anio':
                const inicioAnio = new Date(hoy.getFullYear(), 0, 1);
                fechaDesde.value = formatoFechaInput(inicioAnio);
                fechaHasta.value = formatoFechaInput(hoy);
                break;
                
            default:
                // Personalizado - no hacer nada
                break;
        }
    }
    
        // Cambiar texto del botón según tipo de reporte
    function cambiarTipoReporte() {
        const tipo = document.getElementById('tipoReporte');
        const botonGenerar = document.querySelector('.btn-registrar');
        
        if (tipo && botonGenerar) {
            if (tipo.value === 'ventas') {
                botonGenerar.textContent = 'Continuar a Ventas';
                botonGenerar.title = 'Ir a la página de filtros de ventas para personalizar el reporte';
            } else if (tipo.value === 'compras') {
                botonGenerar.textContent = 'Continuar a Compras';
                botonGenerar.title = 'Ir a la página de filtros de compras para personalizar el reporte';
            } else {
                botonGenerar.textContent = 'Generar Reporte';
                botonGenerar.title = 'Generar reporte directamente';
            }
        }
    }
    
    // Generar reporte
    function generarReporte() {
        const tipoReporte = document.getElementById('tipoReporte').value;
        const form = document.getElementById('formNuevoReporte');
        
        // Validar formulario
        if (!validarFormulario()) {
            return;
        }
        
        // Si es tipo ventas, redirigir a reporte de ventas
        if (tipoReporte === 'ventas') {
            // Crear URL con parámetros
            const formData = new FormData(form);
            const params = new URLSearchParams();
            
            // Agregar todos los parámetros
            for (let [key, value] of formData.entries()) {
                params.append(key, value);
            }
            
            // Agregar flag para identificar que viene del modal
            params.append('fromModal', 'true');
            
            // Redirigir a ventas/reportes
            window.location.href = '/ventas/reportes?' + params.toString();
        } 
        // Si es tipo compras, redirigir a reporte de compras
        else if (tipoReporte === 'compras') {
            // Crear URL con parámetros
            const formData = new FormData(form);
            const params = new URLSearchParams();
            
            // Agregar todos los parámetros
            for (let [key, value] of formData.entries()) {
                params.append(key, value);
            }
            
            // Agregar flag para identificar que viene del modal
            params.append('fromModal', 'true');
            
            // Redirigir a compras/reportes
            window.location.href = '/compras/reportes?' + params.toString();
        }
        else {
            // Para otros tipos, enviar formulario normalmente
            form.action = '/reportes/guardar';
            form.submit();
        }
    }
    
    // Validar formulario
    function validarFormulario() {
        const nombre = document.getElementById('nombreReporte').value;
        const tipo = document.getElementById('tipoReporte').value;
        const formato = document.getElementById('formato').value;
        const fechaDesde = document.getElementById('fechaDesde').value;
        const fechaHasta = document.getElementById('fechaHasta').value;
        
        if (!nombre || !tipo || !formato || !fechaDesde || !fechaHasta) {
            mostrarMensajeError('Por favor complete todos los campos requeridos (*)');
            return false;
        }
        
        // Validar que fecha desde no sea mayor que fecha hasta
        const fechaDesdeObj = new Date(fechaDesde);
        const fechaHastaObj = new Date(fechaHasta);
        
        if (fechaDesdeObj > fechaHastaObj) {
            mostrarMensajeError('La fecha "Desde" no puede ser mayor que la fecha "Hasta"');
            return false;
        }
        
        return true;
    }
    
    // Filtrar reportes por tipo y estado
    function filtrarReportes() {
        const tipo = document.getElementById('filtroTipo').value;
        const estado = document.getElementById('filtroEstado').value;
        const filas = document.querySelectorAll('#tablaReportes .reporte-fila');
        
        let totalGenerados = 0;
        let totalProceso = 0;
        let totalErrores = 0;
        let visibleCount = 0;
        
        filas.forEach(fila => {
            const tipoFila = fila.getAttribute('data-tipo');
            const estadoFila = fila.getAttribute('data-estado');
            
            // Contar estadísticas
            if (estadoFila === 'generado') totalGenerados++;
            else if (estadoFila === 'proceso') totalProceso++;
            else if (estadoFila === 'error') totalErrores++;
            
            // Aplicar filtros
            const mostrarTipo = !tipo || tipo === tipoFila;
            const mostrarEstado = !estado || estado === estadoFila;
            
            if (mostrarTipo && mostrarEstado) {
                fila.style.display = '';
                visibleCount++;
            } else {
                fila.style.display = 'none';
            }
        });
        
        // Actualizar estadísticas
        actualizarEstadisticas(totalGenerados, totalProceso, totalErrores);
        
        // Mostrar mensaje si no hay resultados
        mostrarMensajeSinResultados(visibleCount);
    }
    
    // Actualizar estadísticas en la interfaz
    function actualizarEstadisticas(generados, proceso, errores) {
        const totalGeneradosEl = document.getElementById('totalGenerados');
        const totalProcesoEl = document.getElementById('totalProceso');
        const totalErroresEl = document.getElementById('totalErrores');
        
        if (totalGeneradosEl) totalGeneradosEl.textContent = generados;
        if (totalProcesoEl) totalProcesoEl.textContent = proceso;
        if (totalErroresEl) totalErroresEl.textContent = errores;
    }
    
    // Calcular estadísticas iniciales
    function calcularEstadisticas() {
        const filas = document.querySelectorAll('#tablaReportes .reporte-fila');
        let totalGenerados = 0;
        let totalProceso = 0;
        let totalErrores = 0;
        
        filas.forEach(fila => {
            const estadoFila = fila.getAttribute('data-estado');
            if (estadoFila === 'generado') totalGenerados++;
            else if (estadoFila === 'proceso') totalProceso++;
            else if (estadoFila === 'error') totalErrores++;
        });
        
        actualizarEstadisticas(totalGenerados, totalProceso, totalErrores);
    }
    
    // Mostrar mensaje cuando no hay resultados
    function mostrarMensajeSinResultados(visibleCount) {
        let mensaje = document.getElementById('mensajeSinResultados');
        
        if (visibleCount === 0) {
            if (!mensaje) {
                mensaje = document.createElement('tr');
                mensaje.id = 'mensajeSinResultados';
                mensaje.innerHTML = `
                    <td colspan="7" class="sin-resultados">
                        <div style="text-align: center; padding: 40px 20px;">
                            <div style="font-size: 48px; margin-bottom: 20px;">📊</div>
                            <h3 style="color: #6c757d; margin-bottom: 10px;">No se encontraron reportes</h3>
                            <p style="color: #9ca3af;">Intente con otros filtros o cree un nuevo reporte</p>
                        </div>
                    </td>
                `;
                document.querySelector('#cuerpoTablaReportes').appendChild(mensaje);
            }
        } else if (mensaje) {
            mensaje.remove();
        }
    }
    
    // Editar reporte
    function editarReporte(codigo) {
        // Aquí se implementaría la lógica para cargar los datos del reporte en el modal
        alert(`Editar reporte ${codigo} - Esta funcionalidad está en desarrollo`);
    }
    
    // Eliminar reporte
    function eliminarReporte(codigo) {
        reporteAEliminar = codigo;
        accionConfirmacion = 'eliminar';
        
        mostrarModalConfirmacion(
            `¿Está seguro de eliminar el reporte ${codigo}?`,
            confirmarEliminacion
        );
    }
    
    // Mostrar modal de confirmación
    function mostrarModalConfirmacion(mensaje, callback) {
        const mensajeElement = document.getElementById('mensajeConfirmacion');
        const botonConfirmar = document.getElementById('btnConfirmarAccion');
        
        if (mensajeElement && botonConfirmar && modalConfirmacion) {
            mensajeElement.textContent = mensaje;
            botonConfirmar.onclick = callback;
            modalConfirmacion.style.display = 'flex';
            document.body.style.overflow = 'hidden';
        }
    }
    
    // Cerrar modal de confirmación
    function cerrarModalConfirmacion() {
        if (modalConfirmacion) {
            modalConfirmacion.style.display = 'none';
            document.body.style.overflow = 'auto';
            reporteAEliminar = null;
            accionConfirmacion = null;
        }
    }
    
    // Confirmar eliminación
    function confirmarEliminacion() {
        if (reporteAEliminar && accionConfirmacion === 'eliminar') {
            // Llamada AJAX real para eliminar
            fetch(`/reportes/eliminar/${reporteAEliminar}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': getCsrfToken()
                }
            })
            .then(response => {
                if (response.ok) {
                    mostrarMensajeExito(`Reporte ${reporteAEliminar} eliminado exitosamente`);
                    // Recargar después de 1.5 segundos
                    setTimeout(() => {
                        location.reload();
                    }, 1500);
                } else {
                    throw new Error('Error al eliminar el reporte');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                mostrarMensajeError('Error al eliminar el reporte');
            });
        }
        
        cerrarModalConfirmacion();
    }
    
    // Obtener token CSRF
    function getCsrfToken() {
        return document.querySelector('meta[name="_csrf"]')?.content ||
               document.querySelector('input[name="_csrf"]')?.value;
    }
    
    // Mostrar mensaje de éxito
    function mostrarMensajeExito(mensaje) {
        crearMensaje(mensaje, '#10b981');
    }
    
    // Mostrar mensaje de error
    function mostrarMensajeError(mensaje) {
        crearMensaje(mensaje, '#ef4444');
    }
    
    // Crear mensaje flotante
    function crearMensaje(mensaje, color) {
        // Remover mensajes anteriores
        const mensajesPrevios = document.querySelectorAll('.mensaje-flotante');
        mensajesPrevios.forEach(msg => msg.remove());
        
        const alerta = document.createElement('div');
        alerta.className = 'mensaje-flotante';
        alerta.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: ${color};
            color: white;
            padding: 15px 20px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            z-index: 10000;
            animation: slideInRight 0.3s ease;
            font-family: system-ui, -apple-system, sans-serif;
        `;
        alerta.textContent = mensaje;
        document.body.appendChild(alerta);
        
        setTimeout(() => {
            alerta.style.animation = 'slideOutRight 0.3s ease';
            setTimeout(() => alerta.remove(), 300);
        }, 3000);
    }
    
    // Configurar eventos
    function configurarEventos() {
        // Cerrar modal al hacer clic fuera
        window.addEventListener('click', (event) => {
            if (event.target === modalReporte) {
                cerrarModalNuevoReporte();
            }
            if (event.target === modalConfirmacion) {
                cerrarModalConfirmacion();
            }
        });
        
        // Cerrar modal con Escape
        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape') {
                if (modalReporte && modalReporte.style.display === 'flex') {
                    cerrarModalNuevoReporte();
                }
                if (modalConfirmacion && modalConfirmacion.style.display === 'flex') {
                    cerrarModalConfirmacion();
                }
            }
        });
        
        // Escuchar cambios en tipo de reporte
        const tipoReporteSelect = document.getElementById('tipoReporte');
        if (tipoReporteSelect) {
            tipoReporteSelect.addEventListener('change', cambiarTipoReporte);
        }
    }
    
    // Exportar funciones públicas
    return {
        init: init,
        mostrarModalNuevoReporte: mostrarModalNuevoReporte,
        cerrarModalNuevoReporte: cerrarModalNuevoReporte,
        toggleSection: toggleSection,
        cambiarPeriodo: cambiarPeriodo,
        cambiarTipoReporte: cambiarTipoReporte,
        generarReporte: generarReporte,
        filtrarReportes: filtrarReportes,
        editarReporte: editarReporte,
        eliminarReporte: eliminarReporte,
        cerrarModalConfirmacion: cerrarModalConfirmacion,
        validarFormulario: validarFormulario
    };
})();

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    Reportes.init();
    
    // Agregar estilos CSS para animaciones
    const style = document.createElement('style');
    style.textContent = `
        @keyframes slideInRight {
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }
        
        @keyframes slideOutRight {
            from {
                transform: translateX(0);
                opacity: 1;
            }
            to {
                transform: translateX(100%);
                opacity: 0;
            }
        }
        
        .mensaje-flotante {
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 15px 20px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            z-index: 10000;
            animation: slideInRight 0.3s ease;
            font-family: system-ui, -apple-system, sans-serif;
        }
        
        .hidden {
            display: none !important;
        }
        
        .rotate {
            transform: rotate(180deg);
        }
    `;
    document.head.appendChild(style);
});

// Exportar para uso global
window.Reportes = Reportes;