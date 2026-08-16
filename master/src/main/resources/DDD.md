# SENIOR DEVELOPER EXAM - DDD COMPLETE CHEAT SHEET

## 🎯 THE BIG PICTURE: PROBLEM SPACE vs. SOLUTION SPACE

| | Problem Space | Solution Space |
|:---|:---|:---|
| **Definition** | *What* the business needs. The business reality. | *How* we build it. The software architecture. |
| **Contains** | Business goals, workflows, pain points, revenue, customers | Code, databases, APIs, microservices, design patterns |
| **Owned by** | Business Owners, Domain Experts | Software Architects, Senior Developers |
| **Action** | **Discovered** (you find it by talking to the business) | **Invented** (you design it) |
| **Example** | "We need to ship packages faster than Amazon" | "We'll build a ShippingContext with a Kafka event bus" |

---

## 🏗️ STRATEGIC DDD - THE ARCHITECTURE

### 1. DOMAIN, SUBDOMAINS, & BOUNDED CONTEXTS

```
PROBLEM SPACE (Business Reality)          SOLUTION SPACE (Software)
═══════════════════════════════════════════════════════════════════

  ┌─────────────────────────────┐
  │       THE DOMAIN            │
  │    (Entire Business)        │
  │    e.g., "E-commerce"       │
  │                              │
  │  ┌────────────────────┐     │         ┌──────────────────────┐
  │  │   SUBDOMAINS       │     │  MAPS   │  BOUNDED CONTEXTS   │
  │  │  (Business Parts)  │     │────────►│  (Software Parts)   │
  │  │                    │     │   TO    │                      │
  │  │  CORE (Pricing)    │─────│────────►│  PricingContext     │
  │  │  SUPPORTING (Inv.) │─────│────────►│  InventoryContext   │
  │  │  GENERIC (Email)   │─────│────────►│  NotificationContext│
  │  └────────────────────┘     │         └──────────────────────┘
  └─────────────────────────────┘
```

### 2. SUBDOMAIN TYPES (Problem Space)

| Type | Definition | Example | Investment |
|:---|:---|:---|:---|
| **Core Domain** | Competitive advantage. What makes you money. | Amazon's recommendation engine | **BEST TALENT** |
| **Supporting Subdomain** | Essential but not differentiating. | Employee onboarding | Moderate |
| **Generic Subdomain** | Standard problems. Buy, don't build. | Authentication, Email, Logging | **MINIMAL** |

**The Golden Rule:** Never mix Core and Generic in the same Bounded Context!

### 3. BOUNDED CONTEXT (Solution Space)

**Definition:** A linguistic and conceptual boundary where a specific model applies. Inside this boundary, every term has **ONE unambiguous meaning**.

**Key Rules:**
- ✅ One Bounded Context = One Team (5-8 developers)
- ✅ One Bounded Context = One Ubiquitous Language
- ✅ One Bounded Context = Independent Deployability
- ✅ Map ONE Subdomain to ONE Bounded Context
- ❌ Never merge Core and Generic in one BC

**Example: "Customer" in different BCs:**

| Bounded Context | What "Customer" means |
|:---|:---|
| `SalesContext` | Name, Email, DealSize |
| `SupportContext` | OpenTickets, SubscriptionTier |
| `BillingContext` | PaymentMethod, Balance |

### 4. UBIQUITOUS LANGUAGE

**Definition:** A shared language between developers and domain experts. Zero translation between business talk and code.

**The Test:** If a domain expert reads your code, they should understand it immediately.

| ❌ Bad (Translation Required) | ✅ Good (Ubiquitous Language) |
|:---|:---|
| `ProcessOrder()` | `FulfillShipment()` |
| `CUST_ID`, `ACTIVE_FLAG` | `CustomerId`, `IsActive` |
| `UpdateStatus()` | `ShipOrder()` |

---

## 🗺️ CONTEXT MAPPING - HOW BCs TALK

### Upstream (U) vs. Downstream (D)

| Role | Definition | Power |
|:---|:---|:---|
| **Upstream (U)** | Provider of data/functionality | **POWERFUL** - Changes force downstream to adapt |
| **Downstream (D)** | Consumer of data/functionality | **VULNERABLE** - Must adapt to upstream changes |

### 5 CONTEXT MAPPING PATTERNS

| Pattern | Definition | When to Use | Code Example |
|:---|:---|:---|:---|
| **Anticorruption Layer (ACL)** | Translation layer protecting downstream from messy upstream | Legacy systems, third-party APIs, frequent changes | `LegacyCustomerAdapter` translating `CUST_ID` → `CustomerId` |
| **Shared Kernel** | Small shared model between two teams | Stable data structures, close collaboration, co-located teams | Shared `Address` or `Money` class |
| **Conformist** | Downstream accepts upstream model exactly | Well-designed upstream, stable API, limited time | Directly using Stripe's `PaymentIntent` model |
| **Open Host Service** | Upstream provides a well-documented API for downstream | API-first design, multiple downstream consumers | REST API with versioning |
| **Published Language** | Upstream publishes a standard format (JSON/XML) | Integration between different tech stacks | JSON schema for events |

