# Implementation Plan: Team Task-Tracker API

## Overview
Build a RESTful API service to manage tasks for teams with in-memory storage, proper HTTP semantics, and comprehensive validation. The implementation uses Java generics to create a reusable template that can work with any resource type.

## Generic Architecture Design

The system is built around a **generic entity framework** that can be reused for any entity type:

### Core Generic Components

1. **`Entity<ID>` Interface**: Base contract for all entities
   - Defines common fields: `id`, `createdAt`, `updatedAt`
   - Type parameter `ID` allows flexible ID types (String, Long, UUID, etc.)

2. **`Collection<K, T>` Class**: Universal BatchGet response container
   ```java
   class Collection<K, T> {
       Map<K, T> results;           // Successfully retrieved entities (ID -> Entity)
       Map<K, Integer> errors;      // Failed IDs with HTTP error codes (ID -> error code)
   }
   ```
   - `K`: Key/ID type (e.g., String, Long, UUID)
   - `T`: Entity type (e.g., Task, User, Project)

3. **`ResourceStorageClient<T, ID>` Interface**: Generic storage contract
   - Works with any entity type extending `Entity<ID>`
   - Implemented by `InMemoryStore<T, ID>`

4. **`ResourceValidator<T>` Interface**: Generic validation contract
   - Entity-specific validators implement this interface

5. **Resource Classes**: REST endpoint handlers for each entity
   - Named with "Resource" suffix (e.g., `TaskResource`)
   - Handle HTTP requests and responses

### Benefits
- ✅ **Add new entities** (User, Project, etc.) by just implementing `Entity<ID>` - infrastructure is ready
- ✅ **Type-safe** compile-time checking throughout the stack
- ✅ **Consistent API** structure across all entity types
- ✅ **DRY principle** - write generic logic once, reuse everywhere

## Technology Stack Recommendation
- **Language**: Java
- **Framework Options**: 
  - **Spring Boot** (recommended - comprehensive, production-ready)
  - **Javalin** (lightweight, simple for demos)
  - **Spark Java** (micro-framework, quick setup)
- **Build Tool**: Maven or Gradle
- **Java Version**: Java 17+ (LTS)

## Project Structure
```
rplg/
├── README.md                           # Setup and curl examples
├── implementation-plan.md              # This file
├── pom.xml                            # Maven dependencies (or build.gradle)
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── tasktracker/
    │               ├── TaskTrackerApplication.java    # Main application entry point
│               ├── framework/                     # ⭐ GENERIC FRAMEWORK (Reusable)
│               │   ├── models/
│               │   │   ├── Entity.java            # Generic entity interface<ID>
│               │   │   └── Collection.java        # Generic BatchGet response<K, T>
│               │   ├── storage/
│               │   │   ├── ResourceStorageClient.java  # Generic storage interface
│               │   │   └── InMemoryStore.java     # Generic in-memory implementation
│               │   └── validators/
│               │       ├── ResourceValidator.java # Generic validator interface
│               │       └── ValidationException.java # Validation exception
│               ├── impl/                          # 📝 APPLICATION IMPLEMENTATION (Task-specific)
│               │   ├── models/
│               │   │   ├── Task.java              # Task entity (implements Entity<String>)
│               │   │   ├── TaskStatus.java        # Status enum
│               │   │   └── TaskPriority.java      # Priority enum
│               │   ├── storage/
│               │   │   └── TaskStore.java         # Task-specific store
│               │   ├── validators/
│               │   │   └── TaskValidator.java     # Task validation logic
│               │   └── controllers/
│               │       └── TaskResource.java      # Task REST API endpoints
    │               └── config/
    │                   └── GlobalExceptionHandler.java  # Error handling
    └── test/
        └── java/
            └── com/
                └── tasktracker/
                    └── TaskControllerTest.java        # Unit tests
```

## Implementation Phases

### Phase 1: Generic Entity Model (Current Focus)
**Goal**: Create generic base classes/interfaces that work with any entity type, then implement Task as a concrete example.

**Tasks**:

1. **Define `Entity` interface** (generic base):
   ```java
   public interface Entity<ID> {
       ID getId();
       void setId(ID id);
       LocalDateTime getCreatedAt();
       void setCreatedAt(LocalDateTime createdAt);
       LocalDateTime getUpdatedAt();
       void setUpdatedAt(LocalDateTime updatedAt);
       void updateFromPatch(Map<String, Object> patchData);
   }
   ```

