///////////////////////////////////////////////////////////////////////////////
// EAP.R1
// Dependencies: Generics.R1; constants.js
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// UI class
///////////////////////////////////////////////////////////////////////////////

var UI = {

	// Variables:
	currentLayers: new Array(),
	// TODO remove
	//hideTimeout: null,

	// Methods:
	hideAll: function() {
		while(this.currentLayers.length > 0) {
			DOM.hide(this.currentLayers.pop());
		}
	},
	
	show: function(node) {
		this.currentLayers.push(node);
		DOM.show(node);
	},

	center: function(element) {
		var winWidth = DOM.getWindowWidth();
		var winHeight = DOM.getWindowHeight();
		element.style.left = ((winWidth / 2) - (element.offsetWidth / 2))+"px";
		element.style.top = ((winHeight / 2) - (element.offsetHeight / 2))+"px";
	},
	
	resizeHeight: function(cId) {
		var componentHeight = DOM.getWindowHeight() - Constants.UI.FRAMESET_TOP_PANE_HEIGHT - Constants.UI.FRAMESET_BOTTOM_PANE_HEIGHT;
		DOM.id(cId).style.height = componentHeight+"px";
	},
	
	resizeWidth: function(cId) {
		var componentWidth = DOM.getWindowWidth();
		DOM.id(cId).style.width = componentWidth+"px";
	},
	
	openPage: function(page) {
		DOM.id("frame-main").src = page;
	}
}



///////////////////////////////////////////////////////////////////////////////
// FormManager class
///////////////////////////////////////////////////////////////////////////////

var FormManager = {
	
	// Variables:
	forms: new Array(),
	lastLayerShown: null,
	lastLayerIsValidation: false,
	lastCorrectedLayer: null,
	
	// Methods:
	registerForm: function(form) {
		// Pre-load form images:
		if(this.forms.length == 0) {
			var div = document.createElement("div");
			div.style.display = "none";
			document.body.appendChild(div);
			var formImages = new Array(
				"files/img/button-loading.gif",
				"files/img/icon-ok.png",
				"files/img/icon-warn.png",
				"files/img/icon-error.png",
				"files/img/icon-point-left.png",
				"files/img/icon-point-bottom.png",
				"files/img/icon-point-right.png"
			);
			for(i in formImages) {
				var img = document.createElement("img");
				img.src = formImages[i];
				div.appendChild(img);
			}
		}
		// Register the form:
		var eapForm = new Form(form, this.forms.length+1);
		this.forms.push(eapForm);
		return eapForm;
	},
	
	checkUnsavedChanges: function(fc) {
		if(this.forms.length == 1) {
			return false;
		}
		for(var i=0; i<this.forms.length; i++) {
			if(this.forms[i] != fc) {
				if(this.forms[i]._showUnsavedChanges) {
					if(this.forms[i].hasChanges()) {
						this.hideLastLayer();
						this.forms[i].showUnsavedLayer();
						this.lastLayerShown = this.forms[i]._unsavedLayer;
						this.lastLayerIsValidation = true;
						return true;
					}
					else {
						if(DOM.id(this.forms[i]._unsavedLayer) != null) {
							DOM.hide(DOM.id(this.forms[i]._unsavedLayer));
						}
					}
				}
			}
		}
		return false;
	},

	id: function(fieldId) {
		var field = DOM.id(fieldId);
		if(field == null) {
			return null;
		}
		return field.eap.wrapper;
	},

	show: function(layerId, isValidation) {
		// Must use UI.show instead of DOM.show:
		UI.show(DOM.id(layerId));
		this.lastLayerShown = layerId;
		this.lastLayerIsValidation = isValidation;
	},
	
	hideLastLayer: function() {
		if(this.lastLayerShown != null) {
			DOM.hide(DOM.id(this.lastLayerShown));
			this.lastLayerShown = null;
			this.lastLayerIsValidation = false;
		}
	},
	
	scheduleHide: function(layerId) {
		// Note: we cannot use FormManager.lastLayerShown and need a separate variable:
		this.lastCorrectedLayer = layerId;
		// Note: remove this if we want the confirmation to appear at the same time as info.
		this.lastLayerIsValidation = true;
		setTimeout(this.hideConfirmation, 1500);
	},

	// Note: because this function is called from within a timer,
	// we cannot use references to "this" and must use "FormManager" instead
	hideConfirmation: function() {
		DOM.hide(DOM.id(FormManager.lastCorrectedLayer));
		FormManager.lastLayerIsValidation = false;
	}
};



///////////////////////////////////////////////////////////////////////////////
// Form class
// Note: the Form constructor is called by FormManager
///////////////////////////////////////////////////////////////////////////////

