# Java Reflection — Exam Cheat Sheet

## 1. What is Reflection?

**Reflection** allows Java code to inspect and manipulate classes, methods, fields, and constructors **at runtime**.

Normally:

```java
Service service = new ServiceImpl();
```

With Reflection:

```java
Class<?> clazz = Class.forName("com.example.ServiceImpl");

Object service =
        clazz.getDeclaredConstructor().newInstance();
```

### Mental model

```text
Normal Java
compile time
    ↓
Class known
    ↓
new ServiceImpl()

Reflection
runtime
    ↓
Discover Class
    ↓
Inspect metadata
    ↓
Construct / invoke / access dynamically
```

---

# 2. The `Class<?>` Object

Everything starts with `Class`.

```java
Class<?> clazz;
```

A `Class` object contains metadata about a Java type.

You can ask it:

```text
What is your name?
What constructors do you have?
What methods?
What fields?
What interfaces?
What superclass?
What modifiers?
```

---

# 3. Three Ways to Get `Class`

### 1. `.class`

```java
Class<?> clazz = MyClass.class;
```

When you know the class at compile time.

---

### 2. `getClass()`

```java
MyClass obj = new MyClass();

Class<?> clazz = obj.getClass();
```

When you have an object.

---

### 3. `Class.forName()`

```java
Class<?> clazz =
    Class.forName("com.example.MyClass");
```

When the class name is available dynamically.

### Memorize

```text
MyClass.class
    → compile-time class

obj.getClass()
    → runtime class of object

Class.forName("...")
    → dynamically load/find class by name
```

---

# 4. Class Name

```java
clazz.getName();
```

Example:

```text
com.example.MyClass
```

Simple name:

```java
clazz.getSimpleName();
```

Result:

```text
MyClass
```

Package:

```java
clazz.getPackageName();
```

---

# 5. Constructors

## Get public constructors

```java
Constructor<?>[] constructors =
        clazz.getConstructors();
```

Only **public** constructors.

---

## Get declared constructors

```java
Constructor<?>[] constructors =
        clazz.getDeclaredConstructors();
```

Includes:

```text
public
protected
package-private
private
```

declared by that class.

### Important distinction

```text
getConstructors()
    → public constructors

getDeclaredConstructors()
    → constructors declared by this class
    → including non-public
```

---

# 6. Get a Specific Constructor

No arguments:

```java
Constructor<?> constructor =
    clazz.getDeclaredConstructor();
```

Constructor with parameters:

```java
Constructor<?> constructor =
    clazz.getDeclaredConstructor(
        String.class,
        int.class
    );
```

For:

```java
class User {

    public User(String name, int age) {
    }
}
```

you can find it with:

```java
clazz.getDeclaredConstructor(
    String.class,
    int.class
);
```

### Exam trick

Primitive types must be specified as:

```java
int.class
boolean.class
long.class
double.class
```

not:

```java
Integer.class
Boolean.class
Long.class
Double.class
```

Those are different types.

---

# 7. Create an Object Dynamically

Modern approach:

```java
Object obj =
    clazz.getDeclaredConstructor().newInstance();
```

Instead of:

```java
new MyClass();
```

### Remember

Prefer:

```java
getDeclaredConstructor().newInstance()
```

over the old:

```java
clazz.newInstance()
```

---

# 8. Constructor Parameters

This is **very important for your DI exam question**.

```java
Constructor<?> constructor =
    clazz.getDeclaredConstructors()[0];

Class<?>[] parameterTypes =
    constructor.getParameterTypes();
```

Suppose:

```java
class UserService {

    public UserService(
        Database database,
        Logger logger
    ) {
    }
}
```

Then:

```java
parameterTypes[0] == Database.class
parameterTypes[1] == Logger.class
```

This allows you to discover dependencies dynamically.

---

# 9. Manual Dependency Injection with Reflection

Suppose:

```java
class ServiceConsumer {

    public ServiceConsumer(Service service) {
    }
}
```

Reflection can discover:

```text
ServiceConsumer
      ↓
constructor
      ↓
Service.class
      ↓
resolve Service
      ↓
ServiceImpl
```

Typical pattern:

```java
public <T> T getInstance(Class<T> clazz) {

    Constructor<?> constructor =
        clazz.getDeclaredConstructors()[0];

    Class<?>[] parameterTypes =
        constructor.getParameterTypes();

    Object[] dependencies =
        new Object[parameterTypes.length];

    for (int i = 0; i < parameterTypes.length; i++) {
        dependencies[i] =
            getInstance(parameterTypes[i]);
    }

    Object object =
        constructor.newInstance(dependencies);

    return clazz.cast(object);
}
```

This is the core Reflection technique behind a simple DI container.

---

# 10. Methods

Get public methods:

```java
Method[] methods =
    clazz.getMethods();
```

Get methods declared by the class:

```java
Method[] methods =
    clazz.getDeclaredMethods();
```

### Important

```text
getMethods()
    → public methods
    → includes inherited methods

getDeclaredMethods()
    → methods declared directly by class
    → includes non-public
```

---

# 11. Get a Specific Method

Suppose:

```java
public String process(String input)
```

Find it:

```java
Method method =
    clazz.getDeclaredMethod(
        "process",
        String.class
    );
```

Then:

```java
method.getName();
```

returns:

```text
process
```

---

# 12. Invoke a Method

```java
Object result =
    method.invoke(object, "hello");
```

Conceptually equivalent to:

```java
object.process("hello");
```

Reflection version:

```text
Method
  ↓
invoke()
  ↓
actual method execution
```

---

# 13. Method Metadata

You can inspect:

```java
method.getName();
```

```java
method.getReturnType();
```

```java
method.getParameterTypes();
```

```java
method.getModifiers();
```

Example:

```java
for (Method method : clazz.getDeclaredMethods()) {
    System.out.println(method.getName());
}
```

---

# 14. Fields

Public fields:

```java
Field[] fields =
    clazz.getFields();
```

Declared fields:

```java
Field[] fields =
    clazz.getDeclaredFields();
```

Specific field:

```java
Field field =
    clazz.getDeclaredField("username");
```

---

# 15. Read a Field

```java
Object value =
    field.get(object);
```

Equivalent conceptually to:

```java
object.username;
```

---

# 16. Modify a Field

```java
field.set(object, "Mohammad");
```

Conceptually:

```java
object.username = "Mohammad";
```

---

# 17. Private Fields

Suppose:

```java
private String password;
```

You can find it:

```java
Field field =
    clazz.getDeclaredField("password");
```

Historically/common exam pattern:

```java
field.setAccessible(true);
```

Then:

```java
field.get(object);
```

or:

```java
field.set(object, "secret");
```

### Exam mental model

```text
private field
     ↓
getDeclaredField()
     ↓
setAccessible(true)
     ↓
get() / set()
```

Modern Java's module/access rules can restrict deep reflection in some cases, but this is the standard basic Reflection pattern.

---

# 18. `getDeclaredX()` vs `getX()`

This is one of the most important things to memorize.

| API                         | Meaning                             |
| --------------------------- | ----------------------------------- |
| `getMethods()`              | public methods, including inherited |
| `getDeclaredMethods()`      | methods declared by this class      |
| `getFields()`               | public fields, including inherited  |
| `getDeclaredFields()`       | fields declared by this class       |
| `getConstructors()`         | public constructors                 |
| `getDeclaredConstructors()` | constructors declared by this class |

### Shortcut

```text
getX()
    → public / inherited visibility rules

getDeclaredX()
    → directly declared in this class
    → can include private
```

---

# 19. Interfaces

Get implemented interfaces:

```java
Class<?>[] interfaces =
    clazz.getInterfaces();
```

Example:

```java
class ServiceImpl implements Service
```

Then:

```java
ServiceImpl.class.getInterfaces();
```

contains:

```text
Service.class
```

---

# 20. Superclass

```java
Class<?> parent =
    clazz.getSuperclass();
```

Example:

```java
class Dog extends Animal
```

Then:

```java
Dog.class.getSuperclass()
```

returns:

```text
Animal.class
```

---

# 21. `isInterface()`

```java
clazz.isInterface()
```

Example:

```java
Service.class.isInterface();       // true
ServiceImpl.class.isInterface();   // false
```

Useful when scanning classes.

---

# 22. `Modifier.isAbstract()`

```java
Modifier.isAbstract(
    clazz.getModifiers()
);
```

Useful for filtering classes.

For dynamic implementation discovery:

```java
if (!candidate.isInterface()
        && !Modifier.isAbstract(candidate.getModifiers())) {

    // concrete implementation
}
```

---

# 23. `isAssignableFrom()` — VERY IMPORTANT

This is one of the **highest-value Reflection methods for your exam**.

Suppose:

```java
interface Service {}

class ServiceImpl implements Service {}
```

Then:

```java
Service.class.isAssignableFrom(
    ServiceImpl.class
);
```

returns:

```text
true
```

Because:

```java
Service service = new ServiceImpl();
```

is valid.

### Mental model

```text
A.isAssignableFrom(B)

means:

Can B be assigned to A?
```

So:

```java
Service.class.isAssignableFrom(
    ServiceImpl.class
);
```

means:

> Can a `ServiceImpl` be assigned to a `Service`?

Yes.

---

# 24. Don't Reverse `isAssignableFrom()`

Correct:

```java
Service.class.isAssignableFrom(
    ServiceImpl.class
);
```

