package cz.cvut.fit.run.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import antlr.collections.AST;
import cz.cvut.fit.run.Main;
import cz.cvut.fit.run.compiler.Instruction.InsSet;

public class Compiler implements Constants {

	private int PC = 0; // program counter
	private int BC_VariableCount = 0; // bytecode variable count
	private Map<String, Integer> variableMap;
	private ByteCode byteCode;
	private boolean printNodes = false;

	public Compiler(boolean printNodes) {
		this();
		this.printNodes = printNodes;
	}

	public Compiler() {
		this.PC = BC_VariableCount = 0;
		this.variableMap = new HashMap<String, Integer>();
		this.byteCode = new ByteCode();
	}

	public ByteCode compile(AST root) {
		byteCode.clear();
		AST temp = root;
		traverse(temp, 0);
		while (temp.getNextSibling() != null) {
			temp = temp.getNextSibling();
			traverse(temp, 0);
		}
		return this.byteCode;
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

		AST token_PARAMETERS = tokens.get(3);

		functionParams(token_PARAMETERS);

		AST token_BODY = tokens.get(4);

		fuctionBody(token_BODY);
	}

	// expects PARAMETERS token
	private void functionParams(AST node) {
		List<AST> tokens_PARAMETERS = getAstChildren(node);

		if (tokens_PARAMETERS.isEmpty()) {
			return;
		}

		int i = 0;

		do {
			List<AST> tokens_SINGLE_PARAM = getAstChildren(tokens_PARAMETERS
					.get(i));

			if (tokens_SINGLE_PARAM.isEmpty()) {
				break;
			}

			AST token_TYPE = tokens_SINGLE_PARAM.get(1);
			// TODO

			i++;
		} while (i < tokens_PARAMETERS.size());
	}

	private void fuctionBody(AST node) {
		AST node_token_TOKEN = node.getFirstChild();
		if (node_token_TOKEN == null)
			return;
		do {
			expression(node_token_TOKEN);
			node_token_TOKEN = node_token_TOKEN.getNextSibling();
		} while (node_token_TOKEN != null);
		// return statement
		byteCode.add(new Instruction(InsSet.re_turn));
	}

	private void return_statement(AST node) {
		expression(node.getFirstChild());
		// return statement
		byteCode.add(new Instruction(InsSet.re_turn));
	}

