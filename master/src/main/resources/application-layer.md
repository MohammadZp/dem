> **The Application Layer defines and coordinates use cases; the Domain Layer decides what is valid according to business rules.**

---

# 3. Application Layer & Application Services

Let's build this from the ground up.

## 1. Why do we need an Application Layer?

Imagine a loan system.

A user wants to:

> "Approve a loan."

There are several things involved:

```text
Approve Loan
   │
   ├── Load Loan
   ├── Check permissions
   ├── Start transaction
   ├── Tell Loan to approve itself
   ├── Save Loan
   ├── Publish event
   └── Return result
```

Who should coordinate all of this?

**Application Service.**

For example:

```java
class ApproveLoanService {

    public void approve(ApproveLoanCommand command) {

        Loan loan = loanRepository.findById(command.loanId());

        loan.approve();

        loanRepository.save(loan);

        eventPublisher.publish(
            new LoanApproved(loan.id())
        );
    }
}
```

The Application Service is basically saying:

> "To execute this use case, these steps need to happen in this order."

That's **orchestration**.

---

# 2. Application Service ≠ Business Logic

This distinction is extremely important for your exam.

Consider:

```java
public void approve(Loan loan) {

    if (loan.getStatus() != PENDING) {
        throw new IllegalStateException();
    }

    if (loan.getAmount() > 100_000) {
        throw new IllegalStateException();
    }

    loan.setStatus(APPROVED);
}
```

Where should this logic live?

**Not in the Application Service.**

The business rules belong in the Domain Model:

```java
class Loan {

    public void approve() {

        if (status != PENDING) {
            throw new InvalidLoanState();
        }

        if (amount.isGreaterThan(MAX_APPROVABLE_AMOUNT)) {
            throw new LoanLimitExceeded();
        }

        status = APPROVED;
    }
}
```

Then Application Service:

```java
public void approve(ApproveLoanCommand command) {

    Loan loan = repository.findById(command.loanId());

    loan.approve();

    repository.save(loan);
}
```

Notice the difference.

### Application Layer

```text
What steps need to happen?
```

### Domain Layer

```text
What is allowed according to business rules?
```

---

# 3. Think "Use Case"

Application Services usually correspond to **use cases**.

For example:

```text
LoanApplication
├── CreateLoan
├── ApproveLoan
├── RejectLoan
├── CancelLoan
└── ReleaseLoan
```

You might have:

```java
class ApproveLoanApplicationService
class RejectLoanApplicationService
class CreateLoanApplicationService
```

or one service:

```java
class LoanApplicationService {

    approve()
    reject()
    create()
}
```

The exact class organization isn't the important part.

The important thing is:

> **Application Services expose/use-case-oriented operations.**

---

# 4. What does an Application Service actually do?

Typically:

### 1. Receive input

```java
ApproveLoanCommand command
```

### 2. Load required domain objects

```java
Loan loan = loanRepository.findById(...);
```

### 3. Coordinate the operation

```java
loan.approve();
```

### 4. Manage transaction

```text
BEGIN TRANSACTION

load
domain operation
save

COMMIT
```

### 5. Handle authorization/security concerns

For example:

```java
authorizationService.checkCanApproveLoan(user, loan);
```

### 6. Persist changes

```java
loanRepository.save(loan);
```

### 7. Publish events / interact with external systems

```java
eventPublisher.publish(new LoanApproved(...));
```

### 8. Return a result/DTO

So conceptually:

```text
                Application Service

Input
  ↓
Load
  ↓
Authorize
  ↓
Coordinate
  ↓
Domain operation
  ↓
Persist
  ↓
Publish events
  ↓
Output
```

---

# 5. Thin Application Services

You will often hear:

> **Keep Application Services thin.**

This doesn't mean:

> "Application Services should have almost no code."

It means:

> **Don't put business decisions into them.**

Compare these.

### Bad

```java
class ApproveLoanService {

    public void approve(Long loanId) {

        Loan loan = repository.findById(loanId);

        if (loan.getStatus() != PENDING) {
            throw new IllegalStateException();
        }

        if (loan.getAmount() > 100_000) {
            throw new IllegalStateException();
        }

        if (loan.getCustomer().getCreditScore() < 700) {
            throw new IllegalStateException();
        }

        loan.setStatus(APPROVED);

        repository.save(loan);
    }
}
```

The Application Service has become a **business-rule engine**.

That's bad.

### Better

```java
class ApproveLoanService {

    public void approve(Long loanId) {

        Loan loan = repository.findById(loanId);

        loan.approve();

        repository.save(loan);
    }
}
```

