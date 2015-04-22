package cz.cvut.fit.run.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import antlr.collections.AST;

public class Compile {

	private int PC = 0;
	private int BC_VariableCount = 0; // pocet promennych v bytecode
	private Map<String, Integer> variableMap;
	private ByteCode byteCode;

	public Compile() {
		PC = BC_VariableCount = 0;
		variableMap = new HashMap<String, Integer>();
		byteCode = new ByteCode();
	}

	// expects AST root
	public ByteCode ast(AST node) {
		byteCode.clear();

		String tokenName = node.getText();
		if (tokenName.equals("METHOD_DEF")) {
			BC_VariableCount = 0; // resetujeme pocitadlo promennych, vstupujeme do lokalni promenne
			functionHeader(node);
		}

		return byteCode;
	}

	private List<AST> getAstChildren(AST node) {

		List<AST> list = new ArrayList<AST>();

		if (node == null) {
			return list;
		}

		AST dummy = node.getFirstChild();
		list.add(dummy);

		if (dummy == null) {
			return list;
		}

		while (dummy.getNextSibling() != null) {
			dummy = dummy.getNextSibling();
			list.add(dummy);
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
			List<AST> tokens_SINGLE_PARAM = getAstChildren(tokens_PARAMETERS.get(i));

			if (tokens_SINGLE_PARAM.isEmpty()) {
				break;
			}

			AST token_TYPE = tokens_SINGLE_PARAM.get(1);

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

	}

	private void return_statement(AST node) {
		expression(node.getFirstChild());
		// Nacez se do byte code da return
	}

	private void for_cycle(AST node) {
		/**
		 * Nejdrive provedu inicializaci (prvni cast "for" prikazu). Pak skocim dolu, kde nactu na zasobnik promenne pro podminku. Provedu
		 * podminku. Pokud je true, skocim nahoru na telo kodu (po nemz nasleduje inkrement a opet nacteni promennych pro podminku). Pokud
		 * je false, pokracuji dale, cyklus je hotovy. Struktura: > INIT > jump L1 (je potreba offset -2, kvuli nacteni promennych) > BODY
		 * [L2] > ITERATOR > CONDITION (+ nacteni promennych pro porovnani) [L1] > jump_if_false L3 > jump L2 > pokracovani programu [L3]
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
		byteCode.add(new Instruction(Instruction.InsSet.JUMP, ""));

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
		byteCode.getInstruction(PC_L1).setOpcode(byteCode.getInstruction(PC_L1).getInvertedForInstruction());

		// skok na L1
		// byteCode.add(new Instruction(Instruction.InsSet.JUMP,
		// (PC_jumpToL1 + 1) + ""));

	}

	private void expression(AST node) {
		AST token_EXPRESSION = node;
		if (token_EXPRESSION == null)
			return;
		String tokenName = token_EXPRESSION.getText();
		if (tokenName.equals(Constants.IF)) {
			if_condition(token_EXPRESSION);
		} else if (tokenName.equals(Constants.FOR)) {
			for_cycle(token_EXPRESSION);
		} else if (tokenName.equals(Constants.ASSIGN)) {
			assignment_expression(token_EXPRESSION);
		} else if (tokenName.equals(Constants.VARIABLE_DEF)) {
			variable_definition(token_EXPRESSION);
		} else if (tokenName.equals(Constants.RETURN)) {
			return_statement(token_EXPRESSION);
		} else if (tokenName.equals(Constants.EXPR) || tokenName.equals(Constants.LEFT_CR_BR)) {
			expression(token_EXPRESSION.getFirstChild());
		} else if (isLogical(tokenName)) {
			logic_expression(token_EXPRESSION);
		} else if (isArithmetic(tokenName)) {
			arithmetic_expression(token_EXPRESSION);
		} else if (isNumeric(tokenName)) {
			byteCode.add(new Instruction(Instruction.InsSet.PUSH_NUMBER, tokenName));
		} else
		// je to literal
		{
			byteCode.add(new Instruction(Instruction.InsSet.LOAD_VAR, variableMap.get(tokenName) + ""));
		}
	}

	// zkompiluje podminku v if (...), vstupem je token podminky
	private void logic_expression(AST node) {
		AST node_token_LEFT = node.getFirstChild(); // Left side
		AST node_token_RIGHT = node_token_LEFT.getNextSibling(); // Right side

		expression(node_token_LEFT);
		expression(node_token_RIGHT);

		if (node.getText().equals(Constants.LOGIC_GT)) { // >
			// if-less-than--then-jump-to
			byteCode.add(new Instruction(Instruction.InsSet.IF_LTE_JUMP, ""));
		} else if (node.getText().equals(Constants.LOGIC_LT)) { // <
			// if-greater-than--then-jump-to
			byteCode.add(new Instruction(Instruction.InsSet.IF_GTE_JUMP, ""));
		} else if (node.getText().equals(Constants.LOGIC_EQ)) { // ==
			// if-not-equal--then-jump-to
			byteCode.add(new Instruction(Instruction.InsSet.IF_NEQ_JUMP, ""));
		} else if (node.getText().equals(Constants.LOGIC_NEQ)) { // !=
			// if-equal--then-jump-to
			byteCode.add(new Instruction(Instruction.InsSet.IF_EQ_JUMP, ""));
		}
	}

	private void arithmetic_expression(AST node) {
		AST node_token_LEFT = node.getFirstChild();
		AST node_token_RIGHT = node_token_LEFT.getNextSibling();

		if (isNumeric(node_token_LEFT.getText())) {
			// levy argument je konstanta, dame ji rovnou na stack
			byteCode.add(new Instruction(Instruction.InsSet.PUSH_NUMBER, node_token_LEFT.getText()));
		} else {
			// levy argument je promenna, vytahneme ji z variable map
			expression(node_token_LEFT);
		}
		if (isNumeric(node_token_RIGHT.getText())) {
			byteCode.add(new Instruction(Instruction.InsSet.PUSH_NUMBER, node_token_RIGHT.getText()));
		} else {
			expression(node_token_RIGHT);
		}

		if (node.getText().equals(Constants.PLUS)) { // +
			byteCode.add(new Instruction(Instruction.InsSet.PLUS));
		} else if (node.getText().equals(Constants.MINUS)) { // -
			byteCode.add(new Instruction(Instruction.InsSet.MINUS));
		} else if (node.getText().equals(Constants.MULTI)) { // *
			byteCode.add(new Instruction(Instruction.InsSet.MULTIPLY));
		}
	}

	private void assignment_expression(AST node) {
		if (node.getFirstChild().getText().equals(Constants.LEFT_SQ_BR)) { // array
																			// assign
			AST node_token_VARIABLE = node.getFirstChild().getFirstChild();
			AST node_token_INDEX = node_token_VARIABLE.getNextSibling().getFirstChild();
			AST node_token_VALUE = node.getFirstChild().getNextSibling();
		} else {
			AST node_token_VARIABLE = node.getFirstChild();
			AST node_token_VALUE = node_token_VARIABLE.getNextSibling();
			expression(node_token_VALUE);
			byteCode.add(new Instruction(Instruction.InsSet.STORE_VAR, variableMap.get(node_token_VARIABLE.getText()) + "", "int"));
		}
	}

	private void function_call(AST node) {
		// TODO : blablabla Constants.LEFT_PARENT

	}

	private void if_condition(AST node) { // TODO : slozene zavorky pred
		// IFEXPR/ELSEEXPR
		AST node_token_CONDEXPR = node.getFirstChild(); // Condition
		AST node_token_IFEXPR = node_token_CONDEXPR.getNextSibling(); // If
																		// branch
		AST node_token_ELSEEXPR = node_token_IFEXPR.getNextSibling(); // Else
																		// branch,
																		// might
																		// be
																		// null

		AST node_token_CONDITION = node_token_CONDEXPR.getFirstChild(); // Condition
																		// operator
		AST node_token_LEFT = node_token_CONDITION.getFirstChild(); // Left side
		AST node_token_RIGHT = node_token_LEFT.getNextSibling(); // Right side

		// if (...)
		expression(node_token_CONDEXPR);

		int PC_ifJump = byteCode.size() - 1; // position of IF JUMP instruction

		// { ... } // if-part
		expression(node_token_IFEXPR); // compile if branch expression

		byteCode.add(new Instruction(Instruction.InsSet.JUMP, "")); // L1

		int PC_jumpToL2 = byteCode.size() - 1; // position of JUMP to L2

		// else { ... } // else-part; compile only when present
		if (node_token_ELSEEXPR != null)
			expression(node_token_ELSEEXPR);

		byteCode.add(new Instruction(Instruction.InsSet.NOP, "")); // L2

		int PC_L2 = byteCode.size() - 1;

		// potrebuju:
		// 1) kam skocit z if
		byteCode.changeOperand(PC_ifJump, 0, (PC_jumpToL2 + 1) + "");

		// 2) kam skocit z konce if-part
		byteCode.changeOperand(PC_jumpToL2, 0, PC_L2 + "");
	}

	private void variable_definition(AST node) {
		boolean isArray = false;
		AST node_token_TYPEVAL;
		AST dummy = node.getFirstChild(); // MODIFIERS
		AST node_token_TYPE = dummy.getNextSibling();
		if (node_token_TYPE.getFirstChild().getText().equals(Constants.LEFT_SQ_BR)) {
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
			AST node_token_VARVAL = node_token_ASSIGN.getFirstChild().getFirstChild();
			String varVal = "";
			if (node_token_VARVAL.getText().equals(Constants.MINUS))
				varVal = Constants.MINUS + node_token_VARVAL.getFirstChild().getText();
			else
				varVal = node_token_VARVAL.getText();
			if (isNumeric(varVal)) {
				// v deklaraci prirazujeme cislo, muzeme ho hodit na stack a
				// nahrat do promenne
				byteCode.add(new Instruction(Instruction.InsSet.PUSH_NUMBER, varVal));
			} else {
				// Prirazujeme nejaky vyraz, musime ho nejdriv zpracovat a
				// vysledek pak hodit do promenne
				expression(node_token_ASSIGN.getFirstChild());

			}
			byteCode.add(new Instruction(Instruction.InsSet.STORE_VAR, BC_VariableCount + "", "int"));
			variableMap.put(node_token_VARNAME.getText(), BC_VariableCount);
			BC_VariableCount++;
		} else {
			AST node_token_ARRLEN = node_token_ASSIGN.getFirstChild().getFirstChild().getFirstChild().getNextSibling().getFirstChild()
					.getFirstChild();
		}

	}
}
