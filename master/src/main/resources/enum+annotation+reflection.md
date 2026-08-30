# 📚 **COMPLETE CHEAT SHEET: Chapters 24-25 + Effective Java Items 39 & 65**

## 🎯 **PART 1: ENUMS (Chapter 24)**

### **Enum Basics**
```java
// 1. Simple Enum
public enum Day { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY }

// 2. Enum with Constructor/Fields
public enum Planet {
    MERCURY(3.303e+23, 2.4397e6),
    VENUS(4.869e+24, 6.0518e6),
    EARTH(5.976e+24, 6.37814e6);
    
    private final double mass;
    private final double radius;
    
    Planet(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
    }
    
    public double surfaceGravity() {
        return 6.673e-11 * mass / (radius * radius);
    }
}

// 3. Enum with Abstract Method (Strategy Pattern)
public enum Operation {
    PLUS { public double apply(double x, double y) { return x + y; } },
    MINUS { public double apply(double x, double y) { return x - y; } },
    TIMES { public double apply(double x, double y) { return x * y; } },
    DIVIDE { public double apply(double x, double y) { return x / y; } };
    
    public abstract double apply(double x, double y);
}
```

### **Enum Built-in Methods**
```java
Day[] days = Day.values();                    // Get all constants
Day day = Day.valueOf("MONDAY");              // Convert string to enum
String name = Day.MONDAY.name();              // Get name as string
int ordinal = Day.MONDAY.ordinal();           // Position (0-based)
boolean isEqual = Day.MONDAY == Day.MONDAY;   // Compare (use ==, not equals())
int compare = Day.MONDAY.compareTo(Day.TUESDAY); // -1 (order comparison)

// Common patterns
public boolean isWeekend() {
    return this == SATURDAY || this == SUNDAY;
}
```

### **Enum Important Rules**
```
✅ Enums are implicitly final (can't be extended)
✅ Enums can implement interfaces
✅ Enums cannot extend classes (already extends java.lang.Enum)
✅ Each constant is a singleton instance
✅ Enum constructors are always private (implicitly)
✅ Enum constants must be declared first
✅ Can have abstract methods (each constant must implement)
✅ Use == for comparison (not equals())
```

### **Enum vs Constants - KEY EXAM POINT**
```java
// ❌ BAD: int constants (not type-safe)
public static final int MONDAY = 0;
public static final int TUESDAY = 1;
void processDay(int day) { }  // Can pass any int: processDay(999);

// ✅ GOOD: Enum (type-safe)
public enum Day { MONDAY, TUESDAY }
void processDay(Day day) { }  // Only accepts Day constants

// Advantages of Enum:
// 1. Type safety
// 2. Can add methods/fields
// 3. Better toString representation
// 4. Compile-time checking
// 5. Singleton guarantee
// 6. Can implement interfaces
```

---

## 🏷️ **PART 2: ANNOTATIONS (Chapter 25 + Item 39)**

### **Creating Custom Annotations**
```java
import java.lang.annotation.*;

// Step 1: Define annotation
@Retention(RetentionPolicy.RUNTIME)      // When available
@Target(ElementType.METHOD)              // Where it can be used
@Documented                              // Include in Javadoc
@Inherited                               // Subclasses inherit
public @interface Test {
    // Elements (like abstract methods)
    String description() default "No description";
    int priority() default 1;
    String[] tags() default {};
    Class<?> expected() default None.class;
}
```

### **Meta-Annotations - MUST KNOW!**
| Meta-Annotation | Purpose | Values |
|----------------|---------|--------|
| **@Retention** | When annotation is available | `SOURCE` (compile only) <br>`CLASS` (in .class file) <br>`RUNTIME` (runtime accessible) |
| **@Target** | Where annotation can be used | `TYPE`, `METHOD`, `FIELD`, `PARAMETER` <br>`CONSTRUCTOR`, `ANNOTATION_TYPE`, `PACKAGE` |
| **@Documented** | Include in Javadoc | - |
| **@Inherited** | Subclasses inherit annotation | - |
| **@Repeatable** | Can apply multiple times | - |

