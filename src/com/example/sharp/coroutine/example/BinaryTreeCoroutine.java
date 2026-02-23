package com.example.sharp.coroutine.example;

import com.example.sharp.Delegates;
import com.example.sharp.annotations.Generator;
import com.example.sharp.coroutine.Coroutine;
import com.example.sharp.coroutine.GeneratorBuilder;

import java.util.Comparator;
import java.util.Iterator;

public class BinaryTreeCoroutine {

	/**
	 * Original traversor implementation (kept for reference)
	 */
	public static Iterator<TreeNode<Integer, Integer>> getTravesor(Tree<Integer, Integer> tree) {
		return new Iterator<TreeNode<Integer, Integer>>() {

			Coroutine traverseCoroutine(Coroutine current, TreeNode<Integer, Integer> node) {
				if (node == null) {
					return current;
				}
				Coroutine cor = null;
				if (current == null) {
					cor = new Coroutine();
				} else {
					cor = current.push();
				}
				cor.addInstruction((pthis) -> {
					traverseCoroutine(pthis, node.left);
				});

				cor.addInstruction((pthis) -> {
					pthis.yield(node);
				});
				cor.addInstruction((pthis) -> {
					traverseCoroutine(pthis, node.right);
				});

				cor.start();
				return cor;
			}

			Coroutine cor = traverseCoroutine(null, tree.root);

			@Override
			public boolean hasNext() {

				return !cor.isStopped();
			}

			@Override
			public TreeNode<Integer, Integer> next() {
				if (!cor.isYield()) {
					while (cor.exec())
						;
				}

				TreeNode<Integer, Integer> ret = cor.getYieldValue();
				// prepare next state
				while (cor.exec())
					;
				return ret;
			}

		};
	}
	
	/**
	 * Improved tree traversal using generator pattern
	 * This converts the recursive algorithm to a generator more elegantly
	 */
	@Generator
	public static <K, V> Delegates.IterableEx<TreeNode<K, V>> traverseInOrder(Tree<K, V> tree) {
		Coroutine cor = buildInOrderTraversal(null, tree.root);
		return cor.iterable();
	}
	
	/**
	 * Helper method to build tree traversal coroutine
	 * Converts recursive in-order traversal to coroutine instructions
	 */
	private static <K, V> Coroutine buildInOrderTraversal(Coroutine current, TreeNode<K, V> node) {
		if (node == null) {
			return current;
		}
		
		Coroutine cor = (current == null) ? new Coroutine() : current.push();
		
		// Left subtree
		cor.addInstruction((ctx) -> {
			buildInOrderTraversal(ctx, node.left);
		});
		
		// Current node
		cor.addInstruction((ctx) -> {
			ctx.yield(node);
		});
		
		// Right subtree
		cor.addInstruction((ctx) -> {
			buildInOrderTraversal(ctx, node.right);
		});
		
		cor.start();
		return cor;
	}
	
	/**
	 * Pre-order traversal generator
	 */
	@Generator
	public static <K, V> Delegates.IterableEx<TreeNode<K, V>> traversePreOrder(Tree<K, V> tree) {
		Coroutine cor = buildPreOrderTraversal(null, tree.root);
		return cor.iterable();
	}
	
	private static <K, V> Coroutine buildPreOrderTraversal(Coroutine current, TreeNode<K, V> node) {
		if (node == null) {
			return current;
		}
		
		Coroutine cor = (current == null) ? new Coroutine() : current.push();
		
		// Current node first
		cor.addInstruction((ctx) -> {
			ctx.yield(node);
		});
		
		// Left subtree
		cor.addInstruction((ctx) -> {
			buildPreOrderTraversal(ctx, node.left);
		});
		
		// Right subtree
		cor.addInstruction((ctx) -> {
			buildPreOrderTraversal(ctx, node.right);
		});
		
		cor.start();
		return cor;
	}
	