// TODO add underscore to private variables
function Form(args, index) {

	// Public variables:
	this.elements = new Array();
	// Private variables:
	this._index = index;
	this._validator = args.validator;
	this._showUnsavedChanges = args.showUnsavedChanges;
	this._unsavedLayer = null;
	this._confLayer = null;
	this._button = args.button;

	// Listen on submit:
	args.form.eap = new Object();
	args.form.eap.fc = this;
	args.form.onsubmit = function() {
		try {
			var result = this.eap.fc.validateFields(true);
			if(!result) {
				return false;
			}
			result = this.eap.fc.validateForm();
			if(result && args.button) {
				if(args.button.showProgress == true) {
					// Substitute the button with an image (if we keep the
					// element as a button, IE displays a border around it)
					var oldButton = DOM.id(args.button.id);
					if(oldButton.eap) {
						// Ensure we substitute both buttons in case of ButtonMenu:
						if(oldButton.eap.widget.wgNode) {
							var rightButton = oldButton.eap.widget.wgNode;
							var div = document.createElement("div");
							oldButton.parentNode.replaceChild(div, oldButton);
							oldButton = rightButton;
						}
					}
					var progressImg = document.createElement("img");
					progressImg.src = "files/img/button-loading.gif";
					progressImg.style.width = Constants.UI.PROGRESS_IMG_WIDTH+"px";
					progressImg.style.height = Constants.UI.PROGRESS_IMG_HEIGHT+"px";
					oldButton.parentNode.replaceChild(progressImg, oldButton);
				}
			}
			return result;
		}
		catch(error) {
			Logger.error("[In Form]: "+error);
			return false;
		}
	};

	// Methods:
	this.registerField = function(args) {
		var field = new FieldWrapper(args, this);
		this.elements.push(field);
		return field;
	};
	
	this.validateFields = function() {
		for(var i=0; i<this.elements.length; i++) {
			var msg = null;
			if(this.elements[i]._mandatory && this.elements[i].isEmpty()) {
				if(this.elements[i].domNode.type == "text" || this.elements[i].domNode.type == "password" || this.elements[i].domNode.type == "textarea") {
					msg = Strings.replace(Constants.Locale.FIELD_CANNOT_BE_EMPTY, "{1}", this.elements[i]._name);
				}
				else if(Option.isOption(this.elements[i].domNode) || Radio.isRadio(this.elements[i].domNode)) {
					msg = Strings.replace(Constants.Locale.MUST_SELECT_OPTION, "{1}", this.elements[i]._name.toLowerCase());
				}
				else {
					msg = "UNRECOGNIZED";
				}
			}
			else {
				if(this.elements[i]._validator != null) {
					msg = this.elements[i]._validator(this.elements[i].domNode);
				}
			}
			this.elements[i]._valid = (msg == null);
			if(!this.elements[i]._valid) {
				this.elements[i].domNode.focus();
				this.elements[i].showValidation(false, msg);
				return false;
			}
		}
		FormManager.hideLastLayer();
		return true;
	};
	
	this.validateForm = function() {
		if(this._validator == null) {
			return true;
		}
		var msgs = this._validator(args.form);
		if(msgs == null) {
			return true;
		}
		if(msgs.length == 0) {
			return true;
		}
		// Show error messages with dialog box, or alert if not in frameset
		var dialog = null;
		var nl = "\n";
		if(top.Dialog) {
			dialog = new top.Dialog({width: 315});
			dialog.setTitle({text: "Validation Errors", align: "center"});
			nl = "<br>";
		}
		var txt = "The information you are trying to submit has errors. Please correct them before continuing:"+nl;
		for(var i=0; i<msgs.length; i++) {
			txt = txt + nl + "- " + msgs[i];
		}
		if(dialog != null) {
			dialog.setContents({text: txt, align: "left"});
			dialog.addButton({text:"OK", handler:function(d) {
				d.hide();
			}});
			dialog.show();
		}
		else {
			alert(txt);
		}
		return false;
	};

	this.hasChanges = function() {
		for(var i=0; i<this.elements.length; i++) {
			if(this.elements[i]._changed) {
				return true;
			}
		}
		return false;
	};
	
	this.reset = function() {
		for(var i=0; i<this.elements.length; i++) {
			// TODO careful with options and radio
			this.elements[i].domNode.value = "";
			this.elements[i]._changed = false;
			this.elements[i]._valid = true;
		}
	};
	
	this.showUnsavedLayer = function() {
		var div1 = null;
		// Lazy DOM initialization:
		if(this._unsavedLayer == null) {
			if(args.button == null) {
				Logger.error("Cannot show unsaved changes layer without a button assigned to the form");
				return;
			}
			this._unsavedLayer = "form_"+this._index+"_unsaved";
			div1 = document.createElement("div");
			div1.setAttribute("id", this._unsavedLayer);
			div1.setAttribute("class", "field-msg-bottom-container");
			div1.style.width = Constants.UI.FORM_UNSAVED_LAYER_WIDTH+"px";
			document.body.appendChild(div1);
			var div2 = document.createElement("div");
			div2.setAttribute("class", "field-msg-bottom-content");
			div2.innerHTML = Constants.Locale.FORM_UNSAVED_CHANGES_MSG;
			div1.appendChild(div2);
			var div3 = document.createElement("div");
			div3.setAttribute("class", "field-msg-bottom-icon");
			var icon = document.createElement("img");
			icon.src = "files/img/icon-point-bottom.png";
			div3.appendChild(icon);
			div1.appendChild(div3);
		}
		else {
			div1 = DOM.id(this._unsavedLayer);
		}
		DOM.show(div1);
		this._position(div1);
	};

	this.showConfirmation = function(msg) {
		var div1 = null;
		// Lazy DOM initialization:
		if(this._confLayer == null) {
			if(args.button == null) {
				Logger.error("Cannot show confirmation layer without a button assigned to the form");
				return;
			}
			this._confLayer = "form_"+this._index+"_conf";
			div1 = document.createElement("div");
			div1.setAttribute("id", this._confLayer);
			div1.setAttribute("class", "field-msg-left-container");
			document.body.appendChild(div1);
			var div2 = document.createElement("div");
			div2.setAttribute("class", "field-msg-left-icon");
			div1.appendChild(div2);
			var icon = document.createElement("img");
			icon.src = "files/img/icon-ok.png";
			div2.appendChild(icon);
			var div3 = document.createElement("div");
			div3.setAttribute("class", "field-msg-left-content");
			div3.style.width = Constants.UI.FORM_UNSAVED_LAYER_WIDTH+"px";
			div3.innerHTML = "EMPTY";
			div1.appendChild(div3);
		}
		else {
			div1 = DOM.id(this._confLayer);
		}
		div1.getElementsByTagName("div")[1].innerHTML = msg;
		DOM.show(div1);
		this._position(div1);
		FormManager.scheduleHide(this._confLayer);
	};

	this._position = function(layer) {
		var pos = DOM.getPosition(DOM.id(this._button.id));
		layer.style.top = (pos.y - layer.offsetHeight + Constants.UI.FORM_UNSAVED_LAYER_TOP_SHIFT)+"px";
		layer.style.left = (pos.x + DOM.id(args.button.id).offsetWidth - layer.offsetWidth)+"px";
	};
	
	return this;
}



///////////////////////////////////////////////////////////////////////////////
// FieldWrapper class
///////////////////////////////////////////////////////////////////////////////

