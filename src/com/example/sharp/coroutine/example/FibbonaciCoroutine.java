package com.example.sharp.coroutine.example;

import java.math.BigInteger;
import java.util.Iterator;

import com.example.sharp.coroutine.Coroutine;

public class FibbonaciCoroutine implements Iterator<BigInteger> {
	BigInteger[] bucket = { new BigInteger("1"), new BigInteger("1"), new BigInteger("0") };
	int idx = 2;
	Coroutine coroutine = new Coroutine((pthis) -> {

		pthis.addInstruction("_label1", (me) -> {
			int p1 = idx - 2;
			int p2 = idx - 1;
			if (p1 < 0)
				p1 += 3;
			if (p2 < 0)
				p2 += 3;
			bucket[idx] = bucket[p1].add(bucket[p2]);
			idx = (idx + 1) % bucket.length;

			me.yield(bucket[idx]);

		});
		// goto
		pthis.addInstruction((me) -> {
			me.jmp("_label1");
		});
		pthis.start();
	});

	@Override
	public boolean hasNext() {
		return !coroutine.isStopped();
	}

	@Override
	public BigInteger next() {
		while (coroutine.exec())
			;
		return coroutine.getYieldValue();
	}

	public static void main(String[] argv) {
		FibbonaciCoroutine cor = new FibbonaciCoroutine();
		for (int i = 0; i < 100 && cor.hasNext(); ++i) {
			System.out.printf("%d ", cor.next());
		}
	}

}
