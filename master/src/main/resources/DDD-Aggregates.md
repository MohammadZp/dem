# 2. DDD Aggregates

We'll start with the most fundamental idea:

> **An Aggregate is a consistency boundary.**

If you truly understand that sentence, most of the other Aggregate rules become much easier.

---

# 1. What problem does an Aggregate solve?

Imagine a banking system with:

```text
Loan
 ├── Customer
 ├── Installments
 ├── Collateral
 └── Payments
```

A beginner might naturally model this as one big object graph:

```java
class Loan {

    Customer customer;
    List<Installment> installments;
    List<Collateral> collaterals;
    List<Payment> payments;
}
```

Looks reasonable.

But now imagine a loan has:

* 100 installments
* 20 payments
* 5 collaterals

and thousands of loans are being processed concurrently.

If the entire graph is treated as one transactional unit, you can create serious problems:

* large transactions
* more locking
* database contention
* longer transactions
* higher probability of deadlocks
* poor scalability

DDD asks a more important question:

> **Which objects must be consistent with each other immediately, in the same transaction?**

That question leads us to the Aggregate.

---

# 2. Aggregate = Consistency Boundary

Suppose we have:

```text
Order
 ├── OrderLine
 ├── OrderLine
 └── OrderLine
```

Business rule:

> An Order cannot be confirmed unless it has at least one OrderLine.

This rule requires:

```text
Order + OrderLines
```

to be consistent **at the same time**.

So we might define:

```text
┌─────────────────────────┐
│       Order Aggregate   │
│                         │
│  Order                  │
│   ├── OrderLine         │
│   ├── OrderLine         │
│   └── OrderLine         │
│                         │
└─────────────────────────┘
```

This is one Aggregate.

The important point:

> **The Aggregate defines the boundary within which business invariants are guaranteed transactionally.**

---

# 3. Aggregate Root

Every Aggregate has one entry point:

> **Aggregate Root**

For our example:

```text
Order ← Aggregate Root
  │
  ├── OrderLine
  ├── OrderLine
  └── OrderLine
```

External objects shouldn't directly manipulate internal objects.

Instead:

```java
order.addLine(productId, quantity);
```

rather than:

```java
order.getLines().add(
    new OrderLine(...)
);
```

Why?

Because the Aggregate Root protects its invariants.

---

# 4. What is an Invariant?

An **invariant** is a business rule that must always be true within the consistency boundary.

Example:

```text
Order
```

has:

```text
total = 100
```

and:

```text
OrderLine
1 → 60
2 → 40
```

Invariant:

```text
Order.total = sum(OrderLines)
```

If somebody can directly modify an `OrderLine` without the Order knowing about it:

```text
OrderLine.price = 500
```

we could end up with:

```text
Order.total = 100
OrderLines = 540
```

Our invariant is broken.

So we want:

```text
External world
      ↓
Order (Aggregate Root)
      ↓
OrderLine
```

The root controls changes.

---

# 5. The most important question: What belongs inside?

Don't ask:

> "What objects are related?"

Ask:

> **"What objects must be transactionally consistent?"**

This distinction is extremely important.

Suppose:

```text
Order
Customer
Product
Payment
```

All are related.

But does an Order need the entire Customer object to be updated in the same transaction?

Probably not.

Does it need the entire Product Aggregate?

Probably not.

Therefore:

```text
┌──────────────────┐
│ Order Aggregate  │
│                  │
│ Order            │
│ OrderLines       │
└──────────────────┘

┌──────────────────┐
│ Customer         │
│ Aggregate        │
└──────────────────┘

┌──────────────────┐
│ Product          │
│ Aggregate        │
└──────────────────┘
```

Now we have separate consistency boundaries.

---

# 6. Why Aggregates should usually be small

Suppose we create:

```text
Bank Aggregate
│
├── Customer
├── Account
├── Loan
├── Payment
├── Transaction
├── Card
├── Branch
└── ...
```

We've basically created one giant transactional boundary.

That's terrible.

Imagine:

```text
Transaction A
    ↓
Bank Aggregate
    ↓
locks 500 objects
```

Meanwhile:

```text
Transaction B
    ↓
Bank Aggregate
    ↓
needs the same objects
```

Now concurrency suffers.

So the DDD rule of thumb is:

> **Prefer small Aggregates.**

Small Aggregates generally mean:

* smaller transactions
* fewer locks
* less contention
* fewer deadlocks
* better concurrency
* better scalability

---

# 7. Small Aggregate ≠ small class

Important distinction.

This:

```text
Order Aggregate
 ├── Order
 └── OrderLine × 100
```

can be perfectly reasonable.

The goal isn't:

> "Make every Aggregate tiny."

The goal is:

> **Include only what must participate in the same consistency boundary.**

---

# 8. Reference other Aggregates by ID

This is one of the most important DDD rules.

Suppose:

```text
Order Aggregate
Customer Aggregate
Product Aggregate
```

Don't model:

```java
class Order {

    Customer customer;
    Product product;
}
```

as a domain-level Aggregate relationship if that means pulling the entire other Aggregates into the same object/consistency boundary.

Prefer:

```java
class Order {

    CustomerId customerId;
    ProductId productId;
}
```

So:

```text
Order
 ├── customerId
 └── productId
```

instead of:

```text
Order
 ├── Customer object
 └── Product object
```

The key principle:

> **An Aggregate can reference another Aggregate by identity, but should not require the other Aggregate to be part of its consistency boundary.**

---

# 9. Why ID instead of object reference?

Imagine:

```java
class Order {
    Customer customer;
}
```

Now developers naturally start doing:

```java
order.getCustomer().getAddress().getCity()
```

and:

```java
order.getCustomer().changeAddress(...)
```

Now Order is reaching into another Aggregate.

This starts breaking the boundary.

With:

```java
class Order {
    CustomerId customerId;
}
```

you naturally have:

```java
Customer customer =
    customerRepository.findById(order.customerId());
```

Now the two Aggregates remain separate.

---

# 10. This leads to Eventual Consistency

Suppose:

```text
Order Aggregate
Customer Aggregate
```

An order is created.

The Customer Aggregate needs to know:

> "Customer has placed an order."

Do we update both Aggregates in one giant transaction?

Usually, **no**.

Instead:

```text
Order
  │
  │ transaction
  ↓
OrderCreated
  │
  ↓
Event handler
  │
  ↓
Customer
```

Now:

```text
T1:
Order = updated

T2:
Customer = updated
```

There can be a short period where:

```text
Order = new state
Customer = old state
```

That's **eventual consistency**.

Eventually:

```text
Customer → new state
```

---

# 11. But don't use Eventual Consistency blindly

This is a very important exam point.

Suppose business rule says:

> "You cannot transfer money unless both accounts are updated atomically."

If:

```text
Account A
Account B
```

are separate Aggregates, you can't simply say:

> "We'll use eventual consistency."

Maybe the business invariant actually requires atomicity.

This tells us something important:

> **Aggregate boundaries should be derived from business invariants, not technical convenience.**

If two objects must always change atomically to preserve a business rule, they may belong inside the same Aggregate—or the business process may need a different design.

---

# 12. Example: Bank Transfer

Suppose:

```text
Account A: 1000
Account B: 500
```

Transfer:

```text
A → B : 200
```

Business invariant:

```text
A = 800
B = 700
```

If we update A now:

```text
A = 800
B = 500
```

and wait 5 seconds before updating B, is that acceptable?

Maybe not.

If the business requires atomic balance changes, the domain model and transaction boundary need to reflect that requirement.

This is why:

> **Aggregate design is fundamentally about business consistency, not database tables.**

---

# 13. Aggregate ≠ Database Transaction

They are closely related, but don't confuse them.

An Aggregate defines:

> **The business consistency boundary.**

A transaction defines:

> **The atomicity boundary of a particular operation.**

In DDD, we generally want a command that modifies an Aggregate to commit its invariant atomically.

But the concepts aren't identical.

---

# 14. Tell, Don't Ask

Now let's connect Aggregates to your next bullet.

Bad:

```java
if (order.getStatus() == PENDING) {
    order.setStatus(APPROVED);
}
```

The caller is asking the object for its internal state and deciding what to do.

This is **Ask, Then Act**.

Better:

```java
order.approve();
```

The Aggregate itself decides:

```java
public void approve() {

    if (status != PENDING) {
        throw new IllegalStateException();
    }

    status = APPROVED;
}
```

Now the business rule is inside the Aggregate.

This is:

> **Tell, Don't Ask.**

Tell the object what you want:

```java
order.approve();
```

rather than asking for its data and manipulating it externally.

---

# 15. Why is Tell, Don't Ask important for Aggregates?

