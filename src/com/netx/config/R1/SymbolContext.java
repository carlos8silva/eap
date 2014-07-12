package com.netx.config.R1;
import java.util.List;
import java.util.ArrayList;


public class SymbolContext {

	// Parsed info:
	public String pName;
	public final List<SymbolContext> pSubContexts = new ArrayList<SymbolContext>();
	public final List<SymbolProperty> pProperties = new ArrayList<SymbolProperty>();
	// Analyzed info:
	public Context aCtx;
}
