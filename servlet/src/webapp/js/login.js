let selectedRole = 'TA';

function selectRole(role) {
  selectedRole = role;
  qsa('.role-btn').forEach(b => {
    b.classList.toggle('active', b.dataset.role === role);
  });
  const prefixes = { TA: 'TA001', MO: 'MO001', ADMIN: 'ADMIN001' };
  const input = qs('#userId');
  if (input) input.placeholder = 'e.g. ' + prefixes[role];
}

async function handleLogin(e) {
  e.preventDefault();
  const userId = qs('#userId').value.trim();
  const password = qs('#password').value;
  try {
    const user = await api('/login', {
      method: 'POST',
      body: JSON.stringify({ userId, password })
    });
    setUser(user);
    const dest = { TA: '/ta/dashboard.html', MO: '/mo/dashboard.html', ADMIN: '/admin/dashboard.html' };
    location.href = dest[user.role] || '/index.html';
  } catch (err) {
    toast(err.message || 'Login failed', 'error');
  }
}

function initLoginPage() {
  qsa('.role-btn').forEach(btn => {
    btn.addEventListener('click', () => selectRole(btn.dataset.role));
  });
  qs('#loginForm').addEventListener('submit', handleLogin);
}

const loggedIn = getUser();
if (loggedIn) {
  const dest = { TA: '/ta/dashboard.html', MO: '/mo/dashboard.html', ADMIN: '/admin/dashboard.html' };
  location.href = dest[loggedIn.role] || '/index.html';
} else if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initLoginPage);
} else {
  initLoginPage();
}
