# Generic Framework Package

This package contains **reusable, generic components** that can be used for any entity type. These are the boilerplate files that provide the infrastructure for building REST APIs with CRUD operations.

## 📦 Package Structure

```
framework/
├── models/
│   ├── Entity.java          # Generic base interface for all entities
│   └── Collection.java      # Generic BatchGet response wrapper
├── storage/
│   ├── ResourceStorageClient.java  # Generic storage interface
│   └── InMemoryStore.java          # Generic in-memory implementation
└── validators/
    ├── ResourceValidator.java      # Generic validator interface
    └── ValidationException.java    # Validation exception class
```

## 🎯 Purpose

These files form the **generic framework** that you can reuse across different projects or entity types. When creating a new entity (e.g., User, Project, Order), you only need to:

1. Create your entity class implementing `Entity<ID>`
2. Extend `InMemoryStore<YourEntity, ID>` for storage (optional, can use the base class)
3. Implement `ResourceValidator<YourEntity>` for validation
4. Create a REST resource class with endpoints

**Everything in this package stays the same!**

## 📋 Components

### models/Entity.java
- **Type**: Interface
- **Generic Parameters**: `<ID>` - Type of identifier (String, Long, UUID, etc.)
- **Purpose**: Base contract for all entities
- **Provides**: 
  - `getId()`, `setId(ID)`
  - `getCreatedAt()`, `setCreatedAt(LocalDateTime)`
  - `getUpdatedAt()`, `setUpdatedAt(LocalDateTime)`
  - `updateFromPatch(Map<String, Object>)`

### models/Collection.java
- **Type**: Class
- **Generic Parameters**: `<K, T>` - Key type and Entity type
- **Purpose**: Universal BatchGet response wrapper
- **Fields**:
  - `results`: Map<K, T> - Successfully retrieved entities
  - `errors`: Map<K, Integer> - Failed IDs with HTTP error codes

### storage/ResourceStorageClient.java
- **Type**: Interface
- **Generic Parameters**: `<T extends Entity<ID>, ID>`
- **Purpose**: Generic storage contract for CRUD operations
- **Methods**:
  - `T create(T entity)`
  - `Optional<T> get(ID id)`
  - `Collection<ID, T> batchGet(List<ID> ids)`
  - `List<T> listAll(Map<String, String> filters)`
  - `Optional<T> update(ID id, T entity)`
  - `boolean delete(ID id)`
  - `boolean exists(ID id)`

### storage/InMemoryStore.java
- **Type**: Class (implements ResourceStorageClient)
- **Generic Parameters**: `<T extends Entity<ID>, ID>`
- **Purpose**: Thread-safe in-memory storage implementation
- **Features**:
  - Uses `ConcurrentHashMap` for thread safety
  - Auto-generates UUIDs for String-based IDs
  - Manages timestamps (createdAt, updatedAt)
  - Supports batch operations with error tracking

### validators/ResourceValidator.java
- **Type**: Interface
- **Generic Parameters**: `<T extends Entity<?>>`
- **Purpose**: Generic validator contract
- **Methods**:
  - `void validateCreate(T entity) throws ValidationException`
  - `void validateUpdate(Map<String, Object> patchData) throws ValidationException`

### validators/SchemaValidator.java
- **Type**: Class
- **Generic Parameters**: `<T extends Entity<?>>`
- **Purpose**: Framework-level type and schema validation
- **Features**:
  - Validates field types match entity schema
  - Checks for unknown fields
  - Handles type conversions (String → Enum)
  - Fails fast on type mismatches
- **Methods**:
  - `validatePatchSchema(Map<String, Object>)` - Validate PATCH data types
  - `validateCreateSchema(T entity)` - Validate entity field types

### validators/ValidationException.java
- **Type**: Exception Class
- **Purpose**: Exception for validation failures
- **Fields**:
  - `errors`: Map<String, String> - Field-level error messages

## 🚀 Usage Example

### Creating a New Entity Type (e.g., User)

```java
// 1. Define your entity
public class User implements Entity<String> {
    private String id;
    private String email;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Implement Entity interface methods
    // Add your custom fields
}

// 2. Extend the storage (optional customization)
@Repository
public class UserStore extends InMemoryStore<User, String> {
    // Add custom filtering or storage logic if needed
    // Or just use InMemoryStore<User, String> directly
}

// 3. Implement validator
@Component
public class UserValidator implements ResourceValidator<User> {
    private final SchemaValidator<User> schemaValidator;
    
    public UserValidator() {
        this.schemaValidator = new SchemaValidator<>(User.class);
    }
    
    @Override
    public void validateCreate(User user) throws ValidationException {
        // Framework-level schema validation
        schemaValidator.validateCreateSchema(user);
        
        // Entity-specific business rules
        // Add your validation rules here
    }
    
    @Override
    public void validateUpdate(Map<String, Object> patchData) throws ValidationException {
        // Framework-level schema validation
        schemaValidator.validatePatchSchema(patchData);
        
        // Entity-specific business rules
        // Add your update validation rules here
    }
}

// 4. Create REST resource
@RestController
@RequestMapping("/users")
public class UserResource {
    private final UserStore userStore;
    private final UserValidator userValidator;
    
    // Implement endpoints using the same patterns as TaskResource
}
```

## ✅ Benefits

- **Reusability**: Write once, use for any entity
- **Type Safety**: Full compile-time type checking
- **Consistency**: All entities follow the same patterns
- **Maintainability**: Framework updates propagate to all entities
- **Testability**: Test generic components independently

## 📝 Notes

- **DO NOT modify** these files for entity-specific logic
- Entity-specific code belongs in `com.tasktracker.models`, `com.tasktracker.storage`, etc.
- These files can be copied to new projects and used as-is
- The framework is designed to be language-agnostic in its patterns

