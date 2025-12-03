let currentProduct = null;
const cart = [];

function openProducto(btn) {
    currentProduct = {
        id: parseInt(btn.dataset.id),
        name: btn.dataset.name,
        price: parseFloat(btn.dataset.price),
        img: btn.dataset.img ? '/uploads/' + btn.dataset.img : '/img/default.webp',
        receta: btn.dataset.receta === 'true',
        categoria: btn.dataset.categoria,
        descripcion: btn.dataset.descripcion || 'Sin descripción'
    };

    console.log(currentProduct);

    document.getElementById('prodImg').src = currentProduct.img;
    document.getElementById('prodName').innerText = currentProduct.name;
    document.getElementById('prodPrice').innerText = currentProduct.price.toFixed(2);
    document.getElementById('prodCategoria').innerText = currentProduct.categoria;
    document.getElementById('prodDescripcion').innerText = currentProduct.descripcion;

    const recetaSpan = document.getElementById('prodReceta');
    if (currentProduct.receta) {
        recetaSpan.innerText = 'SÍ';
        recetaSpan.className = 'font-bold text-red-600';
    } else {
        recetaSpan.innerText = 'NO';
        recetaSpan.className = 'font-bold text-green-600';
    }

    document.getElementById('prodCantidad').value = 1;

    const d = document.getElementById('dialogProducto');

    if (typeof d.showModal === 'function') {
        d.showModal();
    } else {
        d.style.display = 'block';
    }
}

function closeProducto() {
    const d = document.getElementById('dialogProducto');

    if (typeof d.close === 'function') {
        d.close();
    }
    else {
        d.style.display = 'none';
    }

}

function addToCart() {
    const qty = parseInt(document.getElementById('prodCantidad').value || '1');

    const item = {
        id: currentProduct.id,
        nombre: currentProduct.name,
        precio: currentProduct.price,
        img: currentProduct.img,
        cantidad: qty
    };

    cart.push(item);
    renderCart();
    closeProducto();
}

function renderCart() {
    const container = document.getElementById('cartItems');
    container.innerHTML = '';

    if (cart.length === 0) {
        container.innerHTML = '<p class="mb-1 text-sm text-gray-500">No hay productos agregados</p>';
        updateSummary();
        return;
    }

    cart.forEach((it, idx) => {
        const row = document.createElement('div');
        row.className = 'flex my-3 shadow-xl rounded-xl';
        row.innerHTML =
            `
            <div>
            <img src="${it.img}" alt="${it.img}-img" class="h-20 w-20 mr-2 object-cover rounded-l-xl">
            </div>
            <div class="flex-1">
            <div class="text-start pl-2">
                <strong>${it.nombre}</strong>
            </div>
            <div class="flex"> 
                <div> 
                    <div class="my-2 border-black/10 border rounded-xl">
                        <div class="flex items-center gap-1">
                        <button type="button" class="size-8 leading-8 text-gray-600 transition hover:opacity-75" onclick="disminuir(${idx})">
                        −
                        </button>
                        
                        <input type="number" id="Quantity" value="${it.cantidad}" min="1" step="1" 
                            class="text-center h-5 w-12 rounded-sm border-gray-200  [&::-webkit-inner-spin-button]:appearance-none [&::-webkit-outer-spin-button]:appearance-none sm:text-sm">
                        
                        <button type="button" class="size-8 leading-8 text-gray-600 transition hover:opacity-75" onclick="aumentar(${idx})">
                        +
                        </button>
                        </div>
                    </div>
                </div>
                <div class="w-1/2 self-center flex items-center justify-around pr-2">
                    <span> S/ ${it.precio.toFixed(2)} </span>
                    <button type="button" aria-label="Eliminar producto" title="Eliminar" class="bg-red-600 text-white rounded-md hover:text-red-800 hover:bg-red-400 font-bold px-2" onclick="cart.splice(${idx},1); renderCart();">
                        ×
                    </button>
                </div>
            </div>
            </div>
            
            `;

        container.appendChild(row);
    });

    updateSummary();
}

function updateQty(idx, qty) {
    cart[idx].cantidad = parseInt(qty);
    updateSummary();
}

