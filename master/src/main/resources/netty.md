---

# Netty Fundamentals — Ch. 1 & 2

## 0. First: What problem is Netty solving?

Suppose you want to build a TCP server.

Without Netty, you can use Java's socket API:

```java
ServerSocket serverSocket = new ServerSocket(8080);

while (true) {
    Socket socket = serverSocket.accept();

    new Thread(() -> {
        // read from socket
        // process request
        // write response
    }).start();
}
```

This works.

But once you have:

* thousands of connections
* many concurrent clients
* non-blocking I/O
* connection management
* message framing
* encoding/decoding
* event handling
* backpressure
* efficient thread usage

things become much harder.

**Netty provides the networking infrastructure so you don't have to build it yourself.**

Think of it as:

```text
Your Application
       ↓
   Netty Pipeline
       ↓
    Netty I/O
       ↓
   Java NIO / OS
       ↓
      TCP
```

---

# Chapter 1 — Fundamentals

## 1. Blocking vs Non-blocking I/O

This is probably the most important concept.

### Traditional blocking model

Imagine:

```java
Socket socket = serverSocket.accept();

InputStream in = socket.getInputStream();

int data = in.read();
```

`read()` can **block**.

Meaning:

```text
Thread
  │
  ├── read()
  │
  │   waiting.........
  │   waiting.........
  │
  └── data arrives
```

If you have 10,000 connections, a naive architecture could require a huge number of threads.

---

## 2. Non-blocking I/O

Java NIO introduced mechanisms such as:

```text
Channel
Buffer
Selector
```

Instead of:

```text
one connection → one blocked thread
```

you can have:

```text
                    ┌─ connection 1
                    ├─ connection 2
EventLoop ──────────┼─ connection 3
                    ├─ connection 4
                    └─ connection 5000
```

The event loop waits for I/O events.

For example:

```text
EventLoop
    ↓
"Connection 17 has data"
    ↓
read it
    ↓
process it
    ↓
"Connection 52 is writable"
    ↓
write it
```

This is the fundamental idea behind Netty.

---

# 3. Netty's Core Architecture

You should have this picture in your head:

```text
                    NETTY SERVER

                      Server
                        │
                     Bootstrap
                        │
                ┌───────┴───────┐
                │               │
          Boss EventLoop    Worker EventLoop
                │               │
          accepts clients   handles I/O
                                │
                         ┌──────┼──────┐
                         ↓      ↓      ↓
                       Conn1  Conn2  Conn3
                         │      │      │
                         ↓      ↓      ↓
                      Pipeline Pipeline Pipeline
                         │
                         ↓
                    ChannelHandlers
```

There are several concepts here that you must understand.

---

# 4. Channel

A **Channel** represents a network connection.

For example:

```text
Client
   │
   │ TCP
   │
   ▼
Channel
```

You can think of it roughly as Netty's abstraction around a socket connection.

Example:

```java
Channel channel
```

You can use it to:

```java
channel.writeAndFlush(message);
```

or:

```java
channel.close();
```

So:

> **Channel = connection**

---

# 5. EventLoop

This is one of Netty's most important concepts.

An `EventLoop` handles I/O events for channels.

Conceptually:

```text
EventLoop
    │
    ├── Channel A
    ├── Channel B
    ├── Channel C
    └── Channel D
```

One EventLoop can handle many channels.

The EventLoop essentially does:

```text
while (running) {

    wait for I/O events

    for each event:
        process event
}
```

This is why Netty can handle huge numbers of connections without creating one thread per connection.

---

# 6. EventLoopGroup

An `EventLoopGroup` manages multiple EventLoops.

```text
EventLoopGroup
       │
       ├── EventLoop 1
       ├── EventLoop 2
       ├── EventLoop 3
       └── EventLoop 4
```

A typical Netty server has two groups:

```text
BossGroup
    ↓
accept connections

WorkerGroup
    ↓
handle I/O
```

Usually:

```java
EventLoopGroup bossGroup = new NioEventLoopGroup(1);

EventLoopGroup workerGroup = new NioEventLoopGroup();
```

Conceptually:

```text
             ServerSocket
                  │
                  ▼
             BossGroup
                  │
            accept connection
                  │
                  ▼
             WorkerGroup
          ┌───────┼────────┐
          ↓       ↓        ↓
       Channel  Channel  Channel
```

---

# 7. Why two EventLoopGroups?

