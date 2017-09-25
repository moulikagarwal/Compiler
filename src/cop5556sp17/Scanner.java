package cop5556sp17;

import java.util.ArrayList;
import java.util.Arrays;

public class Scanner {
	/**
	 * Kind enum
	 */
	
	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
		
	}
	public static enum State {
		START("START"),AFTER_EQ("AFTER_EQ"),AFTER_NTEQ("AFTER_NTEQ"),AFTER_DIV("AFTER_DIV"),
		AFTER_MINUS("AFTER_MINUS"),AFTER_LESSTHAN("AFTER_LESSTHAN"),AFTER_BAR("AFTER_BAR"), 
		IN_DIGIT("IN_DIGIT"),IN_IDENT("IN_IDENT"), COMMENT_START("COMMENT_START"), 
		AFTER_TIMES("AFTER_TIMES"),AFTER_GREATERTHAN("AFTER_GREATERTHAN");
		
		private String text;

		State(String text) {
			this.text = text;
		}
		
		String getText() {
			return text;
		}
	}
/**
 * Thrown by Scanner when an illegal character is encountered
 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}
	
	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
	public IllegalNumberException(String message){
		super(message);
		}
	}
	

	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;
		
		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}


	public class Token {
		public final Kind kind;
		public final int pos;  //position in input array
		public final int length;  

		//returns the text of this Token
		public String getText() {
			if(kind.text == "")
			return chars.substring(this.pos,this.pos+this.length).toString();
			else
				return this.kind.text;
		}
		
		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos(){
			int[] lineArray=new int[endofline.size()];
			for(int i=0;i<endofline.size();i++){
				lineArray[i]=endofline.get(i);
			}
			Arrays.sort(lineArray);
			int line=Arrays.binarySearch(lineArray,this.pos);
			int linePos;
			if(line<0){
				line=(line*-1)-2;
			}
			linePos=lineArray[line];
			return new LinePos(line,this.pos-linePos);
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		/** 
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 * 
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException{
			return Integer.parseInt(chars.substring(this.pos,this.pos+this.length));
		}
		
		@Override
		  public int hashCode() {
		   final int prime = 31;
		   int result = 1;
		   result = prime * result + getOuterType().hashCode();
		   result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		   result = prime * result + length;
		   result = prime * result + pos;
		   return result;
		  }

		  @Override
		  public boolean equals(Object obj) {
		   if (this == obj) {
		    return true;
		   }
		   if (obj == null) {
		    return false;
		   }
		   if (!(obj instanceof Token)) {
		    return false;
		   }
		   Token other = (Token) obj;
		   if (!getOuterType().equals(other.getOuterType())) {
		    return false;
		   }
		   if (kind != other.kind) {
		    return false;
		   }
		   if (length != other.length) {
		    return false;
		   }
		   if (pos != other.pos) {
		    return false;
		   }
		   return true;
		  }

		 

		  private Scanner getOuterType() {
		   return Scanner.this;
		  }
		
	}


	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();
		endofline=new ArrayList<Integer>();
		endofline.add(0);
	}


	
	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		int pos = 0; 
		int length=this.chars.length();
		State state=State.START;
		int startPos = 0;
	    int ch;

	    while (pos <= length) {
	        ch = pos < length ? chars.charAt(pos) : -1;
	        switch (state) {
	            case START: {
	            	pos = skipWhiteSpace(pos);
	                ch = pos < length ? chars.charAt(pos) : -1;
	                startPos = pos;
	                switch (ch) {
	                    case -1: {tokens.add(new Token(Kind.EOF, pos, 0)); pos++;}  break;
	                    case '+': {tokens.add(new Token(Kind.PLUS, startPos, 1));pos++;} break;
	                    case '*': {tokens.add(new Token(Kind.TIMES, startPos, 1));pos++;} break;
	                    case '=': {state = State.AFTER_EQ;pos++;}break;
	                    case '!': {state = State.AFTER_NTEQ;pos++;}break;
	                    case '/': {state = State.AFTER_DIV;pos++;}break;
	                    case '-': {state = State.AFTER_MINUS;pos++;}break;
	                    case '<': {state = State.AFTER_LESSTHAN;pos++;}break;
	                    case '>': {state = State.AFTER_GREATERTHAN;pos++;}break;
	                    case '|': {state=State.AFTER_BAR;pos++;}break;
	                    case ';': {tokens.add(new Token(Kind.SEMI, startPos, 1));pos++;}break;
	                    case '%': {tokens.add(new Token(Kind.MOD, startPos, 1));pos++;}break;
	                    case '&': {tokens.add(new Token(Kind.AND, startPos, 1));pos++;}break;
	                    case ',': {tokens.add(new Token(Kind.COMMA, startPos, 1));pos++;}break;
	                    case '(': {tokens.add(new Token(Kind.LPAREN, startPos, 1));pos++;}break;
	                    case ')': {tokens.add(new Token(Kind.RPAREN, startPos, 1));pos++;}break;
	                    case '{': {tokens.add(new Token(Kind.LBRACE, startPos, 1));pos++;}break;
	                    case '}': {tokens.add(new Token(Kind.RBRACE, startPos, 1));pos++;}break;
	                    case '0': {tokens.add(new Token(Kind.INT_LIT,startPos, 1));pos++;}break;
	                    default: {
	                        if (Character.isDigit(ch)) {state = State.IN_DIGIT;pos++;} 
	                        else if (Character.isJavaIdentifierStart(ch)) {
	                             state = State.IN_IDENT;
	                             pos++;
	                         } 
	                         else {throw new IllegalCharException(
	                                    "illegal char '" +(char)ch+"' at pos "+pos);
	                         }
	                      }
	                }

	            }  break;
	            case IN_DIGIT: {
	            	if(Character.isDigit(ch)) pos++;
	            	else{
	            		checkIntegerValidity(startPos, pos);
	            		tokens.add(new Token(Kind.INT_LIT,startPos, pos-startPos));
	            		state = State.START;
	            	}
	            }  break;
	            case IN_IDENT: {
	                if (Character.isJavaIdentifierPart(ch)) pos++;
	                else {
	                	 Kind enumVal=getEnumByValue(this.chars.substring(startPos, pos));
	                	 if(enumVal != null) tokens.add(new Token(enumVal, startPos, pos-startPos));
	                	 else tokens.add(new Token(Kind.IDENT, startPos, pos-startPos));
	                     state = State.START;
	                }

	            }  break;
	            case AFTER_EQ: {
	            	if(ch != '='){
	            		throw new IllegalCharException(
                                "illegal char " +ch+" at pos "+pos);
	            	}else{
	            		tokens.add(new Token(Kind.EQUAL, startPos, 2));
	            		state=State.START;
	            		pos++;
	            	}
	            }  break;
	            case AFTER_NTEQ:{
	            	if (ch == '='){
	            		tokens.add(new Token(Kind.NOTEQUAL, startPos,2));
	            		pos++;
	            	}
	                else tokens.add(new Token(Kind.NOT, startPos,1));
	            	state = State.START;
	            } break;
	            case AFTER_MINUS:{
	            	if (ch == '>'){
	            		tokens.add(new Token(Kind.ARROW, startPos,2));
	            		pos++;
	            	}
	                else tokens.add(new Token(Kind.MINUS, startPos,1));
	            	state = State.START;
	            }break;
	            case AFTER_BAR:{
	            	if (ch == '-' && (pos+1)<=this.chars.length() && this.chars.charAt(pos+1) == '>'){
	            		tokens.add(new Token(Kind.BARARROW, startPos,3));
	            		pos+=2;
	            	}
	            	else tokens.add(new Token(Kind.OR, startPos,1));
	            	state = State.START;
	            }break;
	            case AFTER_LESSTHAN:{
	            	if(ch == '='){
	            		tokens.add(new Token(Kind.LE, startPos,2));
	            		pos++;
	            	}
	            	else if(ch == '-'){
	            		tokens.add(new Token(Kind.ASSIGN, startPos,2));
	            		pos++;
	            	}else tokens.add(new Token(Kind.LT, startPos,1));
	            	state=State.START;
	            }break;
	            case AFTER_GREATERTHAN:{
	            	if(ch == '='){
	            		tokens.add(new Token(Kind.GE, startPos,2));
	            		pos++;
	            	}else tokens.add(new Token(Kind.GT, startPos,1));
	            	state=State.START;
	            }break;
	            case AFTER_DIV:{
	            	if(ch == '*'){
	            		state=State.COMMENT_START;
	            		pos++;
	            	}
	            	else{
	            		tokens.add(new Token(Kind.DIV, startPos,1));
	            		state=State.START;
	            	}
	            	
	            }break;
	            case COMMENT_START:{
	            	if(ch == '*' && (pos+1)<this.chars.length() && this.chars.charAt(pos+1) == '/'){
	            		pos+=2;
	            		state=State.START;
	            	}else {
	            		if(ch=='\n') this.endofline.add(pos+1);
		            	pos++;
	            	} 
	            }break;
	            default:  assert false;
	        }
	    } 

		tokens.add(new Token(Kind.EOF,pos,0));
		return this;  
	}



	private Kind getEnumByValue(String text) {
		for(Kind e: Kind.values()) {
		    if(e.getText().equals(text)) {
		      return e;
		    }
		}
		return null;
	}



	private void checkIntegerValidity(int startPos, int i) throws IllegalNumberException {
		try{
			long num=Integer.parseInt(this.chars.substring(startPos, i));
		}catch(NumberFormatException e){
			throw new IllegalNumberException("Integer out of range exception at line "+i);
		}
	}

	private int skipWhiteSpace(int pos) {
		int charLen=this.chars.length();
		int i;
		for(i=pos;i<charLen;i++){
			if(this.chars.charAt(i) =='\n') endofline.add(i+1);
			else if(Character.isWhitespace(this.chars.charAt(i))) continue;
			else return i;
		}
		return i;
	}



	final ArrayList<Token> tokens;
	final ArrayList<Integer> endofline;
	final String chars;
	int tokenNum;
	boolean commentEnded;
	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..  
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}
	
	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */
	public Token peek(){
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);		
	}

	

	/**
	 * Returns a LinePos object containing the line and position in line of the 
	 * given token.  
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		return t.getLinePos();
	}


}