	private void for_cycle(AST node) {
		/**
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
		variable_definition(token_FOR_INIT.getFirstChild()); // VARIABLE_DEF

		// Jump na L1 (nacteni promennych + podminka)
		byteCode.add(new Instruction(InsSet.go_to, ""));

		int PC_jumpToL1 = byteCode.size() - 1;

		// L2: vykonani tela cyklu
		expression(token_FOR_BODY); // "{"

		// zavolani iteratoru
		expression(token_FOR_ITERATOR.getFirstChild().getFirstChild()); // EXPR

		// L1: podminka, pokud je splnena, skocime na L2 (telo cyklu)
		expression(token_FOR_CONDITION.getFirstChild()); // EXPR

		int PC_L1 = byteCode.size() - 1; // L1

		byteCode.changeOperand(PC_jumpToL1, 0, PC_L1 - 2 + ""); // "-2", protoze
																// potrebujeme
																// jeste nacist
																// promenne pro
																// skok

		// OLD: if true, pokracuju dolu (tj. skocim na L1); if false, preskocim
		// (tj. PC + 2)
		// skok na L1
		byteCode.changeOperand(PC_L1, 0, (PC_jumpToL1 + 1) + "");
		byteCode.get(PC_L1).setInsCode(
				byteCode.get(PC_L1).getInvertedForInstruction());

		// skok na L1
		// byteCode.add(new Instruction(InsSet.JUMP,
		// (PC_jumpToL1 + 1) + ""));

	}

	private void expression(AST node) {
		AST token_EXPRESSION = node;
		if (token_EXPRESSION == null)
			return;
		String tokenName = token_EXPRESSION.getText();
		if (tokenName.equals(IF)) {
			if_condition(token_EXPRESSION);
		} else if (tokenName.equals(FOR)) {
			for_cycle(token_EXPRESSION);
		} else if (tokenName.equals(ASSIGN)) {
			assignment_expression(token_EXPRESSION);
		} else if (tokenName.equals(VARIABLE_DEF)) {
			variable_definition(token_EXPRESSION);
		} else if (tokenName.equals(RETURN)) {
			return_statement(token_EXPRESSION);
		} else if (tokenName.equals(EXPR)) {
			expression(token_EXPRESSION.getFirstChild());
		} else if (tokenName.equals(LEFT_CR_BR)) {
			AST firstLOC = token_EXPRESSION.getFirstChild();
			expression(firstLOC);
			AST nextLOC = firstLOC.getNextSibling();
			while(nextLOC != null){
				expression(nextLOC);
				nextLOC = nextLOC.getNextSibling();
			}
		} else if (isLogical(tokenName)) {
			logic_expression(token_EXPRESSION);
		} else if (isArithmetic(tokenName)) {
			arithmetic_expression(token_EXPRESSION);
		} else if (isNumeric(tokenName)) {
			byteCode.add(new Instruction(InsSet.bipush, tokenName));
		} else
		// je to literal
		{
			// pro FOR hazi null
			// opravit
			byteCode.add(new Instruction(InsSet.iload, variableMap
					.get(tokenName) + ""));
		}
	}

	// zkompiluje podminku v if (...), vstupem je token podminky
	private void logic_expression(AST node) {
		AST node_token_LEFT = node.getFirstChild(); // Left side
		AST node_token_RIGHT = node_token_LEFT.getNextSibling(); // Right side

		expression(node_token_LEFT);
		expression(node_token_RIGHT);

		if (node.getText().equals(LOGIC_GT)) { // >
			// if-less-than--then-jump-to
			byteCode.add(new Instruction(InsSet.if_icmple, ""));
		} else if (node.getText().equals(LOGIC_LT)) { // <
			// if-greater-than--then-jump-to
			byteCode.add(new Instruction(InsSet.if_icmpge, ""));
		} else if (node.getText().equals(LOGIC_EQ)) { // ==
			// if-not-equal--then-jump-to
			byteCode.add(new Instruction(InsSet.if_icmpne, ""));
		} else if (node.getText().equals(LOGIC_NEQ)) { // !=
			// if-equal--then-jump-to
			byteCode.add(new Instruction(InsSet.if_icmpeq, ""));
		}
	}

	private void arithmetic_expression(AST node) {
		AST node_token_LEFT = node.getFirstChild();
		AST node_token_RIGHT = node_token_LEFT.getNextSibling();

		if (isNumeric(node_token_LEFT.getText())) {
			// levy argument je konstanta, dame ji rovnou na stack
			byteCode.add(new Instruction(InsSet.bipush, node_token_LEFT
					.getText()));
		} else {
			// levy argument je promenna, vytahneme ji z variable map
			expression(node_token_LEFT);
		}
		if (isNumeric(node_token_RIGHT.getText())) {
			byteCode.add(new Instruction(InsSet.bipush, node_token_RIGHT
					.getText()));
		} else {
			expression(node_token_RIGHT);
		}

		if (node.getText().equals(PLUS)) { // +
			byteCode.add(new Instruction(InsSet.iadd));
		} else if (node.getText().equals(MINUS)) { // -
			byteCode.add(new Instruction(InsSet.isub));
		} else if (node.getText().equals(MULTI)) { // *
			byteCode.add(new Instruction(InsSet.imul));
		}
	}

	private void assignment_expression(AST node) {
		if (node.getFirstChild().getText().equals(LEFT_SQ_BR)) { // array
																	// assign
			AST node_token_VARIABLE = node.getFirstChild().getFirstChild();
			AST node_token_INDEX = node_token_VARIABLE.getNextSibling()
					.getFirstChild();
			AST node_token_VALUE = node.getFirstChild().getNextSibling();
		} else {
			AST node_token_VARIABLE = node.getFirstChild();
			AST node_token_VALUE = node_token_VARIABLE.getNextSibling();
			expression(node_token_VALUE);
			byteCode.add(new Instruction(InsSet.istore, variableMap
					.get(node_token_VARIABLE.getText()) + ""));
		}
	}

	private void function_call(AST node) {
		// TODO : blablabla LEFT_PARENT

	}

	private void if_condition(AST node) { // TODO : slozene zavorky pred
		// if condition
		AST node_token_COND_EXPR = node.getFirstChild();
//		mini_print(node);
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
		expression(node_token_COND_EXPR);

		/**
		 * TODO: NOT WORKING FOR SIMPLE IF: puts goto on itself. Needs to be
		 * fixed
		 */

		int PC_ifJump = byteCode.size() - 1; // position of IF JUMP instruction

		// { ... } // if-part
		expression(node_token_IF_BRANCH); // compile if branch expression

		byteCode.add(new Instruction(InsSet.go_to, "")); // L1

		int PC_jumpToL2 = byteCode.size() - 1; // position of JUMP to L2

		// else { ... } // else-part; compile only when present
		if (node_token_ELSE_BRANCH != null)
			expression(node_token_ELSE_BRANCH);

		// old code
		// byteCode.add(new Instruction(InsSet.NOP, "")); // L2

		int PC_L2 = byteCode.size() - 1;

		// potrebuju:
		// 1) kam skocit z if
		byteCode.changeOperand(PC_ifJump, 0, (PC_jumpToL2 + 2) + "");

		// 2) kam skocit z konce if-part
		byteCode.changeOperand(PC_jumpToL2, 0, (PC_L2 + 2) + "");
	}

	private void variable_definition(AST node) {
		boolean isArray = false;
		AST node_token_TYPEVAL;
		AST dummy = node.getFirstChild(); // MODIFIERS
		AST node_token_TYPE = dummy.getNextSibling();
		if (node_token_TYPE.getFirstChild().getText().equals(LEFT_SQ_BR)) {
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
				byteCode.add(new Instruction(InsSet.bipush, varVal));
			} else {
				// Prirazujeme nejaky vyraz, musime ho nejdriv zpracovat a
				// vysledek pak hodit do promenne
				expression(node_token_ASSIGN.getFirstChild());

			}
			byteCode.add(new Instruction(InsSet.istore, BC_VariableCount + ""));
			variableMap.put(node_token_VARNAME.getText(), BC_VariableCount);
			BC_VariableCount++;
		} else {
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
		return str.matches("-?\\d+(\\.\\d+)?"); // match a number with optional
												// '-' and decimal.
	}
}
