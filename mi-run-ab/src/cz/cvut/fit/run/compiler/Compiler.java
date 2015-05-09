package cz.cvut.fit.run.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import antlr.collections.AST;
import cz.cvut.fit.run.compiler.Instruction.InsSet;
import cz.cvut.fit.run.vm.ClassFile;
import cz.cvut.fit.run.vm.MethodInfo;

public class Compiler implements Constants {

	private int PC = 0; // program counter
	private int BC_VariableCount = 0; // bytecode variable count
	private Map<String, Integer> variableMap;
	private List<MethodInfo> methods = null;
	private ClassFile classfile = null;
	private boolean printNodes = false;

	public Compiler(boolean printNodes) {
		this();
		this.printNodes = printNodes;
	}

	public Compiler() {
		this.PC = BC_VariableCount = 0;
		this.variableMap = new HashMap<String, Integer>();
		this.methods = new ArrayList<MethodInfo>();
		this.classfile = new ClassFile();
	}

	public ClassFile compile(AST root) {
		// byteCode.clear();
		AST temp = root;
		traverse(temp, 0);
		while (temp.getNextSibling() != null) {
			temp = temp.getNextSibling();
			traverse(temp, 0);
		}
		// TODO pridavat metody rovnou do classfile
		for (MethodInfo m : methods) {
			classfile.addMethod(m);
		}
		return this.classfile;
	}

	private void traverse(AST node, int depth) {
		PC++;
		if (printNodes) {
			printNode(node, depth);
		}
		compileInternal(node);
		if (node.getFirstChild() == null) {
			return;
		}
		for (AST ast : getAstChildren(node)) {
			traverse(ast, depth + 1);
		}
	}

	private void printNode(AST node, int depth) {
		for (int i = 0; i < depth; i++) {
			System.out.print(" ");
		}
		System.out.println(node.getText());
	}

	// expects AST root
	private void compileInternal(AST node) {
		String tokenName = node.getText();
		if (tokenName.equals("METHOD_DEF")) {
			// resetujeme pocitadlo promennych, vstupujeme do lokalni promenne
			BC_VariableCount = 0;
			functionHeader(node);
		}
	}

	private List<AST> getAstChildren(AST node) {

		List<AST> list = new ArrayList<AST>();

		if (node == null) {
			return list;
		}

		AST temp = node.getFirstChild();

		if (temp == null) {
			return list;
		}
		list.add(temp);

		while (temp.getNextSibling() != null) {
			temp = temp.getNextSibling();
			list.add(temp);
		}

		return list;
	}

	// expects METHOD_DEF token
	private void functionHeader(AST node) {
		List<AST> tokens = getAstChildren(node);
		AST token_MODIFIERS = tokens.get(0);
		AST token_TYPE = tokens.get(1);
		AST token_methodName = tokens.get(2);
		/*AST methodName = null;
		for (AST ast : tokens) {
			// next sibling of the TYPE node is method name, so let's store it!
			if (ast.getText().equals("TYPE")) {
				methodName = ast.getNextSibling();
			}
		}*/
		MethodInfo newMethod = new MethodInfo(token_methodName.getText());
		for (AST flag : getAstChildren(token_MODIFIERS)) {
			// method flags (public, static, final, virtual, ...)
			newMethod.addFlag(flag.getText());
		}
		// method return type (may be "void")
		newMethod.setReturnType(token_TYPE.getFirstChild().getText());
		
		AST token_PARAMETERS = tokens.get(3);
		functionParams(newMethod, token_PARAMETERS);

		AST token_BODY = tokens.get(4);
		ByteCode byteCode = fuctionBody(token_BODY);
		
		newMethod.setBytecode(byteCode);
		this.methods.add(newMethod);
	}

	// expects PARAMETERS token
	private void functionParams(MethodInfo method, AST node) {
		/*
		 * TODO: Based on the parameters there is need to distinc between
		 * invokestatic and invokevirtual. Now this method do practically
		 * nothing
		 */
		List<AST> tokens_PARAMETER_DEF = getAstChildren(node);

		if (tokens_PARAMETER_DEF.isEmpty()) {
			// method has no arguments
			return;
		}

		int i = 0;

		do {
			List<AST> tokens_SINGLE_PARAM = getAstChildren(tokens_PARAMETER_DEF.get(i));

			if (tokens_SINGLE_PARAM.isEmpty()) {
				break;
			}
			// tokens_SINGLE_PARAM.get(0) are MODIFIERS such as final etc.
			AST token_TYPE = tokens_SINGLE_PARAM.get(1);
			method.addArgType(token_TYPE.getFirstChild().getText());
			// does not work for arrays yet, structure [ > ArrayType
			
			// TODO inicializovat vstupni promenne funkce, aby s nimi mohla pracovat
			String paramName = token_TYPE.getNextSibling().getText();
			variableMap.put(paramName, BC_VariableCount++);
			
			i++;
		} while (i < tokens_PARAMETER_DEF.size());
	}

