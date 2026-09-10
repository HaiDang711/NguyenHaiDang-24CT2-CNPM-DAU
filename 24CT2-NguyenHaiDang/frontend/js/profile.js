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
        const canReview = booking.status === 'confirmed' && !booking.review_id;

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

              <span class="status">
                ${getStatusText(booking.status)}
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

          <div class="booking-actions" style="margin-top:12px; display:flex; gap:8px; flex-wrap:wrap;"></div>
          <div class="review-box" style="margin-top:10px; display:none;"></div>

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

        // FR-10: thanh toán (mô phỏng)
        if (canPay) {
          const btn = document.createElement('button');
          btn.className = 'btn-mini-solid';
          btn.textContent = 'Thanh toán ngay';
          btn.addEventListener('click', async () => {
            if (!confirm(`Xác nhận thanh toán ${moneyVN(booking.total_price)} cho đơn ${booking.booking_code}? (Đây là thanh toán mô phỏng, chưa nối cổng ngân hàng thật)`)) return;
            try {
              const res = await apiPost('pay.php', { booking_id: booking.id });
              alert(res.message);
              renderBookings();
            } catch (err) { alert(err.message); }
          });
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

function getStatusText(status) {

  const statuses = {

    confirmed: 'Đã xác nhận',

    pending: 'Đang chờ duyệt',

    cancelled: 'Đã hủy',

    cancel_requested: 'Đã gửi yêu cầu hủy/đổi',

    completed: 'Hoàn thành'

  };


  return statuses[status] || status || 'Không xác định';

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
