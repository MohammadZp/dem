# 📋 **MAVEN EXAM CHEAT SHEET**

## Everything You Need to Know for Your Exam!

---

## 1. WHAT IS MAVEN?

> **Definition:** Maven is a **build automation** and **project management** tool used primarily for Java projects. It uses **XML** for configuration (pom.xml) and follows the principle of **"Convention over Configuration."**

**Key Concepts:**
- **POM** = Project Object Model (the `pom.xml` file)
- **Convention over Configuration** = Maven has default settings, so you only need to specify what's different
- **Declarative** = You DECLARE what you want, not how to do it

---

## 2. BUILD LIFECYCLES (MOST IMPORTANT TOPIC!)

### Maven Has 3 Standard Lifecycles:

| Lifecycle | Purpose |
|-----------|---------|
| **clean** | Deletes the `target/` directory (removes compiled files) |
| **default (build)** | Compiles, tests, packages, installs, and deploys |
| **site** | Generates project documentation and reports |

---

### The DEFAULT (Build) Lifecycle - 23 Phases

**You MUST memorize these 7 key phases (in order):**

```
validate → compile → test → package → verify → install → deploy
```

| Phase | What It Does | Command Example |
|-------|--------------|-----------------|
| **validate** | Checks project structure and POM is valid | `mvn validate` |
| **compile** | Compiles source code into `target/classes/` | `mvn compile` |
| **test** | Runs unit tests (JUnit/TestNG) | `mvn test` |
| **package** | Creates JAR/WAR/EAR in `target/` | `mvn package` |
| **verify** | Runs integration tests | `mvn verify` |
| **install** | Copies artifact to local repo (~/.m2) | `mvn install` |
| **deploy** | Copies artifact to remote repo (shared) | `mvn deploy` |

---

### 📝 **CRITICAL RULE TO REMEMBER:**

> **When you run ANY phase, Maven executes ALL PREVIOUS phases first!**

**Example:**
- `mvn test` → runs `validate` → `compile` → `test`
- `mvn install` → runs `validate` → `compile` → `test` → `package` → `verify` → `install`
- `mvn package` → runs `validate` → `compile` → `test` → `package`

**Exam Trick:** `mvn test` does NOT run `package` or `install`! Only phases up to `test`.

---

## 3. THE CLEAN LIFECYCLE

**Purpose:** Deletes the `target/` directory

**Why use it?**
- Removes compiled classes
- Removes JAR/WAR files
- Ensures a fresh build
- Fixes issues with stale compiled files

**Commands:**
```bash
mvn clean              # Just cleans
mvn clean compile      # Cleans then compiles
mvn clean test         # Cleans then tests
mvn clean install      # Cleans then full build (most common!)
```

---

## 4. PLUGINS & GOALS

### Relationship:

```
Plugin → Contains Goals
Goal → Binds to Lifecycle Phase
Phase → Executes Goals
```

### Key Plugins to Know:

| Plugin | Purpose | Goals |
|--------|---------|-------|
| **maven-compiler-plugin** | Compiles Java code | `compiler:compile`, `compiler:testCompile` |
| **maven-surefire-plugin** | Runs unit tests | `surefire:test` |
| **maven-jar-plugin** | Creates JAR files | `jar:jar` |
| **maven-war-plugin** | Creates WAR files | `war:war` |
| **maven-deploy-plugin** | Deploys to remote repo | `deploy:deploy` |
| **maven-install-plugin** | Installs to local repo | `install:install` |

---

### 📝 **EXAM TIP: Running Goals Directly**

You can bypass the lifecycle and run a goal directly:
```bash
mvn compiler:compile      # Runs only compile goal
mvn surefire:test         # Runs only test goal
mvn jar:jar              # Runs only jar goal
```

**Difference:** When you run `mvn compile`, Maven runs ALL phases up to compile. When you run `mvn compiler:compile`, you ONLY run that goal.

---

## 5. DEPENDENCY MANAGEMENT

### POM Structure:

```xml
<project>
    <!-- Basic Info -->
    <groupId>com.mycompany</groupId>
    <artifactId>my-app</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>   <!-- jar, war, ear, pom -->
    
    <!-- Properties (like variables) -->
    <properties>
        <java.version>17</java.version>
        <spring.version>5.3.0</spring.version>
    </properties>
    
    <!-- Repositories -->
    <repositories>
        <repository>
            <id>maven-central</id>
            <url>https://repo.maven.apache.org/maven2</url>
        </repository>
    </repositories>
    
    <!-- Dependencies -->
    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
            <version>${spring.version}</version>
            <scope>compile</scope>
        </dependency>
    </dependencies>
</project>
```

---

