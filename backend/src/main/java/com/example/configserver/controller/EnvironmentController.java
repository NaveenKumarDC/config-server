package com.example.configserver.controller;

import com.example.configserver.model.Environment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/environments")
@Tag(name = "Environments", description = "API for retrieving available environments")
public class EnvironmentController {

    @GetMapping
    @Operation(summary = "Get all available environments")
    public ResponseEntity<List<String>> getAllEnvironments() {
        List<String> environments = Arrays.stream(Environment.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(environments);
    }
} 