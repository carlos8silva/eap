package com.netx.config.R1;
import java.util.Map;
import java.util.HashMap;
import com.netx.generics.R1.translation.ErrorList;
import com.netx.generics.R1.translation.TranslationStep;
import com.netx.generics.R1.translation.ParseException;
import com.netx.generics.R1.util.Expr;
import com.netx.basic.R1.eh.IntegrityException;


// TODO error messages must come from i18n
// TODO allow exprs with calls to "ctx" to be executed as a last step
public class ConfigAnalyzer extends TranslationStep {

	public ConfigAnalyzer(ConfigParser parser) {
		super(parser);
	}
	
	// Config.getConfigDefinition();
	// Config.getContextDefinition();
	// Config.getTypeDefinition();
	public Object performWork(Object o, ErrorList el) {
		SymbolRoot sRoot = (SymbolRoot)o;
		sRoot.aCtx = Context.getRoot();
		_checkContext(sRoot, Config.getRootDef(), el, "config");
		return null;
	}

	// TODO find another way to go through ctx / props to report errors in correct order
	// TODO detect duplicate names (maybe on the parser)
	@SuppressWarnings("unchecked")
	private void _checkContext(SymbolContext sTopCtx, ContextDef topCtxDef, ErrorList el, String where) {
		// Create a map of all the sub-contexts we will need to find:
		Map<String,ContextDef> rCtxDefs = new HashMap<String,ContextDef>();
		rCtxDefs.putAll(topCtxDef.contexts);
		for(SymbolContext sCtx : sTopCtx.pSubContexts) {
			ContextDef ctxDef = rCtxDefs.remove(sCtx.pName);
			if(ctxDef == null) {
				el.addError(where, "unexpected context '"+sCtx.pName+"'");
				continue;
			}
			final String where2 = where+"."+sCtx.pName;
			// Get the context:
			if(sTopCtx.aCtx == Context.getRoot()) {
				// We can only create the context if we are analyzing the root context.
				// Once we create the level 1 contexts, the Context constructor will actually
				// create all the required structure (including sub-contexts and properties).
				sCtx.aCtx = new Context(ctxDef, sTopCtx.aCtx);
			}
			else {
				// Otherwise we just get it:
				sCtx.aCtx = sTopCtx.aCtx.getContext(sCtx.pName);
			}
			// Check properties:
			Map<String,PropertyDef> rPropDefs = new HashMap<String,PropertyDef>();
			rPropDefs.putAll(ctxDef.properties);
			for(SymbolProperty sProp : sCtx.pProperties) {
				PropertyDef pDef = rPropDefs.remove(sProp.pName);
				if(pDef == null) {
					el.addError(where2, "unexpected property '"+sProp.pName+"'");
					continue;
				}
				final String where3 = where2+"."+pDef.name;
				// Load the property's value:
				// TODO account for explicit NULLs
				Object value = _parseValue(pDef, sProp, el, where3);
				if(el.isEmpty()) {
					if(pDef.type.simpleTL != null) {
						try {
							sCtx.aCtx.setProperty(pDef.name, value);
						}
						catch(Exception e) {
							el.addError(where3, e.getMessage());
						}
					}
					else if(pDef.type.complexTL != null) {
						try {
							value = pDef.type.complexTL.onLoad((Map<String,Object>)value, pDef);
							sCtx.aCtx.setProperty(pDef.name, value);
						}
						catch(Exception e) {
							el.addError(where3, e.getMessage());
						}
					}
					else {
						throw new IntegrityException();
					}
				}
			}
			// Take care of any missing properties:
			if(!rPropDefs.isEmpty()) {
				for(PropertyDef pDef : rPropDefs.values()) {
					if(pDef.mandatory) {
						el.addError(where2, "could not find mandatory property '"+pDef.name+"'");
						continue;
					}
					
					// TODO load defaults
				}
			}
			// Check sub-contexts:
			_checkContext(sCtx, ctxDef, el, where2);
			// Load context into Config:
			
		}
		// Check whether any expected contexts are missing:
		if(!rCtxDefs.isEmpty()) {
			for(ContextDef ctxDef : rCtxDefs.values()) {
				el.addError(where, "could not find sub-context '"+ctxDef.name+"'");
			}
		}
	}
	
	private Object _parseValue(PropertyDef pDef, SymbolProperty sProp, ErrorList el, String where) {
		if(pDef.type.simpleTL != null) {
			// Resolve any generics expressions if needed:
			if(Expr.hasExpression(sProp.pValue)) {
				try {
					sProp.aValue = Expr.evaluate(sProp.pValue).toString();
				}
				catch(ParseException pe) {
					el.addError(where, pe.getMessage());
					return null;
				}
			}
			else {
				sProp.aValue = sProp.pValue;
			}
			// Parse the simple value:
			try {
				return pDef.type.simpleTL.parse(sProp.aValue, pDef);
			}
			catch(TypeLoadException e) {
				el.addError(where, e.getMessage());
				return null;
			}
		}
		else if(pDef.type.complexTL != null) {
			Map<String,Object> values = new HashMap<String,Object>();
			Map<String,PropertyDef> rPropDefs = new HashMap<String,PropertyDef>();
			rPropDefs.putAll(pDef.type.subProps);
			for(SymbolProperty sArg : sProp.pArguments) {
				final String where2 = where+"."+sArg.pName;
				PropertyDef argDef = rPropDefs.remove(sArg.pName);
				if(argDef == null) {
					el.addError(where, "unexpected property '"+sArg.pName+"'");
					continue;
				}
				// Load property value:
				values.put(argDef.name, _parseValue(argDef, sArg, el, where2));
			}
			// Take care of non-provided properties:
			if(!rPropDefs.isEmpty()) {
				for(PropertyDef argDef : rPropDefs.values()) {
					if(argDef.mandatory) {
						el.addError(where, "could not find mandatory property '"+argDef.name+"'");
						continue;
					}
					// TODO load defaults
				}
			}
			return values;
		}
		else {
			throw new IntegrityException();
		}
	}
}
