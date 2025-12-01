# Task Tracker API

A RESTful API service for managing team tasks, built with **Spring Boot** and a **generic resource framework**. The implementation uses Java generics to create reusable templates that can work with any resource type.

## Features

- ✅ Generic entity architecture (Entity<ID>, Collection<K, T>)
- ✅ Full CRUD operations for tasks
- ✅ Batch GET operations with error tracking
- ✅ Filtering support (by status, priority, title)
- ✅ Two-stage validation (schema + business rules) with detailed error messages
- ✅ Thread-safe in-memory storage
- ✅ Proper HTTP status codes and REST semantics

## Prerequisites

- **Java 17+** (JDK)
- **Maven 3.6+**

## Project Structure

```
rplg/
├── pom.xml
├── README.md
├── implementation-plan.md
└── src/
    └── main/
        ├── java/com/tasktracker/
        │   ├── TaskTrackerApplication.java      # Main application
        │   ├── framework/                       # ⭐ GENERIC FRAMEWORK (Reusable)
        │   │   ├── models/
        │   │   │   ├── Entity.java              # Generic entity interface
        │   │   │   └── Collection.java          # Generic BatchGet response
        │   │   ├── storage/
        │   │   │   ├── ResourceStorageClient.java  # Generic storage interface
        │   │   │   └── InMemoryStore.java       # Generic in-memory implementation
        │   │   └── validators/
        │   │       ├── ResourceValidator.java   # Generic validator interface
        │   │       ├── SchemaValidator.java     # Generic schema/type validator
        │   │       └── ValidationException.java # Validation exception
        │   ├── impl/                            # 📝 APPLICATION IMPLEMENTATION (Task-specific)
        │   │   ├── models/
        │   │   │   ├── Task.java                # Task entity
        │   │   │   ├── TaskStatus.java          # Status enum
        │   │   │   └── TaskPriority.java        # Priority enum
        │   │   ├── storage/
        │   │   │   └── TaskStore.java           # Task-specific store
        │   │   ├── validators/
        │   │   │   └── TaskValidator.java       # Task validation logic
        │   │   └── controllers/
        │   │       └── TaskResource.java        # Task REST endpoints
        │   └── config/                          # Configuration
        │       └── GlobalExceptionHandler.java  # Error handling
        └── resources/
            └── application.properties           # App configuration

**Note**: 
- `framework/` = Generic, reusable for any entity type (copy to new projects)
- `impl/` = Task-specific application code (replace for different use cases)
```

## Build and Run

### 1. Build the Project

```bash
mvn clean install
```

### 2. Run the Application

#### 2.1 - Kill the process if the post is busy.
viagniho@viagniho-mn7262 ~/D/P/rplg (main)> lsof -i:8080
COMMAND  PID     USER   FD   TYPE            DEVICE SIZE/OFF NODE NAME
java    1575 viagniho   40u  IPv6 0xfb7c349873af64e      0t0  TCP *:http-alt (LISTEN)


```bash
mvn spring-boot:run
```

Or run the JAR directly:

```bash
java -jar target/task-tracker-api-1.0.0.jar
```

The server will start on **http://localhost:8080**

## API Endpoints

### Health Check

```bash
# Check API health
curl http://localhost:8080/tasks/health
```

**Response:**
```json
{
  "status": "UP",
  "service": "Task Tracker API"
}
```

---

### 1. Create a Task (or Replace if ID Exists)

**Endpoint:** `POST /tasks`

The endpoint supports UPSERT behavior:
- **No ID provided** → Creates new task with auto-generated UUID (201 Created)
- **ID provided + exists** → Replaces the existing task (200 OK)
- **ID provided + doesn't exist** → Returns 404 error (IDs are auto-generated only)

```bash
# Create new task (ID will be auto-generated)
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Implement REST API",
    "description": "Build the task tracker API with Spring Boot",
    "status": "TODO",
    "priority": "HIGH"
  }'

# Replace existing task (UPSERT - ID must exist)
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "id": "607ffb52-c83f-41e0-bcc4-9fe8b2817453",
    "title": "Updated Task",
    "status": "DONE",
    "priority": "LOW"
  }'
# Returns 200 OK if ID exists, 404 if ID doesn't exist
```

**Response (201 Created):**
```json
{
  "id": "607ffb52-c83f-41e0-bcc4-9fe8b2817453",
  "createdAt": 1764550881793,
  "updatedAt": 1764550881793,
  "title": "Implement REST API",
  "description": "Build the task tracker API with Spring Boot",
  "status": "TODO",
  "priority": "HIGH"
}
```
**Note**: Timestamps are in epoch time (milliseconds since January 1, 1970 UTC)

**Error Responses:**

```json
// Validation Error (400 Bad Request)
{
  "error": "Validation failed",
  "details": {
    "title": "Title is required"
  }
}

// ID Not Found (404 Not Found) - when trying to create with non-existent ID
{
  "error": "Task not found with ID: invalid-id-123"
}
```

