// ruangan.js - Depends on catalogue-common.js

// ==== Inisialisasi setiap kartu ruang (max 1) ====
function initCards() {
  document.querySelectorAll('.item-card').forEach(card => {
    const sisaEl = card.querySelector('.sisa');
    const titleEl = card.querySelector('.item-title');
    if (!sisaEl) return;

    // 1. Tentukan Max Stock (untuk ruang selalu 1 atau 0 dari server)
    let max = Number(card.dataset.max);
    if (Number.isNaN(max) || !card.hasAttribute('data-max')) {
      let val = Number(sisaEl.textContent.trim() || '1');
      if (Number.isNaN(val)) val = 1;
      // Clamp between 0 and 1 for rooms
      max = Math.min(1, Math.max(0, val));
      card.dataset.max = max;
    }

    // 2. Cek apakah item ini ada di keranjang local
    let inCart = 0;
    if (window.MSUCart) {
      const name = titleEl ? titleEl.textContent.trim() : '';
      // Note: type 'ruang'
      const found = window.MSUCart.get().find(it => it.name === name && it.type === 'ruang');
      if (found) inCart = Number(found.quantity || 0);
    }

    // 3. Hitung sisa efektif
    let currentSisa = max - inCart;
    if (currentSisa < 0) currentSisa = 0;

    // 4. Update UI
    sisaEl.textContent = String(currentSisa);
    updateBadgeAndButtons(card, currentSisa);
  });
}

window.addEventListener('msu:cart-updated', initCards);

function updateBadgeAndButtons(card, sisa) {
  const badge = card.querySelector('.badge-status');
  const minusBtn = card.querySelector('.qty-btn[data-action="inc"]'); // − batal
  const plusBtn = card.querySelector('.qty-btn[data-action="dec"]'); // ＋ pilih

  if (badge) {
    if (sisa === 0) { badge.textContent = 'Habis'; badge.style.background = '#a94442'; }
    else { badge.textContent = 'Active'; badge.style.background = '#167c73'; }
  }
  if (minusBtn) minusBtn.disabled = (sisa >= 1);
  if (plusBtn) plusBtn.disabled = (sisa <= 0);
}

// ==== Modal konfirmasi tambah ====
let pendingCard = null;
const confirmModalEl = document.getElementById('confirmAddModal');
const confirmModal = confirmModalEl && typeof bootstrap !== 'undefined' ? new bootstrap.Modal(confirmModalEl) : null;
const confirmNameEl = document.getElementById('confirmName');
const confirmTypeEl = document.getElementById('confirmType');
const confirmThumbEl = document.getElementById('confirmThumb');

function openConfirm(card) {
  pendingCard = card;
  const name = card.querySelector('.item-title')?.textContent?.trim() || 'Ruang';
  const thumb = card.querySelector('.item-thumb img')?.getAttribute('src') || '';
  if (confirmNameEl) confirmNameEl.textContent = name;
  if (confirmTypeEl) confirmTypeEl.textContent = 'Ruang';
  if (confirmThumbEl) confirmThumbEl.src = thumb;
  if (confirmModal) confirmModal.show();
  else if (window.confirm(`Pilih "${name}"?`)) confirmAddNoRedirect();
}

document.getElementById('confirmAddBtn')?.addEventListener('click', () => {
  confirmAddNoRedirect();
  if (confirmModal) confirmModal.hide();
});

function confirmAddNoRedirect() {
  if (!pendingCard) return;
  const card = pendingCard; pendingCard = null;

  // pilih ruang (sisa -> 0)
  const sisaEl = card.querySelector('.sisa');
  let sisa = Number(sisaEl.textContent.trim() || '1');
  sisa = Math.max(0, sisa - 1);
  sisaEl.textContent = String(sisa);
  updateBadgeAndButtons(card, sisa);

  // masuk ke keranjang
  const name = card.querySelector('.item-title')?.textContent?.trim() || 'Ruang';
  const thumb = card.querySelector('.item-thumb img')?.getAttribute('src') || '';
  try {
    if (window.MSUCart) {
      MSUCart.add(name, 'ruang', thumb, 1, 1); // Pass max 1
      MSUCart.renderBadge();
    }
  } catch (e) { }
  showToastSuccess(`${name} ditambahkan ke keranjang.`);
}

// Klik tombol qty
document.addEventListener('click', (e) => {
  const btn = e.target.closest('.qty-btn'); if (!btn) return;
  const card = btn.closest('.item-card'); if (!card) return;

  const sisaEl = card.querySelector('.sisa');
  let sisa = Number(sisaEl.textContent.trim() || '1');
  const action = btn.dataset.action;

  if (action === 'dec') { // ＋ → pilih (konfirmasi)
    openConfirm(card);
    return;
  }

  // − : batalkan (kembalikan ketersediaan ke 1)
  sisa = Math.min(1, sisa + 1);
  sisaEl.textContent = String(sisa);
  updateBadgeAndButtons(card, sisa);
});


// Initialization
window.addEventListener('DOMContentLoaded', () => {
  initCards();
  initScheduleFilter(); // From catalogue-common.js
  initSearch(); // From catalogue-common.js
  initFabCheckout(); // From catalogue-common.js

  if (window.MSUCart) MSUCart.renderBadge();
});
