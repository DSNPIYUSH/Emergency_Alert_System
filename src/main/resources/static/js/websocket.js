/**
 * Emergency Alert System - Self-Contained WebSocket / STOMP Client
 * Features:
 * - Direct native WebSocket connection to /ws-alerts-native or /ws-alerts
 * - Auto-reconnection with exponential backoff
 * - Subscriptions to /topic/alerts/all and category channels:
 *   /topic/alerts/FIRE, /topic/alerts/FLOOD, /topic/alerts/EARTHQUAKE, /topic/alerts/SECURITY
 * - Real-time incoming alert dispatcher
 */
class AlertWebSocketClient {
    constructor() {
        this.socket = null;
        this.isConnected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 20;
        this.reconnectDelay = 2000;
        this.subscribers = new Map();
        this.subscriptionIdCounter = 1;
        this.userSubscriptions = new Set(['FIRE', 'FLOOD', 'EARTHQUAKE', 'SECURITY']);
    }

    connect() {
        if (this.socket && (this.socket.readyState === WebSocket.OPEN || this.socket.readyState === WebSocket.CONNECTING)) {
            return;
        }

        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        // Connect to native WebSocket endpoint
        const wsUrl = `${protocol}//${window.location.host}/ws-alerts-native`;

        this.updateStatusIndicator(false, 'Connecting...');

        try {
            this.socket = new WebSocket(wsUrl);
        } catch (err) {
            console.error('WebSocket creation failed:', err);
            this.scheduleReconnect();
            return;
        }

        this.socket.onopen = () => {
            console.log('WebSocket: TCP Connection established, initiating STOMP handshake...');
            this.sendStompFrame('CONNECT', {
                'accept-version': '1.1,1.2',
                'heart-beat': '10000,10000'
            });
        };

        this.socket.onmessage = (event) => {
            this.handleStompMessage(event.data);
        };

        this.socket.onerror = (err) => {
            console.warn('WebSocket error encountered:', err);
        };

        this.socket.onclose = () => {
            this.isConnected = false;
            this.updateStatusIndicator(false, 'Disconnected');
            console.log('WebSocket closed. Scheduling reconnect...');
            this.scheduleReconnect();
        };
    }

    scheduleReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            const delay = Math.min(this.reconnectDelay * Math.pow(1.5, this.reconnectAttempts), 15000);
            this.reconnectAttempts++;
            setTimeout(() => {
                console.log(`WebSocket: Attempting reconnect #${this.reconnectAttempts}...`);
                this.connect();
            }, delay);
        }
    }

    sendStompFrame(command, headers = {}, body = '') {
        if (!this.socket || this.socket.readyState !== WebSocket.OPEN) return;

        let frame = `${command}\n`;
        for (const [key, value] of Object.entries(headers)) {
            frame += `${key}:${value}\n`;
        }
        frame += `\n${body}\0`;
        this.socket.send(frame);
    }

    handleStompMessage(rawData) {
        // Strip trailing null characters and split headers & body
        const cleanData = rawData.replace(/\0+$/, '');
        const dividerIndex = cleanData.indexOf('\n\n');
        if (dividerIndex === -1) return;

        const headerBlock = cleanData.substring(0, dividerIndex);
        const body = cleanData.substring(dividerIndex + 2);

        const lines = headerBlock.split('\n');
        const command = lines[0].trim();
        const headers = {};

        for (let i = 1; i < lines.length; i++) {
            const sep = lines[i].indexOf(':');
            if (sep !== -1) {
                const k = lines[i].substring(0, sep).trim();
                const v = lines[i].substring(sep + 1).trim();
                headers[k] = v;
            }
        }

        if (command === 'CONNECTED') {
            this.isConnected = true;
            this.reconnectAttempts = 0;
            this.updateStatusIndicator(true, 'Live Connected');
            console.log('WebSocket: STOMP Protocol connected successfully.');
            this.subscribeToChannels();
        } else if (command === 'MESSAGE') {
            try {
                const alertData = JSON.parse(body);
                this.handleIncomingAlert(alertData, headers.destination);
            } catch (e) {
                console.error('Failed to parse WebSocket message body:', e, body);
            }
        }
    }

    subscribeToChannels() {
        // 1. Subscribe to system-wide global alert channel
        this.subscribeTopic('/topic/alerts/all');

        // 2. Subscribe to category channels
        ['FIRE', 'FLOOD', 'EARTHQUAKE', 'SECURITY'].forEach(cat => {
            this.subscribeTopic(`/topic/alerts/${cat}`);
        });

        // 3. User queue channel (for authenticated direct alerts)
        this.subscribeTopic('/user/queue/notifications');
    }

    subscribeTopic(destination) {
        const subId = `sub-${this.subscriptionIdCounter++}`;
        this.subscribers.set(destination, subId);
        this.sendStompFrame('SUBSCRIBE', {
            id: subId,
            destination: destination
        });
        console.log(`WebSocket: Subscribed to ${destination}`);
    }

    handleIncomingAlert(alert, destination) {
        console.log('Incoming WebSocket Alert:', alert, 'from:', destination);

        // Check if user has subscribed to this alert's category
        const isSubscribed = !alert.category || this.userSubscriptions.has(alert.category);

        // Trigger Audio Siren & Web Notification
        if (window.emergencyAudio) {
            window.emergencyAudio.playAlertTone(alert.severity);
        }

        // Show Urgent Banner if Critical or High
        if (alert.severity === 'CRITICAL' || alert.severity === 'HIGH') {
            if (window.showUrgentBanner) {
                window.showUrgentBanner(alert);
            }
        }

        // Add to UI Live Feed
        if (window.onAlertReceived) {
            window.onAlertReceived(alert, isSubscribed);
        }

        // Toast notification
        if (window.showToast) {
            const prefix = alert.severity === 'CRITICAL' ? '🚨 [CRITICAL ALERT]' : '⚠️ [ALERT UPDATE]';
            window.showToast(`${prefix} ${alert.category}: ${alert.title}`, alert.severity === 'CRITICAL' ? 'error' : 'success');
        }
    }

    updateUserSubscriptions(categoriesSet) {
        this.userSubscriptions = new Set(categoriesSet);
    }

    updateStatusIndicator(connected, text) {
        const dot = document.getElementById('ws-status-dot');
        const label = document.getElementById('ws-status-text');
        if (dot) {
            if (connected) {
                dot.classList.add('connected');
            } else {
                dot.classList.remove('connected');
            }
        }
        if (label) {
            label.textContent = text;
        }
    }

    sendAcknowledgment(alertUuid) {
        this.sendStompFrame('SEND', {
            destination: '/app/ack',
            'content-type': 'application/json'
        }, JSON.stringify({ alertUuid: alertUuid }));
    }
}

window.alertWsClient = new AlertWebSocketClient();
