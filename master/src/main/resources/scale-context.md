# 4. Architecture at Scale — Clean Architecture

We'll learn the three parts one by one:

1. **Dependency Rule**
2. **Independence from frameworks/tools**
3. **Decoupling modes: source code → deployment → service**

---

# 1. The Dependency Rule

The fundamental rule is:

> **Source-code dependencies must point toward higher-level policies.**

Or more simply:

> **Business rules should not depend on technical details.**

Consider:

```text
┌───────────────────────────────┐
│        Infrastructure         │
│                               │
│ Hibernate / Kafka / Oracle    │
│ REST / Redis / Spring         │
└───────────────┬───────────────┘
                ↓
┌───────────────────────────────┐
│         Application           │
│                               │
│ Use Cases / Coordination      │
└───────────────┬───────────────┘
                ↓
┌───────────────────────────────┐
│            Domain             │
│                               │
│ Business Rules / Invariants   │
└───────────────────────────────┘
```

The arrows represent **source-code dependencies**.

Infrastructure can depend on Application/Domain.

Application can depend on Domain.

But:

```text
Domain → Hibernate
Domain → Kafka
Domain → Spring
```

would violate the Dependency Rule.

---

# 2. What does "higher-level" mean?

This is important.

Imagine:

```java
class Loan {

    public void approve() {
        ...
    }
}
```

The business rule:

> "A loan can only be approved when it satisfies certain conditions."

is a **high-level policy**.

Now:

```java
JpaLoanRepository
KafkaTemplate
Oracle
Redis
```

are implementation details.

So conceptually:

```text
High-level
   ↑
Business Policy
   ↑
Application
   ↑
Infrastructure
Low-level
```

The lower-level details should depend on the policies—not the other way around.

---

# 3. A concrete Java example

### Bad architecture

```java
class LoanService {

    private final LoanRepository repository;

    public LoanService(LoanRepository repository) {
        this.repository = repository;
    }
}
```

Suppose `LoanRepository` is:

```java
public interface LoanRepository
        extends JpaRepository<Loan, Long> {
}
```

Now your application/domain logic depends directly on Spring Data.

You have:

```text
LoanService
    ↓
JpaRepository
    ↓
Spring Data
```

The business/application layer knows about infrastructure.

That's backwards.

---

# 4. Better architecture

Define your own domain/application abstraction:

```java
public interface LoanRepository {

    Loan findById(LoanId id);

    void save(Loan loan);
}
```

Then infrastructure implements it:

```java
@Repository
class JpaLoanRepository implements LoanRepository {

    @Override
    public Loan findById(LoanId id) {
        ...
    }

    @Override
    public void save(Loan loan) {
        ...
    }
}
```

Now:

```text
                ┌────────────────────┐
                │     Application    │
                │                    │
                │ LoanRepository     │
                └─────────▲──────────┘
                          │
                          │ implements
                          │
                ┌─────────┴──────────┐
                │  Infrastructure    │
                │                    │
                │ JpaLoanRepository  │
                └────────────────────┘
```

Notice something interesting:

**The implementation depends on the interface.**

This is the Dependency Inversion Principle at architectural scale.

---

# 5. The Dependency Rule vs Dependency Inversion

They're related but don't confuse them.

### Dependency Inversion Principle

Says:

> High-level modules should not depend directly on low-level modules. Both should depend on abstractions.

### Clean Architecture Dependency Rule

Takes that idea to the architectural level:

> **Source-code dependencies should point inward toward higher-level policies.**

So:

```text
DIP
  ↓
abstractions
  ↓
architectural boundaries
  ↓
Dependency Rule
```

---

# 6. Why interfaces matter

Suppose you have:

```java
interface LoanRepository {
    Loan findById(LoanId id);
}
```

Your Application Layer only knows:

```text
LoanRepository
```

It doesn't care whether the implementation is:

```text
JPA
Oracle
MongoDB
PostgreSQL
REST API
In-memory
```

So you can change:

```text
Oracle → PostgreSQL
```

without changing your use case.

That's the architectural benefit.

---

# 7. Independence from Frameworks

This leads directly to your second point.

Clean Architecture says your architecture should be independent of frameworks.

For example, ideally your core domain shouldn't require:

```java
@Entity
@Table
@Service
@Autowired
@Component
@Transactional
```

to express its business rules.

Instead:

```java
public final class Loan {

    private LoanStatus status;

    public void approve() {
        if (status != PENDING) {
            throw new InvalidLoanState();
        }

        status = APPROVED;
    }
}
```

This is just Java.

