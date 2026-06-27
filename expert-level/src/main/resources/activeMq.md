# ActiveMQ Basics (Messaging)

---

# What is ActiveMQ?

**ActiveMQ** is an **open-source Message Broker** that allows applications to communicate by sending and receiving messages.

Instead of applications communicating directly, they communicate through ActiveMQ.

```
Application A
       │
       ▼
   ActiveMQ Broker
       │
       ▼
Application B
```

---

# Why Use ActiveMQ?

ActiveMQ provides:

* Asynchronous communication
* Loose coupling between applications
* Reliable message delivery
* Scalability

Applications do not need to know whether the other application is currently running.

---

# What is a Message Broker?

A **Message Broker** is software that receives, stores, and forwards messages between applications.

Responsibilities:

* Receive messages
* Store messages
* Deliver messages to consumers
* Ensure reliable communication

Examples:

* ActiveMQ
* RabbitMQ
* Apache Kafka

---

# Messaging Concepts

## Producer

A **Producer** sends messages.

```
Producer
    │
    ▼
 Message
```

Example:

A Spring Boot application sends an order message.

---

## Consumer

A **Consumer** receives messages.

```
Message
    │
    ▼
Consumer
```

Example:

An inventory service receives the order message.

---

## Broker

The **Broker** sits between producers and consumers.

```
Producer
    │
    ▼
 ActiveMQ
    │
    ▼
Consumer
```

The producer and consumer never communicate directly.

---

# Queue (Point-to-Point)

## What is a Queue?

A **Queue** is used for **one-to-one messaging**.

One producer sends a message.

One consumer receives that message.

```
Producer
     │
     ▼
   Queue
     │
     ▼
Consumer
```

If multiple consumers exist:

```
Consumer A
Consumer B
Consumer C
```

Only **one** consumer receives each message.

---

## Characteristics of a Queue

* One producer → one consumer
* Each message is consumed only once
* Used for task processing

Example:

```
Order Processing
Payment Processing
Email Sending
```

---

# Topic (Publish/Subscribe)

## What is a Topic?

A **Topic** is used for **one-to-many messaging**.

One producer publishes a message.

Every subscribed consumer receives a copy.

```
           Consumer A

Producer ──► Topic ──► Consumer B

           Consumer C
```

All subscribers receive the same message.

---

## Characteristics of a Topic

* One producer
* Many consumers
* Every subscriber receives the message

Example:

```
Stock Price Updates

Producer

↓

Topic

↓

Trader A
Trader B
Trader C
```

---

# Queue vs Topic

| Queue                              | Topic                               |
| ---------------------------------- | ----------------------------------- |
| One-to-one                         | One-to-many                         |
| One consumer receives each message | All subscribers receive the message |
| Point-to-Point model               | Publish/Subscribe model             |
| Used for tasks                     | Used for notifications/events       |

---

# Synchronous vs Asynchronous Communication

## Synchronous

The sender waits for a response.

```
Application A
      │
      ▼
Application B
      ▲
      │
Wait
```

Example:

HTTP request.

---

## Asynchronous

The sender sends the message and continues working.

```
Application A
      │
      ▼
 ActiveMQ
      │
      ▼
Application B
```

The producer does **not** wait.

---

# Message Lifecycle

```
Producer
     │
     ▼
Send Message
     │
     ▼
ActiveMQ Broker
     │
     ▼
Queue / Topic
     │
     ▼
Consumer
```

---

# Advantages of ActiveMQ

* Loose coupling
* Reliable messaging
* Asynchronous communication
* Improved scalability
* Better fault tolerance

---

# JMS (Java Message Service)

## What is JMS?

**JMS (Java Message Service)** is a Java API for sending and receiving messages.

ActiveMQ is an implementation of JMS.

Relationship:

```
JMS (API)

        ▲

Implemented by

        │

ActiveMQ
```

---

# High-Priority Exam Questions

## What is ActiveMQ?

ActiveMQ is an open-source message broker that enables asynchronous communication between applications.

---

## What is a Message Broker?

A message broker receives, stores, and forwards messages between applications.

---

## What is a Producer?

A producer sends messages to the broker.

---

## What is a Consumer?

A consumer receives messages from the broker.

---

## What is the difference between a Queue and a Topic?

| Queue                             | Topic                               |
| --------------------------------- | ----------------------------------- |
| One consumer receives the message | All subscribers receive the message |
| Point-to-Point                    | Publish/Subscribe                   |

---

## What is asynchronous communication?

The sender sends a message without waiting for the receiver to process it.

---

## What are the advantages of ActiveMQ?

* Reliable messaging
* Loose coupling
* Scalability
* Asynchronous communication

---

## What is JMS?

JMS (Java Message Service) is the Java API used for messaging. ActiveMQ is one implementation of JMS.

---

# Memory Tips

## ActiveMQ Components

```
Producer
     │
     ▼
ActiveMQ Broker
     │
     ▼
Consumer
```

---

## Queue

```
One Message
      │
      ▼
One Consumer
```

Think:

**Queue = One receiver**

---

## Topic

```
One Message
      │
      ▼
Many Consumers
```

Think:

**Topic = Broadcast**

---

## Queue vs Topic

```
Queue
Producer → Queue → One Consumer

Topic
Producer → Topic → All Subscribers
```

---

## Easy Trick

* **Producer** → Sends messages.
* **Broker** → Stores and forwards messages.
* **Consumer** → Receives messages.
* **Queue** → One receiver.
* **Topic** → Everyone receives.
* **JMS** → Java messaging API.
* **ActiveMQ** → JMS implementation.