2. **Define generic `Collection<K, T>` class** for BatchGet response:
   ```java
   public class Collection<K, T> {
       private Map<K, T> results;           // ID -> Entity object
       private Map<K, Integer> errors;      // ID -> HTTP error code
       
       // Constructors, getters, setters
   }
   ```
   - `K`: Key/ID type (String for Task, but could be Long, UUID, etc.)
   - `T`: Entity type

3. **Define `Task` class** implementing `Entity<String>`:
   - `id`: String (UUID)
   - `title`: String (required)
   - `description`: String (optional)
   - `status`: TaskStatus enum
   - `priority`: TaskPriority enum
   - `createdAt`: LocalDateTime (from Entity)
   - `updatedAt`: LocalDateTime (from Entity)
   - Implement all Entity interface methods
   - Override `updateFromPatch(Map<String, Object> patchData)`

4. **Define enums**:
   - `TaskStatus`: TODO, IN_PROGRESS, DONE
   - `TaskPriority`: LOW, MEDIUM, HIGH

**Deliverable**: `Entity.java` (interface), `Collection.java` (generic class with K and T type parameters), `Task.java` (implements Entity<String>), and enum classes

---

### Phase 2: Generic In-Memory Storage
**Goal**: Implement thread-safe generic storage layer that works with any Resource type.

**Tasks**:

1. **Define `ResourceStorageClient<T, ID>` interface** (generic storage contract):
   ```java
   public interface ResourceStorageClient<T extends Entity<ID>, ID> {
       T create(T resource);                          // Add with generated ID
       Optional<T> get(ID id);                        // Retrieve by ID
       Collection<ID, T> batchGet(List<ID> ids);      // Batch retrieve
       List<T> listAll(Map<String, String> filters);  // List with filters
       Optional<T> update(ID id, T resource);         // Update existing
       boolean delete(ID id);                         // Delete by ID
       boolean exists(ID id);                         // Check existence
   }
   ```

2. **Create `InMemoryStore<T, ID>` class** implementing `ResourceStorageClient<T, ID>`:
   - Use `ConcurrentHashMap<ID, T>` for thread-safe storage
   - Implement ID generation (for String IDs: `UUID.randomUUID().toString()`)
   - Implement all CRUD operations
   - Handle filtering logic in `listAll()`
   - Return `Collection<ID, T>` for batch operations with both results and errors
   - In `batchGet()`: populate `results` map for found IDs, `errors` map for not-found IDs (404)

3. **Instantiate for Task**:
   - Can be used as `InMemoryStore<Task, String>`
   - Or create a type alias/wrapper: `TaskStore extends InMemoryStore<Task, String>`
   - BatchGet returns `Collection<String, Task>` (String IDs, Task entities)

4. Use `@Repository` annotation if using Spring Boot

**Deliverable**: `ResourceStorageClient.java` interface and `InMemoryStore.java` generic implementation

---

### Phase 3: Generic Input Validation
**Goal**: Implement validation logic with generic base and resource-specific implementations.

**Tasks**:

1. **Define `ResourceValidator<T>` interface**:
   ```java
   public interface ResourceValidator<T extends Entity<?>> {
       void validateCreate(T entity) throws ValidationException;
       void validateUpdate(Map<String, Object> patchData) throws ValidationException;
   }
   ```

2. **Create `ValidationException` class**:
   ```java
   public class ValidationException extends RuntimeException {
       private Map<String, String> errors;  // field -> error message
       // Constructor, getters
   }
   ```

3. **Implement `TaskValidator`** implementing `ResourceValidator<Task>`:
   - `validateCreate(Task task)`: Validate POST /tasks payload
   - `validateUpdate(Map<String, Object> patchData)`: Validate PATCH payload
   - Helper methods:
     - `validateStatus(String status)`: Check if status is valid
     - `validatePriority(String priority)`: Check if priority is valid

4. **Validation rules for Task**:
   - `title`: required, non-empty, max 200 chars
   - `description`: optional, max 1000 chars
   - `status`: must be one of allowed enum values
   - `priority`: must be one of allowed enum values

5. **Error response structure**:
   ```java
   {
     "error": "Validation failed",
     "details": {
       "title": "Title cannot be empty",
       "status": "Invalid status value"
     }
   }
   ```

6. **Option**: Use Bean Validation annotations (@NotNull, @Size, etc.) on Task class if using Spring Boot

**Deliverable**: `ResourceValidator.java` interface, `ValidationException.java`, and `TaskValidator.java`

---

### Phase 4: REST Resources
**Goal**: Implement HTTP request handlers for each entity type.

**Tasks**:

1. **Create `TaskResource`** class:
   - `createTask(@RequestBody Task task)`: POST /tasks
   - `getTask(@PathVariable String id)`: GET /tasks/{id}
   - `batchGetTasks(@RequestBody List<String> ids)`: POST /tasks/batch
   - `listTasks(@RequestParam Map<String, String> params)`: GET /tasks
   - `updateTask(@PathVariable String id, @RequestBody Map<String, Object> patch)`: PATCH /tasks/{id}
   - `deleteTask(@PathVariable String id)`: DELETE /tasks/{id}
   
   Note: Batch endpoint accepts `List<String>` directly, not a wrapper class

3. **Implement proper HTTP status codes** using `ResponseEntity<T>`:
   - 200: Success (GET, PATCH)
   - 201: Created (POST)
   - 204: No Content (DELETE)
   - 400: Bad Request (validation errors)
   - 404: Not Found (resource doesn't exist)
   - 500: Internal Server Error

4. **Use Spring annotations**:
   - `@RestController` and `@RequestMapping("/tasks")` for the class
   - `@PostMapping`, `@GetMapping`, `@PatchMapping`, `@DeleteMapping` for methods
   - `@RequestParam` for query parameters (filtering)
   - `@Autowired` for dependency injection

5. **Handle query parameters for filtering**:
   - `/tasks?status=TODO`
   - `/tasks?priority=HIGH`
   - `/tasks?status=IN_PROGRESS&priority=MEDIUM`

**Deliverable**: `TaskResource.java` with all REST endpoints

---

### Phase 5: HTTP Server Setup
**Goal**: Set up and configure the HTTP server.

**Tasks**:
1. Create `TaskTrackerApplication.java` with:
   - `@SpringBootApplication` annotation
   - `main()` method with `SpringApplication.run()`
   - Or setup lightweight server if using Javalin/Spark

2. Create `application.properties` or `application.yml`:
   - `server.port=8080`
   - Logging configuration
   - Jackson JSON serialization settings

3. Create `@ControllerAdvice` for global exception handling:
   - Handle `ValidationException` → 400
   - Handle `ResourceNotFoundException` → 404
   - Handle generic exceptions → 500

4. Add health check endpoint in controller:
   - GET /health → returns 200 OK with simple status message

5. Configure Maven `pom.xml` or Gradle `build.gradle`:
   - Spring Boot dependencies (spring-boot-starter-web)
   - Or Javalin/Spark dependencies
   - Jackson for JSON
   - JUnit for testing

**Deliverable**: `TaskTrackerApplication.java`, config files, and build file ready to run

---

### Phase 6: Documentation & Testing
**Goal**: Provide clear documentation and test examples.

**Tasks**:
1. Create `README.md` with:
   - Prerequisites (Java 17+, Maven/Gradle)
   - Build instructions (`mvn clean install` or `./gradlew build`)
   - How to start the server (`mvn spring-boot:run` or `java -jar target/tasktracker.jar`)
   - Curl examples for all endpoints
   - Expected request/response formats

2. Add curl examples:
   ```bash
   # Create task
   curl -X POST http://localhost:8080/tasks \
     -H "Content-Type: application/json" \
     -d '{"title": "My Task", "status": "TODO", "priority": "HIGH"}'
   
   # Get task
   curl http://localhost:8080/tasks/{id}
   
   # List all tasks
   curl http://localhost:8080/tasks
   
   # Filter tasks
   curl "http://localhost:8080/tasks?status=TODO&priority=HIGH"
   
   # Batch get tasks
   curl -X POST http://localhost:8080/tasks/batch \
     -H "Content-Type: application/json" \
     -d '{"ids": ["id1", "id2", "id3"]}'
   
   # Update task
   curl -X PATCH http://localhost:8080/tasks/{id} \
     -H "Content-Type: application/json" \
     -d '{"status": "DONE"}'
   
   # Delete task
   curl -X DELETE http://localhost:8080/tasks/{id}
   ```

3. Optional: Add basic unit tests in `TaskControllerTest.java` using JUnit and MockMvc

**Deliverable**: Complete documentation

---

## API Contract Specification

### 1. GET /tasks/{id}
**Request**: `GET /tasks/123`
**Response** (200):
```json
{
  "id": "123",
  "title": "Implement API",
  "description": "Build the task tracker API",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "createdAt": "2025-11-30T10:00:00",
  "updatedAt": "2025-11-30T11:00:00"
}
```
**Response** (404):
```json
{
  "error": "Task not found"
}
```

---

### 2. POST /tasks/batch (Batch Get)
**Request**:
```json
{
  "ids": ["id1", "id2", "id3", "id4"]
}
```
**Response** (200) - Returns generic `Collection<String, Task>`:
```json
{
  "results": {
    "id1": { "id": "id1", "title": "Task 1", "status": "TODO", ... },
    "id2": { "id": "id2", "title": "Task 2", "status": "IN_PROGRESS", ... }
  },
  "errors": {
    "id3": 404,
    "id4": 404
  }
}
```
Note: `results` contains successfully retrieved resources (Map<K, T> where K=String, T=Task), `errors` contains IDs that failed with their HTTP error codes (Map<K, Integer>)

---

### 3. POST /tasks (UPSERT/Create)
**Request**:
```json
{
  "title": "New Task",
  "description": "Task description",
  "status": "TODO",
  "priority": "MEDIUM"
}
```
**Response** (201):
```json
{
  "id": "generated-uuid-123",
  "title": "New Task",
  "description": "Task description",
  "status": "TODO",
  "priority": "MEDIUM",
  "createdAt": "2025-11-30T12:00:00",
  "updatedAt": "2025-11-30T12:00:00"
}
```
**Response** (400):
```json
{
  "error": "Validation failed",
  "details": {
    "title": "Title is required"
  }
}
```

---

### 4. PATCH /tasks/{id}
**Request**:
```json
{
  "status": "DONE",
  "description": "Updated description"
}
```
**Response** (200):
```json
{
  "id": "123",
  "title": "Implement API",
  "description": "Updated description",
  "status": "DONE",
  "priority": "HIGH",
  "createdAt": "2025-11-30T10:00:00",
  "updatedAt": "2025-11-30T13:00:00"
}
```
**Response** (404):
```json
{
  "error": "Task not found"
}
```

---

### 5. DELETE /tasks/{id}
**Request**: `DELETE /tasks/123`
**Response** (204): No content
**Response** (404):
```json
{
  "error": "Task not found"
}
```

---

### 6. GET /tasks (List with filters)
**Request**: `GET /tasks?status=TODO&priority=HIGH`
**Response** (200):
```json
{
  "tasks": [
    { "id": "1", "title": "Task 1", "status": "TODO", "priority": "HIGH", ... },
    { "id": "2", "title": "Task 2", "status": "TODO", "priority": "HIGH", ... }
  ],
  "count": 2
}
```

---

## Current Phase: Phase 1
**Next Step**: Create the `Task` resource class with all properties and method stubs (no implementation yet).

## Time Estimate
- Phase 1: 10 minutes (stubs only)
- Phase 2: 15 minutes
- Phase 3: 10 minutes
- Phase 4: 20 minutes
- Phase 5: 5 minutes
- Phase 6: 10 minutes
**Total**: ~70 minutes (leaves buffer for testing)

## Notes

### Code Organization
- **Generic Framework** (`com.tasktracker.framework/`): Reusable boilerplate code
  - Can be copied to any new project
  - Works with any entity type
  - Modify only when enhancing the framework itself
- **Application Implementation** (`com.tasktracker.impl/`): Task-specific code
  - Replace entire package when building a different application
  - Contains all Task-specific business logic
  - Models, storage, validators, controllers all in one place
- **Other** (config, main class): Application-specific configuration
  - Customize as needed for your application

### Generic Design Philosophy
- **Use Java Generics throughout**: Entity<ID>, Collection<K, T>, ResourceStorageClient<T, ID>, ResourceValidator<T>
- **Extensibility**: New entity types (e.g., User, Project) can reuse all framework infrastructure
- **Type Safety**: Compile-time type checking for IDs and entities
- **Collection<K, T> structure**: Universal BatchGet response with type-safe `results` (Map<K, T>) and `errors` (Map<K, Integer>) maps
- **Batch requests**: Accept `List<ID>` directly in endpoint, no wrapper class needed
- **Clear Separation**: Generic framework vs application-specific code

### Implementation Details
- Use Java with Spring Boot for comprehensive features or Javalin for lightweight demo
- All code should be clean, well-commented, following Java conventions and SOLID principles
- Follow REST best practices
- Use `ConcurrentHashMap<ID, T>` for thread-safe generic in-memory storage
- Server-side ID generation using `UUID.randomUUID()` for String IDs
- Proper error handling with `@ControllerAdvice` and custom exceptions
- Use Jackson for JSON serialization/deserialization (automatic with Spring Boot)
- Enum values in UPPERCASE (e.g., TODO, IN_PROGRESS, DONE)
- camelCase for JSON field names (e.g., createdAt, updatedAt)

### Generic Benefits
- **Reusability**: Write once, use for any resource type
- **Maintainability**: Changes to base behavior propagate automatically
- **Testability**: Test generic components independently
- **Scalability**: Easy to add new resource types without code duplication

