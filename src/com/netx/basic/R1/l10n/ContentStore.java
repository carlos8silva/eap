package com.netx.basic.R1.l10n;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.generics.R1.util.Strings;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;


public class ContentStore {

	// TYPE:
	private final static String[] _COLUMNS = {"Module", "Type", "ID", "Parameters"};
	private final static short _COL_MODULE = 0;
	private final static short _COL_TYPE = 1;
	private final static short _COL_ID = 2;
	private final static short _COL_PARAMS = 3;

	// TODO substitute IntegrityException with another one
	public static ContentStore loadFrom(InputStream in, Locale locale) throws IOException {
		Checker.checkNull(in, "in");
		Checker.checkNull(locale, "locale");
		POIFSFileSystem fs = new POIFSFileSystem(in);
		HSSFSheet sheet = new HSSFWorkbook(fs).getSheetAt(0);
		// Check whether the column format is correct:
		Iterator<?> it = sheet.rowIterator();
		HSSFRow r = (HSSFRow)it.next();
		for(short i=0; i<_COLUMNS.length; i++) {
			String value = Excel.getValue(r, i);
			if(!_COLUMNS[i].equalsIgnoreCase(value)) {
				throw new IntegrityException("while parsing content store: expected '"+_COLUMNS[i]+"' in column "+(i+1));
			}
		}
		// Get the language and locale columns for the default locale:
		short languageCol = -1;
		short localeCol = -1;
		for(short i=(short)_COLUMNS.length; i<r.getLastCellNum(); i++) {
			if(locale.getLanguage().equals(Excel.getValue(r, i))) {
				languageCol = i;
			}
			else if(locale.toString().equals(Excel.getValue(r, i))) {
				localeCol = i;
			}
		}
		if(languageCol == -1) {
			throw new IntegrityException("while parsing content store: could not find column for language '"+locale.getLanguage()+"'");
		}
		if(localeCol == -1) {
			throw new IntegrityException("while parsing content store: could not find column for locale '"+locale.toString()+"'");
		}
		// Format ok, let's load the file:
		// We only report the first error that occurs while loading the lines,
		// but continue to load the file until the end:
		String firstError = null;
		ContentStore cs = new ContentStore();
		for(short i=1; it.hasNext(); i++) {
			r = (HSSFRow)it.next();
			// Check whether the row is a title row:
			// (only if the Type, ID and first language cols don't have data):
			if(Excel.isEmpty(r, (byte)1) && Excel.isEmpty(r, (byte)2) && Excel.isEmpty(r, (byte)3)) {
				continue;
			}
			// Nope, parse a normal content row:
			ContentSegment segment = new ContentSegment();
			String localeContent = Excel.getValue(r, localeCol);
			if(!Strings.isEmpty(localeContent)) {
				segment.content = localeContent;
			}
			else {
				// No specific content, need to fetch the language content:
				String langContent = Excel.getValue(r, languageCol);
				if(Strings.isEmpty(langContent)) {
					// Only report if it's first error
					if(firstError == null) {
						firstError = "while parsing content store: could not find content under '"+locale.getLanguage()+"' or '"+locale.toString()+"' columns on line "+(i+1);
					}
					continue;
				}
				segment.content = langContent;
			}
			// Module:
			String module = Excel.getValue(r, _COL_MODULE);
			if(Strings.isEmpty(module)) {
				if(firstError == null) {
					firstError = "while parsing content store: empty Module column on line "+(i+1);
				}
				continue;
			}
			module = module.toLowerCase();
			// Type:
			String type = Excel.getValue(r, _COL_TYPE);
			if(Strings.isEmpty(type)) {
				if(firstError == null) {
					firstError = "while parsing content store: empty Type column on line "+(i+1);
				}
				continue;
			}
			type = type.toLowerCase();
			// ID:
			String id = Excel.getValue(r, _COL_ID);
			if(Strings.isEmpty(id)) {
				if(firstError == null) {
					firstError = "while parsing content store: empty ID column on line "+(i+1);
				}
				continue;
			}
			id = id.toLowerCase();
			// Parameters:
			String params = Excel.getValue(r, _COL_PARAMS);
			if(Strings.isEmpty(params)) {
				segment.parameterCount = 0;
			}
			else {
				segment.parameterCount = params.split("[,]").length;
			}
			// We are ready to add the content segment to the content store:
			ContentKey key = new ContentKey(module, type, id);
			if(cs._contents.get(key) != null) {
				if(firstError == null) {
					firstError = "while parsing content store: repeated content ID on line "+(i+1)+": "+id;
				}
				continue;
			}
			cs._contents.put(key, segment);
		}
		// If errors have occurred while reading the lines, we must throw the first error:
		if(firstError != null) {
			throw new IntegrityException(firstError);
		}
		return cs;
	}
	
	// INSTANCE:
	private final Map<ContentKey,ContentSegment> _contents;

	private ContentStore() {
		_contents = new HashMap<ContentKey,ContentSegment>();
	}

	public String getContent(ContentID id, Object ... parameters) {
		Checker.checkNull(id, "id");
		// Content segment is already cached:
		if(id.getSegment() != null) {
			return id.getSegment().getContent(id.toString(), parameters);
		}
		// Not cached, need to fetch it from the L10n store:
		// Note that we REMOVE the segment: this will make the L10n store lighter:
		ContentSegment segment = _contents.remove(id.getKey());
		if(segment == null) {
			throw new IntegrityException("error getting L10n content segment: could not find content for ID '"+id.getKey().toString()+"'");
		}
		id.setSegment(segment);
		return segment.getContent(id.toString(), parameters);
	}
}
