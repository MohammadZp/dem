# 1. Message Intent

A message can have different **intentions**.

The three important types are:

```text
Message Intent
│
├── Command Message
├── Document Message
└── Event Message
```

The key question is:

> **What is the sender trying to communicate by sending this message?**

---

# 1. Command Message

### Core idea

A Command says:

> **"Do this."**

The sender is asking the receiver to perform a specific action.

### Example

An Order Service wants the Payment Service to process a payment:

```text
Order Service
      │
      │ PayOrderCommand
      ▼
Payment Service
```

Java:

```java
public record PayOrderCommand(
        Long orderId,
        BigDecimal amount
) {}
```

Producer:

```java
class OrderService {

    void payOrder(Long orderId, BigDecimal amount) {

        PayOrderCommand command =
                new PayOrderCommand(orderId, amount);

        messageBroker.send(command);
    }
}
```

Consumer:

```java
class PaymentService {

    void handle(PayOrderCommand command) {

        paymentService.pay(
                command.orderId(),
                command.amount()
        );
    }
}
```

The important thing is that the sender is saying:

```text
"Payment Service, please perform this operation."
```

### Typical Commands

```text
CreateOrderCommand
CancelOrderCommand
PayOrderCommand
BlockChequeCommand
ReserveInventoryCommand
```

### Exam definition

> **A Command Message requests the receiver to perform a specific operation.**

---

# 2. Document Message

A Document Message says:

> **"Here is some data."**

The sender is **not explicitly telling the receiver what operation to perform**.

For example:

```json
{
  "customerId": 123,
  "name": "Ali",
  "email": "ali@example.com"
}
```

Java:

```java
public record CustomerDocument(
        Long customerId,
        String name,
        String email
) {}
```

Producer:

```java
class CustomerService {

    void publishCustomer(Customer customer) {

        CustomerDocument document =
                new CustomerDocument(
                        customer.id(),
                        customer.name(),
                        customer.email()
                );

        messageBroker.send(document);
    }
}
```

Consumer:

```java
class MarketingService {

    void handle(CustomerDocument document) {

        updateCustomerProfile(document);
    }
}
```

Another consumer could do something completely different:

```java
class AnalyticsService {

    void handle(CustomerDocument document) {

        storeForAnalytics(document);
    }
}
```

The sender doesn't say:

```text
"Marketing Service, do X."
```

It simply provides information.

### Exam definition

> **A Document Message transfers data without explicitly specifying the operation that the receiver should perform.**

---

# Command vs Document

This distinction is **very important for the exam**.

### Command

```text
"Do something."
```

Example:

```json
{
  "type": "ShipOrder",
  "orderId": 123
}
```

### Document

```text
"Here is something."
```

Example:

```json
{
  "orderId": 123,
  "customerId": 50,
  "items": [...]
}
```

Think:

```text
Command  → ACTION
Document → DATA
```

---

# 3. Event Message

An Event says:

> **"Something happened."**

The sender isn't asking anyone to perform a particular action.

For example:

```text
Order Service
      │
      │ OrderPlacedEvent
      ▼
    Broker
      │
      ├────→ Payment Service
      ├────→ Inventory Service
      └────→ Notification Service
```

Java:

```java
public record OrderPlacedEvent(
        Long orderId,
        Long customerId,
        Instant occurredAt
) {}
```

Producer:

```java
class OrderService {

    void createOrder(Order order) {

        orderRepository.save(order);

        OrderPlacedEvent event =
                new OrderPlacedEvent(
                        order.id(),
                        order.customerId(),
                        Instant.now()
                );

        eventPublisher.publish(event);
    }
}
```

Now multiple consumers can react:

```java
class PaymentService {

    void handle(OrderPlacedEvent event) {
        createPayment(event.orderId());
    }
}
```

```java
class InventoryService {

    void handle(OrderPlacedEvent event) {
        reserveInventory(event.orderId());
    }
}
```

```java
class NotificationService {

    void handle(OrderPlacedEvent event) {
        sendConfirmation(event.customerId());
    }
}
```

The Order Service doesn't need to know about these consumers.

---

# Command vs Event

This is another **very common exam question**.

Suppose an order has been created.

### Command

```text
ProcessPaymentCommand
```

means:

> **"Payment Service, process this payment."**

The sender wants something to happen.

### Event

```text
OrderPlacedEvent
```

means:

> **"An order has been placed."**

The sender is informing other components about a fact.

So remember:

```text
Command → intention
Event   → fact
```

Or even simpler:

```text
Command → "Do X"
Event   → "X happened"
Document → "Here is X"
```

---

# Banking Example

This is a good way to remember them.

Suppose a customer wants to block a cheque.

### Command

```java
public record BlockChequeCommand(
        String chequeId
) {}
```

Meaning:

> **Block this cheque.**

After the operation succeeds:

### Event

```java
public record ChequeBlockedEvent(
        String chequeId
) {}
```

Meaning:

> **This cheque has been blocked.**

Then multiple systems can react:

```text
ChequeBlockedEvent
       │
       ├── Notification
       ├── Audit
       ├── Reporting
       └── Risk
```

Notice the difference:

```text
BlockChequeCommand
        ↓
      ACTION

ChequeBlockedEvent
        ↓
       FACT
```

---

# 4. Request-Reply

Now let's move to **asynchronous interaction patterns**.

Sometimes a sender wants to send a request and eventually receive a response:

```text
Client
  │
  │ Request
  ▼
Service
  │
  │ Reply
  ▼
Client
```

For example:

```java
public record GetCustomerRequest(
        Long customerId
) {}

public record GetCustomerResponse(
        Long customerId,
        String name
) {}
```

The client sends:

```java
messageBroker.send(
        new GetCustomerRequest(123L)
);
```

The service processes it:

```java
GetCustomerResponse handle(
        GetCustomerRequest request
) {

    Customer customer =
            customerRepository.findById(
                    request.customerId()
            );

    return new GetCustomerResponse(
            customer.id(),
            customer.name()
    );
}
```

The important point is that messaging doesn't necessarily mean **fire-and-forget**.

You can have:

```text
Request → asynchronous processing → Reply
```

---

# 5. Correlation Identifier

This is **extremely important**.

Imagine you send:

```text
Request A
Request B
Request C
```

and responses arrive in a different order:

```text
Response B
Response C
Response A
```

How do you know which response belongs to which request?

Use a **Correlation ID**.

```text
Request A
correlationId = ABC

        ↓

Response
correlationId = ABC
```

Java:

```java
public record MessageHeaders(
        String messageId,
        String correlationId
) {}
```

Request:

```java
String correlationId =
        UUID.randomUUID().toString();

MessageHeaders headers =
        new MessageHeaders(
                UUID.randomUUID().toString(),
                correlationId
        );
```

Response:

```java
MessageHeaders responseHeaders =
        new MessageHeaders(
                UUID.randomUUID().toString(),

                // same correlation ID
                request.headers().correlationId()
        );
```

So:

```text
Request                     Response
────────                    ────────
correlationId = ABC   →     correlationId = ABC
```

### Exam definition

> **A Correlation Identifier is an identifier used to associate a message with a related message or conversation, typically linking a reply to its original request.**

Remember:

> **Correlation ID answers: "Which request does this message belong to?"**

---

# 6. Return Address

Now imagine the receiver doesn't know where to send the response.

The sender can specify a **Return Address**.

```text
Request
│
├── correlationId = ABC
└── returnAddress = customer-replies
```

Java:

```java
public record MessageHeaders(
        String correlationId,
        String returnAddress
) {}
```

Consumer:

```java
void handle(Message<Request> message) {

    Response response =
            process(message.body());

    messageBroker.send(
            message.headers().returnAddress(),
            response
    );
}
```

So:

```text
Request
   │
   │ returnAddress = customer-replies
   ▼
Service
   │
   │
   ▼
customer-replies
```

### Important distinction

| Pattern                    | Question it answers                         |
| -------------------------- | ------------------------------------------- |
| **Correlation Identifier** | Which request does this response belong to? |
| **Return Address**         | Where should the response be sent?          |

---

# 7. Message Sequence

Suppose we have a huge message:

```text
10 GB Customer Data
```

Sending it as one message might be problematic.

We can split it:

```text
Large Message
     │
     ├── Part 1
     ├── Part 2
     ├── Part 3
     └── Part 4
```

Each message contains sequence information:

```java
public record MessagePart(
        String sequenceId,
        int sequenceNumber,
        boolean last,
        String data
) {}
```

Example:

```text
sequenceId = ABC
sequenceNumber = 1
last = false

sequenceId = ABC
sequenceNumber = 2
last = false

sequenceId = ABC
sequenceNumber = 3
last = true
```

The receiver can reconstruct the original message.

### Exam idea

> **Message Sequence breaks a large message into smaller ordered messages and provides enough information to reconstruct the original sequence.**

---

# 8. Message Expiration

Some messages are only useful for a limited amount of time.

Examples:

```text
OTP
Payment authorization
Flash price
Time-sensitive notification
```

Example:

```java
public record PaymentRequest(
        Long orderId,
        Instant expiresAt
) {}
```

Consumer:

```java
if (message.expiresAt().isBefore(Instant.now())) {
    discard(message);
    return;
}

process(message);
```

The idea is:

```text
Message
   │
   ├── valid → process
   │
   └── expired → discard
```

### Exam definition

> **Message Expiration prevents a message from being processed after its useful lifetime.**

---

# 9. Format Indicator

The receiver may need to know **what format or version** the message uses.

For example:

```json
{
  "headers": {
    "format": "customer-v2"
  },
  "body": {
    "id": 123,
    "name": "Ali"
  }
}
```

Or:

```text
format = JSON
schema = customer-v2
version = 2
```

