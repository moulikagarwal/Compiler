package cop5556sp17.AST;

import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Token;
import cop5556sp17.TypeCheckVisitor.TypeCheckException;


public abstract class Chain extends Statement {
	
	public Chain(Token firstToken) {
		super(firstToken);
	}
	public TypeName nameType;

	public TypeName getNameType() {
		return nameType;
	}

	public void setNameType(TypeName nameType) throws TypeCheckException {
		try 
		{
			if(nameType == null)
			{
				this.nameType = Type.getTypeName(firstToken);
			}
			else
				this.nameType = nameType;
		} 
		catch (Exception e) 
		{
			throw new TypeCheckException("Not a valid firstToken" + firstToken.getText());
		}
	}

}
