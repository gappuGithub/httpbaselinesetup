# Implementation Improvements

This document describes key improvements made to the Task Tracker API implementation.

## 1. UPSERT Behavior in POST /tasks

### Problem
Originally, POST would always generate a new ID, even if the user provided one. This prevented clients from creating resources with specific IDs or replacing existing resources.

### Solution
The POST endpoint now supports **UPSERT** behavior:

```java
@PostMapping
public ResponseEntity<?> createTask(@RequestBody Task task) {
    // If ID is provided and exists → replace (return 200)
    if (task.getId() != null && taskStore.exists(task.getId())) {
        Task updatedTask = taskStore.update(task.getId(), task).orElse(task);
        return ResponseEntity.ok(updatedTask);
    }
    
    // Otherwise → create new (return 201)
    Task createdTask = taskStore.create(task);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
}
```

### Behavior

| Scenario | Request | Response | Status |
|----------|---------|----------|--------|
| No ID provided | `{"title": "Task"}` | New task with UUID | 201 Created |
| ID provided, doesn't exist | `{"id": "abc", "title": "Task"}` | Task with ID "abc" | 201 Created |
| ID provided, exists | `{"id": "abc", "title": "New"}` | Replaces existing task | 200 OK |

### Example

```bash
# Create with custom ID
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "id": "task-001",
    "title": "Custom ID Task",
    "status": "TODO",
    "priority": "HIGH"
  }'

# Replace the same task
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "id": "task-001",
    "title": "Updated Task",
    "status": "DONE",
    "priority": "LOW"
  }'
```

---

## 2. Reflection-Based PATCH Implementation

### Problem
The original `updateFromPatch()` required manual field-by-field checking:
```java
if (patchData.containsKey("title")) {
    this.title = (String) patchData.get("title");
}
if (patchData.containsKey("description")) {
    this.description = (String) patchData.get("description");
}
// ... repeat for every field
```

This was:
- ❌ Verbose and repetitive
- ❌ Error-prone (easy to forget fields)
- ❌ Hard to maintain (add new field = update patch logic)

### Solution
Use **Java reflection** to dynamically apply patches:

```java
@Override
public void updateFromPatch(Map<String, Object> patchData) {
    patchData.forEach((fieldName, value) -> {
        // Skip protected fields
        if ("id".equals(fieldName) || "createdAt".equals(fieldName)) {
            return;
        }
        
        try {
            // Get field via reflection
            Field field = this.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            
            // Convert and set value
            Object convertedValue = convertValue(value, field.getType());
            field.set(this, convertedValue);
            
        } catch (NoSuchFieldException e) {
            // Unknown field - ignore silently
        }
    });
    
    this.updatedAt = System.currentTimeMillis();
}
```

### Benefits

✅ **Generic** - Works for any field without manual coding  
✅ **Maintainable** - Add new fields to entity, patch automatically works  
✅ **Type-safe** - Automatic type conversion (String → Enum, etc.)  
✅ **Protected fields** - `id` and `createdAt` cannot be modified via patch  
✅ **Handles unknown fields** - Silently ignores fields that don't exist  

### Type Conversion Support

The `convertValue()` helper handles:
- Enum conversions: `"TODO"` → `TaskStatus.TODO`
- Numeric conversions: `123` → `123L` for Long fields
- Null values
- Type matching

### Example

```bash
# Patch any combination of fields
curl -X PATCH http://localhost:8080/tasks/abc123 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "DONE",
    "priority": "LOW",
    "description": "Updated description"
  }'

# Unknown fields are ignored
curl -X PATCH http://localhost:8080/tasks/abc123 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "DONE",
    "unknownField": "ignored"
  }'

# Protected fields are ignored
curl -X PATCH http://localhost:8080/tasks/abc123 \
  -H "Content-Type: application/json" \
  -d '{
    "id": "new-id",        # ← Ignored
    "createdAt": 123456,   # ← Ignored
    "status": "DONE"       # ← Applied
  }'
```

---

## Alternative Approaches Considered

### Option 1: Jackson ObjectMapper (More robust)
Could use Jackson's `ObjectMapper.updateValue()` for better JSON handling:
```java
ObjectMapper mapper = new ObjectMapper();
mapper.updateValue(this, patchData);
```
**Pros**: Better JSON type handling, null support  
**Cons**: Requires ObjectMapper dependency injection

### Option 2: Apache Commons BeanUtils
Could use `BeanUtils.populate()`:
```java
BeanUtils.populate(this, patchData);
```
**Pros**: Well-tested library  
**Cons**: Additional dependency

### Option 3: Manual (Original)
Field-by-field checking  
**Pros**: Explicit control, no reflection  
**Cons**: Verbose, hard to maintain

**We chose reflection** for the best balance of maintainability and control.

---

## Security Considerations

### Protected Fields
The implementation prevents patching of sensitive fields:
- `id` - Resource identifier (immutable)
- `createdAt` - Creation timestamp (immutable)

These are explicitly skipped in `updateFromPatch()`.

### Unknown Fields
Unknown fields in the patch are silently ignored, preventing:
- Accidental field pollution
- Injection of non-existent fields

### Validation
Patch data is still validated through `TaskValidator.validateUpdate()` before being applied.

---

## Summary

Both improvements make the API more:
- **Flexible**: UPSERT support with custom IDs
- **Maintainable**: Reflection-based patching reduces boilerplate
- **Robust**: Type conversions and field protection built-in
- **RESTful**: Better adherence to REST principles

