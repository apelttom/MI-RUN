package cz.cvut.fit.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import antlr.collections.AST;
import cz.cvut.fit.run.compiler.ClassFile;
import cz.cvut.fit.run.compiler.Compiler;
import cz.cvut.fit.run.parser.JavaLexer;
import cz.cvut.fit.run.parser.JavaRecognizer;
import cz.cvut.fit.run.vm.Interpreter;

public class Main {

	public static void main(String[] args) throws Exception {
		// "src/cz/cvut/fit/run/ABCode.java"
		if (args != null && args.length == 1) {
			try {
				File file = new File(args[0]);
				BufferedReader reader = new BufferedReader(new FileReader(file));

				// Create a scanner that reads from the input stream
				JavaLexer lexer = new JavaLexer(reader);

				// Create a parser that reads from the scanner
				JavaRecognizer parser = new JavaRecognizer(lexer);

				// start parsing at the compilationUnit rule
				parser.compilationUnit();

				// get abstract syntax tree
				AST ast = parser.getAST();
				// printRoot(myTree);

				// generate all classfiles from given AST
				Compiler compiler = new Compiler();
				List<ClassFile> classfiles = compiler.compile(ast);

				// print bytecode of all classes
				for (ClassFile cf : classfiles) {
					System.out.println(cf);
				}

				// interpret bytecode
				Interpreter interpreter = new Interpreter(classfiles);
				interpreter.execute();

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			throw new IllegalArgumentException("please insert path to source in argument");
		}
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
		mini_print(node);
		printRoot(node.getFirstChild());
		printRoot(node.getNextSibling());
	}

	private static void mini_print(AST node) {
		System.out.println("------------------------------");
		String string = "    " + node.toString() + "\n";
		if (node.getFirstChild() != null) {
			string += node.getFirstChild().toString() + "\t";
			AST sibling = node.getFirstChild().getNextSibling();
			while (sibling != null) {
				string += sibling.toString() + "\t";
				sibling = sibling.getNextSibling();
			}
		}
		System.out.println(string);
	}
}
