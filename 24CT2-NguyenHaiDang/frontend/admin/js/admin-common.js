// Đường dẫn tương đối tới backend/admin/api — không phụ thuộc tên thư mục project,
// vì file này luôn được load từ các trang nằm trong frontend/admin/.
const ADMIN_API = '../../backend/admin/api';

async function adminGet(path){
  const res = await fetch(`${ADMIN_API}/${path}`, { credentials: 'include' });
  const data = await res.json();
  if (!res.ok) throw new Error(data.error || 'Đã có lỗi xảy ra.');
  return data;
}
async function adminPost(path, body){
  const res = await fetch(`${ADMIN_API}/${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
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
  if(!t){ t = document.createElement('div'); t.id='toast'; t.className='toast'; document.body.appendChild(t); }
  t.textContent = msg;
  t.classList.add('show');
  setTimeout(()=> t.classList.remove('show'), 2600);
}

const NAV_ITEMS = [
  { id:'dashboard', label:'Tổng quan',      href:'dashboard.html', roles:['admin','staff'] },
  { id:'bookings',  label:'Đơn đặt phòng',  href:'bookings.html',  roles:['admin','staff'] },
  { id:'users',     label:'Người dùng',     href:'users.html',     roles:['admin','staff'] },
  { id:'tours',     label:'Quản lý Tour',      href:'tours.html',     roles:['admin','staff'] },
  { id:'staff',     label:'Tài khoản nhân viên', href:'staff.html', roles:['admin'] },
];

// Gọi ở đầu mỗi trang quản trị (trừ login.html). activeId = id trong NAV_ITEMS.
// requiredRoles: nếu trang chỉ dành riêng cho 1 vai trò (vd staff.html chỉ admin).
async function bootAdminPage(activeId, requiredRoles = ['admin','staff']){
  let me;
  try { me = await adminGet('me.php'); }
  catch(e){ location.href = 'login.html'; return null; }

  if (!me.logged_in) { location.href = 'login.html'; return null; }
  const role = me.admin.role;

  const shell = document.getElementById('admin-shell');
  const navHtml = NAV_ITEMS
    .filter(item => item.roles.includes(role))
    .map(item => `<a href="${item.href}" class="${item.id === activeId ? 'active' : ''}">${item.label}</a>`)
    .join('');

  const sidebar = document.createElement('div');
  sidebar.className = 'sidebar';
  sidebar.innerHTML = `
    <div class="logo"><span class="mark"></span> ViVu Admin</div>
    <span class="role-tag">${role === 'admin' ? 'Quản trị viên' : 'Nhân viên'}</span>
    <nav>${navHtml}</nav>
    <div class="bottom">
      <a href="../index.html" class="logout-btn" style="color:#B9D7D4; margin-bottom:4px;">↗ Về trang web chính</a>
      <div class="who">${me.admin.full_name}</div>
      <button class="logout-btn" id="admin-logout">↩ Đăng xuất</button>
    </div>`;
  shell.prepend(sidebar);
  document.getElementById('admin-logout').addEventListener('click', async () => {
    await adminPost('logout.php', {});
    location.href = 'login.html';
  });

  if (!requiredRoles.includes(role)) {
    document.querySelector('.admin-main').innerHTML =
      '<div class="access-denied"><h2>Không có quyền truy cập</h2><p>Trang này chỉ dành cho quản trị viên.</p></div>';
    return null;
  }
  return me.admin;
}