### **Built-in Annotations**
```java
@Override          // Method overrides superclass
@Deprecated       // Should not be used (with reason)
@SuppressWarnings // Suppress compiler warnings
@FunctionalInterface // Interface with single abstract method
@SafeVarargs     // Suppress heap pollution warnings
```

### **Using Annotations**
```java
// Basic usage
@Test(description = "Test addition", priority = 1)
public void testAdd() { }

// Multiple values
@Test(tags = {"math", "important"}, priority = 5)
public void testMultiply() { }

// Single element (convenient for "value" element)
@SuppressWarnings("unchecked")
public void method() { }

// Marker annotation (no elements)
@Deprecated
public void oldMethod() { }

// Default values
@Test  // Uses all defaults
public void testDefault() { }
```

### **Processing Annotations via Reflection**
```java
public class AnnotationProcessor {
    public static void main(String[] args) {
        // Get all methods
        Method[] methods = Calculator.class.getDeclaredMethods();
        
        for (Method method : methods) {
            // Check if annotation exists
            if (method.isAnnotationPresent(Test.class)) {
                Test test = method.getAnnotation(Test.class);
                
                // Access values
                System.out.println("Method: " + method.getName());
                System.out.println("Description: " + test.description());
                System.out.println("Priority: " + test.priority());
                
                // Execute test method
                try {
                    method.invoke(new Calculator());
                } catch (Exception e) {
                    System.out.println("Test failed!");
                }
            }
        }
        
        // Alternative: Get all annotations
        Annotation[] annotations = method.getAnnotations();
        for (Annotation ann : annotations) {
            if (ann instanceof Test) {
                Test test = (Test) ann;
            }
        }
    }
}
```

### **Item 39: Key Points - MUST REMEMBER**
```
📌 "Prefer annotations to naming patterns"

❌ BAD (Naming Pattern):
- String-based: methods must start with "test"
- No compile-time checking
- Can't add metadata
- Misspelling breaks silently
- Example: testAddition() vs tstAddition()

✅ GOOD (Annotation):
- Compile-time safety
- Type-safe
- Can add rich metadata (priority, description, expected exception)
- Better IDE support
- Example: @Test public void addition() { }
```

### **Annotation Processing at Compile Time**
```java
// Using @Retention(SOURCE) for compile-time processing
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Builder {
    String value() default "";
}

// Used with annotation processors (AbstractProcessor)
// Generated code at compile time (like Lombok)
```

---

## 🔍 **PART 3: REFLECTION (Chapter 26 + Item 65)**

### **Getting Class Objects - 3 Ways**
```java
// Method 1: Using .class
Class<String> clazz1 = String.class;

// Method 2: Using getClass()
Class<?> clazz2 = "Hello".getClass();

// Method 3: Using Class.forName()
Class<?> clazz3 = Class.forName("java.lang.String");
// Throws ClassNotFoundException
```

### **Inspecting Class Structure**
```java
public class ClassInspector {
    public static void inspect(Class<?> clazz) {
        // Basic info
        System.out.println("Class: " + clazz.getName());
        System.out.println("Simple Name: " + clazz.getSimpleName());
        System.out.println("Package: " + clazz.getPackageName());
        
        // Modifiers
        int modifiers = clazz.getModifiers();
        System.out.println("Public: " + Modifier.isPublic(modifiers));
        System.out.println("Final: " + Modifier.isFinal(modifiers));
        System.out.println("Abstract: " + Modifier.isAbstract(modifiers));
        
        // Superclass
        System.out.println("Superclass: " + clazz.getSuperclass());
        
        // Interfaces
        Class<?>[] interfaces = clazz.getInterfaces();
        
        // Fields
        Field[] fields = clazz.getDeclaredFields();      // All fields
        Field[] publicFields = clazz.getFields();        // Public only
        for (Field field : fields) {
            System.out.println("Field: " + field.getName() + 
                             ", Type: " + field.getType());
        }
        
        // Methods
        Method[] methods = clazz.getDeclaredMethods();    // All methods
        Method[] publicMethods = clazz.getMethods();      // Public + inherited
        
        // Constructors
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> c : constructors) {
            System.out.println("Constructor: " + c);
        }
    }
}
```

