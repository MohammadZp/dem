# Java References — Exam Cheat Sheet

## 1. The Core Idea

A Java variable usually holds a **reference to an object**, not the object itself.

```java
User user = new User();
```

Think:

```text
user ─────────→ User object
 reference        object
```

### The most important rule

> **GC cares about reachability, not whether you "still use" the object.**

If an object is reachable from a **GC Root** through strong references, it cannot be garbage collected.

---

# 2. Strong Reference

Normal Java reference:

```java
User user = new User();
```

```text
GC Root
   │
   ▼
 user ─────→ User
```

As long as `user` is reachable, the `User` object is alive.

Most Java references are strong references.

### Common examples

```java
Object obj = new Object();

List<Object> list = new ArrayList<>();
list.add(obj);

Map<String, Object> map = new HashMap<>();
map.put("user", obj);
```

All of these strongly reference `obj`.

---

# 3. Strong Reference → Memory Leak

Classic example:

```java
class Cache {
    private final Map<String, Object> cache = new HashMap<>();

    public void put(String key, Object value) {
        cache.put(key, value);
    }
}
```

If you continuously do:

```java
cache.put("key1", hugeObject);
cache.put("key2", hugeObject);
cache.put("key3", hugeObject);
...
```

and never remove entries:

```text
GC Root
   ↓
Cache
   ↓
HashMap
   ↓
Object
Object
Object
Object
...
```

The objects remain reachable.

### Important

Calling:

```java
System.gc();
```

doesn't solve this.

Why?

Because the objects are **still reachable**.

---

# 4. `null` and References

```java
User user = new User();

user = null;
```

Now:

```text
user ─X→ User
```

If there are **no other strong references**:

```text
User object
     ↑
     X
```

the object becomes **eligible for GC**.

### Important wording

Don't say:

> `user = null` destroys the object.

Say:

> It removes that strong reference, so the object may become eligible for garbage collection.

---

# 5. Multiple References

```java
User a = new User();
User b = a;
```

Both references point to the same object:

```text
a ─────┐
       ├────→ User
b ─────┘
```

Now:

```java
a = null;
```

doesn't make the object collectible:

```text
a ─X

b ─────→ User
```

Because `b` still strongly references it.

---

# 6. WeakReference

```java
WeakReference<User> ref =
        new WeakReference<>(user);
```

Conceptually:

```text
user ───────────→ User
                   ↑
                   │
              WeakReference
```

If the strong reference disappears:

```java
user = null;
```

then:

```text
WeakReference ──→ User
```

is **not enough to keep the object alive**.

The object can be collected.

After GC:

```java
ref.get();
```

may return:

```java
null
```

---

# 7. WeakReference — Key Rule

```text
Strong reference exists
        ↓
Object stays alive

Only WeakReference remains
        ↓
Object can be GC'd
```

### Memorize:

> **WeakReference does not keep the object alive.**

---

# 8. WeakReference Example

```java
User user = new User();

WeakReference<User> ref =
        new WeakReference<>(user);

System.out.println(ref.get()); // User

user = null;

// Eventually GC may collect User

System.out.println(ref.get()); // possibly null
```

### Important

Don't expect:

```java
System.gc();
```

to guarantee:

```java
ref.get() == null
```

GC is not guaranteed.

---

# 9. SoftReference

```java
SoftReference<User> ref =
        new SoftReference<>(user);
```

Soft references are intended for **memory-sensitive caching**.

Conceptually:

```text
Strong reference exists
       ↓
Object stays alive

Only SoftReference remains
       ↓
JVM may keep it

Memory pressure
       ↓
JVM may clear it
```

Access:

```java
User user = ref.get();
```

If cleared:

```java
user == null
```

---

# 10. Weak vs Soft

### WeakReference

```text
Only weak reference
       ↓
Object can be collected
```

### SoftReference

```text
Only soft reference
       ↓
JVM may retain object
       ↓
Memory pressure
       ↓
JVM may collect it
```

### Memorize

```text
Weak → don't keep alive
Soft → keep if memory allows
```

---

# 11. SoftReference Is NOT a Guaranteed Cache

Don't think:

> "SoftReference means JVM only removes it when memory is 100% full."

Wrong.

The JVM controls when soft references are cleared.

There is no simple guaranteed threshold you should rely on.

