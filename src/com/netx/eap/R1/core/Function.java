package com.netx.eap.R1.core;
import java.io.IOException;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.bl.R1.core.BLException;
import com.netx.eap.R1.core.EapRequest.METHOD;


public abstract class Function {

	private String _alias = null;
	private String _permissionId = null;
	
	protected Function() {
	}

	protected void init(EapContext ctx) {
	}
	
	protected void destroy(EapContext ctx) {
	}

	public final String getAlias() {
		return _alias;
	}

	public final String getAliasURL() {
		return _alias + Constants.URL_FUNCTION_SUFFIX;
	}

	public final String getPermissionId() {
		return _permissionId;
	}

	protected void doGet(EapRequest request, EapResponse response) throws IOException, BLException {
		throw new MethodNotAllowedException(METHOD.GET);
	}

	protected void doPost(EapRequest request, EapResponse response) throws IOException, BLException {
		throw new MethodNotAllowedException(METHOD.POST);
	}

	// For SrvInitializer:
	void startup(String alias, String permissionId) {
		if(_alias != null) {
			throw new IntegrityException();
		}
		_alias = alias;
		_permissionId = permissionId;
	}
}
