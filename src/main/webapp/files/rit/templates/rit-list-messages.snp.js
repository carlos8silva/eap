
var gblConvId = null;
var bMenu = null;

table.onFocus = function(id, obj) {
	gblConvId = obj.conversation_id;
	if(bMenu == null) {
		var bView = DOM.id("b_view");
		bView.className = "button-normal";
		bMenu = new Widgets.ButtonMenu({attachTo: bView, menuWidth: 120});
		bMenu.domNode.onclick = viewMessage;
	}
	bMenu.clearOptions();
	if(obj.time_read == "") {
		bMenu.addOption({text:"Mark Read", link:"rit-request-access.x?do=mark-read&conv_id="+gblConvId});
	}
	else {
		bMenu.addOption({text:"Mark Unread", link:"rit-request-access.x?do=mark-unread&conv_id="+gblConvId});
	}
	bMenu.addOption({text:"Reply", link:"rit-request-access.x?do=reply&conv_id="+gblConvId});
	if(obj.time_accepted == "" && obj.time_rejected == "") {
		bMenu.addOption({text:"Reject", link:"rit-request-access.x?do=reject&conv_id="+gblConvId});
		bMenu.addOption({text:"Accept", link:"rit-request-access.x?do=accept&conv_id="+gblConvId});
	}
}

table.onDoubleClick = function(id, obj) {
	viewMessage();
}

function viewMessage() {
	DOM.goto("rit-request-access.x?do=view&conv_id="+gblConvId);
}
