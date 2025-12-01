# Architecture: Generic Framework vs Application Code

## 📦 Package Structure

```
com.tasktracker/
├── 🔧 framework/                           ← GENERIC (Reusable boilerplate)
│   ├── models/
│   │   ├── Entity.java                     ← Interface for all entities
│   │   └── Collection.java                 ← BatchGet response wrapper
│   ├── storage/
│   │   ├── ResourceStorageClient.java      ← Storage interface
│   │   └── InMemoryStore.java              ← In-memory implementation
│   └── validators/
│       ├── ResourceValidator.java          ← Validator interface
│       └── ValidationException.java        ← Validation exception
│
├── 📝 impl/                                ← APPLICATION IMPLEMENTATION (Task-specific)
│   ├── models/
│   │   ├── Task.java                       ← Task entity (implements Entity<String>)
│   │   ├── TaskStatus.java                 ← Task status enum
│   │   └── TaskPriority.java               ← Task priority enum
│   ├── storage/
│   │   └── TaskStore.java                  ← Task storage (extends InMemoryStore)
│   ├── validators/
│   │   └── TaskValidator.java              ← Task validation logic
│   └── controllers/
│       └── TaskResource.java               ← Task REST endpoints
│
├── ⚙️ config/                              ← APPLICATION-SPECIFIC
│   └── GlobalExceptionHandler.java         ← Error handling
│
└── 🚀 TaskTrackerApplication.java          ← APPLICATION-SPECIFIC (Main class)
```

---

## 🔧 Generic Framework (Don't Touch for New Use Cases)

### Purpose
These files form the **reusable infrastructure** that can be used for ANY entity type in ANY project.

### When to Modify
- ✅ When enhancing the framework itself (e.g., adding new generic features)
- ✅ When fixing bugs in the generic infrastructure
- ❌ **NEVER for application-specific logic**

### Files
| File | Purpose | Generic Parameters |
|------|---------|-------------------|
| `Entity.java` | Base interface for all entities | `<ID>` |
| `Collection.java` | BatchGet response wrapper | `<K, T>` |
| `ResourceStorageClient.java` | Storage contract | `<T extends Entity<ID>, ID>` |
| `InMemoryStore.java` | Thread-safe storage implementation | `<T extends Entity<ID>, ID>` |
| `ResourceValidator.java` | Validator contract | `<T extends Entity<?>>` |
| `ValidationException.java` | Validation exception | N/A |

### Copy to New Projects
Simply copy the entire `framework/` package to any new project and it's ready to use!

---

## 📝 Application Implementation (impl/ - Replace for New Use Cases)

### Purpose
The `impl/` package contains the **business logic** for the Task Tracker application. This entire package should be replaced when building a different application.

### When to Modify
- ✅ Always! This is where your application logic lives
- ✅ When building a new entity (User, Order, etc.), replace the entire `impl/` package

### Files in impl/ Package
| File | Purpose | What to Replace |
|------|---------|----------------|
| `impl/models/Task.java` | Task entity definition | Your entity (User, Order, etc.) |
| `impl/models/TaskStatus.java` | Task-specific enum | Your entity's enums |
| `impl/models/TaskPriority.java` | Task-specific enum | Your entity's enums |
| `impl/storage/TaskStore.java` | Task storage logic | Your entity's storage (if customization needed) |
| `impl/validators/TaskValidator.java` | Task validation rules | Your entity's validation rules |
| `impl/controllers/TaskResource.java` | Task REST endpoints | Your entity's REST endpoints |

### Files Outside impl/ Package
| File | Purpose | What to Replace |
|------|---------|----------------|
| `TaskTrackerApplication.java` | Main application class | Rename for your app |
| `config/GlobalExceptionHandler.java` | Error handling | Customize for your app |

---

## 🎯 Example: Adding a User Entity

### What You Keep (Framework)
- ✅ `Entity<ID>` interface
- ✅ `Collection<K, T>` class
- ✅ `ResourceStorageClient<T, ID>` interface
- ✅ `InMemoryStore<T, ID>` implementation
- ✅ `ResourceValidator<T>` interface
- ✅ `ValidationException` class

### What You Create (Application-Specific in impl/ package)
```java
// Create everything in com.yourapp.impl/ package

// 1. impl/models/User.java
package com.yourapp.impl.models;
public class User implements Entity<String> {
    private String id;
    private String email;
    private String name;
    // ... implement Entity interface
}

// 2. impl/storage/UserStore.java (optional)
package com.yourapp.impl.storage;
@Repository
public class UserStore extends InMemoryStore<User, String> {
    // Add custom filtering logic if needed
}

// 3. impl/validators/UserValidator.java
package com.yourapp.impl.validators;
@Component
public class UserValidator implements ResourceValidator<User> {
    // Add validation rules
}

// 4. impl/controllers/UserResource.java
package com.yourapp.impl.controllers;
@RestController
@RequestMapping("/users")
public class UserResource {
    // Add REST endpoints
}
```

---

## 🚀 Quick Reference

### Generic Framework Features
- ✅ Type-safe CRUD operations
- ✅ Automatic ID generation (UUIDs for String IDs)
- ✅ Timestamp management (createdAt, updatedAt)
- ✅ Batch operations with error tracking
- ✅ Thread-safe in-memory storage
- ✅ Generic validation contract
- ✅ Partial updates (PATCH support)

### What You Implement Per Entity
- Define entity fields and enums
- Define validation rules
- Implement REST endpoints
- (Optional) Customize storage/filtering

---

## 📊 Dependency Flow

```
Application Code (Your Logic)
       ↓
   Uses ↓
       ↓
Generic Framework (Reusable)
```

**Example:**
```
TaskResource → TaskValidator → Task → Entity (framework)
     ↓              ↓            ↓
  TaskStore  →  InMemoryStore (framework)
     ↓
Collection (framework)
```

---

## ✨ Benefits of This Separation

1. **Clarity**: Instantly identify what's generic vs specific
2. **Reusability**: Copy `framework/` to any project
3. **Maintainability**: Framework improvements benefit all entities
4. **Onboarding**: New developers know where to focus
5. **Consistency**: All entities follow the same patterns
6. **Testability**: Test framework independently from business logic

---

## 📋 Checklist for New Use Cases

- [ ] Copy `framework/` package to new project (unchanged)
- [ ] Delete or replace entire `impl/` package
- [ ] Create `impl/models/YourEntity.java` implementing `Entity<ID>`
- [ ] Create entity-specific enums in `impl/models/` (if needed)
- [ ] Create `impl/storage/YourEntityStore.java` (optional, extends `InMemoryStore`)
- [ ] Create `impl/validators/YourEntityValidator.java` (implements `ResourceValidator`)
- [ ] Create `impl/controllers/YourEntityResource.java` with REST endpoints
- [ ] Update main application class name
- [ ] Update configuration files

**Time to implement a new entity: ~30 minutes!**
**Time to replace entire application: ~1-2 hours!**

