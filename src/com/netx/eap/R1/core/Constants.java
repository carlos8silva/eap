package com.netx.eap.R1.core;


public interface Constants extends com.netx.basic.R1.shared.Constants {

	// Java error handling:
	public static final String JAVAX_STATUS_CODE = "javax.servlet.error.status_code";
	public static final String JAVAX_REQUEST_URI = "javax.servlet.error.request_uri";
	public static final String JAVAX_SERVLET_NAME = "javax.servlet.error.servlet_name";
	public static final String JAVAX_EXCEPTION = "javax.servlet.error.exception";
	// ServletContext properties:
	public static final String SRVCTX_EAP_CTX = "eap-ctx";
	public static final String SRVCTX_EAP_FUNCTIONS = "eap-functions";
	// Cookies:
	public static final String COOKIE_SESSION_ID = "eap-session-id";
	public static final String COOKIE_USERNAME = "eap-username";
	public static final String COOKIE_LAST_SESSION_ID = "eap-last-session-id";
	// Session attributes:
	public static final String SATTR_AUTH_STEP = "eap-auth-step";
	public static final String SATTR_SESSION_EI = "eap-session-ei";
	public static final String SATTR_SESSION_NUM_FAILED_PASSWORDS = "eap-session-num-failed-passwords";
	public static final String SATTR_ROLE = "eap-role";
	public static final String SATTR_ROLE_PERMS = "eap-role-permissions";
	public static final String SATTR_USER = "eap-user";
	public static final String SATTR_USER_ROLES = "eap-user-roles";
	public static final String SATTR_USER_PERMS = "eap-user-permissions";
	public static final String SATTR_USERS_AFFECTED = "eap-users-affected";
	public static final String SATTR_TABLE_SETTINGS = "eap-table-settings";
	// Request attributes:
	public static final String RATTR_USER_SESSION = "eap-session";
	public static final String RATTR_XML_MODE = "eap-xml-mode";
	public static final String RATTR_FUNCTION_ALIAS = "eap-function-alias";
	public static final String RATTR_PAGE_NAME = "eap-page-name";
	// UI styles:
	public static final String UI_FONT_NORMAL = "fnt-text";
	public static final String UI_FONT_ERROR = "fnt-error";
	// URLs:
	public static final String URL_LOGIN = "login";
	public static final String URL_START = "start.srv";
	public static final String URL_ERROR_HANDLER = "error-handler";
	public static final String URL_FUNCTION_SUFFIX = ".x";
}
