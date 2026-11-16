/**
 * Emergency Alert System - User Subscriptions & Alert Rendering Controller
 */

// Category icons & metadata
const CATEGORY_META = {
    FIRE: { icon: '🔥', label: 'Wildfire & Fire Hazard', color: 'var(--color-fire)' },
    FLOOD: { icon: '🌊', label: 'Flash Flood & Hydrological', color: 'var(--color-flood)' },
    EARTHQUAKE: { icon: '🌋', label: 'Earthquake & Seismic', color: 'var(--color-earthquake)' },
    SECURITY: { icon: '🛡️', label: 'Civil Security & Safety', color: 'var(--color-security)' }
};

let activeAlertsMap = new Map();
let userAcknowledgedAlerts = new Set();

/**
 * Initializes User Subscriptions view from current session or defaults
 */
async function loadUserSubscriptions() {
    const token = localStorage.getItem('token');
    const categories = ['FIRE', 'FLOOD', 'EARTHQUAKE', 'SECURITY'];

    if (!token) {
        // Unauthenticated visitor defaults
        categories.forEach(cat => {
            const toggle = document.getElementById(`sub-${cat.toLowerCase()}`);
            if (toggle) toggle.checked = true;
        });
        return;
    }

    try {
        const response = await fetch('/api/subscriptions', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const data = await response.json();
            const currentSubs = new Set(data.subscriptions || []);

            categories.forEach(cat => {
                const toggle = document.getElementById(`sub-${cat.toLowerCase()}`);
                if (toggle) {
                    toggle.checked = currentSubs.has(cat);
                }
            });

            if (window.alertWsClient) {
                window.alertWsClient.updateUserSubscriptions(currentSubs);
            }
        }
    } catch (err) {
        console.error('Failed to load subscriptions:', err);
    }
}

/**
 * Saves updated subscription preferences to backend
 */
async function saveSubscriptions() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.showToast('Please log in to save subscription preferences', 'error');
        window.openAuthModal('login');
        return;
    }

    const categories = [];
    ['fire', 'flood', 'earthquake', 'security'].forEach(cat => {
        const toggle = document.getElementById(`sub-${cat}`);
        if (toggle && toggle.checked) {
            categories.push(cat.toUpperCase());
        }
    });

    try {
        const response = await fetch('/api/subscriptions', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ categories: categories })
        });

        if (response.ok) {
            const data = await response.json();
            window.showToast('Subscription preferences updated!', 'success');
            if (window.alertWsClient) {
                window.alertWsClient.updateUserSubscriptions(new Set(data.subscriptions));
            }
            // Refresh feeds
            loadActiveAlerts();
        } else {
            const err = await response.json();
            window.showToast(err.error || 'Failed to update preferences', 'error');
        }
    } catch (err) {
        console.error('Error saving subscriptions:', err);
        window.showToast('Network error while saving preferences', 'error');
    }
}

/**
 * Loads active alerts from REST API
 */
async function loadActiveAlerts() {
    const feed = document.getElementById('alerts-feed-container');
    if (!feed) return;

    try {
        const response = await fetch('/api/public/alerts');
        if (response.ok) {
            const alerts = await response.json();
            feed.innerHTML = '';
            activeAlertsMap.clear();

            if (alerts.length === 0) {
                feed.innerHTML = `
                    <div style="text-align: center; padding: 40px; color: var(--text-muted);">
                        <p style="font-size: 1.5rem; margin-bottom: 8px;">✅</p>
                        <p>No active emergency warnings at this time.</p>
                    </div>
                `;
                return;
            }

            alerts.forEach(alert => {
                activeAlertsMap.set(alert.id, alert);
                renderAlertCard(alert, feed, false);
            });

            // Check if any is critical/high to show urgent banner
            const topUrgent = alerts.find(a => a.severity === 'CRITICAL' || a.severity === 'HIGH');
            if (topUrgent) {
                window.showUrgentBanner(topUrgent);
            }
        }
    } catch (err) {
        console.error('Failed to load active alerts:', err);
    }
}

