package com.subgraph.vega.impl.scanner.urls;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpUriRequest;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.html2.HTMLDocument;

import com.subgraph.vega.api.analysis.IContentAnalyzer;
import com.subgraph.vega.api.analysis.IContentAnalyzerResult;
import com.subgraph.vega.api.html.IHTMLParseResult;
import com.subgraph.vega.api.http.requests.IHttpResponse;
import com.subgraph.vega.api.scanner.IInjectionModuleContext;
import com.subgraph.vega.api.scanner.IScannerConfig;
import com.subgraph.vega.impl.scanner.forms.FormProcessor;

public class ResponseAnalyzer {

	private final IContentAnalyzer contentAnalyzer;
	private final UriParser uriParser;
	private final UriFilter uriFilter;
	private final FormProcessor formProcessor;

	public ResponseAnalyzer(IScannerConfig config, IContentAnalyzer contentAnalyzer, UriParser uriParser, UriFilter uriFilter) {
		this.contentAnalyzer = contentAnalyzer;
		this.uriParser = uriParser;
		this.uriFilter = uriFilter;
		this.formProcessor = new FormProcessor(config, uriFilter, uriParser);
	}

	public IContentAnalyzer getContentAnalyzer() {
		return contentAnalyzer;
	}

	public void analyzePivot(IInjectionModuleContext ctx, HttpUriRequest req, IHttpResponse res) {

	}
	public void analyzePage(IInjectionModuleContext ctx, HttpUriRequest req, IHttpResponse res) {

		final IContentAnalyzerResult result = contentAnalyzer.processResponse(res, false, true);
		for(URI u: result.getDiscoveredURIs()) {
			if(uriFilter.filter(u))
				uriParser.processUri(u);
		}
		formProcessor.processForms(ctx, req, res);
	}

	public void analyzeContent(IInjectionModuleContext ctx, HttpUriRequest req, IHttpResponse res) {
		analyzeHtml(ctx, req, res);
		contentAnalyzer.processResponse(res, false, false);
	}

	private void analyzeHtml(IInjectionModuleContext ctx, HttpUriRequest req, IHttpResponse res) {
		IHTMLParseResult html = res.getParsedHTML();
		if(html == null)
			return;
		HTMLDocument document = html.getDOMDocument();
		NodeList elements = document.getElementsByTagName("*");
		for(int i = 0; i < elements.getLength(); i++) {
			Node node = elements.item(i);
			if(node instanceof Element) {
				analyzeHtmlElement(ctx, req, res, (Element) node);
			}
		}
	}

	private void analyzeHtmlElement(IInjectionModuleContext ctx, HttpUriRequest req, IHttpResponse res, Element elem) {
		boolean remoteScript = false;
		NamedNodeMap attributes = elem.getAttributes();
		final String tag = elem.getTagName().toLowerCase();
		for(int i = 0; i < attributes.getLength(); i++) {
			Node item = attributes.item(i);
			if(item instanceof Attr) {
				String n = ((Attr)item).getName().toLowerCase();
				String v = ((Attr)item).getValue().toLowerCase();
				if(match(tag, "script") && match(n, "src"))
					remoteScript = true;
				if((v != null) && (match(n, "href", "src", "action", "codebase") || (match(n, "value") && match(tag, "input")))) {
					if(v.startsWith("vega://"))
						alert(ctx, "vinfo-url-inject", "URL injection into <"+ tag + "> tag", req, res);

					if(v.startsWith("http://vega.invalid/") || v.startsWith("//vega.invalid/")) {
						if(match(tag, "script", "link"))
							alert(ctx, "vinfo-url-inject", "URL injection into actively fetched field in tag <"+ tag +"> (high risk)", req, res);
						else if(match(tag, "a"))
							alert(ctx, "vinfo-url-inject", "URL injection into anchor tag (low risk)", req, res);
						else
							alert(ctx, "vinfo-url-inject", "URL injection into tag <"+ tag +">", req, res);
					}

				}

				if((v != null) && (n.startsWith("on") || n.equals("style"))) {
					checkJavascriptXSS(ctx, req, res, v);
				}

				if(n.contains("vvv"))
					possibleXssAlert(ctx, req, res, n, n.indexOf("vvv"), "xss-inject", "Injected XSS tag into HTML attribute value");

			}
		}

		if(tag.startsWith("vvv"))
			possibleXssAlert(ctx, req, res, tag, 0, "vinfo-xss-inject", "Injected XSS tag into HTML tag name");


		if(tag.equals("style") || (tag.equals("script") && !remoteScript)) {
			String content  = elem.getTextContent();
			if(content != null)
				checkJavascriptXSS(ctx, req, res, content);
		}
	}

