# Generator 语法糖实现 / Generator Syntax Sugar Implementation

## 概述 / Overview

本实现提供了类似 Python/C# 的生成器语法糖，让您可以像写普通循环代码一样编写生成器，而不需要手动构建协程指令。

This implementation provides Python/C#-like generator syntax sugar, allowing you to write generators like normal loop code without manually building coroutine instructions.

## 使用方法 / Usage

### 方法1: YieldHelper.generate() - 简单语法糖

这是最简单的方式，类似 Python 的 yield：

This is the simplest way, similar to Python's yield:

```java
@Generator
public static Iterable<Integer> range(int start, int end) {
    return YieldHelper.generate((yield) -> {
        for (int i = start; i <= end; i++) {
            yield.accept(i);  // 等同于 Python 的 yield i
        }
    });
}
```

**使用 / Usage:**
```java
for (Integer i : range(1, 10)) {
    System.out.println(i);
}
```

### 方法2: YieldHelper.generateWithCoroutine() - 高级控制流

当需要协程的完整控制流时使用：

Use when you need full coroutine control flow:

```java
@Generator
public static Iterable<Integer> advancedRange(int start, int end) {
    return YieldHelper.generateWithCoroutine((cor, yield) -> {
        Var<Integer> i = new Var<>(start);
        cor.For(
            (ctx) -> i.set(start),
            () -> i.get() <= end,
            (ctx) -> i.set(i.get() + 1)
        ).run((ctx) -> {
            yield.yieldInInstruction(i.get());
        });
    });
}
```

## 与 Aparapi @Kernel 的对比

### Aparapi 方式:

```java
@Kernel
public void compute(float[] input, float[] output) {
    int i = getGlobalId();
    output[i] = input[i] * 2;
}
```

### 我们的 @Generator 方式:

```java
@Generator
public static Iterable<Integer> generate() {
    return YieldHelper.generate((yield) -> {
        for (int i = 0; i < 10; i++) {
            yield.accept(i * 2);
        }
    });
}
```

## 实现原理 / Implementation Details

### YieldHelper.generate() 工作原理:

1. **接收 lambda 表达式** - 包含 yield 调用的代码
2. **在构建时执行** - Lambda 立即执行，构建协程指令
3. **收集 yield 调用** - 每个 yield.accept() 添加一个协程指令
4. **启动协程** - 所有指令添加完成后启动
5. **返回迭代器** - 转换为标准 Java Iterator

### 与手动方式的对比:

**传统方式 (手动构建):**
```java
public static Iterable<Integer> range(int start, int end) {
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

**新方式 (语法糖):**
```java
public static Iterable<Integer> range(int start, int end) {
    return YieldHelper.generate((yield) -> {
        for (int i = start; i <= end; i++) {
            yield.accept(i);
        }
    });
}
```

## 常见模式 / Common Patterns

### 1. 简单迭代 / Simple Iteration

```java
@Generator
public static Iterable<String> words() {
    return YieldHelper.generate((yield) -> {
        yield.accept("Hello");
        yield.accept("World");
        yield.accept("!");
    });
}
```

### 2. 条件 yield / Conditional Yield

```java
@Generator
public static Iterable<Integer> evens(int max) {
    return YieldHelper.generate((yield) -> {
        for (int i = 0; i <= max; i++) {
            if (i % 2 == 0) {
                yield.accept(i);
            }
        }
    });
}
```

### 3. 嵌套循环 / Nested Loops

```java
@Generator
public static Iterable<String> coordinates() {
    return YieldHelper.generate((yield) -> {
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                yield.accept(x + "," + y);
            }
        }
    });
}
```

### 4. 过滤和转换 / Filter and Transform

```java
@Generator
public static <T> Iterable<T> filter(Iterable<T> source, Predicate<T> pred) {
    return YieldHelper.generate((yield) -> {
        for (T item : source) {
            if (pred.test(item)) {
                yield.accept(item);
            }
        }
    });
}
```

### 5. 无限序列 / Infinite Sequence

```java
@Generator
public static Iterable<Long> counter(long start) {
    return YieldHelper.generateWithCoroutine((cor, yield) -> {
        Var<Long> count = new Var<>(start);
        cor.addInstruction("loop", (ctx) -> {
            yield.yieldInInstruction(count.get());
            count.set(count.get() + 1);
            ctx.jmp("loop");
        });
    });
}
```

## 注意事项 / Caveats

### ⚠️ 变量捕获问题

**错误做法:**
```java
return YieldHelper.generate((yield) -> {
    for (int i = 0; i < 10; i++) {
        yield.accept(i);  // ❌ i 的值在指令执行时可能不正确
    }
});
```

**正确做法:**
```java
return YieldHelper.generate((yield) -> {
    for (int i = 0; i < 10; i++) {
        final int value = i;
        yield.accept(value);  // ✅ 捕获 final 变量
    }
});
```

### ⚠️ 何时使用高级方式

对于需要 break/continue 或复杂控制流的情况，使用 `generateWithCoroutine()`:

```java
return YieldHelper.generateWithCoroutine((cor, yield) -> {
    Var<Integer> i = new Var<>(0);
    cor.For(init, cond, step).run((ctx) -> {
        if (someCondition) {
            ctx.doBreak();  // 使用协程的 break
            return;
        }
        yield.yieldInInstruction(i.get());
    });
});
```

## 完整示例对比 / Complete Example Comparison

### Python 生成器:
```python
def fibonacci(n):
    a, b = 0, 1
    for _ in range(n):
        yield a
        a, b = b, a + b
```

### C# 生成器:
```csharp
public static IEnumerable<BigInteger> Fibonacci(int n) {
    BigInteger a = 0, b = 1;
    for (int i = 0; i < n; i++) {
        yield return a;
        var temp = a + b;
        a = b;
        b = temp;
    }
}
```

### 我们的 Java 生成器:
```java
@Generator
public static Iterable<BigInteger> fibonacci(int n) {
    return YieldHelper.generate((yield) -> {
        BigInteger a = BigInteger.ZERO;
        BigInteger b = BigInteger.ONE;
        for (int i = 0; i < n; i++) {
            yield.accept(a);
            BigInteger temp = a.add(b);
            a = b;
            b = temp;
        }
    });
}
```

## 性能考虑 / Performance Considerations

- ✅ **编译时开销**: 仅在方法调用时构建一次协程
- ✅ **运行时开销**: 与手动构建的协程相同
- ✅ **内存占用**: 仅存储协程指令，不存储所有值
- ⚠️ **适用场景**: 适合逐个生成值，不适合需要随机访问的场景

## 未来改进 / Future Improvements

### 可能的增强功能:

1. **编译时注解处理器** - 真正的语法转换
   ```java
   @Generator(syntaxSugar = true)
   public static Iterable<Integer> range(int start, int end) {
       for (int i = start; i <= end; i++) {
           yield(i);  // 直接使用 yield 关键字
       }
   }
   ```

2. **字节码增强** - 类似 Lombok
   - 使用 ASM 或 ByteBuddy
   - 在编译后修改字节码

3. **IDE 插件支持** - 语法高亮和代码补全

## 总结 / Summary

`YieldHelper` 提供了：

✅ **简化的语法** - 更接近 Python/C# 的 yield 语法  
✅ **零学习曲线** - 像写普通循环一样写生成器  
✅ **类型安全** - 完全的泛型支持  
✅ **灵活性** - 支持简单和高级两种模式  
✅ **兼容性** - 与现有协程系统完全兼容  

这是目前 Java 中最接近 @Kernel 风格语法糖的实现方式！