### The Decision Matrix

| Question | Answer | Pattern |
|:---|:---|:---|
| Is upstream messy/legacy? | YES | **ACL** |
| Is upstream clean and stable? | YES | **Conformist** |
| Need to share stable data with another team? | YES | **Shared Kernel** |
| Want complete independence? | YES | **ACL** (preferred) |
| Limited time/budget? | YES | **Conformist** (quickest) |

---

## 🔧 TACTICAL DDD - CODE BUILDING BLOCKS

### 1. AGGREGATES (Transactional Consistency Boundary)

**Definition:** A cluster of objects treated as a single unit for data changes.

**The Rules:**
1. ✅ **Small aggregates** - Only include what must be transactionally consistent
2. ✅ **Reference by ID** - Never by object reference
3. ✅ **One Aggregate Root** - Only entry point from outside
4. ✅ **Enforce invariants** - Business rules inside the root

```csharp
// ✅ GOOD: Small aggregate, ID references
public class Order : AggregateRoot {
    public int Id { get; private set; }
    public int CustomerId { get; private set; }  // ID reference
    public List<OrderItem> Items { get; private set; }
    
    // Enforce invariants
    public void AddItem(int productId, int quantity) {
        if (Status == OrderStatus.Shipped)
            throw new DomainException("Cannot modify shipped order");
        Items.Add(new OrderItem(productId, quantity));
    }
}

// ❌ BAD: Large aggregate, object references
public class Order {
    public Customer Customer { get; set; }  // Object reference
    public List<Product> Products { get; set; }  // Object reference
    // 50 more fields...
}
```

### 2. ENTITIES vs. VALUE OBJECTS

| | Entities | Value Objects |
|:---|:---|:---|
| **Identity** | ✅ Has a unique, stable identity | ❌ No identity |
| **Change** | ✅ Can change over time | ❌ **Immutable** (replace whole instance) |
| **Equality** | Based on ID | Based on ALL attributes |
| **Example** | `User`, `Order`, `Customer` | `Money`, `Address`, `Email` |
| **Rule** | Track their lifecycle | Replace them entirely |

```csharp
// Entity - Identity matters
public class Customer {
    public int Id { get; private set; }  // Identity
    public string Name { get; set; }     // Can change
}

// Value Object - No identity, immutable
public class Money : ValueObject {
    public decimal Amount { get; }
    public string Currency { get; }
    
    public Money Add(Money other) {
        return new Money(Amount + other.Amount, Currency);
    }
}
```

**When to use Value Objects:**
- ✅ Prefer VOs over primitives (`Money` vs `decimal`)
- ✅ Prefer VOs over Entities for simple data
- ✅ Use VOs for thread-safety and simplicity

### 3. DOMAIN SERVICES

**Definition:** Stateless operations that don't fit in an Entity or Value Object.

**When to use:**
- ✅ Operation involves **multiple aggregates**
- ✅ Operation is a **core business rule**
- ✅ Operation doesn't naturally belong to one Entity

```csharp
// Domain Service - Cross-aggregate business logic
public interface IShippingService {
    Shipment ScheduleShipment(Order order, Address address);
}

public class ShippingService : IShippingService {
    private readonly IInventoryService _inventory;
    
    public Shipment ScheduleShipment(Order order, Address address) {
        // Complex logic spanning multiple aggregates
        foreach (var item in order.Items) {
            _inventory.Reserve(item.ProductId, item.Quantity);
        }
        return new Shipment(order.Id, address);
    }
}
```

### 4. DOMAIN EVENTS

**Definition:** A record of something significant that happened in the domain.

**Primary use:** Achieving **eventual consistency** across aggregates or BCs.

```csharp
// 1. Define the event
public class OrderShipped : IDomainEvent {
    public Guid OrderId { get; set; }
    public DateTime ShippedDate { get; set; }
}

// 2. Raise in aggregate
public class Order {
    public void Ship() {
        // Business logic
        _status = OrderStatus.Shipped;
        AddDomainEvent(new OrderShipped { OrderId = Id });
    }
}

// 3. Handle elsewhere (eventual consistency)
public class OnOrderShipped {
    public void Handle(OrderShipped @event) {
        // Send email, update inventory, notify logistics
        // All asynchronously!
    }
}
```

### 5. TRANSACTIONAL vs. EVENTUAL CONSISTENCY

| Aspect | Transactional Consistency | Eventual Consistency |
|:---|:---|:---|
| **Scope** | Within ONE aggregate | Across aggregates or BCs |
| **Performance** | Locks resources | Better scalability |
| **Complexity** | Simpler | More complex (handles failures) |
| **When to use** | Invariants must be immediate | Non-critical delays acceptable |

**The Golden Rule:**
- ✅ **Within aggregate** → Transactional Consistency
- ✅ **Between aggregates** → Eventual Consistency (via Domain Events)
- ✅ **Between BCs** → Eventual Consistency (via Domain Events)

