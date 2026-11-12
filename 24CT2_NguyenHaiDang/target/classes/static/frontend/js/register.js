document.getElementById('register-form').addEventListener('submit', async e => {
  e.preventDefault();
  const errEl = document.getElementById('reg-error');
  errEl.style.display = 'none';
  const body = {
    full_name: document.getElementById('reg-name').value.trim(),
    phone: document.getElementById('reg-phone').value.trim(),
    email: document.getElementById('reg-email').value.trim(),
    password: document.getElementById('reg-pass').value
  };
  try {
    await apiPost('register.php', body);
    // đăng ký xong -> quay về trang chủ và yêu cầu đăng nhập lại
    sessionStorage.setItem('justRegistered', '1');
    location.href = 'index.html';
  } catch(err) {
    errEl.textContent = err.message;
    errEl.style.display = 'block';
  }
});
