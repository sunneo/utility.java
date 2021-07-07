package com.example.sharp;



/**
 * boost-alike Tuple for bounding multiple types of items inline
 *
 */
public class Tuples {
	/**
	 * create tuple with 1 param
	 * @param <T1>
	 * @param item1
	 * @return
	 */
	public static <T1> Tuple<T1> tuple(T1 item1){
		return new Tuple<>(item1);
	}
	/**
	 * create tuple with 2 param2
	 * @param <T1>
	 * @param <T2>
	 * @param item1
	 * @param item2
	 * @return
	 */
	public static <T1,T2> Tuple2<T1,T2> tuple(T1 item1, T2 item2){
		return new Tuple2<>(item1,item2);
	}
	/**
	 * create tuple with 3 params
	 * @param <T1>
	 * @param <T2>
	 * @param <T3>
	 * @param item1
	 * @param item2
	 * @param item3
	 * @return
	 */
	public static <T1,T2,T3> Tuple3<T1,T2,T3> tuple(T1 item1, T2 item2, T3 item3){
		return new Tuple3<>(item1,item2,item3);
	}
	/**
	 * create tuple with 4 params
	 * @param <T1>
	 * @param <T2>
	 * @param <T3>
	 * @param <T4>
	 * @param item1
	 * @param item2
	 * @param item3
	 * @param item4
	 * @return
	 */
	public static <T1,T2,T3,T4> Tuple4<T1,T2,T3,T4> tuple(T1 item1, T2 item2, T3 item3, T4 item4){
		return new Tuple4<>(item1,item2,item3,item4);
	}
	/**
	 * create tuple with 5 params
	 * @param <T1>
	 * @param <T2>
	 * @param <T3>
	 * @param <T4>
	 * @param <T5>
	 * @param item1
	 * @param item2
	 * @param item3
	 * @param item4
	 * @param item5
	 * @return
	 */
	public static <T1,T2,T3,T4,T5> Tuple5<T1,T2,T3,T4,T5> tuple(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5){
		return new Tuple5<>(item1,item2,item3,item4,item5);
	}
	/**
	 * create tuple with 6 params
	 * @param <T1>
	 * @param <T2>
	 * @param <T3>
	 * @param <T4>
	 * @param <T5>
	 * @param <T6>
	 * @param item1
	 * @param item2
	 * @param item3
	 * @param item4
	 * @param item5
	 * @param item6
	 * @return
	 */
	public static <T1,T2,T3,T4,T5,T6> Tuple6<T1,T2,T3,T4,T5,T6> tuple(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5, T6 item6){
		return new Tuple6<>(item1,item2,item3,item4,item5,item6);
	}
	/**
	 * create tuple with 7 params
	 * @param <T1>
	 * @param <T2>
	 * @param <T3>
	 * @param <T4>
	 * @param <T5>
	 * @param <T6>
	 * @param <T7>
	 * @param item1
	 * @param item2
	 * @param item3
	 * @param item4
	 * @param item5
	 * @param item6
	 * @param item7
	 * @return
	 */
	public static <T1,T2,T3,T4,T5,T6,T7> Tuple7<T1,T2,T3,T4,T5,T6,T7> tuple(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5, T6 item6, T7 item7){
		return new Tuple7<>(item1,item2,item3,item4,item5,item6,item7);
	}
	/**
	 * create tuple with 8 params 
	 * @param <T1>
	 * @param <T2>
	 * @param <T3>
	 * @param <T4>
	 * @param <T5>
	 * @param <T6>
	 * @param <T7>
	 * @param <T8>
	 * @param item1
	 * @param item2
	 * @param item3
	 * @param item4
	 * @param item5
	 * @param item6
	 * @param item7
	 * @param item8
	 * @return
	 */
	public static <T1,T2,T3,T4,T5,T6,T7,T8> Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> tuple(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5, T6 item6, T7 item7, T8 item8){
		return new Tuple8<>(item1,item2,item3,item4,item5,item6,item7,item8);
	}
	public static class Tuple<T1>{
		public T1 item1;
		public Tuple() {
			
		}
		public Tuple(T1 val) {
			this.item1 = val;
		}
	}
	public static class Tuple2<T1,T2> extends Tuple<T1>{
		public T2 item2;
		public Tuple2() {
			
		}
		public Tuple2(T1 item1, T2 item2) {
			this.item1 = item1;
			this.item2 = item2;
		}
	}
	public static class Tuple3<T1,T2,T3> extends Tuple2<T1,T2>{
		public T3 item3;
		public Tuple3() {
			
		}
		public Tuple3(T1 item1, T2 item2, T3 item3) {
			this.item1 = item1;
			this.item2 = item2;
			this.item3 = item3;
		}
	}
	public static class Tuple4<T1,T2,T3,T4> extends Tuple3<T1,T2,T3>{
		public T4 item4;
		public Tuple4() {
			
		}
		public Tuple4(T1 item1, T2 item2, T3 item3, T4 item4) {
			this.item1 = item1;
			this.item2 = item2;
			this.item3 = item3;
			this.item4 = item4;
		}
	}
	public static class Tuple5<T1,T2,T3,T4,T5> extends Tuple4<T1,T2,T3,T4>{
		public T5 item5;
		public Tuple5() {
			
		}
		public Tuple5(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5) {
			this.item1 = item1;
			this.item2 = item2;
			this.item3 = item3;
			this.item4 = item4;
			this.item5 = item5;
		}
	}
	public static class Tuple6<T1,T2,T3,T4,T5,T6> extends Tuple5<T1,T2,T3,T4,T5>{
		public T6 item6;
		public Tuple6() {
			
		}
		public Tuple6(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5, T6 item6) {
			this.item1 = item1;
			this.item2 = item2;
			this.item3 = item3;
			this.item4 = item4;
			this.item5 = item5;
			this.item6 = item6;
		}
	}
	public static class Tuple7<T1,T2,T3,T4,T5,T6,T7> extends Tuple6<T1,T2,T3,T4,T5,T6>{
		public T7 item7;
		public Tuple7() {
			
		}
		public Tuple7(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5, T6 item6, T7 item7) {
			this.item1 = item1;
			this.item2 = item2;
			this.item3 = item3;
			this.item4 = item4;
			this.item5 = item5;
			this.item6 = item6;
			this.item7 = item7;
		}
	}
	public static class Tuple8<T1,T2,T3,T4,T5,T6,T7, T8> extends Tuple7<T1,T2,T3,T4,T5,T6,T7>{
		public T8 item8;
		public Tuple8() {
			
		}
		public Tuple8(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5, T6 item6, T7 item7, T8 item8) {
			this.item1 = item1;
			this.item2 = item2;
			this.item3 = item3;
			this.item4 = item4;
			this.item5 = item5;
			this.item6 = item6;
			this.item7 = item7;
			this.item8 = item8;
		}
	}
}
