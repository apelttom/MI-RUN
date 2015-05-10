package cz.cvut.fit.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import antlr.CommonAST;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;
import cz.cvut.fit.run.compiler.ClassFile;
import cz.cvut.fit.run.compiler.Compiler;
import cz.cvut.fit.run.compiler.MethodInfo;
import cz.cvut.fit.run.parser.JavaLexer;
import cz.cvut.fit.run.parser.JavaRecognizer;
import cz.cvut.fit.run.vm.Interpreter;

public class Main {
	
/* TODO: method invocation, object creation */

	public static void main(String[] args) throws Exception {
		// TODO add code path do argument

		try {
			File file = new File("src/cz/cvut/fit/run/ABCode.java");
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
			 printRoot(myTree);

			// generate bytecode, true = print nodes
			Compiler compiler = new Compiler();
			List<ClassFile> classfiles = compiler.compile(myTree);

			// print bytecode of all classes
			for (ClassFile cf : classfiles) {
				System.out.println(cf);
			}
			
			// interpret bytecode
			Interpreter interpreter = new Interpreter(classfiles);
			interpreter.execute();

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
		// System.out.println(node.toString());
		mini_print(node);
		printRoot(node.getFirstChild());
		printRoot(node.getNextSibling());
	}

	private static void mini_print(AST node) {
		System.out.println("------------------------------");
		String Basic = "    " + node.toString() + "\n";
		if (node.getFirstChild() != null) {
			Basic += node.getFirstChild().toString() + "\t";
			AST sibling = node.getFirstChild().getNextSibling();
			while (sibling != null) {
				Basic += sibling.toString() + "\t";
				sibling = sibling.getNextSibling();
			}
		}
		System.out.println(Basic);
	}
}
