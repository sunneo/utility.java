package com.example.sharp.coroutine.example;

import com.example.events.Var;
import com.example.sharp.Delegates;
import com.example.sharp.coroutine.Coroutine;

public class CoroutineLoopExample {

	public CoroutineLoopExample() {
		
	}
	
	/**
	 * 3 level nested for loop as generator
	 * @return
	 */
	public static Delegates.IterableEx<Integer> integerCreator(){
    	Coroutine cor = new Coroutine();
    	Var<Integer> i = new Var<>();
    	cor.For((ins)->i.set(0), ()->i.get()<1000*1000, (ins)->i.set(i.get()+1000)).run((ins)->{
    		Var<Integer> j = new Var<>();
    		ins.For((ins2)->j.set(0), ()->j.get()<100*100, (ins2)->j.set(j.get()+100)).run((ins2)->{
    			Var<Integer> k = new Var<>();
	    		ins2.For((ins3)->k.set(0), ()->k.get()<10*10, (ins3)->k.set(k.get()+10)).run((ins3)->{
		    		int vali=i.get();
		    		int valj=j.get();
		    		int valk=k.get();
		    		ins3.yield(vali+valj+valk);
		    	});
	    	});
    	});
    	
    	return Delegates.forall(cor.iterator(Integer.class));
	}

}
