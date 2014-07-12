package com.netx.generics.R1.util;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.shared.Globals;


// For Toolset
class HtmlFormatter {

	public static String format(String source) {
		Checker.checkNull(source, "source");
		StringBuilder sb = new StringBuilder();
		int nbspCount = 0;
		for(int i=0; i<source.length(); i++) {
			if(source.charAt(i) == '\t') {
				// Substitute TAB by spaces:
				int tabLength = Globals.getTabSize();
				if(nbspCount == 0) {
					sb.append(' ');
					tabLength--;
				}
				sb.append(Strings.repeat("&nbsp;", tabLength));
			}
			else if(source.charAt(i) == '\n') {
				sb.append("<br>");
			}
			else {
				String tag = _find(source.charAt(i));
				if(tag != null) {
					// Adds &nbsp; only for second occurrences of spaces.
					// This is needed, otherwise the browser doesn't
					// break the line when spaces appear
					if(source.charAt(i) == ' ' && nbspCount==0) {
						sb.append(' ');
					}
					else {
						sb.append("&"+tag+";");
					}
				}
				else {
					sb.append(source.charAt(i));
				}
			}
			// count spaces:
			if(source.charAt(i) == ' ' || source.charAt(i) == '\t') {
				nbspCount++;
			}
			else {
				nbspCount = 0;
			}
		}
		return sb.toString();
	}

	private static String _find(char c) {
		for(int i=0; i<_chars.length; i++) {
			if(_chars[i][0].charAt(0) == c) {
				return _chars[i][1];
			}
		}
		return null;
	}

