package cz.cvut.fit.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import cz.cvut.fit.run.parser.JavaLexer;
import cz.cvut.fit.run.parser.JavaRecognizer;

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

			// here we have full AST
			System.out.println(parser.getAST().toStringTree());

			// generate bytecode

		} catch (FileNotFoundException | RecognitionException | TokenStreamException e) {
			e.printStackTrace();
		}
	}

}
