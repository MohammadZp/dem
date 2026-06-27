# SQL JOINs, Index Basics & EXPLAIN

# SQL JOINs

## INNER JOIN

### What is an INNER JOIN?

An **INNER JOIN** returns **only the rows that have matching values in both tables**.

If there is no matching row in either table, the row is **not included** in the result.

### Example

**Students**

| student_id | name |
|------------|------|
| 1 | Alice |
| 2 | Bob |
| 3 | Charlie |

**Courses**

| student_id | course |
|------------|--------|
| 1 | Java |
| 2 | SQL |

### Query

```sql
SELECT *
FROM Students
INNER JOIN Courses
ON Students.student_id = Courses.student_id;
```

### Result

| student_id | name | course |
|------------|------|--------|
| 1 | Alice | Java |
| 2 | Bob | SQL |

Charlie is **not returned** because there is no matching course.

---

## LEFT JOIN

### What is a LEFT JOIN?

A **LEFT JOIN** returns:

- All rows from the **left table**
- Matching rows from the **right table**
- If there is no match, the right table columns contain **NULL**

### Query

```sql
SELECT *
FROM Students
LEFT JOIN Courses
ON Students.student_id = Courses.student_id;
```

### Result

| student_id | name | course |
|------------|------|--------|
| 1 | Alice | Java |
| 2 | Bob | SQL |
| 3 | Charlie | NULL |

Charlie appears even though no matching course exists.

---

## RIGHT JOIN

### What is a RIGHT JOIN?

A **RIGHT JOIN** returns:

- All rows from the **right table**
- Matching rows from the **left table**
- If there is no match, the left table columns contain **NULL**

### Example

**Students**

| student_id | name |
|------------|------|
| 1 | Alice |
| 2 | Bob |

**Courses**

| student_id | course |
|------------|--------|
| 1 | Java |
| 2 | SQL |
| 3 | Python |

### Query

```sql
SELECT *
FROM Students
RIGHT JOIN Courses
ON Students.student_id = Courses.student_id;
```

### Result

| student_id | name | course |
|------------|------|--------|
| 1 | Alice | Java |
| 2 | Bob | SQL |
| 3 | NULL | Python |

Student 3 appears even though there is no matching student.

---

## FULL OUTER JOIN

### What is a FULL OUTER JOIN?

A **FULL OUTER JOIN** returns:

- All matching rows
- All non-matching rows from the left table
- All non-matching rows from the right table

Missing values are filled with **NULL**.

### Example

**Students**

| student_id | name |
|------------|------|
| 1 | Alice |
| 2 | Bob |
| 3 | Charlie |

**Courses**

| student_id | course |
|------------|--------|
| 1 | Java |
| 4 | Python |

### Query

```sql
SELECT *
FROM Students
FULL OUTER JOIN Courses
ON Students.student_id = Courses.student_id;
```

### Result

| student_id | name | course |
|------------|------|--------|
| 1 | Alice | Java |
| 2 | Bob | NULL |
| 3 | Charlie | NULL |
| 4 | NULL | Python |

---

# JOIN Comparison

| JOIN | Returns |
|------|---------|
| INNER JOIN | Only matching rows |
| LEFT JOIN | All rows from the left table + matching rows from the right table |
| RIGHT JOIN | All rows from the right table + matching rows from the left table |
| FULL OUTER JOIN | All rows from both tables |

---

# JOIN Visualization

## INNER JOIN

```
      A ∩ B
```

Only matching rows.

---

## LEFT JOIN

```
A + (A ∩ B)
```

Everything from the left table plus matches.

---

## RIGHT JOIN

```
(A ∩ B) + B
```

Everything from the right table plus matches.

---

## FULL OUTER JOIN

```
A ∪ B
```

Everything from both tables.

---

# Important Note

**MySQL does not support `FULL OUTER JOIN` directly.**

It can be simulated using `LEFT JOIN`, `RIGHT JOIN`, and `UNION`.

```sql
SELECT *
FROM Students
LEFT JOIN Courses
ON Students.student_id = Courses.student_id

UNION

SELECT *
FROM Students
RIGHT JOIN Courses
ON Students.student_id = Courses.student_id;
```

