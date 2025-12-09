function filterProducts() {
  const query = document.getElementById('searchInput').value.toLowerCase();
  const products = document.querySelectorAll('.producto');

  products.forEach(prod => {
    const name = prod.getAttribute('data-name');
    if (name.includes(query)) {
      prod.style.display = 'block';
    } else {
      prod.style.display = 'none';
    }
  });
}


let cart = JSON.parse(localStorage.getItem('farmyCart')) || [];

document.addEventListener('DOMContentLoaded', updateCartUI);

function incrementQty() {
  const input = document.getElementById('qtyInput');
  const max = parseInt(input.getAttribute('max')) || 9999;
  let val = parseInt(input.value) || 0;
  if (val < max) {
    input.value = val + 1;
  }
}

function decrementQty() {
  const input = document.getElementById('qtyInput');
  let val = parseInt(input.value) || 0;
  if (val > 0) {
    input.value = val - 1;
  }
}

function addToCartFromDetail(idStr, name, priceStr, image) {
  const id = parseInt(idStr);
  const price = parseFloat(priceStr);
  const qty = parseInt(document.getElementById('qtyInput').value);

  if (qty <= 0) {
    alert("Por favor, seleccione una cantidad válida.");
    return;
  }

  const existingItem = cart.find(item => item.id === id);
  if (existingItem) {
    existingItem.quantity += qty;
  } else {
    cart.push({ id: id, name: name, price: price, image: image, quantity: qty });
  }
  saveCart();
  updateCartUI();

  // Open cart feedback
  const cartPanel = document.getElementById('cartPanel');
  const overlay = document.getElementById('overlay');
  cartPanel.classList.add('open');
  overlay.classList.add('show');
}

function removeFromCart(id) {
  cart = cart.filter(item => item.id !== id);
  saveCart();
  updateCartUI();
}

function saveCart() {
  localStorage.setItem('farmyCart', JSON.stringify(cart));
}

function updateCartUI() {
  const cartCount = document.getElementById('cartCount');
  const cartItemsList = document.getElementById('cartItemsList');
  const emptyCartMsg = document.getElementById('emptyCartMsg');
  const cartTotalSection = document.getElementById('cartTotalSection');
  const cartTotalValue = document.getElementById('cartTotalValue');

  // Update count
  const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
  cartCount.innerText = totalItems;

  // Render items
  cartItemsList.innerHTML = '';
  let total = 0;

  if (cart.length === 0) {
    emptyCartMsg.style.display = 'block';
    cartTotalSection.style.display = 'none';
  } else {
    emptyCartMsg.style.display = 'none';
    cartTotalSection.style.display = 'block';

    cart.forEach(item => {
      const itemTotal = item.price * item.quantity;
      total += itemTotal;

      const div = document.createElement('div');
      div.className = 'cart-item';
      div.innerHTML = `
                    <img src="${item.image}" style="width: 40px; height: 40px; object-fit: cover; border-radius: 4px; margin-right: 10px;">
                    <div class="cart-item-info">
                        <div class="cart-item-title">${item.name}</div>
                        <div class="cart-item-price">S/ ${item.price.toFixed(2)} x ${item.quantity}</div>
                    </div>
                    <div style="font-weight:bold;">S/ ${itemTotal.toFixed(2)}</div>
                    <div class="cart-item-remove" onclick="removeFromCart(${item.id})">✕</div>
                `;
      cartItemsList.appendChild(div);
    });

    cartTotalValue.innerText = total.toFixed(2);
  }
}

// Toggle Cart Panel (from webClient.js logic, repeated here for safety if js file is simple)
const cartBtn = document.getElementById('cartBtn');
const closeCart = document.getElementById('closeCart');
const cartPanel = document.getElementById('cartPanel');
const overlay = document.getElementById('overlay');

if (cartBtn) {
  cartBtn.addEventListener('click', () => {
    cartPanel.classList.add('open');
    overlay.classList.add('show');
  });
}

if (closeCart) {
  closeCart.addEventListener('click', () => {
    cartPanel.classList.remove('open');
    overlay.classList.remove('show');
  });
}

if (overlay) {
  overlay.addEventListener('click', () => {
    cartPanel.classList.remove('open');
    overlay.classList.remove('show');
  });
}