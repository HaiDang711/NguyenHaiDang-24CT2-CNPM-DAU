// ============================
// CẤU HÌNH TÀI KHOẢN NHẬN TIỀN (VietQR)
// Sửa 3 dòng dưới nếu đổi tài khoản ngân hàng.
// BANK_ID là mã ngân hàng theo VietQR (vd: vpbank, mbbank, vietcombank, techcombank, acb...).
// ============================
const BANK_ID = 'vpbank';
const BANK_NAME = 'VPBank';
const BANK_ACCOUNT = '0703562146';
const BANK_OWNER = 'NGUYEN HAI DANG';

function openPayModal(booking, onPaid) {
  const info = booking.booking_code.replace(/[^a-zA-Z0-9]/g, '').slice(0, 24);
  const qrUrl = `https://img.vietqr.io/image/${BANK_ID}-${BANK_ACCOUNT}-compact2.png`
    + `?amount=${Math.round(booking.total_price)}`
    + `&addInfo=${encodeURIComponent(info)}`
    + `&accountName=${encodeURIComponent(BANK_OWNER)}`;

  const overlay = document.createElement('div');
  overlay.className = 'pay-overlay';
  overlay.innerHTML = `
    <div class="pay-modal">
      <h3 style="margin:0 0 4px;">Thanh toán đơn ${escapeHtml(booking.booking_code)}</h3>
      <div class="pay-amount-label">Số tiền cần thanh toán</div>
      <div class="pay-amount">${moneyVN(booking.total_price)}</div>
      <img src="${qrUrl}" alt="Mã QR VietQR">
      <div class="pay-bank">
        🏦 Ngân hàng: <b>${BANK_NAME}</b><br>
        🔢 Số tài khoản: <b>${BANK_ACCOUNT}</b><br>
        👤 Chủ tài khoản: <b>${BANK_OWNER}</b><br>
        📝 Nội dung: <b>${info}</b>
      </div>
      <div class="pay-note">Quét mã bằng app ngân hàng bất kỳ — số tiền và nội dung đã được điền sẵn.</div>
      <button class="btn-mini-solid pay-confirm" style="margin-top:12px; width:100%; padding:12px;">Tôi đã chuyển khoản</button>
      <button class="btn-mini-outline pay-close" style="margin-top:8px; width:100%; padding:10px;">Đóng</button>
      <div class="pay-note">(Mô phỏng — hệ thống chưa tự động đối soát giao dịch với ngân hàng thật)</div>
    </div>`;
  document.body.appendChild(overlay);

  const close = () => overlay.remove();
  overlay.querySelector('.pay-close').addEventListener('click', close);
  overlay.addEventListener('click', e => { if (e.target === overlay) close(); });
  overlay.querySelector('.pay-confirm').addEventListener('click', async () => {
    if (!confirm(`Xác nhận bạn đã chuyển khoản ${moneyVN(booking.total_price)} cho đơn ${booking.booking_code}?`)) return;
    try {
      const res = await apiPost('pay.php', { booking_id: booking.id });
      close();
      alert(res.message + ' Vé điện tử của bạn đã được xuất!');
      onPaid();
    } catch (err) { alert(err.message); }
  });
}

