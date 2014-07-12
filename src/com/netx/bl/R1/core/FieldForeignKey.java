package com.netx.bl.R1.core;
import com.netx.bl.R1.spi.DatabaseDriver;


public class FieldForeignKey extends Field {

	public enum ON_DELETE_CONSTRAINT {
		RESTRICT,
		CASCADE,
		SET_NULL,
		SET_DEFAULT
	}

	private final Field _foreignField;
	private final ON_DELETE_CONSTRAINT _onDelete;

	// Constructor used for foreign keys when the target entity has a composed primary key.
	public FieldForeignKey(MetaData owner, String name, String columnName, Comparable<?> defaultValue, boolean mandatory, boolean readOnly, Field foreignField, ON_DELETE_CONSTRAINT onDelete) {
		super(Field.TYPE.FOREIGN_KEY, owner, name, columnName, defaultValue, mandatory, readOnly);
		_foreignField = foreignField;
		_onDelete = onDelete;
		foreignField.getOwner().addLinkedEntity(owner);
	}

	// Constructor used for foreign keys when the target entity has a simple primary key.
	public FieldForeignKey(MetaData owner, String name, String columnName, Comparable<?> defaultValue, boolean mandatory, boolean readOnly, MetaData foreignEntity, ON_DELETE_CONSTRAINT onDelete) {
		this(owner, name, columnName, defaultValue, mandatory, readOnly, foreignEntity.getPrimaryKeyFields().get(0), onDelete);
	}

	public Field getForeignField() {
		return _foreignField;
	}
	
	public ON_DELETE_CONSTRAINT getOnDeleteConstraint() {
		return _onDelete;
	}

	public Comparable<?> toObject(String value, DatabaseDriver driver) throws WrongFormatException, SizeExceededException {
		return getForeignField().toObject(value, driver);
	}
	
	protected void checkType(Object value) {
		getForeignField().checkType(value);
	}

	public int hashCode() {
		return getForeignField().hashCode();
	}
}
