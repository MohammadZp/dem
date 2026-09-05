## 📚 **Your Exam Topics Breakdown**

| Topic | Maven Focus | Gradle Focus |
|-------|-------------|--------------|
| **Chapter** | Maven: The Definitive Guide - Ch 4 | Gradle in Action - Ch 4 |
| **Main Concept** | Build Lifecycles | Multi-Project & Task Graph |
| **What to know** | Phases, goals, lifecycle binding | Projects, subprojects, task dependencies, profiles |

---

## 🎯 **THEORY: What You Need to Explain**

Let me break down **exactly** what you need to be able to **explain in words** for your exam.

---

## PART 1: Maven Build Lifecycles (Chapter 4)

### **What is a Build Lifecycle?**

> **Definition:** A build lifecycle is a **fixed sequence of phases** that defines the order in which goals are executed. Maven has **3 standard lifecycles**, and the most important is the **default lifecycle**.

### **The 3 Maven Lifecycles:**

1. **clean** - Removes previously built artifacts
2. **default** (or **build**) - Handles project building, testing, and deployment
3. **site** - Generates project documentation

---

### 📝 **EXAM QUESTION 1: Explain the Default Lifecycle**

**Question:** *"What are the key phases of Maven's default build lifecycle?"*

**Your Answer Should Include:**

The default lifecycle has **23 phases**, but the most important are:

```
validate → compile → test → package → verify → install → deploy
```

**Explain each:**

| Phase | What it does |
|-------|--------------|
| **validate** | Validates the project structure and POM is correct |
| **compile** | Compiles the source code |
| **test** | Runs unit tests (using a test framework like JUnit) |
| **package** | Creates the JAR/WAR file |
| **verify** | Runs integration tests to validate the package |
| **install** | Installs the package to local repository (~/.m2) |
| **deploy** | Copies the package to a remote repository |

---

### 📝 **EXAM QUESTION 2: Lifecycle Binding**

**Question:** *"When you run `mvn test`, what actually happens?"*

**Your Answer:**

> When you run `mvn test`, Maven executes **ALL previous phases** in order first. So it runs:
>
> 1. validate
> 2. compile
> 3. test
>
> It does NOT run `package`, `install`, or `deploy` because those come AFTER `test` in the lifecycle.

**Key Concept:** *"Earlier phases ALWAYS run before later phases"*

---

### 📝 **EXAM QUESTION 3: clean vs default lifecycle**

**Question:** *"Explain the difference between `mvn clean` and `mvn clean install`"*

**Your Answer:**

> - `mvn clean` runs the **clean lifecycle** which only deletes the `target/` directory
> - `mvn clean install` runs **two lifecycles**:
    >   1. **clean** - deletes target/
>   2. **default** - runs from validate → compile → test → package → install
>
> The `clean` lifecycle runs first, then the `default` lifecycle runs completely.

---

### 📝 **EXAM QUESTION 4: Plugins and Goals**

**Question:** *"What is the relationship between plugins, goals, and phases in Maven?"*

**Your Answer:**

> - **Plugins** are JAR files that contain **goals** (specific tasks)
> - **Goals** are individual units of work (e.g., `compiler:compile`)
> - **Phases** are positions in the lifecycle that **bind** to goals
>
> **Example:** The `compile` phase is bound to the `compiler:compile` goal of the Maven Compiler Plugin.
>
> You can also run goals directly: `mvn compiler:compile` (bypassing the lifecycle)

---

## PART 2: Gradle Multi-Project & Task Graph (Chapter 4)

### **What is a Multi-Project Build?**

> **Definition:** A Gradle build that has **multiple modules** (projects) that can depend on each other. It consists of a **root project** and one or more **subprojects**.

---

### 📝 **EXAM QUESTION 5: Gradle Project Structure**

**Question:** *"Explain the files needed for a Gradle multi-project build and their purpose."*

**Your Answer:**

| File | Purpose |
|------|---------|
| **`settings.gradle`** | Defines which subprojects exist using `include` |
| **Root `build.gradle`** | Optional configuration that applies to all projects |
| **Subproject `build.gradle`** | Each module's specific configuration |

**Example:**
```groovy
// settings.gradle
include 'core', 'web', 'integration'
```

---

### 📝 **EXAM QUESTION 6: Gradle vs Maven Project Structure**

**Question:** *"How does Gradle's multi-project structure compare to Maven's?"*

**Your Answer:**

| Aspect | Maven | Gradle |
|--------|-------|--------|
| **Parent definition** | `<parent>` in child POM | Automatic from `settings.gradle` |
| **Module declaration** | `<modules>` in parent POM | `include` in `settings.gradle` |
| **Child link to parent** | Explicit in `pom.xml` | Implicit (no need to declare) |
| **Configuration sharing** | `<dependencyManagement>` | `subprojects { }` or `allprojects { }` blocks |

**Key Insight:** In Gradle, subprojects automatically know their parent. In Maven, each child must explicitly declare its parent.

---

### 📝 **EXAM QUESTION 7: Task Graph**

**Question:** *"What is the Gradle task graph and when is it built?"*

**Your Answer:**

> The **task graph** (or **Directed Acyclic Graph - DAG**) represents the dependencies between tasks. Gradle:
>
> 1. **Builds the graph** during the **Configuration Phase**
> 2. **Executes tasks** based on the graph during the **Execution Phase**
>
> **Key Point:** The graph is built BEFORE any task runs. Tasks are NOT executed in the order they appear in the script!

**Example:** If `test` depends on `compileJava`, the graph is:
```
compileJava → test
```

---

### 📝 **EXAM QUESTION 8: Configuration vs Execution**

**Question:** *"Explain the difference between configuration phase and execution phase in Gradle. Why is this important?"*

