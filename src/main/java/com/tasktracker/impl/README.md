# Implementation Package (impl/)

This package contains **application-specific implementation** for the Task Tracker use case. These files implement the business logic for managing tasks.

## 📦 Package Structure

```
impl/
├── models/
│   ├── Task.java           # Task entity (implements Entity<String>)
│   ├── TaskStatus.java     # Task status enum (TODO, IN_PROGRESS, DONE)
│   └── TaskPriority.java   # Task priority enum (LOW, MEDIUM, HIGH)
├── storage/
│   └── TaskStore.java      # Task-specific storage (extends InMemoryStore)
├── validators/
│   └── TaskValidator.java  # Task validation rules
└── controllers/
    └── TaskResource.java   # Task REST API endpoints
```

## 🎯 Purpose

These files contain **Task-specific business logic**. When building a different application (e.g., User Management, Order System), you would **replace this entire package** with your own implementation.

## 🔄 For New Use Cases

### Replace This Package When:
- Building a different application
- Adding a new entity type (User, Order, Project, etc.)
- Implementing different business rules

### What to Keep:
- ✅ The `framework/` package (stays unchanged)
- ✅ The overall patterns and structure
- ✅ Configuration files (modify as needed)

## 📋 Components

### models/Task.java
- **Implements**: `Entity<String>` from framework
- **Purpose**: Defines the Task entity with fields like title, description, status, priority
- **Contains**: Business fields, validation annotations, update logic

### models/TaskStatus.java
- **Type**: Enum
- **Values**: TODO, IN_PROGRESS, DONE
- **Purpose**: Valid statuses for a task

### models/TaskPriority.java
- **Type**: Enum
- **Values**: LOW, MEDIUM, HIGH
- **Purpose**: Priority levels for a task

### storage/TaskStore.java
- **Extends**: `InMemoryStore<Task, String>` from framework
- **Purpose**: Task-specific storage with custom filtering
- **Features**: Filters by status, priority, and title

### validators/TaskValidator.java
- **Implements**: `ResourceValidator<Task>` from framework
- **Purpose**: Task-specific validation rules
- **Validates**:
  - Title: required, max 200 chars
  - Description: optional, max 1000 chars
  - Status: must be valid enum value
  - Priority: must be valid enum value

### controllers/TaskResource.java
- **Type**: REST Controller
- **Base Path**: `/tasks`
- **Purpose**: HTTP endpoints for Task CRUD operations
- **Endpoints**:
  - POST /tasks - Create task
  - GET /tasks/{id} - Get by ID
  - POST /tasks/batch - Batch get
  - GET /tasks - List with filters
  - PATCH /tasks/{id} - Update task
  - DELETE /tasks/{id} - Delete task

## 🚀 Example: Creating a User Implementation

To replace this with User management:

```
impl/
├── models/
│   ├── User.java           # Replace Task with User entity
│   ├── UserRole.java       # Replace TaskStatus with UserRole
│   └── UserStatus.java     # Replace TaskPriority with UserStatus
├── storage/
│   └── UserStore.java      # Replace TaskStore with UserStore
├── validators/
│   └── UserValidator.java  # Replace TaskValidator with UserValidator
└── controllers/
    └── UserResource.java   # Replace TaskResource with UserResource
```

**The framework stays the same!**

## 📊 Dependency on Framework

All classes in this package depend on the framework:

```
impl/models/Task → framework/models/Entity
impl/storage/TaskStore → framework/storage/InMemoryStore
impl/validators/TaskValidator → framework/validators/ResourceValidator
impl/controllers/TaskResource → framework/models/Collection
```

## ✨ Quick Reference

### To Customize for Your Use Case:
1. Replace `Task` with your entity name throughout
2. Update entity fields and validation rules
3. Modify enums to match your domain
4. Implement your custom filtering logic in Store
5. Add your business logic in Resource endpoints

**Time to implement: ~1-2 hours for a new entity type**

