# 1. CQS → CQRS

Before CQRS, understand **CQS — Command Query Separation**.

The principle is:

> A method should either **change state** or **return information**, but not both.

So we have two types of operations.

### Command

Changes state:

```java
public void withdraw(Money amount) {
    balance = balance.subtract(amount);
}
```

The important part:

```text
Command
  ↓
changes state
```

It shouldn't primarily be used to return information.

### Query

Reads state:

```java
public Money getBalance() {
    return balance;
}
```

The important part:

```text
Query
  ↓
returns information
  ↓
doesn't change state
```

So:

```text
CQS

Command → modifies state
Query   → reads state
```

---

# 2. CQRS Takes CQS to the Architecture Level

CQS is about **methods**.

CQRS applies the same separation to **models/architectures**.

Instead of:

```text
                Application
                    │
             ┌──────┴──────┐
             │ Domain Model │
             └──────┬──────┘
                    │
               Database
```

we have:

```text
                 Application
                  /       \
                 /         \
          Commands         Queries
              ↓               ↓
       Command Model     Query Model
              ↓               ↓
         Write Store       Read Store
```

The key idea:

> **The model used to perform business operations doesn't have to be the same model used to read data.**

This is the heart of CQRS.

---

# 3. Why Would We Need Two Models?

This is probably the **most important conceptual question**.

Imagine a banking system.

Your domain model might look like:

```text
Customer
   │
   ├── Account
   │
   └── Loan
```

And you have aggregates such as:

```text
CustomerAggregate
AccountAggregate
LoanAggregate
TransactionAggregate
```

These models are designed around **business rules and transactional consistency**.

But now management asks for this dashboard:

```text
Customer Dashboard

Customer Name
Total Balance
Total Loans
Outstanding Loan
Last 10 Transactions
Credit Score
Branch
Risk Level
```

The data comes from:

```text
Customer
Account
Loan
Transaction
CreditScore
Branch
Risk
```

A normal domain repository might require:

```sql
SELECT ...
FROM customer
JOIN account ...
JOIN loan ...
JOIN transaction ...
JOIN credit_score ...
JOIN branch ...
JOIN risk ...
```

That can become complicated and expensive.

This is the **View Sophistication Problem**.

The model optimized for:

> **business behavior**

is not necessarily optimized for:

> **reading a particular UI view.**

---

# 4. The CQRS Solution

We create a separate Read Model.

```text
                    ┌─────────────────┐
                    │  Command Model  │
                    │                 │
Command ───────────→│ Aggregates      │
                    │ Business Rules  │
                    └────────┬────────┘
                             │
                         Events
                             │
                             ▼
                    ┌─────────────────┐
                    │   Read Model    │
                    │                 │
Query ─────────────→│ Dashboard View  │
                    │ Reporting View  │
                    └─────────────────┘
```

The Command Model is optimized for:

```text
Business behavior
Transactions
Invariants
Consistency
```

The Query Model is optimized for:

```text
Reading
Reporting
Dashboards
Search
Performance
```

That's the fundamental reason for CQRS.

---

# 5. Command Model

Let's create a simple example.

Suppose we have a `Sprint` and a `BacklogItem`.

Instead of exposing setters/getters and allowing arbitrary modification:

```java
item.setSprint(sprint);
```

we expose business behavior:

```java
item.commitTo(sprint);
```

For example:

```java
class BacklogItem {

    private SprintId sprintId;
    private Status status;

    public void commitTo(Sprint sprint) {

        if (status != Status.READY) {
            throw new IllegalStateException(
                "Only ready items can be committed"
            );
        }

        this.sprintId = sprint.id();
        this.status = Status.COMMITTED;
    }
}
```

This is important.

The Command Model is **behavior-oriented**.

Not:

```text
getSomething()
setSomething()
setSomething()
setSomething()
```

but:

```text
commitTo()
cancel()
approve()
reject()
activate()
block()
```

These methods express the **Ubiquitous Language**.

---

# 6. Aggregate in CQRS

An Aggregate protects business invariants.

For example:

