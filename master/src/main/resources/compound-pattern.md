
# 1. Adapter Pattern

### Problem

You have an existing class, but its interface doesn't match what your application expects.

```java
interface Duck {
    void quack();
    void fly();
}
```

Existing class:

```java
class Turkey {
    void gobble() {
        System.out.println("Gobble!");
    }

    void fly() {
        System.out.println("Turkey flying");
    }
}
```

`Turkey` doesn't implement `Duck`.

### Solution: Adapter

```java
class TurkeyAdapter implements Duck {

    private final Turkey turkey;

    public TurkeyAdapter(Turkey turkey) {
        this.turkey = turkey;
    }

    @Override
    public void quack() {
        turkey.gobble();
    }

    @Override
    public void fly() {
        turkey.fly();
    }
}
```

Now:

```java
Duck duck = new TurkeyAdapter(new Turkey());

duck.quack();
duck.fly();
```

Output:

```text
Gobble!
Turkey flying
```

### Mental model

```text
Turkey
   ↓
Adapter
   ↓
Duck interface
   ↓
Client
```

**Exam sentence:**

> Adapter converts the interface of an existing class into an interface expected by the client.

---

# 2. Decorator Pattern

### Problem

You want to add behavior to an object **without modifying its original class**.

Suppose:

```java
interface Coffee {
    double cost();
    String description();
}
```

Basic coffee:

```java
class SimpleCoffee implements Coffee {

    @Override
    public double cost() {
        return 2.0;
    }

    @Override
    public String description() {
        return "Coffee";
    }
}
```

Now we want:

```text
Coffee + Milk
Coffee + Milk + Sugar
Coffee + Milk + Sugar + WhippedCream
```

Instead of creating:

```text
CoffeeWithMilk
CoffeeWithMilkAndSugar
CoffeeWithMilkAndSugarAndCream
...
```

we use Decorator.

```java
abstract class CoffeeDecorator implements Coffee {

    protected final Coffee coffee;

    protected CoffeeDecorator(Coffee coffee) {
        this.coffee = coffee;
    }
}
```

Milk:

```java
class MilkDecorator extends CoffeeDecorator {

    public MilkDecorator(Coffee coffee) {
        super(coffee);
    }

    @Override
    public double cost() {
        return coffee.cost() + 0.5;
    }

    @Override
    public String description() {
        return coffee.description() + ", Milk";
    }
}
```

Sugar:

```java
class SugarDecorator extends CoffeeDecorator {

    public SugarDecorator(Coffee coffee) {
        super(coffee);
    }

    @Override
    public double cost() {
        return coffee.cost() + 0.2;
    }

    @Override
    public String description() {
        return coffee.description() + ", Sugar";
    }
}
```

Usage:

```java
Coffee coffee =
        new SugarDecorator(
                new MilkDecorator(
                        new SimpleCoffee()
                )
        );

System.out.println(coffee.description());
System.out.println(coffee.cost());
```

Output:

```text
Coffee, Milk, Sugar
2.7
```

### Mental model

```text
SugarDecorator
      ↓
MilkDecorator
      ↓
SimpleCoffee
```

Each decorator **wraps** another object.

**Exam sentence:**

> Decorator dynamically adds responsibilities to an object without modifying its class.

---

# 3. Abstract Factory

### Problem

Suppose you're building a UI.

You want different families of components:

```text
Windows UI
 ├── WindowsButton
 └── WindowsCheckbox

Mac UI
 ├── MacButton
 └── MacCheckbox
```

You don't want client code to directly instantiate concrete classes.

First:

```java
interface Button {
    void paint();
}

interface Checkbox {
    void paint();
}
```

Concrete products:

```java
class WindowsButton implements Button {
    public void paint() {
        System.out.println("Windows Button");
    }
}

class WindowsCheckbox implements Checkbox {
    public void paint() {
        System.out.println("Windows Checkbox");
    }
}

class MacButton implements Button {
    public void paint() {
        System.out.println("Mac Button");
    }
}

class MacCheckbox implements Checkbox {
    public void paint() {
        System.out.println("Mac Checkbox");
    }
}
```

Factory:

```java
interface GUIFactory {
    Button createButton();
    Checkbox createCheckbox();
}
```

Windows factory:

```java
class WindowsFactory implements GUIFactory {

    public Button createButton() {
        return new WindowsButton();
    }

    public Checkbox createCheckbox() {
        return new WindowsCheckbox();
    }
}
```

Mac factory:

```java
class MacFactory implements GUIFactory {

    public Button createButton() {
        return new MacButton();
    }

    public Checkbox createCheckbox() {
        return new MacCheckbox();
    }
}
```

Client:

```java
class Application {

    private final Button button;
    private final Checkbox checkbox;

    public Application(GUIFactory factory) {
        this.button = factory.createButton();
        this.checkbox = factory.createCheckbox();
    }

    void render() {
        button.paint();
        checkbox.paint();
    }
}
```

