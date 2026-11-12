const params = new URLSearchParams(location.search);
const tourId = params.get('tour_id');
const packageId = params.get('package_id');
let currentPackage = null;

document.getElementById('back-tour').href = `tour.html?id=${tourId}`;

document.addEventListener('DOMContentLoaded', async () => {
  // Không cho chọn ngày khởi hành trong quá khứ (kiểm tra dữ liệu ngày hợp lệ)
  document.getElementById('bk-departure').min = new Date().toISOString().split('T')[0];

  const me = await apiGet('me.php');
  if (!me.logged_in) { location.href = `tour.html?id=${tourId}`; return; }
  if (me.user) {
    document.getElementById('bk-name').value = me.user.full_name || '';
    document.getElementById('bk-phone').value = me.user.phone || '';
    document.getElementById('bk-email').value = me.user.email || '';
  }

  const data = await apiGet(`tour.php?id=${tourId}`);
  const pkg = data.packages.find(p => String(p.id) === packageId);
  if (!pkg) { location.href = `tour.html?id=${tourId}`; return; }
  currentPackage = pkg;
  document.getElementById('sum-tour').textContent = data.tour.name;
  document.getElementById('sum-package').textContent = pkg.name;
  document.getElementById('sum-total').textContent = moneyVN(pkg.price);

  function syncSummary(){
    const dep = document.getElementById('bk-departure').value;
    document.getElementById('sum-departure').textContent = dep ? new Date(dep).toLocaleDateString('vi-VN') : '—';
    document.getElementById('sum-guests').textContent = document.getElementById('bk-guests').value || '—';
  }
  document.getElementById('bk-departure').addEventListener('change', syncSummary);
  document.getElementById('bk-guests').addEventListener('input', syncSummary);
  syncSummary();
});

document.getElementById('booking-form').addEventListener('submit', async e => {
  e.preventDefault();
  const errEl = document.getElementById('bk-error');
  errEl.style.display = 'none';
  const body = {
    tour_id: tourId,
    package_id: packageId,
    guest_name: document.getElementById('bk-name').value.trim(),
    guest_phone: document.getElementById('bk-phone').value.trim(),
    guest_email: document.getElementById('bk-email').value.trim(),
    guest_count: document.getElementById('bk-guests').value,
    departure_date: document.getElementById('bk-departure').value,
    vehicle_request: document.getElementById('bk-vehicle').value.trim(),
    payment_method: document.querySelector('input[name="pay"]:checked').value
  };
  try {
    const res = await apiPost('book.php', body);
    location.href = `confirm.html?code=${res.booking_code}`;
  } catch(err) {
    errEl.textContent = err.message;
    errEl.style.display = 'block';
  }
});
