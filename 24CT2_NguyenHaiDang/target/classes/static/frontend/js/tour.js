const tourId = new URLSearchParams(location.search).get('id');
let currentPackageId = null;

document.addEventListener('DOMContentLoaded', async () => {
  wireLoginModal(async () => {
    // đăng nhập xong từ trang này -> đi thẳng tới trang đặt tour với gói đã chọn
    if (currentPackageId) location.href = `booking.html?tour_id=${tourId}&package_id=${currentPackageId}`;
  });

  if (!tourId) { location.href = 'index.html'; return; }

  try {
    const data = await apiGet(`tour.php?id=${tourId}`);
    const t = data.tour;
    document.getElementById('tour-hero-img').style.background = `linear-gradient(135deg, ${t.color_from}, ${t.color_to})`;
    document.getElementById('tour-name').textContent = t.name;
    document.getElementById('tour-rating').textContent = `★ ${t.rating}`;
    document.getElementById('tour-address').textContent = t.address;
    document.getElementById('tour-desc').textContent = t.description;
    document.getElementById('side-price').textContent = moneyVN(t.price_from);
    document.getElementById('tour-highlights').innerHTML =
      t.highlights.map(a => `<div class="amenity"><span class="dot"></span>${a}</div>`).join('');

    const pkgList = document.getElementById('package-list');
    pkgList.innerHTML = '';
    data.packages.forEach(p => {
      const card = document.createElement('div');
      card.className = 'package-card';
      card.innerHTML = `
        <div><div class="rname">${p.name}</div><div class="rdesc">${p.description}</div></div>
        <div class="rprice">
          <div class="amt">${moneyVN(p.price)}</div>
          <div style="font-size:12px;color:var(--ink-600)">/ khách</div>
          <button class="choose-btn" type="button">Chọn gói</button>
        </div>`;
      card.querySelector('.choose-btn').addEventListener('click', async () => {
        currentPackageId = p.id;
        const me = await apiGet('me.php');
        if (!me.logged_in) {
          toast('Vui lòng đăng nhập để tiếp tục đặt tour.');
          window.openLoginModal();
          return;
        }
        location.href = `booking.html?tour_id=${tourId}&package_id=${p.id}`;
      });
      pkgList.appendChild(card);
    });

    // FR-12: hiển thị đánh giá dịch vụ
    const summaryEl = document.getElementById('review-summary');
    if (data.review_summary.total > 0) {
      summaryEl.innerHTML = `★ <b>${data.review_summary.average}</b>/5 — dựa trên ${data.review_summary.total} đánh giá`;
    }
    document.getElementById('review-list').innerHTML = data.reviews.map(r => `
      <div class="review-item">
        <div class="stars">${'★'.repeat(r.rating)}${'☆'.repeat(5 - r.rating)}</div>
        <div class="who">${r.full_name} — ${new Date(r.created_at).toLocaleDateString('vi-VN')}</div>
        ${r.comment ? `<div class="comment">${r.comment}</div>` : ''}
      </div>
    `).join('');
  } catch(e) {
    document.getElementById('tour-name').textContent = 'Không tải được tour';
  }
});
