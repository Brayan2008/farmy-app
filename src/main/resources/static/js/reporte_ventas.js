// Reemplaza TODO el contenido de reporte_ventas.js con esto:

const ReporteVentas = (function() {
    let isGenerating = false;
    
    function init() {
        console.log('Reporte de Ventas inicializado');
        configurarFechasPorDefecto();
        configurarEventos();
        consoleValoresIniciales();
    }
    
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
    
    function formatDate(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }
    
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
    }
    
    function consoleValoresIniciales() {
        console.log('Valores iniciales de filtros:');
        console.log('- fechaInicio:', document.querySelector('input[name="fechaInicio"]')?.value);
        console.log('- fechaFin:', document.querySelector('input[name="fechaFin"]')?.value);
        console.log('- tipoDocumento:', document.querySelector('input[name="tipoDocumento"]:checked')?.value);
        console.log('- numeroDocumento:', document.querySelector('input[name="numeroDocumento"]')?.value);
        console.log('- nombreCliente:', document.querySelector('input[name="nombreCliente"]')?.value);
        console.log('- metodoPagoNombre:', document.querySelector('select[name="metodoPagoNombre"]')?.value);
        console.log('- estadoVenta:', document.querySelector('select[name="estadoVenta"]')?.value);
        console.log('- importeMinimo:', document.querySelector('input[name="importeMinimo"]')?.value);
        console.log('- importeMaximo:', document.querySelector('input[name="importeMaximo"]')?.value);
        console.log('- ordenarPor:', document.querySelector('select[name="ordenarPor"]')?.value);
        console.log('- orden:', document.querySelector('select[name="orden"]')?.value);
    }
    
    function aplicarFiltros() {
        console.log('=== APLICANDO FILTROS ===');
        
        // Obtener todos los valores del formulario
        const formData = new FormData(document.getElementById('filtroForm'));
        const params = new URLSearchParams();
        
        console.log('Datos del formulario:');
        for (let [key, value] of formData.entries()) {
            console.log(key + ': ' + value);
            if (value && value !== '') {
                params.append(key, value);
            }
        }
        
        // Agregar parámetros del modal
        const urlParams = new URLSearchParams(window.location.search);
        const fromModal = urlParams.get('fromModal');
        const reporteId = urlParams.get('reporteId');
        
        if (fromModal) {
            params.append('fromModal', fromModal);
        }
        if (reporteId) {
            params.append('reporteId', reporteId);
        }
        
        // Validar fechas
        const fechaInicio = document.querySelector('input[name="fechaInicio"]').value;
        const fechaFin = document.querySelector('input[name="fechaFin"]').value;
        
        if (!fechaInicio || !fechaFin) {
            alert('Las fechas de inicio y fin son requeridas');
            return;
        }
        
        if (new Date(fechaInicio) > new Date(fechaFin)) {
            alert('La fecha de inicio no puede ser mayor a la fecha de fin');
            return;
        }
        
        // Construir URL
        const url = '/ventas/reportes?' + params.toString();
        console.log('Redirigiendo a:', url);
        
        // Mostrar loading
        mostrarLoading(true);
        
        // Redirigir
        setTimeout(() => {
            window.location.href = url;
        }, 500);
    }
    
    function eliminarFiltros() {
        if (!confirm('¿Está seguro de eliminar todos los filtros?')) return;
        
        // Resetear todos los inputs
        document.querySelectorAll('.filtro-input').forEach(input => {
            if (input.type === 'text' || input.type === 'number') {
                input.value = '';
            } else if (input.tagName === 'SELECT') {
                input.selectedIndex = 0;
            }
        });
        
        // Resetear radios
        document.getElementById('doc-todos').checked = true;
        
        // Resetear fechas
        const hoy = new Date();
        const hace30Dias = new Date();
        hace30Dias.setDate(hoy.getDate() - 30);
        
        document.querySelector('input[name="fechaInicio"]').value = formatDate(hace30Dias);
        document.querySelector('input[name="horaInicio"]').value = '00:00';
        document.querySelector('input[name="fechaFin"]').value = formatDate(hoy);
        document.querySelector('input[name="horaFin"]').value = '23:59';
        
        console.log('Filtros eliminados, aplicando...');
        
        // Aplicar filtros después de resetear
        setTimeout(() => {
            aplicarFiltros();
        }, 300);
    }
    
    function generarReporte() {
        if (isGenerating) return;
        
        // Obtener total de registros
        const totalRegistros = document.getElementById('totalRegistros').textContent || '0';
        
        // Obtener valores del modal
        const nombreHidden = document.getElementById('nombreReporteHidden');
        const formatoHidden = document.getElementById('formatoHidden');
        const descripcionHidden = document.getElementById('descripcionHidden');
        const reporteIdHidden = document.getElementById('reporteIdHidden');
        
        const nombre = nombreHidden?.value || 'Reporte de Ventas';
        const formato = formatoHidden?.value || 'pdf';
        const descripcion = descripcionHidden?.value || '';
        const reporteId = reporteIdHidden?.value || '';
        
        console.log('Generando reporte:', { nombre, formato, reporteId, totalRegistros });
        
        if (!confirm(`¿${reporteId ? 'Actualizar' : 'Generar'} reporte?\n\nNombre: ${nombre}\nFormato: ${formato}\nRegistros: ${totalRegistros}`)) {
            return;
        }
        
        isGenerating = true;
        mostrarLoadingGenerar(true);
        
        // Llenar formulario oculto
        document.getElementById('hiddenNombreReporte').value = nombre;
        document.getElementById('hiddenFormato').value = formato;
        document.getElementById('hiddenDescripcion').value = descripcion;
        document.getElementById('hiddenReporteId').value = reporteId;
        document.getElementById('hiddenTotalRegistros').value = totalRegistros;
        
        // Obtener valores actuales de filtros
        const formData = new FormData(document.getElementById('filtroForm'));
        document.getElementById('hiddenFechaInicio').value = formData.get('fechaInicio') || '';
        document.getElementById('hiddenHoraInicio').value = formData.get('horaInicio') || '00:00';
        document.getElementById('hiddenFechaFin').value = formData.get('fechaFin') || '';
        document.getElementById('hiddenHoraFin').value = formData.get('horaFin') || '23:59';
        
        // Enviar formulario
        setTimeout(() => {
            console.log('Enviando formulario de generación...');
            document.getElementById('formGenerarReporte').submit();
        }, 500);
    }
    
    function cancelarReporte() {
        if (confirm('¿Cancelar generación de reporte?')) {
            window.location.href = '/reportes';
        }
    }
    
    function mostrarLoading(mostrar) {
        const btnFiltro = document.querySelector('.btn-filtro');
        if (btnFiltro) {
            if (mostrar) {
                btnFiltro.disabled = true;
                btnFiltro.innerHTML = '<span class="spinner-small"></span> Procesando...';
            } else {
                btnFiltro.disabled = false;
                btnFiltro.textContent = 'Aplicar filtros';
            }
        }
    }
    
    function mostrarLoadingGenerar(mostrar) {
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
    
    return {
        init: init,
        aplicarFiltros: aplicarFiltros,
        eliminarFiltros: eliminarFiltros,
        generarReporte: generarReporte,
        cancelarReporte: cancelarReporte
    };
})();

document.addEventListener('DOMContentLoaded', ReporteVentas.init);
window.ReporteVentas = ReporteVentas;