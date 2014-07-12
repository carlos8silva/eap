package com.netx.generics.R1.util;
import java.text.SimpleDateFormat;
import com.netx.generics.R1.time.Date;
import com.netx.generics.R1.time.Time;
import com.netx.generics.R1.time.Timestamp;


class Builtins {

	public static class NowCall implements FunctionCall {
		
		public String getFunctionName() {
			return "now";
		}
		
		public Class<?>[][] getParameters() {
			return new Class[][] {{}, {String.class}};
		}
		
		public Object call(Object[] args) {
			Timestamp now = new Timestamp();
			if(args.length == 0) {
				return now;
			}
			else {
				try {
					return now.format(new SimpleDateFormat(args[0].toString()));
				}
				catch(IllegalArgumentException iae) {
					throw new IllegalArgumentException("invalid date/time format used as input: '"+args[0]+"'");
				}
			}
		}
	}

	public static class DateCall implements FunctionCall {
		
		public String getFunctionName() {
			return "date";
		}
		
		public Class<?>[][] getParameters() {
			return new Class[][] {{}, {String.class}};
		}

		public Object call(Object[] args) {
			Date date = new Timestamp().getDate();
			if(args.length == 0) {
				return date;
			}
			else {
				try {
					return date.format(new SimpleDateFormat(args[0].toString()));
				}
				catch(IllegalArgumentException iae) {
					throw new IllegalArgumentException("invalid date format used as input: '"+args[0]+"'");
				}
			}
		}
	}

	public static class TimeCall implements FunctionCall {
		
		public String getFunctionName() {
			return "time";
		}
		
		public Class<?>[][] getParameters() {
			return new Class[][] {{}, {String.class}};
		}

		public Object call(Object[] args) {
			Time time = new Timestamp().getTime();
			if(args.length == 0) {
				return time;
			}
			else {
				try {
					return time.format(new SimpleDateFormat(args[0].toString()));
				}
				catch(IllegalArgumentException iae) {
					throw new IllegalArgumentException("invalid time format used as input: '"+args[0]+"'");
				}
			}
		}
	}
	
	public static class EnvCall implements FunctionCall {
		
		public String getFunctionName() {
			return "env";
		}
		
		public Class<?>[][] getParameters() {
			return new Class[][] {{String.class}};
		}

		public String call(Object[] args) {
			return System.getenv(args[0].toString());
		}
	}
}
