package cz.cvut.fit.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import antlr.CommonAST;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;
import cz.cvut.fit.run.compiler.ByteCode;
import cz.cvut.fit.run.compiler.Compiler;
import cz.cvut.fit.run.compiler.Instruction;
import cz.cvut.fit.run.parser.JavaLexer;
import cz.cvut.fit.run.parser.JavaRecognizer;
import cz.cvut.fit.run.vm.Interpreter;

public class Main {

	public static void main(String[] args) throws Exception {
		// TODO add code path do argument

		try {
			File file = new File("src/cz/cvut/fit/run/Code.java");
			BufferedReader reader = new BufferedReader(new FileReader(file));

			// Create a scanner that reads from the input stream
			JavaLexer lexer = new JavaLexer(reader);

			// Create a parser that reads from the scanner
			JavaRecognizer parser = new JavaRecognizer(lexer);

			// start parsing at the compilationUnit rule
			parser.compilationUnit();

			// get AST tree and print it in LISP notation
			CommonAST myTree = (CommonAST) parser.getAST();
			// System.out.println(myTree.toStringList());
//			 printRoot(myTree);

			// generate bytecode, true = print nodes, false = you guess what :)
			Compiler compiler = new Compiler(false);
			ByteCode byteCode = compiler.compile(myTree);

//			 print bytecode
			int pc = 0;
			for (Instruction ins : byteCode) {
				pc++;
				System.out.println(pc + ": " + ins.toString());
			}

			// interpret bytecode somehow
			Interpreter interpreter = new Interpreter(byteCode);
//			interpreter.execute();

		} catch (FileNotFoundException | RecognitionException
				| TokenStreamException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Writes xml form of AST into a file given a specific path
	 * 
	 * @param t
	 *            AST writen into the file
	 * @throws IOException
	 *             if the file exists but is a directory rather than a regular
	 *             file, does not exist but cannot be created, or cannot be
	 *             opened for any other reason
	 */
	/*
	 * static void printCommonAST(CommonAST t) throws IOException{ File file =
	 * new File("path/Filename.xml"); Writer w = new FileWriter(file);
	 * t.xmlSerialize(w); w.flush(); }
	 */

	/**
	 * @param pc
	 *            number of digits of actual program counter
	 * @param max
	 *            number of digits of instructions
	 * @return bytecode padding
	 */
	private static String padding(int pc, int max) {
		String ret = ": ";
		for (int i = 0; i < max - pc; i++) {
			ret = ret.concat(" ");
		}
		return ret;
	}

	/**
	 * Prints the AST into console. Recursion method.
	 * 
	 * @param node
	 *            root of the tree which should be printed
	 */
	public static void printRoot(AST node) {
		if (node == null) {
			return;
		}
//		System.out.println(node.toString());
		mini_print(node);
		printRoot(node.getFirstChild());
		printRoot(node.getNextSibling());
	}
	
	private static void mini_print(AST node) {
		System.out
				.println("------------------------------");
		String Basic = "    " + node.toString() + "\n";
		if(node.getFirstChild() != null){
			Basic += node.getFirstChild().toString() + "\t";
			AST sibling = node.getFirstChild().getNextSibling();
			while(sibling != null){
				Basic += sibling.toString() + "\t";
				sibling = sibling.getNextSibling();
			}
		}
		System.out.println(Basic);
	}
}