function FieldWrapper(args, fc, index) {

	// Variables:
	this.domNode = args.field;
	args.field.eap = new Object();
	args.field.eap.wrapper = this;
	this._name = args.name;
	this._layerId = args.layerId;
	this._width = args.width ? args.width : Constants.UI.FORM_FIELD_INFO_WIDTH;
	this._shift = {top:0, left:0};
	this._shift.top = args.shift ? (args.shift.top ? args.shift.top : 0) : 0;
	this._shift.left = args.shift ? (args.shift.left ? args.shift.left : 0) : 0;
	this._info = args.info;
	this._mandatory = args.mandatory == null ? false : args.mandatory;
	this._validator = args.validator;
	this._showCorrected = args.showCorrected == null ? true : args.showCorrected;
	this._valid = true;
	// TODO careful with radio buttons
	this._originalValue = args.field.value;
	this._changed = false;
	this._fc = fc;

	// DOM layer:
	if(args.layerId != null) {
		this._layerId = args.layerId;
	}
	else {
		this._layerId = "form_"+fc.index+"_field_"+(fc.elements.length+1)+"_info";
		var div1 = document.createElement("div");
		div1.setAttribute("id", this._layerId);
		div1.setAttribute("class", "field-msg-left-container");
		document.body.appendChild(div1);
		var div2 = document.createElement("div");
		div2.setAttribute("class", "field-msg-left-icon");
		div1.appendChild(div2);
		var icon = document.createElement("img");
		icon.src = "files/img/icon-point-left.png";
		div2.appendChild(icon);
		var div3 = document.createElement("div");
		div3.setAttribute("class", "field-msg-left-content");
		div3.style.width = this._width + "px";
		div3.innerHTML = "EMPTY";
		div1.appendChild(div3);
	}
	
	// Event handlers:
	args.field.onfocus = function() {
		this.eap.wrapper.showMessage();
	};
	if(Option.isOption(args.field)) {
		args.field.onchange = function() {
			this.eap.wrapper.validate();
		};
	}
	else {
		args.field.onblur = function() {
			this.eap.wrapper.validate();
			// Custom event handler:
			if(this.eap.wrapper.onBlur != null) {
				this.eap.wrapper.onBlur();
			}
		};
	}
	
	// Methods:
	this.hasChanged = function() {
		return this._changed;
	},

	this.showMessage = function() {
		// Position this field's layer:
		if(this._layerId != null) {
			// Calculate y position:
			var pos = DOM.getPosition(this.domNode);
			var topPos = pos.y;
			// For input elements except text areas, we shift the layer a bit up:
			if(this.domNode.type != "textarea") {
				topPos += Constants.UI.FORM_FIELD_INFO_TOP_SHIFT;
			}
			DOM.id(this._layerId).style.top = (topPos + this._shift.top)+"px";
			// Calculate x position:
			DOM.id(this._layerId).style.left = (pos.x + this.domNode.offsetWidth + Constants.UI.FORM_FIELD_INFO_LEFT_SHIFT + this._shift.left)+"px";
		}
		if(FormManager.checkUnsavedChanges(this._fc)) {
			return false;
		}
		if(!this._valid) {
			this.showValidation(false);
			return true;
		}
		else {
			if(top.Config) {
				if(!top.Config.helpOn) {
					return false;
				}
			}
			this.showInfo();
			return true;
		}
	};

	this.validate = function() {
		if(this.domNode.value != this._originalValue) {
			this._changed = true;
		}
		else {
			this._changed = false;
		}
		// If the field is empty, we do not validate it:
		if(this.isEmpty()) {
			return false;
		}
		var msg = null;
		if(this._validator != null) {
			msg = this._validator(this.domNode);
		}
		if(msg != null) {
			this.showValidation(false, msg);
			this._valid = false;
			return false;
		}
		else {
			// We only show the "Corrected" message if the error
			// message currently being displayed is related to this field:
			if(!this._valid && FormManager.lastLayerShown == this._layerId) {
				this.showValidation(true);
			}
			this._valid = true;
			return true;
		}
	};
	
	this.showInfo = function() {
		if(!FormManager.lastLayerIsValidation) {
			FormManager.hideLastLayer();
			if(this._info != null) {
				DOM.id(this._layerId).getElementsByTagName("img")[0].src = "files/img/icon-point-left.png";
				DOM.id(this._layerId).getElementsByTagName("div")[1].innerHTML = this._info;
				FormManager.show(this._layerId, false);
			}
		}
	};

	this.showValidation = function(ok, msg) {
		FormManager.hideLastLayer();
		if(ok) {
			if(!this._showCorrected) {
				// We only show the corrected message if the element has been configured such
				return;
			}
			DOM.id(this._layerId).getElementsByTagName("img")[0].src = "files/img/icon-ok.png";
			DOM.id(this._layerId).getElementsByTagName("div")[1].innerHTML = msg==null ? "Corrected" : msg;
			DOM.show(DOM.id(this._layerId));
			FormManager.scheduleHide(this._layerId);
		}
		else {
			DOM.id(this._layerId).getElementsByTagName("img")[0].src = "files/img/icon-error.png";
			if (msg != null) {
				DOM.id(this._layerId).getElementsByTagName("div")[1].innerHTML = msg;
			}
			FormManager.show(this._layerId, true);
		}
	};

	this.isEmpty = function() {
		if(this.domNode.type == "text" || this.domNode.type == "password" || this.domNode.type == "textarea") {
			return Validator.isEmpty(this.domNode.value);
		}
		else if(Option.isOption(this.domNode)) {
			return !Option.isSelected(this.domNode);
		}
		else if(Radio.isRadio(this.domNode)) {
			return !Radio.isSelected(this.domNode);
		}
		else {
			Logger.error("unrecognized field type: "+this.domNode.type);
			return true;
		}
	};

	this.reset = function() {
		this._valid = true;
		if(FormManager.lastLayerShown == this._layerId) {
			FormManager.hideLastLayer();
		}
	};
	
	return this;
}



///////////////////////////////////////////////////////////////////////////////
// AjaxRequest class
// Note: this only works with the EAP standard XML message format
// TODO separate Ajax and EAP specific functionality
///////////////////////////////////////////////////////////////////////////////