Usually wrong for implementation discovery:

```java
ServiceImpl.class.isAssignableFrom(
    Service.class
);
```

Think:

```text
parent/interface
      ↓
isAssignableFrom
      ↓
candidate implementation
```

---

# 25. `isInstance()`

`isInstance()` works with an **object**.

```java
Object obj = new ServiceImpl();

Service.class.isInstance(obj);
```

returns:

```text
true
```

Difference:

```text
isAssignableFrom()
    Class vs Class

isInstance()
    Class vs Object
```

### Memorize

```java
Service.class.isAssignableFrom(
    ServiceImpl.class
);
```

vs

```java
Service.class.isInstance(
    serviceObject
);
```

---

# 26. Dynamic Implementation Discovery

Suppose:

```java
interface Service {}
```

and:

```java
class ServiceImpl implements Service {}
class AnotherServiceImpl implements Service {}
```

If you scan classes, you can determine implementations:

```java
if (Service.class.isAssignableFrom(candidate)
        && !candidate.isInterface()
        && !Modifier.isAbstract(candidate.getModifiers())) {

    // candidate is a concrete Service implementation
}
```

This is exactly the kind of logic useful in a homemade DI container.

---

# 27. `Class.forName()` + Reflection

Very common dynamic pattern:

```java
String className =
    "com.example.ServiceImpl";

Class<?> clazz =
    Class.forName(className);

Object object =
    clazz.getDeclaredConstructor()
         .newInstance();
```

Flow:

```text
String
  ↓
Class.forName()
  ↓
Class<?>
  ↓
getDeclaredConstructor()
  ↓
Constructor
  ↓
newInstance()
  ↓
Object
```

---

# 28. `Class<T>` and Generics

A useful DI method:

```java
public <T> T getInstance(Class<T> clazz)
```

Then:

```java
Service service =
    injector.getInstance(Service.class);
```

The `Class<T>` tells Java:

> This Class represents T.

And:

```java
return clazz.cast(object);
```

can safely convert the result to `T`.

---

# 29. `Class.cast()`

Instead of:

```java
return (T) object;
```

you can use:

```java
return clazz.cast(object);
```

Example:

```java
Service service =
    Service.class.cast(object);
```

This is especially useful when writing generic Reflection utilities.

---

# 30. Reflection Exceptions

Know these names.

### `Class.forName()`

```text
ClassNotFoundException
```

Class couldn't be found.

---

### `getDeclaredMethod()`

```text
NoSuchMethodException
```

Method doesn't exist with that signature.

---

### `getDeclaredField()`

```text
NoSuchFieldException
```

Field doesn't exist.

---

### Constructor operations

Can involve:

```text
InstantiationException
IllegalAccessException
InvocationTargetException
NoSuchMethodException
```

For exam questions, recognize that Reflection APIs are commonly wrapped:

```java
try {
    ...
} catch (ReflectiveOperationException e) {
    throw new RuntimeException(e);
}
```

---

# 31. Constructor Signature Matters

Suppose:

```java
class User {

    User(String name) {
    }
}
```

This won't work:

```java
clazz.getDeclaredConstructor();
```

because there is no no-arg constructor.

You need:

```java
clazz.getDeclaredConstructor(
    String.class
);
```

### Exam trap

Reflection doesn't magically know which constructor you want.

You must specify the parameter types.

---

# 32. `getDeclaredConstructors()[0]` — Trap

You may see:

```java
Constructor<?> constructor =
    clazz.getDeclaredConstructors()[0];
```

This is okay for a **simple exam exercise**, but it's not robust.

Why?

A class can have:

```java
ClassA()
ClassA(String)
ClassA(Database, Logger)
```

There is no guarantee that `[0]` is the constructor you actually want.

Better framework design:

```text
@Inject constructor
        ↓
find constructor annotated @Inject
```

or define another selection rule.

For your simple DI challenge, however, the first constructor may be acceptable if the challenge deliberately keeps things simple.

---

# 33. Reflection + Recursive DI

This is a pattern you should recognize immediately.

```java
public <T> T getInstance(Class<T> clazz)
```

### Step 1

Find implementation if `clazz` is an interface.

```text
Service.class
      ↓
ServiceImpl.class
```

### Step 2

Find constructor.

```text
ServiceImpl(...)
```

### Step 3

Find constructor parameters.

```text
Database.class
Logger.class
```

### Step 4

Recursively resolve them.

```text
getInstance(Database.class)
getInstance(Logger.class)
```

### Step 5

Construct.

```text
new ServiceImpl(database, logger)
```

This is the fundamental idea behind dependency injection.

---

# 34. Reflection vs Normal Java

### Normal

```java
Service service = new ServiceImpl();
```

Everything is known statically.

### Reflection