	private ByteCode fuctionBody(AST node) {
		ByteCode bytecode = new ByteCode();

		AST node_token_TOKEN = node.getFirstChild();
		if (node_token_TOKEN == null)
			return bytecode;
		do {
			expression(node_token_TOKEN, bytecode);
			node_token_TOKEN = node_token_TOKEN.getNextSibling();
		} while (node_token_TOKEN != null);
		
		return bytecode;
	}

	private void return_statement(AST node, ByteCode bytecode) {
		expression(node.getFirstChild(), bytecode);
		// return statement
		bytecode.add(new Instruction(InsSet.ireturn));
	}

	private void for_cycle(AST node, ByteCode bytecode) {
		/*
		 * Nejdrive provedu inicializaci (prvni cast "for" prikazu). Pak skocim
		 * dolu, kde nactu na zasobnik promenne pro podminku. Provedu podminku.
		 * Pokud je true, skocim nahoru na telo kodu (po nemz nasleduje
		 * inkrement a opet nacteni promennych pro podminku). Pokud je false,
		 * pokracuji dale, cyklus je hotovy. Struktura: > INIT > jump L1 (je
		 * potreba offset -2, kvuli nacteni promennych) > BODY [L2] > ITERATOR >
		 * CONDITION (+ nacteni promennych pro porovnani) [L1] > jump_if_false
		 * L3 > jump L2 > pokracovani programu [L3]
		 */

		List<AST> tokens = getAstChildren(node);

		AST token_FOR_INIT = tokens.get(0); // for (xxx ; ... ; ...) { ... }
		AST token_FOR_CONDITION = tokens.get(1); // for (... ; xxx ; ...) { ...
													// }
		AST token_FOR_ITERATOR = tokens.get(2); // for (... ; ... ; xxx) { ... }
		AST token_FOR_BODY = tokens.get(3); // for (... ; ... ; ...) { xxx }

		// nejdrive zkompilujeme init (prirazeni do promenne)
		variable_definition(token_FOR_INIT.getFirstChild(), bytecode); // VARIABLE_DEF

		// L1: if (...)
		logic_expression(token_FOR_CONDITION.getFirstChild(), bytecode); // EXPR
		int PC_jumpToL1 = bytecode.size() - 1;

		// L2: vykonani tela cyklu
		expression(token_FOR_BODY, bytecode); // "{

		// zavolani iteratoru
		expression(token_FOR_ITERATOR.getFirstChild().getFirstChild(), bytecode); // EXPR

		// Jump na L1 (nacteni promennych + podminka)
		bytecode.add(new Instruction(InsSet.go_to, ""));
		int PC_L1 = bytecode.size() - 1; // L1

		// "-2", protoze potrebujeme jeste nacteni promennych pro podminku
		bytecode.changeOperand(PC_L1, 0, PC_jumpToL1 - 2 + "");
		// skok na L1
		bytecode.changeOperand(PC_jumpToL1, 0, PC_L1 + 1 + "");
	}

	private void expression(AST node, ByteCode bytecode) {
		AST token_EXPRESSION = node;
		if (token_EXPRESSION == null)
			return;
		String tokenName = token_EXPRESSION.getText();
		if (tokenName.equals(IF)) {
			if_condition(token_EXPRESSION, bytecode);
		} else if (tokenName.equals(FOR)) {
			for_cycle(token_EXPRESSION, bytecode);
		} else if (tokenName.equals(ASSIGN)) {
			assignment_expression(token_EXPRESSION, bytecode);
		} else if (tokenName.equals(VARIABLE_DEF)) {
			variable_definition(token_EXPRESSION, bytecode);
		} else if (tokenName.equals(RETURN)) {
			return_statement(token_EXPRESSION, bytecode);
		} else if (tokenName.equals(EXPR)) {
			expression(token_EXPRESSION.getFirstChild(), bytecode);
		} else if (tokenName.equals(LEFT_CR_BR)) {
			AST firstLOC = token_EXPRESSION.getFirstChild();
			expression(firstLOC, bytecode);
			AST nextLOC = firstLOC.getNextSibling();
			while (nextLOC != null) {
				expression(nextLOC, bytecode);
				nextLOC = nextLOC.getNextSibling();
			}
		} else if (tokenName.equals(LEFT_PARENT)) {
			function_call(token_EXPRESSION, bytecode);
		} else if (isLogical(tokenName)) {
			logic_expression(token_EXPRESSION, bytecode);
		} else if (isArithmetic(tokenName)) {
			arithmetic_expression(token_EXPRESSION, bytecode);
		} else if (tokenName.equals(DOUBLE_PLUS)) {
			// token is now "++", its first child is the variable name
			incrementVar(token_EXPRESSION.getFirstChild().getText(), 1,
					bytecode);
		} else if (isNumeric(tokenName)) {
			bytecode.add(new Instruction(InsSet.bipush, tokenName));
		} else if (tokenName.equals(EMPTY_EXPR)) {
			return;
		} else if (tokenName.equals(NEW_CLASS)) {
			new_class(token_EXPRESSION, bytecode);
		} else
		// je to literal
		{
			bytecode.add(new Instruction(InsSet.iload, variableMap
					.get(tokenName) + ""));
		}
	}