```java
class Order {

    private OrderStatus status;

    public void confirm() {

        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                "Order cannot be confirmed"
            );
        }

        status = OrderStatus.CONFIRMED;
    }

    public void cancel() {

        if (status == OrderStatus.SHIPPED) {
            throw new IllegalStateException(
                "Shipped order cannot be cancelled"
            );
        }

        status = OrderStatus.CANCELLED;
    }
}
```

Notice that the Aggregate isn't primarily designed to answer questions like:

```java
getAllOrdersForCustomer(...)
```

or:

```java
getDashboardForCustomer(...)
```

Those belong on the Query side.

---

# 7. Command Model Repository

The repository is also focused on the domain model.

Typical operations:

```java
interface OrderRepository {

    void add(Order order);

    void save(Order order);

    Order fromId(OrderId id);
}
```

The important idea is:

```text
Repository
   ↓
retrieve Aggregate
   ↓
execute business behavior
   ↓
save Aggregate
```

For example:

```java
Order order = repository.fromId(orderId);

order.confirm();

repository.save(order);
```

Not:

```java
repository.getDashboard(...)
```

That belongs to the Query Model.

---

# 8. Domain Events Connect the Two Sides

After a successful command:

```text
Command
   ↓
Aggregate
   ↓
Business operation
   ↓
Domain Event
```

For example:

```java
public record OrderConfirmed(
        OrderId orderId,
        CustomerId customerId
) {}
```

The aggregate can produce:

```text
OrderConfirmed
```

Then the event is published.

```text
Command Model
      │
      │ OrderConfirmed
      ▼
   Event Bus
      │
      ├─────────────→ Read Model
      │
      ├─────────────→ Notification
      │
      └─────────────→ Analytics
```

This is where CQRS starts becoming very powerful.

---

# 9. Query Model

Now imagine we need this screen:

```text
Customer Dashboard

Name
Total Balance
Loan Amount
Number of Orders
Last Transaction
```

Instead of reconstructing everything from multiple aggregates, we can have:

```text
customer_dashboard
────────────────────────────
customer_id
customer_name
total_balance
loan_amount
order_count
last_transaction
```

This table is **denormalized**.

The Query Model doesn't care about preserving the same domain structure as the Command Model.

It cares about:

> **"Can I retrieve the data needed by this screen efficiently?"**

---

# 10. One Table per View

A useful mental model is:

```text
Command Model

Customer
Account
Loan
Transaction
```

while:

```text
Query Model

CustomerDashboard
LoanDashboard
AccountDashboard
TransactionHistory
```

For example:

```text
customer_dashboard
────────────────────
customer_id
name
total_balance
loan_amount
risk_level
last_transaction
```

Then:

```java
interface CustomerDashboardRepository {

    CustomerDashboard findByCustomerId(
            CustomerId customerId
    );
}
```

The query becomes extremely simple:

```sql
SELECT *
FROM customer_dashboard
WHERE customer_id = ?
```

That's the performance advantage.

---

# 11. Why Denormalize?

Traditional relational modeling often tries to normalize data:

```text
Customer
Account
Loan
Transaction
```

Then queries join them.

CQRS says:

> For the read side, we can duplicate data if that makes queries fast and simple.

So:

```text
Customer
Account
Loan
Transaction
       ↓
   Projection
       ↓
CustomerDashboard
```

The same customer information may exist in several read models.

That's okay.

Because the Read Model is optimized for **reading**, not minimizing duplication.

---

# 12. Disposable Read Models

This is an important senior-level concept.

Suppose your Read Model becomes corrupted:

```text
customer_dashboard
        ↓
      broken
```

You can delete it.

Then replay historical events:

```text
Events
 │
 ├── CustomerCreated
 ├── AccountOpened
 ├── MoneyDeposited
 ├── LoanCreated
 ├── PaymentMade
 └── ...
        ↓
    Projection
        ↓
CustomerDashboard
```

So:

> **The Read Model is a projection, not the ultimate source of truth.**

This makes it **disposable/rebuildable**.

---

# 13. Synchronous vs Asynchronous CQRS

This is extremely important for architecture questions.

## Option 1 — Synchronous

```text
Command
   ↓
Write Model
   ↓
Transaction
   ├── Write DB
   └── Update Read DB
```

Both happen in the same transaction.

Advantages:

```text
Strong consistency
```

If the command succeeds:

```text
Write Model = updated
Read Model  = updated
```