This becomes especially important when messages evolve:

```text
Producer
   │
   ├── Customer V1
   └── Customer V2
```

The consumer can use the format/version information to determine how to deserialize or process the message.

---

# Advanced Patterns

Now we move to **Message Routing** and **Message Transformation**.

---

# 10. Content-Based Router

The router examines the **content of a message** and decides where to send it.

Example:

```text
Order
 │
 ├── country = IR → Iran Processor
 │
 ├── country = US → US Processor
 │
 └── country = EU → EU Processor
```

Java:

```java
class OrderRouter {

    void route(Order order) {

        switch (order.country()) {

            case "IR" ->
                    iranQueue.send(order);

            case "US" ->
                    usQueue.send(order);

            default ->
                    euQueue.send(order);
        }
    }
}
```

The important phrase:

> **Route based on message content.**

---

# 11. Splitter

A Splitter takes **one composite message** and creates multiple messages.

Suppose:

```json
{
  "orderId": 100,
  "items": [
    {"sku": "A"},
    {"sku": "B"},
    {"sku": "C"}
  ]
}
```

Splitter produces:

```text
Order
 │
 ├── Item A
 ├── Item B
 └── Item C
```

Java:

```java
class OrderSplitter {

    List<OrderItemMessage> split(Order order) {

        return order.items()
                .stream()
                .map(item ->
                        new OrderItemMessage(
                                order.id(),
                                item
                        )
                )
                .toList();
    }
}
```

Remember:

> **Splitter = one message → many messages**

---

# 12. Aggregator

Aggregator does the opposite.

It takes multiple related messages and produces one combined result.

```text
PaymentCompleted ──┐
InventoryReserved ─┼──→ Aggregator ──→ OrderCompleted
ShippingCreated ───┘
```

For example:

```java
class OrderAggregator {

    private final Set<String> completedSteps =
            new HashSet<>();

    void handle(String step) {

        completedSteps.add(step);

        if (completedSteps.containsAll(
                Set.of(
                    "PAYMENT",
                    "INVENTORY",
                    "SHIPPING"
                ))) {

            publishOrderCompleted();
        }
    }
}
```

This is especially useful when implementing workflows such as distributed transactions or **Saga-like processes**.

Remember:

> **Aggregator = many messages → one message**

---

# 13. Content Enricher

Sometimes a message doesn't contain enough information.

Example:

```json
{
  "orderId": 123,
  "customerId": 456
}
```

We need customer information.

The Content Enricher retrieves additional data and adds it:

```text
Original Message
       │
       ▼
Content Enricher
       │
       ├── Customer Service
       └── Address Service
       │
       ▼
Enriched Message
```

Java:

```java
class CustomerEnricher {

    CustomerOrder enrich(OrderMessage message) {

        Customer customer =
                customerService.getCustomer(
                        message.customerId()
                );

        return new CustomerOrder(
                message.orderId(),
                customer.id(),
                customer.name(),
                customer.address()
        );
    }
}
```

Remember:

> **Content Enricher = retrieve additional information and add it to the message.**

---

# 14. Normalizer

Suppose we integrate with three external systems:

```text
Bank A → XML
Bank B → JSON
Bank C → CSV
```

Our application doesn't want to understand all three formats.

So:

```text
Bank A ── XML ──┐
Bank B ── JSON ──┼──→ Normalizer ──→ Canonical Model
Bank C ── CSV ──┘
```

Canonical model:

```java
public record Customer(
        String id,
        String name,
        String email
) {}
```

Bank A:

```java
Customer normalize(BankAXml xml) {

    return new Customer(
            xml.customerNumber(),
            xml.fullName(),
            xml.emailAddress()
    );
}
```

Bank B:

```java
Customer normalize(BankBJson json) {

    return new Customer(
            json.id(),
            json.name(),
            json.email()
    );
}
```

Now the rest of the application only understands:

```java
Customer
```

instead of:

```text
BankAXml
BankBJson
BankCCsv
```

This is the **Canonical Data Model** idea.

---

# Final Exam Cheat Sheet

Memorize these:

| Pattern                  | Think                                                |
| ------------------------ | ---------------------------------------------------- |
| **Command**              | "Do this."                                           |
| **Document**             | "Here is data."                                      |
| **Event**                | "This happened."                                     |
| **Request-Reply**        | "Request → Response"                                 |
| **Correlation ID**       | "Which request does this belong to?"                 |
| **Return Address**       | "Where should the reply go?"                         |
| **Message Sequence**     | "Split a large message into ordered parts."          |
| **Message Expiration**   | "Don't process it after its lifetime."               |
| **Format Indicator**     | "What format/version is this?"                       |
| **Content-Based Router** | "Where should this message go based on its content?" |
| **Splitter**             | "One → many"                                         |
| **Aggregator**           | "Many → one"                                         |
| **Content Enricher**     | "Add missing information."                           |
| **Normalizer**           | "Different formats → one canonical format."          |
