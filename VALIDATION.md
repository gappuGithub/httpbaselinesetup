# Two-Stage Validation Architecture

The validation system uses a **two-stage approach** separating schema validation (framework level) from business rule validation (entity specific).

## 🎯 Architecture Overview

```
Request → Schema Validation (Framework) → Business Validation (Entity) → Process
             ↓ Type checking                 ↓ Business rules
             ↓ Field existence               ↓ Length limits
             ↓ Type conversions              ↓ Required fields
                                             ↓ Domain constraints
```

## Stage 1: Schema Validation (Framework Level)

**Location**: `framework/validators/SchemaValidator<T>`  
**Purpose**: Validate field types and schema compatibility  
**Reusable**: Works for ANY entity type

### What it Validates

✅ **Field existence** - Does the field exist in the entity?  
✅ **Type compatibility** - Is the value type compatible with field type?  
✅ **Type conversions** - String → Enum, Number types  
✅ **Unknown fields** - Reject fields not in schema  

### Example

```java
// SchemaValidator validates:
{
  "status": "TODO",        // ✅ String → TaskStatus (valid conversion)
  "priority": 123,         // ❌ Integer → TaskPriority (type mismatch)
  "unknownField": "value"  // ❌ Field doesn't exist in Task
}
```

### Code

```java
SchemaValidator<Task> schemaValidator = new SchemaValidator<>(Task.class);

// For PATCH
schemaValidator.validatePatchSchema(patchData);
// Throws ValidationException if types don't match or unknown fields

// For CREATE
schemaValidator.validateCreateSchema(task);
// Validates all fields have correct types
```

---

## Stage 2: Business Rule Validation (Entity Level)

**Location**: `impl/validators/TaskValidator`  
**Purpose**: Validate entity-specific business rules  
**Custom**: Each entity has its own validator

### What it Validates

✅ **Required fields** - Title must not be empty  
✅ **Length constraints** - Title max 200 chars  
✅ **Value ranges** - Description max 1000 chars  
✅ **Enum validity** - Status must be TODO/IN_PROGRESS/DONE  
✅ **Business logic** - Domain-specific rules  

### Example

```java
// TaskValidator validates:
{
  "title": "",              // ❌ Title cannot be empty (business rule)
  "title": "Very long...",  // ❌ Title exceeds 200 chars (business rule)
  "description": "Valid"    // ✅ Within limits
}
```

---

## Validation Flow

### For CREATE (POST /tasks)

```java
@PostMapping
public ResponseEntity<?> createTask(@RequestBody Task task) {
    try {
        taskValidator.validateCreate(task);
        // ↓ This calls:
        // 1. schemaValidator.validateCreateSchema(task)  ← Framework
        // 2. Business rule validation                    ← TaskValidator
        
        Task createdTask = taskStore.create(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
        
    } catch (ValidationException e) {
        // Returns detailed errors from both stages
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
```

### For UPDATE (PATCH /tasks/{id})

```java
@PatchMapping("/{id}")
public ResponseEntity<?> updateTask(@PathVariable String id, @RequestBody Map<String, Object> patchData) {
    try {
        taskValidator.validateUpdate(patchData);
        // ↓ This calls:
        // 1. schemaValidator.validatePatchSchema(patchData)  ← Framework
        // 2. Business rule validation                        ← TaskValidator
        
        Task task = existingTask.get();
        task.updateFromPatch(patchData);
        // ...
        
    } catch (ValidationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
```

---

## Implementation in TaskValidator

```java
@Component
public class TaskValidator implements ResourceValidator<Task> {
    
    // Framework-level schema validator
    private final SchemaValidator<Task> schemaValidator;
    
    public TaskValidator() {
        this.schemaValidator = new SchemaValidator<>(Task.class);
    }
    
    @Override
    public void validateCreate(Task task) throws ValidationException {
        // Step 1: Schema validation (framework)
        schemaValidator.validateCreateSchema(task);
        
        // Step 2: Business rule validation (entity-specific)
        Map<String, String> errors = new HashMap<>();
        
        // Title validation
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            errors.put("title", "Title is required");
        } else if (task.getTitle().length() > MAX_TITLE_LENGTH) {
            errors.put("title", "Title cannot exceed " + MAX_TITLE_LENGTH + " characters");
        }
        
        // ... more business rules
        
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
    
    @Override
    public void validateUpdate(Map<String, Object> patchData) throws ValidationException {
        // Step 1: Schema validation (framework)
        schemaValidator.validatePatchSchema(patchData);
        
        // Step 2: Business rule validation
        // ... validate field values (not types)
    }
}
```

---

## Benefits of Two-Stage Validation

### 1. Separation of Concerns
- **Framework** handles type safety (generic)
- **Validators** handle business logic (specific)

### 2. Reusability
- `SchemaValidator` works for ANY entity
- No need to write type checking for each entity

### 3. Better Error Messages
```json
{
  "error": "Validation failed",
  "details": {
    "priority": "Type mismatch. Expected TaskPriority but got Integer",
    "title": "Title cannot exceed 200 characters"
  }
}
```
First error is from SchemaValidator, second from TaskValidator.

### 4. Fail Fast
Schema validation happens first - no point checking business rules if types are wrong.

### 5. Consistency
All entities get the same schema validation automatically.

---

## Example Validation Errors

### Schema Validation Errors (Framework)

```bash
# Wrong type
curl -X PATCH http://localhost:8080/tasks/abc \
  -d '{"priority": 123}'

Response:
{
  "error": "Validation failed",
  "details": {
    "priority": "Type mismatch. Expected TaskPriority but got Integer"
  }
}

# Unknown field
curl -X PATCH http://localhost:8080/tasks/abc \
  -d '{"unknownField": "value"}'

Response:
{
  "error": "Validation failed",
  "details": {
    "unknownField": "Unknown field - not part of entity schema"
  }
}
```

### Business Rule Errors (Entity-Specific)

```bash
# Empty title
curl -X POST http://localhost:8080/tasks \
  -d '{"title":"","status":"TODO","priority":"HIGH"}'

Response:
{
  "error": "Validation failed",
  "details": {
    "title": "Title is required and cannot be empty"
  }
}

# Too long
curl -X POST http://localhost:8080/tasks \
  -d '{"title":"Very long title that exceeds...","status":"TODO","priority":"HIGH"}'

Response:
{
  "error": "Validation failed",
  "details": {
    "title": "Title cannot exceed 200 characters"
  }
}
```

---

## Adding Validation to New Entities

When creating a new entity (e.g., User):

### 1. Schema Validation (FREE!)
```java
// Just create the validator with your entity class
SchemaValidator<User> schemaValidator = new SchemaValidator<>(User.class);
```
**Done!** Type checking works automatically.

### 2. Business Validation (Custom)
```java
@Component
public class UserValidator implements ResourceValidator<User> {
    private final SchemaValidator<User> schemaValidator;
    
    public UserValidator() {
        this.schemaValidator = new SchemaValidator<>(User.class);
    }
    
    @Override
    public void validateCreate(User user) throws ValidationException {
        // Schema validation first
        schemaValidator.validateCreateSchema(user);
        
        // Then your business rules
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            errors.put("email", "Valid email is required");
        }
        // ... more rules
    }
}
```

---

## Summary

| Validation Stage | Level | Purpose | Location |
|-----------------|-------|---------|----------|
| **Schema** | Framework | Type safety, field existence | `SchemaValidator<T>` |
| **Business** | Entity | Domain rules, constraints | `TaskValidator`, etc. |

**Result**: Clean separation, maximum reusability, better error messages! ✨

