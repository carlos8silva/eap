
var Config = {

	helpOn: ${help-on},

	enableHelp: function() {
		this.helpOn = true;
		this._persistHelp(true);
	},

	disableHelp: function() {
		this.helpOn = false;
		this._persistHelp(false);
		// Close any visible help panes:
		var frame = DOM.id("frame-main");
		if(frame) {
			if(frame.contentWindow.UI) {
				frame.contentWindow.UI.hideAll();
			}
		}
	},
	
	_persistHelp: function(setting) {
		var request = new AjaxRequest({
			url: "personal-info.srv",
			method: "get",
			parameters: Array({name: "do", value: (setting?"enable":"disable")+"-help"}),
			onSuccess: function(xml) {
				// noop;
			},
			onFailure: function(errorCode, message) {
				Logger.showStatus = true;
				Logger.error("unknown error code: "+errorCode);
			}
		});
		request.send();
	}
};

function Dialog(args) {
	// Variables:
	this.type = Constants.UI.WG_TYPE_DIALOG;
	this.width = args.width;
	this.title = null;
	this.contents = null;
	this.buttons = new Array();
	// Methods:
	this.setTitle = function(args) {
		this.title = args;
	};
	this.setContents = function(args) {
		this.contents = args;
	};
	this.addButton = function(args) {
		if(this.buttons == 2) {
			throw "Dialog Box does not accept more than two buttons";
		}
		this.buttons.push(new _DialogButton(args.text, args.width, args.style, args.handler));
	};
	this.show = function() {
		// Build background:
		// (Note: the background div is only created once)
		var bg = DOM.id("eap-dialog-bg", true);
		if(!bg) {
			bg = document.createElement("div");
			bg.id = "eap-dialog-bg";
			bg.className = "wg-dialog-bg";
			document.body.appendChild(bg);
		}
		// Build window:
		// (Note: the dialog div is reused for all Dialog objects)
		var winDiv = DOM.id("eap-dialog-window", true);
		if(!winDiv) {
			winDiv = document.createElement("div");
			winDiv.id = "eap-dialog-window";
			winDiv.className = "wg-dialog-container";
			document.body.appendChild(winDiv);
		}
		else {
			winDiv.innerHTML = "";
		}
		var totalWidth = this.width - Constants.UI.WG_DIALOG_CONTAINER_PADDING*2;
		winDiv.style.width = totalWidth+"px";
		// Build title:
		var tDiv = document.createElement("div");
		tDiv.className = "wg-dialog-title";
		tDiv.innerHTML = this.title.text;
		if(this.title.align) {
			tDiv.style.textAlign = this.title.align;
		}
		winDiv.appendChild(tDiv);
		// Build contents:
		var cDiv = document.createElement("div");
		cDiv.className = "wg-dialog-contents";
		cDiv.innerHTML = this.contents.text;
		if(this.contents.align) {
			cDiv.style.textAlign = this.contents.align;
		}
		winDiv.appendChild(cDiv);
		// Build buttons:
		var bDiv = document.createElement("div");
		if(this.buttons.length == 0) {
			throw "Dialog Box must have at least one button";
		}
		bDiv.className = "wg-dialog-buttons";
		winDiv.appendChild(bDiv);
		var table = document.createElement("table");
		table.cellSpacing = 0;
		table.cellPadding = 0;
		bDiv.appendChild(table);
		var tr = document.createElement("tr");
		table.appendChild(tr);
		var td = document.createElement("td");
		td.width = totalWidth;
		tr.appendChild(td);		
		for(var i=0; i<this.buttons.length; i++) {
			var input = document.createElement("input");
			input.type = "button";
			if(this.buttons[i].style == "red") {
				input.className = "button-dialog-red";
			}
			else {
				input.className = "button-dialog";
			}
			input.value = this.buttons[i].text;
			if(this.buttons[i].width) {
				input.style.width = this.buttons[i].width+"px";
			}
			input.eap = new Object();
			input.eap.widget = this;
			input.eap.handleEvent = this.buttons[i].handler;
			input.onclick = function() {
				this.eap.handleEvent(this.eap.widget);
			}
			td.appendChild(input);
			td.align = "center";
		}
		
		// Show & size:
		DOM.show(DOM.id("eap-dialog-bg"));
		bg.style.width = DOM.getWindowWidth() + "px";
		bg.style.height = DOM.getWindowHeight() + "px";
		DOM.show(DOM.id("eap-dialog-window"));
		winDiv.style.top = "140px";
		winDiv.style.left = (((770+10+20) / 2) - (winDiv.offsetWidth / 2) + 200)+"px";
	};
	this.hide = function() {
		DOM.hide(DOM.id("eap-dialog-window"));
		DOM.hide(DOM.id("eap-dialog-bg"));
	};
	return this;
}

function _DialogButton(text, width, style, handler) {
	this.text = text;
	this.width = width;
	this.style = style;
	this.handler = handler;
	return this;
}