	private void possibleXssAlert(IInjectionModuleContext ctx, HttpUriRequest req, IHttpResponse res, String text, int offset, String type, String message) {
		final int[] xids = extractXssTag(text, offset);
		if(xids == null)
			return;
		final HttpUriRequest xssReq = ctx.getPathState().getXssRequest(xids[0], xids[1]);
		if(xssReq != null)
			alert(ctx, type, message, xssReq, res);
		else
			alert(ctx, type, message + " (from previous scan)", req, res);
	}

	private boolean match(String s, String ...options) {
		if(s == null)
			return false;
		for(int i = 0; i < options.length; i++)
			if(s.equals(options[i]))
				return true;
		return false;
	}


	private int[] extractXssTag(String text, int offset) {
		//    3     9
		// vvv000000v000000
		if(text.length() < (offset + 16))
			return null;
		if(text.charAt(offset + 9) != 'v')
			return null;

		final int[] res = new int[2];
		res[0] = extractXssId(text, offset + 3);
		res[1] = extractXssId(text, offset + 10);
		if(res[0] == -1 || res[1] == -1)
			return null;
		else
			return res;

	}

	private int extractXssId(String text, int offset) {
		if(text.length() < (offset + 6))
			return -1;
		final String idStr = text.substring(offset, offset + 6);
		try {
			return Integer.parseInt(idStr);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	private void checkJavascriptXSS(IInjectionModuleContext ctx, HttpUriRequest req, IHttpResponse res, String text) {
		if(text == null)
			return;
		int lastWordIdx = 0;
		int idx = 0;
		boolean inQuote = false;
		boolean prevSpace = true;
		while(idx < text.length()) {
			idx = maybeSkipJavascriptComment(text, idx);
			if(idx >= text.length())
				return;
			char c = text.charAt(idx);
			if(!inQuote && (c == '\'' || c == '"')) {
				inQuote = true;
				if(matchStartsWith(text, lastWordIdx, "innerHTML", "open", "url", "href", "write") &&
						matchStartsWith(text, idx + 1, "//vega.invalid/", "http://vega.invalid", "vega:")) {
					alert(ctx, "vinfo-url-inject", "Injected URL in JS/CSS code", req, res);
				}
			} else if (c == '\'' || c == '"') {
				inQuote = false;
			} else if(!inQuote && text.startsWith("vvv", idx)) {
				possibleXssAlert(ctx, req, res, text, idx, "vinfo-xss-inject", "Injected syntax into JS/CSS code");
			} else if(Character.isWhitespace(c) || c == '.') {
				prevSpace = true;
			} else if(prevSpace && Character.isLetterOrDigit(c)) {
				lastWordIdx = idx;
				prevSpace = false;
			}
			idx += 1;
		}
	}

	private boolean matchStartsWith(String str, int offset, String ...options) {
		for(int i = 0; i < options.length; i++) {
			if(str.startsWith(options[i], offset))
				return true;
		}
		return false;
	}

	private int maybeSkipJavascriptComment(String text, int idx) {
		final int max = text.length();
		final int savedIdx = idx;
		if(idx >= max)
			return idx;
		final char c = text.charAt(idx++);
		// Skip escaped characters here too
		if(c == '\\')
			return idx + 1;
		if(c != '/')
			return savedIdx;
		if(idx >= max)
			return idx;
		final char c1 = text.charAt(idx++);
		if(c1 == '/') {
			while(idx < max) {
				char cc = text.charAt(idx);
				if(cc == '\n' || c == '\r')
					return idx;
				idx += 1;
			}
			return max;
		} else if(c1 == '*') {
			int end = text.indexOf("*/", idx);
			if(end == -1)
				return max;
			else
				return end + 2;
		}
		return savedIdx;

	}
	
	private void alert(IInjectionModuleContext ctx, String type, String message, HttpUriRequest request, IHttpResponse response) {
		final String resource = request.getURI().toString();
		final String key = createAlertKey(ctx, type, request);
		ctx.publishAlert(type, key, message, request, response, "resource", resource);
	}
		
	private String createAlertKey(IInjectionModuleContext ctx, String type, HttpUriRequest request) {
		if(ctx.getPathState().isParametric()) {
			final String uri = stripQuery(request.getURI()).toString();
			return type + ":" + uri + ":" + ctx.getPathState().getFuzzableParameter().getName();
		} else {
			return type + ":" + request.getURI();
		}
	}
	
	private URI stripQuery(URI uri) {
		try {
			return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), null, null);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
