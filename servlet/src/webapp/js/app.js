const API = '/api';
/** var so inline page scripts can read the logged-in user after app.js loads */
var user = null;

async function api(path, options = {}) {
  const resp = await fetch(API + path, {
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    ...options
  });
  const data = await resp.json();
  if (resp.status === 401) {
    clearUser();
    user = null;
    if (!location.pathname.endsWith('/index.html') && location.pathname !== '/') {
      location.href = '/index.html';
    }
    throw new Error(data.error || 'Not logged in');
  }
  if (!resp.ok) throw new Error(data.error || 'Request failed');
  return data;
}

/** Cached user snapshot (may be stale); prefer global `user` after requireLogin. */
function getUser() {
  if (user) return user;
  try {
    const u = sessionStorage.getItem('user');
    return u ? JSON.parse(u) : null;
  } catch {
    sessionStorage.removeItem('user');
    return null;
  }
}

function setUser(u) {
  sessionStorage.setItem('user', JSON.stringify(u));
  user = u;
}

function clearUser() {
  sessionStorage.removeItem('user');
  user = null;
}

/** Reload current user from server session and update cache. */
async function refreshUser() {
  const current = await api('/session');
  setUser(current);
  return current;
}

async function requireLogin(role) {
  try {
    const current = await refreshUser();
    if (role && current.role !== role) {
      clearUser();
      location.href = '/index.html';
      return null;
    }
    return current;
  } catch {
    clearUser();
    location.href = '/index.html';
    return null;
  }
}

function dashboardUrl(role) {
  const paths = {
    TA: '/ta/dashboard.html',
    MO: '/mo/dashboard.html',
    ADMIN: '/admin/dashboard.html'
  };
  return paths[role] || '/index.html';
}

const PAGE_BY_PATH = {
  '/ta/dashboard.html': 'dashboard',
  '/ta/profile.html': 'profile',
  '/ta/positions.html': 'browse-positions',
  '/ta/applications.html': 'applications',
  '/mo/dashboard.html': 'dashboard',
  '/mo/publish.html': 'publish',
  '/mo/review.html': 'review',
  '/mo/offers.html': 'offers',
  '/admin/dashboard.html': 'dashboard',
  '/admin/approve.html': 'approve',
  '/admin/stats.html': 'stats',
  '/admin/workload.html': 'workload',
  '/admin/users.html': 'users'
};

function roleFromPath() {
  const p = location.pathname;
  if (p.includes('/mo/')) return 'MO';
  if (p.includes('/admin/')) return 'ADMIN';
  if (p.includes('/ta/')) return 'TA';
  const u = getUser() || user;
  return u ? u.role : null;
}

function pageFromPath() {
  const path = location.pathname.replace(/\/+$/, '');
  if (PAGE_BY_PATH[path]) return PAGE_BY_PATH[path];
  const matched = Object.keys(PAGE_BY_PATH).find(k => path.endsWith(k));
  if (matched) return PAGE_BY_PATH[matched];
  const file = path.split('/').pop() || '';
  if (file === 'dashboard.html') return 'dashboard';
  return file.replace(/\.html$/, '');
}

function removeMainBackLinks() {
  document.querySelectorAll('.main-content .back-home').forEach(el => el.remove());
}

function setupAppChrome(role, activePage) {
  const pageId = activePage || pageFromPath();
  const r = role || roleFromPath();
  if (!r) return;
  renderSidebar(r, pageId || '');
  removeMainBackLinks();
}

async function bootstrapPage(role, fn, activePage) {
  const pageId = activePage || pageFromPath();
  initAppShell();
  paintSidebarFromCache(role, pageId);

  try {
    const u = await requireLogin(role);
    if (!u) return;
    setupAppChrome(role, pageId);
    if (fn) await fn();
  } catch (err) {
    console.error(err);
    toast(err.message || 'Failed to load page', 'error');
  } finally {
    if (user) setupAppChrome(role, pageId);
  }
}

function initAppShell() {
  if (!document.querySelector('.app-layout')) return;
  removeMainBackLinks();
}

/** Show sidebar immediately from sessionStorage while /session is in flight */
function paintSidebarFromCache(role, activePage) {
  const cached = getUser();
  if (!cached) return;
  user = cached;
  const r = role || cached.role;
  const page = activePage || pageFromPath();
  if (r && page) renderSidebar(r, page);
}

function ensureSidebarVisible() {
  const sidebar = document.querySelector('.app-layout .sidebar');
  if (!sidebar || !user) return;
  const role = roleFromPath();
  const page = pageFromPath();
  if (role && page) renderSidebar(role, page);
}

