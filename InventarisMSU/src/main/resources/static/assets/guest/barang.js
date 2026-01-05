// barang.js - Depends on catalogue-common.js

// ==== Inisialisasi stok & tombol ====
function initCards() {
  document.querySelectorAll('.item-card').forEach(card => {
    const sisaEl = card.querySelector('.sisa');
    const titleEl = card.querySelector('.item-title');
    if (!sisaEl) return;

    // 1. Tentukan Max Stock (dari HTML server saat pertama load)
    let max = Number(card.dataset.max);
    if (Number.isNaN(max) || !card.hasAttribute('data-max')) {
      // Belum ada dataset, berarti ini load pertama/fresh
      max = Number(sisaEl.textContent.trim() || '0');
      if (Number.isNaN(max)) max = 0;
      card.dataset.max = max;
    }

    // 2. Cek apakah item ini ada di keranjang local
    let inCart = 0;
    if (window.MSUCart) {
      const name = titleEl ? titleEl.textContent.trim() : '';
      const found = window.MSUCart.get().find(it => it.name === name && it.type === 'barang');
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

// Update juga saat cart berubah (misal sync dari tab lain atau server)
window.addEventListener('msu:cart-updated', initCards);

function updateBadgeAndButtons(card, sisa) {
  const max = Number(card.dataset.max || 0);
  const badge = card.querySelector('.badge-status');
  const minusBtn = card.querySelector('.qty-btn[data-action="inc"]'); // − kembalikan sisa
  const plusBtn = card.querySelector('.qty-btn[data-action="dec"]'); // ＋ pilih (tambah ke cart)

  if (badge) {
    if (sisa === 0) { badge.textContent = 'Habis'; badge.style.background = '#a94442'; }
    else { badge.textContent = 'Active'; badge.style.background = '#167c73'; }
  }
  if (minusBtn) minusBtn.disabled = (sisa >= max);
  if (plusBtn) plusBtn.disabled = (sisa <= 0);
}

// ==== Modal konfirmasi tambah (seperti index) ====
let pendingCard = null;
const confirmedOnce = new Set(); // nama item yang sudah dikonfirmasi setidaknya sekali

const confirmModalEl = document.getElementById('confirmAddModal');
const confirmModal = confirmModalEl && typeof bootstrap !== 'undefined' ? new bootstrap.Modal(confirmModalEl) : null;
const confirmNameEl = document.getElementById('confirmName');
const confirmTypeEl = document.getElementById('confirmType');
const confirmThumbEl = document.getElementById('confirmThumb');

function openConfirm(card) {
  const name = card.querySelector('.item-title')?.textContent?.trim() || 'Item';

  // SKIP modal jika item ini sudah pernah dikonfirmasi sebelumnya
  if (confirmedOnce.has(name)) {
    pendingCard = card;
    confirmAddNoRedirect();
    return;
  }

  pendingCard = card;
  const thumb = card.querySelector('.item-thumb img')?.getAttribute('src') || '';
  if (confirmNameEl) confirmNameEl.textContent = name;
  if (confirmTypeEl) confirmTypeEl.textContent = 'Barang';
  if (confirmThumbEl) confirmThumbEl.src = thumb;
  if (confirmModal) confirmModal.show();
  else if (window.confirm(`Tambah "${name}" ke keranjang?`)) confirmAddNoRedirect();
}

document.getElementById('confirmAddBtn')?.addEventListener('click', () => {
  confirmAddNoRedirect();
  if (confirmModal) confirmModal.hide();
});

function confirmAddNoRedirect() {
  if (!pendingCard) return;
  const card = pendingCard; pendingCard = null;

  // simpan meta booking (biar pasti ke-save saat user mulai add)
  // Assuming inputs exist on page
  const dateStart = document.getElementById('filterDateStart');
  if (dateStart) {
    const meta = {
      start: dateStart.value || todayISO(),
      end: document.getElementById('filterDateEnd')?.value || todayISO(),
      time: document.getElementById('filterTime')?.value || '10:00',
      duration: document.getElementById('filterDuration')?.value || '2'
    };
    localStorage.setItem('msu_dates_v1', JSON.stringify(meta));
  }

  // Kurangi sisa 1
  const sisaEl = card.querySelector('.sisa');
  let sisa = Number(sisaEl.textContent.trim() || '0');
  sisa = Math.max(0, sisa - 1);
  sisaEl.textContent = String(sisa);
  updateBadgeAndButtons(card, sisa);

  // Masukkan ke keranjang
  const name = card.querySelector('.item-title')?.textContent?.trim() || 'Item';
  const thumb = card.querySelector('.item-thumb img')?.getAttribute('src') || '';
  const max = Number(card.dataset.max || 999); // Read max stock
  try {
    if (window.MSUCart) {
      MSUCart.add(name, 'barang', thumb, 1, max); // Pass max
      MSUCart.renderBadge();
    }
  } catch (e) { }

  // tandai sudah pernah konfirmasi → klik selanjutnya tidak muncul modal
  confirmedOnce.add(name);

  showToastSuccess(`${name} ditambahkan ke keranjang.`);
}

// Klik tombol qty
document.addEventListener('click', (e) => {
  const btn = e.target.closest('.qty-btn'); if (!btn) return;
  const card = btn.closest('.item-card'); if (!card) return;

  const sisaEl = card.querySelector('.sisa');
  let sisa = Number(sisaEl.textContent.trim() || '0');
  const max = Number(card.dataset.max || 0);
  const action = btn.dataset.action;

  if (action === 'dec') { // ＋ → konfirmasi (atau skip jika sudah pernah)
    openConfirm(card);
    return;
  }

  // − : kembalikan stok di tampilan (simulasi)
  sisa = Math.min(max, sisa + 1);
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