Now:

```text
Application
    ↓
"Please approve this loan."
    ↓
Domain
    ↓
"Is this actually allowed?"
```

---

# 6. Why does fat Application Service cause problems?

Imagine:

```java
class LoanApplicationService {

    approveLoan() {
        // 200 lines
    }

    rejectLoan() {
        // 150 lines
    }

    calculateRisk() {
        // 300 lines
    }

    calculateInterest() {
        // 100 lines
    }
}
```

Eventually the Application Layer becomes:

```text
Controller
   ↓
Application Service
   ↓
ALL BUSINESS LOGIC
```

while the Domain becomes:

```text
Entity
   ↓
getters/setters
```

That's the classic:

# Anemic Domain Model

---

# 7. What is Anemic Domain Model?

An anemic model looks like:

```java
class Loan {

    private Money amount;
    private LoanStatus status;

    public Money getAmount() {
        return amount;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }
}
```

It contains:

* data
* getters
* setters

but almost no meaningful behavior.

Then:

```java
class LoanService {

    public void approve(Loan loan) {

        if (...) {
            ...
        }

        loan.setStatus(APPROVED);
    }
}
```

All the business intelligence has escaped into services.

That's what you generally want to avoid in a rich domain model.

---

# 8. Rich Domain Model

Instead:

```java
class Loan {

    public void approve() {

        ensureCanBeApproved();

        status = APPROVED;
    }

    private void ensureCanBeApproved() {
        ...
    }
}
```

Now:

```text
Loan
 ├── state
 ├── behavior
 └── business invariants
```

The Aggregate becomes responsible for protecting itself.

This connects directly to what we just learned:

> **Aggregate Root protects its invariants.**

And the Application Service coordinates the Aggregate.

---

# 9. The perfect separation

This is one of the most important diagrams for your exam:

```text
                 Application Layer
                         │
                         │ coordinates
                         ↓
                 ┌───────────────┐
                 │ Domain Model  │
                 │               │
                 │ Loan.approve()│
                 │ Loan.cancel() │
                 └───────────────┘
                         │
                         │ business rules
                         ↓
                  Domain Invariants
```

Application Service asks:

> **"What needs to happen?"**

Domain asks:

> **"What is valid?"**

---

# 10. Application Service and Transactions

Application Services are often a natural place for transaction boundaries.

For example in Spring:

```java
@Transactional
public void approveLoan(Long loanId) {

    Loan loan = loanRepository.findById(loanId);

    loan.approve();

    loanRepository.save(loan);
}
```

The Application Service defines:

> "This entire use case should execute as one transaction."

That's very different from saying:

> "The Application Service owns the business rules."

It doesn't.

It owns the **orchestration/transaction boundary**.

---

# 11. Application Service and Security

Another responsibility can be authorization.

For example:

```java
public void approveLoan(
        UserId userId,
        LoanId loanId) {

    Loan loan = repository.findById(loanId);

    authorization.checkCanApprove(userId, loan);

    loan.approve();

    repository.save(loan);
}
```

The Application Layer coordinates:

```text
Authentication / Authorization
        ↓
Application Service
        ↓
Domain
```

But don't confuse **authorization** with business rules.

For example:

> "Only loan officers can approve loans."

could be an authorization policy.

Whereas:

> "A loan cannot be approved if its status is CLOSED."

is a domain invariant.

---

# 12. DTOs

Now we get to your third bullet.

Suppose your domain object is:

```java
class Loan {

    private LoanId id;
    private Money amount;
    private LoanStatus status;
    private CustomerId customerId;

    // domain behavior
}
```

Should your REST API return this object directly?

Usually, **no**.

Instead:

```java
record LoanResponse(
    String id,
    BigDecimal amount,
    String status
) {}
```

Then:

```java
LoanResponse approve(...) {
    Loan loan = ...;

    loan.approve();

    repository.save(loan);

    return new LoanResponse(
        loan.id().value(),
        loan.amount().value(),
        loan.status().name()
    );
}
```

---

# 13. Why use DTOs?

Because the domain model and external API have **different responsibilities**.

Without DTO:

```text
Database
   ↓
Entity/Domain
   ↓
REST API
```

Now your API becomes coupled to your internal domain structure.

A domain refactoring could accidentally break your API.

With DTO:

```text
Domain
   ↓
Mapping
   ↓
DTO
   ↓
REST API
```

Now:

```text
Internal model
     ≠
External contract
```

