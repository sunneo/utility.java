package com.example.sharp.coroutine;


import com.example.events.Var;
import com.example.sharp.BaseLinkedList;
import com.example.sharp.CString;
import com.example.sharp.Delegates;
import com.example.sharp.Tracer;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * coroutine which contains instructions and provide yield operation, see
 * cor.example
 *
 */
public class Coroutine {

	public enum State {
		None, Run, Suspend, Stop
	}
	public static class CompositeBlock{
		int ip;
		Coroutine parent;
		Delegates.Action1<Coroutine> body;
		// instruction point
		public int getIP() {
			return ip;
		}
		public CompositeBlock run(Delegates.Action1<Coroutine> ins) {
			this.body=ins;
			return this;
		}
	}
	public static class ForeachBody<T> extends CompositeBlock{
		public CompositeBlock run(Delegates.Action2<Coroutine,T> ins) {
			this.body=(x)->{
				ins.Invoke(x,x.getValue("Value"));
			};
			return this;
		}
	}
	/**
	 * convert coroutine to iterable
	 * @param <T>
	 * @param clz class to hint java adopt as base type for casting
	 * @return
	 */
	public <T> Delegates.IterableEx<T> iterable(Class<T> clz){
		return Delegates.forall(iterator());
	}
	/**
	 * convert coroutine to iterable
	 * @param <T>
	 * @return
	 */
	public <T> Delegates.IterableEx<T> iterable(){
		return Delegates.forall(iterator());
	}
	/**
	 * convret coroutien to iterator
	 * @param <T>
	 * @param clz
	 * @return
	 */
	public <T> Delegates.IteratorEx<T> iterator(Class<T> clz){
		return iterator();
	}
	/**
	 * convert to iterator
	 * @param <T>
	 * @return
	 */
	public <T> Delegates.IteratorEx<T> iterator(){
		
		return Delegates.iterator(new Iterator<T>() {
			boolean tested = false;
			@Override
			public boolean hasNext() {
				if(!tested) {
					if (!isYield()) {
						while (exec()) { }
					}
					tested = true;
				}
				return !isStopped();			
			}

			@Override
			public T next() {
				if (!isYield()) {
					while (exec()) { }
				}
				T ret = getYieldValue();
				while (exec()) {

				}
				return ret;
			}
		});
	}

	public Vector<Delegates.Action1<Coroutine>> instructions = new Vector<Delegates.Action1<Coroutine>>();
	public Hashtable<String, Object> globals = new Hashtable<String, Object>();
	public Hashtable<String, Integer> labels = new Hashtable<String, Integer>();
	static long seriesId = Long.MIN_VALUE;
	long Id = seriesId++;
	String name;

	public long getId() {
		return Id;
	}

	public void setName(String name) {
		this.name = name;
	}

	Object yieldValue;
	int ip;
	State state = State.None;

	public Coroutine() {

	}

	/**
	 * constructor with an action for lazy-inline instantiation.
	 *
	 * @param r action which is invoked with self
	 */
	public Coroutine(Delegates.Action1<Coroutine> r) {
		r.Invoke(this);
	}

	public Coroutine push(Delegates.Action1<Coroutine> r) {
		next = new Coroutine(r);
		next.parent = this;
		return next;
	}

	public Coroutine push(String name, Delegates.Action1<Coroutine> r) {
		Coroutine ret = push(r);
		ret.setName(name);
		return ret;
	}

	public Coroutine push(String name) {
		Coroutine ret = push();
		ret.setName(name);
		return ret;
	}

	public Coroutine push() {
		next = new Coroutine();
		next.parent = this;
		return next;
	}

	private void pop() {
		if (parent != null) {
			parent.next = null;
		}
		next = null;
	}

	Coroutine next;
	Coroutine parent;

	/**
	 * clear all context
	 */
	public void clear() {
		instructions.clear();
		globals.clear();
		labels.clear();
	}

	/**
	 * set global value with value
	 *
	 * @param <T>   type
	 * @param name  name
	 * @param value content of value
	 */
	public <T> void setValue(String name, T value) {
		if (globals.containsKey(name)) {
			globals.remove(name);
		}
		globals.put(name, value);
	}

	/**
	 * get a global value provided by setValue(name,value)
	 *
	 * @param <T>
	 * @param name global value name
	 * @return value for given name, or null if not existed
	 */
	@SuppressWarnings("unchecked")
	public <T> T getValue(String name) {
		if (!globals.containsKey(name)) {
			return null;
		}
		return (T) globals.get(name);
	}


