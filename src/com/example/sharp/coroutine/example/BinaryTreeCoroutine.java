package com.example.sharp.coroutine.example;

import java.util.Comparator;
import java.util.Iterator;

import com.example.sharp.coroutine.Coroutine;

public class BinaryTreeCoroutine {

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
		normalTraversal(tree.root);
		Iterator<TreeNode<Integer, Integer>> iter = getTravesor(tree);
		while (iter.hasNext()) {
			System.err.printf("%s\n", iter.next());
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