### Dependency Scopes (⚠️ VERY IMPORTANT!)

| Scope | When Available | Example |
|-------|----------------|---------|
| **compile** | Everywhere (default) | Core libraries |
| **test** | Only during test phase | JUnit, TestNG |
| **provided** | Compile + test, but NOT packaged | Servlet API (provided by container) |
| **runtime** | Test + runtime, NOT compile | JDBC drivers |
| **system** | Like provided, but from system path | Rarely used (avoid!) |
| **import** | Only in `<dependencyManagement>` | For dependency management |

**Memorize This Rule:**
```
compile    → Available everywhere
test       → Only for tests
provided   → Available for compile/test, excluded from package
runtime    → Available during runtime, excluded from compile
```

---

### Transitive Dependencies

> **Definition:** When you add a dependency, Maven automatically downloads ALL dependencies that dependency needs.

**Example:**
```
You add: spring-core
Maven downloads: spring-core, plus spring-jcl, plus all its dependencies
```

---

## 6. MULTI-MODULE PROJECTS

### Structure:

```
parent-project/
├── pom.xml            ← Parent POM (packaging=pom)
├── core/
│   └── pom.xml        ← Child module
└── web/
    └── pom.xml        ← Child module
```

### Parent POM:

```xml
<project>
    <groupId>com.mycompany</groupId>
    <artifactId>parent</artifactId>
    <version>1.0</version>
    <packaging>pom</packaging>   <!-- MUST be pom -->
    
    <!-- Define child modules -->
    <modules>
        <module>core</module>
        <module>web</module>
    </modules>
    
    <!-- Manage dependency versions (not add them!) -->
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework</groupId>
                <artifactId>spring-core</artifactId>
                <version>5.3.0</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

### Child POM:

```xml
<project>
    <parent>
        <groupId>com.mycompany</groupId>
        <artifactId>parent</artifactId>
        <version>1.0</version>
    </parent>
    
    <artifactId>core</artifactId>   <!-- No groupId needed (inherited) -->
    <packaging>jar</packaging>
    
    <dependencies>
        <!-- Depend on other module -->
        <dependency>
            <groupId>com.mycompany</groupId>
            <artifactId>web</artifactId>
            <version>${project.version}</version>
        </dependency>
    </dependencies>
</project>
```

---

### 📝 **IMPORTANT RULE:**

> **Modules declared in parent POM with `<modules>` are built in the order they're listed.**

---

## 7. PROFILES

### What are Profiles?

> **Definition:** Profiles allow you to have different configurations for different environments (development, test, production, etc.)

### Profile Structure:

```xml
<project>
    <!-- ... -->
    <profiles>
        <profile>
            <id>development</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <env>dev</env>
                <database.url>localhost:3306</database.url>
            </properties>
        </profile>
        
        <profile>
            <id>production</id>
            <properties>
                <env>prod</env>
                <database.url>prod-db.company.com</database.url>
            </properties>
            <dependencies>
                <!-- Production-specific dependencies -->
            </dependencies>
        </profile>
    </profiles>
</project>
```

### Activating Profiles:

```bash
# With -P flag
mvn clean install -Pproduction

# With environment variable
mvn clean install -Denv=production

