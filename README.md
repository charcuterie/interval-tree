# interval-tree

## Introduction

This is an implementation of a red-black interval-tree for half-open integer
intervals. Details can be found either explicitly or as exercises in
[Introduction to Algorithms](https://mitpress.mit.edu/books/introduction-algorithms).

## Why write this?

The short story is that I needed a data structure to represent a collection of
gene annotations that would allow efficient retrieval of all annotations
overlapping a given interval.

## Why not use another implementation?

There is a lot of debate on the Internet about which sort of implementation is
"best": top-down vs bottom-up, 2-3 vs 2-3-4, etc. Whatever the negatives of the
CLRS implementation may be, the benefit is that clear, thorough documentation
can be found in any university library.

Additionally, I wanted an implementation that

1. was well documented
2. was written in a "Java" style (e.g., EnumSets instead of bitfields)
3. returned Optionals instead of nulls
4. did not expose the underlying node structure
5. was tested
6. handled Objects which implemented an Interval interface rather than pairs of
ints

None of the implementations I found online hit all six points, so I wrote my
own.
