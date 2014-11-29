///////////////////////////////////////////////////////////////////////////////
// Generics.R1
// Dependencies: none;
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Logger class:
///////////////////////////////////////////////////////////////////////////////

var Logger = {

	showStatus: false,
	_INFO: "info",
	_WARN: "warning",
	_ERROR: "error",

	buffer: new Array(),
	
	info: function(msg) {
		this._log(msg, this._INFO);
	},
	
	warn: function(msg) {
		this._log(msg, this._WARN);
	},
	
	error: function(msg) {
		this._log(msg, this._ERROR);
	},
	
	_log: function(msg, type) {
		msg = msg == null ? "[null]" : msg.toString();
		this.buffer.push(type+": "+msg);
		if(window.console) {
			if(type == this._INFO) {
				console.info(msg);
			}
			else if(type == this._WARN) {
				console.warn(msg);
			}
			else {
				console.error(msg);
			}
		}
		if(this.showStatus) {
			window.status = msg;
		}
	}
};



///////////////////////////////////////////////////////////////////////////////
// UnitTest class
///////////////////////////////////////////////////////////////////////////////

var UnitTest = {

	print: function(line) {
		document.write(line);
	},
	
	nl: function() {
		document.write("<br>");
	},

	println: function(line) {
		this.print(line);
		this.nl();
	},

	printTitle: function(string) {
		document.write("<br><b>"+string+"</b><br>");
	},
	
	printArray: function(array) {
		for(var i in array) {
			this.println(array[i]);
		}
	},

	eval: function(expr, quotes) {
		document.write(expr+": ");
		if(quotes) {
			document.write("\"");
		}
		document.write("<b>"+eval(expr)+"</b>");
		if(quotes) {
			document.write("\"");
		}
		document.write("<br>");
	},
	
	assert: function(expr, expected, quotes) {
		document.write(expr+": ");
		var result = eval(expr)+"";
		if(result != expected) {
			document.write("<span class=\"fnt-error\">");
		}
		if(quotes) {
			document.write("\"");
		}
		document.write("<b>"+result+"</b>");
		if(quotes) {
			document.write("\"");
		}
		if(result != expected) {
			document.write("</span> (expected \""+expected+"\")");
		}
		document.write("<br>");
	}
};



///////////////////////////////////////////////////////////////////////////////
// Strings class
///////////////////////////////////////////////////////////////////////////////

var Strings = {

	isString: function(obj) {
		return obj.charAt != null && obj.toLowerCase != null && obj.toUpperCase != null;
	},

	// This is a more complicated version:
	// isString: function(obj) {
	//	 var type = typeof obj;
	//	 if(type == "string") {
	//		return true;
	//	 }
	//	 if(!type == "object") {
	//		return false;
	//	 }
	//	 if(!obj.constructor) {
	//		return false;
	//	 }
	//	 type = obj.constructor.toString().substring("function ".length+1);
	//	 return Strings.startsWith(type, "String");
	// },
		
	isEmpty: function(str) {
		return !str || !str.length;
	},

	removeLeading: function(str, pattern) {
		if(this.isEmpty(str)) {
			return "";
		}
		if(this.startsWith(str, pattern)) {
			return this.removeLeading(str.substring(pattern.length), pattern);
		}
		return str;
	},

	removeTrailing: function(str, pattern) {
		if(this.isEmpty(str)) {
			return "";
		}
		if(this.endsWith(str, pattern)) {
			return this.removeTrailing(str.substring(0, str.length-pattern.length), pattern);
		}
		return str;
	},

	remove: function(str, pattern) {
		return this.removeTrailing(this.removeLeading(str, pattern), pattern);
	},

	replace: function(str, oldStr, newStr) {
		if(this.isEmpty(str)) {
			return "";
		}
		else {
			var convertedString = str.split(oldStr);
			convertedString = convertedString.join(newStr);
			return convertedString;
		}
	},

	trim: function(str) {
		if(this.isEmpty(str)) {
			return "";
		}
		else {
			str = this.replace(str, "\n", " ");
			str = this.replace(str, "\t", " ");
			str = this.replace(str, "\b", " ");
			return this.remove(str, " ");
		}
	},

	toInteger: function(str) {
		//! replace implementation for parseInt(str, [10]);
		if(this.isEmpty(str)) {
			return "";
		}
		else {
			// IE bug:
			str = this.removeLeading(str, '0');
			if(this.isEmpty(str)) {
				return 0;
			}
			else {
				return parseInt(str);
			}
		}
	},

	valueOf: function(number, llength, rlength) {
		if(!llength) llength = 0;
		if(!rlength) rlength = 0;
		var array = (number+"").split(".");
		var lead = "";

		// First step: separation of functions:

		// valueOf(int number, int length)
		if(array.length == 1 && rlength == 0) {
			var str = number + "";
			if(str.length < llength) {
				for(var i=str.length; i<llength; i++) {
					lead += "0";
				}
			}
			return lead + number;
		}

		// valueOf(double number, int llength, int rlength)
		else {
			if(array.length == 1) {
				array = Array(array[0], "0");
			}
			if(array[0].length < llength) {
				for(var j=array[0].length; j<llength; j++) {
					lead += "0";
				}
			}
			// Note: float.toPrecision's argument considers the left of the number also.
			var precision = array[0].length + array[1].length;
			if(rlength > 0) {
				precision = array[0].length + rlength;
			}
			return lead + parseFloat(array[0]+"."+array[1]).toPrecision(precision);
		}
	},
	
	countWords: function(str) {
		str = Strings.trim(str);
		var array = str.split(/ /);
		var words = 0;
		for(var i=0; i<array.length; i++) {
			var s = Strings.trim(array[i]);
			if(s && s != "") {
				words++;
			}
		}
		return words;
	},
	
	startsWith: function(str, start) {
		var tmp = str.substring(0, start.length);
		return tmp == start;
	},
	
	endsWith: function(str, end) {
		if(end.length > str.length) {
			return false;
		}
		var tmp = str.substring(str.length - end.length);
		return tmp == end;
	}
};



