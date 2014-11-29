package com.netx.basic.R1.l10n;
import java.io.InputStream;
import java.io.IOException;
import java.util.Locale;
import com.netx.basic.R1.eh.IntegrityException;


public class L10n {

	// Shared constants:
	public final static String TYPE_MSG = "msg";
	public final static String TYPE_UI = "ui";
	public final static String TYPE_WORD = "word";
	public final static String M_GLOBAL = "global";

	// Internal constants:
	private final static String _I18N_FILENAME = "L10n-store.xls";
	private final static String _M_GENERICS = "generics";
	private final static String _M_BASIC = "basic";

	// Global content ID's:
	public static final ContentID GLOBAL_WORD_ERROR = new ContentID(M_GLOBAL, TYPE_WORD, "error");
	public static final ContentID GLOBAL_WORD_WARNING = new ContentID(M_GLOBAL, TYPE_WORD, "warning");
	public static final ContentID GLOBAL_WORD_FOLDER = new ContentID(M_GLOBAL, TYPE_WORD, "folder");
	public static final ContentID GLOBAL_WORD_DIRECTORY = new ContentID(M_GLOBAL, TYPE_WORD, "directory");
	public static final ContentID GLOBAL_WORD_FILE = new ContentID(M_GLOBAL, TYPE_WORD, "file");
	public static final ContentID GLOBAL_WORD_FUNCTION = new ContentID(M_GLOBAL, TYPE_WORD, "function");
	public static final ContentID GLOBAL_WORD_CONTEXT = new ContentID(M_GLOBAL, TYPE_WORD, "context");
	public static final ContentID GLOBAL_WORD_SPACE = new ContentID(M_GLOBAL, TYPE_WORD, "space");
	public static final ContentID GLOBAL_WORD_PROPERTY = new ContentID(M_GLOBAL, TYPE_WORD, "property");
	public static final ContentID GLOBAL_MSG_UNEXPECTED_ERROR = new ContentID(M_GLOBAL, TYPE_MSG, "unexpected-error");
	// Content ID's for generics.collections:
	// Content ID's for generics.sql:
	// Content ID's for generics.tasks:
	// Content ID's for generics.time:
	// Content ID's for generics.translation:
	public static final ContentID GENERICS_MSG_ERROR_AT_LINE = new ContentID(_M_GENERICS, TYPE_MSG, "error-at-line");
	public static final ContentID GENERICS_MSG_ERROR_AT_LOCATION = new ContentID(_M_GENERICS, TYPE_MSG, "error-at-location");
	// Content ID's for generics.util:
	public static final ContentID GENERICS_MSG_CONSTRUCTOR_NOT_FOUND = new ContentID(_M_GENERICS, TYPE_MSG, "constructor-not-found");
	public static final ContentID GENERICS_MSG_CONSTRUCTOR_NOT_VISIBLE = new ContentID(_M_GENERICS, TYPE_MSG, "constructor-not-visible");
	public static final ContentID GENERICS_MSG_CONSTRUCTION_TARGET_ABSTRACT = new ContentID(_M_GENERICS, TYPE_MSG, "construction-target-abstract");
	public static final ContentID GENERICS_MSG_CONSTRUCTION_TARGET_INTERFACE = new ContentID(_M_GENERICS, TYPE_MSG, "construction-target-interface");
	public static final ContentID GENERICS_MSG_CONSTRUCTION_INVOCATION = new ContentID(_M_GENERICS, TYPE_MSG, "construction-invocation");
	// Content ID's for basic.eh:
	public static final ContentID BASIC_MSG_ALREADY_EXISTS = new ContentID(_M_BASIC, TYPE_MSG, "already-exists");
	public static final ContentID BASIC_MSG_NOT_FOUND = new ContentID(_M_BASIC, TYPE_MSG, "not-found");
	// Content ID's for basic.eh.ErrorHandler:
	public static final ContentID BASIC_MSG_EH_UNKNOWN = new ContentID(_M_BASIC, TYPE_MSG, "eh-unknown");
	public static final ContentID BASIC_MSG_EH_ARITHMETIC = new ContentID(_M_BASIC, TYPE_MSG, "eh-arithmetic");
	public static final ContentID BASIC_MSG_EH_INDEX_OUT_OF_BOUNDS = new ContentID(_M_BASIC, TYPE_MSG, "eh-index-out-of-bounds");
	public static final ContentID BASIC_MSG_EH_CLASS_CAST = new ContentID(_M_BASIC, TYPE_MSG, "eh-class-cast");
	public static final ContentID BASIC_MSG_EH_ILLEGAL_ARG = new ContentID(_M_BASIC, TYPE_MSG, "eh-illegal-arg");
	public static final ContentID BASIC_MSG_EH_NUMBER_FORMAT = new ContentID(_M_BASIC, TYPE_MSG, "eh-number-format");
	public static final ContentID BASIC_MSG_EH_ILLEGAL_FORMAT = new ContentID(_M_BASIC, TYPE_MSG, "eh-illegal-format");
	public static final ContentID BASIC_MSG_EH_ILLEGAL_MONITOR_STATE = new ContentID(_M_BASIC, TYPE_MSG, "eh-illegal-monitor-state");
	public static final ContentID BASIC_MSG_EH_ILLEGAL_STATE = new ContentID(_M_BASIC, TYPE_MSG, "eh-illegal-state");
	public static final ContentID BASIC_MSG_EH_NULL_POINTER = new ContentID(_M_BASIC, TYPE_MSG, "eh-null-pointer");
	public static final ContentID BASIC_MSG_EH_SECURITY = new ContentID(_M_BASIC, TYPE_MSG, "eh-security");
	public static final ContentID BASIC_MSG_EH_UNSUPPORTED_OPERATION = new ContentID(_M_BASIC, TYPE_MSG, "eh-unsupported-operation");
	public static final ContentID BASIC_MSG_EH_CONCURRENT_MODIFICATION = new ContentID(_M_BASIC, TYPE_MSG, "eh-concurrent-modification");
	public static final ContentID BASIC_MSG_EH_MISSING_RESOURCE = new ContentID(_M_BASIC, TYPE_MSG, "eh-missing-resource");
	public static final ContentID BASIC_MSG_EH_BUFFER_OVERFLOW = new ContentID(_M_BASIC, TYPE_MSG, "eh-buffer-overflow");
	public static final ContentID BASIC_MSG_EH_BUFFER_UNDERFLOW = new ContentID(_M_BASIC, TYPE_MSG, "eh-buffer-underflow");
	public static final ContentID BASIC_MSG_EH_CLASS_NOT_FOUND = new ContentID(_M_BASIC, TYPE_MSG, "eh-class-not-found");
	public static final ContentID BASIC_MSG_EH_CLONE_NOT_SUPPORTED = new ContentID(_M_BASIC, TYPE_MSG, "eh-clone-not-supported");
	public static final ContentID BASIC_MSG_EH_ILLEGAL_ACCESS = new ContentID(_M_BASIC, TYPE_MSG, "eh-illegal-access");
	public static final ContentID BASIC_MSG_EH_INSTANTIATION = new ContentID(_M_BASIC, TYPE_MSG, "eh-instantiation");
	public static final ContentID BASIC_MSG_EH_INTERRUPTED = new ContentID(_M_BASIC, TYPE_MSG, "eh-interrupted");
	public static final ContentID BASIC_MSG_EH_NO_SUCH_FIELD = new ContentID(_M_BASIC, TYPE_MSG, "eh-no-such-field");
	public static final ContentID BASIC_MSG_EH_NO_SUCH_METHOD = new ContentID(_M_BASIC, TYPE_MSG, "eh-no-such-method");
	public static final ContentID BASIC_MSG_EH_IO = new ContentID(_M_BASIC, TYPE_MSG, "eh-io");
	public static final ContentID BASIC_MSG_EH_INVALID_PROPERTIES_FORMAT = new ContentID(_M_BASIC, TYPE_MSG, "eh-invalid-properties-format");
	public static final ContentID BASIC_MSG_EH_CHAR_CONVERSION = new ContentID(_M_BASIC, TYPE_MSG, "eh-char-conversion");
	public static final ContentID BASIC_MSG_EH_EOF = new ContentID(_M_BASIC, TYPE_MSG, "eh-eof");
	public static final ContentID BASIC_MSG_EH_FILE_NOT_FOUND = new ContentID(_M_BASIC, TYPE_MSG, "eh-file-not-found");
	public static final ContentID BASIC_MSG_EH_INTERRUPTED_IO = new ContentID(_M_BASIC, TYPE_MSG, "eh-interrupted-io");
	public static final ContentID BASIC_MSG_EH_SOCKET_TIMEOUT = new ContentID(_M_BASIC, TYPE_MSG, "eh-socket-timeout");
	public static final ContentID BASIC_MSG_EH_OBJECT_STREAM = new ContentID(_M_BASIC, TYPE_MSG, "eh-object-stream");
	public static final ContentID BASIC_MSG_EH_SYNC_FAILED = new ContentID(_M_BASIC, TYPE_MSG, "eh-sync-failed");
	public static final ContentID BASIC_MSG_EH_UNSUPPORTED_ENCODING = new ContentID(_M_BASIC, TYPE_MSG, "eh-unsupported-encoding");
	public static final ContentID BASIC_MSG_EH_UTF_DATA_FORMAT = new ContentID(_M_BASIC, TYPE_MSG, "eh-utf-data-format");
	public static final ContentID BASIC_MSG_EH_BIND = new ContentID(_M_BASIC, TYPE_MSG, "eh-bind");
	public static final ContentID BASIC_MSG_EH_CONNECT = new ContentID(_M_BASIC, TYPE_MSG, "eh-connect");
	public static final ContentID BASIC_MSG_EH_NO_ROUTE_TO_HOST = new ContentID(_M_BASIC, TYPE_MSG, "eh-no-route-to-host");
	public static final ContentID BASIC_MSG_EH_PORT_UNREACHABLE = new ContentID(_M_BASIC, TYPE_MSG, "eh-port-unreachable");
	public static final ContentID BASIC_MSG_EH_HTTP_RETRY = new ContentID(_M_BASIC, TYPE_MSG, "eh-http-retry");
	public static final ContentID BASIC_MSG_EH_MALFORMED_URL = new ContentID(_M_BASIC, TYPE_MSG, "eh-malformed-url");
	public static final ContentID BASIC_MSG_EH_PROTOCOL = new ContentID(_M_BASIC, TYPE_MSG, "eh-protocol");
	public static final ContentID BASIC_MSG_EH_UNKNOWN_HOST = new ContentID(_M_BASIC, TYPE_MSG, "eh-unknown-host");
	public static final ContentID BASIC_MSG_EH_UNKNOWN_SERVICE = new ContentID(_M_BASIC, TYPE_MSG, "eh-unknown-service");
	public static final ContentID BASIC_MSG_EH_TOO_MANY_LISTENERS = new ContentID(_M_BASIC, TYPE_MSG, "eh-too-many-listeners");
	public static final ContentID BASIC_MSG_EH_URI_SYNTAX = new ContentID(_M_BASIC, TYPE_MSG, "eh-uri-syntax");
	public static final ContentID BASIC_MSG_EH_SQL = new ContentID(_M_BASIC, TYPE_MSG, "eh-sql");
	public static final ContentID BASIC_MSG_EH_LINKAGE = new ContentID(_M_BASIC, TYPE_MSG, "eh-linkage");
	public static final ContentID BASIC_MSG_EH_EXCEPTION_IN_INITIALIZER = new ContentID(_M_BASIC, TYPE_MSG, "eh-exception-in-initializer");
	public static final ContentID BASIC_MSG_EH_NO_CLASS_DEF_FOUND = new ContentID(_M_BASIC, TYPE_MSG, "eh-no-class-def-found");
	public static final ContentID BASIC_MSG_EH_UNSATISFIED_LINK = new ContentID(_M_BASIC, TYPE_MSG, "eh-unsatisfied-link");
	public static final ContentID BASIC_MSG_EH_VM_INTERNAL = new ContentID(_M_BASIC, TYPE_MSG, "eh-vm-internal");
	public static final ContentID BASIC_MSG_EH_VM_OUT_OF_MEMORY = new ContentID(_M_BASIC, TYPE_MSG, "eh-vm-out-of-memory");
	public static final ContentID BASIC_MSG_EH_VM_STACK_OVERFLOW = new ContentID(_M_BASIC, TYPE_MSG, "eh-vm-stack-overflow");
	public static final ContentID BASIC_MSG_EH_VM_UNKNOWN = new ContentID(_M_BASIC, TYPE_MSG, "eh-vm-unknown");
	public static final ContentID BASIC_MSG_EH_VALIDATION = new ContentID(_M_BASIC, TYPE_MSG, "eh-validation");
	public static final ContentID BASIC_MSG_EH_ILLEGAL_USAGE = new ContentID(_M_BASIC, TYPE_MSG, "eh-illegal-usage");
	public static final ContentID BASIC_MSG_EH_INTEGRITY = new ContentID(_M_BASIC, TYPE_MSG, "eh-integrity");
	// Content ID's for basic.i18n - none: if i18n is not loaded correcly, we cannot get messages from the i18n store.
	// Content ID's for basic.io:
	public static final ContentID BASIC_MSG_FILE_ALREADY_EXISTS = new ContentID(_M_BASIC, TYPE_MSG, "file-already-exists");
	public static final ContentID BASIC_MSG_FOLDER_ALREADY_EXISTS = new ContentID(_M_BASIC, TYPE_MSG, "folder-already-exists");
	public static final ContentID BASIC_MSG_FOLDER_NOT_FOUND = new ContentID(_M_BASIC, TYPE_MSG, "folder-not-found");
	public static final ContentID BASIC_MSG_FOLDER_DELETED = new ContentID(_M_BASIC, TYPE_MSG, "folder-deleted");
	public static final ContentID BASIC_MSG_FILE_DELETED = new ContentID(_M_BASIC, TYPE_MSG, "file-deleted");
	public static final ContentID BASIC_MSG_FOLDER_IS_FILE = new ContentID(_M_BASIC, TYPE_MSG, "folder-is-file");
	public static final ContentID BASIC_MSG_READ_ACCESS_DENIED = new ContentID(_M_BASIC, TYPE_MSG, "read-access-denied");
	public static final ContentID BASIC_MSG_WRITE_ACCESS_DENIED = new ContentID(_M_BASIC, TYPE_MSG, "write-access-denied");
	public static final ContentID BASIC_MSG_WRITE_ACCESS_DENIED_RO = new ContentID(_M_BASIC, TYPE_MSG, "write-access-denied-ro");
	public static final ContentID BASIC_MSG_NOT_OWNER = new ContentID(_M_BASIC, TYPE_MSG, "not-owner");
	public static final ContentID BASIC_MSG_FILE_LOCKED = new ContentID(_M_BASIC, TYPE_MSG, "file-locked");
	public static final ContentID BASIC_MSG_FOLDER_NOT_EMPTY = new ContentID(_M_BASIC, TYPE_MSG, "folder-not-empty");
	public static final ContentID BASIC_MSG_OPERATION_FAILED = new ContentID(_M_BASIC, TYPE_MSG, "operation-failed");
	public static final ContentID BASIC_MSG_UNKNOWN_HOST = new ContentID(_M_BASIC, TYPE_MSG, "unknown-host");

	// Content ID's for basic.logging:
	// TODO move to EAP:
	// Content ID's for EAP:
	public static final ContentID EAP_MSG_TEMPLATE_ERRORS = new ContentID("eap", TYPE_MSG, "template-errors");
	
	// i18n store:
	private static ContentStore _i18nStore;

	public static String getContent(ContentID id, Object ... parameters) {
		if(_i18nStore == null) {
			_i18nStore = _load();
		}
		return _i18nStore.getContent(id, parameters);
	}

	private static ContentStore _load() {
		try {
			// Load the i18n store:
			InputStream in = L10n.class.getClassLoader().getResourceAsStream(_I18N_FILENAME);
			if(in == null) {
				throw new IntegrityException("could not find i18n store in classpath");
			}
			return ContentStore.loadFrom(in, Locale.getDefault());
		}
		catch(IOException io) {
			throw new IntegrityException("input error while reading i18n store: "+io.getMessage(), io);
		}
	}
}