	/**
	 * add instruction
	 *
	 * @param ins an instruction with param to this
	 * @return new instruction pointer
	 */
	public int addInstruction(Delegates.Action1<Coroutine> ins) {
		int idx = instructions.size();
		instructions.add(ins);
		return idx;
	}
	/**
	 * break
	 */
	public void doBreak() {
		if(ipLoopEnd.IsEmpty.get()) return;
		int end = ipLoopEnd.Last.get().Value;
		if(end >= 0) {
			jmp(end);
		}
	}
	/**
	 * break
	 */
	public void doContinue() {
		if(ipLoopStart.IsEmpty.get()) return;
		int end = ipLoopStart.Last.get().Value;
		if(end >= 0) {
			jmp(end);
		}
	}

	BaseLinkedList<Integer> ipLoopEnd = new BaseLinkedList<Integer>();
	BaseLinkedList<Integer> ipLoopStart = new BaseLinkedList<Integer>();
	/**
	 * foreach string
	 * @param <T>
	 * @param iterator
	 * @return
	 */
	public ForeachBody<String> Foreach(StringTokenizer arr) {
		return Foreach(Delegates.forall(arr));
	}
	/**
	 * foreach loop
	 * @param <T>
	 * @param iterator
	 * @return
	 */
	public <T> ForeachBody<T> Foreach(Enumeration<T> arr) {
		return Foreach(Delegates.forall(arr));
	}
	/**
	 * foreach loop
	 * @param <T>
	 * @param iterator
	 * @return
	 */
	public <T> ForeachBody<T> Foreach(T[] arr) {
		return Foreach(Delegates.forall(arr));
	}
	/**
	 * foreach loop
	 * @param <T>
	 * @param iterator
	 * @return
	 */
	public <T> ForeachBody<T> Foreach(Iterable<T> iterable) {
		return Foreach(iterable.iterator());
	}
	/**
	 * foreach loop
	 * @param iterator iterator to perform foreach coroutine
	 * @return
	 */
	public <T> ForeachBody<T> Foreach(Iterator<T> iterator) {
		Var<Integer> breakLoop = new Var<>();
		ForeachBody<T> ret = new ForeachBody<T>();
		Coroutine pthat=this.push();
		ret.parent = pthat;
		
		int loopPos = pthat.addInstruction((cor)->{
			if(!iterator.hasNext()) {
				cor.jmp(breakLoop.get());
				return;
			}
		});
		pthat.addInstruction((cor)->{
			try {
				// While Loop Body
				if(ret.body!=null) {
				    cor.setValue("Value", iterator.next());
				    ret.body.Invoke(cor);
				}
			}catch(Exception ee) {
				Tracer.D(ee);
				cor.stop();
			}
		});
		pthat.addInstruction((cor)->cor.jmp(loopPos));
		int endloop=pthat.addInstruction((cor)->{
		});
		breakLoop.set(endloop);
		ret.ip = loopPos;
		return ret;
	}
	/**
	 * while loop
	 * @param cond
	 * @return
	 */
	public CompositeBlock While(Delegates.Func<Boolean> cond) {
		Var<Integer> breakLoop = new Var<>();
		CompositeBlock ret = new CompositeBlock();
		Coroutine pthat=this.push();
		ret.parent = pthat;
		
		int loopPos = pthat.addInstruction((cor)->{
			if(!cond.Invoke()) {
				cor.jmp(breakLoop.get());
				return;
			}
		});
		pthat.addInstruction((cor)->{
			try {
				// While Loop Body
				if(ret.body!=null) {
				   ret.body.Invoke(cor);
				}
			}catch(Exception ee) {
				Tracer.D(ee);
				cor.stop();
			}
		});
		pthat.addInstruction((cor)->cor.jmp(loopPos));
		int endloop=pthat.addInstruction((cor)->{
		});
		breakLoop.set(endloop);
		ret.ip = loopPos;
		return ret;
	}
	/**
	 * For-Loop
	 * @param init
	 * @param cond
	 * @param step
	 * @return
	 */
	public CompositeBlock For(Delegates.Action1<Coroutine> init,Delegates.Func<Boolean> cond,Delegates.Action1<Coroutine> step) {
		Var<Integer> breakLoop = new Var<>();
		CompositeBlock ret = new CompositeBlock();
		Coroutine pthat=this.push();
		ret.parent = pthat;
		int initPos = pthat.addInstruction(init);
		
		int loopPos = pthat.addInstruction((cor)->{
			if(!cond.Invoke()) {
				cor.jmp(breakLoop.get());
				return;
			}
		});
		pthat.addInstruction((cor)->{
			try {
				// While Loop Body
				if(ret.body!=null) {
				   ret.body.Invoke(cor);
				}
			}catch(Exception ee) {
				Tracer.D(ee);
				cor.stop();
			}
		});
		pthat.addInstruction(step);
		pthat.addInstruction((cor)->cor.jmp(loopPos));
		int endloop=pthat.addInstruction((cor)->{
			 
		});
		breakLoop.set(endloop);
		ret.ip = loopPos;
		return ret;
	}
	/**
	 * If-Else conditional block.
	 * 
	 * Note: Uses the same coroutine context rather than pushing a new one.
	 * This allows if-else blocks to work properly within loop bodies where
	 * a push() would create unnecessary nesting and complicate control flow.
	 * 
	 * @param cond condition to evaluate
	 * @return IfBlock for fluent configuration
	 */
	public IfBlock If(Delegates.Func<Boolean> cond) {
		Var<Integer> endIf = new Var<>();
		Var<Integer> elseBlock = new Var<>();
		IfBlock ret = new IfBlock();
		ret.parent = this;  // Don't push, use same coroutine
		ret.endIfPos = endIf;
		ret.elseBlockPos = elseBlock;
		ret.cond = cond;
		
		// The actual instructions will be added when then()/Else() are called
		ret.ip = this.instructions.size();
		
		return ret;
	}
	
