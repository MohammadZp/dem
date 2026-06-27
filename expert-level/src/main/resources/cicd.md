# Maven Lifecycle & GitHub Actions (CI/CD)

---

# Maven

## What is Maven?

Maven is a **build automation** and **dependency management** tool for Java projects.

It helps developers:

- Compile source code
- Run tests
- Download dependencies
- Package applications (JAR/WAR)

---

# Maven Lifecycle

The basic Maven lifecycle phases are:

```
compile
    ↓
test
    ↓
package
```

Each phase automatically executes the previous phases.

---

## Compile

### Command

```bash
mvn compile
```

### Purpose

- Compiles Java source code
- Produces `.class` files
- Does **not** run tests
- Does **not** create a JAR

---

## Test

### Command

```bash
mvn test
```

### Purpose

- Compiles test code
- Runs unit tests (JUnit)
- Stops the build if any test fails
- Does **not** package the application

---

## Package

### Command

```bash
mvn package
```

### Purpose

- Compiles source code
- Runs all tests
- Creates the final application package

Produces:

- `.jar`
- `.war`

---

# Maven Lifecycle Summary

| Phase | What it does |
|--------|--------------|
| compile | Compiles Java source code |
| test | Runs unit tests |
| package | Creates JAR/WAR |

---

# GitHub Actions

## What is GitHub Actions?

GitHub Actions is GitHub's **CI/CD platform**.

It automatically performs tasks when events occur, such as:

- Push
- Pull Request
- Release

---

## Workflow

A workflow is a YAML file that defines automated tasks.

Location:

```
.github/workflows/
```

Example:

```
.github/workflows/build.yml
```

---

# Continuous Integration (CI)

## What is CI?

Continuous Integration (CI) automatically **builds and tests** code whenever developers push changes.

### CI Pipeline

```
Developer
     │
     ▼
Push to GitHub
     │
     ▼
GitHub Actions
     │
     ▼
Compile
     │
     ▼
Run Tests
     │
     ▼
Pass / Fail
```

### Benefits

- Detects bugs early
- Runs tests automatically
- Prevents broken code from being merged
- Ensures the project builds successfully

---

# Continuous Deployment (CD)

## What is CD?

Continuous Deployment (CD) automatically **deploys** the application after it successfully passes all build and test stages.

### CD Pipeline

```
Developer
     │
     ▼
Push
     │
     ▼
Build
     │
     ▼
Test
     │
     ▼
Package
     │
     ▼
Deploy
```

Deployment means making the application available to users (e.g., on a server or cloud platform).

---

# CI vs CD

| Continuous Integration (CI) | Continuous Deployment (CD) |
|------------------------------|----------------------------|
| Builds code automatically | Deploys code automatically |
| Runs automated tests | Releases the application |
| Focuses on code quality | Focuses on software delivery |
| Stops if tests fail | Deploys only after successful builds/tests |

---

# GitHub Actions Pipeline

A basic Java GitHub Actions workflow typically performs:

1. Checkout repository
2. Set up Java
3. Compile project
4. Run tests (`mvn test`)
5. Package application (`mvn package`)
6. Deploy (optional)

---

## Example Workflow

```yaml
name: Java CI

on: [push]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 21

      - run: mvn test
```

---

# High-Priority Exam Questions

## What is Maven?

A build automation and dependency management tool for Java projects.

---

## What are the three main Maven lifecycle phases?

- Compile
- Test
- Package

---

## What does `mvn compile` do?

Compiles Java source code into `.class` files.

---

## What does `mvn test` do?

Runs all unit tests. If a test fails, the build stops.

---

## What does `mvn package` do?

Compiles the project, runs all tests, and creates a JAR or WAR file.

---

## What is GitHub Actions?

GitHub Actions is GitHub's built-in CI/CD platform that automates building, testing, and deploying applications.

---

## What is Continuous Integration (CI)?

Automatically building and testing code whenever developers push changes.

---

## What is Continuous Deployment (CD)?

Automatically deploying an application after it has successfully passed all build and test stages.

---

## Difference Between CI and CD

| CI | CD |
|----|----|
| Build + Test | Deploy |
| Detects problems early | Releases software automatically |

---

# Memory Tips

### Maven

```
compile
    ↓
test
    ↓
package
```

Remember:

- **Compile** → Build the code
- **Test** → Verify the code
- **Package** → Create the application

---

### CI/CD

```
Write Code
      ↓
Push
      ↓
Build
      ↓
Test
      ↓
Package
      ↓
Deploy
```

Easy way to remember:

- **CI = Build + Test**
- **CD = Deploy**