# Java Memory Model & volatile - EXAM CHEAT SHEET 📋

---

## 1. **JMM Core Concepts**

```
┌─────────────────────────────────────────────────────┐
│              JAVA MEMORY MODEL (JMM)                │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Main Memory ←→ CPU Cache ←→ Thread               │
│                                                     │
│  Problem: Stale values + Reordering                │
│  Solution: Happens-Before guarantees              │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### **Key JMM Rules**
| Rule | Description |
|------|-------------|
| **Program Order** | Each statement happens-before next in same thread |
| **Monitor Lock** | Unlock happens-before subsequent lock |
| **Volatile** | Write happens-before subsequent read |
| **Thread Start** | `start()` happens-before thread actions |
| **Thread Join** | Thread actions happen-before `join()` returns |
| **Transitivity** | A→B and B→C means A→C |

---

## 2. **volatile Keyword - What It Does**

```
┌──────────────────────────────────────────────────────┐
│                    volatile                          │
├──────────────────────────────────────────────────────┤
│  ✅ Visibility  - No caching, direct memory access  │
│  ✅ Ordering    - Prevents reordering               │
│  ✅ Happens-Before - Write → Read guarantee         │
│  ❌ Atomicity   - NO atomicity for compound ops     │
│  ❌ Mutual Exclusion - NO lock protection           │
└──────────────────────────────────────────────────────┘
```

### **When to Use volatile**
| Use Case | Example |
|----------|---------|
| **Flags/State** | `volatile boolean running = true;` |
| **Single Writer** | One thread writes, many read |
| **Double-Checked Locking** | `private static volatile Singleton instance;` |
| **Simple Status** | `volatile int status;` |

### **When NOT to Use volatile**
| Use Case | Reason | Solution |
|----------|--------|----------|
| **Counters** | `counter++` not atomic | `AtomicInteger` |
| **Check-then-act** | Race condition | `synchronized` |
| **Multiple variables** | Inconsistent state | `synchronized` |
| **Long/double on 32-bit** | May not be atomic | `volatile` helps |

---

## 3. **Atomicity Quick Reference**

```
┌─────────────────────────────────────────────────────────┐
│                ATOMICITY CHECKLIST                     │
├─────────────────────────────────────────────────────────┤
│  ✅ Atomic:                                            │
│     • Simple assignment: int x = 5;                   │
│     • Reference assignment: obj.field = value;        │
│     • AtomicInteger operations                        │
│     • volatile long/double reads/writes               │
│                                                       │
│  ❌ NOT Atomic:                                       │
│     • x++ / x-- (read-modify-write)                  │
│     • x += 2 (read-modify-write)                     │
│     • check-then-act: if(x) then x++                 │
│     • new Object() (multiple steps)                  │
└─────────────────────────────────────────────────────────┘
```

### **Atomic Operations Comparison**
| Approach | Atomicity | Visibility | Performance |
|----------|-----------|------------|-------------|
| `volatile` | ❌ No | ✅ Yes | Fastest |
| `synchronized` | ✅ Yes | ✅ Yes | Slower |
| `AtomicInteger` | ✅ Yes | ✅ Yes | Fast (lock-free) |
| `LongAdder` | ✅ Yes | ✅ Yes | Fastest for writes |
| `ReentrantLock` | ✅ Yes | ✅ Yes | Moderate |

---

## 4. **Memory Barriers (Hardware Level)**

```
┌────────────────────────────────────────────────────────┐
│              MEMORY BARRIER TYPES                     │
├────────────────────────────────────────────────────────┤
│                                                       │
│  LoadLoad:   Load1 → LoadLoad → Load2                │
│  StoreStore: Store1 → StoreStore → Store2            │
│  LoadStore:  Load1 → LoadStore → Store2              │
│  StoreLoad:  Store1 → StoreLoad → Load2 (expensive)  │
│                                                       │
│  Volatile Write:                                      │
│  StoreStore → [Write] → StoreLoad                    │
│                                                       │
│  Volatile Read:                                       │
│  LoadLoad → [Read] → LoadStore                       │
└────────────────────────────────────────────────────────┘
```

---

## 5. **Common Patterns & Anti-Patterns**

### ✅ **Correct Patterns**

**Pattern 1: Flag/Status**
```java
private volatile boolean running = true;

// Writer
public void stop() { running = false; }

// Reader
while (running) { /* work */ }
```

**Pattern 2: Double-Checked Locking**
```java
private static volatile Singleton instance;

public static Singleton getInstance() {
    if (instance == null) {
        synchronized (Singleton.class) {
            if (instance == null) {
                instance = new Singleton(); // volatile prevents reordering
            }
        }
    }
    return instance;
}
```

**Pattern 3: Copy-on-Write**
```java
private volatile Map<String, String> cache = new HashMap<>();

public void put(String key, String value) {
    Map<String, String> newCache = new HashMap<>(cache);
    newCache.put(key, value);
    cache = newCache; // volatile write
}
```

### ❌ **Anti-Patterns (Common Mistakes)**

**Mistake 1: volatile with compound operations**
```java
private volatile int counter = 0;
public void increment() {
    counter++; // ❌ NOT ATOMIC!
}
// Fix: Use AtomicInteger
```

**Mistake 2: Check-then-act**
```java
private volatile List<String> list = new ArrayList<>();
public void addIfAbsent(String s) {
    if (!list.contains(s)) { // Check
        list.add(s);         // Act - Race condition!
    }
}
// Fix: Use synchronized
```

**Mistake 3: Assuming volatile for multiple variables**
```java
private volatile int x = 0, y = 0;