---

### 2. Get a Task by ID

**Endpoint:** `GET /tasks/{id}`

```bash
curl http://localhost:8080/tasks/550e8400-e29b-41d4-a716-446655440000
```

**Response (200 OK):**
```json
{
  "id": "607ffb52-c83f-41e0-bcc4-9fe8b2817453",
  "createdAt": 1764550881793,
  "updatedAt": 1764550881793,
  "title": "Implement REST API",
  "description": "Build the task tracker API with Spring Boot",
  "status": "TODO",
  "priority": "HIGH"
}
```

**Not Found (404):**
```json
{
  "error": "Task not found"
}
```

---

### 3. Batch Get Specific Tasks by IDs

**Endpoint:** `GET /tasks?ids=...`

```bash
# Comma-separated IDs (recommended)
curl "http://localhost:8080/tasks?ids=607ffb52-c83f-41e0-bcc4-9fe8b2817453,4c40d503-cabf-45a3-8f23-a85c338e27a8,invalid-id"

# Multiple id parameters (also works)
curl "http://localhost:8080/tasks?ids=607ffb52-c83f-41e0-bcc4-9fe8b2817453&ids=4c40d503-cabf-45a3-8f23-a85c338e27a8&ids=invalid-id"
```

**Response (200 OK):**
```json
{
  "results": {
    "607ffb52-c83f-41e0-bcc4-9fe8b2817453": {
      "id": "607ffb52-c83f-41e0-bcc4-9fe8b2817453",
      "createdAt": 1764550881793,
      "updatedAt": 1764550881793,
      "title": "Implement REST API",
      "description": "Build the task tracker API",
      "status": "TODO",
      "priority": "HIGH"
    },
    "4c40d503-cabf-45a3-8f23-a85c338e27a8": {
      "id": "4c40d503-cabf-45a3-8f23-a85c338e27a8",
      "createdAt": 1764550882163,
      "updatedAt": 1764550882163,
      "title": "Write Tests",
      "description": null,
      "status": "TODO",
      "priority": "MEDIUM"
    }
  },
  "errors": {
    "invalid-id": 404
  }
}
```

---

### 4. Get All Tasks (with optional filtering)

**Endpoint:** `GET /tasks/all`

```bash
# Get all tasks
curl http://localhost:8080/tasks/all

# Filter by status
curl "http://localhost:8080/tasks/all?status=TODO"

# Filter by priority
curl "http://localhost:8080/tasks/all?priority=HIGH"

# Multiple filters
curl "http://localhost:8080/tasks/all?status=IN_PROGRESS&priority=HIGH"
```

**Response (200 OK):**
```json
{
  "count": 2,
  "tasks": [
    {
      "id": "607ffb52-c83f-41e0-bcc4-9fe8b2817453",
      "createdAt": 1764550881793,
      "updatedAt": 1764550881793,
      "title": "Implement REST API",
      "description": "Build the task tracker API",
      "status": "TODO",
      "priority": "HIGH"
    },
    {
      "id": "4c40d503-cabf-45a3-8f23-a85c338e27a8",
      "createdAt": 1764550882163,
      "updatedAt": 1764550882163,
      "title": "Write Tests",
      "description": null,
      "status": "TODO",
      "priority": "MEDIUM"
    }
  ]
}
```

---

### 5. Update a Task (PATCH)

**Endpoint:** `PATCH /tasks/{id}`

Uses reflection-based patching - only fields present in the request body are updated.
Protected fields (`id`, `createdAt`) are ignored if included.

```bash
# Update status only
curl -X PATCH http://localhost:8080/tasks/607ffb52-c83f-41e0-bcc4-9fe8b2817453 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "IN_PROGRESS"
  }'

# Update multiple fields at once (enums are case-insensitive)
curl -X PATCH http://localhost:8080/tasks/607ffb52-c83f-41e0-bcc4-9fe8b2817453 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "done",
    "description": "Completed successfully",
    "priority": "low"
  }'
```

**Response (200 OK):**
```json
{
  "id": "607ffb52-c83f-41e0-bcc4-9fe8b2817453",
  "createdAt": 1764550881793,
  "updatedAt": 1764550882501,
  "title": "Implement REST API",
  "description": "Completed successfully",
  "status": "DONE",
  "priority": "LOW"
}
```
**Note**: `updatedAt` timestamp is automatically updated on every PATCH

---

### 6. Delete a Task

**Endpoint:** `DELETE /tasks/{id}`

```bash
curl -X DELETE http://localhost:8080/tasks/607ffb52-c83f-41e0-bcc4-9fe8b2817453
```

**Response (204 No Content):** *(empty body)*

**Not Found (404):**
```json
{
  "error": "Task not found"
}
```

---

