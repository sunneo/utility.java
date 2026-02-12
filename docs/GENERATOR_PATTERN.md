# Coroutine and Generator Optimization

This document describes the improvements made to the coroutine system and the new generator annotation pattern.

## Overview

The coroutine system has been enhanced with:

1. **@Generator Annotation**: Mark methods as generators for easy conversion to iterators
2. **GeneratorBuilder**: Helper class for building generators with a fluent API
3. **Enhanced Control Flow**: Added If-Else blocks to complement existing For, While, and Foreach loops
4. **Improved Examples**: Demonstrates converting recursive algorithms to generators

## @Generator Annotation

The `@Generator` annotation marks methods that can yield values and be used as iterators.

```java
@Generator(returnType = Integer.class)
public static Delegates.IterableEx<Integer> range(int start, int end) {
    return GeneratorBuilder.fromBody((ctx) -> {
        Var<Integer> i = new Var<>(start);
        ctx.For(
            (cor) -> i.set(start),
            () -> i.get() <= end,
            (cor) -> i.set(i.get() + 1)
        ).run((cor) -> {
            cor.yield(i.get());
        });
    });
}
```

### Using Generators

```java
for (Integer num : range(1, 10)) {
    System.out.println(num);
}
```

## GeneratorBuilder

The `GeneratorBuilder` class provides a fluent API for creating generators:

### Simple Generator

```java
Delegates.IterableEx<Integer> numbers = GeneratorBuilder.fromBody((ctx) -> {
    ctx.yield(1);
    ctx.yield(2);
    ctx.yield(3);
});
```

### Generator with Loops

```java
Delegates.IterableEx<Integer> evens = GeneratorBuilder.fromBody((ctx) -> {
    Var<Integer> i = new Var<>(0);
    ctx.For(
        (cor) -> i.set(0),
        () -> i.get() < 100,
        (cor) -> i.set(i.get() + 2)
    ).run((cor) -> {
        cor.yield(i.get());
    });
});
```

## Control Flow

### For Loops

```java
Var<Integer> i = new Var<>(0);
coroutine.For(
    (ctx) -> i.set(0),      // init
    () -> i.get() < 10,      // condition
    (ctx) -> i.set(i.get() + 1)  // step
).run((ctx) -> {
    ctx.yield(i.get());
});
```

### While Loops

```java
Var<Integer> count = new Var<>(0);
coroutine.While(() -> count.get() < 10).run((ctx) -> {
    ctx.yield(count.get());
    count.set(count.get() + 1);
});
```

### Foreach Loops

```java
List<String> items = Arrays.asList("a", "b", "c");
coroutine.Foreach(items).run((ctx, item) -> {
    ctx.yield(item);
});
```

### If-Else Blocks

```java
coroutine.If(() -> condition)
    .then((ctx) -> {
        // then branch
    })
    .Else((ctx) -> {
        // else branch
    });
```

## Converting Recursive Algorithms to Generators

### Factorial Generator

Traditional recursive:
```java
public static BigInteger factorial(int n) {
    if (n <= 1) return BigInteger.ONE;
    return BigInteger.valueOf(n).multiply(factorial(n - 1));
}
```

As generator (yields intermediate results):
```java
@Generator
public static Delegates.IterableEx<BigInteger> factorialGenerator(int max) {
    return GeneratorBuilder.fromBody((ctx) -> {
        Var<BigInteger> result = new Var<>(BigInteger.ONE);
        Var<Integer> i = new Var<>(0);
        
        ctx.yield(result.get()); // 0! = 1
        
        ctx.For(
            (cor) -> i.set(1),
            () -> i.get() <= max,
            (cor) -> i.set(i.get() + 1)
        ).run((cor) -> {
            result.set(result.get().multiply(BigInteger.valueOf(i.get())));
            cor.yield(result.get());
        });
    });
}
```

### Tree Traversal Generator

Traditional recursive:
```java
void traverse(TreeNode node) {
    if (node == null) return;
    traverse(node.left);
    process(node);
    traverse(node.right);
}
```

As generator:
```java
@Generator
public static <T> Delegates.IterableEx<T> treeTraversal(TreeNode<T> root) {
    Coroutine cor = new Coroutine();
    buildTraversal(cor, root);
    cor.start();
    return cor.iterable();
}

private static <T> void buildTraversal(Coroutine cor, TreeNode<T> node) {
    if (node == null) return;
    
    cor.addInstruction((ctx) -> {
        Coroutine left = ctx.push();
        buildTraversal(left, node.left);
    });
    
    cor.addInstruction((ctx) -> ctx.yield(node.value));
    
    cor.addInstruction((ctx) -> {
        Coroutine right = ctx.push();
        buildTraversal(right, node.right);
    });
}
```

## Examples

### Range Generator

```java
for (Integer i : range(1, 10)) {
    System.out.println(i); // Prints 1 through 10
}
```

### Fibonacci Generator

```java
for (BigInteger fib : fibonacciGenerator(10)) {
    System.out.println(fib); // Prints first 10 Fibonacci numbers
}
```

### Filter and Map

```java
Delegates.IterableEx<Integer> evens = filter(range(1, 20), x -> x % 2 == 0);
Delegates.IterableEx<Integer> squares = map(range(1, 10), x -> x * x);
```

## Best Practices

1. **Use Var for mutable state**: Coroutine instructions execute at different times, so use `Var<T>` to hold state that changes across instructions.

2. **Yield in separate instructions**: Each `yield()` call should be in its own instruction for proper coroutine suspension.

3. **Build instructions at construction time**: When creating generators, set up all instructions before calling `start()`.

4. **Use GeneratorBuilder for simple cases**: For straightforward generators, `GeneratorBuilder.fromBody()` provides a clean API.

5. **Use direct Coroutine for complex cases**: For recursive algorithms or complex control flow, build the Coroutine instructions directly.

## Implementation Notes

- Coroutines use an instruction-based execution model
- Each instruction is executed sequentially until `yield()` suspends execution
- Nested coroutines (via `push()`) allow for recursive algorithm conversion
- The `iterator()` method converts a coroutine to a standard Java Iterator

## See Also

- `RecursiveToGeneratorExample.java`: Examples of converting recursive algorithms
- `GeneratorAnnotationExample.java`: Examples using the @Generator annotation pattern
- `BinaryTreeCoroutine.java`: Tree traversal with multiple traversal modes
- `CoroutineLoopExample.java`: Nested loop examples