function AjaxRequest(args) {
	
	this.url = args.url;
	this.method = args.method == null ? "GET" : args.method.toUpperCase();
	this.async = args.async==null ? true : args.async;
	this.parameters = args.parameters;
	this.onSuccess = args.onSuccess;
	this.onFailure = args.onFailure;
	
	this.send = function() {
		request = this._getXmlHttp();
		request.onreadystatechange = this._onChangeState;
		request.onSuccess = this.onSuccess;
		request.onFailure = this.onFailure;
		// Build request parameters:
		var reqParams = null;
		if(this.parameters != null) {
			reqParams = "";
			for(var i=0; i<this.parameters.length; i++) {
				reqParams += (this.parameters[i].name + "=" + this.parameters[i].value);
				if(i != this.parameters.length-1) {
					reqParams += "&";
				}
			}
		}
		if(this.method == "GET") {
			request.open(this.method, this.url + (reqParams != null ? "?" + reqParams : ""), this.async);
			request.send(null);
		}
		else if(this.method == "POST") {
			request.open(this.method, this.url, this.async);
			request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			request.setRequestHeader("Content-length", reqParams.length);
			request.setRequestHeader("Connection", "close");
			request.send(reqParams);
		}
		else {
			Logger.error("wrong method argument: "+this.method);
			return;
		}
	};
	
	this._getXmlHttp = function() {
		try { return new XMLHttpRequest(); } catch(e) {}
		try { new ActiveXObject("Msxml2.XMLHTTP.6.0"); } catch(e) {}
		try { new ActiveXObject("Msxml2.XMLHTTP.3.0"); } catch(e) {}
		try { new ActiveXObject("Msxml2.XMLHTTP"); } catch(e) {}
		try { new ActiveXObject("Microsoft.XMLHTTP"); } catch(e) {}
		throw "XML HTTP not supported";
	};
	
	this._onChangeState = function() {
		if(this.readyState == 4) {
			try {
				// Note: status is 0 if the script is run on a local drive
				if(this.status != 200 && this.status != 0) {
					throw "server status code: "+this.status+(this.statusText ? " ("+this.statusText+")" : "");
				}
				var xml = this.responseXML;
				if(xml == null) {
					throw "XML response is null. Text: '"+this.responseText+"'";
				}
				if(this.responseXML.firstChild == null) {
					throw "response not in XML format. Text: '"+this.responseText+"'";
				}
				var status = xml.getElementsByTagName("status")[0].childNodes[0].nodeValue;
				if(status.toUpperCase() == "OK") {
					try {
						this.onSuccess(xml);
					}
					catch(error) {
						Logger.error("[AjaxRequest error on custom onSuccess callback]: "+ error);
					}
				}
				else if(status.toUpperCase() == "ERROR") {
					var errorCode = xml.getElementsByTagName("error-code").length > 0 ? xml.getElementsByTagName("error-code")[0].childNodes[0].nodeValue : null;
					var message = xml.getElementsByTagName("message").length > 0 ? xml.getElementsByTagName("message")[0].childNodes[0].nodeValue : null;
					try {
						this.onFailure(errorCode, message);
					}
					catch(error) {
						Logger.error("[AjaxRequest error on custom onFailure callback]: "+ error);
					}
				}
				else {
					throw "unknown response status: "+status;
				}
			}
			catch(error) {
				Logger.error("[In AjaxRequest]: "+error);
			}
		}
	};
	
	return this;
}



///////////////////////////////////////////////////////////////////////////////
// Widgets class
///////////////////////////////////////////////////////////////////////////////

