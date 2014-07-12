package com.netx.ut.generics.R1;
import com.netx.generics.R1.util.FunctionCall;


public class ExprFunctions {

	public static class MinCall implements FunctionCall {
		
		public String getFunctionName() {
			return "min";
		}

		public Class<?>[][] getParameters() {
			return new Class[][] {{String.class}};
		}

		public String call(Object[] args) throws Exception {
			return args[0].toString().toLowerCase();
		}
	}

	public static class WrongCall1 implements FunctionCall {
		
		public String getFunctionName() {
			return "wrong1";
		}

		public Class<?>[][] getParameters() {
			return new Class[][] {{String.class}, {Object.class}};
		}

		public String call(Object[] args) throws Exception {
			return args[0].toString().toLowerCase();
		}
	}

	public static class WrongCall2 implements FunctionCall {
		
		public String getFunctionName() {
			return "wrong2";
		}

		public Class<?>[][] getParameters() {
			return new Class[][] {{}, {UTUtil.class}};
		}

		public String call(Object[] args) throws Exception {
			return args[0].toString().toLowerCase();
		}
	}
}
