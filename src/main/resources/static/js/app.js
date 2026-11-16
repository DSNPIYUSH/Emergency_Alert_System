/**
 * Emergency Alert System - Main Application Orchestrator
 */

document.addEventListener('DOMContentLoaded', () => {
    initAuth();
    initTabs();
    initAudioControls();

    // Start WebSocket Connection
    if (window.alertWsClient) {
        window.alertWsClient.connect();
    }

    // Load initial data
    loadActiveAlerts();
    loadUserSubscriptions();
});

// -----------------------------------------------------------------------------
// Authentication & Session
// -----------------------------------------------------------------------------
function initAuth() {
    updateAuthUI();
}

function updateAuthUI() {
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    const role = localStorage.getItem('role');

    const authSection = document.getElementById('auth-nav-section');
    const adminTabBtn = document.getElementById('tab-btn-admin');

    if (token && username) {
        authSection.innerHTML = `
            <div class="user-badge">
                <span>👤 <strong>${escapeHtml(username)}</strong></span>
                <span class="role-tag">${role === 'ROLE_ADMIN' ? 'ADMIN' : 'USER'}</span>
            </div>
            <button class="btn btn-secondary btn-sm" onclick="logout()">Logout</button>
        `;

        if (role === 'ROLE_ADMIN' && adminTabBtn) {
            adminTabBtn.style.display = 'flex';
        } else if (adminTabBtn) {
            adminTabBtn.style.display = 'none';
        }
    } else {
        authSection.innerHTML = `
            <button class="btn btn-secondary btn-sm" onclick="openAuthModal('login')">Login</button>
            <button class="btn btn-primary btn-sm" onclick="openAuthModal('register')">Register</button>
        `;
        if (adminTabBtn) adminTabBtn.style.display = 'none';
    }
}

function openAuthModal(mode = 'login') {
    const modal = document.getElementById('auth-modal');
    const loginForm = document.getElementById('login-form-container');
    const registerForm = document.getElementById('register-form-container');
    const modalTitle = document.getElementById('auth-modal-title');

    if (mode === 'login') {
        modalTitle.textContent = 'Sign In to Alert Portal';
        loginForm.style.display = 'block';
        registerForm.style.display = 'none';
    } else {
        modalTitle.textContent = 'Register Citizen Account';
        loginForm.style.display = 'none';
        registerForm.style.display = 'block';
    }
    modal.classList.add('active');
}

function closeAuthModal() {
    document.getElementById('auth-modal').classList.remove('active');
}

async function handleLogin(event) {
    event.preventDefault();
    const username = document.getElementById('login-username').value.trim();
    const password = document.getElementById('login-password').value;

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('token', data.token);
            localStorage.setItem('username', data.username);
            localStorage.setItem('role', data.role);

            closeAuthModal();
            updateAuthUI();
            loadUserSubscriptions();
            showToast(`Welcome back, ${data.username}!`, 'success');

            if (data.role === 'ROLE_ADMIN') {
                switchTab('admin');
            }
        } else {
            const err = await response.json();
            showToast(err.error || 'Authentication failed', 'error');
        }
    } catch (e) {
        showToast('Login network error', 'error');
    }
}

async function handleRegister(event) {
    event.preventDefault();
    const username = document.getElementById('reg-username').value.trim();
    const email = document.getElementById('reg-email').value.trim();
    const password = document.getElementById('reg-password').value;
    const fullName = document.getElementById('reg-fullname').value.trim();
    const phoneNumber = document.getElementById('reg-phone').value.trim();

    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username,
                email,
                password,
                fullName,
                phoneNumber,
                subscriptions: ['FIRE', 'FLOOD', 'EARTHQUAKE', 'SECURITY']
            })
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('token', data.token);
            localStorage.setItem('username', data.username);
            localStorage.setItem('role', data.role);

            closeAuthModal();
            updateAuthUI();
            loadUserSubscriptions();
            showToast('Registration successful! Subscribed to all categories.', 'success');
        } else {
            const err = await response.json();
            showToast(err.error || 'Registration failed', 'error');
        }
    } catch (e) {
        showToast('Registration network error', 'error');
    }
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    updateAuthUI();
    switchTab('feed');
    showToast('Logged out successfully', 'success');
}

