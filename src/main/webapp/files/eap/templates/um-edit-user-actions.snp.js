// This snippet is used by the Manage Users AF (um-list-users.x)
// to allow user managers to edit or disable user accounts.

var tmpUserId = null;
var tmpUsername = null;

table.onFocus = function(id, obj) {
	var bEdit = DOM.id("b_edit");
	bEdit.className = "button-normal";
	bEdit.onclick = editAccount;
	var bDisable = DOM.id("b_disable");
	if(obj.column4 == "Disabled") {
		bDisable.className = "button-normal";
		bDisable.value = "Enable";
		bDisable.onclick = enableAccount;
		bEdit.value = "View";
	}
	else {
		bDisable.className = "button-danger";
		bDisable.value = "Disable";
		bDisable.onclick = disableAccount;
		bEdit.value = "Edit";
	}
	tmpUserId = id;
	tmpUsername = obj.column1;
}

table.onDoubleClick = function(id, obj) {
	editAccount();
}

function editAccount() {
	DOM.goto("um-edit-user.x?do=edit&id="+tmpUserId);
}

function enableAccount() {
	DOM.goto("um-edit-user.x?do=enable&id="+tmpUserId);
}

function disableAccount() {
	document.forms[0].id.value = tmpUserId;
	confirmDisableAccount(tmpUsername);
}
