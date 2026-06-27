# REST URL Design Best Practices

## 1. Use Nouns, Not Verbs

A URL should represent a **resource (noun)**, not an action (verb).

### ✅ Good

```text
/students
/books
/orders
```

### ❌ Bad

```text
/getStudents
/createStudent
/deleteBook
```

**Reason:** The HTTP method already specifies the action.

| HTTP Method | Action         |
| ----------- | -------------- |
| GET         | Retrieve       |
| POST        | Create         |
| PUT         | Update/Replace |
| PATCH       | Partial Update |
| DELETE      | Delete         |

---

## 2. Use Plural Nouns

Resources usually represent collections.

### ✅ Good

```text
/students
/books
/employees
```

### ❌ Bad

```text
/student
/book
/employee
```

---

## 3. Use Hierarchical URLs

Show relationships between resources.

### Example

```text
/students/10/courses
/orders/25/items
```

Meaning:

* Courses that belong to student **10**
* Items that belong to order **25**

---

## 4. Use Path Variables for Resource IDs

Every resource should have a unique identifier.

Examples:

```http
GET /students/5
```

```http
PUT /students/5
```

```http
DELETE /students/5
```

---

## 5. Use Query Parameters for Filtering and Searching

Instead of creating many URLs, use query parameters.

Examples:

```text
/students?age=20
/books?author=John
/products?category=Laptop
```

---

## 6. Keep URLs Simple and Readable

### ✅ Good

```text
/customers
/customers/5
/orders/10
```

### ❌ Bad

```text
/CustomerManagementSystem/GetAllCustomersData
```

---

## 7. Use Lowercase Letters

### ✅ Good

```text
/students
```

### ❌ Bad

```text
/Students
/STUDENTS
```

---

## 8. Use Hyphens (-), Not Underscores (_)

### ✅ Good

```text
/student-records
```

### ❌ Bad

```text
/student_records
```

---

## 9. Do Not Include File Extensions

### ✅ Good

```text
/students
```

### ❌ Bad

```text
/students.json
/students.xml
```

The response format should be determined by HTTP headers, not by the URL.

---

## 10. Use HTTP Methods Correctly

| HTTP Method | Purpose                     | Example              |
| ----------- | --------------------------- | -------------------- |
| GET         | Retrieve resources          | `GET /students`      |
| POST        | Create a resource           | `POST /students`     |
| PUT         | Replace/update a resource   | `PUT /students/5`    |
| PATCH       | Partially update a resource | `PATCH /students/5`  |
| DELETE      | Delete a resource           | `DELETE /students/5` |

---

## 11. Use Proper HTTP Status Codes

| Status Code                   | Meaning                                  |
| ----------------------------- | ---------------------------------------- |
| **200 OK**                    | Request successful                       |
| **201 Created**               | Resource created successfully            |
| **204 No Content**            | Successful request with no response body |
| **400 Bad Request**           | Invalid request from the client          |
| **404 Not Found**             | Resource does not exist                  |
| **500 Internal Server Error** | Server error                             |

---

## 12. Version Your API

Include the version in the URL when introducing breaking changes.

Example:

```text
/api/v1/students
/api/v2/students
```

---

# Good REST API Example

| Operation                | HTTP Method | URL           |
| ------------------------ | ----------- | ------------- |
| Get all students         | GET         | `/students`   |
| Get student by ID        | GET         | `/students/5` |
| Create student           | POST        | `/students`   |
| Update student           | PUT         | `/students/5` |
| Partially update student | PATCH       | `/students/5` |
| Delete student           | DELETE      | `/students/5` |

---

# Poor REST API Example

| ❌ URL                          | Problem                                     |
| ------------------------------ | ------------------------------------------- |
| `/getStudents`                 | Uses a verb instead of a noun               |
| `/deleteStudent/5`             | Action should be the HTTP method (`DELETE`) |
| `/createStudent`               | Should use `POST /students`                 |
| `/StudentDataManagementSystem` | Too long and unclear                        |
| `/students.json`               | File extension should not be in the URL     |

---

# Request Bodies in HTTP Methods

| HTTP Method | Request Body? | Typical Purpose                    |
| ----------- | ------------- | ---------------------------------- |
| GET         | ❌ No          | Retrieve data                      |
| POST        | ✅ Yes         | Create a new resource              |
| PUT         | ✅ Yes         | Replace or fully update a resource |
| PATCH       | ✅ Yes         | Partially update a resource        |
| DELETE      | ⚠️ Usually No | Delete a resource                  |

### Examples

#### POST

```http
POST /students
Content-Type: application/json

{
  "name": "Alice",
  "age": 20
}
```

#### PUT

```http
PUT /students/5

{
  "name": "Alice",
  "age": 21
}
```

#### PATCH

```http
PATCH /students/5

{
  "age": 22
}
```

#### DELETE

```http
DELETE /students/5
```

---

# Exam Summary

## REST URL Design Rules

* Use **nouns**, not verbs.
* Use **plural** resource names.
* Use **path variables** for resource IDs.
* Use **query parameters** for filtering and searching.
* Keep URLs **short and readable**.
* Use **lowercase** letters.
* Use **hyphens (-)** instead of underscores (_).
* Do **not** include file extensions.
* Use the correct **HTTP methods**.
* Use appropriate **HTTP status codes**.
* **Version** your API when making breaking changes.

## HTTP Methods with Request Bodies

* ✅ POST
* ✅ PUT
* ✅ PATCH
* ❌ GET
* ⚠️ DELETE (usually no body in REST APIs)

## Memory Tip

* **POST** → Create → Body ✅
* **PUT** → Full Update → Body ✅
* **PATCH** → Partial Update → Body ✅
* **GET** → Retrieve → No Body ❌
* **DELETE** → Delete → Usually No Body ⚠️