// -----------------------------------------------------------------------------
// Tabs Navigation
// -----------------------------------------------------------------------------
function initTabs() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const tabName = btn.getAttribute('data-tab');
            switchTab(tabName);
        });
    });
}

function switchTab(tabName) {
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.getAttribute('data-tab') === tabName);
    });

    document.querySelectorAll('.tab-content').forEach(section => {
        section.style.display = section.id === `tab-${tabName}` ? 'block' : 'none';
    });

    if (tabName === 'admin') {
        if (window.loadAdminStats) window.loadAdminStats();
        if (window.loadAdminAlertsTable) window.loadAdminAlertsTable();
    } else if (tabName === 'history') {
        loadHistoryAlerts();
    } else if (tabName === 'feed') {
        loadActiveAlerts();
    }
}

// -----------------------------------------------------------------------------
// Urgent Banner Controls
// -----------------------------------------------------------------------------
window.showUrgentBanner = function(alert) {
    const banner = document.getElementById('urgent-broadcast-banner');
    if (!banner) return;

    document.getElementById('urgent-banner-title').textContent = alert.title;
    document.getElementById('urgent-banner-desc').textContent = alert.description;
    document.getElementById('urgent-banner-action').textContent = alert.instructions;
    document.getElementById('urgent-banner-location').textContent = alert.location;
    banner.classList.add('visible');
};

function dismissUrgentBanner() {
    const banner = document.getElementById('urgent-broadcast-banner');
    if (banner) banner.classList.remove('visible');
}

// -----------------------------------------------------------------------------
// Audio Controls
// -----------------------------------------------------------------------------
function initAudioControls() {
    const audioBtn = document.getElementById('audio-toggle-btn');
    if (!audioBtn) return;

    audioBtn.addEventListener('click', () => {
        if (window.emergencyAudio) {
            const isMuted = window.emergencyAudio.toggleMute();
            audioBtn.textContent = isMuted ? '🔇 Audio Muted' : '🔊 Audio Active';
            audioBtn.title = isMuted ? 'Click to enable emergency warning siren' : 'Click to mute warning sounds';
            showToast(isMuted ? 'Emergency alert audio muted' : 'Emergency alert audio enabled', 'success');
        }
    });
}

// -----------------------------------------------------------------------------
// Alert History Filter & Search
// -----------------------------------------------------------------------------
async function loadHistoryAlerts() {
    const historyContainer = document.getElementById('history-feed-container');
    if (!historyContainer) return;

    try {
        const response = await fetch('/api/public/alerts');
        if (response.ok) {
            const alerts = await response.json();
            renderHistoryList(alerts);
        }
    } catch (e) {
        console.error('Failed to load history:', e);
    }
}

function renderHistoryList(alerts) {
    const container = document.getElementById('history-feed-container');
    const filterCat = document.getElementById('history-filter-category').value;
    const searchKeyword = document.getElementById('history-search-input').value.toLowerCase().trim();

    container.innerHTML = '';

    const filtered = alerts.filter(a => {
        if (filterCat !== 'ALL' && a.category !== filterCat) return false;
        if (searchKeyword) {
            const text = `${a.title} ${a.location} ${a.description} ${a.alertUuid}`.toLowerCase();
            return text.includes(searchKeyword);
        }
        return true;
    });

    if (filtered.length === 0) {
        container.innerHTML = '<p style="text-align: center; color: var(--text-muted); padding: 24px;">No alerts match filter criteria.</p>';
        return;
    }

    filtered.forEach(alert => {
        renderAlertCard(alert, container, false);
    });
}

// -----------------------------------------------------------------------------
// Toast Notification Utility
// -----------------------------------------------------------------------------
window.showToast = function(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    const icon = type === 'error' ? '⚠️' : '📢';
    toast.innerHTML = `<span>${icon}</span> <span>${escapeHtml(message)}</span>`;

    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4500);
};

window.openAuthModal = openAuthModal;
window.closeAuthModal = closeAuthModal;
window.handleLogin = handleLogin;
window.handleRegister = handleRegister;
window.logout = logout;
window.switchTab = switchTab;
window.dismissUrgentBanner = dismissUrgentBanner;
window.renderHistoryList = renderHistoryList;
