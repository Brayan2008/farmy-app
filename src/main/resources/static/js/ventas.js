let cart = [];

function openProducto(btn) {


    const id = btn.dataset.id;
    const name = btn.dataset.name;
    const price = parseFloat(btn.dataset.price);
    const stock = parseInt(btn.dataset.stock);
    const lote = btn.dataset.lote;
    const vencimiento = btn.dataset.vencimiento;
    const img = btn.dataset.img;
    const receta = btn.dataset.receta;
    const categoria = btn.dataset.categoria;
    const descripcion = btn.dataset.descripcion;

    document.getElementById('prodName').innerText = name;
    document.getElementById('prodPrice').innerText = price.toFixed(2);
    document.getElementById('prodStock').innerText = stock;
    document.getElementById('prodLote').innerText = lote;
    document.getElementById('prodVencimiento').innerText = vencimiento;
    document.getElementById('prodCategoria').innerText = categoria || 'Sin categoría';
    document.getElementById('prodDescripcion').innerText = descripcion || 'Sin descripción';
    document.getElementById('prodReceta').innerText = (receta === 'true') ? 'Sí' : 'No';

    const imgElem = document.getElementById('prodImg');
    if (img && img !== 'null') {
        imgElem.src = '/uploads/' + img;
    } else {
        imgElem.src = '/img/default.webp';
    }

    // Reset quantity input
    const qtyInput = document.getElementById('prodCantidad');
    qtyInput.value = 1;
    qtyInput.max = stock;

    // Store current product data in modal for add action
    const modal = document.getElementById('dialogProducto');
    modal.dataset.currentId = id;
    modal.dataset.currentName = name;
    modal.dataset.currentPrice = price;
    modal.dataset.currentStock = stock;
    modal.dataset.currentLote = lote;

    if (typeof modal.showModal === 'function') {
        modal.showModal();
    } else {
        modal.style.display = 'block';
    }
}

function closeProducto() {
    const modal = document.getElementById('dialogProducto');
    if (typeof modal.close === 'function') {
        modal.close();
    } else {
        modal.style.display = 'none';
    }
}

function addToCart() {
    const modal = document.getElementById('dialogProducto');
    const id = modal.dataset.currentId;
    const name = modal.dataset.currentName;
    const price = parseFloat(modal.dataset.currentPrice);
    const stock = parseInt(modal.dataset.currentStock);
    const lote = modal.dataset.currentLote;

    console.log('Adding to cart:', {id, name, price, stock, lote});

    const qtyInput = document.getElementById('prodCantidad');
    const quantity = parseInt(qtyInput.value);

    if (quantity > stock) {
        alert('La cantidad no puede superar el stock disponible (' + stock + ')');
        return;
    }
    if (quantity <= 0) {
        alert('La cantidad debe ser mayor a 0');
        return;
    }

    // Check if already in cart
    const existingItemIndex = cart.findIndex(item => item.lote === lote);
    if (existingItemIndex >= 0) {
        const newQty = cart[existingItemIndex].quantity + quantity;
        if (newQty > stock) {
            alert('La cantidad total en el carrito superaría el stock disponible.');
            return;
        }
        cart[existingItemIndex].quantity = newQty;
    } else {
        cart.push({
            id: id,
            name: name,
            price: price,
            quantity: quantity,
            stock: stock,
            lote: lote
        });
    }

    renderCart();
    closeProducto();
}

function removeFromCart(index) {
    cart.splice(index, 1);
    renderCart();
}

function renderCart() {
    const container = document.getElementById('cartItems');
    container.innerHTML = '';

    if (cart.length === 0) {
        container.innerHTML = '<p class="text-sm text-gray-500">No hay productos agregados</p>';
        updateTotals();
        return;
    }

    cart.forEach((item, index) => {
        const subtotal = item.price * item.quantity;

        const itemDiv = document.createElement('div');
        itemDiv.className = 'flex justify-between items-center p-2 border-b border-gray-200 text-sm';
        itemDiv.innerHTML = `
            <div class="flex-1">
                <div class="font-bold">${item.name}</div>
                <div class="text-xs text-gray-500">Lote: ${item.lote} | S/. ${item.price.toFixed(2)} x ${item.quantity}</div>
            </div>
            <div class="font-bold mx-2">S/. ${subtotal.toFixed(2)}</div>
            <button onclick="removeFromCart(${index})" class="text-red-500 hover:text-red-700">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clip-rule="evenodd" />
                </svg>
            </button>
        `;
        container.appendChild(itemDiv);
    });

    updateTotals();
}

