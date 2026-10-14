package com.example.HospitalManagementSystem.Config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Hospital Management System API", version = "1.0", description = "REST APIs for patients, doctors, departments, appointments, prescriptions and bills"))
public class OpenApiConfig {
}