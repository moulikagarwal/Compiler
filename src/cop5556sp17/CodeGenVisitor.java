package cop5556sp17;

import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;


import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
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
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	private int slot=0;
	private int param_iter=0;
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		ArrayList<ParamDec> params = program.getParams();
		for (ParamDec dec : params)
			dec.visit(this, mv);
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
		
		//TODO  visit the local variables
		ArrayList<Dec> declr =program.getB().getDecs();
		for (int i=0;i<declr.size();i++)
		{
			Dec dcl=declr.get(i);
			String fieldName=dcl.getIdent().getText();
			String fieldType=dcl.getNameType().getJVMTypeDesc();
			int slotNum=dcl.getSlotNumber();
			mv.visitLocalVariable(fieldName, fieldType, null, startRun, endRun, slotNum);
		}
		
		//mv.visitLocalVariable("args", "[Ljava/lang/String;", null, startRun, endRun, 1);
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method


		cw.visitEnd();//end of class

		//generate classfile and return it
		return cw.toByteArray();
	}


	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getNameType());
		assignStatement.getVar().visit(this, arg);
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		Chain bin_chain_0=binaryChain.getE0();
		ChainElem bin_chain_1=binaryChain.getE1();
		bin_chain_0.visit(this, true);
		if(bin_chain_0.getNameType().getJVMTypeDesc().equals(PLPRuntimeImageIO.URLDesc)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL", PLPRuntimeImageIO.readFromURLSig,false);
		}
		else if(bin_chain_0.getNameType().getJVMTypeDesc().equals(PLPRuntimeImageIO.FileDesc)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile", PLPRuntimeImageIO.readFromFileDesc,false);
		}
		mv.visitInsn(DUP);
		bin_chain_1.visit(this, false);
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {

		binaryExpression.getE0().visit(this,arg);
		binaryExpression.getE1().visit(this,arg);
		TypeName expName=binaryExpression.getNameType();
		Label expStart = new Label();
		Label expEnd = new Label();
		
		switch(binaryExpression.getOp().kind){
			case AND:
				mv.visitInsn(IAND);
				break;
			case MINUS:
				if(expName.equals(IMAGE)) {
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,"sub", PLPRuntimeImageOps.subSig, false);
				} else {
					mv.visitInsn(ISUB);
				}
				break;
				
			case PLUS:
				if(expName.equals(IMAGE)) {
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,"add", PLPRuntimeImageOps.addSig, false);
				} else {
					mv.visitInsn(IADD);
				}
				break;
			case TIMES:
				if(expName.equals(IMAGE)) {
					if(binaryExpression.getE0().getNameType() == TypeName.INTEGER)
						mv.visitInsn(SWAP);
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,"mul", PLPRuntimeImageOps.mulSig, false);
				} else {
					mv.visitInsn(IMUL);
				}
				break;
			case DIV:
				if(expName.equals(IMAGE)) {
					if(binaryExpression.getE0().getNameType() == TypeName.INTEGER)
						mv.visitInsn(SWAP);
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,"div", PLPRuntimeImageOps.divSig, false);
				} else {
					mv.visitInsn(IDIV);
				}
				break;
			case OR:
				mv.visitInsn(IOR);
				break;
			case MOD:
				if(expName.equals(IMAGE)) {
					if(binaryExpression.getE0().getNameType() == TypeName.INTEGER)
						mv.visitInsn(SWAP);
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,"mod", PLPRuntimeImageOps.modSig, false);
				} else {
					mv.visitInsn(IREM);
				}
				break;
			case LE: {
				mv.visitJumpInsn(IF_ICMPLE, expStart);
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, expEnd);
				mv.visitLabel(expStart);
				mv.visitLdcInsn(true);
				mv.visitLabel(expEnd);
			}
			break;
			case LT: {
				mv.visitJumpInsn(IF_ICMPLT, expStart);
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, expEnd);
				mv.visitLabel(expStart);
				mv.visitLdcInsn(true);
				mv.visitLabel(expEnd);
			}
			break;
			
			case GT: {
				mv.visitJumpInsn(IF_ICMPGT, expStart);
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, expEnd);
				mv.visitLabel(expStart);
				mv.visitLdcInsn(true);
				mv.visitLabel(expEnd);
			}
			break;
			case GE: {
				mv.visitJumpInsn(IF_ICMPGE, expStart);
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, expEnd);
				mv.visitLabel(expStart);
				mv.visitLdcInsn(true);
				mv.visitLabel(expEnd);
			}
			break;
			case NOTEQUAL: {
				mv.visitJumpInsn(IF_ICMPNE, expStart);
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, expEnd);
				mv.visitLabel(expStart);
				mv.visitLdcInsn(true);
				mv.visitLabel(expEnd);
			}
			break;
			case EQUAL: {
                if (binaryExpression.getE0().getNameType().isType(TypeName.INTEGER, TypeName.BOOLEAN)) {
                	mv.visitJumpInsn(IF_ICMPEQ, expStart);
				}
				else {
					mv.visitJumpInsn(IF_ACMPEQ, expStart);
				}
        		mv.visitLdcInsn(false);
        		mv.visitJumpInsn(GOTO, expEnd);
        		mv.visitLabel(expStart);
        		mv.visitLdcInsn(true);
        		mv.visitLabel(expEnd);
			}
			break;
			default:
				break;
		}

		return null;
	}
	
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		//TODO  Implement this
		Label blkStart = new Label();
		mv.visitLabel(blkStart);
		ArrayList<Dec> declr=block.getDecs();
		for(int i=0;i<block.getDecs().size();i++)
		{
			declr.get(i).visit(this, arg);
		}
		for(Statement blk_stmt:block.getStatements())
		{
			if(blk_stmt instanceof AssignmentStatement)
			{
				if(((AssignmentStatement)blk_stmt).getVar().getDec() instanceof ParamDec)
					mv.visitVarInsn(ALOAD, 0);
			}
			blk_stmt.visit(this, arg); 
			if(blk_stmt instanceof BinaryChain){
				mv.visitInsn(POP);
			}
		}
		Label blkEnd = new Label();
		mv.visitLabel(blkEnd);
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		//TODO Implement this
		mv.visitLdcInsn(booleanLitExpression.getValue());
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		Token tok=constantExpression.getFirstToken();
		if(tok.kind.equals(KW_SCREENHEIGHT)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName,"getScreenHeight",PLPRuntimeFrame.getScreenHeightSig ,false);
		}
		else if(tok.kind.equals(KW_SCREENWIDTH)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName,"getScreenWidth",PLPRuntimeFrame.getScreenWidthSig ,false);
		}
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		//TODO Implement this
		declaration.setSlotNumber(slot++); //fix for new block
		if(declaration.getNameType().getJVMTypeDesc().equals("Ljava/awt/image/BufferedImage;") || declaration.getNameType().getJVMTypeDesc().equals("Lcop5556sp17/PLPRuntimeFrame;")){
			mv.visitInsn(ACONST_NULL);
			//int slt_num=declaration.getSlotNumber();
			mv.visitVarInsn(ASTORE, declaration.getSlotNumber());
		}
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		mv.visitInsn(POP);
		mv.visitInsn(ACONST_NULL);
		if (filterOpChain.firstToken.kind == OP_CONVOLVE) {
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "convolveOp", PLPRuntimeFilterOps.opSig, false);
		}else if (filterOpChain.firstToken.kind == OP_BLUR) {
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "blurOp", PLPRuntimeFilterOps.opSig, false);
		} else if (filterOpChain.firstToken.kind == OP_GRAY) {
			mv.visitInsn(POP);
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "grayOp", PLPRuntimeFilterOps.opSig, false);
		}
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		Tuple frm_opchain_tup=frameOpChain.getArg();
		frm_opchain_tup.visit(this, arg);
		Token frm_opchain_tok=frameOpChain.getFirstToken();
		if(frm_opchain_tok.kind.equals(KW_SHOW)){
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "showImage", PLPRuntimeFrame.showImageDesc,false);
		}else if(frm_opchain_tok.kind.equals(KW_HIDE)){
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "hideImage", PLPRuntimeFrame.hideImageDesc,false);
		}
		else if(frm_opchain_tok.kind.equals(KW_YLOC)){
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "getYVal", PLPRuntimeFrame.getYValDesc,false);
		}else if(frm_opchain_tok.kind.equals(KW_XLOC)){
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "getXVal", PLPRuntimeFrame.getXValDesc,false);
		}
		else if(frm_opchain_tok.kind.equals(KW_MOVE)){
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "moveFrame", PLPRuntimeFrame.moveFrameDesc,false);
		}
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		boolean left_ptr = (boolean)arg;
		Dec declr = identChain.getDeclr();
		String fType = declr.getNameType().getJVMTypeDesc();
		if(left_ptr) {
			if(declr instanceof ParamDec) {
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, identChain.getDeclr().getIdent().getText(), fType);
			}
			else {
				if(identChain.getNameType() == TypeName.INTEGER)
					mv.visitVarInsn(ILOAD, declr.getSlotNumber());
				else 
					mv.visitVarInsn(ALOAD, declr.getSlotNumber());
			}
		} else {
			if(declr instanceof ParamDec) {
				if (declr.getNameType() == TypeName.FILE) {
					mv.visitInsn(POP);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, declr.getIdent().getText(), declr.getNameType().getJVMTypeDesc());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write", PLPRuntimeImageIO.writeImageDesc, false);
				} else {
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD, className, identChain.getDeclr().getIdent().getText(), fType);
				}
			}
			else {
				if(declr.getNameType() == TypeName.IMAGE) {
					mv.visitVarInsn(ASTORE, declr.getSlotNumber());
				}
				else if(declr.getNameType() == TypeName.FILE)  {
					mv.visitInsn(POP);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, declr.getIdent().getText(), declr.getNameType().getJVMTypeDesc());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className,"write", PLPRuntimeImageIO.writeImageDesc, false);
				} 
				else if(identChain.getDeclr().getNameType() == TypeName.FRAME) {
					mv.visitInsn(POP);
					mv.visitVarInsn(ALOAD, declr.getSlotNumber());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName,"createOrSetFrame", PLPRuntimeFrame.createOrSetFrameSig, false);
					mv.visitInsn(DUP);
					mv.visitVarInsn(ASTORE, declr.getSlotNumber());
				}
				else if(declr.getNameType() == TypeName.INTEGER)
					mv.visitVarInsn(ISTORE, declr.getSlotNumber());
			}
		}
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		Dec declr = identExpression.getDec();
		if(declr instanceof ParamDec) {
			mv.visitVarInsn(ALOAD, 0);
			if(declr.getNameType() == TypeName.BOOLEAN)
				mv.visitFieldInsn(GETFIELD, className, identExpression.getFirstToken().getText(), "Z");
			else if(declr.getNameType() == TypeName.INTEGER)
				mv.visitFieldInsn(GETFIELD, className, identExpression.getFirstToken().getText(), "I");
		}
		else {
			if(identExpression.getNameType() == TypeName.INTEGER || identExpression.getNameType() == TypeName.BOOLEAN)
				mv.visitVarInsn(ILOAD, declr.getSlotNumber());
			else
				mv.visitVarInsn(ALOAD, declr.getSlotNumber());
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		Dec declr = identX.getDec();
		String typ = declr.getNameType().getJVMTypeDesc();
		
		if(declr instanceof ParamDec) {
			mv.visitFieldInsn(PUTFIELD, className, identX.getFirstToken().getText(), typ);
		}
		else {
			if(identX.getDec().getNameType() == TypeName.IMAGE) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,"copyImage", PLPRuntimeImageOps.copyImageSig, false);
				mv.visitVarInsn(ASTORE, declr.getSlotNumber());
			} else if(identX.getDec().getNameType() == TypeName.INTEGER || identX.getDec().getNameType() == TypeName.BOOLEAN)
				mv.visitVarInsn(ISTORE, declr.getSlotNumber());
			else {
				mv.visitVarInsn(ASTORE, declr.getSlotNumber());
			}
		}
		return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		//TODO Implement this
		Expression exp_if=ifStatement.getE();
		exp_if.visit(this, arg);
		Label end_lbl = new Label();
		mv.visitJumpInsn(IFEQ, end_lbl);
		Block exp_b=ifStatement.getB();
		exp_b.visit(this, arg);
		mv.visitLabel(end_lbl);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		Tuple image_opchain_tup=imageOpChain.getArg();
		image_opchain_tup.visit(this, arg);
		Token image_tok=imageOpChain.getFirstToken();
		if(image_tok.kind.equals(KW_SCALE)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale", PLPRuntimeImageOps.scaleSig, false);
		}
		else if(image_tok.kind.equals(OP_HEIGHT)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeImageIO.BufferedImageClassName, "getHeight", PLPRuntimeImageOps.getHeightSig, false);
		}
		else if(image_tok.kind.equals(OP_WIDTH)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeImageIO.BufferedImageClassName, "getWidth", PLPRuntimeImageOps.getWidthSig, false);
		}
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		//TODO Implement this
		mv.visitLdcInsn(intLitExpression.value);
		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		FieldVisitor visitor ;
		visitor = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), paramDec.getNameType().getJVMTypeDesc(), null, null);
		visitor.visitEnd();

		paramDec.setSlotNumber(slot++);

		mv.visitVarInsn(ALOAD, 0);

		if(paramDec.getNameType() == TypeName.INTEGER) {
			paramDecInt(paramDec);
		} else if(paramDec.getNameType() == TypeName.BOOLEAN) {
			paramDecBool(paramDec);
		}
		else if(paramDec.getNameType() == TypeName.FILE) {
			paramDecFile(paramDec);
		}
		else if(paramDec.getNameType() == TypeName.URL) {
			paramDecURL(paramDec);
		} 
		return null;
	}
	
	public void paramDecInt(ParamDec paramDec){
		mv.visitVarInsn(ALOAD, 1);
		mv.visitLdcInsn(param_iter++);
		mv.visitInsn(AALOAD);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
		mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
	}
	
	public void paramDecBool(ParamDec paramDec){
		mv.visitVarInsn(ALOAD, 1);
		mv.visitLdcInsn(param_iter++);
		mv.visitInsn(AALOAD);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
		mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");
	}
	
	public void paramDecFile(ParamDec paramDec){
		mv.visitTypeInsn(NEW, "java/io/File");
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitLdcInsn(param_iter++);
		mv.visitInsn(AALOAD);
		mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
		mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), paramDec.getNameType().getJVMTypeDesc());
	}
	
	public void paramDecURL(ParamDec paramDec){
		mv.visitVarInsn(ALOAD, 1);
		mv.visitLdcInsn(param_iter++);
		mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL", PLPRuntimeImageIO.getURLSig, false);
		mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), paramDec.getNameType().getJVMTypeDesc());
	}
	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.getE().visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread","sleep", "(J)V",false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		for(Expression exp : tuple.getExprList())
		{
			exp.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		//TODO Implement this
		Label WHILEGUARD = new Label();
		mv.visitJumpInsn(GOTO, WHILEGUARD);
		Label BODY = new Label();
		mv.visitLabel(BODY);
		whileStatement.getB().visit(this, arg);
		mv.visitLabel(WHILEGUARD);
		whileStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFNE, BODY);
		return null;
	}

}
