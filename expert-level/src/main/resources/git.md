# Git Interactive Rebase & Merge Strategies

## Interactive Rebase (`git rebase -i`)

### What is Git Rebase?

`git rebase` reapplies commits from one branch onto another base commit, creating a **linear commit history**.

Unlike `git merge`, rebase **rewrites commit history**.

---

### What is Interactive Rebase?

Interactive Rebase (`git rebase -i`) allows you to:

* Edit commits
* Reorder commits
* Rename commit messages
* Squash multiple commits into one
* Remove commits
* Split commits

It is mainly used to **clean up commit history** before sharing code.

---

### Why use Interactive Rebase?

* Clean commit history
* Combine small commits
* Improve commit messages
* Remove unnecessary commits
* Reorder commits logically

---

### Start an Interactive Rebase

```bash
git rebase -i HEAD~3
```

This rebases the **last 3 commits** interactively.

---

### Interactive Rebase Commands

| Command  | Purpose                                                            |
| -------- | ------------------------------------------------------------------ |
| `pick`   | Keep the commit unchanged                                          |
| `reword` | Change the commit message                                          |
| `edit`   | Modify the commit                                                  |
| `squash` | Combine with the previous commit and edit the message              |
| `fixup`  | Combine with the previous commit and discard this commit's message |
| `drop`   | Remove the commit                                                  |

---

### Difference Between `squash` and `fixup`

| Squash                                                      | Fixup                                                  |
| ----------------------------------------------------------- | ------------------------------------------------------ |
| Combines commits and lets you edit the final commit message | Combines commits but keeps the previous commit message |

---

### Advantages of Interactive Rebase

* Produces a clean Git history.
* Makes commits easier to understand.
* Improves code reviews.
* Removes unnecessary commits.

---

### Disadvantages

Interactive Rebase **rewrites Git history**, so it should **not** be used on commits that have already been shared with others unless everyone agrees.

---

# Merge Strategies

## What is Git Merge?

`git merge` combines changes from one branch into another.

---

## Fast-Forward Merge

A Fast-Forward merge occurs when the target branch has **not changed** since the feature branch was created.

Git simply moves the branch pointer forward.

### Before

```text
main:
A --- B

feature:
      C --- D
```

### After

```text
A --- B --- C --- D
```

No merge commit is created.

---

## Non-Fast-Forward Merge (`--no-ff`)

### Command

```bash
git merge --no-ff feature
```

### Purpose

Forces Git to create a **merge commit**, even when a Fast-Forward merge is possible.

### Advantages

* Preserves branch history.
* Clearly shows where a feature branch was merged.
* Easier to track features.

---

## Squash Merge (`--squash`)

### Command

```bash
git merge --squash feature
```

### Purpose

Combines **all commits** from a feature branch into **one new commit** before merging.

### Advantages

* Cleaner commit history.
* Avoids many small commits.
* Produces one meaningful commit.

---

# Comparison

## Normal Merge vs Squash Merge

| Normal Merge              | Squash Merge                  |
| ------------------------- | ----------------------------- |
| Preserves all commits     | Combines all commits into one |
| Keeps branch history      | Simplifies history            |
| May create a merge commit | Creates one new commit        |

---

## `--no-ff` vs `--squash`

| `--no-ff`                 | `--squash`                                  |
| ------------------------- | ------------------------------------------- |
| Creates a merge commit    | Creates one squashed commit                 |
| Preserves branch history  | Does not preserve individual commit history |
| Keeps all commits visible | Combines all commits into one               |

---

## Merge vs Rebase

| Merge                          | Rebase                              |
| ------------------------------ | ----------------------------------- |
| Combines branches              | Reapplies commits onto another base |
| Preserves branch history       | Creates a linear history            |
| Usually creates a merge commit | Does not create a merge commit      |
| Does not rewrite history       | Rewrites commit history             |

---

# High-Priority Exam Questions

## 1. What is Interactive Rebase?

Interactive Rebase (`git rebase -i`) is a Git feature that allows developers to edit, reorder, squash, rename, or remove commits before integrating them into another branch.

---

## 2. Why is Interactive Rebase used?

It is used to clean up commit history by:

* Reordering commits
* Combining commits
* Editing commit messages
* Removing unnecessary commits

---

## 3. What is the purpose of `git merge --no-ff`?

`--no-ff` forces Git to create a merge commit, preserving the history of the merged feature branch.

---

## 4. What is the purpose of `git merge --squash`?

`--squash` combines all commits from a feature branch into a single commit before merging.

---

## 5. What is the difference between `--no-ff` and `--squash`?

* **`--no-ff`** preserves the feature branch history by creating a merge commit.
* **`--squash`** creates one clean commit and does **not** preserve the individual commits from the feature branch.

---

# Memory Tips

## Interactive Rebase

Think:

> **"Clean my commits before sharing."**

You can:

* ✏️ Edit commits
* 🔄 Reorder commits
* 📦 Squash commits
* 📝 Rename commit messages
* 🗑️ Remove commits

---

## Merge Strategies

* **Fast-Forward** → No merge commit, just move the branch pointer.
* **`--no-ff`** → Always create a merge commit to preserve branch history.
* **`--squash`** → Combine all commits into one clean commit.

---

# Quick Revision

## Interactive Rebase

* `git rebase -i`
* Edit commits
* Reorder commits
* Squash commits
* Rename commit messages
* Remove commits
* Rewrites history

## Merge Strategies

* **Fast-Forward** → No merge commit
* **`--no-ff`** → Merge commit + preserve history
* **`--squash`** → One clean commit + simplified history