///////////////////////////////////////////////////////////////////////////////
// Dates class
///////////////////////////////////////////////////////////////////////////////

var Dates = {
	
	isLeap: function(year) {
		return ((year%400==0)||((year%4==0)&&(year%100!=0)));
	},

	getYear: function(date) {
		// return date.getYear() > 1900 ? date.getYear() : date.getYear() + 1900;
		return date.getFullYear();
	},

	toString: function(date) {
		return this.getYear(date)+"-"+Strings.valueOf(date.getMonth()+1, 2)+"-"+Strings.valueOf(date.getDate(), 2);
	},

	valueOf: function(str) {
		if(str && str.length > 0) {
			if(str.indexOf(" ") != -1) {
				delimiter = " ";
			}
			else if(str.indexOf("/") != -1) {
				delimiter = "/";
			}
			else if(str.indexOf("-") != -1) {
				delimiter = "-";
			}
			else {
				return false;
			}
			var array = str.split(delimiter);
			var year = 0;
			var day = 0;
			if(array[0].length == 4) {
				year = Strings.toInteger(array[0]);
				day = Strings.toInteger(array[2]);
			}
			else {
				day = Strings.toInteger(array[0]);
				year = Strings.toInteger(array[2]);
			}
			// year:
			if(year < 1900 || year > 2500) {
				return false;
			}
			// month:
			var month = 0;
			var months = [["JAN", 31], ["FEV", 28], ["MAR", 31], ["ABR", 30], ["MAI", 31], ["JUN", 30], ["JUL", 31], ["AGO", 31], ["SET", 30], ["OUT", 31], ["NOV", 30], ["DEZ", 31]];
			if(isNaN(array[1])) {
				for(var j=0; j<months.length; j++) {
					if(array[1].toUpperCase() == months[j][0]) {
						month = j+1;
					}
				}
			}
			else {
				month = Strings.toInteger(array[1]);
			}
			if(month < 1 || month > 12) {
				return false;
			}
			// day:
			if(day < 1 || day > months[month-1][1]) {
				if(month==2 && this.isLeap(year) && day==29) {
					// noop;
				}
				else {
					return false;
				}
			}
			// build Date object:
			var date = new Date();
			date.setYear(year);
			date.setMonth(month-1);
			date.setDate(day);
			// reset time attributes:
			date.setHours(0);
			date.setMinutes(0);
			date.setSeconds(0);
			date.setMilliseconds(0);
			return date;
		}
		else {
			return false;
		}
	},

	equals: function(date1, date2) {
		return date1.getYear()==date2.getYear() && date1.getMonth()==date2.getMonth() && date1.getDate()==date2.getDate(); 
	},
	
	formatTime: function(date) {
		if(date == null) {
			date = new Date();
		}
		var hours = date.getHours();
		if(hours < 10) {
			hours = "0"+hours;
		}
		var minutes = date.getMinutes();
		if(minutes < 10) {
			minutes = "0"+minutes;
		}
		return hours + ":" + minutes;
	}
};


