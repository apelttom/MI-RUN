package cz.cvut.fit.run;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.security.Principal;

import antlr.CommonAST;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.TreeParser;
import antlr.collections.AST;
import cz.cvut.fit.run.parser.JavaLexer;
import cz.cvut.fit.run.parser.JavaRecognizer;
import cz.cvut.fit.run.parser.JavaTreeParser;

public class Main {

	public static void main(String[] args) {
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
//			System.out.println(myTree.toStringList());
			
//			printCommonAST(myTree);
			
			printRoot(myTree);
			
			
			// generate bytecode

		} catch (FileNotFoundException | RecognitionException | TokenStreamException e) {
			e.printStackTrace();
		} catch (IOException e) {
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
	static void printCommonAST(CommonAST t) throws IOException{
		File file = new File("path/Filename.xml");
		Writer w = new FileWriter(file);
		t.xmlSerialize(w);
		w.flush();
	}
*/
	
	/**
	 * Prints the AST into console. Recursion method.
	 * 
	 * @param node
	 *            root of the tree which should be printed
	 */
	static void printRoot(AST node) {
		if (node == null) {
			return;
		}
		System.out.println(node.toString());
		printRoot(node.getFirstChild());
		printRoot(node.getNextSibling());
	}

}