	/**
	 * If block with then and optional else branches
	 */
	public static class IfBlock extends CompositeBlock {
		Delegates.Action1<Coroutine> thenBody;
		Delegates.Action1<Coroutine> elseBody;
		Var<Integer> endIfPos;
		Var<Integer> elseBlockPos;
		Delegates.Func<Boolean> cond;
		int jumpToEndPos;
		boolean instructionsAdded = false;
		
		/**
		 * Define the then branch
		 */
		public IfBlock then(Delegates.Action1<Coroutine> thenAction) {
			this.thenBody = thenAction;
			if (!instructionsAdded) {
				addInstructions();
			}
			return this;
		}
		
		private void addInstructions() {
			instructionsAdded = true;
			
			// Add condition check instruction
			int condPos = parent.addInstruction((cor) -> {
				if (!cond.Invoke()) {
					// Jump to else or end
					if (elseBlockPos.get() != null) {
						cor.jmp(elseBlockPos.get());
					} else if (endIfPos.get() != null) {
						cor.jmp(endIfPos.get());
					}
				}
			});
			ip = condPos;
			
			// Add then body instruction
			parent.addInstruction((cor) -> {
				try {
					if (thenBody != null) {
						thenBody.Invoke(cor);
					}
				} catch (Exception ee) {
					Tracer.D(ee);
					cor.stop();
				}
			});
			
			// Jump over else block after then completes
			jumpToEndPos = parent.addInstruction((cor) -> {
				if (endIfPos.get() != null) {
					cor.jmp(endIfPos.get());
				}
			});
		}
		
		/**
		 * Define the else branch
		 */
		public IfBlock Else(Delegates.Action1<Coroutine> elseAction) {
			this.elseBody = elseAction;
			
			// Add else block instructions
			int elseStart = parent.addInstruction((cor) -> {
				try {
					if (elseBody != null) {
						elseBody.Invoke(cor);
					}
				} catch (Exception ee) {
					Tracer.D(ee);
					cor.stop();
				}
			});
			elseBlockPos.set(elseStart);
			
			// End of if-else
			int endPos = parent.addInstruction((cor) -> {
				// No-op, just a marker
			});
			endIfPos.set(endPos);
			
			return this;
		}
		
		@Override
		public CompositeBlock run(Delegates.Action1<Coroutine> ins) {
			return then(ins).finishIf();
		}
		
		/**
		 * Finish the if block without else
		 */
		private IfBlock finishIf() {
			if (elseBlockPos.get() == null) {
				// No else block, set end position
				int endPos = parent.addInstruction((cor) -> {
					// No-op, just a marker
				});
				endIfPos.set(endPos);
			}
			return this;
		}
	}
	
