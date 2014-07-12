package com.netx.rit.R1.app;
import java.io.PrintWriter;
import com.netx.basic.R1.io.BasicIOException;
import com.netx.bl.R1.core.Connection;
import com.netx.bl.R1.core.BLException;
import com.netx.eap.R1.core.EapRequest;
import com.netx.eap.R1.core.EapResponse;
import com.netx.eap.R1.core.MimeTypes;
import com.netx.rit.R1.bl.Items;
import com.netx.rit.R1.bl.Item;


public class FxEditItem extends ProjectFunction {

	protected void doGet(EapRequest request, EapResponse response) throws BasicIOException, BLException {
		// Retrieve item:
		Long itemId = request.getLongParameter("item_id");
		Connection c = request.getConnection();
		Item item = Items.getInstance().get(c, itemId);
		// Check permissions:
		checkPermission(request, item.getProject(c));
		c.close();
		// Render response:
		PrintWriter out = response.getWriter();
		response.setContentType(MimeTypes.TEXT_PLAIN);
		response.setDisableCache();
		out.println("ITEM ID: "+item.getItemId());
		out.println("TITLE: "+item.getTitle());
		out.close();
	}
}
