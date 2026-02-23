package com.example.sharp.coroutine.example;

import com.example.events.Var;
import com.example.sharp.Delegates;
import com.example.sharp.annotations.Generator;
import com.example.sharp.coroutine.Coroutine;
import com.example.sharp.coroutine.GeneratorBuilder;

import java.math.BigInteger;

/**
 * Example showing how to convert recursive algorithms to generators
 * using the new generator pattern.
 */
public class RecursiveToGeneratorExample {

    /**
     * Traditional recursive factorial (for reference)
     */
    public static BigInteger factorialRecursive(int n) {
        if (n <= 1) {
            return BigInteger.ONE;
        }
        return BigInteger.valueOf(n).multiply(factorialRecursive(n - 1));
    }
    
    /**
     * Factorial as a generator - yields all intermediate results
     * This demonstrates converting a recursive calculation to an iterative generator
     */
    @Generator(returnType = BigInteger.class)
    public static Delegates.IterableEx<BigInteger> factorialGenerator(int max) {
        return GeneratorBuilder.fromBody((ctx) -> {
            Var<BigInteger> result = new Var<>(BigInteger.ONE);
            Var<Integer> i = new Var<>(0);
            
            // Yield 0! = 1
            ctx.yield(result.get());
            
            // Use coroutine For loop to iterate
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
    
    /**
     * Fibonacci sequence generator using iterative approach
     * Converts what could be recursive into an efficient generator
     */
    @Generator(returnType = BigInteger.class)
    public static Delegates.IterableEx<BigInteger> fibonacciGenerator(int count) {
        return GeneratorBuilder.fromBody((ctx) -> {
            Var<BigInteger> a = new Var<>(BigInteger.ZERO);
            Var<BigInteger> b = new Var<>(BigInteger.ONE);
            Var<Integer> i = new Var<>(0);
            
            ctx.For(
                (cor) -> i.set(0),
                () -> i.get() < count,
                (cor) -> i.set(i.get() + 1)
            ).run((cor) -> {
                cor.yield(a.get());
                BigInteger temp = a.get().add(b.get());
                a.set(b.get());
                b.set(temp);
            });
        });
    }
    
    /**
     * Tree traversal generator - converts recursive tree walk to generator
     */
    @Generator
    public static <T> Delegates.IterableEx<T> treeTraversalGenerator(TreeNode<T> root) {
        Coroutine cor = new Coroutine();
        traverseTree(cor, root);
        cor.start();
        return cor.iterable();
    }
    
    /**
     * Helper method to build tree traversal coroutine recursively
     */
    private static <T> void traverseTree(Coroutine cor, TreeNode<T> node) {
        if (node == null) {
            return;
        }
        
        cor.addInstruction((ctx) -> {
            // Visit left subtree
            Coroutine left = ctx.push();
            traverseTree(left, node.left);
        });
        
        cor.addInstruction((ctx) -> {
            // Visit current node
            ctx.yield(node.value);
        });
        
        cor.addInstruction((ctx) -> {
            // Visit right subtree
            Coroutine right = ctx.push();
            traverseTree(right, node.right);
        });
    }
    
    /**
     * Simple tree node for demonstration
     */
    public static class TreeNode<T> {
        public T value;
        public TreeNode<T> left;
        public TreeNode<T> right;
        
        public TreeNode(T value) {
            this.value = value;
        }
        
        public TreeNode(T value, TreeNode<T> left, TreeNode<T> right) {
            this.value = value;
            this.left = left;
            this.right = right;
        }
    }
    
    /**
     * Power set generator - generates all subsets of a set
     * Demonstrates converting recursive backtracking to generator
     * Returns arrays of indices representing which elements are included
     */
    @Generator
    public static Delegates.IterableEx<int[]> powerSetGenerator(int n) {
        Coroutine cor = new Coroutine();
        Var<Integer> i = new Var<>(0);
        int totalSubsets = 1 << n; // 2^n
        
        cor.For(
            (ctx) -> i.set(0),
            () -> i.get() < totalSubsets,
            (ctx) -> i.set(i.get() + 1)
        ).run((ctx) -> {
            int current = i.get();
            int size = Integer.bitCount(current);
            
            int[] subset = new int[size];
            int idx = 0;
            
            for (int j = 0; j < n; j++) {
                if ((current & (1 << j)) != 0) {
                    subset[idx++] = j;
                }
            }
            
            ctx.yield(subset);
        });
        
        cor.start();
        return cor.iterable();
    }
    
    /**
     * Permutation generator - generates all permutations
     * Shows conversion of recursive backtracking to generator
     */
    @Generator
    public static Delegates.IterableEx<Integer[]> permutationGenerator(int n) {
        Coroutine cor = new Coroutine();
        Integer[] arr = new Integer[n];
        for (int i = 0; i < n; i++) {
            arr[i] = i;
        }
        
        buildPermutations(cor, arr, 0);
        cor.start();
        return cor.iterable();
    }
    
    /**
     * Helper for permutation generation - builds coroutine instructions recursively
     */
    private static void buildPermutations(Coroutine cor, Integer[] arr, int index) {
        if (index == arr.length - 1) {
            cor.addInstruction((ctx) -> ctx.yield(arr.clone()));
            return;
        }
        
        for (int i = index; i < arr.length; i++) {
            final int fi = i;
            cor.addInstruction((ctx) -> {
                swap(arr, index, fi);
            });
            
            buildPermutations(cor, arr, index + 1);
            
            cor.addInstruction((ctx) -> {
                swap(arr, index, fi); // backtrack
            });
        }
    }
    
    private static void swap(Integer[] arr, int i, int j) {
        Integer temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
    
    /**
     * Main method demonstrating all generators
     */
    public static void main(String[] args) {
        System.out.println("=== Factorial Generator ===");
        int count = 0;
        for (BigInteger fact : factorialGenerator(10)) {
            System.out.printf("%d! = %s\n", count++, fact);
        }
        
        System.out.println("\n=== Fibonacci Generator ===");
        count = 0;
        for (BigInteger fib : fibonacciGenerator(10)) {
            System.out.printf("fib(%d) = %s\n", count++, fib);
        }
        
        System.out.println("\n=== Tree Traversal Generator ===");
        TreeNode<Integer> tree = new TreeNode<>(5,
            new TreeNode<>(3,
                new TreeNode<>(1),
                new TreeNode<>(4)
            ),
            new TreeNode<>(8,
                new TreeNode<>(6),
                new TreeNode<>(9)
            )
        );
        
        System.out.print("In-order traversal: ");
        for (Integer value : treeTraversalGenerator(tree)) {
            System.out.print(value + " ");
        }
        System.out.println();
        
        System.out.println("\n=== Power Set Generator (n=3, first 8) ===");
        count = 0;
        for (int[] subset : powerSetGenerator(3)) {
            if (count++ >= 8) break;
            System.out.print("{ ");
            for (int num : subset) {
                System.out.print(num + " ");
            }
            System.out.println("}");
        }
        
        System.out.println("\n=== Permutation Generator (n=3) ===");
        for (Integer[] perm : permutationGenerator(3)) {
            System.out.print("[ ");
            for (Integer num : perm) {
                System.out.print(num + " ");
            }
            System.out.println("]");
        }
    }
}