---

# Index Basics

## What is an Index?

An **index** is a database structure that speeds up searching for rows.

Think of it like the **index in a textbook**—instead of reading every page, you go directly to the page you need.

---

## Why are Indexes Used?

Indexes improve:

- Searching (`WHERE`)
- Sorting (`ORDER BY`)
- Joining tables (`JOIN`)
- Looking up primary keys

---

## Advantages of Indexes

- Faster SELECT queries
- Faster JOIN operations
- Faster ORDER BY operations
- Faster WHERE filtering

---

## Disadvantages of Indexes

- Require additional disk space
- Slow down INSERT operations
- Slow down UPDATE operations
- Slow down DELETE operations

Reason: the index must also be updated whenever data changes.

---

## When Should You Create an Index?

Create indexes on columns that are:

- Frequently searched
- Frequently joined
- Frequently sorted
- Frequently filtered

Examples:

- `student_id`
- `email`
- `username`

---

## When Should You Avoid Indexes?

Avoid indexes on:

- Small tables
- Columns that change frequently
- Columns with very few distinct values (low selectivity)

---

## Primary Key Index

A **PRIMARY KEY** automatically creates a unique index.

Example:

```sql
CREATE TABLE Student(
    student_id INT PRIMARY KEY,
    name VARCHAR(50)
);
```

No additional index is required for the primary key.

---

# EXPLAIN

## What is EXPLAIN?

`EXPLAIN` shows **how the database plans to execute a query**.

It is used to analyze and improve query performance.

---

## Why Use EXPLAIN?

It shows:

- Whether an index is used
- Which tables are accessed
- Join order
- Query execution plan
- Whether a full table scan occurs

---

## Example

```sql
EXPLAIN
SELECT *
FROM Student
WHERE student_id = 5;
```

---

# Table Scan vs Index Scan

## Table Scan

The database checks **every row**.

```
Row 1
Row 2
Row 3
Row 4
...
```

Slower for large tables.

---

## Index Scan

The database first looks in the index.

```
Index

1 → Row 25
2 → Row 81
3 → Row 190
```

Then it jumps directly to the correct row.

Much faster.

---

# High-Priority Exam Questions

## 1. What is an INNER JOIN?

Returns **only matching rows** from both tables.

---

## 2. What is a LEFT JOIN?

Returns **all rows from the left table** and matching rows from the right table. Missing matches become **NULL**.

---

## 3. What is a RIGHT JOIN?

Returns **all rows from the right table** and matching rows from the left table. Missing matches become **NULL**.

---

## 4. What is a FULL OUTER JOIN?

Returns **all rows from both tables**, matching where possible and filling missing values with **NULL**.

---

## 5. Compare INNER, LEFT, RIGHT, and FULL OUTER JOIN.

| INNER | LEFT | RIGHT | FULL OUTER |
|--------|------|--------|------------|
| Matching rows only | All left rows | All right rows | All rows from both tables |

---

## 6. What is an index?

A database structure that speeds up data retrieval.

---

## 7. What are the advantages of indexes?

- Faster searching
- Faster joins
- Faster sorting
- Faster filtering

---

## 8. What are the disadvantages of indexes?

- More storage
- Slower INSERT
- Slower UPDATE
- Slower DELETE

---

## 9. What is EXPLAIN?

`EXPLAIN` displays the query execution plan and helps optimize SQL queries.

---

## 10. Why is EXPLAIN useful?

It shows:

- Index usage
- Table scans
- Join order
- Query execution strategy

---

# Memory Tips

## JOINs

- **INNER** → Matching rows only.
- **LEFT** → Keep everything on the left.
- **RIGHT** → Keep everything on the right.
- **FULL OUTER** → Keep everything from both tables.

---

## Index

Think of the **index in a book**:

Without an index → Read every page.

With an index → Jump directly to the correct page.

---

## EXPLAIN

Think:

> **"Show me how the database will execute my query."**

It tells you:

- Which indexes are used
- Whether a table scan occurs
- Join order
- Execution plan