package com.example.sharp;

public class Tuples {
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
