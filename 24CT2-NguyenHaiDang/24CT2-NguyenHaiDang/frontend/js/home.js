async function loadTours(){
  const grid = document.getElementById('dest-grid');
  const place = document.getElementById('filter-place').value;
  const sort = document.getElementById('filter-sort').value;
  const qs = new URLSearchParams();
  if (place) qs.set('place', place);
  if (sort) qs.set('sort', sort);

  try {
    const data = await apiGet(`tours.php?${qs.toString()}`);
    grid.innerHTML = '';

    if (!document.getElementById('filter-place').dataset.filled) {
      data.places.forEach(p => {
        const opt = document.createElement('option');
        opt.value = p; opt.textContent = p;
        document.getElementById('filter-place').appendChild(opt);
      });
      document.getElementById('filter-place').dataset.filled = '1';
    }

    if (!data.tours.length) {
      grid.innerHTML = '<p style="color:var(--ink-600)">Không tìm thấy tour phù hợp.</p>';
      return;
    }

    data.tours.forEach(t => {
      const card = document.createElement('button');
      card.className = 'dest-card';
      card.innerHTML = `
        <div class="dest-img" style="background:linear-gradient(135deg, ${t.color_from}, ${t.color_to})">
          <span class="dest-badge">${t.place}</span>
        </div>
        <div class="dest-body">
          <div class="place">${t.name}</div>
          <div class="rating">★ ${t.rating}</div>
          <div class="price">Từ <b>${moneyVN(t.price_from)}</b> / khách</div>
        </div>`;
      card.addEventListener('click', () => location.href = `tour.html?id=${t.id}`);
      grid.appendChild(card);
    });
  } catch(e) {
    grid.innerHTML = '<p style="color:#C4402E">Không tải được danh sách tour. Kiểm tra XAMPP đã bật MySQL/Apache và đã chạy đủ script nâng cấp chưa.</p>';
  }
}

async function loadNews(){
  const grid = document.getElementById('news-grid');
  try {
    const data = await apiGet('news.php');
    if (!data.success || !data.items.length) {
      grid.innerHTML = '<p style="color:var(--ink-600)">Chưa tải được tin tức lúc này, thử lại sau nhé.</p>';
      return;
    }
    grid.innerHTML = data.items.map(item => `
      <a class="news-card" href="${item.link}" target="_blank" rel="noopener">
        <div class="news-img" style="${item.image ? `background-image:url('${item.image}')` : ''}"></div>
        <div class="news-body">
          <div class="ntitle">${item.title}</div>
          <div class="nsum">${item.summary}</div>
          <div class="nsrc">Nguồn: ${data.source} →</div>
        </div>
      </a>
    `).join('');
  } catch(e) {
    grid.innerHTML = '<p style="color:var(--ink-600)">Chưa tải được tin tức lúc này, thử lại sau nhé.</p>';
  }
}

document.addEventListener('DOMContentLoaded', async () => {
  wireLoginModal();
  refreshUserChip();

  if (sessionStorage.getItem('justRegistered')) {
    sessionStorage.removeItem('justRegistered');
    toast('Đăng ký thành công! Vui lòng đăng nhập để tiếp tục.');
    setTimeout(() => window.openLoginModal && window.openLoginModal(), 500);
  }

  document.getElementById('filter-place').addEventListener('change', loadTours);
  document.getElementById('filter-sort').addEventListener('change', loadTours);
  loadTours();
  loadNews();
});
