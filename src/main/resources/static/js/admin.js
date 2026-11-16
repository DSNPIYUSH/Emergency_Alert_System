/**
 * Emergency Alert System - Admin Command Center Controller
 */

async function broadcastAlert(event) {
    event.preventDefault();
    const token = localStorage.getItem('token');
    if (!token) {
        window.showToast('Authentication required', 'error');
        return;
    }

    const payload = {
        title: document.getElementById('admin-title').value.trim(),
        category: document.getElementById('admin-category').value,
        severity: document.getElementById('admin-severity').value,
        location: document.getElementById('admin-location').value.trim(),
        durationMinutes: parseInt(document.getElementById('admin-duration').value, 10),
        description: document.getElementById('admin-description').value.trim(),
        instructions: document.getElementById('admin-instructions').value.trim()
    };

    if (!payload.title || !payload.location || !payload.description || !payload.instructions) {
        window.showToast('Please fill out all required fields', 'error');
        return;
    }

    try {
        const response = await fetch('/api/admin/alerts', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            const data = await response.json();
            window.showToast(`Alert [${data.alertUuid}] successfully broadcasted!`, 'success');
            document.getElementById('broadcast-form').reset();
            loadAdminStats();
            loadAdminAlertsTable();
        } else {
            const err = await response.json();
            window.showToast(err.error || 'Failed to dispatch broadcast', 'error');
        }
    } catch (err) {
        console.error('Error broadcasting alert:', err);
        window.showToast('Network error while dispatching broadcast', 'error');
    }
}

async function loadAdminStats() {
    const token = localStorage.getItem('token');
    if (!token) return;

    try {
        const response = await fetch('/api/admin/stats', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const stats = await response.json();
            const activeEl = document.getElementById('stat-active-alerts');
            const totalEl = document.getElementById('stat-total-alerts');
            const usersEl = document.getElementById('stat-total-users');

            if (activeEl) activeEl.textContent = stats.activeAlerts;
            if (totalEl) totalEl.textContent = stats.totalAlerts;
            if (usersEl) usersEl.textContent = stats.totalUsers;

            // Render Category subscription metrics
            const catContainer = document.getElementById('stat-categories-breakdown');
            if (catContainer && stats.subscriptionsByCategory) {
                catContainer.innerHTML = '';
                for (const [cat, count] of Object.entries(stats.subscriptionsByCategory)) {
                    const tag = document.createElement('div');
                    tag.style.cssText = 'padding: 4px 8px; background: rgba(255,255,255,0.05); border-radius: 4px; font-size: 0.8rem;';
                    tag.innerHTML = `<strong>${cat}:</strong> ${count} subscribers`;
                    catContainer.appendChild(tag);
                }
            }
        }
    } catch (err) {
        console.error('Error loading admin stats:', err);
    }
}

async function loadAdminAlertsTable() {
    const token = localStorage.getItem('token');
    const tableBody = document.getElementById('admin-alerts-table-body');
    if (!token || !tableBody) return;

    try {
        const response = await fetch('/api/admin/alerts', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const alerts = await response.json();
            tableBody.innerHTML = '';

            if (alerts.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--text-muted); padding: 16px;">No alert history found.</td></tr>';
                return;
            }

            alerts.forEach(alert => {
                const tr = document.createElement('tr');
                tr.style.borderBottom = '1px solid var(--border-color)';

                const timeStr = alert.createdAt ? new Date(alert.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '';

                tr.innerHTML = `
                    <td style="padding: 10px; font-weight: 600;">${escapeHtml(alert.alertUuid)}</td>
                    <td style="padding: 10px;">
                        <span class="category-badge category-${alert.category}">${alert.category}</span>
                    </td>
                    <td style="padding: 10px;">
                        <span class="severity-pill ${alert.severity}">${alert.severity}</span>
                    </td>
                    <td style="padding: 10px;">
                        <div style="font-weight: 600;">${escapeHtml(alert.title)}</div>
                        <small style="color: var(--text-muted);">${escapeHtml(alert.location)} | ${timeStr}</small>
                    </td>
                    <td style="padding: 10px;">
                        <span class="alert-status-pill ${alert.status}">${alert.status}</span>
                    </td>
                    <td style="padding: 10px; white-space: nowrap;">
                        ${alert.status === 'ACTIVE' ? `
                            <button class="btn btn-success btn-sm" onclick="resolveAlert(${alert.id})" title="Resolve Alert">
                                Resolve
                            </button>
                            <button class="btn btn-danger btn-sm" onclick="cancelAlert(${alert.id})" title="Cancel Alert" style="margin-left: 4px;">
                                Cancel
                            </button>
                        ` : `
                            <span style="color: var(--text-muted); font-size: 0.8rem;">Closed</span>
                        `}
                    </td>
                `;
                tableBody.appendChild(tr);
            });
        }
    } catch (err) {
        console.error('Error loading admin alerts table:', err);
    }
}

async function resolveAlert(alertId) {
    const token = localStorage.getItem('token');
    if (!confirm('Are you sure you want to resolve this emergency alert?')) return;

    try {
        const response = await fetch(`/api/admin/alerts/${alertId}/resolve`, {
            method: 'PUT',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            window.showToast('Alert resolved successfully', 'success');
            loadAdminStats();
            loadAdminAlertsTable();
            loadActiveAlerts();
        } else {
            window.showToast('Failed to resolve alert', 'error');
        }
    } catch (err) {
        console.error('Error resolving alert:', err);
    }
}

async function cancelAlert(alertId) {
    const token = localStorage.getItem('token');
    if (!confirm('Are you sure you want to cancel / revoke this alert?')) return;

    try {
        const response = await fetch(`/api/admin/alerts/${alertId}/cancel`, {
            method: 'PUT',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            window.showToast('Alert marked as CANCELLED', 'success');
            loadAdminStats();
            loadAdminAlertsTable();
            loadActiveAlerts();
        } else {
            window.showToast('Failed to cancel alert', 'error');
        }
    } catch (err) {
        console.error('Error cancelling alert:', err);
    }
}

window.loadAdminStats = loadAdminStats;
window.loadAdminAlertsTable = loadAdminAlertsTable;
window.resolveAlert = resolveAlert;
window.cancelAlert = cancelAlert;
