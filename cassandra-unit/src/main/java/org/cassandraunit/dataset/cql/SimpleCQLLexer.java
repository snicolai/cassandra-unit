package org.cassandraunit.dataset.cql;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Steve Nicolai
 */

/* see the full CQL grammar at:
 * https://github.com/apache/cassandra/blob/trunk/src/java/org/apache/cassandra/cql3/Cql.g
 * 
 * This parser a series of lines, removes comments and breaks the lines into statements
 * at semicolon boundaries.
 */

public class SimpleCQLLexer {
	
	String text;
	
    enum LexState {
    	
    	DEFAULT,
    	INSINGLELINECOMMENT,
    	INMULTILINECOMMENT,
    	INQUOTESTRING,
    	INSQUOTESTRING,
    	
    }
    
    LexState state;
    int pos;

	public SimpleCQLLexer(List<String> lines) {
    	StringBuffer t = new StringBuffer();
    	for (String l : lines) {
    		t.append(l);
    		t.append('\n');
    	}
    	
    	text = t.toString();
    	pos = 0;
    	state = LexState.DEFAULT;
	}
	
	char getChar() {
		if (pos < text.length())
			return text.charAt(pos++);
		else
			return 0;
	}
	
	char peekAhead() {
		if (pos < text.length())
			return text.charAt(pos);  // don't advance
		else
			return 0;
	}
	
	/* Skip the peekAhead character and not copy it to the output.
	 */
	void advance() {
		pos++;
	}
	
	List<String> getStatements() {
        List<String> statements = new ArrayList<String>();
        StringBuffer statementUnderConstruction = new StringBuffer();

        char c;
    	while ((c = getChar()) != 0) {    		
    		switch (state) {
    		case DEFAULT: 
    			if (c == '/' && peekAhead() == '/') {
    				state = LexState.INSINGLELINECOMMENT;
    				advance();
    			} else if (c == '-' && peekAhead() == '-') {
    				state = LexState.INSINGLELINECOMMENT;
    				advance();
    			} else if (c == '/' && peekAhead() == '*') {
    				state = LexState.INMULTILINECOMMENT;
    				advance();
    			} else {
    				statementUnderConstruction.append(c);
    				if (c == '\"') {
        				state = LexState.INQUOTESTRING;
    				} else if (c == '\'') {
    					state = LexState.INSQUOTESTRING;
    				} else if (c == ';') {
                        statements.add(statementUnderConstruction.toString());
                        statementUnderConstruction.setLength(0);
        			}
    			}
    			break;
    		
    		case INSINGLELINECOMMENT:
    			if (c == '\n') {
    				state = LexState.DEFAULT;
    			}
    			break;
    			
    		case INMULTILINECOMMENT:
    			if (c == '*' && peekAhead() == '/') {
    				state = LexState.DEFAULT;
    				advance();
    			}
    			break;
    		
    		case INQUOTESTRING:
				statementUnderConstruction.append(c);
    			if (c == '"' && peekAhead() != '"') {
    				state = LexState.DEFAULT;
    			}
    			break;
    		
			case INSQUOTESTRING:
				statementUnderConstruction.append(c);
				if (c == '\'' && peekAhead() != '\'') {
					state = LexState.DEFAULT;
				}
				break;
			}

    	}
    	
    	if (statementUnderConstruction.length() > 0) {
            statements.add(statementUnderConstruction.toString());
    	}
    	    	
    	return statements;
	}
	
}