Usage:

```java
GUIFactory factory = new WindowsFactory();

Application app = new Application(factory);

app.render();
```

### Why "Abstract Factory"?

Because you're creating a **family of related objects**.

```text
GUIFactory
    │
    ├── createButton()
    └── createCheckbox()
```

**Exam sentence:**

> Abstract Factory provides an interface for creating families of related objects without specifying their concrete classes.

---

# 4. Composite Pattern

### Problem

Imagine a file system:

```text
Folder
 ├── File
 ├── File
 └── Folder
      ├── File
      └── File
```

We want the client to treat:

```text
File
```

and

```text
Folder
```

through the same interface.

```java
interface FileSystemItem {
    void print();
}
```

Leaf:

```java
class File implements FileSystemItem {

    private final String name;

    public File(String name) {
        this.name = name;
    }

    @Override
    public void print() {
        System.out.println("File: " + name);
    }
}
```

Composite:

```java
class Folder implements FileSystemItem {

    private final String name;
    private final List<FileSystemItem> children = new ArrayList<>();

    public Folder(String name) {
        this.name = name;
    }

    public void add(FileSystemItem item) {
        children.add(item);
    }

    @Override
    public void print() {
        System.out.println("Folder: " + name);

        for (FileSystemItem child : children) {
            child.print();
        }
    }
}
```

Usage:

```java
Folder root = new Folder("root");

root.add(new File("a.txt"));
root.add(new File("b.txt"));

Folder src = new Folder("src");
src.add(new File("Main.java"));
src.add(new File("User.java"));

root.add(src);

root.print();
```

Output:

```text
Folder: root
File: a.txt
File: b.txt
Folder: src
File: Main.java
File: User.java
```

The important thing is:

```java
FileSystemItem item;
```

can represent either:

```text
File       ← Leaf
Folder     ← Composite
```

### Mental model

```text
             FileSystemItem
              /          \
           File         Folder
                         /  \
                      File  Folder
```

**Exam sentence:**

> Composite lets clients treat individual objects and compositions of objects uniformly.

---

# 5. Observer Pattern

### Problem

Suppose a `WeatherStation` changes its temperature.

We want multiple objects to automatically receive updates.

```text
WeatherStation
      │
      ├──── Display
      ├──── MobileApp
      └──── Logger
```

Observer interface:

```java
interface Observer {
    void update(double temperature);
}
```

Subject:

```java
interface Subject {
    void registerObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers();
}
```

Implementation:

```java
class WeatherStation implements Subject {

    private final List<Observer> observers = new ArrayList<>();
    private double temperature;

    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(temperature);
        }
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
        notifyObservers();
    }
}
```

Observer:

```java
class MobileApp implements Observer {

    @Override
    public void update(double temperature) {
        System.out.println(
            "Mobile temperature: " + temperature
        );
    }
}
```

Usage:

```java
WeatherStation station = new WeatherStation();

station.registerObserver(new MobileApp());

station.setTemperature(25);
station.setTemperature(30);
```

Output:

```text
Mobile temperature: 25
Mobile temperature: 30
```

### Important concept

The WeatherStation doesn't need to know:

```text
"Who is MobileApp?"
"How does MobileApp work?"
```

It only knows:

```java
Observer
```

That's the key to **loose coupling**.

**Exam sentence:**

> Observer defines a one-to-many dependency so that when one object's state changes, all its dependents are notified automatically.

---

# Now connect them to Compound Patterns

This is the part I'd focus on for the exam.

Imagine our Duck Simulator:

```text
                         Duck Simulator
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
     Adapter              Decorator             Factory
        │                     │                     │
   adapt objects         add behavior         create objects
                              │
                              │
                          Composite
                              │
                       group Ducks together
                              │
                              ▼
                          Observer
                              │
                         notify observers
```

The important insight is:

**We aren't choosing one pattern.**

We're solving different parts of the same larger problem:

| Pattern              | Main problem it solves              |
| -------------------- | ----------------------------------- |
| **Adapter**          | Incompatible interface              |
| **Decorator**        | Add behavior dynamically            |
| **Abstract Factory** | Create families of related objects  |
| **Composite**        | Treat individual + groups uniformly |
| **Observer**         | Notify dependent objects of changes |

And **together**, they form a **Compound Pattern**.

### Exam trick

If they ask:

> "Is MVC a design pattern?"

A good answer is:

**Yes, MVC is commonly described as a compound pattern because it combines multiple patterns.**

For example:

```text
MVC
│
├── Model
│    └── Observer
│
├── View
│    └── Composite
│
└── Controller
     └── Strategy
```

That's exactly why Chapter 12 moves from individual patterns toward **architectural patterns built from multiple patterns**.

Next, the most important thing to study is **how these patterns actually combine inside the Duck Simulator**, because that's where exam questions can get tricky.