# With settings.xml (user-specific)
```

---

### 📝 **EXAM TIP: Profile Activation**

Profiles can be activated by:
1. **Command line:** `mvn clean install -Pprofile-name`
2. **System property:** `mvn clean install -Dproperty=value`
3. **File existence:** If a certain file exists
4. **OS detection:** Based on operating system
5. **Active by default:** `<activeByDefault>true</activeByDefault>`

---

## 8. GOAL BINDING (How Phases Execute Goals)

### Default Bindings by Packaging Type:

| Packaging | Phase | Goal |
|-----------|-------|------|
| **jar** | compile | compiler:compile |
| | test | surefire:test |
| | package | jar:jar |
| | install | install:install |
| | deploy | deploy:deploy |
| **war** | compile | compiler:compile |
| | test | surefire:test |
| | package | war:war |
| | install | install:install |
| | deploy | deploy:deploy |

---

### Custom Goal Binding:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-antrun-plugin</artifactId>
            <version>1.7</version>
            <executions>
                <execution>
                    <id>custom-task</id>
                    <phase>compile</phase>   <!-- When to run -->
                    <goals>
                        <goal>run</goal>      <!-- Which goal -->
                    </goals>
                    <configuration>
                        <tasks>
                            <echo>Running custom task!</echo>
                        </tasks>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

**This runs the `run` goal of the Ant plugin during the `compile` phase.**

---

## 9. COMMON MAVEN COMMANDS

| Command | What it does |
|---------|--------------|
| `mvn clean` | Deletes target/ directory |
| `mvn compile` | Compiles source code |
| `mvn test` | Compiles + runs unit tests |
| `mvn package` | Compiles + tests + creates JAR/WAR |
| `mvn install` | Builds + installs to local repository |
| `mvn deploy` | Builds + deploys to remote repository |
| `mvn validate` | Validates project structure |
| `mvn verify` | Runs integration tests |
| `mvn clean install` | Cleans + full build (most common) |
| `mvn site` | Generates project documentation |
| `mvn dependency:tree` | Shows dependency tree |
| `mvn help:effective-pom` | Shows the effective POM (merged) |
| `mvn test -Dtest=MyTest` | Runs a specific test class |
| `mvn clean install -DskipTests` | Builds without running tests |
| `mvn package -Pproduction` | Builds with production profile |

---

## 10. MAVEN VS GRADLE (Comparison Table)

| Aspect | Maven | Gradle |
|--------|-------|--------|
| **Configuration** | XML (declarative) | Groovy/Kotlin (programmable) |
| **Lifecycle** | Fixed phases | Flexible task graph |
| **Performance** | Slower (no incremental) | Faster (incremental + daemon) |
| **Learning Curve** | Easier | Steeper |
| **Multi-project** | Parent POM + `<modules>` | `settings.gradle` + `include` |
| **Profiles** | Built-in (`<profiles>`) | Not built-in (properties) |
| **Dependency Scope** | `compile`, `test`, `provided`, `runtime` | `implementation`, `testImplementation`, `compileOnly`, `runtimeOnly` |
| **Build File** | `pom.xml` | `build.gradle` or `build.gradle.kts` |

---

## 🎯 **QUICK MEMORY AIDS**

### "V C T P V I D" = Default Lifecycle Phases
**V**alidate → **C**ompile → **T**est → **P**ackage → **V**erify → **I**nstall → **D**eploy

### "C T P" = Most Common Phases
**C**ompile → **T**est → **P**ackage

### "C I D" = Build & Distribute
**C**lean → **I**nstall → **D**eploy

### Dependency Scopes (Memorize by "C T P R")
**C**ompile → **T**est → **P**rovided → **R**untime

---

## 📝 **SAMPLE EXAM QUESTIONS WITH ANSWERS**

### Q1: What is the difference between `mvn compile` and `mvn install`?

**Answer:** `mvn compile` runs phases up to `compile` (validate + compile). `mvn install` runs ALL phases up to `install` (validate → compile → test → package → verify → install). Install also copies the JAR/WAR to your local `.m2` repository.

---

### Q2: What happens when you run `mvn test`?

**Answer:** Maven runs:
1. validate
2. compile
3. test

It does NOT run `package`, `install`, or `deploy`.

---

### Q3: What is the purpose of `<dependencyManagement>`?

**Answer:** It declares dependency versions at the parent POM level so child modules can use them without specifying versions. This ensures all modules use the same version.

---

### Q4: What's the difference between `provided` and `compile` scope?

**Answer:**
- **`compile` scope:** Dependency is available in ALL phases and IS packaged in the final JAR/WAR
- **`provided` scope:** Dependency is available at compile/test, but IS NOT packaged (assumed provided by runtime container, e.g., servlet-api)

---

### Q5: How do you create a multi-module project in Maven?

**Answer:**
1. Create parent POM with `<packaging>pom</packaging>`
2. Define modules in parent using `<modules>`
3. Create child POMs with `<parent>` referencing parent
4. Build from parent directory using `mvn install`

---

### Q6: What is a Maven profile?

**Answer:** A way to customize builds for different environments (dev, test, prod). Profiles can:
- Activate different properties
- Add different dependencies
- Use different plugins
- Be activated via `-P` flag, system properties, or OS detection

---

### Q7: What's the difference between `mvn clean install` and `mvn install`?

**Answer:** `mvn install` builds the project. `mvn clean install` deletes the `target/` directory FIRST, then builds from scratch. Clean ensures a fresh build without stale compiled files.

---

## ✅ **BEFORE YOUR EXAM: QUICK CHECKLIST**

**Can you explain these concepts in your own words?**

- [ ] Maven build lifecycle (validate → compile → test → package → verify → install → deploy)
- [ ] How phases run previous phases
- [ ] Difference between clean, default, and site lifecycles
- [ ] Dependency scopes (compile, test, provided, runtime)
- [ ] How multi-module projects work
- [ ] What profiles are and how to activate them
- [ ] Plugin goals and how they bind to phases
- [ ] What transitive dependencies are
- [ ] The purpose of `dependencyManagement`
- [ ] The difference between `mvn clean compile` and `mvn compile`

---