function logout() {
  api('/logout', { method: 'POST' }).catch(() => {});
  clearUser();
  location.href = '/index.html';
}

function toast(msg, type = 'success') {
  let container = document.querySelector('.toast-container');
  if (!container) {
    container = document.createElement('div');
    container.className = 'toast-container';
    document.body.appendChild(container);
  }
  const el = document.createElement('div');
  el.className = 'toast toast-' + type;
  el.textContent = msg;
  container.appendChild(el);
  setTimeout(() => el.remove(), 3000);
}

function badgeClass(status) {
  const map = {
    'APPROVED': 'badge-success', 'PASSED': 'badge-success',
    'PENDING': 'badge-warning',
    'REJECTED': 'badge-danger', 'FAILED': 'badge-danger',
    'CLOSED': 'badge-gray'
  };
  return map[status] || 'badge-info';
}

function initials(name) {
  if (!name) return '?';
  const parts = name.trim().split(/\s+/);
  if (parts.length >= 2) return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  return name.substring(0, 2).toUpperCase();
}

function renderSidebar(role, activePage) {
  const current = user || getUser();
  const name = current ? current.name : '';
  const avatarColors = ['#2563EB', '#7C3AED', '#059669', '#D97706', '#DC2626'];
  const color = avatarColors[(name.length || 0) % avatarColors.length];

  const menus = {
    TA: [
      { section: 'Main' },
      { icon: '📊', label: 'Dashboard', href: '/ta/dashboard.html', id: 'dashboard' },
      { icon: '👤', label: 'My Profile', href: '/ta/profile.html', id: 'profile' },
      { section: 'Recruitment' },
      { icon: '💼', label: 'Browse Positions', href: '/ta/positions.html', id: 'browse-positions' },
      { icon: '📋', label: 'My Applications', href: '/ta/applications.html', id: 'applications' },
    ],
    MO: [
      { section: 'Main' },
      { icon: '📊', label: 'Dashboard', href: '/mo/dashboard.html', id: 'dashboard' },
      { section: 'Management' },
      { icon: '📝', label: 'Publish Position', href: '/mo/publish.html', id: 'publish' },
      { icon: '👥', label: 'Review Applications', href: '/mo/review.html', id: 'review' },
      { icon: '📄', label: 'Offer Letters', href: '/mo/offers.html', id: 'offers' },
    ],
    ADMIN: [
      { section: 'Main' },
      { icon: '📊', label: 'Dashboard', href: '/admin/dashboard.html', id: 'dashboard' },
      { section: 'Management' },
      { icon: '✅', label: 'Approve Positions', href: '/admin/approve.html', id: 'approve' },
      { icon: '📈', label: 'Statistics', href: '/admin/stats.html', id: 'stats' },
      { icon: '⏱', label: 'TA Workload', href: '/admin/workload.html', id: 'workload' },
      { icon: '👥', label: 'Manage Users', href: '/admin/users.html', id: 'users' },
    ]
  };

  let html = `
    <a class="sidebar-brand" href="/${role.toLowerCase()}/dashboard.html">
      <div class="sb-icon">TA</div>
      <span>TA Recruitment</span>
    </a>
    <div class="sidebar-nav" role="navigation" aria-label="Main navigation">`;

  const items = menus[role] || [];
  for (const item of items) {
    if (item.section) {
      html += `<div class="sidebar-section">${item.section}</div>`;
    } else {
      const cls = item.id === activePage ? 'sidebar-item active' : 'sidebar-item';
      html += `<a class="${cls}" href="${item.href}">
        <span class="si-icon">${item.icon}</span>${item.label}</a>`;
    }
  }

  html += `</div>
    <div class="sidebar-user">
      <div class="sidebar-avatar" style="background:${color}">${initials(name)}</div>
      <div>
        <div style="font-weight:600;font-size:13px">${escapeHtml(name) || 'Guest'}</div>
        <div style="font-size:11px;color:rgba(255,255,255,0.4)">${role}</div>
      </div>
      <button class="sidebar-logout" onclick="logout()" title="Logout">⏻</button>
    </div>`;

  const sidebar = document.querySelector('.app-layout .sidebar');
  if (sidebar) {
    sidebar.innerHTML = html;
    sidebar.dataset.ready = '1';
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '-';
  return dateStr;
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function bootShell() {
  initAppShell();
  const role = roleFromPath();
  const page = pageFromPath();
  if (role && page) paintSidebarFromCache(role, page);
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', bootShell);
} else {
  bootShell();
}