This is an excellent interview question.

The boss should primarily do:

```text
accept()
```

The workers do:

```text
read
decode
process
write
```

So you don't want a busy worker processing application traffic to prevent the server from accepting new connections.

Think:

```text
Boss
 ↓
"Hey, someone wants to connect!"

Worker
 ↓
"Okay, I'll handle that connection."
```

---

# 8. ChannelPipeline

Now we get to one of Netty's most powerful concepts.

Every Channel has a **pipeline**.

Imagine:

```text
Incoming TCP data
       ↓
┌──────────────────┐
│ ChannelPipeline  │
│                  │
│ Decoder          │
│      ↓           │
│ Authentication   │
│      ↓           │
│ Business Logic   │
│      ↓           │
│ Encoder          │
└──────────────────┘
       ↓
 Application
```

A pipeline is basically a chain of handlers.

For example:

```java
pipeline.addLast(new MyDecoder());
pipeline.addLast(new MyBusinessHandler());
pipeline.addLast(new MyEncoder());
```

Data travels through these handlers.

---

# 9. ChannelHandler

A `ChannelHandler` processes events/data.

For example:

```java
public class MyHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(
            ChannelHandlerContext ctx,
            Object msg) {

        System.out.println(msg);
    }
}
```

This is an **inbound handler**.

Inbound means:

```text
NETWORK
   ↓
Netty
   ↓
Handler
   ↓
Your application
```

Outbound is the opposite:

```text
Your application
   ↓
Handler
   ↓
Netty
   ↓
NETWORK
```

---

# 10. ChannelHandlerContext

You'll see this everywhere:

```java
ChannelHandlerContext ctx
```

It provides access to the handler's context.

For example:

```java
ctx.writeAndFlush("hello");
```

and:

```java
ctx.close();
```

You can think of it as:

> "The object through which my handler interacts with the Netty pipeline/channel."

---

# 11. ByteBuf

This is another **critical Netty concept**.

Netty doesn't primarily use Java's:

```java
byte[]
```

It uses:

```java
ByteBuf
```

Example:

```java
ByteBuf buffer = Unpooled.buffer();

buffer.writeInt(10);
buffer.writeBytes("hello".getBytes());
```

And:

```java
int value = buffer.readInt();
```

Why?

Because Netty needs a highly optimized buffer abstraction suitable for network I/O.

You'll eventually learn:

* reader index
* writer index
* capacity
* direct buffers
* heap buffers
* reference counting

For now:

> **ByteBuf = Netty's main byte buffer abstraction.**

---

# 12. Important: TCP does NOT understand messages

This is extremely important when building Netty applications.

Suppose client sends:

```text
HELLO
```

You might imagine:

```text
read() → "HELLO"
```

But TCP is a **byte stream**.

You might actually receive:

```text
HE
LLO
```

or:

```text
HELLO
WORLD
```

or:

```text
HEL
LOWORLD
```

Therefore Netty applications often need:

```text
TCP bytes
    ↓
Framing
    ↓
Message
    ↓
Business logic
```

That's why Netty has decoders such as:

```java
LineBasedFrameDecoder
LengthFieldBasedFrameDecoder
DelimiterBasedFrameDecoder
```

We'll spend significant time on this later.

---

# Chapter 2 — Bootstrapping a Server

Now let's build a server.

---

# 13. Bootstrap

A `Bootstrap` configures a Netty client.

A `ServerBootstrap` configures a Netty server.

For a server:

```java
ServerBootstrap bootstrap = new ServerBootstrap();
```

Think:

```text
ServerBootstrap
      ↓
configure Netty server
      ↓
bind port
      ↓
server starts
```

---

# 14. Minimal Netty Server

Here's the basic structure:

```java
EventLoopGroup bossGroup =
        new NioEventLoopGroup(1);

EventLoopGroup workerGroup =
        new NioEventLoopGroup();

try {

    ServerBootstrap bootstrap = new ServerBootstrap();

    bootstrap
        .group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) {

                ch.pipeline().addLast(
                    new MyHandler()
                );
            }
        });

    Channel channel = bootstrap
        .bind(8080)
        .sync()
        .channel();

    channel.closeFuture().sync();

} finally {

    bossGroup.shutdownGracefully();
    workerGroup.shutdownGracefully();
}
```

Don't memorize this yet.

Understand what each part means.

---

