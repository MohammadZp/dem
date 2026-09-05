# Apache MINA Cheat Sheet

## 1. The MINA mental model ⭐

Memorize this first:

```text
                    Apache MINA

Client
  │
  │ TCP
  ▼
IoAcceptor / IoConnector
  │
  ▼
IoSession
  │
  ▼
IoFilterChain
  │
  ├── Logging
  ├── Authentication
  ├── Protocol Codec
  └── Other Filters
  │
  ▼
IoHandler
  │
  ▼
Your Business Logic
```

The basic chain is:

```text
IoService
    ↓
IoSession
    ↓
IoFilterChain
    ↓
Codec
    ↓
IoHandler
```

---

# 2. Core classes

| MINA class            | Think of it as                |
| --------------------- | ----------------------------- |
| `IoAcceptor`          | TCP server                    |
| `IoConnector`         | TCP client                    |
| `IoSession`           | One TCP connection            |
| `IoHandler`           | Your event/business handler   |
| `IoHandlerAdapter`    | Convenient handler base class |
| `IoFilter`            | Middleware/interceptor        |
| `IoFilterChain`       | Pipeline of filters           |
| `ProtocolCodecFilter` | Encoder + decoder             |
| `ProtocolEncoder`     | Java object → bytes           |
| `ProtocolDecoder`     | bytes → Java object           |
| `ConnectFuture`       | Async connection result       |
| `WriteFuture`         | Async write result            |

---

# 3. Server

The basic MINA server:

```java
NioSocketAcceptor acceptor = new NioSocketAcceptor();

acceptor.setHandler(new MyHandler());

acceptor.bind(
    new InetSocketAddress(8080)
);
```

Mental model:

```text
NioSocketAcceptor
       │
       ├── listens on port
       │
       ├── accepts connections
       │
       └── creates IoSession
```

---

# 4. Client

Client:

```java
NioSocketConnector connector =
    new NioSocketConnector();

connector.setHandler(new MyHandler());

ConnectFuture future =
    connector.connect(
        new InetSocketAddress("localhost", 8080)
    );

future.awaitUninterruptibly();

IoSession session =
    future.getSession();
```

Then:

```java
session.write(message);
```

---

# 5. IoSession ⭐⭐⭐

**One `IoSession` = one network connection.**

```text
Client A ──────── Server
                    │
                 Session A

Client B ──────── Server
                    │
                 Session B
```

Useful methods:

```java
session.write(message);
```

Send data.

```java
session.closeNow();
```

Close connection.

```java
session.getRemoteAddress();
```

Get remote address.

```java
session.getLocalAddress();
```

Get local address.

```java
session.isConnected();
```

Check connection.

---

# 6. Session attributes

Store connection-specific state:

```java
session.setAttribute(
    "authenticated",
    true
);
```

Retrieve:

```java
Boolean authenticated =
    (Boolean) session.getAttribute("authenticated");
```

Remove:

```java
session.removeAttribute("authenticated");
```

Think:

```text
IoSession
 ├── connection
 ├── remote address
 ├── local address
 └── attributes
       ├── authenticated
       ├── user
       └── state
```

Useful for protocol state machines.

---

# 7. IoHandler ⭐⭐⭐

Your handler reacts to events.

Most important methods:

```java
public class MyHandler
        extends IoHandlerAdapter {

    @Override
    public void sessionCreated(IoSession session) {
    }

    @Override
    public void sessionOpened(IoSession session) {
    }

    @Override
    public void messageReceived(
            IoSession session,
            Object message) {
    }

    @Override
    public void messageSent(
            IoSession session,
            Object message) {
    }

    @Override
    public void sessionClosed(IoSession session) {
    }

    @Override
    public void exceptionCaught(
            IoSession session,
            Throwable cause) {
    }
}
```

---

# 8. Handler lifecycle ⭐

Remember:

```text
Connection
    ↓
sessionCreated()
    ↓
sessionOpened()
    ↓
messageReceived()
    ↓
messageSent()
    ↓
sessionClosed()
```

