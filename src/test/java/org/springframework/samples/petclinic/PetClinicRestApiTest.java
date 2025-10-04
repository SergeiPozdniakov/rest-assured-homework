package org.springframework.samples.petclinic;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static io.restassured.RestAssured.given;

// Добавляем @TestPropertySource для отключения безопасности
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"h2", "spring-data-jpa"})
@TestPropertySource(properties = {"petclinic.security.enable=false"})
class PetClinicRestApiTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        // Безопасность отключена, RestAssured может вызывать эндпоинты без аутентификации
    }

    @Test
    void testSetupAndScenarios() {
        // --- Предварительная настройка системы ---
        System.out.println("--- Начало предварительной настройки ---");

        // 1. Добавить одного пользователя с ролью admin
        String adminUsername = "test_admin_" + System.currentTimeMillis();
        String adminPassword = "password";
        String adminRole = "ADMIN";
        String userJson = """
            {
                "username": "%s",
                "password": "%s",
                "enabled": true,
                "roles": [
                    {
                        "name": "%s"
                    }
                ]
            }
            """.formatted(adminUsername, adminPassword, adminRole);


        given()
            .contentType(ContentType.JSON)
            .body(userJson)
            .when()
            .post("/petclinic/api/users") // Путь к эндпоинту создания пользователя
            .then()
            .statusCode(HttpStatus.CREATED.value()); // Только проверяем статус 201


        System.out.println("Создан пользователь с username: " + adminUsername);

        // 2. Добавить 2 владельцев питомцев
        String owner1Json = """
            {
                "firstName": "John",
                "lastName": "Doe",
                "address": "123 Main St",
                "city": "Anytown",
                "telephone": "1234567890"
            }
            """;
        int ownerId1 = given()
            .contentType(ContentType.JSON)
            .body(owner1Json)
            .when()
            .post("/petclinic/api/owners")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .jsonPath()
            .getInt("id");

        String owner2Json = """
            {
                "firstName": "Jane",
                "lastName": "Smith",
                "address": "456 Oak Ave",
                "city": "Othertown",
                "telephone": "0987654321"
            }
            """;
        int ownerId2 = given()
            .contentType(ContentType.JSON)
            .body(owner2Json)
            .when()
            .post("/petclinic/api/owners")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .jsonPath()
            .getInt("id");

        System.out.println("Созданы владельцы с ID: " + ownerId1 + ", " + ownerId2);

        // 3. Добавить 3 вида питомцев
        String petType1Json = """
            {
                "name": "Dog"
            }
            """;
        int petTypeId1 = given()
            .contentType(ContentType.JSON)
            .body(petType1Json)
            .when()
            .post("/petclinic/api/pettypes")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .jsonPath()
            .getInt("id");

        String petType2Json = """
            {
                "name": "Cat"
            }
            """;
        int petTypeId2 = given()
            .contentType(ContentType.JSON)
            .body(petType2Json)
            .when()
            .post("/petclinic/api/pettypes")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .jsonPath()
            .getInt("id");

        String petType3Json = """
            {
                "name": "Hamster"
            }
            """;
        int petTypeId3 = given()
            .contentType(ContentType.JSON)
            .body(petType3Json)
            .when()
            .post("/petclinic/api/pettypes")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .jsonPath()
            .getInt("id");

        System.out.println("Созданы типы питомцев с ID: " + petTypeId1 + ", " + petTypeId2 + ", " + petTypeId3);

        // 4. Создать 2 специализации ветеринаров
        String specialty1Json = """
            {
                "name": "Surgery"
            }
            """;
        int specId1 = given()
            .contentType(ContentType.JSON)
            .body(specialty1Json)
            .when()
            .post("/petclinic/api/specialties")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .jsonPath()
            .getInt("id");

        String specialty2Json = """
            {
                "name": "Dentistry"
            }
            """;
        int specId2 = given()
            .contentType(ContentType.JSON)
            .body(specialty2Json)
            .when()
            .post("/petclinic/api/specialties")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .jsonPath()
            .getInt("id");

        System.out.println("Созданы специализации с ID: " + specId1 + ", " + specId2);

        // 5. Создать 3 ветеринара
        // НУЖНО получить имя специальности по её ID
        String spec1Name = given()
            .when()
            .get("/petclinic/api/specialties/" + specId1)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getString("name"); // Извлекаем имя специальности

        String spec2Name = given()
            .when()
            .get("/petclinic/api/specialties/" + specId2)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getString("name"); // Извлекаем имя второй специальности

        System.out.println("Имена специальностей: " + spec1Name + ", " + spec2Name);

        String vet1Json = """
            {
                "firstName": "Alice",
                "lastName": "Johnson",
                "specialties": [
                    {
                        "id": %d,
                        "name": "%s"
                    }
                ]
            }
            """.formatted(specId1, spec1Name); // <-- Включаем id и name

        int vetId1 = given()
            .contentType(ContentType.JSON)
            .body(vet1Json)
            .when()
            .post("/petclinic/api/vets")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .jsonPath()
            .getInt("id");

        String vet2Json = """
            {
                "firstName": "Bob",
                "lastName": "Brown",
                "specialties": [
                    {
                        "id": %d,
                        "name": "%s"
                    }
                ]
            }
            """.formatted(specId2, spec2Name); // <-- Включаем id и name
        int vetId2 = given()
            .contentType(ContentType.JSON)
            .body(vet2Json)
            .when()
            .post("/petclinic/api/vets")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .jsonPath()
            .getInt("id");

        // Создадим третий ветеринар с обеими специальностями
        String vet3Json = """
            {
                "firstName": "Carol",
                "lastName": "Davis",
                "specialties": [
                    {
                        "id": %d,
                        "name": "%s"
                    },
                    {
                        "id": %d,
                        "name": "%s"
                    }
                ]
            }
            """.formatted(specId1, spec1Name, specId2, spec2Name); // <-- Включаем id и name для обеих
        int vetId3 = given()
            .contentType(ContentType.JSON)
            .body(vet3Json)
            .when()
            .post("/petclinic/api/vets")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .jsonPath()
            .getInt("id");

        System.out.println("Созданы ветеринары с ID: " + vetId1 + ", " + vetId2 + ", " + vetId3);

        // ... остальная часть теста ...
    }
}
