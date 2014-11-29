package com.netx.eap.R1.info;
import javax.servlet.ServletConfig;
import com.netx.eap.R1.core.EapRequest;
import com.netx.eap.R1.core.EapResponse;
import com.netx.eap.R1.core.EapServlet;


public final class SrvInfoError extends EapServlet {

	public void init(ServletConfig config) {
		super.init(config);
	}

	public void doGet(EapRequest request, EapResponse response) {
		throw new RuntimeException("ooh I crashed!");
	}
}