Errors can trigger:

```text
exceptionCaught()
```

---

# 9. The most important handler method

Usually:

```java
@Override
public void messageReceived(
        IoSession session,
        Object message) {

    System.out.println(
        "Received: " + message
    );

    session.write("ACK");
}
```

Flow:

```text
TCP bytes
    ↓
Decoder
    ↓
Java Object
    ↓
messageReceived()
    ↓
Business logic
```

---

# 10. IoFilterChain ⭐⭐⭐

Think of filters as middleware.

```java
acceptor.getFilterChain().addLast(
    "logger",
    new LoggingFilter()
);
```

Multiple filters:

```text
Incoming data
     ↓
┌──────────────┐
│ Logging      │
├──────────────┤
│ Security     │
├──────────────┤
│ Codec        │
├──────────────┤
│ Other Filter │
└──────┬───────┘
       ↓
    Handler
```

Common uses:

* logging
* authentication
* encryption
* compression
* encoding/decoding
* metrics
* throttling

---

# 11. ProtocolCodecFilter ⭐⭐⭐

One of the most important pieces.

```java
filterChain.addLast(
    "codec",
    new ProtocolCodecFilter(
        encoder,
        decoder
    )
);
```

It connects your network bytes with Java objects.

```text
            ProtocolCodec

Bytes ───────→ Decoder ───────→ Java Object

Bytes ←────── Encoder ←──────── Java Object
```

---

# 12. Decoder

The decoder handles:

```text
Network bytes
       ↓
Java object
```

Conceptually:

```java
public class MyDecoder
        extends CumulativeProtocolDecoder {

    @Override
    protected boolean doDecode(
            IoSession session,
            IoBuffer in,
            ProtocolDecoderOutput out)
            throws Exception {

        // read bytes
        // build message
        // out.write(message)

        return true;
    }
}
```

The exact decoder base class depends on your protocol.

---

# 13. Encoder

Opposite direction:

```text
Java object
      ↓
Encoder
      ↓
Bytes
      ↓
TCP
```

Conceptually:

```java
public class MyEncoder
        implements ProtocolEncoder {

    @Override
    public void encode(
            IoSession session,
            Object message,
            ProtocolEncoderOutput out)
            throws Exception {

        // convert object → bytes
    }
}
```

---

# 14. TCP does NOT have messages ⭐⭐⭐⭐⭐

This is critical.

TCP is a **byte stream**.

If client sends:

```text
HELLO
WORLD
```

you cannot assume MINA receives:

```text
"HELLO"
"WORLD"
```

It might receive:

```text
"HELLOWORLD"
```

or:

```text
"HEL"
"LOWORLD"
```

or:

```text
"HELLOW"
"ORLD"
```

Therefore:

> **Your protocol must define message framing.**

---

# 15. Common framing strategies

### Fixed length

```text
[10 bytes]
[10 bytes]
[10 bytes]
```

### Length prefix

```text
[5][HELLO]
[5][WORLD]
```

### Delimiter

```text
HELLO\n
WORLD\n
```

### Header + body

Common in enterprise/banking protocols:

```text
┌──────────┬───────────────┐
│ Header   │ Body          │
└──────────┴───────────────┘
```

For example:

```text
[Message Type]
[Length]
[Transaction ID]
[Body]
```

---

# 16. `IoBuffer`

MINA's buffer abstraction:

```java
IoBuffer buffer =
    IoBuffer.allocate(1024);
```

Write:

```java
buffer.putInt(100);
buffer.putString("HELLO", ...);
```

Prepare for reading:

```java
buffer.flip();
```

Read:

```java
int value = buffer.getInt();
```

Basic lifecycle:

```text
allocate
   ↓
put()
   ↓
flip()
   ↓
get()
```

---

# 17. Async communication

MINA is asynchronous.

For example:

```java
session.write(message);
```

returns a `WriteFuture`.

You can use:

```java
WriteFuture future =
    session.write(message);

future.awaitUninterruptibly();
```

But don't blindly block in event-processing code.

The important mental model is:

```text
Your code
   │
   ├── submit write
   │
   ▼
MINA
   │
   ▼
Network
```

---

# 18. Acceptor vs Connector

Very easy:

```text
SERVER
NioSocketAcceptor
      ↓
accept connections


CLIENT
NioSocketConnector
      ↓
create connections
```

Remember:

```text
Accept = Server
Connect = Client
```

---

# 19. Handler vs Filter

This distinction is important.

### Filter

Cross-cutting/network processing:

```text
Logging
Authentication
Codec
Compression
Encryption
Metrics
```

### Handler

Application-level processing:

```text
"Process transaction"
"Validate request"
"Call service"
"Create response"
```

Think:

```text
Filter → infrastructure concerns

Handler → application concerns
```

---

# 20. A complete simple architecture

```text
                    CLIENT
                       │
                       │ TCP
                       ▼
              ┌─────────────────┐
              │  IoAcceptor     │
              └────────┬────────┘
                       │
                       ▼
              ┌─────────────────┐
              │   IoSession     │
              └────────┬────────┘
                       │
                       ▼
              ┌─────────────────┐
              │  IoFilterChain  │
              │                 │
              │ Logging         │
              │ Security        │
              │ Codec           │
              └────────┬────────┘
                       │
                       ▼
              ┌─────────────────┐
              │    Decoder      │
              └────────┬────────┘
                       │
                       ▼
              ┌─────────────────┐
              │    Handler      │
              └────────┬────────┘
                       │
                       ▼
               Business Service
                       │
                       ▼
              ┌─────────────────┐
              │    Encoder      │
              └────────┬────────┘
                       │
                       ▼
                     TCP
```

---

# 21. MINA vs normal Java Socket

Without MINA:

```java
Socket socket = ...;

InputStream input =
    socket.getInputStream();

OutputStream output =
    socket.getOutputStream();
```

You have to manage:

```text
threads
connections
reading
writing
buffers
framing
timeouts
errors
```

With MINA:

```text
MINA
 ├── connection management
 ├── event handling
 ├── async I/O
 ├── filters
 ├── codecs
 └── sessions
```

Your code focuses more on the protocol/application.

---

# 22. The MINA "recipe"

For a typical server:

```java
NioSocketAcceptor acceptor =
    new NioSocketAcceptor();

acceptor.getFilterChain().addLast(
    "codec",
    new ProtocolCodecFilter(
        encoder,
        decoder
    )
);

acceptor.setHandler(
    new MyHandler()
);

acceptor.bind(
    new InetSocketAddress(PORT)
);
```

That's the skeleton you should recognize immediately.

---

# 23. Debugging checklist 🐛

When a MINA application isn't working:

### Connection problem?

Check:

```text
IoAcceptor
port
bind()
firewall
client address
```

### Message isn't reaching handler?

Check:

```text
IoFilterChain
ProtocolCodecFilter
Decoder
message framing
```

### Message is corrupted?

Check:

```text
Encoder
Decoder
byte order
encoding
message length
```

### Connection unexpectedly closes?

Check:

```text
exceptionCaught()
sessionClosed()
timeouts
protocol errors
```

### Messages are split/combined?

**Think TCP framing first.**

---

# 24. The 10 things to memorize

If you forget everything else, remember these:

```text
1. IoAcceptor      = Server

2. IoConnector     = Client

3. IoSession       = Connection

4. IoHandler       = Application event handler

5. IoFilter        = Middleware

6. IoFilterChain   = Middleware pipeline

7. Encoder         = Object → Bytes

8. Decoder         = Bytes → Object

9. TCP             = Byte stream, NOT messages

10. Session        = Connection-specific state
```

And this diagram:

```text
       SERVER
         │
    IoAcceptor
         │
      Session
         │
    FilterChain
         │
       Codec
         │
      Handler
         │
   Business Logic
```

