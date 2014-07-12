package com.netx.ut.generics.R1;

class CreateObject {

	public static class ToCreate {
		private ToCreate(Integer i) {
		}

		public ToCreate(String s) {
			this(1);
			throw new IllegalArgumentException();
		}
		
	}
	
	public abstract class ToCreateAbstract {
		public ToCreateAbstract() {
		}
	}

	public static interface ToCreateInterface {
	}

}
