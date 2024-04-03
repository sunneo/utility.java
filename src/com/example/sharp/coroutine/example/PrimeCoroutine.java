package com.example.sharp.coroutine.example;

import com.example.sharp.coroutine.Coroutine;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.Vector;

public class PrimeCoroutine implements Iterator<BigInteger> {
	Vector<BigInteger> primes = new Vector<BigInteger>();

	final BigInteger TWO = new BigInteger("2");
	BigInteger value = new BigInteger("3");
	Coroutine coroutine = new Coroutine((pthis) -> {
		pthis.addInstruction((me) -> {
			primes.add(TWO);
			me.yield(TWO);
		});
		pthis.addInstruction("_label1", (me) -> {
			BigInteger half = value.divide(new BigInteger("2"));
			for (BigInteger prime : primes) {
				if (prime.compareTo(half) >= 0) {
					break;
				}
				if (value.mod(prime).equals(BigInteger.ZERO)) {
					return;
				}
			}
			primes.add(value);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			me.yield(value);
		});
		pthis.addInstruction((me) -> {
			value = value.add(TWO);
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
		PrimeCoroutine cor = new PrimeCoroutine();
		for (int i = 0; i < 10000 && cor.hasNext(); ++i) {
			System.out.printf("%d\n", cor.next());
		}
	}

}