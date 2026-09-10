// Đường dẫn tương đối tới backend/api — không phụ thuộc tên thư mục project,
// vì file này luôn được load từ các trang nằm trực tiếp trong frontend/.
const API_BASE = '../backend/api';

async function apiGet(path) {
  const res = await fetch(`${API_BASE}/${path}`, {
    credentials: 'include',
    headers: { 'ngrok-skip-browser-warning': 'true' }
  });

  const data = await res.json();

  if (!res.ok) {
    throw new Error(data.error || 'Đã có lỗi xảy ra.');
  }

  return data;
}
async function apiPost(path, body) {
  const res = await fetch(`${API_BASE}/${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'ngrok-skip-browser-warning': 'true' },
    credentials: 'include',
    body: JSON.stringify(body)
  });
  const data = await res.json();
  if (!res.ok) throw new Error(data.error || 'Đã có lỗi xảy ra.');
  return data;
}
function moneyVN(n){ return Number(n).toLocaleString('vi-VN') + 'đ'; }

function toast(msg){
  let t = document.getElementById('toast');
  if(!t){
    t = document.createElement('div');
    t.id = 'toast'; t.className = 'toast';
    t.innerHTML = '<span class="dot"></span><span id="toast-text"></span>';
    document.body.appendChild(t);
  }
  document.getElementById('toast-text').textContent = msg;
  t.classList.add('show');
  setTimeout(()=> t.classList.remove('show'), 2600);
}

// ---- login modal: dùng chung trên mọi trang (được include qua header.js) ----
function wireLoginModal(onLoginSuccess){
  const overlay = document.getElementById('login-overlay');
  const openBtn = document.getElementById('open-login');
  const closeBtn = document.getElementById('close-login');
  const form = document.getElementById('login-form');
  if(!overlay) return;

  function open(){ overlay.classList.add('open'); document.getElementById('login-id').focus(); }
  function close(){ overlay.classList.remove('open'); document.getElementById('login-error').style.display='none'; }
  window.openLoginModal = open;
  window.closeLoginModal = close;

  if(openBtn) openBtn.addEventListener('click', open);
  if(closeBtn) closeBtn.addEventListener('click', close);
  overlay.addEventListener('click', e => { if(e.target === overlay) close(); });

  form.addEventListener('submit', async e => {
    e.preventDefault();
    const identifier = document.getElementById('login-id').value.trim();
    const password = document.getElementById('login-pass').value;
    const errEl = document.getElementById('login-error');
    errEl.style.display = 'none';

    // 1) Thử đăng nhập như KHÁCH HÀNG trước (trường hợp phổ biến nhất)
    try {
      const data = await apiPost('login.php', { identifier, password });
      form.reset();
      close(); // luôn thu gọn lại hoàn toàn, không che nội dung phía sau
      toast('Đăng nhập thành công. Chúc bạn có chuyến đi vui vẻ!');
      if(onLoginSuccess) onLoginSuccess(data.user);
      else refreshUserChip();
      return;
    } catch(customerErr){
      // Không phải tài khoản khách hàng (hoặc sai mật khẩu) -> thử tiếp tài khoản QUẢN TRỊ (admin/nhân viên)
      try {
        const res = await fetch('../backend/admin/api/login.php', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json', 'ngrok-skip-browser-warning': 'true' },
          credentials: 'include',
          body: JSON.stringify({ username: identifier, password })
        });
        const adminData = await res.json();
        if (!res.ok) throw new Error(adminData.error || 'Sai thông tin đăng nhập.');

        form.reset();
        toast(`Xin chào ${adminData.admin.full_name} — đang chuyển đến trang quản trị...`);
        setTimeout(() => { location.href = 'admin/dashboard.html'; }, 600);
        return;
      } catch(adminErr){
        // Cả 2 đều sai -> hiện lỗi chung (không lộ ra là tài khoản khách hàng hay quản trị sai)
        errEl.textContent = 'Số điện thoại/tên đăng nhập hoặc mật khẩu không đúng.';
        errEl.style.display = 'block';
      }
    }
  });
}

async function refreshUserChip() {
  try {
    const data = await apiGet('me.php');

    const chip = document.getElementById('user-chip');
    const openBtn = document.getElementById('open-login');
    const regBtn = document.getElementById('go-register');

    if (!chip) return data;

    if (data.logged_in && data.user) {
      const nameEl = document.getElementById('user-chip-name');

      nameEl.textContent =
        data.user.full_name.split(' ').slice(-1)[0];

      chip.style.display = 'flex';
      chip.style.cursor = 'pointer';

      if (openBtn) {
        openBtn.style.display = 'none';
      }

      if (regBtn) {
        regBtn.style.display = 'none';
      }

      // Bấm vào tài khoản để vào trang cá nhân
      chip.onclick = () => {
        window.location.href = 'profile.html';
      };

    } else {
      chip.style.display = 'none';

      if (openBtn) {
        openBtn.style.display = '';
      }

      if (regBtn) {
        regBtn.style.display = '';
      }
    }

    return data;

  } catch (error) {
    console.error('Không thể kiểm tra đăng nhập:', error);

    return {
      logged_in: false,
      user: null
    };
  }
}