That's valuable.

---

# 14. DTO ≠ Anemic Model

Another common confusion.

This:

```java
record LoanResponse(...) {}
```

being data-only is completely fine.

It's a **DTO**, not a domain entity.

The problem is when your **domain model** itself is just:

```java
getters
setters
data
```

So:

```text
DTO
→ intentionally data-oriented

Domain Entity
→ should encapsulate domain behavior
```

Very important distinction.

---

# 15. Application Service as an API to the Domain

Suppose your UI has:

```text
Loan Screen
Customer Screen
Payment Screen
```

The UI shouldn't need to understand all internal domain objects.

Instead:

```text
UI
 ↓
Application API
 ↓
Domain Models
```

For example:

```http
GET /loans/123
```

might return:

```json
{
  "id": "123",
  "status": "APPROVED",
  "amount": 500000,
  "customerName": "Mohammad",
  "remainingAmount": 300000
}
```

That response might combine information from:

```text
Loan Aggregate
Customer Aggregate
Payment model
```

The UI doesn't care.

The Application Layer can orchestrate the required queries and construct a **view DTO**.

---

# 16. This is the "integration" responsibility

Imagine:

```text
Loan
Customer
Payment
```

The UI wants:

```text
Loan Dashboard
```

which contains:

```text
Customer name
Loan amount
Loan status
Paid amount
Remaining amount
```

The Application Layer can coordinate:

```text
          Application Service
                 │
        ┌────────┼────────┐
        ↓        ↓        ↓
      Loan    Customer  Payment
        │        │        │
        └────────┼────────┘
                 ↓
          LoanDashboardDTO
                 ↓
                UI
```

This doesn't mean we're merging these into one Aggregate.

We're just **composing data for a use case/view**.

That's an important distinction.

---

# 17. Application Service vs Domain Service

This is highly exam-worthy.

### Application Service

```java
class ApproveLoanApplicationService {

    approve(...) {
        load loan
        authorize
        loan.approve()
        save loan
        publish event
    }
}
```

Its job:

> **Coordinate the use case.**

### Domain Service

Suppose risk calculation doesn't naturally belong to one Entity:

```java
class LoanRiskCalculator {

    RiskScore calculate(
        Loan loan,
        Customer customer,
        MarketData marketData
    ) {
        // domain logic
    }
}
```

That's a **Domain Service** because the operation represents business/domain logic.

So:

```text
Application Service
    → orchestration

Domain Service
    → business logic
```

---

# 18. A very common exam trap

Suppose:

```java
class LoanApplicationService {

    public void approve(Loan loan) {

        if (loan.amount() > 100_000) {
            ...
        }

        if (loan.customerCreditScore() < 700) {
            ...
        }
    }
}
```

Someone might say:

> "It's okay because it's in the Application Layer."

❌ Wrong.

The location doesn't make something application logic.

The **nature of the logic** matters.

If it's a business rule:

```text
"Loan > 100k requires additional approval"
```

it belongs to the domain model/policy.

---

# 19. The complete picture

You should now see how Topics 2 and 3 connect:

```text
                 Application Layer
                         │
                         │ coordinates
                         ↓
              ┌────────────────────┐
              │  Aggregate Root    │
              │                    │
              │  approve()         │
              │  cancel()          │
              │  addLine()         │
              └────────────────────┘
                         │
                         ↓
                  Domain Rules
                  & Invariants
```

Application Service:

```text
Load → Coordinate → Tell → Save → Publish
```

Aggregate:

```text
Receive command → Validate invariant → Change state
```

---

# 20. Exam mental model

When you see an Application Service, ask:

### Does it coordinate?

```text
Repository
Domain
Transaction
Events
Security
External systems
```

Good.

### Does it make business decisions?

```text
if loan.amount > X
if customer.score < Y
if order.status == Z
```

🚨 Investigate.

Those rules probably belong in the Domain.

---

# The four things you need to remember

### 1. Application Service

> **Coordinates a use case.**

### 2. Thin Application Service

> **Orchestration, not business-rule implementation.**

### 3. DTO

> **Separates external contracts from internal domain models.**

### 4. Domain Service vs Application Service

> **Domain Service contains domain logic; Application Service coordinates the workflow.**

And the overall flow:

```text
Controller
    ↓
Application Service
    ↓
Domain Model / Aggregate
    ↓
Repository Interface
    ↓
Infrastructure
```

The Application Layer sits **between the outside world and the domain**, but its job is to **orchestrate**, not to become the domain itself.
