# Task Tracker API

A RESTful API service for managing team tasks, built with **Spring Boot** and a **generic resource framework**. The implementation uses Java generics to create reusable templates that can work with any resource type.

## Features

- ✅ Generic entity architecture (Entity<ID>, Collection<K, T>)
- ✅ Full CRUD operations for tasks
- ✅ Batch GET operations with error tracking
- ✅ Filtering support (by status, priority, title)
- ✅ Input validation with detailed error messages
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

### 1. Create a Task (or Replace if ID provided)

**Endpoint:** `POST /tasks`

The endpoint supports UPSERT behavior:
- **No ID provided** → Creates new task with auto-generated UUID
- **ID provided + doesn't exist** → Creates task with the specified ID
- **ID provided + exists** → Replaces the existing task (returns 200)

```bash
# Create new task (ID will be generated)
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Implement REST API",
    "description": "Build the task tracker API with Spring Boot",
    "status": "TODO",
    "priority": "HIGH"
  }'

# Create or replace with specific ID
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "id": "custom-id-123",
    "title": "Task with Custom ID",
    "status": "TODO",
    "priority": "HIGH"
  }'
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Implement REST API",
  "description": "Build the task tracker API with Spring Boot",
  "status": "TODO",
  "priority": "HIGH",
  "createdAt": 1701345600000,
  "updatedAt": 1701345600000
}
```
**Note**: Timestamps are in epoch time (milliseconds since January 1, 1970 UTC)

**Validation Error (400 Bad Request):**
```json
{
  "error": "Validation failed",
  "details": {
    "title": "Title is required and cannot be empty"
  }
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
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Implement REST API",
  "description": "Build the task tracker API with Spring Boot",
  "status": "TODO",
  "priority": "HIGH",
  "createdAt": 1701345600000,
  "updatedAt": 1701345600000
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
# Option 1: Comma-separated IDs
curl "http://localhost:8080/tasks?ids=550e8400-e29b-41d4-a716-446655440000,660e8400-e29b-41d4-a716-446655440001,non-existent-id"

# Option 2: Multiple id parameters
curl "http://localhost:8080/tasks?ids=550e8400-e29b-41d4-a716-446655440000&ids=660e8400-e29b-41d4-a716-446655440001&ids=non-existent-id"
```

**Response (200 OK):**
```json
{
  "results": {
    "550e8400-e29b-41d4-a716-446655440000": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "Implement REST API",
      "status": "TODO",
      "priority": "HIGH",
      ...
    },
    "660e8400-e29b-41d4-a716-446655440001": {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "title": "Write tests",
      "status": "TODO",
      "priority": "MEDIUM",
      ...
    }
  },
  "errors": {
    "non-existent-id": 404
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
  "tasks": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "Implement REST API",
      "status": "TODO",
      "priority": "HIGH",
      ...
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "title": "Write tests",
      "status": "TODO",
      "priority": "MEDIUM",
      ...
    }
  ],
  "count": 2
}
```

---

### 5. Update a Task (PATCH)

**Endpoint:** `PATCH /tasks/{id}`

Uses reflection-based patching - only fields present in the request body are updated.
Protected fields (`id`, `createdAt`) are ignored if included.

```bash
# Update status only
curl -X PATCH http://localhost:8080/tasks/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "IN_PROGRESS"
  }'

# Update multiple fields at once
curl -X PATCH http://localhost:8080/tasks/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "DONE",
    "description": "Completed successfully",
    "priority": "LOW"
  }'
```

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Implement REST API",
  "description": "Completed successfully",
  "status": "DONE",
  "priority": "HIGH",
  "createdAt": 1701345600000,
  "updatedAt": 1701351000000
}
```

---

### 6. Delete a Task

**Endpoint:** `DELETE /tasks/{id}`

```bash
curl -X DELETE http://localhost:8080/tasks/550e8400-e29b-41d4-a716-446655440000
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
| `id` | String (UUID) | Auto-generated | Unique identifier |
| `title` | String | Yes | Task title (max 200 chars) |
| `description` | String | No | Task description (max 1000 chars) |
| `status` | Enum | Yes | `TODO`, `IN_PROGRESS`, `DONE` |
| `priority` | Enum | Yes | `LOW`, `MEDIUM`, `HIGH` |
| `createdAt` | Long | Auto-generated | Creation timestamp (epoch milliseconds) |
| `updatedAt` | Long | Auto-updated | Last update timestamp (epoch milliseconds) |

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
5. **`ResourceValidator<T>`** - Generic validation interface
6. **`ValidationException`** - Validation exception class

### 📝 `impl/` - Application Implementation (Task-specific)

**Replace this for new use cases!** Contains Task-specific code:

1. **Task entity** - Implements `Entity<String>`
2. **TaskStatus & TaskPriority enums** - Domain-specific enums
3. **TaskStore** - Extends `InMemoryStore` with custom filtering
4. **TaskValidator** - Implements `ResourceValidator` with Task rules
5. **TaskResource** - REST endpoints for Task operations

### Adding New Entity Types

To add a new entity (e.g., User, Project):

1. Copy the `framework/` package (unchanged)
2. Replace `impl/` package with your entity:
   - Create `User implements Entity<ID>`
   - Create `UserStore extends InMemoryStore<User, ID>`
   - Create `UserValidator implements ResourceValidator<User>`
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

