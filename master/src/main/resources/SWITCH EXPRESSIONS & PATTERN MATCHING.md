# 📚 **COMPLETE CHEAT SHEET: SWITCH EXPRESSIONS & PATTERN MATCHING**

> **Java 17+ Features | Last Updated: 2026**

---

## 📑 **TABLE OF CONTENTS**

1. [Switch Expressions](#-1-switch-expressions)
2. [Pattern Matching - instanceof](#-2-pattern-matching---instanceof)
3. [Pattern Matching - switch](#-3-pattern-matching---switch)
4. [Pattern Guards](#-4-pattern-guards-when-vs-)
5. [Nested & Record Patterns](#-5-nested--record-patterns)
6. [Common Pitfalls](#-6-common-pitfalls)
7. [Quick Reference](#-7-quick-reference)
8. [Exam Practice](#-8-exam-practice-questions)

---

## 🔄 **1. SWITCH EXPRESSIONS**

### **Evolution**

| Feature | Old Switch (Statement) | New Switch (Expression) |
|---------|----------------------|------------------------|
| **Returns value** | ❌ No | ✅ Yes |
| **Break required** | ✅ Yes | ❌ No |
| **Fall-through** | ✅ Default | ❌ Arrow syntax prevents |
| **Exhaustiveness** | ❌ Optional | ✅ Required |
| **Yield keyword** | ❌ No | ✅ Yes |

### **Syntax Comparison**

#### **❌ Old Way (Java < 14)**
```java
String result;
switch (day) {
    case MONDAY:
    case FRIDAY:
        result = "Weekend";
        break;
    case TUESDAY:
        result = "Work";
        break;
    default:
        result = "Unknown";
        break;
}
// Problems: verbose, fall-through, breaks, mutable variable
```

#### **✅ New Way (Java 14+)**
```java
// Arrow syntax (preferred)
String result = switch (day) {
    case MONDAY, FRIDAY, SUNDAY -> "Weekend";
    case TUESDAY -> "Work";
    case WEDNESDAY, THURSDAY -> "Midweek";
    default -> "Unknown";
};
```

### **Syntax Types**

#### **1. Arrow Syntax (`->`)**
```java
// Single expression
int length = switch (day) {
    case MONDAY, FRIDAY, SUNDAY -> 6;
    case TUESDAY -> 7;
    default -> 0;
};

// Block with yield
int length = switch (day) {
    case MONDAY, FRIDAY, SUNDAY -> {
        System.out.println("Weekend");
        yield 6;  // Return value from block
    }
    default -> 0;
};
```

#### **2. Colon Syntax with `yield`**
```java
int length = switch (day) {
    case MONDAY:
    case FRIDAY:
    case SUNDAY:
        yield 6;
    default:
        yield 0;
};
```

### **Exhaustiveness Rules**

```java
// ✅ With enums - no default if all covered
String result = switch (Day) {
    case MONDAY -> "Start";
    case TUESDAY -> "Second";
    case WEDNESDAY -> "Middle";
    case THURSDAY -> "Almost";
    case FRIDAY -> "TGIF";
    case SATURDAY, SUNDAY -> "Weekend";
};  // No default needed

// ✅ With integers - default required
String result = switch (num) {
    case 1 -> "One";
    case 2 -> "Two";
    default -> "Other";  // Required!
};
```

### **Multiple Case Labels**
```java
String result = switch (day) {
    case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> "Weekday";
    case SATURDAY, SUNDAY -> "Weekend";
    default -> "Unknown";
};
```

---

## 🔍 **2. PATTERN MATCHING - instanceof**

### **Evolution**

#### **❌ Old Way (Java < 16)**
```java
if (obj instanceof String) {
    String s = (String) obj;  // Explicit cast needed
    System.out.println(s.length());
}
```

#### **✅ New Way (Java 16+)**
```java
// Basic pattern
if (obj instanceof String s) {
    System.out.println(s.length());  // s is already String
}

// With condition
if (obj instanceof String s && s.length() > 5) {
    System.out.println(s.toUpperCase());
}
```

### **Pattern Variable Scope**

```java
// ✅ In scope in true branch
if (obj instanceof String s && s.length() > 0) {
    System.out.println(s);  // ✅ s in scope
}

// ❌ Not in scope in false branch
if (obj instanceof String s && s.length() > 0) {
    // s in scope
} else {
    // ❌ s NOT in scope
}

// ✅ In scope after negated check
if (!(obj instanceof String s)) {
    return;  // Early return
}
System.out.println(s);  // ✅ s in scope
```

### **Pattern Matching Rules**

```java
// ✅ AND condition - works
if (obj instanceof String s && s.length() > 5) {
    System.out.println(s);
}

// ❌ OR condition - won't compile
if (obj instanceof String s || s.length() > 5) {  // ERROR!
    System.out.println(s);  // s might not be in scope
}
```

---

## 🚀 **3. PATTERN MATCHING - switch**

### **Basic Type Patterns**

```java
String result = switch (obj) {
    case String s -> "String: " + s;
    case Integer i -> "Integer: " + i;
    case Double d -> "Double: " + d;
    case null -> "Null value";
    default -> "Unknown type";
};
```

### **Guarded Patterns (with `when`)**

```java
String describe = switch (obj) {
    case Integer i when i == 0 -> "Zero";
    case Integer i when i > 0 -> "Positive";
    case Integer i when i < 0 -> "Negative";
    case String s when s.length() > 10 -> "Long string";
    case String s -> "Short string";
    case null -> "Null";
    default -> "Unknown";
};
```

### **Pattern Order Matters**

```java
// ❌ WRONG - Unreachable code
String result = switch (obj) {
    case String s -> "String";      // This catches ALL strings
    case String s when s.length() > 5 -> "Long";  // UNREACHABLE!
    default -> "Other";
};

// ✅ CORRECT - Specific patterns first
String result = switch (obj) {
    case String s when s.length() > 5 -> "Long";  // Most specific
    case String s -> "String";                     // More general
    default -> "Other";
};
```

---

## 🛡️ **4. PATTERN GUARDS: `when` vs `&&`**

### **Critical Difference**

| Feature | `when` | `&&` |
|---------|--------|------|
| **Used in** | Switch expressions | instanceof in if |
| **Purpose** | Guard clause for patterns | Logical AND operator |
| **Syntax** | `case Type v when condition` | `if (obj instanceof Type v && condition)` |
| **Java version** | Java 17+ | Java 16+ |

### **Correct Usage**

```java
// ✅ CORRECT - when in switch
String result = switch (obj) {
    case String s when s.length() > 5 -> "Long";
    case Integer i when i > 100 -> "Large";
    default -> "Other";
};

// ✅ CORRECT - && in if
if (obj instanceof String s && s.length() > 5) {
    System.out.println("Long string");
}

// ❌ WRONG - && in switch
switch (obj) {
    case String s && s.length() > 5 -> "Long";  // COMPILE ERROR!
}

// ❌ WRONG - when in if
if (obj instanceof String s when s.length() > 5) {  // COMPILE ERROR!
}
```

### **Combining Multiple Conditions**

```java
// ✅ Multiple conditions in when
String result = switch (obj) {
    case String s when s.length() > 5 && s.startsWith("A") -> "Long A-string";
    case String s when s.length() > 5 -> "Long string";
    case String s when s.matches("\\d+") -> "Number string";
    case String s -> "Regular string";
    default -> "Other";
};

// ✅ Complex logic in when
String result = switch (obj) {
    case Person p when p.age() >= 18 && p.age() <= 65 -> "Working age";
    case Person p when p.age() > 65 -> "Senior";
    case Person p -> "Minor";
    default -> "Unknown";
};
```

---

## 📦 **5. NESTED & RECORD PATTERNS**

### **Record Patterns**

```java
record Point(int x, int y) { }
record Circle(Point center, int radius) { }

// ✅ Basic record pattern
String describe = switch (obj) {
    case Point p -> "Point at (" + p.x() + ", " + p.y() + ")";
    case Circle c -> "Circle at " + c.center() + " radius " + c.radius();
    default -> "Unknown";
};

// ✅ Nested record pattern
String describe = switch (obj) {
    case Point(int x, int y) when x == y -> "Square point";
    case Point(int x, int y) -> "Point at (" + x + ", " + y + ")";
    case Circle(Point(int x, int y), int r) when r > 10 -> "Large circle";
    case Circle(var center, int r) -> "Circle at " + center;
    default -> "Unknown";
};
```

### **Record Patterns in if**

```java
record Person(String name, int age) { }

// ✅ Using record pattern in if
if (obj instanceof Person(String name, int age) && age >= 18) {
    System.out.println(name + " is adult");
}

// ✅ With conditions
if (obj instanceof Person(var name, var age) && age > 65) {
    System.out.println(name + " is senior");
}
```

### **Complex Record Examples**

```java
// Nested records
record Address(String street, String city, String zip) { }
record Employee(String name, int id, Address address) { }

String describe = switch (obj) {
    case Employee(String name, int id, Address addr) when id > 1000 ->
        name + " (senior)";
    case Employee(var name, var id, Address(String street, var city, var zip)) 
        when city.equals("New York") ->
        name + " works in NYC";
    case Employee(var name, var id, var addr) ->
        name + " (ID: " + id + ")";
    default -> "Unknown";
};
```

### **Sealed Classes + Pattern Matching**

```java
sealed interface Shape permits Circle, Rectangle, Square { }
record Circle(double radius) implements Shape { }
record Rectangle(double width, double height) implements Shape { }
record Square(double side) implements Shape { }

double area = switch (shape) {
    case Circle(double r) -> Math.PI * r * r;
    case Rectangle(double w, double h) -> w * h;
    case Square(double s) -> s * s;
    // No default needed - all types covered!
};
```

---

## ⚠️ **6. COMMON PITFALLS**

### **1. Pattern Order**
```java
// ❌ WRONG - Unreachable patterns
String result = switch (obj) {
    case Object o -> "Object";      // Catches EVERYTHING
    case String s -> "String";       // UNREACHABLE!
    default -> "Default";            // UNREACHABLE!
};

// ✅ CORRECT
String result = switch (obj) {
    case String s -> "String";
    case Integer i -> "Integer";
    case Object o -> "Object";       // Most general last
};
```

### **2. Scope Issues**
```java
// ❌ WRONG - Pattern variable not in scope
if (obj instanceof String s || s.length() > 0) {  // ERROR!
    System.out.println(s);
}

// ✅ CORRECT
if (obj instanceof String s && s.length() > 0) {
    System.out.println(s);
}
```

### **3. Null Handling**
```java
// ❌ WRONG - Null not handled
String result = switch (obj) {
    case String s -> "String";
    // Null falls through to default
    default -> "Unknown";  // Null becomes "Unknown"
};

// ✅ CORRECT - Explicit null handling
String result = switch (obj) {
    case String s -> "String";
    case null -> "Null";  // Explicit null case
    default -> "Unknown";
};
```

### **4. Yield vs Return**
```java
// ❌ WRONG - Can't use return in switch expression
String result = switch (day) {
    case MONDAY -> {
        return "Weekend";  // ERROR!
    }
    default -> "Work";
};

// ✅ CORRECT - Use yield
String result = switch (day) {
    case MONDAY -> {
        yield "Weekend";  // Correct
    }
    default -> "Work";
};
```

### **5. Type Casting**
```java
// ❌ WRONG - Pattern variable conflicts
switch (obj) {
    case String s when s.length() > 5 -> {
        String s = "Hello";  // ERROR! s already defined
        yield s;
    }
    default -> "Other";
}

// ✅ CORRECT - Use different variable name
switch (obj) {
    case String s when s.length() > 5 -> {
        String result = "Hello";  // Different name
        yield result;
    }
    default -> "Other";
}
```

---

## 📝 **7. QUICK REFERENCE**

### **Switch Expression Cheatsheet**

```java
// ===== ARROW SYNTAX =====
String result = switch (value) {
    case A, B -> "Group 1";
    case C -> {
        // complex logic
        yield "Group 2";
    }
    default -> "Other";
};

// ===== COLON + YIELD =====
String result = switch (value) {
    case A:
    case B:
        yield "Group 1";
    default:
        yield "Other";
};

// ===== PATTERN MATCHING =====
String result = switch (obj) {
    case String s -> "String: " + s;
    case Integer i -> "Int: " + i;
    case null -> "Null";
    default -> "Other";
};

// ===== GUARDED PATTERNS =====
String result = switch (obj) {
    case String s when s.length() > 5 -> "Long";
    case String s -> "Short";
    case Integer i when i < 0 -> "Negative";
    default -> "Other";
};

// ===== RECORD PATTERNS =====
String result = switch (obj) {
    case Point(int x, int y) when x == y -> "Square";
    case Point(int x, int y) -> "Point";
    default -> "Other";
};
```

### **Pattern Matching Comparison**

| Feature | `instanceof` | `switch` |
|---------|-------------|----------|
| **Multiple types** | Multiple if-else | Single switch |
| **Guards** | `&&` condition | `when` clause |
| **Exhaustiveness** | No | Yes |
| **Null handling** | Separate check | `case null` |
| **Records** | Nested pattern | Nested pattern |
| **Readability** | Good for 1-2 types | Great for many types |

### **Syntax Rules Summary**

```java
// ✅ ALLOWED
if (obj instanceof String s && s.length() > 5) { }
if (obj instanceof Point(int x, int y) && x > y) { }

switch (obj) {
    case String s when s.length() > 5 -> "Long";
    case Point(int x, int y) when x > y -> "X bigger";
}

// ❌ NOT ALLOWED
if (obj instanceof String s when s.length() > 5) { }  // Use &&
switch (obj) {
    case String s && s.length() > 5 -> "Long";        // Use when
}
```

---

## 🎓 **8. EXAM PRACTICE QUESTIONS**

### **Q1: Convert to Switch Expression**

**Convert this to switch expression:**
```java
String status;
if (score >= 90) {
    status = "A";
} else if (score >= 80) {
    status = "B";
} else if (score >= 70) {
    status = "C";
} else {
    status = "F";
}
```

**Answer:**
```java
String status = switch (score / 10) {
    case 10, 9 -> "A";
    case 8 -> "B";
    case 7 -> "C";
    default -> "F";
};
```

### **Q2: Identify the Output**

```java
Object[] items = {"Hello", 42, 3.14, null, "Java"};
for (Object item : items) {
    String result = switch (item) {
        case String s when s.length() > 4 -> "Long string: " + s;
        case String s -> "Short string: " + s;
        case Integer i -> "Integer: " + i;
        case Double d -> "Double: " + d;
        case null -> "Null";
        default -> "Unknown";
    };
    System.out.println(result);
}
```

**Answer:**
```
Long string: Hello
Integer: 42
Double: 3.14
Null
Long string: Java
```

### **Q3: Fix the Errors**

```java
// Find and fix the errors
String process(Object obj) {
    return switch (obj) {
        case String s && s.length() > 10 -> "Long";    // Error 1
        case Integer i && i < 0 -> "Negative";          // Error 2
        case String s when s.isEmpty() -> "Empty";      // OK
        case null -> "Null";
        default -> "Other";
    };
}
```

**Answer:**
```java
String process(Object obj) {
    return switch (obj) {
        case String s when s.length() > 10 -> "Long";   // Fixed
        case Integer i when i < 0 -> "Negative";        // Fixed
        case String s when s.isEmpty() -> "Empty";
        case null -> "Null";
        default -> "Other";
    };
}
```

### **Q4: Complex Pattern Matching**

```java
// Write a switch expression that:
// 1. Checks if String starts with "A" and length > 5
// 2. Checks if String contains "Java"
// 3. Checks if Integer is between 1 and 100
// 4. Handles null and other types

String result = switch (obj) {
    case String s when s.startsWith("A") && s.length() > 5 -> "A-string long";
    case String s when s.contains("Java") -> "Contains Java";
    case Integer i when i >= 1 && i <= 100 -> "Between 1 and 100";
    case Integer i -> "Other number";
    case null -> "Null";
    default -> "Unknown";
};
```

### **Q5: Record Pattern Challenge**

```java
// Given these records:
record Person(String name, int age) { }
record Address(String street, String city) { }
record Employee(Person person, Address address, double salary) { }

// Write a switch expression that:
// 1. Identifies employees with salary > 100000
// 2. Identifies employees from "New York"
// 3. Identifies employees under 25

String describe = switch (obj) {
    case Employee(Person(String name, int age), Address addr, double salary) 
            when salary > 100000 -> name + " (high earner)";
    case Employee(Person(String name, int age), Address(String street, String city), double salary) 
            when city.equals("New York") -> name + " (NYC)";
    case Employee(Person(String name, int age), Address addr, double salary) 
            when age < 25 -> name + " (young)";
    case Employee(var person, var addr, var salary) -> person.name() + " (employee)";
    default -> "Not an employee";
};
```

---

## 🎯 **KEY TAKEAWAYS**

### **Switch Expressions:**
- ✅ Returns a value (expression, not statement)
- ✅ Arrow syntax `->` prevents fall-through
- ✅ `yield` returns value from block
- ✅ Must be exhaustive (all cases covered)
- ✅ No explicit break needed

### **Pattern Matching - instanceof:**
- ✅ Combines type check and cast in one
- ✅ Pattern variable in scope in true branch
- ✅ Use `&&` for additional conditions
- ✅ Nested patterns for records

### **Pattern Matching - switch:**
- ✅ Matches on type, not just value
- ✅ Guards with `when` for conditions
- ✅ Record patterns for deconstruction
- ✅ Exhaustive checking
- ✅ Null handling with `case null`

### **Remember This Rule:**
```
📌 In Java 17+ Pattern Matching:

- if (obj instanceof Type v && condition) → Use &&
- switch (obj) { case Type v when condition → Use when }

They are NOT interchangeable!
```

---

## 📚 **JAVA VERSION SUPPORT**

| Feature | Java Version |
|---------|--------------|
| Switch Expressions | Java 14 (preview), Java 16+ (final) |
| Pattern Matching instanceof | Java 16 |
| Pattern Matching switch | Java 17 (preview), Java 21+ (final) |
| Record Patterns | Java 19 (preview), Java 21+ (final) |
| Sealed Classes | Java 17 |

---