```java
Class<?> clazz = ...;

Object object =
    clazz.getDeclaredConstructor()
         .newInstance();
```

The concrete type can be discovered at runtime.

---

# 35. Reflection vs `ServiceLoader`

Very important given your previous challenge.

### Reflection

You implement discovery yourself:

```text
scan classes
   ↓
Class.forName()
   ↓
inspect classes
   ↓
find implementation
```

### `ServiceLoader`

Java provides a standard service discovery mechanism:

```text
META-INF/services
       ↓
ServiceLoader
       ↓
registered implementations
```

So:

```text
Reflection
= dynamically inspect/load classes

ServiceLoader
= dynamically discover registered service implementations
```

---

# 36. Reflection + OCP

If you see:

```java
if (type.equals("A")) {
    return new A();
}

if (type.equals("B")) {
    return new B();
}
```

and the requirement says:

> Add new implementations without modifying existing code.

Think:

```text
❌ hardcoded if/switch

Possible solutions:
✓ Reflection
✓ ServiceLoader
✓ registration map
✓ plugin architecture
✓ DI container
```

Use the solution suggested by the **hints**.

---

# 37. Reflection Security / Access Trap

Reflection can expose things that normal code cannot easily access:

```java
private field
private method
private constructor
```

using APIs such as:

```java
getDeclaredField()
getDeclaredMethod()
getDeclaredConstructor()
```

and, where permitted:

```java
setAccessible(true)
```

Therefore Reflection can weaken normal encapsulation.

### Exam phrase

> Reflection allows runtime access to class members, but it should be used carefully because it can bypass normal compile-time access restrictions and makes code more fragile.

---

# 38. Reflection Performance

Reflection generally has more overhead and less compile-time safety than direct calls.

Normal:

```java
service.process(input);
```

Reflection:

```java
method.invoke(service, input);
```

Reflection is useful when you need:

```text
dynamic discovery
frameworks
DI
serialization
plugins
testing
configuration
```

but shouldn't be used just because it looks clever.

---

# 39. The Reflection API You Actually Need to Memorize

For your exam, prioritize these:

```java
Class.forName(...)
```

```java
clazz.getDeclaredConstructor(...)
```

```java
constructor.newInstance(...)
```

```java
constructor.getParameterTypes()
```

```java
clazz.getDeclaredMethods()
```

```java
clazz.getDeclaredMethod(...)
```

```java
method.invoke(...)
```

```java
clazz.getDeclaredFields()
```

```java
clazz.getDeclaredField(...)
```

```java
field.get(...)
```

```java
field.set(...)
```

```java
clazz.isInterface()
```

```java
Modifier.isAbstract(...)
```

```java
interfaceType.isAssignableFrom(candidate)
```

```java
type.isInstance(object)
```

```java
clazz.cast(object)
```

---

# 40. One-Minute Reflection Revision

```text
                    Reflection
                        │
             ┌──────────┼──────────┐
             ↓          ↓          ↓
           Class     Constructor   Method
             │          │           │
             │          │           └─ invoke()
             │          │
             │          └─ newInstance()
             │
             ├─ getName()
             ├─ getSuperclass()
             ├─ getInterfaces()
             ├─ isInterface()
             ├─ getDeclaredMethods()
             ├─ getDeclaredFields()
             └─ getDeclaredConstructors()
```

### Most important patterns

```text
Get class dynamically:
Class.forName("...")

Create object:
clazz.getDeclaredConstructor().newInstance()

Find dependencies:
constructor.getParameterTypes()

Invoke method:
method.invoke(object, args...)

Access field:
field.get(object)
field.set(object, value)

Check implementation:
Service.class.isAssignableFrom(candidate)

Check object:
Service.class.isInstance(object)

Generic cast:
clazz.cast(object)
```

---

# 41. Final Exam Memory Hook

If the question says:

> **"At runtime, discover what this class needs and create it dynamically."**

Think:

```text
Class
 ↓
Constructor
 ↓
getParameterTypes()
 ↓
resolve dependencies recursively
 ↓
newInstance()
```

If it says:

> **"Find all implementations of this interface dynamically."**

Think:

```text
ServiceLoader
```

or, if the challenge explicitly expects manual scanning:

```text
ClassLoader
 → Class.forName()
 → isAssignableFrom()
```

If it says:

> **"Access a private field/method."**

Think:

```text
getDeclaredField()
getDeclaredMethod()
getDeclaredConstructor()
```

and potentially:

```java
setAccessible(true);
```

If it says:

> **"Add implementations without modifying existing code."**

Think:

```text
OCP
↓
dynamic discovery / registration
↓
ServiceLoader / Reflection / Strategy
```

**These are the Reflection patterns I'd prioritize for your exam; you don't need to memorize the entire Reflection API.**
