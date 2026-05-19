async function handleRegister(e) {
  e.preventDefault();
  const password = qs('#regPassword').value;
  const confirm = qs('#regConfirm').value;
  if (password !== confirm) {
    toast('Passwords do not match', 'error');
    return;
  }

  try {
    const result = await api('/register', {
      method: 'POST',
      body: JSON.stringify({
        name: qs('#regName').value.trim(),
        email: qs('#regEmail').value.trim(),
        phone: qs('#regPhone').value.trim(),
        programme: qs('#regProgramme').value.trim(),
        password
      })
    });
    toast('Registration successful! Your ID is ' + result.userId);
    setTimeout(() => { location.href = '/index.html'; }, 1500);
  } catch (err) {
    toast(err.message || 'Registration failed', 'error');
  }
}

function initRegisterPage() {
  qs('#registerForm').addEventListener('submit', handleRegister);
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initRegisterPage);
} else {
  initRegisterPage();
}
