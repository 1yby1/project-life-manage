# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot 3.5.3 backend for a CRM / Project Lifecycle Management System (项目全生命周期管理系统). Java 17, MySQL, MyBatis Plus, JWT authentication.

## Build & Run Commands

```bash
./mvnw clean package          # Build
./mvnw spring-boot:run        # Run (localhost:8080)
./mvnw test                   # Run all tests
./mvnw test -Dtest=ClassName  # Run a single test class
./mvnw com.ly.smart-doc:smart-doc-maven-plugin:html  # Generate API docs
```

On Windows use `mvnw.cmd` instead of `./mvnw`.

## Architecture

Standard layered Spring Boot architecture: **Controller → Service → Mapper → MySQL**

- **Authentication**: JWT-based stateless auth via Spring Security. `JwtAuthenticationFilter` intercepts requests, `JwtUtil` handles token operations, passwords use BCrypt.
- **API responses**: All endpoints return `Result<T>` (util/Result.java) as the unified response wrapper.
- **ORM**: MyBatis Plus with annotation-based table mapping (`@TableName`, `@TableId`). Mappers extend `BaseMapper<T>`.
- **Config**: `SecurityConfig` defines endpoint security rules, `MybatisPlusConfig` sets up pagination interceptor.

## Domain Model

CRM domain: Leads (`CrmLead`) → Opportunities (`CrmOpportunity`) → Contracts (`CrmContract`) with related entities: Customers, CustomerVisits, ContractPayments, OppTasks, OppTeams. Users and roles handled via `SysUser`/`SysRole`.

## Database

MySQL database `projectlife` on localhost:3306. Connection pool: Druid. Config in `application.yml`.

## Key Dependencies

MyBatis Plus 3.5.7, JJWT 0.11.5, Apache POI 5.4.0 (Excel import/export), Lombok, Spring Boot Actuator.