### 6. REPOSITORIES

**Definition:** Provides the illusion of an in-memory collection for aggregate roots.

**Rules:**
- ✅ One repository per **Aggregate Root**
- ✅ Returns **Aggregate Roots** only
- ✅ Use for finding by ID
- ❌ Never expose internal entities

```csharp
public interface IOrderRepository {
    Order GetById(Guid id);
    IEnumerable<Order> GetByCustomer(Guid customerId);
    void Save(Order order);
}

// Usage - Work with aggregates as if in memory
var order = _orderRepo.GetById(orderId);
order.AddItem(productId, quantity);
_orderRepo.Save(order);
```

### 7. FACTORIES

**Definition:** Encapsulates complex creation of aggregates, ensuring all invariants are satisfied.

```csharp
public class OrderFactory {
    public Order CreateOrder(
        CustomerId customerId, 
        List<OrderItem> items) {
        // Validate all invariants
        // Apply discounts
        // Calculate taxes
        // Return a valid Order
        return new Order(customerId, items);
    }
}

// Usage
var order = _orderFactory.CreateOrder(customerId, items);
_orderRepo.Save(order);
```

---

## 📝 EXAM READINESS CHECKLIST

### For the 4 Architectural Challenges (Strategic DDD)
- [ ] Can I identify **Core/Supporting/Generic Subdomains**?
- [ ] Can I draw **Bounded Context** boundaries?
- [ ] Can I choose the right **Context Mapping pattern** (ACL, Shared Kernel, Conformist)?
- [ ] Can I identify **Upstream/Downstream** relationships?

### For the 50 Descriptive Questions (Tactical DDD)
- [ ] Can I explain **Transactional vs. Eventual Consistency**?
- [ ] Can I justify why **Value Objects > Entities** for performance?
- [ ] Can I design a **small aggregate** with ID references?
- [ ] Can I explain **Domain Events** and when to use them?

---

## 🚀 SENIOR-LEVEL BUZZWORDS TO DROP

| Term | Context | Why It's Senior-Level |
|:---|:---|:---|
| **Ubiquitous Language** | Communication | Shows business-code alignment |
| **Transactional Consistency Boundary** | Aggregates | Technical precision |
| **Independent Deployability** | BCs/Microservices | Architectural maturity |
| **Anticorruption Layer** | Integration | Practical experience |
| **Invariants** | Business rules | Mathematical rigor |
| **Eventual Consistency** | Domain Events | Advanced architecture |
| **Value Object Immutability** | Tactical design | Deep OOP understanding |
| **Context Map** | Strategic design | System-level thinking |
| **Big Ball of Mud** | Anti-pattern | Knows the pitfalls |

---

## ⚡ QUICK ANSWER TEMPLATES

### "How do Subdomains relate to Bounded Contexts?"
> *"Subdomains exist in the **Problem Space** and represent distinct business capabilities. Bounded Contexts exist in the **Solution Space** and represent the software boundaries we invent to solve those capabilities. We achieve strategic alignment by **mapping** each Subdomain to its own Bounded Context, ensuring the Core Subdomain receives the most investment and is never mixed with Generic Subdomains."*

### "When should you use an ACL?"
> *"I would implement an **Anticorruption Layer** when the upstream system is messy, legacy, or a third-party API. The ACL translates their ugly data structures into our pure Ubiquitous Language, protecting our domain model from external chaos. This allows us to swap out the upstream system later without rewriting our core logic."*

### "Why should Aggregates be small?"
> *"Small aggregates are better for **performance** (less to load), **concurrency** (fewer conflicts), and **scalability**. Large aggregates cause performance bottlenecks and force users to wait for locks. They should reference other aggregates **by ID**, not by object reference, to maintain loose coupling and clear transactional boundaries."*

---

## 🎯 THE SENIOR MINDSET

Remember these 5 principles in every exam answer:

1. **Problem first, Solution second** - Always start with business needs
2. **Language is king** - If the code doesn't match business talk, it's wrong
3. **Keep boundaries clear** - Each BC has ONE language and ONE team
4. **Protect your core** - Build ACLs, use events, never mix Core with Generic
5. **Think in trade-offs** - Transactional vs. Eventual, ACL vs. Conformist

---

## ✅ FINAL CHECK: DOMAIN vs. SUBDOMAIN vs. BC

| Concept | Space | Definition | Example |
|:---|:---|:---|:---|
| **Domain** | Both | The entire business world | "E-commerce" |
| **Subdomain** | Problem | A specific business capability | "Pricing" (Core), "Inventory" (Supporting), "Email" (Generic) |
| **Bounded Context** | Solution | A software boundary that solves a Subdomain | `PricingContext`, `InventoryContext`, `NotificationContext` |

**The Cardinal Rule:**
> **Subdomains are mapped TO Bounded Contexts. They never live INSIDE them. a single Subdomain CAN be broken into multiple Bounded Contexts. But this is a technical decision driven by team size, complexity, and deployment needs—NOT by the business.**
