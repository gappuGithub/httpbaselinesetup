# Project Structure Overview

## 📁 Clean Package Organization

```
com.tasktracker/
│
├── 🔧 framework/                    ← GENERIC (Copy to any project)
│   ├── models/
│   │   ├── Entity.java              ← Generic entity interface
│   │   └── Collection.java          ← Generic batch response
│   ├── storage/
│   │   ├── ResourceStorageClient.java  ← Storage contract
│   │   └── InMemoryStore.java       ← Generic implementation
│   └── validators/
│       ├── ResourceValidator.java   ← Validator contract
│       └── ValidationException.java ← Validation error
│
├── 📝 impl/                         ← TASK IMPLEMENTATION (Replace for new use cases)
│   ├── models/
│   │   ├── Task.java                ← Task entity
│   │   ├── TaskStatus.java          ← Task status enum
│   │   └── TaskPriority.java        ← Task priority enum
│   ├── storage/
│   │   └── TaskStore.java           ← Task storage
│   ├── validators/
│   │   └── TaskValidator.java       ← Task validation
│   └── controllers/
│       └── TaskResource.java        ← Task REST endpoints
│
├── ⚙️ config/
│   └── GlobalExceptionHandler.java  ← Error handling
│
└── 🚀 TaskTrackerApplication.java   ← Main application
```

---

## 🎯 What Goes Where

### framework/ - Generic Boilerplate
**Purpose**: Reusable infrastructure for any entity type  
**Modify**: Only when enhancing the framework itself  
**Copy**: To any new project as-is

| Component | Description |
|-----------|-------------|
| `Entity<ID>` | Base interface all entities must implement |
| `Collection<K,T>` | Wrapper for batch get responses with results + errors |
| `ResourceStorageClient<T,ID>` | Storage interface defining CRUD operations |
| `InMemoryStore<T,ID>` | Thread-safe in-memory storage implementation |
| `ResourceValidator<T>` | Validation interface for create/update |
| `ValidationException` | Exception with field-level error details |

---

### impl/ - Application Implementation
**Purpose**: Task-specific business logic  
**Modify**: Always! This is your application code  
**Replace**: Entire package when building a different app

| Component | Description |
|-----------|-------------|
| `Task` | Task entity with title, description, status, priority |
| `TaskStatus` | Enum: TODO, IN_PROGRESS, DONE |
| `TaskPriority` | Enum: LOW, MEDIUM, HIGH |
| `TaskStore` | Extends InMemoryStore with Task-specific filtering |
| `TaskValidator` | Validation rules for Task creation/updates |
| `TaskResource` | REST endpoints: POST, GET, PATCH, DELETE /tasks |

---

## 🔄 For a New Use Case (e.g., User Management)

### Step 1: Keep framework/ (unchanged)
```
✅ framework/models/Entity.java
✅ framework/models/Collection.java
✅ framework/storage/ResourceStorageClient.java
✅ framework/storage/InMemoryStore.java
✅ framework/validators/ResourceValidator.java
✅ framework/validators/ValidationException.java
```

### Step 2: Replace impl/ with your entity
```
❌ Delete impl/ (or rename to impl.task for reference)

✨ Create new impl/
   ├── models/
   │   ├── User.java             ← Implements Entity<String>
   │   ├── UserRole.java         ← Your enum
   │   └── UserStatus.java       ← Your enum
   ├── storage/
   │   └── UserStore.java        ← Extends InMemoryStore<User, String>
   ├── validators/
   │   └── UserValidator.java    ← Implements ResourceValidator<User>
   └── controllers/
       └── UserResource.java     ← @RestController @RequestMapping("/users")
```

### Step 3: Update main class
```
Rename: TaskTrackerApplication → YourAppApplication
```

**Done! You have a new application with the same infrastructure.**

---

## 📊 Dependency Flow

```
impl/controllers/TaskResource
    ↓ uses
impl/storage/TaskStore → framework/storage/InMemoryStore
    ↓ uses
impl/models/Task → framework/models/Entity
    ↓ uses
impl/validators/TaskValidator → framework/validators/ResourceValidator
    ↓ returns
framework/models/Collection
```

---

## ✨ Benefits of This Organization

| Benefit | Description |
|---------|-------------|
| **Crystal Clear** | Instantly see what's generic vs specific |
| **Reusable** | Copy `framework/` to any project |
| **Replaceable** | Replace `impl/` for different use cases |
| **Maintainable** | Framework updates benefit all implementations |
| **Consistent** | All implementations follow same patterns |
| **Fast Development** | New entities in ~30 minutes |

---

## 📝 Quick Reference

### Files You Never Touch (for new use cases)
- Everything in `framework/` package ✅

### Files You Always Replace (for new use cases)
- Everything in `impl/` package 🔄

### Files You Customize (for new use cases)
- `TaskTrackerApplication.java` (rename)
- `config/GlobalExceptionHandler.java` (customize)
- `application.properties` (adjust settings)

---

## 🚀 Getting Started

1. **For Task Tracker**: Ready to run! Just `mvn spring-boot:run`
2. **For New Entity**: Copy `framework/`, replace `impl/`, done!
3. **For New Project**: Copy entire structure, customize as needed