// Thread 1: x = 1; y = 2;
// Thread 2: if (y == 2) { System.out.println(x); } // x might be 0!
// Fix: Use synchronized or combine in one volatile object
private volatile Point point = new Point(0, 0);
```

---

## 6. **Happens-Before Examples**

```
┌─────────────────────────────────────────────────────────┐
│           HAPPENS-BEFORE VISUALIZED                    │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  Thread A              Thread B                        │
│  ┌────────────┐       ┌────────────┐                  │
│  │ x = 42     │       │            │                  │
│  │            │       │            │                  │
│  │ flag=true  │──────▶│ if(flag)   │  HB ✓           │
│  │ (volatile) │  HB   │ (volatile) │                  │
│  │            │       │            │                  │
│  │            │       │ print(x)   │  Sees 42! ✓     │
│  └────────────┘       └────────────┘                  │
│                                                         │
│  HB = Happens-Before                                   │
│  ✓ = Guaranteed by volatile                            │
└─────────────────────────────────────────────────────────┘
```

---

## 7. **Quick Decision Tree**

```
                    Need thread safety?
                           │
                    ┌──────▼──────┐
                    │ Is it just  │
                    │ a flag/state?│
                    └──────┬──────┘
                       Yes │   No
                     ┌─────┴─────┐
                     │ volatile  │
                     └───────────┘
                           │
                    ┌──────▼──────┐
                    │ Is it a      │
                    │ counter?     │
                    └──────┬──────┘
                       Yes │   No
                     ┌─────┴─────┐
                     │ Atomic    │
                     │ Integer   │
                     └───────────┘
                           │
                    ┌──────▼──────┐
                    │ Multiple    │
                    │ variables?  │
                    └──────┬──────┘
                       Yes │   No
                     ┌─────┴─────┐
                     │ synchronized│
                     └───────────┘
```

---

## 8. **Exam-Style Question Templates**

### **Q1: Visibility Question**
```java
class Test {
    private static boolean flag = false; // without volatile
    private static int value = 0;
    
    // Thread 1: value = 42; flag = true;
    // Thread 2: if (flag) System.out.println(value);
    
    // Q: Is 42 guaranteed? 
    // A: NO - flag not volatile, no happens-before
}
```

### **Q2: Atomicity Question**
```java
class Counter {
    private volatile int count = 0;
    public void increment() { count++; }
    
    // Q: Is this thread-safe?
    // A: NO - count++ is not atomic!
}
```

### **Q3: Double-Checked Locking Question**
```java
class Singleton {
    private static Singleton instance; // without volatile
    
    public static Singleton getInstance() {
        if (instance == null) {
            synchronized(Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                    // Q: What's wrong?
                    // A: Reordering possible - other thread sees partial object!
                }
            }
        }
        return instance;
    }
}
```

---

## 9. **Key Differences Summary**

```
┌──────────────────────────────────────────────────────────────┐
│              COMPARISON TABLE                               │
├──────────────┬────────────┬────────────┬────────────┬───────┤
│ Feature      │ volatile   │synchronized│ Atomic*    │ Lock  │
├──────────────┼────────────┼────────────┼────────────┼───────┤
│ Visibility   │ ✅ Yes     │ ✅ Yes     │ ✅ Yes     │ ✅ Yes│
│ Atomicity    │ ❌ No      │ ✅ Yes     │ ✅ Yes     │ ✅ Yes│
│ Reordering   │ ✅ Prevents│ ✅ Prevents│ ✅ Prevents│ ✅ Yes│
│ Performance  │ Fastest    │ Slow       │ Fast       │ Medium│
│ Lock-free    │ ✅ Yes     │ ❌ No      │ ✅ Yes     │ ❌ No │
│ Single var   │ ✅ Good    │ ✅ Good    │ ✅ Best    │ ✅ OK │
│ Multiple var │ ❌ Bad     │ ✅ Best    │ ❌ Bad     │ ✅ Good│
└──────────────┴────────────┴────────────┴────────────┴───────┘
```

---

## 10. **Memory Formula (Memorize This!)**

```
volatile = (CacheBypass + MemoryBarrier + HappensBefore) - Atomicity

Atomicity = synchronized OR AtomicInteger OR Lock

Visibility = volatile OR synchronized OR AtomicInteger

Happens-Before = volatile OR synchronized OR start() OR join()

Thread Safety = Visibility + Atomicity + Ordering
```

---

## 11. **Final Checklist Before Exam ✅**

- [ ] Know that `volatile` = visibility + ordering, NOT atomicity
- [ ] Remember `counter++` is NOT atomic (read-modify-write)
- [ ] Double-checked locking needs `volatile`
- [ ] `synchronized` gives both atomicity AND visibility
- [ ] `AtomicInteger` is lock-free and thread-safe
- [ ] Happens-before rules for volatile: write → read
- [ ] Memory barriers prevent reordering
- [ ] Check-then-act needs synchronization
- [ ] `long`/`double` need `volatile` for atomic 64-bit ops
- [ ] Use `volatile` for flags, `AtomicInteger` for counters

---

## 🚀 **Quick Memory Triggers**

```
VOLATILE = V.O.A.T.
V - Visibility
O - Ordering
A - Atomicity? NO!
T - Threads see updates

SYNCHRONIZED = S.A.F.E.
S - Safe
A - Atomic
F - Full visibility
E - Exclusive access

ATOMIC = C.A.S.
C - Compare
A - And
S - Swap (hardware instruction)
```