var Widgets = {
	
	_nextId: 0,
	
	_getNextId: function() {
		this._nextId++;
		return "EAP_WIDGET_"+this._nextId;
	},
	
	Overlay: function(args) {
		// Checks:
		if(args.attachTo == null) {
			throw "attachTo is NULL";
		}
		// Public variables:
		this.type = Constants.UI.WG_TYPE_OVERLAY;
		this.domNode = args.attachTo;
		this.wgNode = null;
		// Private variables:
		var _title = args.title;
		var _content = args.content;
		var _width = args.width;
		var _shift = args.shift;
		// Private methods:
		this._render = function() {
			// DOM structure:
			this.wgNode = document.createElement("div");
			document.body.appendChild(this.wgNode);
			this.wgNode.id = this.domNode.id + "_overlay";
			this.wgNode.className = "info-container";
			this.wgNode.style.width = _width + "px";
			var div2 = document.createElement("div");
			this.wgNode.appendChild(div2);
			div2.className = "info-title";
			div2.innerHTML = _title;
			var div3 = document.createElement("div");
			this.wgNode.appendChild(div3);
			div3.innerHTML = _content;
			// Widget references:
			this.wgNode.eap = new Object();
			this.wgNode.eap.widget = this;
			// Widget event handlers:
			this.wgNode.onmouseout = function() {
				UI.hideAll();
			};
		};
		this._show = function() {
			if(this.wgNode == null) {
				this._render();
			}
			DOM.cover({toCover: this.domNode, cover: this.wgNode, paddingLeft: 5, paddingTop: 5, shift: _shift});
			UI.hideAll();
			UI.show(this.wgNode);
		};
		// DOM references:
		this.domNode.eap = new Object();
		this.domNode.eap.widget = this;
		// DOM event handlers:
		this.domNode.onmouseover = function() {
			this.eap.widget._show();
		};
	},
	
	Menu: function(args) {
		// Checks:
		if(args.attachTo == null) {
			throw "attachTo is NULL";
		}
		// Public variables:
		this.type = Constants.UI.WG_TYPE_MENU;
		this.domNode = args.attachTo;
		this.wgNode = null;
		this.keepVisible = args.keepVisible;
		// Protected variables:
		this._options = new Array();
		if(args.width == "inherit") {
			this._width = args.attachTo.offsetWidth - 2;
		}
		else {
			this._width = args.width;
		}
		this._lastOption = null;
		this._hideTimeout = false;
		this._parent = args.parent;
		this._event = args.event ? args.event : "onclick";
		// Private variables:
		var _wg = this;
		var _shift = args.shift ? args.shift : {top: 0, left: 0};
		var _position = args.position ? args.position : "bottom";
		var _align = args.align;
		var _visible = false;
		// Public functions:
		this.addOption = function(args, parent) {
			var option = new Widgets._MenuOption(this, args, parent);
			this._lastOption = option;
			this._options.push(option);
			return option;
		};
		this.addCover = function(args) {
			if(!args) {
				args = new Object();
			}
			args.text = this.domNode.innerHTML;
			if(!args.style) {
				args.style = this.domNode.className;
			}
			this._cover = new Widgets._MenuCover(this, args);
			// Modify DOM element to include arrow:
			this.domNode.innerHTML = "";
			this._cover._putArrow(this.domNode, args.text, true);
			return this._cover;
		};
		this.addSeparator = function() {
			this._options.push("separator");
			//if(this._lastOption != null) {
			//	this._lastOption.wgNode.childNodes[0].style.borderBottom = Constants.UI.WG_MENU_SEPARATOR_BORDER;
			//}
		};
		this.clearOptions = function() {
			if(_wg.wgNode != null) {
				document.body.removeChild(_wg.wgNode);
				_wg.wgNode = null;
			}
			Arrays.clear(this._options);
		};
		this.onShow = function(widget) {};
		this.onHide = function(widget) {};
		// toggle: used to show/hide the menu. This function needs to use the _wg
		// variable ("this" resolver) as it is used as an event handler for the domNode
		this.toggle = function() {
			if(_wg.wgNode == null) {
				_wg._render();
				// To ensure the first positioning is correct:
				_wg._position();
			}
			if(_wg._lastHighlighted) {
				_wg._lastHighlighted.className = "wg-menu-column";
			}
			if(_visible) {
				_wg._hide();
			}
			else {
				UI.hideAll();
				UI.show(_wg.wgNode);
				// Note: _position MUST be called after UI.show
				_wg._position();
				_visible = true;
				_wg.onShow(_wg);
				// Make the menu disappear after 2 seconds if the user does not use it:
				if(_wg._hideTimeout) {
					clearTimeout(_wg._hideTimeout);
				}
				_wg._hideTimeout = setTimeout(_wg._hide, 2000);
			}
		};
		// Private functions:
		this._render = function() {
			// DOM structure:
			this.wgNode = document.createElement("table");
			document.body.appendChild(this.wgNode);
			this.wgNode.id = Widgets._getNextId();
			this.wgNode.className = "wg-menu";
			this.wgNode.setAttribute("border", "0");
			// Widget references:
			this.wgNode.eap = new Object();
			this.wgNode.eap.widget = this;
			// Render cover:
			if(this._cover) {
				this._cover._render();
			}
			// Render options:
			for(var i=0; i<this._options.length; i++) {
				if(this._options[i] == "separator" && i>0) {
					this._options[i-1].wgNode.childNodes[0].style.borderBottom = Constants.UI.WG_MENU_SEPARATOR_BORDER;
				}
				else {
					this._options[i]._render();
				}
			}
		};
		// _position: Used to position the menu relatively to the DOM element. This is called
		// when the menu is showed, to ensure positioning is correct even if the window is resized.
		this._position = function() {
			var pos = DOM.getPosition(this.domNode);
			if(this._cover != null) {
				this.wgNode.style.left = pos.x + "px";
				this.wgNode.style.top = pos.y + "px";
			}
			else if(_position == "bottom" || _position == "top") {
				// Position the menu horizontally:
				if(_align == "right") {
					this.wgNode.style.left = (pos.x + _shift.left - this._width + this.domNode.offsetWidth - 2) + "px";
				}
				else {
					this.wgNode.style.left = (pos.x + _shift.left) + "px";
				}
				// Position the menu vertically:
				if(_position == "bottom") {
					this.wgNode.style.top = (pos.y + this.domNode.offsetHeight + _shift.top - 1) + "px";
				}
				else {
					this.wgNode.style.top = (pos.y - this.wgNode.offsetHeight - _shift.top + 1) + "px";
				}
			}
			else if(_position == "side") {
				// Position the menu horizontally:
				this.wgNode.style.left = (pos.x + _shift.left + this.domNode.offsetWidth) + "px";
				// Position the menu vertically:
				this.wgNode.style.top = (pos.y + _shift.top - 1) + "px";
			}
			else {
				throw "illegal position value: "+_position;
			}
		};
		// _hide: hides the menu. Used on the toggle function, as a timeout function,
		// and as an event handler (onmouseout) for the menu options
		this._hide = function() {
			DOM.hide(_wg.wgNode);
			_visible = false;
			clearTimeout(_wg._hideTimeout);
			_wg.onHide(_wg);
		};
		// _showSub: used instead of "toggle" to show the menu when it is a sub-menu
		this._showSub = function() {
			if(_wg.wgNode == null) {
				_wg._render();
			}
			UI.show(_wg.wgNode);
			_wg._position();
		};
		// DOM references:
		this.domNode.eap = new Object();
		this.domNode.eap.widget = this;
		// DOM event handlers:
		if(!this._parent) {
			this.domNode[this._event] = this.toggle;
			// If the DOM node is a link, we need to re-set its link to show the menu:
			if(this.domNode.nodeName == "A") {
				this.domNode.setAttribute("href", "javascript:DOM.id('"+this.domNode.id+"').onclick();");
			}
		}
	},
	
	// The MenuOption component is for internal Menu use:
	_MenuOption: function(menu, args, parent) {
		// Public variables:
		this.type = Constants.UI.WG_TYPE_MENU_OPTION;
		// Private variables:
		this._options = null;
		this._menu = menu;
		this._link = args.link;
		this._linkArgs = args.linkArgs;
		this._icon = args.icon;
		this._subMenu = null;
		this._parent = parent;
		this.keepVisible = args.keepVisible==null ? menu.keepVisible : args.keepVisible;
		// Public functions:
		this.getMenu = function() {
			return this._menu;
		};		
		this.setText = function(text) {
			this.wgNode.getElementsByTagName("td")[0].innerHTML = text;
		};
		this.addOption = function(subArgs) {
			if(this._options == null) {
				this._options = new Array();
			}
			// If the option does not have an icon, we need to add it:
			if(!this._icon) {
				this._icon = "files/img/wg-menu-arrow-option.png";
			}
			this._options.push(subArgs);
			// TODO what do we do here?
			// This may actually be an effective way of preventing sub-sub-options
			return null;
		};
		// Private functions:
		this._render = function() {
			// DOM structure:
			this.wgNode = document.createElement("tr");
			menu.wgNode.appendChild(this.wgNode);
			this.wgNode.className = "wg-menu-column";
			var td = document.createElement("td");
			this.wgNode.appendChild(td);
			td.className = "wg-menu-link";
			td.innerHTML = args.text;
			if(this._icon) {
				if(menu._width) {
					// TODO instead of using a constant, use the actual icon width
					td.width = this._menu._width - Constants.UI.WG_MENU_PADDING - Constants.UI.WG_MENU_ICON_WIDTH;
				}
				td = document.createElement("td");
				this.wgNode.appendChild(td);
				td.className = "wg-menu-img";
				td.width = Constants.UI.WG_MENU_ICON_WIDTH;
				var img = document.createElement("img");
				td.appendChild(img);
				img.src = this._icon;
			}
			else {
				if(this._menu._width) {
					td.width = this._menu._width - Constants.UI.WG_MENU_PADDING;
				}
				td.colSpan = "2";
			}
			// Widget references:
			this.wgNode.eap = new Object();
			this.wgNode.eap.widget = this;
			// Widget event handlers:
			this.wgNode.onmouseover = function() {
				var wg = this.eap.widget;
				if(wg.getMenu()._lastHighlighted) {
					wg.getMenu()._lastHighlighted.className = "wg-menu-column";
				}
				// Prevent the menu from disappearing while
				// the user has the mouse over it:
				if(wg.getMenu()._hideTimeout) {
					clearTimeout(wg.getMenu()._hideTimeout);
				}
				// If the option has a sub-menu, show it:
				if(wg._subMenu != null) {
					wg._subMenu._showSub();
					clearTimeout(wg._subMenu._hideTimeout);
				}
				// If the option is part of a sub-menu,
				// prevent the parent menu from disappearing:
				if(wg.getMenu()._parent != null) {
					clearTimeout(wg.getMenu()._parent._hideTimeout);
					wg._parent.wgNode.className = "wg-sub-menu-column";
					wg._parent.getMenu()._lastHighlighted = wg._parent.wgNode;
				}
			};
			this.wgNode.onmouseout = function() {
				var wg = this.eap.widget;
				// Make the menu disappear if the user hovers away:
				wg.getMenu()._hideTimeout = setTimeout(wg.getMenu()._hide, 500);
				// Also make the sub-menu disappear (if applicable):
				if(wg._subMenu) {
					wg._subMenu._hideTimeout = setTimeout(wg._subMenu._hide, 500);
				}
				// Also make the parent menu disappear (if applicable):
				if(wg.getMenu()._parent) {
					wg.getMenu()._parent._hideTimeout = setTimeout(wg.getMenu()._parent._hide, 500);
				}
			};
			this.wgNode.onclick = function() {
				var wg = this.eap.widget;
				if(typeof wg._link == "function") {
					// Function callback:
					wg._link(wg, wg._linkArgs);
				}
				else if(Strings.startsWith(wg._link, "javascript:")) {
					// JavaScript link:
					eval(wg._link);
				}
				else {
					// Regular (href) link:
					document.location = wg._link;
				}
				if(!wg.keepVisible) {
					// Hide the menu:
					wg.getMenu()._hide();
				}
			};
			// Render sub-menu:
			if(this._options != null) {
				var shift = System.browser.ie ? {top:0, left:0} : {top:1, left:1};
				this._subMenu = new Widgets.Menu({attachTo:this.wgNode.childNodes[1], position:"side", align:"left", keepVisible:true, parent:this.getMenu(), shift:shift});
				// Reset links for this option:
				this._link = this._subMenu._showSub;
				this._linkArgs = null;
				for(var i=0; i<this._options.length; i++) {
					this._subMenu.addOption(this._options[i], this);
				}
				this._subMenu._render();
			}
		}
		// DOM references: N/A
		// DOM event handlers: N/A
	},

	// The MenuCover component is for internal Menu use:
	_MenuCover: function(menu, args) {
		this._menu = menu;
		// Public functions:
		this.getMenu = function() {
			return this._menu;
		};		
		this.setText = function(text) {
			this.wgNode.getElementsByTagName("td")[0].innerHTML = text;
		};
		// Private functions:
		this._render = function() {
			// DOM structure:
			this.wgNode = document.createElement("tr");
			menu.wgNode.appendChild(this.wgNode);
			var td = document.createElement("td");
			this.wgNode.appendChild(td);
			td.colSpan = "2";
			td.className = args.style;
			td.style.color = "#FFFFFF";
			this._putArrow(td, args.text, false);
			// Widget references:
			this.wgNode.eap = new Object();
			this.wgNode.eap.widget = this;
			// Widget event handlers:
			this.wgNode.onmouseover = function() {
				var wg = this.eap.widget;
				// Prevent the menu from disappearing while
				// the user has the mouse over it:
				if(wg.getMenu()._hideTimeout) {
					clearTimeout(wg.getMenu()._hideTimeout);
				}
			};
			this.wgNode.onmouseout = function() {
				var wg = this.eap.widget;
				// Make the menu disappear if the user hovers away:
				wg.getMenu()._hideTimeout = setTimeout(wg.getMenu()._hide, 500);
			};
		};
		this._putArrow = function(elem, text, domNode) {
			var cTable = document.createElement("table");
			elem.appendChild(cTable);
			var cTr = document.createElement("tr");
			cTable.appendChild(cTr);
			var cTd1 = document.createElement("td");
			cTr.appendChild(cTd1);
			cTd1.innerHTML = text;
			var cTd2 = document.createElement("td");
			cTr.appendChild(cTd2);
			cTd2.style.paddingLeft = "5px";
			var cImg = document.createElement("img");
			cTd2.appendChild(cImg);
			cImg.src = domNode ? "files/img/wg-menu-cover-arrow-1.png" : "files/img/wg-menu-cover-arrow-2.png";
		};
		// DOM references: N/A
		// DOM event handlers: N/A
	},
	
	ButtonMenu: function(args) {
		this.type = Constants.UI.WG_TYPE_BUTTON_MENU;
		this.wgNode = null;
		// Create DOM structure:
		var nodeId = args.attachTo.id;
		args.attachTo.className = "wg-button-menu-left";
		var bParent = args.attachTo.parentNode;
		var table = document.createElement("table");
		table.setAttribute("cellspacing", "0");
		table.setAttribute("cellpadding", "0");
		table.setAttribute("border", "0");
		var tr = document.createElement("tr");
		table.appendChild(tr);
		var td = document.createElement("td");
		td.setAttribute("align", "right");
		td.innerHTML = bParent.innerHTML;
		tr.appendChild(td);
		var td = document.createElement("td");
		td.style.width = Constants.UI.WG_BUTTON_MENU_WIDTH+"px";
		tr.appendChild(td);
		this.wgNode = document.createElement("input");
		this.wgNode.id = Widgets._getNextId();
		this.wgNode.type = "button";
		this.wgNode.className = "wg-button-menu-right";
		this.wgNode.title = "Click to see additional options";
		td.appendChild(this.wgNode);
		bParent.innerHTML = "";
		bParent.appendChild(table);
		// References:
		this.domNode = DOM.id(nodeId);
		this.domNode.eap = new Object();
		this.domNode.eap.widget = this;
		this.wgNode.eap = new Object();
		this.wgNode.eap.widget = this;
		// Add menu:
		var width = args.menuWidth;
		if(width == "inherit") {
			// For ButtonMenu, inheriting the width means the original button + the newly created arrow button:
			width = this.domNode.offsetWidth + this.wgNode.offsetWidth - 2;
		}
		this._menu = new Widgets.Menu({attachTo: this.wgNode, position:"top", align:"right", width:width});
		
		// Methods:
		this.addOption = function(args) {
			this._menu.addOption(args);
		};
		
		this.clearOptions = function() {
			this._menu.clearOptions();
		};
	},
	
	SelectList: function() {
		this._pane1 = null;
		this._pane2 = null;

		// Methods:
		this.addPane = function(args) {
			if(this._pane2 != null) {
				throw "SelectList can only hold two panes";
			}
			// Work out widths of the panes:
			var paneWidth = 0;
			if(args.columns.length == 3) {
				if(this._pane1 != null) {
					throw "SelectList pane 2 cannot have 3 columns";
				}
				args.columns[0] = {name: args.columns[0], width: 75};
				args.columns[1] = {name: args.columns[1], width: 215};
				args.columns[2] = {name: args.columns[2], width: 53};
				paneWidth = 389;
			}
			else if(args.columns.length == 2) {
				if(this._pane1 == null || (this._pane1 != null && this._pane1._columns.length == 2)) {
					args.columns[0] = {name: args.columns[0], width: 85};
					args.columns[1] = {name: args.columns[1], width: 254};
					paneWidth = 369;
				}
				else {
					args.columns[0] = {name: args.columns[0], width: 75};
					args.columns[1] = {name: args.columns[1], width: 244};
					paneWidth = 349;
				}				
			}
			else {
				throw "SelectList panes can only have either 2 or 3 columns";
			}
			// Create the pane:
			var pane = new Widgets._SelectListPane(args, paneWidth);
			if(this._pane1 == null) {
				this._pane1 = pane;
			}
			else {
				this._pane2 = pane;
				this._pane1._connectTo(this._pane2);
			}
			return pane;
		};
	},

	Entry: function(id, name, description, checked) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.checked = checked;
	},
	
	// SelectListPane is for internal SelectList use:
	_SelectListPane: function(args, paneWidth) {
		// Init:
		this.entries = new Array();
		this.domNode = args.attachTo;
		this.domNode.eap = new Object();
		this.domNode.eap.widget = this;
		this.onCheck = null;
		this._columns = args.columns;
		this._otherList = null;
		this._paneId = Widgets._getNextId();
		this._tableId = Widgets._getNextId();
		// Constants:
		this._colorSelected = args.selectedColor == null ? Constants.UI.COLOR_BLUE : args.selectedColor;
		this._colorBlank = Constants.UI.COLOR_WHITE;
		this._colorHover = Constants.UI.COLOR_GREEN;
		this._colorBorder = "#999999";
		this._paneHeight = 200;
		this._col1Width = 0;
		this._col2Width = 0;
		this._col3Width = 0;
		if(paneWidth == 369) {
			this._col1Width = 84;
			this._col2Width = 245;
		}
		else if(paneWidth == 389) {
			this._col1Width = 74;
			this._col2Width = 224;
			this._col3Width = 45;
		}
		else if(paneWidth == 349) {
			this._col1Width = 74;
			this._col2Width = 245;
		}
		else {
			throw "unexpected paneWidth: "+paneWidth;
		}
		// Event handlers:
		this._onRollOver = function() {
			var row = this.parentNode;
			if(row.eap.entry.selected == false) {
				for(var i in row.eap.cols) {
					row.eap.cols[i].style.backgroundColor = row.eap.list._colorHover;
				}
			}
		};
		this._onRollOut = function() {
			var row = this.parentNode;
			if(row.eap.entry.selected == false) {
				for(var i in row.eap.cols) {
					row.eap.cols[i].style.backgroundColor = row.eap.list._colorBlank;
				}
			}
		};
		this._onClick = function() {
			var row = this.parentNode;
			if(row.eap.entry.selected) {
				for(var i in row.eap.cols) {
					row.eap.cols[i].style.backgroundColor = row.eap.list._colorBlank;
				}
				row.eap.entry.selected = false;
			}
			else {
				for(var i in row.eap.cols) {
					row.eap.cols[i].style.backgroundColor = row.eap.list._colorSelected;
				}
				row.eap.entry.selected = true;
			}
		};
		
		// Create DOM structure:
		var table = document.createElement("table");
		table.cellPadding = "0";
		table.cellSpacing = "0";
		table.border = "0";
		this.domNode.appendChild(table);
		var tr = document.createElement("tr");
		table.appendChild(tr);
		for(var i=0; i<args.columns.length; i++) {
			var td = document.createElement("td");
			td.style.width = args.columns[i].width+"px"
			td.innerHTML = args.columns[i].name;
			td.className = "wg-sl-title-cell wg-sl-title-b-right";
			tr.appendChild(td);
		}
		td.className = "wg-sl-title-cell";
		var div1 = document.createElement("div");
		div1.className = "wg-sl-pane-holder";
		this.domNode.appendChild(div1);
		var div2 = document.createElement("div");
		div2.className = "wg-sl-pane-content";
		div2.id = this._paneId;
		div2.style.width = paneWidth+"px";
		div1.appendChild(div2);
		table = document.createElement("table");
		table.cellPadding = 0;
		table.cellSpacing = 0;
		table.border = 0;
		table.width = paneWidth - 10;
		table.id = this._tableId;
		div2.appendChild(table);
		var div3 = document.createElement("div");
		div3.className = "wg-sl-pane-filler";
		div3.style.width = (paneWidth - 10)+"px";
		div2.appendChild(div3);
		
		// Methods:
		this.addEntry = function(entry) {
			this.entries.push(entry);
			var table = DOM.id(this._tableId);
			var tr = document.createElement("tr");
			entry.domNode = tr;
			entry.selected = false;
			tr.eap = new Object();
			tr.eap.entry = entry;
			tr.eap.list = this;
			tr.eap.cols = new Array();
			table.appendChild(tr);		
			var td1 = document.createElement("td");
			td1.className = "wg-sl-cell wg-sl-cell-b-right wg-sl-cell-b-bottom pointer";
			td1.style.backgroundColor = this._colorBlank;
			td1.width = this._col1Width;
			td1.onmouseover = this._onRollOver;
			td1.onmouseout = this._onRollOut;
			td1.onclick = this._onClick;
			var div1 = document.createElement("div");
			div1.className = "word-wrap";
			div1.style.width = this._col1Width+"px";
			td1.appendChild(div1);
			div1.innerHTML = entry.name;
			tr.appendChild(td1);
			tr.eap.cols.push(td1);
			var td2 = document.createElement("td");
			td2.className = "wg-sl-cell wg-sl-cell-b-bottom pointer";
			td2.style.backgroundColor = this._colorBlank;
			td2.width = this._col2Width;
			td2.onmouseover = this._onRollOver;
			td2.onmouseout = this._onRollOut;
			td2.onclick = this._onClick;
			td2.innerHTML = entry.description;
			tr.appendChild(td2);
			tr.eap.cols.push(td2);
			if(this._columns.length == 3) {
				td2.className += " wg-sl-cell-b-right";
				var td3 = document.createElement("td");
				td3.className = "wg-sl-cell wg-sl-cell-b-bottom";
				td3.style.backgroundColor = this._colorBlank;
				td3.width = this._col3Width;
				td3.onmouseover = this._onRollOver;
				td3.onmouseout = this._onRollOut;
				td3.style.textAlign = "center";
				tr.appendChild(td3);
				var radio = document.createElement("input");
				radio.eap = new Object();
				radio.eap.widget = this;
				radio.type = "radio";
				radio.name = "checked_entry";
				radio.value = entry.id;
				radio.className = "radio";
				if(entry.checked) {
					radio.checked = true;
				}
				// TODO change
				radio.onclick = function() {
					if(this.eap.widget.onCheck != null) {
						this.eap.widget.onCheck(this);
					}
				};
				td3.appendChild(radio);
				tr.eap.cols.push(td3);
			}
			return entry;
		}

		this.redraw = function() {
			var paneId = "#"+this._paneId;
			$(paneId).jScrollPane();
			if(this.entries.length == 0) {
				return;
			}
			// Set the bottom border on all rows:
			var totalHeight = 0;
			for(var i in this.entries) {
				totalHeight += this.entries[i].domNode.offsetHeight;
				var entry = this.entries[i];
				for(var j in entry.domNode.eap.cols) {
					entry.domNode.eap.cols[j].style.borderBottom = "1px solid "+this._colorBorder;
				}
			}
			// Remove the last row's border, if exceeding the pane's size:
			if(totalHeight > this._paneHeight) {
				var entry = Arrays.last(this.entries);
				for(var i in entry.domNode.eap.cols) {
					entry.domNode.eap.cols[i].style.borderBottom = "1px solid "+this._colorBlank;
				}
			}
			else {
				var entry = Arrays.last(this.entries);
				for(var i in entry.domNode.eap.cols) {
					entry.domNode.eap.cols[i].style.borderBottom = "1px solid "+this._colorBorder;
				}
			}
		};

		this.moveEntries = function() {
			var toRemove = new Array();
			for(var i in this.entries) {
				if(this.entries[i].selected) {
					var entry = this.entries[i];
					// Un-select primary role:
					entry.primary = false;
					entry.domNode.parentNode.removeChild(entry.domNode);
					this._otherList.addEntry(entry);
					toRemove.push(entry);
				}
			}
			for(var i in toRemove) {
				this._removeElement(this.entries, toRemove[i]);
			}
			this.redraw();
			this._otherList.redraw();
		};

		this._connectTo = function(otherList) {
			this._otherList = otherList;
			otherList._otherList = this;
		};

		this._removeElement = function(array, entry) {
			for(var i in array) {
				if(entry.name == array[i].name) {
					array.splice(i, 1);
					return true;
				}
			}
			return false;
		};
	}
}