///////////////////////////////////////////////////////////////////////////////
// Char class
///////////////////////////////////////////////////////////////////////////////

var Char = {

	isLetter: function(c) {
		// English characters:
		if(c.match(/[a-zA-Z]/)) {
			return true;
		}
		// Non-english characters:
		if(c.match(/[^\x00-\x80]/)) {
			return true;
		}
		return false;
	},

	isDigit: function(c) {
		if(c.match(/[0-9]/)) {
			return true;
		}
		return false;
	},

	isAlphaNumeric: function(c) {
		return Char.isDigit(c) || Char.isLetter(c);
	}
}



///////////////////////////////////////////////////////////////////////////////
// Arrays class
///////////////////////////////////////////////////////////////////////////////

var Arrays = {

	flatten: function(args) {
		if(args.array == null) {
			return "";
		}
		if(args.array.length == 0) {
			return "";
		}
		if(args.translator == null) {
			args.translator = function(entry) { return entry; };
		}
		var str = "";
		for(var i=0; i<args.array.length-1; i++) {
			str = str + args.translator(args.array[i]) + args.separator;
		}
		str = str + args.translator(this.last(args.array));
		return str;
	},
	
	last: function(array) {
		return array[array.length-1];
	},
	
	clear: function(array) {
		while(array.length > 0) {
			array.pop();
		}
	}
};



///////////////////////////////////////////////////////////////////////////////
// Validator class
///////////////////////////////////////////////////////////////////////////////

var Validator = {

	isEmpty: function(str) {
		return Strings.isEmpty(Strings.trim(str));
	},

	isNumber: function(str) {
		return (!isNaN(str));
	},

	isInteger: function(str) {
		return this.isNumber(str) && str.indexOf(".")==-1;
	},

	isAlphabetic: function(str) {
		for(var i=0; i<str.length; i++) {
			if(!Char.isLetter(str.charAt(i))) {
				return false;
			}
		}
		return true;
	},

	isAlphaNumeric: function(str) {
		for(var i=0; i<str.length; i++) {
			if(!Char.isAlphaNumeric(str.charAt(i))) {
				return false;
			}
		}
		return true;
	},

	isEmail: function(str) {
		if(str.length < 6) {
			return false;
		}
		var array = str.split("@");
		if(array.length != 2) {
			return false;
		}
		if(array[0].length == 0) {
			return false;
		}
		if(array[1].indexOf(".") == -1) {
			return false;
		}
		if(array[1].substring(array[1].lastIndexOf(".")+1).length < 2) {
			return false;
		}
		return true;
	},

	isTime: function(str) {
		var array = str.split(":");
		if(array.length != 2 && array.length != 3) {
			return false;
		}
		var hours = Strings.toInteger(array[0]);
		if(!this.isNumber(hours) || hours<0 || hours>23) {
			return false;
		}
		var mins = Strings.toInteger(array[1]);
		if(!this.isNumber(mins) || mins<0 || mins>59) {
			return false;
		}
		if(array.length == 3) {
			var secs = Strings.toInteger(array[2]);
			if(!this.isNumber(secs) || secs<0 || secs>59) {
				return false;
			}
		}
		return true;
	},

	isDate: function(str) {
		return Dates.valueOf(str) != false;
	}
};



//////////////////////////////////////////////////////////////////////
// System class:
//////////////////////////////////////////////////////////////////////

function _System_setVersion(obj, version) {
	obj.version = Strings.trim(version);
	var vParts = version.split(".");
	obj.majorVersion = vParts[0];
	obj.minorVersion = vParts[1];
}