/**
 * Renders an alert card into a container element
 */
function renderAlertCard(alert, container, prepend = false) {
    const card = document.createElement('div');
    card.id = `alert-card-${alert.id}`;
    card.className = `alert-item sev-${alert.severity}`;

    const catInfo = CATEGORY_META[alert.category] || { icon: '⚠️', label: alert.category };
    const isAcked = userAcknowledgedAlerts.has(alert.alertUuid);

    const formattedTime = alert.createdAt ? new Date(alert.createdAt).toLocaleString() : 'Just now';
    const expiresTime = alert.expiresAt ? new Date(alert.expiresAt).toLocaleTimeString() : 'N/A';

    card.innerHTML = `
        <div class="alert-header">
            <div class="alert-meta">
                <span class="category-badge category-${alert.category}">
                    ${catInfo.icon} ${alert.category}
                </span>
                <span class="severity-pill ${alert.severity}">${alert.severity}</span>
                <span class="alert-status-pill ${alert.status}">${alert.status}</span>
                <span style="font-size: 0.75rem; color: var(--text-muted);">UUID: ${escapeHtml(alert.alertUuid)}</span>
            </div>
            <div class="alert-actions">
                ${!isAcked && alert.status === 'ACTIVE' ? `
                    <button class="btn btn-secondary btn-sm" onclick="acknowledgeAlert('${escapeHtml(alert.alertUuid)}', ${alert.id})">
                        ✓ Acknowledge
                    </button>
                ` : `
                    <span style="font-size: 0.8rem; color: #4ade80;">✓ Acknowledged</span>
                `}
            </div>
        </div>

        <h3 class="alert-title">${escapeHtml(alert.title)}</h3>
        <div class="alert-location">📍 <strong>Location:</strong> ${escapeHtml(alert.location)}</div>
        <p class="alert-description">${escapeHtml(alert.description)}</p>

        <div class="alert-instructions-box">
            <strong>Protective Action Required:</strong>
            <p>${escapeHtml(alert.instructions)}</p>
        </div>

        <div class="alert-footer">
            <span>🕒 Issued: ${formattedTime} | Valid until: ${expiresTime}</span>
            <span>Broadcast Authority: <strong>${escapeHtml(alert.broadcastBy)}</strong></span>
        </div>
    `;

    if (prepend && container.firstChild) {
        container.insertBefore(card, container.firstChild);
    } else {
        container.appendChild(card);
    }
}

/**
 * Acknowledges an alert
 */
function acknowledgeAlert(alertUuid, cardId) {
    userAcknowledgedAlerts.add(alertUuid);

    // Send acknowledgment over WebSocket
    if (window.alertWsClient) {
        window.alertWsClient.sendAcknowledgment(alertUuid);
    }

    // Update UI card
    const card = document.getElementById(`alert-card-${cardId}`);
    if (card) {
        const actionArea = card.querySelector('.alert-actions');
        if (actionArea) {
            actionArea.innerHTML = '<span style="font-size: 0.8rem; color: #4ade80;">✓ Acknowledged</span>';
        }
    }

    window.showToast(`Alert [${alertUuid}] acknowledged`, 'success');
}

/**
 * Handles incoming WebSocket alert pushed to client
 */
window.onAlertReceived = function(alert, isSubscribed) {
    const feed = document.getElementById('alerts-feed-container');
    if (!feed) return;

    // Check if card already exists (e.g. status update to RESOLVED or CANCELLED)
    const existing = document.getElementById(`alert-card-${alert.id}`);
    if (existing) {
        existing.remove();
    }

    // Only render if subscribed or if alert is still ACTIVE
    if (alert.status === 'ACTIVE') {
        renderAlertCard(alert, feed, true);
    }

    // Update metrics if on admin page
    if (window.loadAdminStats) {
        window.loadAdminStats();
    }
};

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, "&amp;")
              .replace(/</g, "&lt;")
              .replace(/>/g, "&gt;")
              .replace(/"/g, "&quot;")
              .replace(/'/g, "&#039;");
}