	/**
	 * add labeled instruction
	 *
	 * @param label label to this instruction, can be used to jmp, it will replace
	 *              the occurrence if named was duplicated.
	 * @param ins   an instruction with param to this
	 * @return new instruction pointer
	 */
	public int addInstruction(String label, Delegates.Action1<Coroutine> ins) {
		int idx = instructions.size();
		instructions.add(ins);
		if (labels.containsKey(label)) {
			labels.remove(label);
		}
		labels.put(label, idx);
		return idx;
	}

	/**
	 * jump to given instruction pointer
	 *
	 * @param ip instruction pointer from addInstruction
	 */
	public void jmp(int ip) {
		this.ip = ip;
		if (this.ip < 0) {
			this.ip = 0;
		}
		if (this.ip >= instructions.size()) {
			this.ip = instructions.size() - 1;
		}
	}

	/**
	 * jump to given label
	 *
	 * @param label label assigned in addInstrction(label,ins)
	 */
	public void jmp(String label) {
		if (labels.containsKey(label)) {
			jmp(labels.get(label));
		}
	}

	/**
	 * get yield value
	 *
	 * @param <T>
	 * @return yield value
	 */
	@SuppressWarnings("unchecked")
	public <T> T getYieldValue() {
		Coroutine selection = this;
		while (selection.next != null && !selection.next.isStopped()) {
			selection = selection.next;
		}
		T ret = (T) selection.yieldValue;
		selection.yieldValue = null;

		return ret;
	}

	/**
	 * suspend execution and put a value to yieldValue
	 *
	 * @param <T>
	 * @param value
	 */
	public <T> void yield(T value) {
		yieldValue = value;
		state = State.Suspend;
	}

	/**
	 * reset the instruction pointer and transfer to initial state
	 */
	public void reset() {
		ip = 0;
		state = State.None;
	}

	/**
	 * transfer state to run state To run pushed instruction, use exec() instead
	 */
	public void start() {
		state = State.Run;
		if (parent != null) {
			this.addInstruction((pthis) -> {
				pthis.pop();
			});
		}
	}

	/**
	 * transfer to stopped state this will stop the execution
	 */
	public void stop() {
		state = State.Stop;
	}

	/**
	 * execute instruction to run until stop, use
	 *
	 * <pre>
	 * while (!coroutine.isStopped()) {
	 * 	coroutine.exec();
	 * }
	 * </pre>
	 *
	 * if somewhere yield value, use
	 *
	 * <pre>
	 * while (!coroutine.isStopped()) {
	 * 	while (!coroutine.exec()) /// this will stop when yield() occurred.
	 * 		;
	 * 	Object yieldValue = coroutine.getYieldValue();
	 * }
	 * </pre>
	 *
	 * @return true if it can continue, or false when stopped/suspend
	 */
	boolean execCurrent() {
		if (state == State.Suspend) {
			state = State.Run;
		}
		if (state != State.Stop && ip < instructions.size()) {
			Delegates.Action1<Coroutine> instruction = instructions.get(ip);
			++ip;
			instruction.Invoke(this);
			if (ip == instructions.size()) {
				state = State.Stop;
			}
			if (state == State.Suspend) {
				return false;
			}
			return true;
		}
		return false;
	}

	public boolean exec() {
		Coroutine selection = this;
		while (selection.next != null && !selection.next.isStopped()) {
			selection = selection.next;
		}
		return selection.execCurrent();
	}

	boolean currentIsStopped() {
		return state == State.Stop || ip == instructions.size();
	}

	/**
	 * test if stopped
	 *
	 * @return true if stopped
	 */
	public boolean isStopped() {
		Coroutine selection = this;
		while (selection.next != null) {
			selection = selection.next;
		}
		if (selection != this) {
			if (!selection.currentIsStopped()) {
				return false;
			}
		}
		return currentIsStopped();

	}

	/**
	 * test if stopped
	 *
	 * @return true if stopped
	 */
	public boolean isYield() {
		Coroutine selection = this;
		// check topmost one
		while (selection.next != null) {
			selection = selection.next;
		}
		return selection.state == State.Suspend;
	}

	@Override
	public String toString() {
		if (!CString.IsNullOrEmpty(name)) {
			return name;
		}
		return "Coroutine-" + String.valueOf(Id);
	}
}