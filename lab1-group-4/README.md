# Lab 1: Binary search

This short lab has two purposes:
* to introduce you to the lab system,
* to practice versions of binary search.

### Important notes

* This lab is part of the examination of the course.
  Therefore, you must not copy code from or show code to other students.
  You are welcome to discuss general ideas with one another, but anything you do must be **your own work**.

* This is the **Java version** of the lab.
  To switch to the Python version, [see here](https://chalmers.instructure.com/courses/27979/pages/the-lab-system#switching-language).

* Further info on Canvas:
  - [general information](https://chalmers.instructure.com/courses/27979/pages/labs-general-information)
  - [the lab system](https://chalmers.instructure.com/courses/27979/pages/the-lab-system)
  - [running Java labs](https://chalmers.instructure.com/courses/27979/pages/running-python-labs)


## Overview

Binary search is a highly efficient way to find an item in a *sorted* array.
It works by looking at increasingly smaller *ranges* of the array (where each range is given by a low index and a high index, or a start index and an end index).
Initiality, the range is the entire array.
In each step, we compare the given item with the middle element of the current range:
* If the item is smaller, it can only be in the left side of the range.
* If the item is larger, it can only be in the right side of the range.

We continue with this, if necessary until the range is empty.

In this lab, you will implement two kinds of binary search in [BinarySearch.java](BinarySearch.java) (without looking up how to do it):
* a basic one that just checks if the item is in the array,
* an advanced one that finds the *first position* where the item occurs.

Both kinds should use as few comparisons as possible.
For the basic kind, you should implement one *iterative* and one *recursive* version.
For the advanced kind, you can decide if you want to make it iterative or recursive.

Finally, you will answer a few questions in `answers.txt`.


## Part 1: Checking for a matching element

Your first task is to implement the following two functions:

```java
public static<V extends Comparable<? super V>> boolean containsIterative(V[] array, V value) {
    [...]
}

public static<V extends Comparable<? super V>> boolean containsRecursive(V[] array, V value) {
    [...]
}
```

Assume that all arguments and array values are non-null and that `array` is sorted according to the natural ordering (i.e., smaller items come first).

These functions are *generic* in the type `V` of the array elements, as long as they are `Comparable`.
That means they can be called with `V` substituted by any concrete type, for example numbers (`Integer`) or strings (`String`) (but not primitive types such as `int`).
When writing these functions, we do not know anything about `V`, so we have to treat it as a black box.

### Different approaches

There are two main approaches for implementing binary search:

* the *iterative style*: using a loop,
* the *recursive style*: using a helper function that calls itself.

We want you to experiment with both styles.
So implement `containsIterative` and `containsRecursive` in the respective style.

**Note**:
If you are stuck on the recursive approach, have a look at the [recursion practice](https://chalmers.instructure.com/courses/23356/pages/reading-material#recursion-practice).
If you are really stuck, here are some hints (click to see):

<details>
<summary>Spoiler 1</summary>

Do you have an iterative solution (using a loop)?
Try to convert it into a recursive solution by turning the loop into a recursive helper function.
</details>

<details>
<summary>Spoiler 2</summary>

For the recursive solution, you will have to add a helper function.
This function should take the same arguments as the original function, except for some extra arguments that change in the recursive call.
</details>

<details>
<summary>Spoiler 3</summary>

Try to give the recursive helper function two extra arguments `lo` and `hi`  (or `start` and `end`).
These tell you which range of the array to search in.
How does the helper function call itself?
</details>

### Comparing two values

To compare values `x` and `y`, we use the method `compareTo` of the interface `Comparable`:

```java
int cmp = x.compareTo(y);
```

This has the following meaning:
* If c < 0, then x is **less than** y.
* If c = 0, then x is **equal to** y.
* If c > 0, then x is **greater than** y.

Since we assume the array is sorted, smaller array elements come before larger array elements.

### Requirements

* You are not allowed to call existing search methods on the array or look up code for binary search.
  You have to implement your own binary search!

* We consider values `x` and `y` equal if `x.compareTo(y)` returns 0.
  Do not use `x.equals(y)` or `x == y` (reference equality).

* All functions should use as few comparisons as possible for a given array size.
  Note that the array may contain lots of duplicate elements.


## Part 2: Find the first matching index

Now you should implement the following function:

```java
public static<V extends Comparable<? super V>> int firstIndexOf(V[] array, V value) {
    [...]
}
```

It should return the **first** index whose array element matches the given search value.
If such an index does not exist, it should return -1.

For example, calling `firstIndexOf` on the array [1, 1, 3, 3, 5, 5] with search value 3 should return 2 because the first occurrence of 3 in the array is at index 2.
But if we call it with search value 7, it should return -1.

You can decide for yourself if you want to implement it iteratively or recursively.
The requirements above still apply.

### Testing

* The class `BinarySearch` has a main function with a few tests, using `assert`.
  To use them, run Java with assertion checks enabled (e.g., `java -ea BinarySearch`).
  Feel free to add your own tests.

* The debugger is very useful for finding out where your function goes wrong.
  It allows you to go through your function line-by-line and see how the variables change.
  If you have not used a debugger before, grab a teaching assistant to teach you.

* [Robograder](https://chalmers.instructure.com/courses/27979/pages/the-lab-system#robograder) will run some pretty comprehensive tests on your code.
  Make sure to use it before you submit.


## Part 3: Test your implementation

Run some tests to figure out the following question:
*How many comparisons does `firstIndexOf` use *at most* for an array of...*
- *10 elements?*
- *100 elements?*
- *1,000 elements?*
- *1,000,000 elements?*

Write down your answers in **answers.txt**.

*Hints*:
* If you use a debugger to through a program run line-by-line, you can count the number of comparisons.
* Alternatively, you can (temporarily) insert print-statements, or increase a counter.
  (But make sure to remove those before submitting your solution!)


## Part 4: Reason about your implementation

Calculate the answer to the following question:
*How many comparisons would `firstIndexOf` need for 1,000,000,000,000 elements?*
(This is way too big for your computer's memory!)

Write down your answer and a justification in **answers.txt**.


## Submission

Double check:
* Have you answered all questions in **answers.txt**?
  (don't forget the ones in the appendix)
* Have you tested your code with Robograder?

[How to submit](https://chalmers.instructure.com/courses/27979/pages/the-lab-system#submit-a-lab).
