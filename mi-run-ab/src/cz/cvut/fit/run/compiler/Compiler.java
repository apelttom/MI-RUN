package cz.cvut.fit.run.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import antlr.collections.AST;
import cz.cvut.fit.run.compiler.Instruction.InsSet;

public class Compiler implements Constants {

	private int BC_VariableCount; // bytecode variable count
	private Map<String, Integer> variableMap;
	private ClassFile classfile; // actually processed class
	private List<ClassFile> classfiles;
	private String assignedVariableType;

	public Compiler() {
	}

	public List<ClassFile> compile(AST root) {
		classfiles = new ArrayList<ClassFile>();
		AST temp = root;
		while (temp != null) {
			if ("CLASS_DEF".equals(temp.getText())) {
				init();
				classHeader(temp);
				classfiles.add(classfile);
			}
			temp = temp.getNextSibling();
		}
		return this.classfiles;
	}

	private void init() {
		BC_VariableCount = 0;
		this.variableMap = new HashMap<String, Integer>();
		classfile = null;
		assignedVariableType = null;
	}

	private void classHeader(AST token_CLASS_DEF) {
		AST token_MODIFIERS = token_CLASS_DEF.getFirstChild();
		AST token_name = token_MODIFIERS.getNextSibling();

		this.classfile = new ClassFile(token_name.getText());

		for (AST flag : getAstChildren(token_MODIFIERS)) {
			classfile.addFlag(flag.getText());
		}
		AST token_EXTENDS = token_name.getNextSibling();
		AST token_EXTENDS_token = token_EXTENDS.getFirstChild();
		classfile.setSuper(token_EXTENDS_token == null ? null : token_EXTENDS_token.getText());
		AST token_IMPLEMENTS = token_EXTENDS.getNextSibling();
		for (AST iface : getAstChildren(token_IMPLEMENTS)) {
			classfile.addFlag(iface.getText());
		}

		createGenericConstructor();
		AST token_OBJBLOCK = token_IMPLEMENTS.getNextSibling();
		objectBlock(token_OBJBLOCK);
	}

	private void createGenericConstructor() {
		MethodInfo constructor = new MethodInfo(classfile.getThis());
		constructor.addFlag("public");
		ByteCode bytecode = new ByteCode();
		constructor.setBytecode(bytecode);
		// method at index 0 is always generic constructor
		classfile.addMethod(constructor);
	}

	private void objectBlock(AST token_OBJBLOCK) {
		AST token = token_OBJBLOCK.getFirstChild();
		while (token != null) {
			if ("METHOD_DEF".equals(token.getText())) {
				functionHeader(token);
			} else if ("VARIABLE_DEF".equals(token.getText())) {
				classField(token);
			} else if ("CTOR_DEF".equals(token.getText())) {
				constructorDefinition(token);
			}
			token = token.getNextSibling();
		}
	}

	private void classField(AST token_VARIABLE_DEF) {
		List<AST> tokens = getAstChildren(token_VARIABLE_DEF);
		AST token_MODIFIERS = tokens.get(0);
		AST token_TYPE = tokens.get(1);
		AST token_name = tokens.get(2);

		FieldInfo field = new FieldInfo(token_name.getText(), token_TYPE.getFirstChild().getText());
		for (AST flag : getAstChildren(token_MODIFIERS)) {
			field.flags.add(flag.getText());
		}
		classfile.addField(field);
		try {
			ByteCode constructorByteCode = classfile.getMethod(0).getBytecode();
			expression(token_VARIABLE_DEF, constructorByteCode);
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		}
	}

	private void constructorDefinition(AST token_CTOR_DEF) {
		List<AST> tokens = getAstChildren(token_CTOR_DEF);
		AST token_MODIFIERS = tokens.get(0);
		AST token_methodName = tokens.get(1);

		boolean isGeneric = (tokens.get(2).getNumberOfChildren() == 0);

		MethodInfo constructor = isGeneric ? classfile.getMethod(0) : new MethodInfo(
				token_methodName.getText());
		for (AST flag : getAstChildren(token_MODIFIERS)) {
			// constructor flags (public, private, ...)
			if (isGeneric && "public".equals(flag.getText())) {
				// do not insert second public flag, one is already there
				continue;
			}
			constructor.addFlag(flag.getText());
		}
		AST token_PARAMETERS = tokens.get(2);
		functionParams(constructor, token_PARAMETERS);

		AST token_BODY = tokens.get(3);
		ByteCode byteCode = fuctionBody(token_BODY);

		if (isGeneric) {
			constructor.getBytecode().merge(byteCode);
		} else {
			constructor.setBytecode(byteCode);
			this.classfile.addMethod(constructor);
		}
	}