For production caches, explicit eviction policies such as:

```text
LRU
maximum size
TTL
Caffeine
```

are generally more predictable.

---

# 12. WeakHashMap

Very important.

```java
Map<Key, Value> map =
        new WeakHashMap<>();
```

`WeakHashMap` uses **weak references for its keys**.

Example:

```java
Key key = new Key();

map.put(key, value);
```

Initially:

```text
key ─────→ Key
            ↑
            │
       WeakHashMap
```

Then:

```java
key = null;
```

If there are no other strong references to the key:

```text
GC may collect Key
        ↓
WeakHashMap entry disappears
```

---

# 13. HashMap vs WeakHashMap

### HashMap

```java
Map<Key, Value> map = new HashMap<>();
```

The map strongly references its keys.

```text
HashMap
   │
   └──strong──→ Key
```

Removing your external reference:

```java
key = null;
```

doesn't make the key collectible because:

```text
HashMap ─────→ Key
```

still exists.

---

### WeakHashMap

```text
WeakHashMap
      │
      └──weak──→ Key
```

So if no external strong reference exists, the key can be collected.

---

# 14. Classic Exam Example

```java
Map<Object, String> map = new HashMap<>();

Object key = new Object();

map.put(key, "hello");

key = null;

System.gc();
```

Map entry normally remains.

Why?

```text
HashMap ─────strong────→ key
```

---

With:

```java
Map<Object, String> map = new WeakHashMap<>();
```

the entry **may disappear after GC**.

---

# 15. WeakHashMap Important Trap

`WeakHashMap` has **weak keys**, not weak values.

```java
map.put(key, value);
```

Conceptually:

```text
WeakHashMap
     │
     ├── weak → key
     │
     └── strong → value
```

So don't confuse:

```text
WeakHashMap
≠
Map with weak values
```

---

# 16. `System.gc()`

```java
System.gc();
```

means:

> JVM, please consider performing garbage collection.

It does **not** guarantee:

```text
GC happens immediately
```

and does not guarantee:

```text
specific object gets collected
```

### Exam answer

> `System.gc()` is only a request/hint to the JVM.

---

# 17. GC Roots

An object is collectible when it is no longer reachable from GC Roots through strong references.

Typical GC Roots include things such as:

```text
Live thread references
Stack/local references
Static references
JNI references
```

Simplified exam model:

```text
GC Root
   ↓
strong reference
   ↓
object
```

Object stays alive.

If the chain is broken:

```text
GC Root
   ↓
   X

object
```

the object becomes eligible for GC.

---

# 18. Reachability

This is the most important concept behind memory-leak questions.

Suppose:

```java
Object a = new Object();
Object b = a;
```

```text
GC Root
   ↓
 a ───→ Object
 b ───→ Object
```

Set:

```java
a = null;
```

Still:

```text
GC Root
   ↓
 b ───→ Object
```

Object remains reachable.

Set:

```java
b = null;
```

Now no strong reference remains.

The object becomes eligible for GC.

---

# 19. Collection Memory Leak

Classic:

```java
List<byte[]> cache = new ArrayList<>();

for (int i = 0; i < 10000; i++) {
    cache.add(new byte[1024 * 1024]);
}
```

The problem isn't necessarily the `byte[]`.

The problem is:

```text
cache
  ↓
ArrayList
  ↓
byte[]
byte[]
byte[]
...
```

The collection keeps strong references to everything.

### Exam phrase

> The collection retains strong references to objects that are no longer needed, preventing garbage collection and causing unbounded memory growth.

---

# 20. `clear()` vs `null`

```java
list.clear();
```

removes the references stored inside the list.

```text
List ─X→ Object
```

If nothing else references those objects, they become eligible for GC.

But:

```java
list = null;
```

removes your reference to the **list itself**.

Different things.

---

# 21. Removing One Element

```java
list.remove(obj);
```

means:

> Remove that reference from the list.

It does **not** destroy `obj`.

If:

```java
Object obj = ...;
```

still exists:

```text
obj ─────→ Object
```

the object remains alive.

---

# 22. Static Reference — Common Memory Leak

```java
static List<Object> cache =
        new ArrayList<>();
```

Static fields can remain reachable for a very long time.

Conceptually:

```text
GC Root
   ↓
Class
   ↓
static cache
   ↓
Object
```

