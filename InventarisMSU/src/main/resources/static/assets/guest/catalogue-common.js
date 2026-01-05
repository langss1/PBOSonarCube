/**
 * catalogue-common.js
 * Shared logic for barang.js and ruangan.js (Guest Catalogue Pages)
 */

// ==== Reveal on scroll & drop-in ====
window.addEventListener('load', () => {
    document.querySelector('.drop-in')?.classList.add('show');
});

const ioObserver = new IntersectionObserver((entries, obs) => {
    entries.forEach(e => {
        if (e.isIntersecting) {
            e.target.classList.add('show');
            obs.unobserve(e.target);
        }
    });
}, { threshold: .12, rootMargin: "0px 0px -40px 0px" });

document.querySelectorAll('.reveal-up').forEach(el => ioObserver.observe(el));

// Tap animation (mobile)
function addTapAnimation(el) {
    el.addEventListener('touchstart', () => el.classList.add('tap-active'), { passive: true });
    el.addEventListener('touchend', () => setTimeout(() => el.classList.remove('tap-active'), 150));
    el.addEventListener('touchcancel', () => el.classList.remove('tap-active'));
}
document.querySelectorAll('.tap-anim').forEach(addTapAnimation);

// ==== Toast Utils ====
function showToastSuccess(text) {
    const wrap = document.getElementById('toastStack');
    if (!wrap) return alert(text);
    const id = 't' + Date.now();
    wrap.insertAdjacentHTML('beforeend', `
    <div id="${id}" class="toast align-items-center text-bg-success border-0" role="alert" aria-live="assertive" aria-atomic="true">
      <div class="d-flex">
        <div class="toast-body"><i class="bi bi-check2-circle me-1"></i>${text}</div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
      </div>
    </div>`);
    const el = document.getElementById(id);
    // Use bootstrap global if available, or assume loaded
    if (typeof bootstrap !== 'undefined') {
        const t = new bootstrap.Toast(el, { delay: 2200 });
        t.show();
        el.addEventListener('hidden.bs.toast', () => el.remove());
    }
}

// ==== Schedule Filter Logic ====
function todayISO() {
    const t = new Date(); t.setHours(0, 0, 0, 0);
    return t.toISOString().split('T')[0];
}

function initScheduleFilter(onCheckCallback) {
    const dateStart = document.getElementById('filterDateStart');
    const dateEnd = document.getElementById('filterDateEnd');
    const timeStart = document.getElementById('filterTime');
    const duration = document.getElementById('filterDuration');
    const btnCheck = document.getElementById('btnCheckAvailability');
    const resultText = document.getElementById('filterResultText');

    if (!dateStart || !btnCheck) return;

    // Set default date to today
    const today = todayISO();
    dateStart.min = today;
    dateStart.value = today;

    // Auto-set end date based on start date
    dateStart.addEventListener('change', () => {
        if (dateStart.value) dateEnd.value = dateStart.value;
    });

    // Load saved meta if any
    const saved = JSON.parse(localStorage.getItem('msu_dates_v1') || '{}');
    if (saved.start) {
        dateStart.value = saved.start;
        dateEnd.value = saved.end || saved.start;
    }
    if (saved.time) timeStart.value = saved.time;
    if (saved.duration) duration.value = saved.duration;

    // Initial state: Disable all item buttons
    toggleItemButtons(false);

    btnCheck.addEventListener('click', () => {
        if (!dateStart.value || !timeStart.value || !duration.value) {
            alert('Mohon lengkapi tanggal, jam mulai, dan durasi peminjaman.');
            return;
        }

        // "Check" logic (simulation - usually calling backend here)
        toggleItemButtons(true);

        // Update UI text
        const d = new Date(dateStart.value).toLocaleDateString('id-ID', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });
        if (resultText) {
            resultText.innerHTML = `<span class="text-success fw-bold"><i class="bi bi-check-circle me-1"></i>Jadwal dipilih:</span> ${d} (Jam ${timeStart.value}, ${duration.value} jam)`;
            resultText.classList.remove('text-muted');
        }

        // Save meta
        const meta = {
            start: dateStart.value,
            end: dateEnd.value,
            time: timeStart.value,
            duration: duration.value
        };
        localStorage.setItem('msu_dates_v1', JSON.stringify(meta));

        // Callback if any (e.g., scroll to grid)
        if (typeof onCheckCallback === 'function') onCheckCallback();

        // Scroll to items
        document.getElementById('itemsGrid')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });

    // AUTO-CHECK if data exists
    if (dateStart.value && timeStart.value && duration.value) {
        setTimeout(() => btnCheck.click(), 50);
    }
}

function toggleItemButtons(enable) {
    const btns = document.querySelectorAll('.qty-btn');
    btns.forEach(btn => {
        btn.disabled = !enable;
        const card = btn.closest('.item-card');
        if (card) {
            if (!enable) {
                card.style.opacity = '0.6';
                card.style.pointerEvents = 'none';
            } else {
                card.style.opacity = '1';
                card.style.pointerEvents = 'auto';
            }
        }
    });

    // Re-run update logic if enabling
    if (enable && typeof updateBadgeAndButtons === 'function') {
        document.querySelectorAll('.item-card').forEach(card => {
            const sisaEl = card.querySelector('.sisa');
            const sisa = Number(sisaEl.textContent.trim() || '0');
            updateBadgeAndButtons(card, sisa);
        });
    }
}

// ==== Search Logic ====
function initSearch(gridId = 'itemsGrid', searchInputId = 'searchInput', clearBtnId = 'clearSearch', emptyStateId = 'emptyState') {
    const q = document.getElementById(searchInputId);
    const clearBtn = document.getElementById(clearBtnId);
    const gridEl = document.getElementById(gridId);
    const emptyState = document.getElementById(emptyStateId);

    function applyFilter() {
        const term = (q?.value || '').trim().toLowerCase();
        if (!gridEl) return;
        let visible = 0;
        gridEl.querySelectorAll('.col').forEach(col => {
            const title = col.querySelector('.item-title')?.textContent?.toLowerCase() || '';
            const match = title.includes(term);
            col.style.display = match ? '' : 'none';
            if (match) visible++;
        });
        emptyState?.classList.toggle('d-none', visible > 0);
    }

    q?.addEventListener('input', applyFilter);
    clearBtn?.addEventListener('click', () => { if (q) { q.value = ''; applyFilter(); } });

    // Initial apply
    applyFilter();
}

// ==== Common Card Expansion Logic ====
document.addEventListener('click', (e) => {
    const card = e.target.closest('.item-card'); if (!card) return;
    if (e.target.closest('.qty-btn')) return;
    const grid = card.closest('.items-grid');
    if (!grid) return;

    const already = card.classList.contains('is-expanded');
    grid.classList.remove('has-expanded');
    grid.querySelectorAll('.item-card').forEach(c => c.classList.remove('is-expanded'));
    if (!already) { card.classList.add('is-expanded'); grid.classList.add('has-expanded'); }
});

// ==== FAB Logic ====
function initFabCheckout() {
    document.getElementById('fabCheckout')?.addEventListener('click', () => {
        // pastikan meta booking tersimpan terbaru (optional, sometimes inputs are outside form)
        // For simple redirect:
        const c = (window.MSUCart ? MSUCart.count() : 0);
        if (c <= 0) return;
        window.location.href = '/form?from=fab';
    });
}