function updateTotals() {
    let total = 0;
    cart.forEach(item => {
        total += item.price * item.quantity;
    });

    // Assuming prices include IGV or logic is similar to purchases
    // For sales, usually price is final. Let's assume price includes IGV for display.
    // Or if we need to calculate IGV separately:
    const subtotal = total / 1.18;
    const igv = total - subtotal;

    document.getElementById('subtotal').innerText = subtotal.toFixed(2);
    document.getElementById('igv').innerText = igv.toFixed(2);
    document.getElementById('total').innerText = total.toFixed(2);

    // Update max for initial payment if credit
    const initialPaymentInput = document.getElementById('initialPayment');
    if (initialPaymentInput) {
        initialPaymentInput.max = total.toFixed(2);
    }
}

function mostrarPagoInicial(select) {
    const pagoInicialDiv = document.getElementById('pagoInicial');
    const fechaVencimientoDiv = document.getElementById('fechaVencimientoDIV');

    if (select.value === 'CREDITO') {
        pagoInicialDiv.style.display = 'flex';
        fechaVencimientoDiv.style.display = 'block';
        document.getElementById('fechaVencimientoId').required = true;

    } else {
        pagoInicialDiv.style.display = 'none';
        fechaVencimientoDiv.style.display = 'none';
        document.getElementById('fechaVencimientoId').required = false;

        document.getElementById('enableInitial').checked = false;
        activarPagoInicial(); // Reset state
    }
}

function activarPagoInicial() {
    const checkbox = document.getElementById('enableInitial');
    const input = document.getElementById('initialPayment');
    const metodoPagoSelect = document.getElementById('metodoPagoSelect');

    if (checkbox.checked) {
        input.disabled = false;
        input.focus();
    } else {
        input.disabled = true;
        input.value = '0.00';
    }
}

function abrirConfirmarRegistro() {
    if (cart.length === 0) {
        alert('Debe agregar productos al carrito');
        return;
    }

    const cliente = document.getElementById('clienteSelect').value;
    alert('Cliente seleccionado: ' + cliente);
    if (!cliente) {
        alert('Debe seleccionar un cliente');
        return;
    }

    const tipoVenta = document.getElementById('tipoVentaSelect').value;
    const metodoPago = document.getElementById('metodoPagoSelect').value;

    if (tipoVenta === 'CONTADO' && !metodoPago) {
        alert('Debe seleccionar un método de pago para ventas al contado');
        return;
    }

    if (tipoVenta === 'CREDITO') {
        const fechaVenc = document.getElementById('fechaVencimientoId').value;
        if (!fechaVenc) {
            alert('Debe seleccionar una fecha de vencimiento para ventas a crédito');
            return;
        }

        const enableInitial = document.getElementById('enableInitial').checked;
        if (enableInitial && !metodoPago) {
            alert('Debe seleccionar un método de pago para el pago inicial');
            return;
        }
    }

    // Fill hidden form
    document.getElementById('formClienteId').value = cliente;
    document.getElementById('formTipoVenta').value = tipoVenta;
    document.getElementById('formMetodoPagoId').value = metodoPago;
    document.getElementById('formFechaVencimiento').value = document.getElementById('fechaVencimientoId').value;
    document.getElementById('formMontoPagoInicial').value = document.getElementById('initialPayment').value;
    document.getElementById('formSubtotal').value = parseFloat(document.getElementById('subtotal').innerText);
    document.getElementById('formIgv').value = parseFloat(document.getElementById('igv').innerText);
    document.getElementById('formTotal').value = parseFloat(document.getElementById('total').innerText);

    alert('montoPagoInicial: ' + document.getElementById('formMontoPagoInicial').value);
    // Create the JSON structure expected by the backend
    const detallesData = cart.map(item => ({
        lote: item.lote,
        cantidad: item.quantity,
    }));

    document.getElementById('formItemsJson').value = JSON.stringify(detallesData);

    const modal = document.getElementById('dialogConfirmar');
    if (typeof modal.showModal === 'function') {
        modal.showModal();
    } else {
        modal.style.display = 'block';
    }
}

function addHiddenInput(form, name, value) {
    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = name;
    input.value = value;
    input.className = 'detalle-input';
    form.appendChild(input);
}

function closeConfirmar() {
    const modal = document.getElementById('dialogConfirmar');
    if (typeof modal.close === 'function') {
        modal.close();
    } else {
        modal.style.display = 'none';
    }
}

function filterProducts() {
    const query = document.getElementById('searchProduct').value.toLowerCase();
    const cards = document.querySelectorAll('.product-card-container');
    cards.forEach(card => {
        const name = card.dataset.name.toLowerCase();
        if (name.includes(query)) {
            card.style.display = 'block';
        } else {
            card.style.display = 'none';
        }
    });
}