function System() {
	// Base:
	this.base = new Object();
	this.base.name = navigator.appCodeName;
	_System_setVersion(this.base, navigator.appVersion.split("(")[0]);
	// Browser:
	this.browser = new Object();
	this.browser.name = "unknown";
	this.browser.ie = false;
	this.browser.safari = false;
	this.browser.chrome = false;
	this.browser.ns = false;
	// TODO opera, canoma
	if(navigator.appVersion.indexOf("MSIE") != -1) {
		this.browser.ie = true;
		this.browser.name = "Internet Explorer";
		this.browser.language = navigator.browserLanguage;
		_System_setVersion(this.browser, navigator.appVersion.substring(navigator.appVersion.indexOf("MSIE")+5, navigator.appVersion.indexOf("MSIE")+8));
	}
	else if(navigator.appVersion.indexOf("Safari") != -1) {
		var parts = navigator.appVersion.split(")");
		this.browser.language = Strings.trim(parts[0].substring(parts[0].lastIndexOf(" ")));
		if(navigator.appVersion.indexOf("Chrome") != -1) {
			this.browser.chrome = true;
			this.browser.name = "Chrome";
		}
		else {
			this.browser.safari = true;
			this.browser.name = "Safari";
		}
		parts = Arrays.last(parts).substring(Arrays.last(parts).indexOf("/")+1).split(".");
		this.browser.majorVersion = parseInt(parts[0]);
		this.browser.minorVersion = parseInt(parts[1]);
		this.browser.version = this.browser.majorVersion+"."+this.browser.minorVersion;
	}
	// TODO remove Netscape?
	else if(navigator.appName.indexOf("Netscape") != -1) {
		this.browser.ns = true;
		this.browser.name = "Netscape";
		this.browser.language = navigator.language;
		// TODO Same version as base.version
		_System_setVersion(this.browser, navigator.appVersion.substring(0, 3));
	}
	this.browser.cookiesEnabled = function() {
		return navigator.cookieEnabled;
	};
	this.browser.javaOn = function() {
		return navigator.javaEnabled();
	};
	// OS:
	this.os = new Object();
	this.os.mac = false;
	this.os.windows = false;
	this.os.version = "unknown";
	// TODO linux, unix...
	var str = this.browser.ns ? navigator.userAgent : navigator.appVersion;
	if(navigator.appVersion.indexOf("Windows") != -1) {
		this.os.windows = true;
		this.os.name = "Windows";
		if(str.indexOf("NT 5.1") != -1) {
			this.os.version = "XP";
		}
		else if(str.indexOf("NT") != -1) {
			this.os.version = "NT";
		}
		else if(str.indexOf("95") != -1) {
			this.os.version = "95";
		}
		else if(str.indexOf("98") != -1) {
			this.os.version = "98";
		}
		else if(str.indexOf("ME") != -1) {
			this.os.version = "ME";
		}
	}
	else if(str.indexOf("Mac") != -1) {
		this.os.mac = true;
		this.os.name = "MacOS";
		if(str.indexOf("OS X") != -1) {
			this.os.version = "X";
		}
	}
	// Device:
	this.device = new Object();
	this.device.pc = false;
	this.device.iPad = false;
	this.device.iPhone = false;
	if(navigator.appVersion.indexOf("iPhone") != -1) {
		this.device.iPhone = true;
	}
	else if(navigator.appVersion.indexOf("iPad") != -1) {
		this.device.iPad = true;
	}
	else {
		this.device.pc = true;
	}
	return this;
}

var System = new System();



//////////////////////////////////////////////////////////////////////
// Option class:
//////////////////////////////////////////////////////////////////////

var Option = {

	isOption: function(field) {
		return field.type == "select-one" || field.type == "select-multiple";
	},
	
	isSelected: function(field) {
		return this.selectedIndex(field) != 0;
	},

	selectedIndex: function(field) {
		return field.selectedIndex;
	}
};



//////////////////////////////////////////////////////////////////////
// Radio class:
//////////////////////////////////////////////////////////////////////

var Radio = {
	
	isRadioArray: function(field) {
		if(field.length) {
			if(field.length == 0) {
				return false;
			}
			return field[0].type == "radio";
		}
		return false;
	},

	isRadioButton: function(field) {
		return field.type == "radio";
	},

	isSelected: function(field) {
		return this.selectedIndex(field) != -1;
	},

	selectedIndex: function(field) {
		for(i=0; i<field.length; i++) {
			if(field[i].checked) {
				return i;
			}
		}
		return -1;
	},

	selectedValue: function(field) {
		var index = this.selectedIndex(field);
		if(index != -1) {
			return field[index].value;
		}
		else {
			return false;
		}
	}
};



///////////////////////////////////////////////////////////////////////////////
// DOM class:
///////////////////////////////////////////////////////////////////////////////

