package cop5556sp17;



import java.util.HashMap;
import java.util.ListIterator;
import java.util.Stack;

import cop5556sp17.AST.Dec;
import cop5556sp17.TypeCheckVisitor.TypeCheckException;


public class SymbolTable {
	
	int currentScope, nextScope;
	Stack<Integer> scopeStack;
	HashMap<String, HashMap<Integer,Dec>> m1;

	/** 
	 * to be called when block entered
	 */
	public void enterScope(){
		currentScope = nextScope++; 
		scopeStack.push(currentScope);
	}
	
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		scopeStack.pop();
		currentScope = scopeStack.peek();
	}
	
	public boolean insert(String ident, Dec dec) throws TypeCheckException{
		if(m1.containsKey(ident))
		{
			HashMap<Integer,Dec> m2 = m1.get(ident);
			if(m2.containsKey(currentScope)){
				throw new TypeCheckException("Ident is declared already");
			}
			m2.put(currentScope, dec);
			m1.put(ident, m2);
		}
		else
		{
			HashMap<Integer,Dec> m2 = new HashMap<Integer,Dec>();
			m2.put(currentScope, dec);
			m1.put(ident, m2);
		}
		if(m1.get(ident) == null)
			return false;
		else
			return true;
	}
	
	public Dec lookup(String ident){
		Dec dec = null;
		HashMap<Integer,Dec> m2 = m1.get(ident);
		int level;
		ListIterator<Integer> it = scopeStack.listIterator(scopeStack.size());
		while(it.hasPrevious())
		{
			level = it.previous();
			if(m2.get(level) != null)
			{
				dec = m2.get(level);
				break;
			}
			else
				continue;
		}
		return dec;
	}
		
	public SymbolTable() {
		currentScope = 0;
		scopeStack = new Stack<Integer>();
		scopeStack.push(0);
		nextScope = currentScope + 1;
		m1 = new HashMap<String, HashMap<Integer,Dec>>();
	}


	@Override
	public String toString() {
		return "SymbolTable [currentScope=" + currentScope + ", nextScope=" + nextScope + ", scopeStack=" + scopeStack
				+ ", m1=" + m1 + "]";
	}
}
