registrar = (id) => {
    console.log('works');
    
    const modal = document.getElementById(id);
    modal.showModal();
}

function aplicarFiltros() {

    const searchInput = document.getElementById('search-input');
    const searchText = searchInput ? searchInput.value.toLowerCase() : '';
    
    const statusSelect = document.getElementById('filtroEstado');
    const statusFilter = statusSelect ? statusSelect.value : 'all';

    const typeSelect = document.getElementById('filtroTipo');
    const typeFilter = typeSelect ? typeSelect.value : 'all';

    // 1. Table Filtering (Marcas, Categorias, etc.)
    const table = document.querySelector('table');
    if (table) {
        const rows = table.getElementsByTagName('tr');
        // Start from 1 to skip header
        for (let i = 1; i < rows.length; i++) {
            const row = rows[i];
            
            // Text Search
            const cells = row.getElementsByTagName('td');
            let textMatch = false;
            if (searchText === '') {
                textMatch = true;
            } else {
                for (let j = 0; j < cells.length; j++) {
                    if ((cells[j].textContent || cells[j].innerText).toLowerCase().indexOf(searchText) > -1) {
                        textMatch = true;
                        break;
                    }
                }
            }

            // Status Filter
            let statusMatch = true;
            if (statusFilter !== 'all') {
                const statusCell = row.querySelector('td[data-label="Estado"]');
                if (statusCell) {
                    const statusText = (statusCell.textContent || statusCell.innerText).toLowerCase();
                    if (statusFilter === 'true' && !statusText.includes('activo')) statusMatch = false;
                    else if (statusFilter === 'false' && !statusText.includes('inactivo')) statusMatch = false;
                    // Generic check for other values (PENDIENTE, PAGADO, ANULADO)
                    else if (statusFilter !== 'true' && statusFilter !== 'false' && !statusText.includes(statusFilter.toLowerCase())) statusMatch = false;
                }
            }

            // Type Filter (e.g. Payment Type)
            let typeMatch = true;
            if (typeFilter !== 'all') {
                const typeCell = row.querySelector('td[data-label="Tipo"]');
                if (typeCell) {
                    const typeText = (typeCell.textContent || typeCell.innerText).toLowerCase();
                    if (!typeText.includes(typeFilter.toLowerCase())) typeMatch = false;
                }
            }

            if (textMatch && statusMatch && typeMatch) {
                row.style.display = "";
            } else {
                row.style.display = "none";
            }
        }
        return;
    }

    // 2. Grid Filtering (Productos)
    const gridContainer = document.querySelector('.grid.grid-cols-1');
    if (gridContainer) {
        const cards = gridContainer.children;
        for (let i = 0; i < cards.length; i++) {
            const card = cards[i];
            if (card.nodeType !== 1) continue; // Ensure it's an element

            const text = (card.textContent || card.innerText).toLowerCase();
            
            if (text.indexOf(searchText) > -1) {
                card.style.display = "";
            } else {
                card.style.display = "none";
            }
        }
    }
}