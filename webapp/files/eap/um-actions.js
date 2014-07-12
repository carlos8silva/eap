
// Used in Edit User:
function confirmDisableAccount(username) {
	var dialog = new top.Dialog({width: 315});
	dialog.setTitle({text: "Disable Account"});
	dialog.setContents({text: 
		"Disabling this user account will delete all personal settings, prevent the user "+
		"from logging back into the application, and make the "+username+" username available "+
		"for new user accounts. Are you sure you want to continue?"
	});
	dialog.addButton({text:"Disable Account", width: 120, style: "red", handler:function(d) {
		document.forms[0].action.value = "disable";
		document.forms[0].submit();
		d.hide();
	}});
	dialog.addButton({text:"Cancel", handler:function(d) {
		d.hide();
	}});
	dialog.show();
}

// Used in Edit Role:
function checkRoleName(field) {
	var msg = _checkIsText(field, "Role name");
	if(msg != null) {
		return msg;
	}
	// Check whether the role name already exists:
	var request = new AjaxRequest({
		url: "um-edit-role.x",
		method: "get",
		async: false,
		parameters: Array({name: "id", value: "${role-id}"}, {name: "do", value: "check-role-name"}, DOM.id("name")),
		onSuccess: function(xml) {
			// noop;
		},
		onFailure: function(errorCode, message) {
			if(errorCode == "unavailable") {
				msg = message;
			}
			else {
				Logger.error("unknown error code ["+errorCode+"]: "+message);
			}
		}
	});
	request.send();
	return msg;
}

// TODO move this to EAP.js
function _checkIsText(field, fieldName) {
	// Allow spaces and apostrophes:
	var str = field.value;
	str = Strings.replace(str, " ", "");
	str = Strings.replace(str, "\'", "");
	if(!Validator.isAlphabetic(str)) {
		return Strings.replace(Constants.Locale.FIELD_MUST_HAVE_ONLY_LETTERS, "{1}", fieldName);
	}
	return null;
}
