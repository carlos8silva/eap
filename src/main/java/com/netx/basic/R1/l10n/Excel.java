package com.netx.basic.R1.l10n;
import com.netx.generics.R1.util.Strings;
import com.netx.basic.R1.eh.Checker;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;


class Excel {

	public static String getValue(HSSFRow row, short col) {
		Checker.checkNull(row, "row");
		Checker.checkIndex(col, "col");
		HSSFCell cell = row.getCell(col);
		if(cell == null) {
			return null;
		}
		else {
			return cell.getRichStringCellValue().toString();
		}
	}
	
	public static boolean isEmpty(HSSFRow row, short col) {
		return Strings.isEmpty(getValue(row, col));
	}
}
