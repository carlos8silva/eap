// This snippet is used by the Manage Users AF (um-list-roles.x)
// to allow user managers to edit or disable roles.

var tmpRoleId = null;

table.onFocus = function(id, obj) {
	var bDelete = DOM.id("b_delete");
	bDelete.className = "button-danger";
	bDelete.onclick = deleteRole;
	var bEdit = DOM.id("b_edit");
	bEdit.className = "button-normal";
	bEdit.onclick = editRole;
	tmpRoleId = id;
}

table.onDoubleClick = function(id, obj) {
	editRole();
}

function editRole() {
	DOM.goto("um-edit-role.x?do=edit&id="+tmpRoleId);
}

function deleteRole() {
	DOM.goto("um-edit-role.x?do=delete&id="+tmpRoleId);
}