	private void new_class(AST token_new, ByteCode bytecode) {
		AST token_className = token_new.getFirstChild();
		String className = token_className.getText();
		
		List<AST> tokens_classArgs = getAstChildren(token_className.getNextSibling());
		for (AST arg : tokens_classArgs) {
			// evaluate all arguments
			expression(arg, bytecode);
		}
		bytecode.add(new Instruction(InsSet.new_class, className, 
				String.valueOf(tokens_classArgs.size())));
	}

	private void incrementVar(String variable, int n, ByteCode bytecode) {
		int varIndex = variableMap.get(variable);
		bytecode.add(new Instruction(InsSet.iinc, Integer.toString(varIndex),
				Integer.toString(n)));
	}

	// zkompiluje podminku v if (...), vstupem je token podminky
	private void logic_expression(AST node, ByteCode bytecode) {
		AST node_token_LEFT = node.getFirstChild(); // Left side
		AST node_token_RIGHT = node_token_LEFT.getNextSibling(); // Right side

		expression(node_token_LEFT, bytecode);
		expression(node_token_RIGHT, bytecode);

		if (node.getText().equals(LOGIC_GT)) { // >
			// if-less-than--then-jump-to
			bytecode.add(new Instruction(InsSet.if_icmple, ""));
		} else if (node.getText().equals(LOGIC_LT)) { // <
			// if-greater-than--then-jump-to
			bytecode.add(new Instruction(InsSet.if_icmpge, ""));
		} else if (node.getText().equals(LOGIC_EQ)) { // ==
			// if-not-equal--then-jump-to
			bytecode.add(new Instruction(InsSet.if_icmpne, ""));
		} else if (node.getText().equals(LOGIC_NEQ)) { // !=
			// if-equal--then-jump-to
			bytecode.add(new Instruction(InsSet.if_icmpeq, ""));
		}
	}

	private void arithmetic_expression(AST node, ByteCode bytecode) {
		AST node_token_LEFT = node.getFirstChild();
		AST node_token_RIGHT = node_token_LEFT.getNextSibling();

		if (isNumeric(node_token_LEFT.getText())) {
			// levy argument je konstanta, dame ji rovnou na stack
			bytecode.add(new Instruction(InsSet.bipush, node_token_LEFT
					.getText()));
		} else {
			// levy argument je promenna, vytahneme ji z variable map
			expression(node_token_LEFT, bytecode);
		}
		if (isNumeric(node_token_RIGHT.getText())) {
			bytecode.add(new Instruction(InsSet.bipush, node_token_RIGHT
					.getText()));
		} else {
			expression(node_token_RIGHT, bytecode);
		}

		if (node.getText().equals(PLUS)) { // +
			bytecode.add(new Instruction(InsSet.iadd));
		} else if (node.getText().equals(MINUS)) { // -
			bytecode.add(new Instruction(InsSet.isub));
		} else if (node.getText().equals(MULTI)) { // *
			bytecode.add(new Instruction(InsSet.imul));
		}
	}

	private void assignment_expression(AST node, ByteCode bytecode) {
		if (node.getFirstChild().getText().equals(LEFT_SQ_BR)) { // array
																	// assign
			AST node_token_VARIABLE = node.getFirstChild().getFirstChild();
			AST node_token_INDEX = node_token_VARIABLE.getNextSibling()
					.getFirstChild();
			AST node_token_VALUE = node.getFirstChild().getNextSibling();
		} else {
			AST node_token_VARIABLE = node.getFirstChild();
			AST node_token_VALUE = node_token_VARIABLE.getNextSibling();
			expression(node_token_VALUE, bytecode);
			bytecode.add(new Instruction(InsSet.istore, variableMap
					.get(node_token_VARIABLE.getText()) + ""));
		}
	}

	private void function_call(AST node, ByteCode bytecode) {
		AST token_methodName = node.getFirstChild();
		AST token_ELIST = token_methodName.getNextSibling(); //parent of all parameters
		for (AST param : getAstChildren(token_ELIST)) {
			expression(param, bytecode);
		}
		bytecode.add(new Instruction(InsSet.invoke, token_methodName.getText()));
		// TODO invokestatic, invokespecial, invokedynamic, invokeinterface, invokevirtual
		// TODO invoke method by index in constant pool, not by name
	}