But:

```text
More coupling
More latency
Potentially worse SLA
```

---

# 14. Asynchronous CQRS

Much more common in distributed architectures:

```text
Command
   ↓
Write Model
   ↓
Transaction commits
   ↓
Domain Event
   ↓
Message Broker
   ↓
Read Model
```

Example:

```text
Order.confirm()
      ↓
OrderConfirmed
      ↓
Kafka
      ↓
Dashboard Projection
```

The Read Model updates later.

So:

```text
t0 → Order confirmed
t1 → Event published
t2 → Read Model updated
```

Between `t0` and `t2`:

```text
Write Model = new state
Read Model  = old state
```

This is **Eventual Consistency**.

---

# 15. Strong vs Eventual Consistency

Exam question:

> "What is the main trade-off of asynchronous CQRS?"

Answer:

> **Performance and scalability versus immediate consistency.**

### Synchronous

```text
Command
 ↓
Write
 ↓
Read Model
 ↓
Response
```

Strong consistency, but potentially slower.

### Asynchronous

```text
Command
 ↓
Write
 ↓
Event
 ↓
Response

       ...

Event
 ↓
Read Model
```

Faster and more decoupled, but the read side can temporarily be stale.

---

# 16. Handling Stale Data

Suppose the user transfers $100.

Immediately after:

```text
Write Model:
Balance = $900
```

But Read Model hasn't updated yet:

```text
Read Model:
Balance = $1000
```

The UI might temporarily show:

```text
Balance: $1000
```

even though the transaction succeeded.

Senior architects can mitigate this with UI techniques.

For example:

```text
Balance: $900
Last updated: Just now
```

Or temporarily display:

```text
Transfer completed: -$100
```

instead of relying immediately on the projection.

The important thing is:

> **Eventual consistency is not just a backend concern; the UX needs to account for it.**

---

# 17. CQRS Does NOT Necessarily Mean Two Databases

This is a **common exam trap**.

CQRS means:

> **Separate Command and Query responsibilities/models.**

It does **not inherently mean**:

```text
Write DB ≠ Read DB
```

You can have:

```text
Command Model ──→ Database
Query Model   ──→ Same Database
```

or:

```text
Command Model ──→ PostgreSQL

Query Model ────→ Elasticsearch
```

or:

```text
Command Model ──→ Oracle

Query Model ────→ PostgreSQL
```

The important separation is **model responsibility**, not necessarily physical database separation.

---

# 18. CQRS + Event Sourcing

These are often confused.

They are **not the same thing**.

CQRS:

```text
Separate Write Model and Read Model
```

Event Sourcing:

```text
Store state changes as events
```

You can have:

```text
CQRS without Event Sourcing
```

For example:

```text
Write Model → PostgreSQL
Read Model  → PostgreSQL
Events      → Kafka
```

You can also have:

```text
CQRS + Event Sourcing
```

```text
Command
   ↓
Aggregate
   ↓
Events
   ↓
Event Store
   ↓
Read Model
```

So remember:

> **CQRS and Event Sourcing are complementary patterns, not synonyms.**

---

# 19. CQRS vs Normal Repository Query

This is a very important **senior-level trade-off**.

Suppose you only have:

```text
Order
Customer
```

and your UI needs:

```text
Order ID
Customer Name
Order Status
```

You don't necessarily need CQRS.

A normal repository could have:

```java
OrderSummary findOrderSummary(OrderId id);
```

with a specialized query:

```sql
SELECT
    o.id,
    c.name,
    o.status
FROM orders o
JOIN customer c
    ON c.id = o.customer_id
WHERE o.id = ?
```

This is often the **better solution**.

Don't introduce:

```text
Kafka
Event Bus
Projection
Read Database
Eventual Consistency
```

just because the word CQRS sounds architecturally impressive.

---

# 20. When Should You Use CQRS?

Good candidates:

### Complex dashboards

```text
Many aggregates
      ↓
Complex joins
      ↓
High read traffic
```

CQRS can create:

```text
Denormalized Read Model
```

---

### Read-heavy systems

For example:

```text
Writes: 1,000/sec
Reads: 100,000/sec
```

You can scale the read side independently.