## Task Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | String (UUID) | Auto-generated | Unique identifier (cannot be set by user) |
| `title` | String | Yes | Task title (max 200 chars) |
| `description` | String | No | Task description (max 1000 chars) |
| `status` | Enum | Yes | `TODO`, `IN_PROGRESS`, `DONE` (case-insensitive) |
| `priority` | Enum | Yes | `LOW`, `MEDIUM`, `HIGH` (case-insensitive) |
| `createdAt` | Long | Auto-generated | Creation timestamp (epoch milliseconds) |
| `updatedAt` | Long | Auto-updated | Last update timestamp (epoch milliseconds, updated on PATCH) |

## Validation

The API uses a **two-stage validation approach**:

### Stage 1: Schema Validation (Framework Level)
- Performed by `SchemaValidator<T>` using reflection
- Validates field types and structure
- Handles String → Enum conversion (case-insensitive)
- Ensures data matches entity schema

### Stage 2: Business Rule Validation (Application Level)
- Performed by `TaskValidator` (entity-specific)
- Validates business logic (e.g., length limits, required fields)
- Uses a switch-case pattern for field-specific rules
- Same validation logic for both CREATE and PATCH

**Example Validation Errors:**
```json
{
  "error": "Validation failed",
  "details": {
    "title": "Title is required",
    "description": "Description cannot exceed 1000 characters"
  }
}
```

## HTTP Status Codes

- **200 OK** - Successful GET/PATCH operations
- **201 Created** - Successful POST (create)
- **204 No Content** - Successful DELETE
- **400 Bad Request** - Validation errors or invalid input
- **404 Not Found** - Resource not found
- **500 Internal Server Error** - Server errors

## Testing with curl Examples

### Complete Workflow

```bash
# 1. Create a task
TASK_ID=$(curl -s -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"My Task","status":"TODO","priority":"HIGH"}' \
  | grep -o '"id":"[^"]*' | cut -d'"' -f4)

echo "Created task: $TASK_ID"

# 2. Get the task
curl http://localhost:8080/tasks/$TASK_ID

# 3. Update the task
curl -X PATCH http://localhost:8080/tasks/$TASK_ID \
  -H "Content-Type: application/json" \
  -d '{"status":"IN_PROGRESS"}'

# 4. List all tasks
curl http://localhost:8080/tasks/all

# 5. Delete the task
curl -X DELETE http://localhost:8080/tasks/$TASK_ID
```

## Architecture: Framework vs Implementation

This project is organized into two main packages:

### 🔧 `framework/` - Generic Framework (Reusable)

**Copy this to any project!** Contains reusable components:

1. **`Entity<ID>` Interface** - Base contract for all entities
2. **`Collection<K, T>` Class** - Universal BatchGet response container
3. **`ResourceStorageClient<T, ID>`** - Generic storage interface
4. **`InMemoryStore<T, ID>`** - Thread-safe in-memory implementation
5. **`ResourceValidator<T>`** - Generic validation interface (overloaded: `validate(T)` and `validate(Map)`)
6. **`SchemaValidator<T>`** - Generic schema/type validator using reflection
7. **`ValidationException`** - Validation exception class

### 📝 `impl/` - Application Implementation (Task-specific)

**Replace this for new use cases!** Contains Task-specific code:

1. **Task entity** - Implements `Entity<String>` with reflection-based patching
2. **TaskStatus & TaskPriority enums** - Domain-specific enums
3. **TaskStore** - Extends `InMemoryStore` with custom filtering
4. **TaskValidator** - Implements `ResourceValidator` with two-stage validation:
   - Stage 1: Schema validation (types/structure via `SchemaValidator`)
   - Stage 2: Business rules (length limits, required fields, etc.)
5. **TaskResource** - REST endpoints for Task operations

### Adding New Entity Types

To add a new entity (e.g., User, Project):

1. Copy the `framework/` package (unchanged)
2. Replace `impl/` package with your entity:
   - Create `User implements Entity<ID>`
   - Create `UserStore extends InMemoryStore<User, ID>`
   - Create `UserValidator implements ResourceValidator<User>`:
     ```java
     private final SchemaValidator<User> schemaValidator = new SchemaValidator<>(User.class);
     
     public void validate(User user) {
         schemaValidator.validate(user);  // Stage 1: Schema validation
         // Add your business rules here    // Stage 2: Business validation
     }
     ```
   - Create `UserResource` with REST endpoints

**The framework package never changes!**

## Development

### Running in Debug Mode

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

### Configuration

Edit `src/main/resources/application.properties` to change:
- Server port (default: 8080)
- Logging levels
- JSON formatting

## Troubleshooting

### Port Already in Use

If port 8080 is already in use, change it in `application.properties`:

```properties
server.port=8081
```

### Build Errors

Ensure you have Java 17+ and Maven 3.6+:

```bash
java -version
mvn -version
```

## License

This is a sample project for demonstration purposes.