document.addEventListener('DOMContentLoaded', async () => {

  // ============================
  // KIỂM TRA ĐĂNG NHẬP
  // ============================

  let me;

  try {
    me = await apiGet('me.php');

  } catch (error) {
    console.error(error);

    alert('Không thể kết nối đến máy chủ.');

    window.location.href = 'index.html';
    return;
  }


  // Nếu chưa đăng nhập
  if (!me.logged_in || !me.user) {

    alert('Bạn cần đăng nhập để xem trang cá nhân.');

    window.location.href = 'index.html';

    return;
  }


  // ============================
  // HIỂN THỊ THÔNG TIN USER
  // ============================

  const user = me.user;

  document.getElementById('profile-name').textContent =
    user.full_name || '-';

  document.getElementById('profile-email-short').textContent =
    user.email || '';

  document.getElementById('info-name').textContent =
    user.full_name || '-';

  document.getElementById('info-phone').textContent =
    user.phone || '-';

  document.getElementById('info-email').textContent =
    user.email || 'Chưa cập nhật';


  if (user.created_at) {

    const date = new Date(user.created_at);

    document.getElementById('info-created').textContent =
      date.toLocaleDateString('vi-VN');

  } else {

    document.getElementById('info-created').textContent = '-';

  }


  // ============================
  // LẤY LỊCH SỬ ĐẶT TOUR
  // ============================

  const bookingList =
    document.getElementById('booking-list');


  async function renderBookings(){
    try {

      const data = await apiGet('bookings.php');


      if (!data.bookings || data.bookings.length === 0) {

        bookingList.innerHTML = `
          <div class="empty-booking">
            <h3>Bạn chưa có đơn đặt tour nào</h3>
            <p>
              Hãy khám phá các tour du lịch và bắt đầu chuyến đi của bạn!
            </p>
          </div>
        `;

        return;
      }


      bookingList.innerHTML = '';


      data.bookings.forEach(booking => {

        const item = document.createElement('div');

        item.className = 'booking-item';


        const createdDate = booking.created_at
          ? new Date(booking.created_at)
              .toLocaleString('vi-VN')
          : '-';

        const canCancelRequest =
          ['pending', 'confirmed'].includes(booking.status) &&
          (!booking.departure_date || new Date(booking.departure_date) >= new Date(new Date().toDateString()));

        const canPay = booking.status === 'confirmed' && booking.payment_status === 'unpaid';
        const isPaid = booking.status === 'confirmed' && booking.payment_status === 'paid';
        const canReview = isPaid && !booking.review_id;

        // Giống mua vé xem phim online: thanh toán xong mới hiện vé
        let ticketHtml = '';
        if (isPaid) {
          ticketHtml = `
            <div class="ticket">
              <div class="ticket-main">
                <div class="ticket-brand">VÉ ĐIỆN TỬ · DANGBOOKING</div>
                <div class="ticket-title">${escapeHtml(booking.tour_name)}</div>
                <div class="ticket-sub">${escapeHtml(booking.package_name)} · ${escapeHtml(booking.tour_place)}</div>
                <div class="ticket-grid">
                  <div><small>Khách hàng</small><b>${escapeHtml(booking.guest_name)}</b></div>
                  <div><small>Khởi hành</small><b>${booking.departure_date ? new Date(booking.departure_date).toLocaleDateString('vi-VN') : 'Chưa có'}</b></div>
                  <div><small>Số khách</small><b>${escapeHtml(String(booking.guest_count ?? 1))}</b></div>
                  <div><small>Phương tiện</small><b>${escapeHtml(booking.assigned_vehicle || 'Nhân viên sẽ liên hệ')}</b></div>
                </div>
              </div>
              <div class="ticket-stub">
                <span class="ticket-paid">ĐÃ THANH TOÁN</span>
                <div class="ticket-code">${escapeHtml(booking.booking_code)}</div>
                <div class="ticket-barcode"></div>
                <div style="font-size:13px; font-weight:700;">${moneyVN(booking.total_price)}</div>
              </div>
            </div>`;
        } else if (booking.status === 'confirmed') {
          ticketHtml = `<div class="ticket-locked">🔒 Đơn đã được duyệt. Hãy thanh toán <b>${moneyVN(booking.total_price)}</b> để nhận vé điện tử.</div>`;
        } else if (booking.status === 'pending') {
          ticketHtml = `<div class="ticket-locked">⏳ Đơn đang chờ nhân viên duyệt. Sau khi được duyệt, bạn thanh toán để nhận vé điện tử.</div>`;
        }

        item.innerHTML = `

          <div class="booking-head">

            <div>

              <h3 class="booking-title">
                ${escapeHtml(booking.tour_name)}
              </h3>

              <div>
                ${escapeHtml(booking.package_name)}
              </div>

            </div>


            <div>

              <div class="booking-code">
                ${escapeHtml(booking.booking_code)}
              </div>

              <br>

              <span class="status ${isPaid ? 'ticketed' : (booking.status === 'confirmed' || booking.status === 'pending' ? 'waiting' : '')}">
                ${getStatusText(booking)}
              </span>

            </div>

          </div>


          <div class="booking-details">

            <div>
              📍 ${escapeHtml(booking.tour_place)}
            </div>

            <div>
              🧭 ${escapeHtml(booking.tour_address)}
            </div>

            <div>
              💳 ${escapeHtml(booking.payment_method)}
              (${booking.payment_status === 'paid' ? 'Đã thanh toán' : 'Chưa thanh toán'})
            </div>

            <div>
              🗓️ Khởi hành: ${booking.departure_date ? new Date(booking.departure_date).toLocaleDateString('vi-VN') : 'Chưa có'}
            </div>

            <div>
              👥 Số người: ${escapeHtml(String(booking.guest_count ?? 1))}
            </div>

            <div>
              📅 Đặt lúc: ${createdDate}
            </div>

            <div class="booking-price">
              💰 ${moneyVN(booking.total_price)}
            </div>

            ${booking.status === 'cancel_requested' ? `
              <div style="color:#B4551A;">
                ⚠️ Đã gửi yêu cầu hủy/đổi${booking.cancel_reason ? ': ' + escapeHtml(booking.cancel_reason) : ''} — đang chờ nhân viên xử lý.
              </div>` : ''}

          </div>

          ${ticketHtml}

          <div class="booking-actions" style="margin-top:12px; display:flex; gap:8px; flex-wrap:wrap;"></div>
          <div class="review-box" style="margin-top:10px; display:none;"></div>
          <div class="pay-box" style="margin-top:10px; display:none;"></div>

        `;

        const actionsEl = item.querySelector('.booking-actions');

        // FR-09: yêu cầu hủy/đổi tour
        if (canCancelRequest) {
          const btn = document.createElement('button');
          btn.className = 'btn-mini-outline';
          btn.textContent = 'Yêu cầu hủy/đổi';
          btn.addEventListener('click', async () => {
            const reason = prompt('Vui lòng cho biết lý do muốn hủy/đổi tour này:');
            if (reason === null) return;
            try {
              const res = await apiPost('cancel-request.php', { booking_id: booking.id, reason });
              alert(res.message);
              renderBookings();
            } catch (err) { alert(err.message); }
          });
          actionsEl.appendChild(btn);
        }

        // FR-10: thanh toán — mở cửa sổ VietQR hiện đúng số tiền của đơn.
        // Vẫn là MÔ PHỎNG: khách chuyển khoản xong tự bấm "Tôi đã chuyển khoản" để xuất vé.
        if (canPay) {
          const btn = document.createElement('button');
          btn.className = 'btn-mini-solid';
          btn.textContent = 'Thanh toán ngay';
          btn.addEventListener('click', () => openPayModal(booking, renderBookings));
          actionsEl.appendChild(btn);
        }

        // FR-12: đánh giá dịch vụ
        if (canReview) {
          const reviewBox = item.querySelector('.review-box');
          const btn = document.createElement('button');
          btn.className = 'btn-mini-outline';
          btn.textContent = 'Đánh giá tour này';
          btn.addEventListener('click', () => {
            reviewBox.style.display = reviewBox.style.display === 'none' ? 'block' : 'none';
          });
          actionsEl.appendChild(btn);

          reviewBox.innerHTML = `
            <div style="border:1px solid var(--line, #E4E0D4); border-radius:10px; padding:12px;">
              <div class="star-picker" style="font-size:22px; cursor:pointer; margin-bottom:8px;">
                ${[1,2,3,4,5].map(n => `<span data-star="${n}" style="color:#ccc;">★</span>`).join('')}
              </div>
              <textarea class="review-comment" placeholder="Cảm nhận của bạn về tour (không bắt buộc)" style="width:100%; padding:8px; border-radius:8px; border:1px solid #E4E0D4; font-family:inherit; min-height:60px;"></textarea>
              <button class="btn-mini-solid submit-review" style="margin-top:8px;">Gửi đánh giá</button>
            </div>
          `;

          let selectedStars = 0;
          const stars = reviewBox.querySelectorAll('[data-star]');
          stars.forEach(s => {
            s.addEventListener('click', () => {
              selectedStars = parseInt(s.dataset.star, 10);
              stars.forEach(x => x.style.color = parseInt(x.dataset.star, 10) <= selectedStars ? '#F2A93B' : '#ccc');
            });
          });

          reviewBox.querySelector('.submit-review').addEventListener('click', async () => {
            if (!selectedStars) { alert('Vui lòng chọn số sao đánh giá.'); return; }
            try {
              const res = await apiPost('review.php', {
                booking_id: booking.id,
                rating: selectedStars,
                comment: reviewBox.querySelector('.review-comment').value.trim()
              });
              alert(res.message);
              renderBookings();
            } catch (err) { alert(err.message); }
          });
        }

        bookingList.appendChild(item);

      });


    } catch (error) {

      console.error(error);

      bookingList.innerHTML = `
        <div class="empty-booking">
          Không thể tải lịch sử đặt tour.
        </div>
      `;

    }
  }

  renderBookings();

});


