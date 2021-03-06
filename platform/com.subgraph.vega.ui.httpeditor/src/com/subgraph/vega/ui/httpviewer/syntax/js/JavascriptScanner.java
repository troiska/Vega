package com.subgraph.vega.ui.httpviewer.syntax.js;

import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

import com.subgraph.vega.ui.httpviewer.Colors;
import com.subgraph.vega.ui.httpviewer.syntax.AbstractScanner;

public class JavascriptScanner extends AbstractScanner {

	private final static String[] KEYWORDS = { 
		"break", "const", "continue", "delete", "do", "while", "export", 
		"function", "for", "in", "if", "else", "import", "in", "instanceOf", "label", 
		"let", "new", "return", "switch", "this", "throw", "try", "catch", 
		"typeof", "var", "void", "while", "with", "yield" };

	private final static String[] CONSTANTS = {
		"true", "false", "null"
	};
	
	public JavascriptScanner(Colors colors) {
		super(colors);
	}

	@Override
	protected void initializeRules(List<IRule> rules) {
		final IToken keyword = createToken(Colors.KEYWORD);
		final IToken constant = createToken(Colors.ENCODED_CHAR);
		final IToken string = createToken(Colors.STRING);
		final IToken comment = createToken(Colors.SINGLE_LINE_COMMENT);
		final IToken other = createToken(Colors.OTHER);
		
		rules.add(new EndOfLineRule("//", comment));
		rules.add(new SingleLineRule("\"", "\"", string, '\\'));
		rules.add(new SingleLineRule("'", "'", string, '\\'));
		
		rules.add(new WhitespaceRule(new JavascriptWhitespaceDetector()));
		WordRule wordRule = new WordRule(new JavascriptWordDetector(), other);
		
		wordRule.addWord("function", keyword);
		for(String s : KEYWORDS)
			wordRule.addWord(s, keyword);
		for(String s: CONSTANTS) 
			wordRule.addWord(s, constant);
		
		rules.add(wordRule);
	}

}