### **Accessing Private Members (Breaking Encapsulation)**
```java
public class Secret {
    private String hidden = "Top Secret";
    private static final String CONSTANT = "Secret!";
    private void reveal() { System.out.println("Secret revealed!"); }
}

// Access private field
Secret obj = new Secret();
Field field = Secret.class.getDeclaredField("hidden");
field.setAccessible(true);  // ⚠️ Break encapsulation
String value = (String) field.get(obj);
field.set(obj, "New Value");

// Access private static field
Field constField = Secret.class.getDeclaredField("CONSTANT");
constField.setAccessible(true);
String constValue = (String) constField.get(null);  // null for static

// Access private method
Method method = Secret.class.getDeclaredMethod("reveal");
method.setAccessible(true);
method.invoke(obj);  // Invoke method

// Access private constructor
Constructor<Secret> constructor = Secret.class.getDeclaredConstructor();
constructor.setAccessible(true);
Secret newObj = constructor.newInstance();
```

### **Dynamic Method Invocation**
```java
public class DynamicInvocation {
    public static void main(String[] args) throws Exception {
        // Create instance dynamically
        Class<?> clazz = Class.forName("java.util.ArrayList");
        Object list = clazz.getDeclaredConstructor().newInstance();
        
        // Invoke add method
        Method addMethod = clazz.getMethod("add", Object.class);
        addMethod.invoke(list, "Hello");
        addMethod.invoke(list, "World");
        
        // Invoke size method
        Method sizeMethod = clazz.getMethod("size");
        int size = (int) sizeMethod.invoke(list);
        System.out.println("Size: " + size);  // 2
        
        // Invoke get method
        Method getMethod = clazz.getMethod("get", int.class);
        String item = (String) getMethod.invoke(list, 0);
        System.out.println("Item 0: " + item);  // Hello
        
        // Static method invocation
        Method staticMethod = Math.class.getMethod("max", double.class, double.class);
        double result = (double) staticMethod.invoke(null, 5.0, 3.0);
    }
}
```

### **Common Reflection Patterns**
```java
// 1. Create instance
Object obj = clazz.getDeclaredConstructor().newInstance();
Object obj = clazz.getConstructor(String.class).newInstance("Hello");

// 2. Invoke method
Method method = clazz.getMethod("methodName", paramTypes);
Object result = method.invoke(obj, params);

// 3. Access field
Field field = clazz.getDeclaredField("fieldName");
field.setAccessible(true);
Object value = field.get(obj);
field.set(obj, newValue);

// 4. Array operations
Object array = Array.newInstance(String.class, 5);
Array.set(array, 0, "Hello");
String value = (String) Array.get(array, 0);
```

---

## ⚡ **PERFORMANCE TIPS (Item 65) - CRITICAL**

### **Why Reflection is SLOW:**
```
1. No JIT optimization/inlining
2. Security checks for each call
3. Boxing/unboxing overhead
4. Method lookup is expensive
5. Cannot be optimized at compile time
```

### **Optimization Strategies**

#### **1. Cache Reflection Objects (MOST IMPORTANT)**
```java
// ❌ BAD: Lookup every time
for (int i = 0; i < 100000; i++) {
    Method method = obj.getClass().getMethod("doSomething");
    method.invoke(obj);
}

// ✅ GOOD: Cache once
private static final Method DO_SOMETHING_METHOD;
static {
    try {
        DO_SOMETHING_METHOD = SomeClass.class.getMethod("doSomething");
        DO_SOMETHING_METHOD.setAccessible(true);
    } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
    }
}

for (int i = 0; i < 100000; i++) {
    DO_SOMETHING_METHOD.invoke(obj);
}
```