	private void if_condition(AST node, ByteCode bytecode) { // TODO : slozene
																// zavorky pred
		// if condition
		AST node_token_COND_EXPR = node.getFirstChild();
		// mini_print(node);
		// if branch
		AST node_token_IF_BRANCH = node_token_COND_EXPR.getNextSibling();
		// else branch (might be null)
		AST node_token_ELSE_BRANCH = node_token_IF_BRANCH.getNextSibling();
		// condition operator
		AST node_token_COND_OPERATOR = node_token_COND_EXPR.getFirstChild();
		// Left operands
		AST node_token_COND_OPRDS_LEFT = node_token_COND_OPERATOR
				.getFirstChild();
		// Right operands
		AST node_token_COND_OPRDS_RIGHT = node_token_COND_OPRDS_LEFT
				.getNextSibling();

		// if (...)
		expression(node_token_COND_EXPR, bytecode);

		int PC_ifJump = bytecode.size() - 1; // position of IF
												// JUMP
												// instruction

		// { ... } // if-part
		expression(node_token_IF_BRANCH, bytecode); // compile if branch
													// expression

		bytecode.add(new Instruction(InsSet.go_to, "")); // L1

		int PC_jumpToL2 = bytecode.size() - 1; // position of
												// JUMP to L2

		// else { ... } // else-part; compile only when present
		if (node_token_ELSE_BRANCH != null)
			expression(node_token_ELSE_BRANCH, bytecode);

		// old code
		// byteCode.add(new Instruction(InsSet.NOP, "")); // L2

		int PC_L2 = bytecode.size() - 1;

		// potrebuju:
		// 1) kam skocit z if
		bytecode.changeOperand(PC_ifJump, 0, (PC_jumpToL2 + 1) + "");

		// 2) kam skocit z konce if-part
		bytecode.changeOperand(PC_jumpToL2, 0, (PC_L2 + 1) + "");
	}

	private void variable_definition(AST node, ByteCode bytecode) {
		boolean isArray = false;
		AST node_token_TYPEVAL;
		AST token_MODIFIERS = node.getFirstChild();
		AST node_token_TYPE = token_MODIFIERS.getNextSibling();
		if (node_token_TYPE.getFirstChild().getText().equals(LEFT_SQ_BR)) {
			// array
			AST node_token_ARRTYPE = node_token_TYPE.getFirstChild();
			node_token_TYPEVAL = node_token_ARRTYPE.getFirstChild();
			isArray = true;
		} else {
			node_token_TYPEVAL = node_token_TYPE.getFirstChild();
		}
		AST node_token_VARNAME = node_token_TYPE.getNextSibling();

		AST node_token_ASSIGN = node_token_VARNAME.getNextSibling();
		if (node_token_ASSIGN == null) {
			// not an assignment, just declaring, can ignore
			return;
		}

		if (!isArray) {
			AST node_token_VARVAL = node_token_ASSIGN.getFirstChild()
					.getFirstChild();
			String varVal = "";
			if (node_token_VARVAL.getText().equals(MINUS))
				varVal = MINUS + node_token_VARVAL.getFirstChild().getText();
			else
				varVal = node_token_VARVAL.getText();
			if (isNumeric(varVal)) {
				// v deklaraci prirazujeme cislo, muzeme ho hodit na stack a
				// nahrat do promenne
				bytecode.add(new Instruction(InsSet.bipush, varVal));
			} else {
				// Prirazujeme nejaky vyraz, musime ho nejdriv zpracovat a
				// vysledek pak hodit do promenne
				expression(node_token_ASSIGN.getFirstChild(), bytecode);

			}
			bytecode.add(new Instruction(InsSet.istore, BC_VariableCount + ""));
			variableMap.put(node_token_VARNAME.getText(), BC_VariableCount);
			BC_VariableCount++;
		} else {
			// is array
			AST node_token_ARRLEN = node_token_ASSIGN.getFirstChild()
					.getFirstChild().getFirstChild().getNextSibling()
					.getFirstChild().getFirstChild();
		}

	}

	private static boolean isArithmetic(String tokenName) {
		if (tokenName.equals(MINUS) || tokenName.equals(MULTI)
				|| tokenName.equals(PLUS)) {
			return true;
		}
		return false;
	}

	private static boolean isLogical(String tokenName) {
		if (tokenName.equals(LOGIC_EQ) || tokenName.equals(LOGIC_NEQ)
				|| tokenName.equals(LOGIC_GT) || tokenName.equals(LOGIC_LT)) {
			return true;
		}
		return false;
	}

	private static boolean isNumeric(String str) {
		// match a number with optional '-' and decimal.
		return str.matches("-?\\d+(\\.\\d+)?");
	}
}