Because the Aggregate Root should protect its invariants.

Bad:

```java
order.getLines().clear();
order.setStatus(CANCELLED);
order.setTotal(0);
```

External code is manipulating internals.

Better:

```java
order.cancel();
```

The Aggregate controls the transition.

```text
Application Service
        │
        │ tell
        ↓
Order.cancel()
        │
        ↓
protect invariants
```

---

# 16. Law of Demeter

Law of Demeter is basically about limiting how much an object knows about the internal structure of other objects.

Bad:

```java
order.getCustomer()
     .getAddress()
     .getCity();
```

This is a classic **train wreck**.

You're traversing:

```text
Order
 ↓
Customer
 ↓
Address
 ↓
City
```

The caller knows too much about the internal structure.

Better:

```java
order.customerCity();
```

or, depending on the domain:

```java
customer.city();
```

The exact implementation depends on the model, but the principle is:

> **Don't reach through objects to manipulate or depend on their internal structure.**

---

# 17. Law of Demeter + Aggregates

This becomes especially important across Aggregate boundaries.

Bad:

```java
order.getCustomer()
     .getAccount()
     .getBalance();
```

Now Order → Customer → Account.

You're effectively navigating through multiple domain objects.

Better:

```java
customerAccountService.getBalance(customerId);
```

or whatever API represents the actual business operation.

The point is not to blindly create methods like:

```java
getX()
getY()
getZ()
```

The point is to keep **encapsulation and Aggregate boundaries intact**.

---

# 18. Putting everything together

Let's say we have:

```text
Order Aggregate
│
├── Order
└── OrderLine

Customer Aggregate
│
└── Customer

Product Aggregate
│
└── Product
```

Order contains:

```java
class Order {

    private CustomerId customerId;
    private List<OrderLine> lines;
}
```

Not:

```java
class Order {

    private Customer customer;
    private List<Product> products;
}
```

The Application Service might do:

```java
public void createOrder(CreateOrderCommand command) {

    Customer customer =
        customerRepository.findById(command.customerId());

    // validate customer eligibility

    Order order =
        Order.create(command.customerId());

    order.addLine(
        command.productId(),
        command.quantity()
    );

    orderRepository.save(order);

    eventPublisher.publish(
        new OrderCreated(order.id())
    );
}
```

Notice what's happening:

```text
Application Service
        │
        ├── loads other Aggregates if needed
        │
        ↓
      Order
        │
        └── protects its own invariants
```

The Application Service coordinates.

The Aggregate owns business behavior.

That's a very important connection to **Topic 3: Application Layer**, which we'll study next.

---

# 19. The exam mental model

When you see a DDD design question, ask these questions **in this order**:

### Question 1

**What business invariant am I protecting?**

### Question 2

**Which objects must be consistent immediately to protect that invariant?**

Those objects are candidates for the same Aggregate.

### Question 3

**Can the Aggregate be smaller?**

If yes, consider separating it.

### Question 4

**How does one Aggregate refer to another?**

Usually:

```text
ID
```

not a direct object relationship that crosses the consistency boundary.

### Question 5

**Does the operation require immediate consistency across Aggregates?**

If not, consider:

```text
Domain Event
     ↓
Eventual Consistency
```

### Question 6

**Who protects the invariant?**

Usually:

```text
Aggregate Root
```

not the Application Service.

---

# The 5 rules you should remember

For the exam:

> **1. Aggregate = consistency boundary.**

> **2. Aggregate Root protects the invariants of the Aggregate.**

> **3. Prefer small Aggregates.**

> **4. Reference other Aggregates by identity, not direct object references.**

> **5. Changes across Aggregate boundaries are often eventually consistent.**

And for encapsulation:

> **Tell, Don't Ask + Law of Demeter → don't let external code manipulate or navigate through Aggregate internals.**

---

## Exam scenario

Let's test your understanding before moving to **Application Services**.

You have:

```text
Order
 ├── Customer
 ├── Product
 ├── OrderLines
 └── Payment
```

The business rule is:

> **An Order can only be confirmed if it contains at least one OrderLine and its total is greater than zero.**

And there are 10,000 concurrent orders.

A developer proposes:

```java
class Order {

    Customer customer;
    Product product;
    List<OrderLine> lines;
    Payment payment;
}
```

and says:

> "They're all related to the Order, so they should all be inside one Aggregate."