Therefore, an ever-growing static collection is a classic memory-leak pattern.

---

# 23. Instance Field Can Also Cause Retention

```java
class Service {
    private List<Object> cache = new ArrayList<>();
}
```

If the `Service` itself is still reachable:

```text
GC Root
   ↓
Service
   ↓
cache
   ↓
Objects
```

those objects remain reachable.

So the issue isn't:

> "static is the only way to cause a memory leak."

The actual issue is **unwanted reachability**.

---

# 24. Reference Types — Quick Table

| Reference        | Keeps object alive? | Can GC object?             | Typical use            |
| ---------------- | ------------------- | -------------------------- | ---------------------- |
| Strong           | Yes                 | No while reachable         | Normal objects         |
| WeakReference    | No                  | Yes                        | Weak caches/listeners  |
| SoftReference    | Not necessarily     | Yes                        | Memory-sensitive cache |
| PhantomReference | No                  | Special lifecycle tracking | Advanced cleanup       |

---

# 25. The Golden Comparison

```text
STRONG
Object stays alive
        ↓
"KEEP IT"

SOFT
Object may stay alive
        ↓
"KEEP IF POSSIBLE"

WEAK
Doesn't keep object alive
        ↓
"YOU CAN COLLECT IT"

PHANTOM
Used for post-reachability cleanup tracking
```

---

# 26. `==` vs `.equals()`

This is technically not a GC topic, but **references make this important**.

```java
User a = new User();
User b = new User();
```

```java
a == b
```

asks:

> Do both references point to the exact same object?

Usually:

```text
false
```

While:

```java
a.equals(b)
```

asks whether the objects are logically equal according to `equals()`.

---

# 27. Reference Assignment

```java
User a = new User();
User b = a;
```

This does **not** copy the object.

It copies the reference.

```text
a ─────┐
       ├────→ User
b ─────┘
```

So:

```java
b.setName("Mohammad");
```

can affect what you see through:

```java
a.getName();
```

because both point to the same object.

---

# 28. `final` Reference Trap

```java
final List<String> list = new ArrayList<>();
```

You cannot:

```java
list = new ArrayList<>(); // ❌
```

But you can:

```java
list.add("hello"); // ✅
```

Why?

`final` prevents changing the **reference**, not mutating the referenced object.

```text
final list ─────→ mutable ArrayList
       ↑
   cannot change
```

---

# 29. The Most Important Exam Question

When you see a possible memory leak, ask:

### Step 1

**What object is growing?**

```text
List?
Map?
Cache?
Static collection?
ThreadLocal?
```

### Step 2

**Who holds the reference?**

```text
static field?
collection?
instance field?
thread?
```

### Step 3

**Is the reference strong?**

If yes, GC can't collect the object while that path remains reachable.

### Step 4

**When should the reference disappear?**

Then choose the solution:

```text
Explicit lifecycle → remove/clear/evict
Weak association   → WeakReference / WeakHashMap
Memory-sensitive cache → SoftReference
Bounded cache      → eviction / maximum size
```

---

# 30. Your Memory-Leak Exam Pattern

If you see:

```java
Map<String, Object> cache = new HashMap<>();

for (...) {
    cache.put(..., hugeObject);
}

System.gc();
```

**Don't immediately answer:**

> "Call `clear()`."

First ask:

> **Does the program even survive until `clear()`?**

If the loop retains 10 GB:

```text
insert
insert
insert
insert
...
OOM
```

may happen **before** the cleanup method executes.

That's exactly the kind of trap you encountered in the `CustomCache` challenge.

---

# 31. Final 15-Second Revision

```text
Strong
  → normal reference
  → keeps object alive

WeakReference
  → doesn't keep object alive
  → get() may become null after GC

SoftReference
  → memory-sensitive
  → JVM may clear under pressure

WeakHashMap
  → weak KEYS
  → entries may disappear after keys become unreachable

System.gc()
  → request, NOT guarantee

GC
  → based on reachability

Memory leak
  → unwanted objects remain reachable

HashMap cache
  → strong references
  → can grow forever

clear()
  → removes references from collection

null
  → removes one reference

== 
  → same object/reference

equals()
  → logical equality

final reference
  → reference can't change
  → object may still mutate
```

### One sentence to memorize

> **An object is eligible for GC when it is no longer reachable through strong references from GC Roots.**
