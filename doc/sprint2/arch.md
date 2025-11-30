## System Architecture

Our entire application follows the MVC (model, view, controller). Classes use dependency injection to prevent tight coupling.

Environment:
- Uses Spring Boot, so JVM required to compile the code.
- Uses Maven build automation tool.

View the diagram here: https://drive.google.com/file/d/1M_xzJZPM8a9yB7DUUNYLAJ-MG-M67DvL/view?usp=sharing

### Backend


#### Startup

General responsibilites:
- The entry point to our Spring Boot application.

| Property | Description |
|-----------|-------------|
| **Class Name** | `YUCircleApplication` |
| **Parent Class** | None |
| **Subclasses** | None |

Description: This is the first class that runs when you do

```
mvn spring-boot:run
```
It scans the entire project to find all the components. 


#### Controllers

General responsibilites:
- Handle incoming API requests from the client.  
- Delegate logic to the appropriate service classes.  
- Return responses with proper HTTP status codes and messages.

Responses:
- 201 HTTP response will be responded when a resource is sucessfully created.
- 200 OK to HTTP response for successfully accessing a resource or for deleting a resource sucessfully.
- 409 CONFLICT will be responded if trying to create a resource that already exists in the database.
- 400 response for when access to a resource is not granted.

| Property | Description |
|-----------|-------------|
| **Class Name** | `AuthController` |
| **Parent Class** | None |
| **Subclasses** | None |

Description: 

Handle authentication and authorization, which includes signup, login, and email verification.

| Property | Description |
|-----------|-------------|
| **Class Name** | `StudentController` |
| **Parent Class** | None |
| **Subclasses** | None |

Description: 

Handle CRUD operations for the student table, which includes, create, read, update, and delete. 

| Property | Description |
|-----------|-------------|
| **Class Name** | `CommentController` |
| **Parent Class** | None |
| **Subclasses** | None |

Description: 

Handle CRUD operations for the comment table, which includes, create, read, update, and delete, for the posts (comments).

#### Services

General responsibilites:
- Handle the business logic for incoming requests.
- Call the data layer to apply the database changes. 

| Property | Description |
|-----------|-------------|
| **Class Name** | `StudentServices` |
| **Parent Class** | None |
| **Subclasses** | None |

Description: 

Handle the business logic for our application. 

| Property | Description |
|-----------|-------------|
| **Class Name** | `CommentService` |
| **Parent Class** | None |
| **Subclasses** | None |

Description: 

Handle comments and business logic for that, calls the DAL (data access layer).

| Property | Description |
|-----------|-------------|
| **Class Name** | `AzureOcrService` |
| **Parent Class** | None |
| **Subclasses** | None |

Description: 

Logic for connecting to AzureOcr, used to parse images of schedules uploaded by user.

#### Repository

General responsibilites:
- Handle the data access layer from backend to database.
- Apply the proper schema (migrations) to our database as necessary.

| Property | Description |
|-----------|-------------|
| **Class Name** | `StudentRepo` |
| **Parent Class** | `JPARepository` |
| **Subclasses** | None |

Description: 

This is the data access layer part of our backend. It mimics what you would normally use in a database to apply changes (SQL). 

| Property | Description |
|-----------|-------------|
| **Class Name** | `CourseRepo` |
| **Parent Class** | `JPARepository` |
| **Subclasses** | None |

Description: 

Used for finding course by its course code.

| Property | Description |
|-----------|-------------|
| **Class Name** | `CourseSessionRepo` |
| **Parent Class** | `JPARepository` |
| **Subclasses** | None |

Description: 

Check if a course session exists.

#### Entities

General responsibilites:
- Mimic the schema for our RDBMS tables. (i.e a student class with fields would be the same as an actual student table in a RDBMS).

| Property | Description |
|-----------|-------------|
| **Class Name** | `Student` |
| **Parent Class** | None |
| **Subclasses** | None |

Description: 

This class repersents the schema for our student table. The annotations @Entity and @Table tells JPA that this is the corresponding schema.