	private void functionHeader(AST token_METHOD_DEF) {
		List<AST> tokens = getAstChildren(token_METHOD_DEF);
		AST token_MODIFIERS = tokens.get(0);
		AST token_TYPE = tokens.get(1);
		AST token_methodName = tokens.get(2);

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
		this.classfile.addMethod(newMethod);
	}

	private void functionParams(MethodInfo method, AST token_PARAMETERS) {
		// reset local variable count to number of class fields (global vars)
		BC_VariableCount = classfile.getFields().size();
		List<AST> tokens_PARAMETER_DEF = getAstChildren(token_PARAMETERS);

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

			String paramName = token_TYPE.getNextSibling().getText();
			variableMap.put(paramName, BC_VariableCount++);
			i++;
		} while (i < tokens_PARAMETER_DEF.size());
	}

	private ByteCode fuctionBody(AST token_CR_BRACKET) {
		ByteCode bytecode = new ByteCode();

		AST token = token_CR_BRACKET.getFirstChild();
		if (token == null)
			return bytecode;
		do {
			expression(token, bytecode);
			token = token.getNextSibling();
		} while (token != null);

		return bytecode;
	}

	private void expression(AST token, ByteCode bytecode) {
		if (token == null)
			return;
		String tokenName = token.getText();
		if (IF.equals(tokenName)) {
			if_condition(token, bytecode);
		} else if (FOR.equals(tokenName)) {
			for_cycle(token, bytecode);
		} else if (ASSIGN.equals(tokenName)) {
			assignment_expression(token, bytecode);
		} else if (VARIABLE_DEF.equals(tokenName)) {
			variable_definition(token, bytecode);
		} else if (RETURN.equals(tokenName)) {
			return_statement(token, bytecode);
		} else if (EXPR.equals(tokenName)) {
			expression(token.getFirstChild(), bytecode);
		} else if (LEFT_CR_BR.equals(tokenName)) {
			AST firstLOC = token.getFirstChild();
			expression(firstLOC, bytecode);
			AST nextLOC = firstLOC.getNextSibling();
			while (nextLOC != null) {
				expression(nextLOC, bytecode);
				nextLOC = nextLOC.getNextSibling();
			}
		} else if (LEFT_PARENT.equals(tokenName)) {
			function_call(token, bytecode);
		} else if (isLogical(tokenName)) {
			logic_expression(token, bytecode);
		} else if (isArithmetic(tokenName)) {
			arithmetic_expression(token, bytecode);
		} else if (DOUBLE_PLUS.equals(tokenName)) {
			// token is now "++", its first child is the variable name
			increment_variable(token.getFirstChild().getText(), 1, bytecode);
		} else if (isNumeric(tokenName)) {
			bytecode.add(new Instruction(InsSet.bipush, tokenName));
		} else if (EMPTY_EXPR.equals(tokenName)) {
			return;
		} else if (NEW_CLASS.equals(tokenName)) {
			new_class(token, bytecode);
		} else {
			// je to literal
			bytecode.add(new Instruction(Instruction.load(assignedVariableType), String
					.valueOf(variableMap.get(tokenName))));
		}
	}

	// zkompiluje podminku v if (...), vstupem je token podminky
	private void logic_expression(AST token, ByteCode bytecode) {
		AST token_LEFT = token.getFirstChild(); // Left side
		AST token_RIGHT = token_LEFT.getNextSibling(); // Right side

		expression(token_LEFT, bytecode);
		expression(token_RIGHT, bytecode);

		String tokenName = token.getText();
		if (LOGIC_GT.equals(tokenName)) { // >
			// if-less-than--then-jump-to
			bytecode.add(new Instruction(InsSet.if_icmple, ""));
		} else if (LOGIC_LT.equals(tokenName)) { // <
			// if-greater-than--then-jump-to
			bytecode.add(new Instruction(InsSet.if_icmpge, ""));
		} else if (LOGIC_EQ.equals(tokenName)) { // ==
			// if-not-equal--then-jump-to
			bytecode.add(new Instruction(InsSet.if_icmpne, ""));
		} else if (LOGIC_NEQ.equals(tokenName)) { // !=
			// if-equal--then-jump-to
			bytecode.add(new Instruction(InsSet.if_icmpeq, ""));
		}
	}

	private void arithmetic_expression(AST token, ByteCode bytecode) {
		AST token_LEFT = token.getFirstChild();
		AST token_RIGHT = token_LEFT.getNextSibling();

		if (isNumeric(token_LEFT.getText())) {
			// levy argument je konstanta, dame ji rovnou na stack
			bytecode.add(new Instruction(InsSet.bipush, token_LEFT.getText()));
		} else {
			// levy argument je promenna, vytahneme ji z variable map
			expression(token_LEFT, bytecode);
		}
		if (isNumeric(token_RIGHT.getText())) {
			bytecode.add(new Instruction(InsSet.bipush, token_RIGHT.getText()));
		} else {
			expression(token_RIGHT, bytecode);
		}

		if (PLUS.equals(token.getText())) { // +
			bytecode.add(new Instruction(InsSet.iadd));
		} else if (MINUS.equals(token.getText())) { // -
			bytecode.add(new Instruction(InsSet.isub));
		} else if (MULTI.equals(token.getText())) { // *
			bytecode.add(new Instruction(InsSet.imul));
		}
	}

	private void assignment_expression(AST token, ByteCode bytecode) {
		if (token.getFirstChild().getText().equals(LEFT_SQ_BR)) {
			// TODO array assign
			AST node_token_VARIABLE = token.getFirstChild().getFirstChild();
			AST node_token_INDEX = node_token_VARIABLE.getNextSibling().getFirstChild();
			AST node_token_VALUE = token.getFirstChild().getNextSibling();
		} else {
			AST node_token_VARIABLE = token.getFirstChild();
			AST node_token_VALUE = node_token_VARIABLE.getNextSibling();
			expression(node_token_VALUE, bytecode);
			bytecode.add(new Instruction(Instruction.store(assignedVariableType), variableMap
					.get(node_token_VARIABLE.getText()) + ""));
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
		bytecode.add(new Instruction(InsSet.new_class, className, String.valueOf(tokens_classArgs
				.size())));
	}

	private void variable_definition(AST token, ByteCode bytecode) {
		boolean isArray = false;
		AST token_TYPEVAL;
		AST token_MODIFIERS = token.getFirstChild();
		AST token_TYPE = token_MODIFIERS.getNextSibling();
		if (token_TYPE.getFirstChild().getText().equals(LEFT_SQ_BR)) {
			// array
			AST token_ARRTYPE = token_TYPE.getFirstChild();
			token_TYPEVAL = token_ARRTYPE.getFirstChild();
			isArray = true;
		} else {
			token_TYPEVAL = token_TYPE.getFirstChild();
		}
		assignedVariableType = token_TYPEVAL.getText();
		AST token_VARNAME = token_TYPE.getNextSibling();

		AST token_ASSIGN = token_VARNAME.getNextSibling();
		if (token_ASSIGN == null) {
			// not an assignment, just declaring, assign default
			bytecode.add(new Instruction(InsSet.bipush, getDefaultValue(assignedVariableType)));
		} else if (!isArray) {
			AST token_VARVAL = token_ASSIGN.getFirstChild().getFirstChild();
			String varVal = "";
			if (token_VARVAL.getText().equals(MINUS))
				varVal = MINUS + token_VARVAL.getFirstChild().getText();
			else
				varVal = token_VARVAL.getText();
			if (isNumeric(varVal)) {
				// v deklaraci prirazujeme cislo, muzeme ho hodit na stack a
				// nahrat do promenne
				bytecode.add(new Instruction(InsSet.bipush, varVal));
			} else {
				// Prirazujeme nejaky vyraz, musime ho nejdriv zpracovat a
				// vysledek pak hodit do promenne
				expression(token_ASSIGN.getFirstChild(), bytecode);
			}
		} else {
			// TODO is array
			AST token_ARRLEN = token_ASSIGN.getFirstChild().getFirstChild()
					.getFirstChild().getNextSibling().getFirstChild().getFirstChild();
		}
		bytecode.add(new Instruction(Instruction.store(assignedVariableType), BC_VariableCount + ""));
		variableMap.put(token_VARNAME.getText(), BC_VariableCount);
		BC_VariableCount++;
	}

	private void return_statement(AST token, ByteCode bytecode) {
		expression(token.getFirstChild(), bytecode);
		// return statement
		bytecode.add(new Instruction(InsSet.ireturn));
	}

	private void function_call(AST token, ByteCode bytecode) {
		AST token_methodName = token.getFirstChild();
		AST token_ELIST = token_methodName.getNextSibling(); // parent of all parameters
		for (AST param : getAstChildren(token_ELIST)) {
			expression(param, bytecode);
		}
		if (METHOD_INVOCATION.equals(token_methodName.getText())) {
			// method called on some object
			AST token_object = token_methodName.getFirstChild(); // TODO might be "new"
			token_methodName = token_object.getNextSibling();
			bytecode.add(new Instruction(InsSet.invoke, token_methodName.getText(), String
					.valueOf(this.variableMap.get(token_object.getText()))));
		} else {
			// method in the same class
			bytecode.add(new Instruction(InsSet.invoke, token_methodName.getText()));
		}
	}

	private void if_condition(AST node, ByteCode bytecode) { 
		// TODO slozene zavorky pred
		// if condition
		AST token_COND_EXPR = node.getFirstChild();
		// mini_print(node);
		// if branch
		AST token_IF_BRANCH = token_COND_EXPR.getNextSibling();
		// else branch (might be null)
		AST token_ELSE_BRANCH = token_IF_BRANCH.getNextSibling();
		// condition operator
		AST token_COND_OPERATOR = token_COND_EXPR.getFirstChild();
		// Left operands
		AST token_COND_OPRDS_LEFT = token_COND_OPERATOR.getFirstChild();
		// Right operands
		AST token_COND_OPRDS_RIGHT = token_COND_OPRDS_LEFT.getNextSibling();

		// if (...)
		expression(token_COND_EXPR, bytecode);
		int PC_ifJump = bytecode.size() - 1; // position of IF JUMP instruction

		// { ... } // if-part
		expression(token_IF_BRANCH, bytecode); // compile if branch expression
		bytecode.add(new Instruction(InsSet.go_to, "")); // L1
		int PC_jumpToL2 = bytecode.size() - 1; // position of JUMP to L2

		// else { ... } // else-part; compile only when present
		if (token_ELSE_BRANCH != null)
			expression(token_ELSE_BRANCH, bytecode);

		int PC_L2 = bytecode.size() - 1;

		// potrebuju:
		// 1) kam skocit z if
		bytecode.changeOperand(PC_ifJump, 0, (PC_jumpToL2 + 1) + "");

		// 2) kam skocit z konce if-part
		bytecode.changeOperand(PC_jumpToL2, 0, (PC_L2 + 1) + "");
	}

	private void for_cycle(AST token, ByteCode bytecode) {
		/*
		 * Nejdrive provedu inicializaci (prvni cast "for" prikazu). Pak skocim dolu, kde nactu na zasobnik promenne pro podminku. Provedu
		 * podminku. Pokud je true, skocim nahoru na telo kodu (po nemz nasleduje inkrement a opet nacteni promennych pro podminku). Pokud
		 * je false, pokracuji dale, cyklus je hotovy. Struktura: > INIT > jump L1 (je potreba offset -2, kvuli nacteni promennych) > BODY
		 * [L2] > ITERATOR > CONDITION (+ nacteni promennych pro porovnani) [L1] > jump_if_false L3 > jump L2 > pokracovani programu [L3]
		 */

		List<AST> tokens = getAstChildren(token);

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

	private void increment_variable(String variable, int n, ByteCode bytecode) {
		int varIndex = variableMap.get(variable);
		bytecode.add(new Instruction(InsSet.iinc, Integer.toString(varIndex), Integer.toString(n)));
	}

	private List<AST> getAstChildren(AST token) {
		List<AST> list = new ArrayList<AST>();

		if (token == null) {
			return list;
		}

		AST temp = token.getFirstChild();
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

	private static String getDefaultValue(String type) {
		if (type == null) {
			throw new NullPointerException("get default value with null reference type");
		}
		switch (type) {
		case "int":	return "0";
		default: 	return "null";
		}
	}

	private static boolean isArithmetic(String tokenName) {
		if (tokenName.equals(MINUS) || tokenName.equals(MULTI) || tokenName.equals(PLUS)) {
			return true;
		}
		return false;
	}

	private static boolean isLogical(String tokenName) {
		if (tokenName.equals(LOGIC_EQ) || tokenName.equals(LOGIC_NEQ) || tokenName.equals(LOGIC_GT)
				|| tokenName.equals(LOGIC_LT)) {
			return true;
		}
		return false;
	}

	private static boolean isNumeric(String str) {
		// match a number with optional '-' and decimal.
		return str.matches("-?\\d+(\\.\\d+)?");
	}
}
