package com.example.demo.config;

import com.example.demo.Repository.*;
import com.example.demo.entites.*;
import com.example.demo.enums.RoleName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            RoleRepository roleRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            AgencyRepository agencyRepository,
            CarRepository carRepository) {

        return args -> {
            log.info("🚀 Initialisation des données de test...");

            // ========== CRÉATION DES RÔLES ==========
            if (roleRepository.count() == 0) {
                log.info("📌 Création des rôles...");

                Role roleClient = Role.builder()
                        .name(RoleName.ROLE_CLIENT)
                        .build();
                roleRepository.save(roleClient);

                Role roleAdmin = Role.builder()
                        .name(RoleName.ROLE_ADMIN)
                        .build();
                roleRepository.save(roleAdmin);

                Role roleAgent = Role.builder()
                        .name(RoleName.ROLE_AGENT)
                        .build();
                roleRepository.save(roleAgent);

                log.info("✅ Rôles créés : CLIENT, ADMIN, AGENT");
            }

            // ========== CRÉATION DES UTILISATEURS ==========
            if (userRepository.count() == 0) {
                log.info("📌 Création des utilisateurs...");

                Role roleClient = roleRepository.findByName(RoleName.ROLE_CLIENT).orElseThrow();
                Role roleAdmin = roleRepository.findByName(RoleName.ROLE_ADMIN).orElseThrow();

                // Admin
                User admin = User.builder()
                        .fullName("Admin System")
                        .email("admin@carrental.com")
                        .password("Admin@123") // TODO: Hacher avec BCrypt
                        .phoneNumber("+237600000001")
                        .build();
                Set<Role> adminRoles = new HashSet<>();
                adminRoles.add(roleAdmin);
                adminRoles.add(roleClient);
                admin.setRoles(adminRoles);
                userRepository.save(admin);

                // Client 1
                User client1 = User.builder()
                        .fullName("Jean Dupont")
                        .email("jean.dupont@example.com")
                        .password("Client@123") // TODO: Hacher avec BCrypt
                        .phoneNumber("+237600000002")
                        .build();
                Set<Role> client1Roles = new HashSet<>();
                client1Roles.add(roleClient);
                client1.setRoles(client1Roles);
                userRepository.save(client1);

                // Client 2
                User client2 = User.builder()
                        .fullName("Marie Kameni")
                        .email("marie.kameni@example.com")
                        .password("Client@123") // TODO: Hacher avec BCrypt
                        .phoneNumber("+237600000003")
                        .build();
                Set<Role> client2Roles = new HashSet<>();
                client2Roles.add(roleClient);
                client2.setRoles(client2Roles);
                userRepository.save(client2);

                log.info("✅ Utilisateurs créés : Admin, Jean Dupont, Marie Kameni");
            }

            // ========== CRÉATION DES CATÉGORIES ==========
            if (categoryRepository.count() == 0) {
                log.info("📌 Création des catégories...");

                Category citadine = Category.builder()
                        .name("Citadine")
                        .description("Petites voitures idéales pour la ville")
                        .pricePerDay(new BigDecimal("15000"))
                        .build();
                categoryRepository.save(citadine);

                Category berline = Category.builder()
                        .name("Berline")
                        .description("Voitures confortables pour longs trajets")
                        .pricePerDay(new BigDecimal("25000"))
                        .build();
                categoryRepository.save(berline);

                Category suv = Category.builder()
                        .name("SUV")
                        .description("Véhicules tout-terrain spacieux")
                        .pricePerDay(new BigDecimal("35000"))
                        .build();
                categoryRepository.save(suv);

                Category luxe = Category.builder()
                        .name("Luxe")
                        .description("Voitures haut de gamme")
                        .pricePerDay(new BigDecimal("60000"))
                        .build();
                categoryRepository.save(luxe);

                log.info("✅ Catégories créées : Citadine, Berline, SUV, Luxe");
            }

            // ========== CRÉATION DES AGENCES ==========
            if (agencyRepository.count() == 0) {
                log.info("📌 Création des agences...");

                Agency douala = Agency.builder()
                        .name("Car Rental Douala")
                        .city("Douala")
                        .address("Boulevard de la Liberté, Akwa")
                        .phoneNumber("+237233000001")
                        .build();
                agencyRepository.save(douala);

                Agency yaounde = Agency.builder()
                        .name("Car Rental Yaoundé")
                        .city("Yaoundé")
                        .address("Avenue Kennedy, Centre-ville")
                        .phoneNumber("+237222000001")
                                .build();
                agencyRepository.save(yaounde);

                Agency bafoussam = Agency.builder()
                        .name("Car Rental Bafoussam")
                        .city("Bafoussam")
                        .address("Quartier Commercial")
                        .phoneNumber("+237233000002")
                        .build();
                agencyRepository.save(bafoussam);

                log.info("✅ Agences créées : Douala, Yaoundé, Bafoussam");
            }

            // ========== CRÉATION DES VOITURES ==========
            if (carRepository.count() == 0) {
                log.info("📌 Création des voitures...");

                Category citadine = categoryRepository.findByName("Citadine").orElseThrow();
                Category berline = categoryRepository.findByName("Berline").orElseThrow();
                Category suv = categoryRepository.findByName("SUV").orElseThrow();
                Category luxe = categoryRepository.findByName("Luxe").orElseThrow();

                Agency douala = agencyRepository.findByName("Car Rental Douala").orElseThrow();
                Agency yaounde = agencyRepository.findByName("Car Rental Yaoundé").orElseThrow();

                // Citadines
                Car car1 = Car.builder()
                        .brand("Toyota")
                        .model("Yaris")
                        .licensePlate("LT-001-DLA")
                        .yearOfManufacture(2022)
                        .color("Blanc")
                        .mileage(15000)
                        .category(citadine)
                        .agency(douala)
                        .build();
                carRepository.save(car1);

                Car car2 = Car.builder()
                        .brand("Renault")
                        .model("Clio")
                        .licensePlate("LT-002-DLA")
                        .yearOfManufacture(2023)
                        .color("Rouge")
                        .mileage(8000)
                        .category(citadine)
                        .agency(douala)
                        .build();
                carRepository.save(car2);

                // Berlines
                Car car3 = Car.builder()
                        .brand("Toyota")
                        .model("Corolla")
                        .licensePlate("LT-003-YDE")
                        .yearOfManufacture(2022)
                        .color("Gris")
                        .mileage(20000)
                        .category(berline)
                        .agency(yaounde)
                        .build();
                carRepository.save(car3);

                Car car4 = Car.builder()
                        .brand("Honda")
                        .model("Accord")
                        .licensePlate("LT-004-YDE")
                        .yearOfManufacture(2023)
                        .color("Noir")
                        .mileage(12000)
                        .category(berline)
                        .agency(yaounde)
                        .build();
                carRepository.save(car4);

                // SUV
                Car car5 = Car.builder()
                        .brand("Toyota")
                        .model("RAV4")
                        .licensePlate("LT-005-DLA")
                        .yearOfManufacture(2023)
                        .color("Bleu")
                        .mileage(10000)
                        .category(suv)
                        .agency(douala)
                        .build();
                carRepository.save(car5);

                Car car6 = Car.builder()
                        .brand("Nissan")
                        .model("X-Trail")
                        .licensePlate("LT-006-YDE")
                        .yearOfManufacture(2022)
                        .color("Argent")
                        .mileage(18000)
                        .category(suv)
                        .agency(yaounde)
                        .build();
                carRepository.save(car6);

                // Luxe
                Car car7 = Car.builder()
                        .brand("Mercedes")
                        .model("E-Class")
                        .licensePlate("LT-007-DLA")
                        .yearOfManufacture(2024)
                        .color("Noir")
                        .mileage(5000)
                        .category(luxe)
                        .agency(douala)
                        .build();
                carRepository.save(car7);

                Car car8 = Car.builder()
                        .brand("BMW")
                        .model("Serie 5")
                        .licensePlate("LT-008-YDE")
                        .yearOfManufacture(2024)
                        .color("Blanc")
                        .mileage(3000)
                        .category(luxe)
                        .agency(yaounde)
                        .build();
                carRepository.save(car8);

                log.info("✅ Voitures créées : 8 véhicules dans différentes catégories");
            }

            log.info("🎉 Initialisation des données terminée avec succès !");
        };
    }
}