#### **2. Use setAccessible(true)**
```java
// ❌ SLOW: Security checks every time
field.get(obj);

// ✅ FASTER: Skip security checks
field.setAccessible(true);
field.get(obj);
```

#### **3. Cache Field/Method Lookups**
```java
// ✅ GOOD: Cache in static map
public class ReflectionCache {
    private static final Map<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();
    
    public static Method getMethod(Class<?> clazz, String name, Class<?>... params) 
            throws NoSuchMethodException {
        String key = clazz.getName() + "." + name;
        return METHOD_CACHE.computeIfAbsent(key, k -> {
            try {
                return clazz.getMethod(name, params);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
```

#### **4. Type Checking**
```java
// ❌ BAD: Use Object for everything
Object result = method.invoke(obj);
if (result instanceof Integer) { int val = (Integer) result; }

// ✅ GOOD: Type-safe invocation
int result = (Integer) method.invoke(obj);
```

### **When to Use Reflection**
| **✅ GOOD Use Cases** | **❌ BAD Use Cases** |
|---------------------|---------------------|
| Frameworks (Spring, Hibernate) | Business logic |
| Testing tools (JUnit) | Performance-critical paths |
| Serialization libraries | Hot code paths |
| Dependency Injection | Regular method calls |
| Configuration loading | Everyday programming |
| Annotation processing | Simple POJO operations |
| IDE/Development tools | Database operations |

---

## 🔥 **QUICK REFERENCE TABLES**

### **Method vs getDeclaredMethod**
| Method | Returns | Superclass | Private |
|--------|---------|------------|---------|
| `getMethods()` | Public only | ✅ Yes | ❌ No |
| `getDeclaredMethods()` | All | ❌ No | ✅ Yes |

### **Class Method Patterns**
```java
// For ALL methods (including private, but not inherited)
clazz.getDeclaredMethods()

// For PUBLIC methods (including inherited)
clazz.getMethods()

// For specific private method
clazz.getDeclaredMethod("name", params)

// For specific public method
clazz.getMethod("name", params)
```

### **Field Access Patterns**
```java
// ALL fields (including private)
clazz.getDeclaredFields()
clazz.getDeclaredField("name")

// PUBLIC fields only
clazz.getFields()
clazz.getField("name")
```

---

## 📝 **EXAM PRACTICE TEMPLATES**

### **Template 1: Custom Annotation**
```java
// 1. Define annotation
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface MyAnnotation {
    String value() default "";
    int count() default 0;
}

// 2. Use annotation
@MyAnnotation(value = "test", count = 5)
public void method() { }

// 3. Process annotation
if (method.isAnnotationPresent(MyAnnotation.class)) {
    MyAnnotation ann = method.getAnnotation(MyAnnotation.class);
    String value = ann.value();
    int count = ann.count();
}
```

### **Template 2: Enum with Methods**
```java
public enum Status {
    PENDING("Waiting"), 
    APPROVED("Accepted"), 
    REJECTED("Declined");
    
    private final String description;
    
    Status(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isFinal() {
        return this == APPROVED || this == REJECTED;
    }
}
```

### **Template 3: Reflection Helper**
```java
public class ReflectionHelper {
    public static Object getPrivateField(Object obj, String fieldName) 
            throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
    
    public static void invokePrivateMethod(Object obj, String methodName, 
            Object... args) throws Exception {
        Class<?>[] paramTypes = Arrays.stream(args)
            .map(Object::getClass)
            .toArray(Class[]::new);
        Method method = obj.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        method.invoke(obj, args);
    }
}
```

---

## 🎯 **COMMON EXAM TRICKS**

### **Trick 1: Enum Comparison**
```java
// ❌ Wrong
if (day.equals(Day.MONDAY)) { }

// ✅ Correct (enum constants are singletons)
if (day == Day.MONDAY) { }
```

### **Trick 2: getDeclared vs get Methods**
```java
// Get private method
method = clazz.getDeclaredMethod("privateMethod");  // ✅ Works

// Won't work (getMethod only for public)
method = clazz.getMethod("privateMethod");          // ❌ NoSuchMethodException
```