No Spring.

No Hibernate.

No Kafka.

No REST.

No database.

That's what **framework independence** means.

---

# 8. Why is framework independence valuable?

Imagine your business logic depends heavily on Spring:

```text
Domain
  ↓
Spring
  ↓
Hibernate
  ↓
Oracle
```

Now changing technology becomes expensive.

For example:

```text
Spring → another framework
Hibernate → jOOQ
Oracle → PostgreSQL
REST → GraphQL
```

could require changes throughout your domain.

With good architecture:

```text
                  Domain
                    ↑
              Application
                    ↑
        ┌───────────┼───────────┐
        ↓           ↓           ↓
     Spring      Hibernate     Kafka
```

The technical details surround the core rather than controlling it.

---

# 9. "Database Independence"

This doesn't mean:

> "The application should never care about database technology."

Of course infrastructure does.

It means:

> **Business rules shouldn't depend on database implementation details.**

For example, this is bad domain code:

```java
class Loan {

    @Query("""
        SELECT ...
        FROM loan
        WHERE ...
    """)
    ...
}
```

Now your domain knows SQL/database details.

Better:

```java
interface LoanRepository {
    Loan findById(LoanId id);
}
```

And infrastructure handles:

```text
SQL
Hibernate
JPA
Oracle
indexes
queries
```

---

# 10. "UI Independence"

Same principle.

Your domain shouldn't know:

```text
REST
HTTP
JSON
React
Angular
GraphQL
```

For example, this is bad:

```java
class Loan {

    public ResponseEntity<?> approve() {
        ...
    }
}
```

Now your domain knows HTTP.

Instead:

```java
class Loan {

    public void approve() {
        ...
    }
}
```

The controller/application layer converts that into an HTTP response.

---

# 11. "Frameworks are details"

This is a powerful Clean Architecture idea.

Think:

```text
                 ┌─────────────────┐
                 │   Frameworks    │
                 │   UI            │
                 │   DB            │
                 │   Messaging     │
                 └───────┬─────────┘
                         │
                         ↓
                 ┌───────────────┐
                 │  Application  │
                 └───────┬───────┘
                         ↓
                 ┌───────────────┐
                 │    Domain     │
                 └───────────────┘
```

The outer technology can change.

The business rules should survive.

---

# 12. Now the interesting part: Decoupling Modes

This is where architecture gets more advanced.

There are different **levels of separation**.

You can decouple things at:

1. **Source-code level**
2. **Deployment level**
3. **Service level**

They are not the same.

---

# 13. Level 1 — Source-Code Decoupling

Suppose:

```text
application
├── loan
├── risk
└── accounting
```

They are in the same application.

But the modules have clean boundaries:

```text
loan
  ↓
interfaces

risk
  ↓
interfaces

accounting
  ↓
interfaces
```

You can change one module without heavily affecting another.

This is **source-code/component decoupling**.

For example:

```text
Single JVM
Single application
Single deployment
```

but:

```text
Loan module
Risk module
Accounting module
```

are independently structured.

This is basically what a **modular monolith** tries to achieve.

---

# 14. Level 2 — Deployment Decoupling

Now suppose:

```text
loan.jar
risk.jar
accounting.jar
```

can be deployed independently.

You might have:

```text
Loan Application
Risk Application
Accounting Application
```

but they aren't necessarily separate microservices in the full distributed-systems sense.

The key property is:

> **Independent deployment.**

Team A can deploy Loan without deploying Risk.

That's a much stronger form of decoupling than source-code separation.

---

# 15. Level 3 — Service Decoupling

Now you have actual independently running services:

```text
┌───────────────┐
│ Loan Service  │
└───────┬───────┘
        │
      HTTP/Kafka
        │
        ↓
┌───────────────┐
│ Risk Service  │
└───────────────┘
```

Each service has:

* separate process
* separate deployment
* network boundary
* potentially separate database
* independent scaling
* independent failure boundary

This is what people usually mean by **microservices**.

---

# 16. These three are NOT equivalent

This is a very important exam point.

You can have:

```text
Source-code separation
        ↓
without
        ↓
Independent deployment
```

For example:

```text
Modular Monolith
```

You can also have:

```text
Independent deployment
        ↓
without
        ↓
full microservice architecture
```

depending on how the system is structured.

And microservices give you a much stronger isolation boundary.

---

# 17. Why not always use microservices?

Because every stronger boundary introduces costs.

Compare:

```text
Modular Monolith
```

vs:

```text
Microservices
```

Microservices introduce:

* network calls
* serialization
* distributed transactions
* eventual consistency
* service discovery
* observability
* retries
* timeouts
* failure handling
* deployment complexity

So don't think:

> "More decoupling = always better."

Instead:

> **Use the weakest boundary that provides the independence you actually need.**

This is a very senior architectural mindset.

---

# 18. Example

Suppose you have:

```text
Loan
Risk
Collateral
```

### Option A — Poor monolith

```text
Everything
    ↓
one giant module
```

No clear boundaries.

### Option B — Modular monolith

```text
Application
│
├── Loan Module
├── Risk Module
└── Collateral Module
```

Same JVM.

Same deployment.

But strong source-code boundaries.

### Option C — Separate applications

```text
Loan App
Risk App
Collateral App
```

Independent deployment.

### Option D — Microservices

```text
Loan Service
      ↕
Risk Service
      ↕
Collateral Service
```

Network communication and independent operational boundaries.

---

# 19. Independent Deployability

Now we can define it.

Suppose Team A changes:

```text
Loan Service
```

Can they deploy it without deploying:

```text
Risk
Collateral
Accounting
```

?

If yes:

> **They have independent deployability.**

But source-code separation alone doesn't guarantee this.

For example:

```text
loan-module
risk-module
```

inside:

```text
banking-app.jar
```

still means:

```text
One deployment
```

So:

```text
Source-code independence
≠
Deployment independence
```

This distinction is extremely important.

---

# 20. Connect everything we've learned

Now look at the four topics together.

### SRP

Separates responsibilities according to **actors/reasons to change**.

### Aggregates

Separates **business consistency boundaries**.

### Application Layer

Separates **use-case orchestration from domain logic**.

### Dependency Rule

Separates **business policies from technical details**.

And then:

### Decoupling Modes

Determine **how independently those boundaries can evolve and deploy**.

So:

```text
SRP
 ↓
Responsibility boundaries

DDD Aggregates
 ↓
Consistency boundaries

Application Layer
 ↓
Use-case boundaries

Dependency Rule
 ↓
Dependency boundaries

Deployment / Services
 ↓
Operational boundaries
```

This is the bigger architectural picture.

---

# 21. One final exam scenario

Imagine your Java system looks like this:

```java
@Service
class LoanService {

    @Autowired
    private JpaLoanRepository repository;

    @Autowired
    private KafkaTemplate<String, LoanApproved> kafka;

    public ResponseEntity<?> approve(Long id) {

        LoanEntity loan = repository.findById(id);

        if (loan.getStatus() != PENDING) {
            return ResponseEntity.badRequest().build();
        }

        loan.setStatus(APPROVED);

        repository.save(loan);

        kafka.send("loan-approved", ...);

        return ResponseEntity.ok(loan);
    }
}
```

There are **multiple architectural problems** here.

We can identify them using everything we've studied:

### SRP

The class may be mixing:

```text
HTTP
business logic
persistence
messaging
application orchestration
```

### Application Layer

Business rules are sitting inside the service:

```java
if (loan.getStatus() != PENDING)
```

instead of:

```java
loan.approve();
```

### Dependency Rule

Application logic directly depends on:

```text
JpaLoanRepository
KafkaTemplate
Spring
ResponseEntity
```

### Framework Independence

The use case is coupled to:

```text
Spring MVC
Spring Data
Kafka
```

### Domain Model

`LoanEntity` looks like a persistence model rather than a rich domain model.

A better architecture might look like:

```text
             Controller
                 │
                 ↓
       ApproveLoanApplicationService
                 │
          ┌──────┴──────┐
          ↓             ↓
        Loan       LoanRepository
     (Domain)       (Interface)
                        ↑
                        │
                 JpaLoanRepository
```

and:

```text
ApproveLoanApplicationService
        │
        ├── load Loan
        ├── loan.approve()
        ├── save Loan
        └── publish event
```

Now you've applied **all four topics together**.

---

# The 3 things to memorize for this section

### Dependency Rule

> **Dependencies point toward high-level business policies, not toward technical details.**

### Independence

> **Domain/business rules should not depend on Spring, Hibernate, databases, UI, Kafka, or other implementation details.**

### Decoupling Modes

> **Source-code decoupling, deployment decoupling, and service decoupling are progressively stronger boundaries; independent deployability requires more than merely separating code into modules.**

---

## Your final exam test

Consider this architecture:

```text
loan-module
    ↓
Spring Data JPA
    ↓
Oracle
```

and:

```java
class Loan {

    @Entity
    @Table(name = "LOAN")
    public void approve() {
        ...
    }
}
```