///////////////////////////////////////////////////////////////////////////////
// EAP class
// This is used to hold any function that does not fit the other classes
///////////////////////////////////////////////////////////////////////////////

var EAP = {

	checkIdentifier: function(field) {
		var str = field.value;
		if(!Validator.isAlphabetic(str.charAt(0))) {
			return Strings.replace(Constants.Locale.USERNAME_START_WITH_LETTER, "{1}", "Username");
		}
		str = Strings.replace(str, ".", "");
		str = Strings.replace(str, "_", "");
		str = Strings.replace(str, "-", "");
		if(!Validator.isAlphaNumeric(str)) {
			return Strings.replace(Constants.Locale.USERNAME_ILLEGAL_CHARS, "{1}", "Username");
		}
		return null;
	},
	
	checkPassword: function(field) {
		if(Strings.trim(field.value).length != field.value.length) {
			return Constants.Locale.PASSWORD_CANNOT_HAVE_SPACES;
		}
		if(field.value.length < Constants.Other.VAL_PASSWORD_MAX_LENGTH) {
			return Strings.replace(Strings.replace(Constants.Locale.FIELD_MUST_HAVE_MIN_LENGTH, "{1}", "Password"), "{2}", Constants.Other.VAL_PASSWORD_MAX_LENGTH+"");
		}
		return null;
	},
	
	getCurrentTime: function() {
		var today = new Date();
		return Constants.Locale.DAY_NAMES[today.getDay()] + " " + today.getDate() + " " + Dates.formatTime(today);
	}	
};