# 15. `.group()`

```java
bootstrap.group(bossGroup, workerGroup);
```

This tells Netty:

```text
BossGroup   → accept connections

WorkerGroup → handle connections
```

---

# 16. `.channel()`

```java
.channel(NioServerSocketChannel.class)
```

This tells Netty which Channel implementation to use.

For Java NIO:

```java
NioServerSocketChannel
```

You can think:

```text
ServerBootstrap
       ↓
"What networking implementation should I use?"
       ↓
NioServerSocketChannel
       ↓
Java NIO
```

Later you'll encounter:

```text
Nio
Epoll
KQueue
Unix Domain Socket
```

depending on OS and requirements.

---

# 17. `.childHandler()`

This one is confusing initially.

```java
.childHandler(...)
```

means:

> "When a client connects, how should its Channel be configured?"

For example:

```java
.childHandler(new ChannelInitializer<SocketChannel>() {

    @Override
    protected void initChannel(SocketChannel ch) {

        ch.pipeline().addLast(
            new MyHandler()
        );
    }
});
```

Remember:

```text
Server Channel
       │
       │ accepts
       ▼
Client Channel
       │
       ├── Decoder
       ├── Handler
       └── Encoder
```

`childHandler()` configures those client Channels.

---

# 18. ChannelInitializer

You'll see this constantly in Netty.

```java
new ChannelInitializer<SocketChannel>() {

    @Override
    protected void initChannel(SocketChannel ch) {

        ch.pipeline().addLast(...);
    }
}
```

Its job is basically:

```text
New connection
      ↓
Initialize its pipeline
      ↓
Add handlers
```

For example:

```java
pipeline.addLast(new StringDecoder());
pipeline.addLast(new StringEncoder());
pipeline.addLast(new ChatHandler());
```

---

# 19. `bind()`

This:

```java
bootstrap.bind(8080)
```

means:

```text
Bind server to TCP port 8080
```

Similar conceptually to:

```java
new ServerSocket(8080);
```

---

# 20. `.sync()`

You'll see:

```java
bootstrap.bind(8080).sync();
```

The operation is asynchronous.

`.sync()` waits for that operation to complete.

Conceptually:

```text
bind()
  ↓
asynchronous operation
  ↓
sync()
  ↓
wait until complete
```

---

# 21. `closeFuture().sync()`

This line confuses almost everyone initially:

```java
channel.closeFuture().sync();
```

It means:

> Keep the main server thread waiting until the server Channel is closed.

Without it:

```text
start server
   ↓
main() reaches end
   ↓
program may terminate
```

With it:

```text
start server
   ↓
wait...
   ↓
wait...
   ↓
server closed
   ↓
continue
   ↓
shutdown groups
```

---

# 22. The complete lifecycle

This is the most important diagram from these chapters:

```text
                    ServerBootstrap
                           │
                           ▼
                    configure server
                           │
             ┌─────────────┴─────────────┐
             │                           │
        BossGroup                   WorkerGroup
             │                           │
             │                       EventLoops
             │                           │
        accept connection                │
             │                           │
             └──────────────┬────────────┘
                            │
                            ▼
                       Client Channel
                            │
                            ▼
                     ChannelPipeline
                            │
                 ┌──────────┼──────────┐
                 ↓          ↓          ↓
              Decoder    Handler    Encoder
                 │          │          │
                 └──────────┼──────────┘
                            │
                            ▼
                     Business Logic
```

If you understand this diagram, you understand the foundation of Netty.

---

# 23. Let's make a real Echo Server

An Echo server returns whatever the client sends.

Client:

```text
Hello
```

Server:

```text
Hello
```

Handler:

```java
public class EchoHandler
        extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(
            ChannelHandlerContext ctx,
            Object msg) {

        System.out.println("Received: " + msg);

        ctx.writeAndFlush(msg);
    }
}
```

And pipeline:

```java
ch.pipeline().addLast(
    new EchoHandler()
);
```

So:

```text
Client
  │
  │ "Hello"
  ▼
TCP
  │
  ▼
Netty
  │
  ▼
EchoHandler
  │
  │ ctx.writeAndFlush(msg)
  ▼
TCP
  │
  ▼
Client
```

---

# 24. What happens when a client connects?

Let's trace it.

### Step 1

Client connects:

```text
Client ──────── TCP ────────> Server
```

### Step 2