function updateSummary() {
    const subtotal = cart.reduce((s, i) => s + i.precio * i.cantidad, 0);
    const igv = subtotal * 0.18;
    const total = subtotal + igv;

    document.getElementById('subtotal').innerText = subtotal.toFixed(2);
    document.getElementById('igv').innerText = igv.toFixed(2);
    document.getElementById('total').innerText = total.toFixed(2);
}

function mostrarPagoInicial(select) {
    let mP = document.getElementById('metodoPagoSelect');
    let mPLabel = document.getElementById('metodoPagoLabel');

    if (select.value === "1") {
        document.querySelector('#pagoInicial').style.display = 'flex';
        document.querySelector('#fechaVencimientoDIV').style.display = 'block';
        
        mP.required = false;
        mP.style.display = 'none';
        mPLabel.style.display = 'none';

    } else {
        document.querySelector('#pagoInicial').style.display = 'none';
        document.querySelector('#fechaVencimientoDIV').style.display = 'none';

        mP.required = true;
        mP.style.display = 'block';
        mPLabel.style.display = 'block';
    }
}

function abrirConfirmarRegistro() {
    const proveedorId = document.getElementById('proveedorSelect').value;
    const metodoPagoId = document.getElementById('metodoPagoSelect').value;
    const tipoCompraId = document.querySelector('#tipoCompraSelect').value;
    const fechaVencimiento = document.getElementById('fechaVencimientoId').value;

    if (!proveedorId) {
        alert('Seleccione un proveedor o registre uno.');
        return;
    }

    if (!metodoPagoId && tipoCompraId !== "1") {
        alert('Seleccione un método de pago.');
        return;
    }

    if (!tipoCompraId) {
        alert('Seleccione un tipo de compra.');
        return;
    }

    if (cart.length === 0) {
        alert('Agregue productos a la compra.');
        return;
    }

    const subtotal = parseFloat(document.getElementById('subtotal').innerText || '0');
    const igv = parseFloat(document.getElementById('igv').innerText || '0');
    const total = parseFloat(document.getElementById('total').innerText || '0');

    document.getElementById('formProveedorId').value = proveedorId;
    document.getElementById('formTipoCompraId').value = tipoCompraId;
    document.getElementById('formMetodoPagoId').value = metodoPagoId;
    document.getElementById('formFechaVencimiento').value = fechaVencimiento;
    document.getElementById('formSubtotal').value = subtotal;
    document.getElementById('formIgv').value = igv;
    document.getElementById('formTotal').value = total;
    // construir itemsJson desde el carrito

    const items = cart.map(it => ({
        id: it.id,
        cantidad: it.cantidad,
        precio: it.precio
    }));

    document.getElementById('itemsJson').value = JSON.stringify(items);

    const d = document.getElementById('dialogConfirmar');

    if (typeof d.showModal === 'function') {
        d.showModal();
    } else {
        d.style.display = 'block';
    }
}

function closeConfirmar() {
    const d = document.getElementById('dialogConfirmar');

    if (typeof d.close === 'function') {
        d.close();
    } else {
        d.style.display = 'none';
    }

}

const aumentar = (idx) => {
    const elements = document.querySelectorAll('input[id="Quantity"]');
    const element = elements[idx];
    if (!element) return;
    element.stepUp();
    updateQty(idx, element.value);
}

const disminuir = (idx) => {
    const elements = document.querySelectorAll('input[id="Quantity"]');
    const element = elements[idx];
    if (!element) return;
    element.stepDown();
    updateQty(idx, element.value);
}

const activarPagoInicial = () => {
    const checkbox = document.getElementById('enableInitial');
    const inputInitial = document.getElementById('initialPayment');

    if (checkbox.checked) {
        inputInitial.disabled = false;
    } else {
        inputInitial.disabled = true;
        inputInitial.value = '0.00';
    }
};

// Inicializar scripts:

const d = new Date();
d.setDate(d.getDate() - 1);
const today = d.toISOString().split('T')[0];
document.addEventListener('DOMContentLoaded', function () {
    document.getElementById('fechaVencimientoId').setAttribute('min', today);
});