package com.subgraph.vega.ui.httpviewer.syntax.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class RequestMethodRule implements IRule {

	private final IToken token;
	
	public RequestMethodRule(IToken token) {
		this.token = token;
	}
	
	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		if(scanner.getColumn() > 0)
			return Token.UNDEFINED;

		int tokenLength = 0;
		while(true) {
			int c = scanner.read();
			if(isMethodCharacter(c)) {
				tokenLength++;
			} else if(Character.isWhitespace(c) && tokenLength > 0) {
				scanner.unread();
				return token;
			} else {
				for(int i = 0; i < tokenLength + 1; i++)
					scanner.unread();
				return Token.UNDEFINED;
			}
			
		}
	}
	
	private boolean isMethodCharacter(int c) {
		return c >= 'A' && c <= 'Z';
	}
}