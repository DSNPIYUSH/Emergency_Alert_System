-- =============================================================================
-- SAMPLE SEED DATA FOR DEMO & TESTING
-- Passwords:
-- admin   -> Admin@12345 (BCrypt: $2a$10$wT0E8kIkgZ3yR.0vYtX4fOcJ.2uPq6lCjV8Z.M6bY3dDk3.iP0d3a)
-- citizen -> User@12345  (BCrypt: $2a$10$fV2pWw5mN3sT0kL1uB7hReY8zQ1iX4aP6mO9lK2jH5gD8sF1aE4qW)
-- Note: Spring Boot DataInitializer also auto-verifies & seeds these accounts on boot.
-- =============================================================================

INSERT INTO users (id, username, email, password_hash, full_name, phone_number, role, enabled)
VALUES 
(1, 'admin', 'admin@emergency-alert.internal', '$2a$10$wT0E8kIkgZ3yR.0vYtX4fOcJ.2uPq6lCjV8Z.M6bY3dDk3.iP0d3a', 'Emergency Ops Director', '+15550199001', 'ROLE_ADMIN', TRUE),
(2, 'citizen', 'citizen@demo-user.internal', '$2a$10$fV2pWw5mN3sT0kL1uB7hReY8zQ1iX4aP6mO9lK2jH5gD8sF1aE4qW', 'Jane Citizen', '+15550199002', 'ROLE_USER', TRUE)
ON DUPLICATE KEY UPDATE username=username;

-- Subscriptions for demo user: FIRE, FLOOD, EARTHQUAKE
INSERT INTO user_subscriptions (user_id, category)
VALUES 
(2, 'FIRE'),
(2, 'FLOOD'),
(2, 'EARTHQUAKE')
ON DUPLICATE KEY UPDATE category=category;

-- Sample Demo Alerts
INSERT INTO alerts (id, alert_uuid, category, severity, title, location, description, instructions, status, broadcast_by, created_at, expires_at)
VALUES 
(1, 'ALT-DEMO-2026-001', 'FIRE', 'CRITICAL', 'Wildfire Evacuation Warning - Sector North', 'North Ridge Canyon, Zone 4', 'A fast-moving vegetation fire has broken out near North Ridge Canyon, driven by 35mph winds.', 'Immediate evacuation recommended for residents north of Oak Lane. Proceed south toward Community Shelter #2.', 'ACTIVE', 'admin', NOW() - INTERVAL 1 HOUR, NOW() + INTERVAL 5 HOUR),
(2, 'ALT-DEMO-2026-002', 'FLOOD', 'HIGH', 'Flash Flood Warning - River Basin Area', 'Lower Cedar River Basin', 'Heavy upstream rainfall has caused the Cedar River to exceed flood crest levels by 3.5 feet.', 'Move to higher ground immediately. Do not attempt to walk or drive through flowing water.', 'ACTIVE', 'admin', NOW() - INTERVAL 30 MINUTE, NOW() + INTERVAL 4 HOUR),
(3, 'ALT-DEMO-2026-003', 'EARTHQUAKE', 'MEDIUM', 'Seismic Advisory - Magnitude 4.2 Tremor Recorded', 'Coastal Fault Line, 12km Offshore', 'A magnitude 4.2 earthquake occurred 12km offshore at a depth of 10km. No tsunami risk detected.', 'Inspect premises for structural cracks or gas leaks. Report downed lines to local dispatch.', 'RESOLVED', 'admin', NOW() - INTERVAL 12 HOUR, NOW() - INTERVAL 2 HOUR)
ON DUPLICATE KEY UPDATE title=title;
