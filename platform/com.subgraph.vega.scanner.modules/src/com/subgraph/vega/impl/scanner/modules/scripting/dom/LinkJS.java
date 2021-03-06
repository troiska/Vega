package com.subgraph.vega.impl.scanner.modules.scripting.dom;

import org.w3c.dom.html2.HTMLLinkElement;

public class LinkJS extends HTMLElementJS {

	private static final long serialVersionUID = 1L;
	
	private final HTMLLinkElement linkElement;
	
	public LinkJS() {
		this.linkElement = null;
	}
	
	public LinkJS(HTMLLinkElement linkElement) {
		this.linkElement = linkElement;
	}
	
	@Override 
	public void jsConstructor(Object ob) {
		
	}
	
	@Override
	public String getClassName() {
		return "Link";
	}
	
	public String jsGet_href() {
		return linkElement.getHref();
	}
	
	public String jsGet_charset() {
		return linkElement.getCharset();
	}
	
	
	public String jsGet_hreflang() {
		return linkElement.getHreflang();
	}
		
	public String jsGet_rel() {
		return linkElement.getRev();
	}
	
	public String jsGet_rev() {
		return linkElement.getRev();
	}
	
	public String jsGet_target() {
		return linkElement.getTarget();
	}
	
	public String jsGet_type() {
		return linkElement.getType();
	}
	
	public void jsFunction_blur() {
		
	}
	
	public void jsFunction_focus() {
		
	}	
}
