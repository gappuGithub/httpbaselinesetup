The coding will include coding a web API / service in any language of your choice.
● To prepare, we recommend that you come with a small amount of code for a sample API endpoint and you are ready to start an HTTP server and to make requests against that server.



I have comeup with this task -

# 🎯 Question: “Team Task-Tracker API”
# Problem Statement
Build a simple RESTful web service to manage “Tasks” for teams.
Each task has at least: id, title, description, status, priority, created_at, updated_at.
status can be e.g. ["todo", "in_progress", "done"].
priority can be e.g. ["low", "medium", "high"].

# Your API should support the following endpoints:
- POST /tasks — create a new task
- GET /tasks/{id} — list all tasks, optionally support filtering by status or priority via query params
- BATCH GET /tasks/{list of id} — get details of a single task
- PATCH /tasks/{id} — partial update (e.g. update status or description only)
- DELETE /tasks/{id} — delete a task

# Additional requirements / constraints:
- Use in-memory storage (e.g. a list or map) so you don’t need a real database — enough to demonstrate API logic within 60 mins.
- Ensure proper HTTP semantics: correct status codes (201 for create, 200 for read/update, 204 or 200 for delete, 404 when task not found, 400 for invalid input, etc).
- Validate input: e.g. status and priority must be one of allowed values; title cannot be empty. Return meaningful error responses.



# Ask : Can you perform these actions:
- A boilerplate api resource class for all above CRUD operations. 
- Boiler plate code to start a http server for this application.
- Instructions on how to start the HTTP server and curl against this resource using curl in a readme.md
- At a high level, this is the API contract
    - GET takes an ID returns the Resource object e.g. Course
    - Batchget takes a list of ID and returns a Collection. Collection contains two maps:
        1. map of ID -> Resource object
        2. map of ID -> Http Error codes (for IDs that errored out)
    - UPSERT - Takes the Resource object as body and upserts the record. returns object with proper http code. The ID is minted server side. 
    - PATCH - takes the patch. Patches the record in the backend. 404 is record doesn't exist. 
    - DELETE - Deletes the record with given ID. returns proper http error code.
    
## Lets first start with just the resource. Everything can be not implemented. In next iteration, we build on this to add the service implementation and validation etc.