var DOM = {

	id: function(objId, ignoreNull) {
		var obj = document.getElementById(objId);
		if(obj == null) {
			if(!ignoreNull) {
				Logger.error("object with ID '"+objId+"' could not be found");
			}
		}
		return obj;
	},

	goto: function(url) {
		document.location = url;
	},
	
	show: function(div) {
		div.style.display = "inline";
	},

	hide: function(div) {
		div.style.display = "none";
	},

	getPosition: function(elem) {
		var left = 0;
		var top = 0;
		do {
			left += elem.offsetLeft;
			top += elem.offsetTop;
		} while(elem = elem.offsetParent);
		return {x: left, y: top};
	},
	
	getSize: function(elem) {
		return {width: elem.offsetWidth, height: elem.offsetHeight};
	},

	getWindow: function(name, link) {
		var p = this.popups[name];
		if(!p) {
			p = new Popup(name, link, false);
			this.popups[name] = p;
		}
		return p;
	},

	getWindowHeight: function() {
		if(window.innerHeight) {
			return window.innerHeight;
		}
		return document.documentElement.clientHeight;
	},

	getWindowWidth: function() {
		if(window.innerWidth) {
			return window.innerWidth;
		}
		return document.documentElement.clientWidth;
	},

	cover: function(args) {
		if(args.paddingLeft == null) {
			args.paddingLeft = 0;
		}
		if(args.paddingTop == null) {
			args.paddingTop = 0;
		}
		if(args.shift == null) {
			args.shift = {top:0, left:0};
		}
		var pos = DOM.getPosition(args.toCover);
		args.cover.style.left = (pos.x - args.paddingLeft + args.shift.left)+"px";
		args.cover.style.top = (pos.y - args.paddingTop + args.shift.top)+"px";
		return args.cover;
	},

	getPopup: function(name, link) {
		var p = this.popups[name];
		if(!p) {
			p = new Popup(name, link, true);
			this.popups[name] = p;
		}
		return p;
	},
	
	popups: new Object(),
	
	addEvent: function(node, onEvent, handler) {
		if(!node.eap) {
			node.eap = new Object();
		}
		if(!node.eap[onEvent]) {
			node.eap[onEvent] = new Object();
			node.eap[onEvent].handlers = new Array();
			if((typeof node[onEvent]) == "function") {
				node.eap[onEvent].handlers.push(node[onEvent]);
			}
			node[onEvent] = function() {
				for(var i=0; i<this.eap[onEvent].handlers.length; i++) {
					this.eap[onEvent].handlers[i](this);
				}
			}
		}
		node.eap[onEvent].handlers.push(handler);
	}
};



///////////////////////////////////////////////////////////////////////////////
// Popup class (Used under DOM):
///////////////////////////////////////////////////////////////////////////////

function Popup(name, link, popup) {
	this.name = name;
	this.link = link;
	this.x = false;
	this.y = false;
	this.width = false;
	this.height = false;
	this.window = false;
	if(popup) {
		this.scrollbars = false;
		this.menubar = false;
		this.resizable = false;
	}
	else {
		this.scrollbars = true;
		this.menubar = true;
		this.resizable = true;
	}
	
	this.setSize = function(width, height) {
		this.width = width;
		this.height = height;
		return this;
	};

	this.setBounds = function(x, y, width, height) {
		this.x = x;
		this.y = y;
		this.setSize(width, height);
		return this;
	};

	this.setScrollbars = function(value) {
		this.scrollbars = value;
		return this;
	};

	this.setMenubars = function(value) {
		this.menubar = value;
		return this;
	};

	this.setResizable = function(value) {
		this.resizable = value;
		return this;
	};

	this.open = function() {
		if(this.window) {
			if(!this.window.closed) {
				this.window.close();
			}
		}
		var options = "";
		options = (options=="" ? options : options+", ") + (this.x ? "screenX="+this.x+", screenY="+this.y : "");
		options = (options=="" ? options : options+", ") + (this.width ? "width="+this.width+", height="+this.height : "");
		options = (options=="" ? options : options+", ") + "scrollbars="+(this.scrollbars ? "yes" : "no");
		options = (options=="" ? options : options+", ") + "menubar="+(this.menubar ? "yes" : "no");
		options = (options=="" ? options : options+", ") + "resizable="+(this.resizable ? "yes" : "no");
		this.window = window.open(this.link, this.name, options);
	};
}


