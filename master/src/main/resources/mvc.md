## 1. Model → Observer

The Model contains the application's state.

```java
interface Observer {
    void update(int value);
}

class CounterModel {

    private final List<Observer> observers = new ArrayList<>();
    private int value = 0;

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void increment() {
        value++;
        notifyObservers();
    }

    public int getValue() {
        return value;
    }

    private void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(value);
        }
    }
}
```

The important part is:

```java
model.increment();
        ↓
notifyObservers();
        ↓
View.update(value);
```

The Model **doesn't directly know about the View**.

It only knows:

```java
Observer
```

So we have:

```text
Model
  │
  │ notifies
  ↓
Observer
  │
  ├── View 1
  ├── View 2
  └── View 3
```

That's the **Observer Pattern**.

---

# 2. View → Composite

Now imagine our UI isn't just one component.

It contains:

```text
CounterView
 ├── Label
 ├── Button
 └── Panel
      ├── Label
      └── Button
```

We can use Composite.

First, common interface:

```java
interface UIComponent {
    void render();
}
```

Leaf components:

```java
class Label implements UIComponent {

    private final String text;

    public Label(String text) {
        this.text = text;
    }

    @Override
    public void render() {
        System.out.println("Label: " + text);
    }
}
```

```java
class Button implements UIComponent {

    private final String text;

    public Button(String text) {
        this.text = text;
    }

    @Override
    public void render() {
        System.out.println("Button: " + text);
    }
}
```

Composite:

```java
class Panel implements UIComponent {

    private final List<UIComponent> children = new ArrayList<>();

    public void add(UIComponent component) {
        children.add(component);
    }

    @Override
    public void render() {
        for (UIComponent child : children) {
            child.render();
        }
    }
}
```

Now our View can contain multiple components:

```java
class CounterView implements Observer {

    private final Panel root = new Panel();

    public CounterView() {
        root.add(new Label("Counter App"));
        root.add(new Button("Increase"));
    }

    @Override
    public void update(int value) {
        System.out.println("Counter value = " + value);
    }

    public void render() {
        root.render();
    }
}
```

So:

```text
View
 │
 └── Panel
      ├── Label
      └── Button
```

This is **Composite**.

---

# 3. Controller → Strategy

Now the Controller needs to decide **what action to perform** when the user interacts with the application.

We can define:

```java
interface UserAction {
    void execute(CounterModel model);
}
```

Different strategies:

```java
class IncrementAction implements UserAction {

    @Override
    public void execute(CounterModel model) {
        model.increment();
    }
}
```

Another strategy:

```java
class ResetAction implements UserAction {

    @Override
    public void execute(CounterModel model) {
        // reset logic
    }
}
```

Controller:

```java
class CounterController {

    private final CounterModel model;

    public CounterController(CounterModel model) {
        this.model = model;
    }

    public void handle(UserAction action) {
        action.execute(model);
    }
}
```

Now the controller doesn't need:

```java
if (action == INCREMENT) {
    ...
} else if (action == RESET) {
    ...
}
```

Instead:

```java
controller.handle(new IncrementAction());
```

or:

```java
controller.handle(new ResetAction());
```

That's **Strategy**.

---

# 4. Put everything together

Now we can see the actual MVC architecture:

```text
                  USER
                   │
                   │ clicks
                   ▼
             ┌─────────────┐
             │ Controller  │
             │  Strategy   │
             └──────┬──────┘
                    │
                    │ execute()
                    ▼
             ┌─────────────┐
             │    Model    │
             │             │
             │  Observer   │
             └──────┬──────┘
                    │
                    │ notify
                    ▼
             ┌─────────────┐
             │    View     │
             │             │
             │  Composite  │
             └─────────────┘
```

And the Java usage:

```java
public class Main {

    public static void main(String[] args) {

        // Model
        CounterModel model = new CounterModel();

        // View
        CounterView view = new CounterView();

        // Model -> View
        model.addObserver(view);

        // Controller
        CounterController controller =
                new CounterController(model);

        // User clicks "Increase"
        controller.handle(new IncrementAction());

        // Render UI
        view.render();
    }
}
```

The important chain is:

```text
User
 ↓
Controller
 ↓
Strategy
 ↓
Model
 ↓
Observer
 ↓
View
 ↓
Composite
```