**Your Answer:**

> **Configuration Phase:**
> - Reads ALL `build.gradle` files
> - Builds the task graph
> - **Code at the top level of `build.gradle` runs here**
> - Runs even if the task is not executed!
>
> **Execution Phase:**
> - Only runs tasks requested (and their dependencies)
> - **Code inside `doFirst` and `doLast` blocks runs here**
>
> **Why it matters:** This is a common exam trick! If you have:
> ```groovy
> task myTask {
>     println "Configuring"   // Runs ALWAYS
>     doLast {
>         println "Executing" // Runs ONLY when task executes
>     }
> }
> ```
> Running `gradle clean` will print "Configuring" but NOT "Executing"!

---

### 📝 **EXAM QUESTION 9: Task Dependencies**

**Question:** *"Explain the difference between `dependsOn`, `mustRunAfter`, and `shouldRunAfter` in Gradle."*

**Your Answer:**

| Keyword | Meaning | Example |
|---------|---------|---------|
| **`dependsOn`** | **Hard dependency** - Task B CANNOT run unless Task A runs | `test.dependsOn compileJava` |
| **`mustRunAfter`** | **Soft ordering** - If both tasks run, B runs after A. But B can run without A | `deploy.mustRunAfter test` |
| **`shouldRunAfter`** | **Soft suggestion** - Same as `mustRunAfter` but can be ignored (rarely used) | `deploy.shouldRunAfter test` |

**Exam Trick:** `dependsOn` = REQUIRED. `mustRunAfter` = ORDERING ONLY.

---

### 📝 **EXAM QUESTION 10: Gradle Profiles**

**Question:** *"Does Gradle have a built-in 'profiles' feature like Maven? If not, how do you achieve the same thing?"*

**Your Answer:**

> **NO**, Gradle does NOT have a built-in profiles system like Maven's `<profiles>`.
>
> **Alternatives in Gradle:**
>
> 1. **Project Properties:** `gradle build -Penv=production`
     >    ```groovy
>    if (project.hasProperty('env') && env == 'production') {
>        // Production config
>    }
>    ```
>
> 2. **`gradle.properties` file:** Set properties in a file
     >    ```properties
>    env=production
>    ```
>
> 3. **Environment Variables:** `System.getenv('BUILD_ENV')`
>
> **Important:** The `--profile` flag in Gradle is for **performance profiling**, NOT environment profiles!

---

## 🆚 **COMPARISON QUESTIONS (Most Important for Exam!)**

### 📝 **EXAM QUESTION 11: Key Differences**

**Question:** *"Compare Maven and Gradle in terms of flexibility, performance, and learning curve."*

**Your Answer:**

| Aspect | Maven | Gradle |
|--------|-------|--------|
| **Configuration** | XML (declarative, rigid) | Groovy/Kotlin (programmable, flexible) |
| **Lifecycle** | Fixed phases | Flexible task graph |
| **Performance** | Slower (no incremental builds) | Faster (incremental builds, daemon, cache) |
| **Learning Curve** | Easier (standard structure) | Steeper (more complex) |
| **Multi-project** | Parent POM with modules | `settings.gradle` with `include` |
| **Profiles** | Built-in (`<profiles>`) | Not built-in (use properties) |

---

### 📝 **EXAM QUESTION 12: Build Speed**

**Question:** *"Why is Gradle generally faster than Maven?"*

**Your Answer:**

Gradle is faster because:

1. **Incremental Builds:** Only recompiles changed files (Maven recompiles everything)
2. **Build Cache:** Reuses outputs from previous builds
3. **Gradle Daemon:** Keeps a JVM running in the background to avoid startup time
4. **Parallel Execution:** Can run independent tasks concurrently

---

### 📝 **EXAM QUESTION 13: Dependency Scopes (Maven) vs Configurations (Gradle)**

**Question:** *"Translate Maven dependency scopes to Gradle configurations."*

**Your Answer:**

| Maven Scope | Gradle Configuration | When used |
|-------------|---------------------|-----------|
| `compile` | `implementation` | Main source code (most common) |
| `test` | `testImplementation` | Test source code |
| `provided` | `compileOnly` | Compile-time only, provided at runtime |
| `runtime` | `runtimeOnly` | Runtime only, not needed to compile |
| `system` | Not recommended | Use `compileOnly` or `implementation` |

---

## 📋 **EXAM CHEAT SHEET (Must Memorize!)**

### Maven Key Points:
1. **3 Lifecycles:** clean, default (build), site
2. **Default phases:** validate → compile → test → package → verify → install → deploy
3. **Earlier phases ALWAYS run** before later phases
4. **Plugins** contain **goals** bound to phases
5. **Parent POM** uses `<modules>` and `<dependencyManagement>`
6. **Profiles** are built-in with `<profiles>`

### Gradle Key Points:
1. **Multi-project:** `settings.gradle` with `include`
2. **Task Graph:** Built in **Configuration Phase**, executed in **Execution Phase**
3. **Top-level code runs ALWAYS** (even if task not executed)
4. **`doFirst`/`doLast`** run ONLY when task executes
5. **No built-in profiles** - use `-P` properties or environment variables
6. **`dependsOn`** = hard dependency, **`mustRunAfter`** = ordering only

---

## 📝 **FINAL EXAM TIPS**

1. **For Maven questions:** Always mention the **fixed lifecycle** and **convention over configuration**
2. **For Gradle questions:** Always mention the **flexible task graph**, **configuration vs execution phases**, and **incremental builds**
3. **Comparison questions:** Structure your answer with clear advantages/disadvantages of each
4. **Trick questions:** Remember that Gradle's `--profile` is for **performance**, NOT environment profiles!

---
