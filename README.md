# interval-tree

## Introduction

This project consists of two classes: IntervalTree and IntervalSetTree.

IntervalTree is an implementation of a red-black interval-tree for half-open
integer intervals. Details can be found either explicitly or as exercises in
[Introduction to Algorithms](https://mitpress.mit.edu/books/introduction-algorithms).
It has the basic functionality one would expect from an interval-tree:
insertion, deletion, and overlap query.

IntervalSetTree is a modification of IntervalTree which handles Intervals with
the same start and end coordinates, but which differ in some other way, for
example, a unique name field. Rather than contain a single Interval, a Node
in an IntervalSetTree contains a Set of Intervals, all with the same
coordinates. The functionality is otherwise the same, except that many methods
will return an Iterator<? extends Interval> rather than an
Optional<? extends Interval> since multiple values can be returned.

## Why write this?

The short story is that I needed a data structure to represent a collection of
gene annotations in the manner that the IntervalSetTree does. I don't know of
any implementation online that does this. The simpler IntervalTree is included
with the hope that others may find it helpful, since it

1. is documented
2. doesn't have public methods that return null
3. doesn't expose the underlying node structure
4. is tested

## Why not use another implementation?

There is a lot of debate on the Internet about which sort of implementation is
"best": top-down vs bottom-up, 2-3 vs 2-3-4, etc. Whatever the negatives of the
CLRS implementation may be, the benefit is that clear, thorough documentation
can be found in any university library.

## How do I use this?

Using this classes should be straightforward. In the following examples, Impl
implements the Interval interface.

### Creating an empty tree

Creating trees is done through the class constructors.

Empty tree:

```java
IntervalTree<Impl> tree = new IntervalTree<>();
```

One-element tree:

```java
IntervalTree<Impl> tree = new IntervalTree<>(new Impl(1, 100));
```

### Adding intervals

```java
tree.insert(new Impl(3, 10));
```

This method returns a boolean if the value was added (that is, no duplicate
found), so feel free to do something like

```java
if (tree.insert(interval)) {
  celebrate(goodTimes);
} else {
  cry();
}
```

### Querying the tree

Querying the tree is simply

```java
tree.contains(interval)

```

If you're looking for, say, the maximum value

```java
Impl max = tree.maximum()
               .orElseThrow(() -> new SomeTypeOfException("cant find the max!"));
```

You can also iterate through the tree

```java
for (Impl i : tree) {
  System.out.println(i.toString);
}
```

```java
tree.iterator()
    .forEachRemaining( x -> System.out.println(x.toString()) );
```

```java
Iterator<Impl> i = tree.iterator();
while (i.hasNext()) {
  System.out.println((i.next()).toString());
}
```

Overlap queries are pretty much the same.

```java
tree.overlappers(someInterval)
    .forEachRemaining( x -> System.out.println(x.toString()) );
```

### Removing intervals

Removing intervals is as you might guess.

```java
if (tree.delete(someInterval)) {
  System.out.println("Get outta here!");
}
```

```java
if (tree.deleteOverlappers(someInterval)) {
  System.out.println("All y'all get outta here!");
}
```