```text
             CQRS
              │
      ┌───────┴────────┐
      │                │
   Writes            Reads
      │                │
   3 nodes          20 nodes
```

---

### Different read requirements

One domain may need:

```text
Mobile View
Admin Dashboard
Reporting
Search
Analytics
```

Each can have its own optimized projection.

```text
             Events
                │
      ┌─────────┼──────────┐
      ↓         ↓          ↓
 MobileView AdminView  ReportingView
```

---

# 21. When NOT to Use CQRS

This is equally important.

If you have:

```text
Simple CRUD
   ↓
Simple UI
   ↓
Simple queries
```

CQRS is probably overkill.

For example:

```text
User
 ├── name
 ├── email
 └── phone
```

and your UI simply does:

```text
GET /users/{id}
```

Don't create:

```text
Command Service
Event Bus
Projection Service
Read Database
Event Store
```

unless there's an actual architectural reason.

---

# 22. CQRS in a System Design Interview

Suppose the examiner asks:

> "Design a banking system with a highly complex customer dashboard and very high read traffic."

A strong senior answer could be:

```text
                    Commands
                       │
                       ▼
                Command Model
                       │
                  Transaction
                       │
                       ▼
                 Domain Events
                       │
                       ▼
                   Kafka
                       │
          ┌────────────┼────────────┐
          ▼            ▼            ▼
     Dashboard     Reporting     Search
      Projection   Projection   Projection
          │
          ▼
      Read Store
```

Then explain:

> "I would use CQRS because the dashboard aggregates data across multiple domain aggregates and has significantly higher read traffic than write traffic. The command model remains optimized for business invariants and transactional consistency, while denormalized read models are optimized for the dashboard. I would update the projections asynchronously, accepting eventual consistency on the read side."

That is a **senior-level answer**.

---

# 23. CQRS Exam Cheat Sheet

Memorize this structure:

```text
                 CQRS
                  │
        ┌─────────┴─────────┐
        │                   │
   Command Model        Query Model
        │                   │
   Business Logic       Read Optimization
   Aggregates           Denormalized Data
   Invariants           Projections
   Transactions         Dashboards
        │                   │
        └────── Events ─────┘
```

### Command Model

> **Optimized for changing state and enforcing business rules.**

### Query Model

> **Optimized for retrieving data efficiently.**

### Domain Event

> **Carries the change from the Command side toward projections/other consumers.**

### Denormalization

> **Duplicates data to make reads fast and avoid expensive joins.**

### Projection

> **A Read Model built from domain events or other source data.**

### Eventual Consistency

> **The Read Model may temporarily lag behind the Write Model.**

### Independent Scaling

> **Read and write workloads can be scaled independently.**

---

# 🔥 The 6 Questions I Would Expect in a Senior Exam

You should be able to answer these without notes:

### Q1

**What is the difference between CQS and CQRS?**

Expected:

> CQS separates commands and queries at the method/object level. CQRS applies this separation architecturally by using distinct Command and Query models.

### Q2

**Why would you introduce CQRS?**

Expected:

> When the read requirements are significantly different from the write/domain model, especially for complex views, reporting, high read traffic, or cross-aggregate queries.

### Q3

**Does CQRS require two databases?**

**No.**

It requires separation of responsibilities/models, not necessarily physical databases.

### Q4

**What is the consistency trade-off?**

```text
Synchronous → Strong consistency
Asynchronous → Eventual consistency
```

### Q5

**Is CQRS the same as Event Sourcing?**

**No.**

CQRS separates read/write models.

Event Sourcing stores state changes as events.

They can be used independently or together.

### Q6

**When should you NOT use CQRS?**

> When the domain and read requirements are simple enough for a conventional model and repository queries. CQRS adds operational and architectural complexity, so there should be a concrete reason to introduce it.

---

## The most important mental model

If you remember only one thing, remember this:

```text
                 WRITE SIDE
                    │
              "What can I do?"
                    │
                    ▼
               Domain Model
              Aggregates
              Invariants
                    │
                    │ Events
                    ▼
                 READ SIDE
                    │
              "What can I see?"
                    │
                    ▼
              Read Projection
              Denormalized
              Fast Queries
```

**CQRS is fundamentally about accepting that the model that is best for enforcing business rules is often NOT the model that is best for answering complex queries.**