	/**
	 * Post-order traversal generator
	 */
	@Generator
	public static <K, V> Delegates.IterableEx<TreeNode<K, V>> traversePostOrder(Tree<K, V> tree) {
		Coroutine cor = buildPostOrderTraversal(null, tree.root);
		return cor.iterable();
	}
	
	private static <K, V> Coroutine buildPostOrderTraversal(Coroutine current, TreeNode<K, V> node) {
		if (node == null) {
			return current;
		}
		
		Coroutine cor = (current == null) ? new Coroutine() : current.push();
		
		// Left subtree
		cor.addInstruction((ctx) -> {
			buildPostOrderTraversal(ctx, node.left);
		});
		
		// Right subtree
		cor.addInstruction((ctx) -> {
			buildPostOrderTraversal(ctx, node.right);
		});
		
		// Current node last
		cor.addInstruction((ctx) -> {
			ctx.yield(node);
		});
		
		cor.start();
		return cor;
	}

	static void normalTraversal(TreeNode<Integer, Integer> t) {
		if (t == null)
			return;
		normalTraversal(t.left);
		System.err.printf("normalTraversal:%d->%d\n", t.key, t.value);
		normalTraversal(t.right);
	}

	public static void main(String[] argv) {
		Tree<Integer, Integer> tree = new Tree<Integer, Integer>((o1, o2) -> o1.compareTo(o2));
		int[] seq = new int[] { 3, 1, 2, 5, 4, 8, 6, 7, 10, 9, 0, 42, 27, -50, -1024 };
		for (int i = 0; i < seq.length; ++i) {
			tree.set(seq[i], i);
		}
		
		System.err.println("=== Traditional Recursive Traversal ===");
		normalTraversal(tree.root);
		
		System.err.println("\n=== Original Coroutine Iterator ===");
		Iterator<TreeNode<Integer, Integer>> iter = getTravesor(tree);
		while (iter.hasNext()) {
			System.err.printf("%s\n", iter.next());
		}
		
		System.err.println("\n=== In-Order Generator ===");
		for (TreeNode<Integer, Integer> node : traverseInOrder(tree)) {
			System.err.printf("In-Order: %s\n", node);
		}
		
		System.err.println("\n=== Pre-Order Generator ===");
		for (TreeNode<Integer, Integer> node : traversePreOrder(tree)) {
			System.err.printf("Pre-Order: %s\n", node);
		}
		
		System.err.println("\n=== Post-Order Generator ===");
		for (TreeNode<Integer, Integer> node : traversePostOrder(tree)) {
			System.err.printf("Post-Order: %s\n", node);
		}
	}
}

class TreeNode<K extends Object, T extends Object> {
	public K key;
	public T value;
	public TreeNode<K, T> left;
	public TreeNode<K, T> right;

	public K getKey() {
		return key;
	}

	public T getValue() {
		return value;
	}

	public TreeNode(K key, T value) {
		this.key = key;
		this.value = value;
	}

	public String toString() {
		return String.format("[%s,%s]", key, value);
	}
}

class Tree<K extends Object, T extends Object> {
	public TreeNode<K, T> root;
	public Comparator<K> comparator;

	public Tree(Comparator<K> comparator) {
		this.comparator = comparator;
	}

	public void set(K key, T value) {

		if (root == null) {
			root = new TreeNode<K, T>(key, value);
		} else {
			TreeNode<K, T> parent = root;
			TreeNode<K, T> current = parent;
			int compareResult = 0;

			while (true) {
				TreeNode<K, T> next = null;
				compareResult = comparator.compare(key, current.key);

				if (compareResult < 0) {
					next = current.left;
				} else if (compareResult > 0) {
					next = current.right;
				} else {
					current.value = value;
					return;
				}
				if (next != null) {
					current = next;
					parent = current;
				} else {
					break;
				}
			}
			if (compareResult < 0) {
				parent.left = new TreeNode<K, T>(key, value);
			} else {
				parent.right = new TreeNode<K, T>(key, value);
			}
		}
	}
}