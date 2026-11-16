package com.emergency.alert.config;

import com.emergency.alert.model.*;
import com.emergency.alert.repository.AlertRepository;
import com.emergency.alert.repository.UserRepository;
import com.emergency.alert.repository.UserSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final AlertRepository alertRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           UserSubscriptionRepository subscriptionRepository,
                           AlertRepository alertRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.alertRepository = alertRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing Emergency Alert System default seed data...");

        // 1. Initialize Admin User if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User(
                    "admin",
                    "admin@emergency-alert.internal",
                    passwordEncoder.encode("Admin@12345"),
                    "Emergency Operations Administrator",
                    "+15550199001",
                    Role.ROLE_ADMIN
            );
            userRepository.save(admin);
            log.info("Default ADMIN user initialized: username=admin, role=ROLE_ADMIN");
        }

        // 2. Initialize Citizen Demo User if not exists
        if (!userRepository.existsByUsername("citizen")) {
            User citizen = new User(
                    "citizen",
                    "citizen@demo-user.internal",
                    passwordEncoder.encode("User@12345"),
                    "Jane Citizen",
                    "+15550199002",
                    Role.ROLE_USER
            );
            User savedCitizen = userRepository.save(citizen);

            // Add default subscriptions
            subscriptionRepository.save(new UserSubscription(savedCitizen, AlertCategory.FIRE));
            subscriptionRepository.save(new UserSubscription(savedCitizen, AlertCategory.FLOOD));
            subscriptionRepository.save(new UserSubscription(savedCitizen, AlertCategory.EARTHQUAKE));
            log.info("Default USER initialized: username=citizen with FIRE, FLOOD, EARTHQUAKE subscriptions");
        }

        // 3. Initialize Initial Alerts if table is empty
        if (alertRepository.count() == 0) {
            Alert wildfire = new Alert(
                    "ALERT-FIRE-1001",
                    AlertCategory.FIRE,
                    AlertSeverity.CRITICAL,
                    "Wildfire Immediate Evacuation Order - Sector North",
                    "North Ridge Canyon, Zone 4",
                    "A fast-moving vegetation fire has broken out near North Ridge Canyon, driven by 35mph dry winds. Structural hazard imminent.",
                    "Evacuate immediately toward the South Highway corridor. Assemble at Community Shelter #2. Keep headlights on and windows closed.",
                    "admin",
                    LocalDateTime.now().plusHours(4)
            );

            Alert flood = new Alert(
                    "ALERT-FLOOD-1002",
                    AlertCategory.FLOOD,
                    AlertSeverity.HIGH,
                    "Flash Flood Warning - Cedar River Basin",
                    "Cedar Valley Lowlands & River Road",
                    "Torrential upstream rain has caused rapid cresting of Cedar River. Roadway submersion reported at Mile Marker 14.",
                    "Move immediately to higher ground. Do not attempt to walk, swim, or drive through flooded areas. Turn Around, Don't Drown.",
                    "admin",
                    LocalDateTime.now().plusHours(3)
            );

            Alert earthquake = new Alert(
                    "ALERT-EARTHQUAKE-1003",
                    AlertCategory.EARTHQUAKE,
                    AlertSeverity.MEDIUM,
                    "Seismic Advisory - Magnitude 4.5 Tremor Recorded",
                    "Coastal Fault Line, 15km Offshore",
                    "A moderate earthquake occurred offshore at a depth of 12km. Tremors felt throughout the metropolitan area.",
                    "Inspect residential gas lines and water heaters. Be prepared for minor aftershocks. No tsunami warning has been issued.",
                    "admin",
                    LocalDateTime.now().plusHours(6)
            );

            Alert security = new Alert(
                    "ALERT-SECURITY-1004",
                    AlertCategory.SECURITY,
                    AlertSeverity.HIGH,
                    "Security Perimeter Alert - Industrial District",
                    "East Logistics Park, Gate 3",
                    "Hazardous chemical transport spill containment operation in progress. Police and Hazmat teams have established a 500m safety cordon.",
                    "Avoid the East Logistics Park area. Residents within 1km should shelter in place with windows and ventilation vents closed.",
                    "admin",
                    LocalDateTime.now().plusHours(2)
            );

            alertRepository.save(wildfire);
            alertRepository.save(flood);
            alertRepository.save(earthquake);
            alertRepository.save(security);
            log.info("Initial demo emergency alerts seeded successfully.");
        }
    }
}