// ============================
// ĐĂNG XUẤT
// ============================

document.getElementById('logout-btn')
.addEventListener('click', async () => {

  const confirmLogout =
    confirm('Bạn có chắc chắn muốn đăng xuất không?');


  if (!confirmLogout) return;


  try {

    const data = await apiPost('logout.php', {});


    if (data.success) {

      alert('Bạn đã đăng xuất.');

      window.location.href = 'index.html';

    }

  } catch (error) {

    alert(error.message);

  }

});


// ============================
// HIỂN THỊ TRẠNG THÁI
// ============================

function getStatusText(booking) {

  if (booking.status === 'confirmed') {
    return booking.payment_status === 'paid'
      ? 'Đã thanh toán ✓ Vé đã xuất'
      : 'Đã duyệt – chờ thanh toán';
  }

  const statuses = {
    pending: 'Đang chờ duyệt',
    cancelled: 'Đã hủy',
    cancel_requested: 'Đã gửi yêu cầu hủy/đổi',
    completed: 'Hoàn thành'
  };

  return statuses[booking.status] || booking.status || 'Không xác định';

}


// ============================
// CHỐNG HTML XSS ĐƠN GIẢN
// ============================

function escapeHtml(value) {

  if (value === null || value === undefined) {
    return '';
  }


  const div = document.createElement('div');

  div.textContent = value;

  return div.innerHTML;

}
