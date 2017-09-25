package cop5556sp17;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import java.util.ArrayList;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import cop5556sp17.TypeCheckVisitor.TypeCheckException;

import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		public TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		binaryChain.getE0().visit(this, arg);
		binaryChain.getE1().visit(this, arg);
		TypeName c1 = binaryChain.getE0().getNameType();
		ChainElem chainElemObj = binaryChain.getE1();
		Kind opKind = binaryChain.getArrow().kind;
		switch(opKind)
		{
			case ARROW:
			{
				switch(c1)
				{
					case FRAME:
						if((chainElemObj instanceof FrameOpChain) && 
								(chainElemObj.getFirstToken().kind.equals(KW_YLOC) || chainElemObj.getFirstToken().kind.equals(KW_XLOC)))
							binaryChain.setNameType(INTEGER);
						else if((chainElemObj instanceof FrameOpChain) && 
								(chainElemObj.getFirstToken().kind.equals(KW_MOVE) || chainElemObj.getFirstToken().kind.equals(KW_HIDE)
										|| chainElemObj.getFirstToken().kind.equals(KW_SHOW)))
							binaryChain.setNameType(FRAME);
						else
							throw new TypeCheckException("Illegal chainElem" + binaryChain.toString());
						break;
					case URL:
					case FILE:
						if(chainElemObj.getNameType().isType(IMAGE))
							binaryChain.setNameType(IMAGE);
						else
							throw new TypeCheckException("Illegal chainElem" + binaryChain.toString());
						break;
					case INTEGER:
						if(chainElemObj instanceof IdentChain && chainElemObj.getNameType().isType(INTEGER) )
							binaryChain.setNameType(INTEGER);
						else
							throw new TypeCheckException("Illegal chainElem" + binaryChain.toString());
						break;
					case IMAGE:
						if((chainElemObj instanceof ImageOpChain) && 
								(chainElemObj.getFirstToken().kind.equals(OP_WIDTH) || chainElemObj.getFirstToken().kind.equals(OP_HEIGHT)))
							binaryChain.setNameType(INTEGER);
						else if(chainElemObj.getNameType().isType(FRAME))
							binaryChain.setNameType(FRAME);
						else if(chainElemObj.getNameType().isType(FILE))
							binaryChain.setNameType(NONE);
						else if((chainElemObj instanceof FilterOpChain) && 
								(chainElemObj.getFirstToken().kind.equals(OP_CONVOLVE) || chainElemObj.getFirstToken().kind.equals(OP_GRAY) 
										|| chainElemObj.getFirstToken().kind.equals(OP_BLUR)))
							binaryChain.setNameType(IMAGE);
						else if((chainElemObj instanceof ImageOpChain) && 
								(chainElemObj.getFirstToken().kind.equals(KW_SCALE)))
							binaryChain.setNameType(IMAGE);
						else if(chainElemObj instanceof IdentChain)
							binaryChain.setNameType(IMAGE);
						else if(chainElemObj instanceof IdentChain && chainElemObj.getNameType().isType(IMAGE)){
							binaryChain.setNameType(IMAGE);
						}
						else
							throw new TypeCheckException("Illegal chainElem" + binaryChain.toString());
						break;
					default:
						throw new TypeCheckException("Illegal chainElem" + binaryChain.toString());
				}	
			}
			break;
			case BARARROW:
				if(c1.isType(IMAGE) && (chainElemObj instanceof FilterOpChain) && 
						(chainElemObj.getFirstToken().kind.equals(OP_GRAY) || chainElemObj.getFirstToken().kind.equals(OP_BLUR)
								|| chainElemObj.getFirstToken().kind.equals(OP_CONVOLVE)))
					binaryChain.setNameType(IMAGE);
				else
					throw new TypeCheckException("Illegal chainElem");
				break;
			default:
				throw new TypeCheckException("opKind is not BARARROW OR ARROW : " + opKind.text);
		}
		return binaryChain;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		binaryExpression.getE0().visit(this, arg);
		binaryExpression.getE1().visit(this, arg);
		TypeName type1 = binaryExpression.getE0().getNameType();
		TypeName type2 = binaryExpression.getE1().getNameType();
		Kind op = binaryExpression.getOp().kind;
		if(type1.isType(INTEGER) && type2.isType(INTEGER))
		{
			if (op.equals(PLUS) || op.equals(MINUS) || op.equals(TIMES) || op.equals(DIV)|| op.equals(MOD))
			{
				binaryExpression.setNameType(INTEGER);
			}
			else if(op.equals(EQUAL) || op.equals(NOTEQUAL) || op.equals(LT) || op.equals(GT) || op.equals(LE) || 
					op.equals(GE) || op.equals(AND) || op.equals(OR))
			{
				binaryExpression.setNameType(BOOLEAN);
			}
			else
				throw new TypeCheckException("Expression types not compatible");
		}
		else if((type1.isType(INTEGER) && type2.isType(IMAGE)) || (type1.isType(IMAGE) && type2.isType(INTEGER)))
		{
			if(op.equals(TIMES) || op.equals(DIV) || op.equals(MOD))
				binaryExpression.setNameType(IMAGE);
			else
				throw new TypeCheckException("Expression types not compatible");
		}
		else if(type1.isType(BOOLEAN) && type2.isType(BOOLEAN))
		{
			if (op.equals(LT) || op.equals(GT) || op.equals(LE) || op.equals(GE) || 
					op.equals(EQUAL) || op.equals(NOTEQUAL) || op.equals(AND) || op.equals(OR))
			{
				binaryExpression.setNameType(BOOLEAN);
			}
			else
				throw new TypeCheckException("Expression types not compatible");
		}
		else if (type1.isType(IMAGE) && type2.isType(IMAGE))
		{
			if (op.equals(PLUS) || op.equals(MINUS))
			{
				binaryExpression.setNameType(IMAGE);
			}
			else if(op.equals(EQUAL) || op.equals(NOTEQUAL))
			{
				binaryExpression.setNameType(BOOLEAN);
			}
			else
				throw new TypeCheckException("Expression types not compatible");
		}
		else if(type1.isType(type2) && (op.equals(EQUAL) || op.equals(NOTEQUAL)))
		{
			binaryExpression.setNameType(BOOLEAN);
		}
		else
			throw new TypeCheckException("Expression types not compatible");
		return binaryExpression;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		try 
		{
			symtab.enterScope();
			ArrayList<Dec> declList = block.getDecs();
			for(Dec dec : declList){
				Dec dclr = (Dec) dec.visit(this, arg);
				if(dclr != null)
				continue;
			}
			ArrayList<Statement> stmList = block.getStatements();
			for(Statement stmt : stmList){
				Statement stm = (Statement) stmt.visit(this, arg);
				if(stm != null)
				continue;
			}
			symtab.leaveScope();
			return block;
		} 
		catch (Exception e) 
		{
			throw new TypeCheckException("Illegal Block" + block.toString());
		}
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		try 
		{
			booleanLitExpression.setNameType(BOOLEAN);
			return booleanLitExpression;
		} 
		catch (Exception e) 
		{
			throw new TypeCheckException("Illegal booleanLitExpression" + booleanLitExpression.toString());
		}
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		filterOpChain.getArg().visit(this, arg);
		if(filterOpChain.getArg().getExprList().size() == 0)
		{
			filterOpChain.setNameType(IMAGE);
		}
		else
		{
			throw new TypeCheckException("Illegal filterOpChain" + filterOpChain.toString());
		}
		return filterOpChain;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		frameOpChain.getArg().visit(this, arg);
		if(frameOpChain.firstToken.kind.equals(KW_MOVE))
		{
			if(frameOpChain.getArg().getExprList().size() == 2)
			{
				frameOpChain.setNameType(NONE);
			}
			else
				throw new TypeCheckException("Illegal frameOpChain" + frameOpChain.toString());
		}
		else if(frameOpChain.firstToken.kind.equals(KW_XLOC)|| frameOpChain.firstToken.kind.equals(KW_YLOC))
		{
			if(frameOpChain.getArg().getExprList().size() == 0)
			{
				frameOpChain.setNameType(INTEGER);
			}
			else
				throw new TypeCheckException("Illegal frameOpChain" + frameOpChain.toString());
		}
		else if(frameOpChain.firstToken.kind.equals(KW_SHOW)|| frameOpChain.firstToken.kind.equals(KW_HIDE))
		{
			if(frameOpChain.getArg().getExprList().size() == 0)
			{
				frameOpChain.setNameType(NONE);
			}
			else
				throw new TypeCheckException("Illegal frameOpChain" + frameOpChain.toString());
		}
		else
			throw new TypeCheckException("Illegal frameOpChain" + frameOpChain.toString());
		return frameOpChain;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		String idText = identChain.getFirstToken().getText();
		Dec decl = symtab.lookup(idText);
		if(decl != null)
		{
			decl.setNameType(null);
			identChain.setNameType(decl.getNameType());
			identChain.setDeclr(decl);
		}
		return identChain;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		String idText = identExpression.getFirstToken().getText();
		Dec symTabDec = symtab.lookup(idText);
		if(symTabDec != null)
		{
			symTabDec.setNameType(null);
			identExpression.setNameType(symTabDec.getNameType());
			identExpression.setDec(symTabDec);
		}
		return identExpression;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		ifStatement.getE().visit(this, arg);
		ifStatement.getB().visit(this, arg);
		if(ifStatement.getE().getNameType().isType(BOOLEAN))
			return ifStatement;
		else
			throw new TypeCheckException("Illegal ifStatement" + ifStatement.toString());
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		try 
		{
			intLitExpression.setNameType(INTEGER);
			return intLitExpression;
		} 
		catch (Exception e) 
		{
			throw new TypeCheckException("Illegal intLitExpression" + intLitExpression.toString());
		}
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.getE().visit(this, arg);
		if(sleepStatement.getE().getNameType().isType(INTEGER))
			return sleepStatement;
		else
			throw new TypeCheckException("Illegal sleepStatement" + sleepStatement.toString());
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception 
	{
		whileStatement.getE().visit(this, arg);
		whileStatement.getB().visit(this, arg);
		if(whileStatement.getE().getNameType().isType(BOOLEAN))
			return whileStatement;
		else
			throw new TypeCheckException("Illegal whileStatement" + whileStatement.toString());
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		boolean dInsert = symtab.insert(declaration.getIdent().getText(), declaration);
		if(dInsert){
			declaration.setNameType(null);
			return declaration;
		}
		else
			throw new TypeCheckException("Illegal declaration" + declaration.toString());
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		try 
		{
			ArrayList<ParamDec> param = program.getParams();
			for(ParamDec p1 : param)
			{
				p1.visit(this, arg);
			}
			Block bl = program.getB();
			bl.visit(this, arg);
			return program;
		} 
		catch (Exception e) 
		{
			throw new TypeCheckException("Illegal Program" + program.toString());
		}
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception 
	{
		assignStatement.getVar().visit(this, arg);
		assignStatement.getE().visit(this, arg);
		if(assignStatement.getVar().getDec().getNameType().isType(assignStatement.getE().getNameType()))
			return assignStatement;
		else
			throw new TypeCheckException("Illegal assignStatement" + assignStatement.toString());
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		String idText = identX.getText();
		Dec decSymTab = symtab.lookup(idText);
		if(decSymTab != null)
		{
			decSymTab.setNameType(null);
			identX.setDec(decSymTab);
			identX.dec.setNameType(null);
		}
		return identX;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		boolean pInsert = symtab.insert(paramDec.getIdent().getText(), paramDec);
		if(pInsert){
			paramDec.setNameType(null);
			return paramDec;
		}
		else
			throw new TypeCheckException("The insertion of paramDec is illegal" + paramDec.toString());
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		try 
		{
			constantExpression.setNameType(INTEGER);
			return constantExpression;
		} 
		catch (TypeCheckException e) 
		{
			return null;
		}
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		imageOpChain.getArg().visit(this, arg);
		if (imageOpChain.firstToken.kind.equals(KW_SCALE))
		{
			if(imageOpChain.getArg().getExprList().size() == 1)
			{
				imageOpChain.setNameType(IMAGE);
			}
			else
				throw new TypeCheckException("Illegal ImageOpChain" + imageOpChain.toString());
		}
		else if(imageOpChain.firstToken.kind.equals(OP_HEIGHT) || imageOpChain.firstToken.kind.equals(OP_WIDTH))
		{
			if(imageOpChain.getArg().getExprList().size() == 0)
			{
				imageOpChain.setNameType(INTEGER);
			}
			else
				throw new TypeCheckException("Illegal ImageOpChain" + imageOpChain.toString());
		}
		return imageOpChain;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		for(Expression expr : tuple.getExprList())
		{
			expr.visit(this, arg);
			if(expr.getNameType() != INTEGER)
				throw new TypeCheckException("The Type is not Integer" + expr.toString());
		}
		return tuple;
	}


}
