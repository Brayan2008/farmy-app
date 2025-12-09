// Reporte de Compras - Versión Simplificada y Funcional
const ReporteCompras = (function() {
    // Variables
    let isGenerating = false;
    
    // Inicialización
    function init() {
        console.log('Reporte de Compras inicializado');
        configurarFechasPorDefecto();
        configurarEventos();
        consoleValoresIniciales();

        const reporteIdElement = document.getElementById('reporteIdHidden');
        if (reporteIdElement) {
            console.log('Reporte ID disponible:', reporteIdElement.value);
        }
    }
    
    // Mostrar valores iniciales en consola para depuración
    function consoleValoresIniciales() {
        console.log('Valores iniciales:');
        console.log('- Nombre:', document.getElementById('nombreReporteHidden')?.value);
        console.log('- Formato:', document.getElementById('formatoHidden')?.value);
        console.log('- Descripción:', document.getElementById('descripcionHidden')?.value);
    }
    
    // Configurar fechas por defecto
    function configurarFechasPorDefecto() {
        const fechaInicio = document.querySelector('input[name="fechaInicio"]');
        const fechaFin = document.querySelector('input[name="fechaFin"]');
        const hoy = new Date();
        const hace30Dias = new Date();
        hace30Dias.setDate(hoy.getDate() - 30);
        
        if (fechaInicio && !fechaInicio.value) {
            fechaInicio.value = formatDate(hace30Dias);
        }
        
        if (fechaFin && !fechaFin.value) {
            fechaFin.value = formatDate(hoy);
        }
        
        const horaInicio = document.querySelector('input[name="horaInicio"]');
        const horaFin = document.querySelector('input[name="horaFin"]');
        
        if (horaInicio && !horaInicio.value) horaInicio.value = '00:00';
        if (horaFin && !horaFin.value) horaFin.value = '23:59';
    }
    
    // Formatear fecha
    function formatDate(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }
    
    // Configurar eventos
    function configurarEventos() {
        // Botón aplicar filtros
        const btnFiltro = document.querySelector('.btn-filtro');
        if (btnFiltro) {
            btnFiltro.addEventListener('click', aplicarFiltros);
        }
        
        // Botón eliminar filtros
        const btnEliminar = document.querySelector('.btn-eliminar-filtros');
        if (btnEliminar) {
            btnEliminar.addEventListener('click', eliminarFiltros);
        }
        
        // Botón generar reporte
        const btnGenerar = document.querySelector('.btn-generar');
        if (btnGenerar) {
            btnGenerar.addEventListener('click', generarReporte);
        }
        
        // Botón cancelar
        const btnCancelar = document.querySelector('.btn-cancelar');
        if (btnCancelar) {
            btnCancelar.addEventListener('click', cancelarReporte);
        }
        
        // Botón exportar
        const btnExportar = document.querySelector('button[onclick*="exportarDatos"]');
        if (btnExportar) {
            btnExportar.addEventListener('click', exportarDatos);
        }
    }
    
    // Aplicar filtros
    function aplicarFiltros() {
        // Validar fechas
        const fechaInicio = document.querySelector('input[name="fechaInicio"]');
        const fechaFin = document.querySelector('input[name="fechaFin"]');
        
        if (!fechaInicio.value || !fechaFin.value) {
            alert('Las fechas de inicio y fin son requeridas');
            return;
        }
        
        if (new Date(fechaInicio.value) > new Date(fechaFin.value)) {
            alert('La fecha de inicio no puede ser mayor a la fecha de fin');
            return;
        }
        
        // Enviar formulario de filtros (GET)
        document.getElementById('filtroForm').submit();
    }
    
    // Eliminar filtros
    function eliminarFiltros() {
        if (!confirm('¿Está seguro de eliminar todos los filtros?')) return;
        
        // Resetear valores
        document.querySelectorAll('.filtro-input').forEach(input => {
            if (input.type === 'text' || input.type === 'number') {
                input.value = '';
            }
        });
        
        // Checkboxes
        document.getElementById('tipo-contado').checked = true;
        document.getElementById('tipo-credito').checked = true;
        
        // Selects
        document.querySelector('select[name="estadoCompra"]').value = 'todos';
        document.querySelector('select[name="ordenarPor"]').value = 'fecha';
        document.querySelector('select[name="orden"]').value = 'desc';
        
        // Fechas
        const hoy = new Date();
        const hace30Dias = new Date();
        hace30Dias.setDate(hoy.getDate() - 30);
        
        document.querySelector('input[name="fechaInicio"]').value = formatDate(hace30Dias);
        document.querySelector('input[name="horaInicio"]').value = '00:00';
        document.querySelector('input[name="fechaFin"]').value = formatDate(hoy);
        document.querySelector('input[name="horaFin"]').value = '23:59';
        
        // Aplicar
        aplicarFiltros();
    }
    
    // En la función generarReporte():
    function generarReporte() {
        if (isGenerating) return;
        
        // Validar
        const fechaInicio = document.querySelector('input[name="fechaInicio"]');
        const fechaFin = document.querySelector('input[name="fechaFin"]');
        
        if (!fechaInicio.value || !fechaFin.value) {
            alert('Las fechas de inicio y fin son requeridas');
            return;
        }
        
        if (new Date(fechaInicio.value) > new Date(fechaFin.value)) {
            alert('La fecha de inicio no puede ser mayor a la fecha de fin');
            return;
        }
        
        // Obtener valores de los campos ocultos (IMPORTANTE)
        const nombreHidden = document.getElementById('nombreReporteHidden');
        const formatoHidden = document.getElementById('formatoHidden');
        const descripcionHidden = document.getElementById('descripcionHidden');
        const reporteIdHidden = document.getElementById('reporteIdHidden');
        
        const nombre = nombreHidden?.value || 'Reporte de Compras';
        const formato = formatoHidden?.value || 'pdf';
        const descripcion = descripcionHidden?.value || '';
        const reporteId = reporteIdHidden?.value || '';
        
        console.log('=== DATOS A ENVIAR ===');
        console.log('Nombre:', nombre);
        console.log('Formato:', formato);
        console.log('Descripción:', descripcion);
        console.log('Reporte ID:', reporteId);
        console.log('Fecha inicio:', fechaInicio.value);
        console.log('Fecha fin:', fechaFin.value);
        
        // Obtener total de registros
        const totalRegistrosElement = document.getElementById('totalRegistros');
        let totalRegistros = '0';
        
        if (totalRegistrosElement) {
            const text = totalRegistrosElement.textContent.trim();
            totalRegistros = text && !isNaN(text) ? text : '0';
        }
        
        // Confirmar con información clara
        const mensajeConfirmacion = `¿${reporteId ? 'Actualizar' : 'Generar'} reporte?\n\n` +
                                `Nombre: ${nombre}\n` +
                                `Formato: ${formato}\n` +
                                `Registros: ${totalRegistros}\n` +
                                `Fechas: ${fechaInicio.value} a ${fechaFin.value}` +
                                (reporteId ? `\n\n(Actualizando reporte ID: ${reporteId})` : '');
        
        if (!confirm(mensajeConfirmacion)) return;
        
        isGenerating = true;
        mostrarLoading(true);
        
        // Llenar formulario oculto CON TODOS LOS DATOS
        document.getElementById('hiddenNombreReporte').value = nombre;
        document.getElementById('hiddenFormato').value = formato;
        document.getElementById('hiddenDescripcion').value = descripcion;
        document.getElementById('hiddenReporteId').value = reporteId; // ← CRÍTICO
        document.getElementById('hiddenFechaInicio').value = fechaInicio.value;
        document.getElementById('hiddenHoraInicio').value = document.querySelector('input[name="horaInicio"]')?.value || '00:00';
        document.getElementById('hiddenFechaFin').value = fechaFin.value;
        document.getElementById('hiddenHoraFin').value = document.querySelector('input[name="horaFin"]')?.value || '23:59';
        document.getElementById('hiddenTotalRegistros').value = totalRegistros;
        
        // Enviar formulario después de 500ms
        setTimeout(() => {
            console.log('Enviando formulario... Reporte ID:', reporteId);
            document.getElementById('formGenerarReporte').submit();
        }, 500);
    }
    
    // Cancelar reporte
    function cancelarReporte() {
        if (confirm('¿Cancelar generación de reporte?')) {
            window.location.href = '/reportes';
        }
    }
    
    // Exportar datos
    function exportarDatos() {
        const tabla = document.getElementById('tablaCompras');
        if (!tabla) {
            alert('No hay tabla disponible');
            return;
        }
        
        let csv = '';
        const rows = tabla.querySelectorAll('tr');
        
        rows.forEach(row => {
            const rowData = [];
            row.querySelectorAll('th, td').forEach(cell => {
                rowData.push(`"${cell.textContent.trim().replace(/"/g, '""')}"`);
            });
            csv += rowData.join(',') + '\n';
        });
        
        const blob = new Blob([csv], { type: 'text/csv' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'reporte_compras.csv';
        a.click();
        window.URL.revokeObjectURL(url);
        
        alert('Datos exportados exitosamente');
    }
    
    // Mostrar loading
    function mostrarLoading(mostrar) {
        const btnGenerar = document.querySelector('.btn-generar');
        const texto = document.getElementById('btnGenerarTexto');
        const loading = document.getElementById('btnGenerarLoading');
        
        if (btnGenerar && texto && loading) {
            if (mostrar) {
                btnGenerar.disabled = true;
                texto.classList.add('hidden');
                loading.classList.remove('hidden');
            } else {
                btnGenerar.disabled = false;
                texto.classList.remove('hidden');
                loading.classList.add('hidden');
                isGenerating = false;
            }
        }
    }
    
    // API pública
    return {
        init: init,
        aplicarFiltros: aplicarFiltros,
        eliminarFiltros: eliminarFiltros,
        generarReporte: generarReporte,
        cancelarReporte: cancelarReporte,
        exportarDatos: exportarDatos
    };
})();

// Inicializar
document.addEventListener('DOMContentLoaded', ReporteCompras.init);
window.ReporteCompras = ReporteCompras;