// Reporte de Compras - Funcionalidades JavaScript
const ReporteCompras = (function() {
    // Variables privadas
    let formFiltros;
    let isGenerating = false;
    
    // Inicialización
    function init() {
        console.log('Reporte de Compras inicializado');
        
        // Referencias a elementos DOM
        formFiltros = document.getElementById('filtroForm');
        
        // Configurar fechas por defecto
        configurarFechasPorDefecto();
        
        // Configurar eventos
        configurarEventos();
        
        // Configurar validaciones
        configurarValidaciones();
        
        // Actualizar total de registros
        actualizarTotalRegistros();
    }
    
    // Configurar fechas por defecto
    function configurarFechasPorDefecto() {
        const fechaInicio = document.querySelector('input[name="fechaInicio"]');
        const fechaFin = document.querySelector('input[name="fechaFin"]');
        
        // Si las fechas están vacías, establecer valores por defecto
        if (fechaInicio && !fechaInicio.value) {
            const hace30Dias = new Date();
            hace30Dias.setDate(hace30Dias.getDate() - 30);
            fechaInicio.value = formatDate(hace30Dias);
        }
        
        if (fechaFin && !fechaFin.value) {
            const hoy = new Date();
            fechaFin.value = formatDate(hoy);
        }
        
        // Configurar hora por defecto si está vacía
        const horaInicio = document.querySelector('input[name="horaInicio"]');
        const horaFin = document.querySelector('input[name="horaFin"]');
        
        if (horaInicio && !horaInicio.value) {
            horaInicio.value = '00:00';
        }
        
        if (horaFin && !horaFin.value) {
            horaFin.value = '23:59';
        }
    }
    
    // Formatear fecha para input date
    function formatDate(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }
    
    // Configurar eventos
    function configurarEventos() {
        // Validar fechas antes de enviar
        if (formFiltros) {
            formFiltros.addEventListener('submit', function(e) {
                e.preventDefault();
                generarReporte();
            });
        }
        
        // Configurar inputs de importe
        const importeMinimo = document.querySelector('input[name="importeMinimo"]');
        const importeMaximo = document.querySelector('input[name="importeMaximo"]');
        
        if (importeMinimo) {
            importeMinimo.addEventListener('change', validarImportes);
        }
        
        if (importeMaximo) {
            importeMaximo.addEventListener('change', validarImportes);
        }
        
        // Configurar botón de eliminar filtros
        const btnEliminarFiltros = document.querySelector('.btn-eliminar-filtros');
        if (btnEliminarFiltros) {
            btnEliminarFiltros.addEventListener('click', eliminarFiltros);
        }
        
        // Configurar botón de aplicar filtros
        const btnFiltro = document.querySelector('.btn-filtro');
        if (btnFiltro) {
            btnFiltro.addEventListener('click', aplicarFiltros);
        }
        
        // Configurar botón de cancelar
        const btnCancelar = document.querySelector('.btn-cancelar');
        if (btnCancelar) {
            btnCancelar.addEventListener('click', cancelarReporte);
        }
        
        // Configurar botón de generar
        const btnGenerar = document.querySelector('.btn-generar');
        if (btnGenerar) {
            btnGenerar.addEventListener('click', generarReporte);
        }
    }
    
    // Configurar validaciones
    function configurarValidaciones() {
        // Agregar validación personalizada a inputs numéricos
        const inputsNumericos = document.querySelectorAll('input[type="number"]');
        inputsNumericos.forEach(input => {
            input.addEventListener('input', function(e) {
                const value = parseFloat(e.target.value);
                if (value < 0) {
                    e.target.value = 0;
                }
                
                // Limitar decimales a 2
                if (e.target.value.includes('.')) {
                    const parts = e.target.value.split('.');
                    if (parts[1].length > 2) {
                        e.target.value = parseFloat(e.target.value).toFixed(2);
                    }
                }
            });
        });
        
        // Validar fechas en tiempo real
        const fechaInicio = document.querySelector('input[name="fechaInicio"]');
        const fechaFin = document.querySelector('input[name="fechaFin"]');
        
        if (fechaInicio) {
            fechaInicio.addEventListener('change', validarFechasEnTiempoReal);
        }
        
        if (fechaFin) {
            fechaFin.addEventListener('change', validarFechasEnTiempoReal);
        }
    }
    
    // Validar fechas en tiempo real
    function validarFechasEnTiempoReal() {
        const fechaInicio = document.querySelector('input[name="fechaInicio"]');
        const fechaFin = document.querySelector('input[name="fechaFin"]');
        
        if (fechaInicio.value && fechaFin.value) {
            const fechaInicioObj = new Date(fechaInicio.value);
            const fechaFinObj = new Date(fechaFin.value);
            
            if (fechaInicioObj > fechaFinObj) {
                mostrarMensaje('La fecha de inicio no puede ser mayor a la fecha de fin', 'error');
                fechaInicio.focus();
                return false;
            }
        }
        return true;
    }
    
    // Validar fechas
    function validarFechas() {
        const fechaInicio = document.querySelector('input[name="fechaInicio"]');
        const horaInicio = document.querySelector('input[name="horaInicio"]');
        const fechaFin = document.querySelector('input[name="fechaFin"]');
        const horaFin = document.querySelector('input[name="horaFin"]');
        
        // Validar campos requeridos
        if (!fechaInicio.value || !fechaFin.value) {
            mostrarMensaje('Las fechas de inicio y fin son requeridas', 'error');
            return false;
        }
        
        // Validar que fecha inicio no sea mayor que fecha fin
        if (fechaInicio.value && fechaFin.value) {
            const fechaInicioCompleta = new Date(fechaInicio.value + ' ' + horaInicio.value);
            const fechaFinCompleta = new Date(fechaFin.value + ' ' + horaFin.value);
            
            if (fechaInicioCompleta > fechaFinCompleta) {
                mostrarMensaje('La fecha de inicio no puede ser mayor a la fecha de fin', 'error');
                return false;
            }
        }
        
        return true;
    }
    
    // Validar importes
    function validarImportes() {
        const importeMinimo = document.querySelector('input[name="importeMinimo"]');
        const importeMaximo = document.querySelector('input[name="importeMaximo"]');
        
        if (importeMinimo.value && importeMaximo.value) {
            const minimo = parseFloat(importeMinimo.value);
            const maximo = parseFloat(importeMaximo.value);
            
            if (minimo > maximo) {
                mostrarMensaje('El importe mínimo no puede ser mayor al importe máximo', 'error');
                importeMinimo.focus();
                return false;
            }
        }
        
        return true;
    }
    
    // Aplicar filtros
    function aplicarFiltros() {
        if (!validarFechas()) {
            return;
        }
        
        if (!validarImportes()) {
            return;
        }
        
        // Mostrar loading
        mostrarLoading(true);
        
        // Crear formulario temporal para GET
        const formTemp = document.createElement('form');
        formTemp.method = 'GET';
        formTemp.action = '/compras/reportes';
        
        // Copiar todos los datos del formulario principal
        const formData = new FormData(formFiltros);
        for (let [key, value] of formData.entries()) {
            if (key !== 'nombreReporte' && key !== 'tipoReporte' && key !== 'formato' && key !== 'descripcion') {
                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = key;
                input.value = value;
                formTemp.appendChild(input);
            }
        }
        
        // Agregar parámetros del modal
        const urlParams = new URLSearchParams(window.location.search);
        if (urlParams.has('fromModal')) {
            const fromModalInput = document.createElement('input');
            fromModalInput.type = 'hidden';
            fromModalInput.name = 'fromModal';
            fromModalInput.value = 'true';
            formTemp.appendChild(fromModalInput);
        }
        
        // Enviar formulario
        document.body.appendChild(formTemp);
        formTemp.submit();
    }
    
    // Eliminar filtros
    function eliminarFiltros() {
        if (!confirm('¿Está seguro de que desea eliminar todos los filtros?')) {
            return;
        }
        
        // Resetear todos los filtros
        document.querySelectorAll('.filtro-input').forEach(input => {
            if (input.type === 'text' || input.type === 'number') {
                input.value = '';
            } else if (input.tagName === 'SELECT') {
                input.selectedIndex = 0;
            }
        });
        
        // Resetear fechas a valores por defecto
        const hoy = new Date();
        const hace30Dias = new Date();
        hace30Dias.setDate(hoy.getDate() - 30);
        
        const fechaInicio = document.querySelector('input[name="fechaInicio"]');
        const horaInicio = document.querySelector('input[name="horaInicio"]');
        const fechaFin = document.querySelector('input[name="fechaFin"]');
        const horaFin = document.querySelector('input[name="horaFin"]');
        
        if (fechaInicio) fechaInicio.value = formatDate(hace30Dias);
        if (horaInicio) horaInicio.value = '00:00';
        if (fechaFin) fechaFin.value = formatDate(hoy);
        if (horaFin) horaFin.value = '23:59';
        
        // Aplicar filtros
        aplicarFiltros();
    }
    
    // Generar reporte
    function generarReporte() {
        if (isGenerating) return;
        
        // Validar formulario
        if (!validarFechas()) {
            return;
        }
        
        // Obtener total de registros
        const totalRegistros = document.getElementById('totalRegistros').textContent;
        if (parseInt(totalRegistros) === 0) {
            if (!confirm('No hay registros para generar el reporte. ¿Desea continuar de todos modos?')) {
                return;
            }
        }
        
        // Mostrar confirmación
        if (!confirm('¿Está seguro de generar el reporte? Esto guardará el reporte en el sistema.')) {
            return;
        }
        
        isGenerating = true;
        mostrarLoadingGenerar(true);
        
        // Agregar total de registros al formulario
        let totalInput = document.querySelector('input[name="totalRegistros"]');
        if (!totalInput) {
            totalInput = document.createElement('input');
            totalInput.type = 'hidden';
            totalInput.name = 'totalRegistros';
            formFiltros.appendChild(totalInput);
        }
        totalInput.value = totalRegistros;
        
        // Enviar formulario
        setTimeout(() => {
            formFiltros.submit();
        }, 500);
    }
    
    // Cancelar reporte
    function cancelarReporte() {
        if (confirm('¿Está seguro de cancelar la generación de este reporte?')) {
            window.location.href = '/reportes';
        }
    }
    
    // Exportar datos
    function exportarDatos() {
        const compras = document.querySelectorAll('#tablaCompras tbody tr');
        if (compras.length === 0) {
            mostrarMensaje('No hay datos para exportar', 'error');
            return;
        }
        
        let csvContent = "data:text/csv;charset=utf-8,";
        
        // Encabezados
        const headers = ['N° Factura', 'Proveedor', 'RUC', 'Tipo Compra', 'Método Pago', 'Subtotal', 'IGV', 'Total', 'Saldo Pendiente', 'Estado', 'Fecha'];
        csvContent += headers.join(",") + "\n";
        
        // Datos
        compras.forEach(compra => {
            const cells = compra.querySelectorAll('td');
            const row = [];
            
            cells.forEach((cell, index) => {
                let text = cell.textContent.trim();
                
                // Limpiar texto para CSV
                if (text.includes(',')) {
                    text = `"${text}"`;
                }
                
                row.push(text);
            });
            
            csvContent += row.join(",") + "\n";
        });
        
        // Crear enlace de descarga
        const encodedUri = encodeURI(csvContent);
        const link = document.createElement("a");
        link.setAttribute("href", encodedUri);
        link.setAttribute("download", "reporte_compras.csv");
        document.body.appendChild(link);
        
        // Descargar
        link.click();
        document.body.removeChild(link);
        
        mostrarMensaje('Datos exportados exitosamente', 'success');
    }
    
    // Actualizar total de registros
    function actualizarTotalRegistros() {
        const totalRegistros = document.getElementById('totalRegistros');
        if (totalRegistros) {
            const compras = document.querySelectorAll('#tablaCompras tbody tr:not([style*="display: none"])');
            totalRegistros.textContent = compras.length;
        }
    }
    
    // Mostrar/ocultar loading
    function mostrarLoading(mostrar) {
        const btnFiltro = document.querySelector('.btn-filtro');
        
        if (mostrar) {
            if (btnFiltro) {
                btnFiltro.disabled = true;
                btnFiltro.innerHTML = '<span class="spinner-small"></span> Procesando...';
            }
            
            // Deshabilitar todos los inputs
            document.querySelectorAll('input, select, button').forEach(element => {
                if (!element.classList.contains('btn-eliminar-filtros')) {
                    element.disabled = true;
                }
            });
        } else {
            if (btnFiltro) {
                btnFiltro.disabled = false;
                btnFiltro.textContent = 'Aplicar filtros';
            }
            
            // Habilitar todos los inputs
            document.querySelectorAll('input, select, button').forEach(element => {
                element.disabled = false;
            });
        }
    }
    
    // Mostrar/ocultar loading en botón generar
    function mostrarLoadingGenerar(mostrar) {
        const btnGenerar = document.querySelector('.btn-generar');
        const btnGenerarTexto = document.getElementById('btnGenerarTexto');
        const btnGenerarLoading = document.getElementById('btnGenerarLoading');
        
        if (btnGenerar && btnGenerarTexto && btnGenerarLoading) {
            if (mostrar) {
                btnGenerar.disabled = true;
                btnGenerarTexto.classList.add('hidden');
                btnGenerarLoading.classList.remove('hidden');
            } else {
                btnGenerar.disabled = false;
                btnGenerarTexto.classList.remove('hidden');
                btnGenerarLoading.classList.add('hidden');
                isGenerating = false;
            }
        }
    }
    
    // Mostrar mensaje
    function mostrarMensaje(mensaje, tipo = 'info') {
        // Remover mensajes anteriores
        const mensajesPrevios = document.querySelectorAll('.mensaje-compras');
        mensajesPrevios.forEach(msg => msg.remove());
        
        const alerta = document.createElement('div');
        alerta.className = `mensaje-compras fixed top-4 right-4 z-50 px-4 py-3 rounded-lg shadow-lg ${tipo === 'error' ? 'bg-red-100 text-red-800 border border-red-200' : 'bg-green-100 text-green-800 border border-green-200'}`;
        alerta.innerHTML = `
            <div class="flex items-center">
                <span class="mr-2">${tipo === 'error' ? '❌' : '✅'}</span>
                <span>${mensaje}</span>
            </div>
        `;
        
        document.body.appendChild(alerta);
        
        // Auto-remover después de 5 segundos
        setTimeout(() => {
            alerta.style.opacity = '0';
            alerta.style.transition = 'opacity 0.5s';
            setTimeout(() => alerta.remove(), 500);
        }, 5000);
    }
    
    // Función para formatear moneda
    function formatoMoneda(valor) {
        return new Intl.NumberFormat('es-PE', {
            style: 'currency',
            currency: 'PEN',
            minimumFractionDigits: 2
        }).format(valor);
    }
    
    // Función para formatear fecha
    function formatoFecha(fechaString) {
        if (!fechaString) return '---';
        
        const fecha = new Date(fechaString);
        return fecha.toLocaleDateString('es-ES', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric'
        });
    }
    
    // Exportar funciones públicas
    return {
        init: init,
        eliminarFiltros: eliminarFiltros,
        aplicarFiltros: aplicarFiltros,
        generarReporte: generarReporte,
        cancelarReporte: cancelarReporte,
        exportarDatos: exportarDatos,
        formatoMoneda: formatoMoneda,
        formatoFecha: formatoFecha
    };
})();

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    ReporteCompras.init();
});

// Exportar para uso global
window.ReporteCompras = ReporteCompras;