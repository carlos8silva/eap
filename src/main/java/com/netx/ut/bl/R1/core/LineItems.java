package com.netx.ut.bl.R1.core;
import java.util.List;
import com.netx.generics.R1.time.Date;
import com.netx.basic.R1.eh.Checker;
import com.netx.bl.R1.core.*;


public class LineItems extends Association<LineItemsMetaData,LineItem> {

	// TYPE:
	public static LineItems getInstance() {
		return Seller.getLineItems();
	}
	
	// INSTANCE:
	private Select _qSelectLargerQuantities = null;
	private Select _qSelectLineItemsBefore = null;
	private Select _qSelectTotalQuantitiesOrdered = null;

	LineItems() {
		super(new LineItemsMetaData(), Seller.getOrders(), Seller.getProducts());
	}

	protected void onLoad() {
		_qSelectLargerQuantities = createSelect("select-larger-quantities", "SELECT * FROM line_items WHERE quantity > 10 ORDER BY quantity");
		_qSelectLineItemsBefore = createSelect("select-line-items-before", "SELECT line_items.* FROM line_items JOIN orders ON line_items.order_id=orders.order_id WHERE orders.date_made < ? ORDER BY line_items.product_id ASC");
		_qSelectTotalQuantitiesOrdered = getRepository().createSelect("select-total-qty-ordered", "SELECT product_id, SUM(quantity) AS total FROM line_items GROUP BY product_id ORDER BY total DESC");
	}

	protected int save(Connection c, AssociationMap<LineItem> lineItems) throws BLException {
		return insertOrUpdate(c, lineItems);
	}
	
	public AssociationMap<LineItem> getLineItemsFor(Connection c, Order o) throws BLException {
		return getAssociationsFor(c, o);
	}

	public List<LineItem> listLargerQuantities(Connection c) throws BLException {
		return selectList(c, _qSelectLargerQuantities);
	}

	public List<LineItem> listLineItemsBefore(Connection c, Date dateMade) throws BLException {
		Checker.checkNull(dateMade, "dateMade");
		return selectList(c, _qSelectLineItemsBefore, dateMade);
	}

	public Results selectTotalQuantitiesOrdered(Connection c) throws BLException {
		return c.select(_qSelectTotalQuantitiesOrdered);
	}
}