### **Trick 3: Class.forName() Requires Full Name**
```java
// ❌ Wrong
Class.forName("String");  // ❌ ClassNotFoundException

// ✅ Correct
Class.forName("java.lang.String");
```

### **Trick 4: Reflection with Primitives**
```java
// Array types
Class.forName("[Ljava.lang.String;");  // String[]
Class.forName("[I");                   // int[]

// Primitive types
Class.forName("int");  // ❌ Doesn't work!
int.class;             // ✅ Use .class
Integer.TYPE;          // ✅ Also works
```

---

## 📊 **COMPARISON CHARTS**

### **Enum vs Class vs Interface**
| Feature | Enum | Class | Interface |
|---------|------|-------|-----------|
| Can be instantiated | ❌ (singletons) | ✅ | ❌ |
| Can have fields | ✅ | ✅ | ✅ (static final) |
| Can have methods | ✅ | ✅ | ✅ (abstract/default) |
| Can extend | ❌ | ✅ | ❌ |
| Can implement | ✅ | ✅ | ✅ |
| Singleton guarantee | ✅ | ❌ | ❌ |

### **RetentionPolicy Comparison**
| Policy | Available | Use Case |
|--------|-----------|----------|
| `SOURCE` | Source code only | Code generation, linting |
| `CLASS` | .class file (not runtime) | Bytecode processing |
| `RUNTIME` | Runtime via Reflection | Spring, JUnit, serialization |

---

## 🚨 **ERRORS TO AVOID**

```java
// ❌ ERROR 1: Forgetting setAccessible for private
Field field = clazz.getDeclaredField("privateField");
field.get(obj);  // IllegalAccessException!

// ✅ Fix:
field.setAccessible(true);
field.get(obj);

// ❌ ERROR 2: Wrong parameter types for getMethod
clazz.getMethod("method", "String")  // Wrong

// ✅ Fix:
clazz.getMethod("method", String.class)

// ❌ ERROR 3: Forgetting NoSuchMethodException
try {
    Method m = clazz.getMethod("nonExistent");
} catch (NoSuchMethodException e) { }

// ❌ ERROR 4: Using newInstance() with no default constructor
clazz.newInstance();  // If no default constructor

// ✅ Fix:
Constructor<?> c = clazz.getConstructor(String.class);
c.newInstance("param");

// ❌ ERROR 5: Enum constructor not private
public enum Color { RED; public Color() { } }  // Compile error!

// ✅ Fix: Enum constructors are implicitly private
public enum Color { RED; Color() { } }  // Actually private
```

---

## 💡 **MEMORY AIDS**

### **For Annotations: "R T D I"**
- **R** - RetentionPolicy (SOURCE, CLASS, RUNTIME)
- **T** - Target (where it can be used)
- **D** - Documented (include in JavaDoc)
- **I** - Inherited (subclasses inherit)

### **For Reflection: "C M F C"**
- **C** - Class (get the Class object)
- **M** - Methods (inspect/invoke)
- **F** - Fields (read/write)
- **C** - Constructors (create instances)

### **Performance: "C S T"**
- **C** - Cache reflection objects
- **S** - Use setAccessible(true)
- **T** - Type check carefully

---

## 🎓 **LAST-MINUTE EXAM CHECKLIST**

- [ ] Enums are implicitly final and singleton
- [ ] Enum constructors are always private
- [ ] Use `==` not `equals()` for enum comparison
- [ ] Custom annotations use `@interface`
- [ ] Know all 5 meta-annotations (R, T, D, I, Repeatable)
- [ ] Reflection: `getDeclared` vs `get` methods
- [ ] Always call `setAccessible(true)` for private members
- [ ] Cache Method/Field objects for performance
- [ ] Reflection is SLOW - mention performance impact
- [ ] `Class.forName()` throws ClassNotFoundException
- [ ] Annotations > naming patterns (Item 39)
- [ ] Use `isAnnotationPresent()` before `getAnnotation()`

---