	private static final String[][] _chars = {
		{" ", "nbsp"},
		{"\"", "quot"},
		{"&", "amp"},
		{"<", "lt"},
		{">", "gt"},
		{"€", "euro"},
		{"Á", "Aacute"},
		{"á", "aacute"},
		{"Â", "Acirc"},
		{"â", "acirc"},
		{"´", "acute"},
		{"æ", "aelig"},
		{"Æ", "AElig"},
		{"À", "Agrave"},
		{"à", "agrave"},
		{"Å", "Aring"},
		{"å", "aring"},
		{"Ã", "Atilde"},
		{"ã", "atilde"},
		{"Ä", "Auml"},
		{"ä", "auml"},
		{"¦", "brvbar"},
		{"Ç", "Ccedil"},
		{"ç", "ccedil"},
		{"¸", "cedil"},
		{"¢", "cent"},
		{"^", "circ"},
		{"©", "copy"},
		{"¤", "curren"},
		{"°", "deg"},
		{"÷", "divide"},
		{"É", "Eacute"},
		{"é", "eacute"},
		{"Ê", "Ecirc"},
		{"ê", "ecirc"},
		{"È", "Egrave"},
		{"è", "egrave"},
		{"Ð", "ETH"},
		{"ð", "eth"},
		{"Ë", "Euml"},
		{"ë", "euml"},
		{"ƒ", "fnof"},
		{"½", "frac12"},
		{"¼", "frac14"},
		{"¾", "frac34"},
		{"Í", "Iacute"},
		{"í", "iacute"},
		{"Î", "Icirc"},
		{"î", "icirc"},
		{"Ì", "Igrave"},
		{"ì", "igrave"},
		{"¡", "iexcl"},
		{"¿", "iquest"},
		{"Ï", "Iuml"},
		{"ï", "iuml"},
		{"«", "laquo"},
		{"¯", "macr"},
		{"µ", "micro"},
		{"·", "middot"},
		{"¬", "not"},
		{"Ñ", "Ntilde"},
		{"ñ", "ntilde"},
		{"Ó", "Oacute"},
		{"ó", "oacute"},
		{"Ô", "Ocirc"},
		{"ô", "ocirc"},
		{"Ò", "Ograve"},
		{"ò", "ograve"},
		{"Œ", "OElig"},
		{"œ", "oelig"},
		{"ª", "ordf"},
		{"º", "ordm"},
		{"Ø", "oslash"},
		{"ø", "oslash"},
		{"Õ", "Otilde"},
		{"õ", "otilde"},
		{"Ö", "Ouml"},
		{"ö", "ouml"},
		{"¶", "para"},
		{"±", "plusmn"},
		{"£", "pound"},
		{"»", "raquo"},
		{"®", "reg"},
		{"Š", "Scaron"},
		{"š", "scaron"},
		{"§", "sect"},
		{"­", "shy"},
		{"¹", "sup1"},
		{"²", "sup2"},
		{"³", "sup3"},
		{"ß", "szlig"},
		{"Þ", "THORN"},
		{"þ", "thorn"},
		{"~", "tilde"},
		{"×", "times"},
		{"Ú", "Uacute"},
		{"ú", "uacute"},
		{"Û", "Ucirc"},
		{"û", "ucirc"},
		{"Ù", "Ugrave"},
		{"ù", "ugrave"},
		{"¨", "uml"},
		{"Ü", "Uuml"},
		{"ü", "uuml"},
		{"Ý", "Yacute"},
		{"ý", "yacute"},
		{"¥", "yen"},
		{"Ÿ", "Yuml"},
		{"ÿ", "yuml"},
		// {"", "ensp"},
		// {"", "emsp"},
		// {"", "thinsp"},
		// {"", "zwnj"},
		// {"", "zwj"},
		// {"", "lrm"},
		// {"", "rlm"},
		{"–", "ndash"},
		{"—", "mdash"},
		{"‘", "lsquo"},
		{"’", "rsquo"},
		{"‚", "sbquo"},
		{"“", "ldquo"},
		{"”", "rdquo"},
		{"„", "bdquo"},
		{"‹", "lsaquo"},
		{"›", "rsaquo"},
		{"†", "dagger"},
		{"‡", "Dagger"},
		{"‰", "permil"},
		{"•", "bull"},
		{"…", "hellip"},
		//TODO get these chars, and backup the file
		//{"?", "Prime"},
		//{"?", "prime"},
		//{"?", "oline"},
		//{"?", "frasl"},
		// {"", "weierp"},
		// {"", "image"},
		// {"", "real"},
		{"™", "trade"},
		// {"", "alefsym"},
		// {"", "larr"},
		// {"", "uarr"},
		// {"", "rarr"},
		// {"", "darr"},
		// {"", "harr"},
		// {"", "carr"},
		// {"", "lArr"},
		// {"", "uArr"},
		// {"", "rArr"},
		// {"", "dArr"},
		// {"", "hArr"},
		// {"", "forall"},
		//{"?", "part"},
		// {"", "exist"},
		// {"", "empty"},
		// {"", "nabla"},
		// {"", "isin"},
		// {"", "notin"},
		// {"", "ni"},
		//{"?", "prod"},
		//{"?", "sum"},
		//{"?", "minus"},
		//{"", "lowast"},
		//{"?", "radic"},
		//{"", "prop"},
		//{"?", "infin"},
		// {"", "ang"},
		// {"", "and"},
		// {"", "or"},
		// {"", "cap"},
		// {"", "cup"},
		//{"?", "int"},
		// {"", "there4"},
		// {"", "sim"},
		// {"", "cong"},
		//{"?", "asymp"},
		//{"?", "ne"},
		// {"", "equiv"},
		// {"?", "le"},
		// {"?", "ge"},
		// {"", "sub"},
		// {"", "sup"},
		// {"", "nsub"},
		// {"", "sube"},
		// {"", "supe"},
		// {"", "oplus"},
		// {"", "otimes"},
		// {"", "perp"},
		// {"", "sdot"},
		// {"", "lceil"},
		// {"", "rceil"},
		// {"", "lfloor"},
		// {"", "rfloor"},
		// {"", "lang"},
		// {"", "rang"},
		//{"?", "loz"},
		// {"", "spades"},
		// {"", "clubs"},
		// {"", "hearts"},
		// {"", "diams"},
		//{"?", "Alpha"},
		//{"?", "alpha"},
		//{"?", "Beta"},
		//{"?", "beta"},
		//{"?", "Gamma"},
		//{"?", "gamma"},
		//{"?", "Delta"},
		//{"?", "delta"},
		//{"?", "Epsilon"},
		//{"?", "epsilon"},
		//{"?", "Zeta"},
		//{"?", "zeta"},
		//{"?", "Eta"},
		//{"?", "eta"},
		//{"?", "Theta"},
		//{"?", "theta"},
		// {"", "thetasym"},
		//{"?", "Iota"},
		//{"?", "iota"},
		//{"?", "Kappa"},
		//{"?", "kappa"},
		//{"?", "Lambda"},
		//{"?", "lambda"},
		//{"?", "Mu"},
		//{"?", "mu"},
		//{"?", "Nu"},
		//{"?", "nu"},
		//{"?", "Xi"},
		//{"?", "xi"},
		//{"?", "Omicron"},
		//{"?", "omicron"},
		//{"?", "Pi"},
		//{"?", "pi"},
		// {"", "piv"},
		//{"?", "Rho"},
		//{"?", "rho"},
		//{"?", "Sigma"},
		//{"?", "sigma"},
		//{"?", "sigmaf"},
		//{"?", "Tau"},
		//{"?", "tau"},
		//{"?", "Upsilon"},
		//{"?", "upsilon"},
		//{"", "upsih"},
		//{"?", "Phi"},
		//{"?", "phi"},
		//{"?", "Chi"},
		//{"?", "chi"},
		//{"?", "Psi"},
		//{"?", "psi"},
		//{"?", "Omega"},
		//{"?", "omega"}
	};

}
