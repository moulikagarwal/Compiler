package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;
import java.util.ArrayList;
import java.util.List;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.*;

public class Parser {
	
	ArrayList<Kind> arrStatement,arrChainElem,arrRelOp,arrWeakOp,arrStrongOp,arrDec,paramdec,filterOpChain,frameOpChain,imageOpChain;
	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
		arrStatement = new ArrayList<Kind>();
		arrChainElem = new ArrayList<Kind>();
		arrRelOp = new ArrayList<Kind>();
		arrWeakOp = new ArrayList<Kind>();
		arrStrongOp = new ArrayList<Kind>();
		arrDec = new ArrayList<Kind>();
		paramdec = new ArrayList<Kind>();
		filterOpChain = new ArrayList<Kind>();
		frameOpChain = new ArrayList<Kind>();
		imageOpChain = new ArrayList<Kind>();
		parserHelper();
	}

	void parserHelper()
	{
		arrDec.add(KW_FRAME);arrDec.add(KW_INTEGER);arrDec.add(KW_BOOLEAN);arrDec.add(KW_IMAGE);
		
		paramdec.add(KW_INTEGER);paramdec.add(KW_BOOLEAN);paramdec.add(KW_URL);paramdec.add(KW_FILE);
		
		arrRelOp.add(LE);arrRelOp.add(LT);arrRelOp.add(GT);arrRelOp.add(GE);arrRelOp.add(EQUAL);arrRelOp.add(NOTEQUAL);
		
		arrWeakOp.add(PLUS);arrWeakOp.add(MINUS);arrWeakOp.add(OR);
		
		arrStrongOp.add(MOD);arrStrongOp.add(AND);arrStrongOp.add(TIMES);arrStrongOp.add(DIV);

		arrChainElem.add(IDENT);arrChainElem.add(OP_BLUR);arrChainElem.add(OP_GRAY); arrChainElem.add(OP_CONVOLVE);arrChainElem.add(KW_SHOW);
		arrChainElem.add(KW_HIDE);arrChainElem.add(KW_MOVE);arrChainElem.add(KW_XLOC);arrChainElem.add(OP_WIDTH);arrChainElem.add(KW_SCALE);
		arrChainElem.add(KW_YLOC);arrChainElem.add(OP_HEIGHT);
		
		filterOpChain.add(OP_BLUR);filterOpChain.add(OP_GRAY);filterOpChain.add(OP_CONVOLVE);
		
		frameOpChain.add(KW_SHOW);frameOpChain.add(KW_YLOC);frameOpChain.add(KW_HIDE);frameOpChain.add(KW_MOVE);frameOpChain.add(KW_XLOC);
		
		imageOpChain.add(OP_WIDTH);imageOpChain.add(OP_HEIGHT);imageOpChain.add(KW_SCALE);
		
		arrStatement.add(OP_SLEEP);arrStatement.add(KW_WHILE);arrStatement.add(KW_IF); arrStatement.add(KW_SCALE);arrStatement.add(KW_SHOW);
		arrStatement.add(IDENT);arrStatement.add(OP_BLUR);arrStatement.add(OP_WIDTH);arrStatement.add(OP_HEIGHT);arrStatement.add(KW_MOVE);
		arrStatement.add(OP_GRAY); arrStatement.add(OP_CONVOLVE);arrStatement.add(KW_YLOC);arrStatement.add(KW_XLOC);arrStatement.add(KW_HIDE);
	}
	
	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	Program parse() throws SyntaxException {
		Program prg = program();
		matchEOF();
		return prg;
	}

	Expression expression() throws SyntaxException {
		//TODO
		Expression expr0 = null;
		Expression expr1 = null;
		Token firstToken = t;
		try{
			expr0 = term();
				while(arrRelOp.contains(t.kind)){
					Token op = t;
					consume();
					expr1 = term();
					expr0 = new BinaryExpression(firstToken,expr0,op,expr1);
				}
				return expr0;
		}
		catch(SyntaxException e){
			throw new SyntaxException("illegal expression "+ t.getLinePos().toString());
		}
	}

	Expression term() throws SyntaxException {
		//TODO
		Expression expr0 = null;
		Expression expr1 = null;
		Token firstToken = t;
		 try{
			expr0 = elem();
				while(arrWeakOp.contains(t.kind)){
					Token op = t;
					consume();
					expr1 = elem();
					expr0 = new BinaryExpression(firstToken,expr0,op,expr1);
				}
				return expr0;
		}
		catch(SyntaxException e){
			throw new SyntaxException("illegal term "+ t.getLinePos().toString());
		}
	}

	Expression elem() throws SyntaxException {
		//TODO
		Expression expr0 = null;
		Expression expr1 = null;
		Token firstToken = t;
		try{
			expr0 = factor();
				while(arrStrongOp.contains(t.kind)){
					Token op = t;
					consume();
					expr1 = factor();
					expr0 = new BinaryExpression(firstToken,expr0,op,expr1);
				}
				return expr0;
		}
		catch(SyntaxException e){
			throw new SyntaxException("illegal element "+ t.getLinePos().toString());
		}
	}

	Expression factor() throws SyntaxException {
		Kind kind = t.kind;
		Expression expr = null;
		Token firstToken = t;
		switch (kind) {
		case IDENT: {
						expr = new IdentExpression(firstToken);
						consume();
					}
					break;
		case INT_LIT: {
						expr = new IntLitExpression(firstToken);
						consume();
					  }
					  break;
		case KW_TRUE: {
						 expr = new BooleanLitExpression(firstToken);
						 consume();
					  }
						break;
		case KW_FALSE: {
						 expr = new BooleanLitExpression(firstToken);
						 consume();
					   }
						break;
		case KW_SCREENWIDTH:{
							  expr = new ConstantExpression(firstToken);
							  consume();
							}
							break;
		case KW_SCREENHEIGHT: {
								expr = new ConstantExpression(firstToken);
								consume();
							  }
							  break;
		case LPAREN: {
						consume();
						expr = expression();
						match(RPAREN);
					 }
					 break;
		default:
			throw new SyntaxException("illegal factor "+ t.getLinePos().toString());
		}
		return expr;
	}
	
	
	Block block() throws SyntaxException {
		//TODO
		ArrayList<Dec> decList = new ArrayList<Dec>();
		ArrayList<Statement> statementList = new ArrayList<Statement>();
		Token firstToken = t;
		Block blk = null;
		match(LBRACE);
		while(arrDec.contains(t.kind)||arrStatement.contains(t.kind)){
			if(arrDec.contains(t.kind)){
				decList.add(dec());
			}
			else if(arrStatement.contains(t.kind)){
				statementList.add(statement());
			}
			else{
				throw new SyntaxException("illegal block "+ t.getLinePos().toString());
			}
		}
		blk = new Block(firstToken,decList,statementList);
		match(RBRACE);
		return blk;
	}

	
	ParamDec paramDec() throws SyntaxException {
		//TODO
		Token firstToken = t;
		ParamDec pDec = null;
		if(paramdec.contains(t.kind))
		{
			consume();
			Token idt = t;
			match(IDENT);	
			pDec = new ParamDec(firstToken, idt);
		}
		else{
			throw new SyntaxException("illegal parameterDeclaration "+ t.getLinePos().toString());
		}
		return pDec;
	}

	
	Dec dec() throws SyntaxException {
		//TODO
		Token firstToken = t;
		Dec dDec = null;
		if(arrDec.contains(t.kind))
		{
			consume();
			Token idt = t;
			match(IDENT);
			dDec = new Dec(firstToken, idt);
		}
		else
		{
			throw new SyntaxException("illegal declaration "+ t.getLinePos().toString());
		}
		return dDec;
	}

	
	Statement statement() throws SyntaxException {
		//TODO
		Expression expr = null;
		Statement stmt = null;
		Block blk = null;
		IdentLValue iVal = null;
		Token firstToken = t;
		if(arrStatement.contains(t.kind))
		{
			switch(t.kind)
			{
			case OP_SLEEP:  {
							consume();
							expr = expression();
							stmt = new SleepStatement(firstToken, expr);
							match(SEMI);
							}
							break;
			case IDENT:		{
								if(scanner.peek().kind.equals(ASSIGN))
								{
									iVal = new IdentLValue(firstToken);
									consume();
									consume();
									expr = expression();
									match(SEMI);
									stmt = new AssignmentStatement(firstToken,iVal,expr);
								}
								else if(scanner.peek().kind.equals(ARROW) || scanner.peek().kind.equals(BARARROW))
								{
									stmt = chain();
									match(SEMI);
								}
								else{
									throw new SyntaxException("Illegal statement "+ t.getLinePos().toString());
								}
							}
							break;
			case KW_IF:		{
							consume();
							match(LPAREN);
							expr = expression();
							match(RPAREN);
							blk = block();
							stmt = new IfStatement(firstToken,expr,blk);
							}
							break;
			case KW_WHILE:  {
							consume();
							match(LPAREN);
							expr = expression();
							match(RPAREN);
							blk = block();
							stmt = new WhileStatement(firstToken, expr,blk);
							}
							break;
			default: {	
						stmt = chain();
						match(SEMI);
						}
			}
		}
		else
		{
			throw new SyntaxException("Illegal ParameterStatement"+ t.getLinePos().toString());
		}
		return stmt;
	}
	
	
	Program program() throws SyntaxException {
		//TODO
		Program prg = null;
		Block blk = null;
		ArrayList<ParamDec> pDec = new ArrayList<ParamDec>();
		Token firstToken = t;
		if(t.kind.equals(IDENT)){
			consume();
			if(t.kind.equals(LBRACE)){
				blk = block();
			}
			else if(paramdec.contains(t.kind)) {
				pDec.add(paramDec());
				while(t.kind.equals(COMMA)){
					consume();
					pDec.add(paramDec());
				}
				blk = block();
			}
			else{
				throw new SyntaxException("illegal program "+ t.getLinePos().toString());
			}
			prg = new Program(firstToken,pDec,blk);
		}
		else{
			throw new SyntaxException("illegal program "+ t.getLinePos().toString());
		}
		return prg;
	}
	

	Chain chain() throws SyntaxException {
		//TODO
		ChainElem cElem0 = null;
		ChainElem cElem1 = null;
		Chain chn = null;
		Token firstToken = t;
		if(arrChainElem.contains(t.kind))
		{
			cElem0 = chainElem();
			Token aOp = t;
			arrowOp();
			cElem1 = chainElem();
			chn = new BinaryChain(firstToken, cElem0, aOp, cElem1);
			while(t.kind.equals(ARROW) || t.kind.equals(BARARROW))
			{
				Token aOp1 = t;
				arrowOp();
				cElem0 = chainElem();
				chn = new BinaryChain(firstToken, chn, aOp1, cElem0);
			}			
		}
		else{
			throw new SyntaxException("Illegal chainStatement "+ t.getLinePos().toString());
		}
		return chn;
	}
	
	
	void arrowOp() throws SyntaxException {
		if(t.kind.equals(ARROW)||(t.kind.equals(BARARROW))){
			consume();
		}
		else{
			throw new SyntaxException("illegal arrowOperation "+ t.getLinePos().toString());
		}
	}

	
	ChainElem chainElem() throws SyntaxException {
		//TODO
		Tuple tup = null;
		ChainElem cElem = null;
		Token firstToken = t;
			if(t.kind.equals(IDENT))
			{
				cElem = new IdentChain(firstToken);
				consume();
			}
			else if(filterOpChain.contains(t.kind))
			{
				consume();
				tup = arg();
				cElem = new FilterOpChain(firstToken,tup);
			}
			else if(frameOpChain.contains(t.kind))
			{
				consume();
				tup = arg();
				cElem = new FrameOpChain(firstToken,tup);
			}
			else if(imageOpChain.contains(t.kind))
			{
				consume();
				tup = arg();
				cElem = new ImageOpChain(firstToken,tup);
			}
		else{
			throw new SyntaxException("Illegal chainElement "+ t.getLinePos().toString());
		}
		return cElem;
	}

	
	Tuple arg() throws SyntaxException {
		//TODO
		Tuple tup = null;
		ArrayList<Expression> expr = new ArrayList<Expression>();
		Token firstToken = t;
		if(t.kind.equals(LPAREN)){
			consume();
			expr.add(expression());  
			while(t.kind.equals(COMMA)){
				consume();
				expr.add(expression());
			}
			match(RPAREN);
		}
		tup = new Tuple(firstToken,expr);
		return tup;

	}
	
	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind.equals(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.kind.equals(kind)) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + "expected " + kind +"at "+t.getLinePos().toString());
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	@SuppressWarnings("unused")
	private Token match(Kind... kinds) throws SyntaxException {
		// TODO. Optional but handy
		return null; //replace this statement
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