Boss EventLoop receives the connection event.

```text
Boss EventLoop
      ↓
accept()
```

### Step 3

Netty creates a Channel for that client.

```text
Client
  ↓
Channel
```

### Step 4

The Channel is registered with a worker EventLoop.

```text
Worker EventLoop
       ↓
   Client Channel
```

### Step 5

`ChannelInitializer` configures the pipeline.

```text
Channel
   ↓
Pipeline
   ├── Decoder
   ├── Business Handler
   └── Encoder
```

### Step 6

Client sends bytes.

```text
Client
  ↓
TCP
  ↓
ByteBuf
  ↓
Pipeline
  ↓
Decoder
  ↓
Handler
```

### Step 7

Handler responds.

```java
ctx.writeAndFlush(response);
```

---

# 25. One subtle but VERY important rule

Netty's EventLoop is generally **single-threaded per EventLoop**.

Meaning if:

```text
EventLoop 1
   │
   ├── Channel A
   ├── Channel B
   └── Channel C
```

the handlers for these channels are normally executed by the same EventLoop thread.

This gives you an important property:

> You often don't need synchronization for state that is confined to one EventLoop.

But it also creates a huge rule:

### DON'T BLOCK THE EVENT LOOP

Bad:

```java
@Override
public void channelRead(...) {

    database.query(); // potentially blocking

}
```

or:

```java
Thread.sleep(5000);
```

Because you could block the EventLoop and therefore affect **many connections**.

Instead, expensive/blocking work should generally be moved to another executor/thread pool.

We'll go deep into this when we cover Netty concurrency.

---

# 26. Netty vs Apache MINA

Since you just studied MINA, this comparison will help.

| Concept          | MINA                   | Netty           |
| ---------------- | ---------------------- | --------------- |
| Connection       | IoSession              | Channel         |
| Event loop       | IoProcessor            | EventLoop       |
| Group            | IoAcceptor/IoProcessor | EventLoopGroup  |
| Processing chain | Filter chain           | ChannelPipeline |
| Handler          | IoHandler              | ChannelHandler  |
| Buffer           | IoBuffer               | ByteBuf         |
| Server bootstrap | IoAcceptor             | ServerBootstrap |
| Architecture     | Event-driven           | Event-driven    |

The mental model is remarkably similar.

The biggest thing to learn with Netty is its specific abstractions and its very powerful pipeline/buffer/concurrency model.

---

# 27. What you should memorize from Ch 1 & 2

Don't memorize the entire server code.

Memorize this:

```text
ServerBootstrap
      │
      ├── Boss EventLoopGroup
      │       └── accepts connections
      │
      ├── Worker EventLoopGroup
      │       └── handles I/O
      │
      ├── Channel
      │       └── represents connection
      │
      └── ChannelPipeline
              │
              ├── ChannelInboundHandler
              ├── ChannelInboundHandler
              └── ChannelOutboundHandler
```

And:

```text
Client
  ↓
Channel
  ↓
Pipeline
  ↓
Decoder
  ↓
Business Handler
  ↓
Encoder
  ↓
Channel
  ↓
Client
```

---

# 28. Your mental model

If I asked you tomorrow:

> "Explain Netty to me."

A good answer would be:

> **Netty is an asynchronous, event-driven networking framework. A server uses EventLoopGroups to manage I/O. A Boss EventLoop accepts new connections, while Worker EventLoops handle I/O for established Channels. Each Channel has a ChannelPipeline containing ChannelHandlers that process inbound and outbound events. Netty uses ByteBuf for efficient network buffering.**

That's the **Ch 1–2 level answer**.

---

# 29. What comes next

The natural learning path from here is:

```text
Ch 1-2
Fundamentals
   ↓
Ch 3
ByteBuf
   ↓
Ch 4
ChannelHandler
   ↓
Ch 5
ChannelPipeline
   ↓
Ch 6
Concurrency
   ↓
Ch 7
EventLoop
   ↓
Ch 8
Bootstrap
   ↓
Ch 9
Unit Testing
```

But before moving on, I strongly recommend you **write the Echo Server yourself** rather than just reading it.

The key exercise is to understand this flow:

```text
ServerBootstrap
      ↓
EventLoopGroups
      ↓
Channel
      ↓
ChannelInitializer
      ↓
ChannelPipeline
      ↓
Handler
      ↓
ByteBuf
      ↓
Client
```