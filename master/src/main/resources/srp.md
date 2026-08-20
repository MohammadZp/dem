# 1. Single Responsibility Principle — SRP

Let's learn it at the **architecture/senior Java level**, not the beginner definition of “a class should have one responsibility.”

---

## 1. The classic definition

You probably know:

> **A class should have only one reason to change.**

That's correct, but incomplete.

Robert C. Martin's more precise architectural interpretation is:

> **A module should be responsible to one, and only one, actor.**

The word **actor** is the important part.

---

## 2. What is an Actor?

An **Actor** is a group of people/stakeholders who have the same interest in how some part of the system behaves.
Actor = source of requirements/change

It does **not** necessarily mean one human user.

For example, imagine a banking system:

```text
                    LoanApplication
                          │
        ┌─────────────────┼─────────────────┐
        ↓                 ↓                 ↓
   Loan Officer       Risk Team        Accounting
```

These are different actors.

They have different reasons for requesting changes.

For example:

**Loan Officer**

> "We need to change how loan applications are created."

**Risk Team**

> "We need to change the risk calculation."

**Accounting**

> "We need to change how financial reporting works."

Even though all three concerns involve a `Loan`, they represent **different actors**.

---

# 3. The real meaning of SRP

Consider this:

```java
class LoanService {

    public void createLoan(...) {
        // loan creation
    }

    public RiskResult calculateRisk(...) {
        // risk calculation
    }

    public Report generateAccountingReport(...) {
        // accounting report
    }
}
```

At first glance, someone might say:

> "This class has too many responsibilities."

But that's not the deepest problem.

The architectural problem is:

```text
LoanService
    ↑
    │
 ┌──┴───────────────┐
 │                  │
Loan Officer      Risk Team
 │                  │
changes            changes
 │                  │
 └──────→ SAME CLASS ←──────┘
```

Now imagine:

```text
Risk Team:
"Change risk calculation."

Accounting:
"Change report format."
```

Both changes modify the same module.

That creates **coupling between actors**.

And that's what SRP is trying to prevent.

---

# 4. Accidental Duplication

This is one of the most important concepts from the book.

Suppose:

```java
class Employee {

    public double calculatePay() {
        ...
    }

    public void save() {
        ...
    }

    public String reportHours() {
        ...
    }
}
```

Imagine:

* `calculatePay()` → Payroll department
* `save()` → Database administrators
* `reportHours()` → HR

Now Payroll changes something:

```java
calculatePay()
```

But because everything is inside the same class, the change happens in a module that HR and database-related code also depend on.

That's dangerous.

The problem isn't necessarily **duplicate code**.

It's **different actors being coupled to the same source-code unit**.

---

# 5. Why is it called "Accidental Duplication"?

Imagine two methods need similar logic:

```java
calculatePay()
reportHours()
```

Someone says:

> "These two calculations look similar. Let's extract them."

So they create:

```java
class Employee {

    private double calculateHours() {
        ...
    }
}
```

Now:

```text
Payroll
   ↓
calculateHours()

HR
   ↓
calculateHours()
```

It looks like good reuse.

But later:

> Payroll changes the definition of "working hours."

So `calculateHours()` changes.

Unfortunately, HR's definition of working hours was different.

Now the change for **one actor** affects another actor.

That's **accidental duplication**.

The code was shared because it looked similar, but the underlying **business reasons were different**.

---

# 6. The important rule

This gives us a very important principle:

> **Don't group code together merely because it looks similar. Group it together when it changes for the same reason.**

This is much more important than:

> "Don't duplicate code."

Sometimes **duplication is safer than incorrect coupling**.

For example:

```java
class PayrollCalculator {
    private int calculateWorkingHours(...) {
        ...
    }
}
```

and:

```java
class HrReportGenerator {
    private int calculateWorkingHours(...) {
        ...
    }
}
```

Yes, there is duplicated code.

But if Payroll and HR have **different business rules**, this duplication may actually be the better architecture.

---

# 7. SRP is about change boundaries

Think about SRP like this:

```text
Actor A ───────→ Module A
Actor B ───────→ Module B
Actor C ───────→ Module C
```

Instead of:

```text
Actor A ──┐
Actor B ──┼──→ Giant Module
Actor C ──┘
```

The goal is to make:

> **One reason for change → one responsible module**

This is why SRP becomes an **architectural principle**, not merely a class-design principle.

---

# 8. SRP at different levels

This is especially important for your Java exam.

SRP can be applied at multiple levels:

### Class

```text
Order
OrderValidator
OrderRepository
```

instead of:

```text
OrderEverything
```

### Package

Instead of:

```text
loan/
    LoanService
    RiskService
    AccountingService
```

you might separate according to domain responsibilities.

### Module

For example:

```text
loan-origination
risk
accounting
```

### Microservice

Potentially:

```text
Loan Service
Risk Service
Accounting Service
```

But **SRP does NOT mean every responsibility must become a microservice.**

That's a common senior-level trap.

---

# 9. SRP ≠ "one thing"

A common interview answer is:

> "A class should do one thing."

That's too vague.

Consider:

```java
class LoanApplicationService {

    createLoan()
    approveLoan()
    rejectLoan()
}
```

Is this violating SRP?

**Not necessarily.**

All three operations may belong to the same actor and the same business responsibility:

```text
Actor:
Loan Officer

        ↓

Loan Application Management
```

So multiple methods can absolutely belong to one responsibility.

The question is:

> **Do these operations change for the same reason?**

That's the real test.

---

# 10. A practical test

When you're looking at a class in an exam, ask:

### Question 1

**Who requests changes to this code?**

Identify the actors.

### Question 2

**Can I identify two groups with different reasons for changing it?**

If yes, potential SRP violation.

### Question 3

**If Actor A changes a requirement, could Actor B's code be affected?**

If yes, strong indication of a violation.

### Question 4

**Are we sharing code because it is genuinely one responsibility, or merely because the implementation looks similar?**

This catches accidental duplication.

---

# 11. Java example

Bad:

```java
class LoanService {

    public Loan createLoan(LoanRequest request) {
        // business logic
    }

    public RiskResult calculateRisk(Loan loan) {
        // risk logic
    }

    public byte[] generateReport(Loan loan) {
        // accounting/reporting logic
    }

    public void save(Loan loan) {
        // persistence
    }
}
```

Potentially four different actors:

```text
Loan Officer
Risk Team
Accounting
Infrastructure/DB
```

Better:

```java
class LoanApplicationService {
    createLoan(...)
}
```

```java
class RiskAssessmentService {
    calculateRisk(...)
}
```

```java
class LoanReportService {
    generateReport(...)
}
```

```java
interface LoanRepository {
    save(...)
}
```

Now each area has a more independent reason to change.

---

# 12. SRP and team development

There's another architectural benefit.

Imagine:

```text
Team A → Loan
Team B → Risk
Team C → Accounting
```

If all three teams constantly modify:

```text
LoanService.java
```

you get:

```text
                LoanService.java
                 ↙     ↓     ↘
             Team A  Team B  Team C
```

Result:

* merge conflicts
* accidental changes
* difficult code ownership
* more coordination
* harder deployments

Good separation gives:

```text
Loan module       → Team A
Risk module       → Team B
Accounting module → Team C
```

This is one reason SRP is connected to **team organization and architecture**.

---

# 13. SRP and microservices

Be careful here.

Suppose you have:

```text
Loan Service
```

and it contains:

```text
Loan Creation
Risk Calculation
Accounting
```

You shouldn't immediately conclude:

> "We need three microservices."

First ask:

> **Do these responsibilities need independent deployment/scaling/ownership?**

Maybe the correct solution is simply:

```text
loan-service
├── loan
├── risk
└── accounting
```

as separate modules/packages.

That's still SRP.

Then, if deployment independence becomes necessary:

```text
loan-service
risk-service
accounting-service
```

So:

> **SRP can drive separation, but it does not dictate the deployment boundary.**

This connects directly to the **decoupling modes** you'll study later.

---

# 14. The exam-level mental model

Remember this:

```text
SRP
 │
 ├── Identify Actors
 │
 ├── Identify Reasons to Change
 │
 ├── Group code that changes together
 │
 ├── Separate code that changes for different actors
 │
 └── Prevent accidental coupling
```

The deepest idea is:

> **SRP is about organizing code around independent sources of change.**

Not:

> "Every class should have only one method."

Not:

> "Every class should represent one noun."

Not:

> "Never duplicate code."

---

## Exam scenario

Imagine this:

```java
class EmployeeService {

    calculateSalary()
    generateTaxReport()
    generatePerformanceReport()
    saveEmployee()
}
```

The requirements say:

* Payroll team changes salary calculation.
* Tax team changes tax reports.
* HR changes performance reports.
* Infrastructure team changes persistence.

**Question: Is this an SRP violation? Why?**

The correct senior-level answer is:

> Yes. The module has multiple independent actors with different reasons to change. Changes requested by Payroll, Tax, HR, and Infrastructure can all modify the same module. This creates coupling between unrelated sources of change and can cause accidental duplication and merge conflicts.

And the important follow-up:

> **The solution is not simply "create one class per method." The classes/modules should be separated according to the actors and business reasons for change.**

---

### One sentence to memorize for the exam

> **SRP means a module should be responsible to one actor, so that changes required by one actor do not accidentally affect code belonging to another actor.**