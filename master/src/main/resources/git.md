Absolutely. Here are the **3 graphs you should memorize for the exam**.

---

# 1. Rebase workflow — `rebase --onto`

Start:

```text
A---B---C---D        main
     \
      E---F          feature
           \
            G---H    topic
```

Suppose `topic` accidentally started from `feature`, but you want **G and H to be based directly on `main`**.

Run:

```bash
git rebase --onto main feature topic
```

Meaning:

```text
                    ┌── NEW BASE
                    ↓
git rebase --onto main feature topic
                         ↑       ↑
                       OLD BASE  BRANCH
```

Git takes:

```text
G---H
```

and replays them onto `main`:

```text
A---B---C---D        main
     \
      E---F          feature

             G'---H' topic
            /
           D
```

### Remember

```text
--onto = "take this range of commits and put it somewhere else"
```

---

# 2. Merge `--no-ff`

Start:

```text
A---B---C        main
     \
      D---E---F  feature
```

Run:

```bash
git checkout main
git merge --no-ff feature
```

Result:

```text
          D---E---F
         /         \
A---B---C-----------M    main
```

`M` is a **merge commit**.

Normally Git could do:

```text
A---B---C---D---E---F
```

because it can fast-forward.

But:

```bash
--no-ff
```

says:

> **"Don't fast-forward. Make a merge commit."**

### Remember

```text
--no-ff = force a merge commit
```

---

# 3. Merge `--squash`

Start:

```text
A---B---C        main
     \
      D---E---F  feature
```

Run:

```bash
git checkout main
git merge --squash feature
git commit
```

Result:

```text
A---B---C---S    main
     \
      D---E---F  feature
```

`S` contains the **combined changes** from D, E and F.

Notice:

```text
D---E---F
```

are **not merged into main as individual commits**.

Instead:

```text
D + E + F
    ↓
    S
```

### Remember

```text
--squash = combine changes into one commit
```

And you have to run:

```bash
git commit
```

yourself.

---

# ⭐ Put all three next to each other

### `rebase --onto`

```text
BEFORE:

A---B---C---D        main
     \
      E---F          feature
           \
            G---H    topic


COMMAND:

git rebase --onto main feature topic


AFTER:

A---B---C---D        main
     \
      E---F          feature
             \
              G'---H' topic
```

**Moves/replays a range of commits onto another base.**

---

### `merge --no-ff`

```text
BEFORE:

A---B---C        main
     \
      D---E---F  feature


COMMAND:

git merge --no-ff feature


AFTER:

          D---E---F
         /         \
A---B---C-----------M
```

**Keeps the branch and creates a merge commit.**

---

### `merge --squash`

```text
BEFORE:

A---B---C        main
     \
      D---E---F  feature


COMMAND:

git merge --squash feature
git commit


AFTER:

A---B---C---S    main
     \
      D---E---F  feature
```

**Combines the feature's changes into one normal commit.**

---

## 🧠 Exam cheat sheet

| Operation        | What happens?                             |
| ---------------- | ----------------------------------------- |
| `rebase --onto`  | **Move a range of commits to a new base** |
| `merge --no-ff`  | **Force a merge commit**                  |
| `merge --squash` | **Combine changes into one commit**       |

The easiest visual memory:

```text
REBASE --ONTO

G---H  ──────────→  G'---H'
                     ↑
                   new base


MERGE --NO-FF

D---E---F
 \     /
  \   /
   M


MERGE --SQUASH

D + E + F
    ↓
    S
```

If you understand those **three graphs**, you have the core of this exam section.
