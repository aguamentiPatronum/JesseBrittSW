package net.sf.eclipsecs.sample.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/*
 * The Blob, also named God Class, is a class implementing different responsibilities,
 * generally characterized by the presence of a high number of attributes and methods, which
 * implement different functionalities, and by many dependencies with data classes (i.e., classes
 * implementing only getter and setter methods) [24].
 */

public class BlobCheck extends AbstractCheck {

	private int max = 10;
	// variable to keep count of the number of variables declared
	private int variableCount = 0;
	// variable to keep count of the number of methods defined
	private int methodCount = 0;

	@Override
	public int[] getAcceptableTokens() {
		return new int[] { TokenTypes.VARIABLE_DEF, TokenTypes.METHOD_DEF };
	}

	@Override
	public int[] getRequiredTokens() {
		return new int[0];
	}

	@Override
	public int[] getDefaultTokens() {
		return new int[] { TokenTypes.VARIABLE_DEF, TokenTypes.METHOD_DEF };
	}

	/*
	 * Sets the max value for both the variables and methods
	 */
	public void setMax(int limit) {
		max = limit;
	}

	/*
	 * Counter Method; increments count for every desired token that is visited by
	 * one. Allows for re-factoring if needed to include other token types
	 */
	@Override
	public void visitToken(DetailAST ast) {
		switch (ast.getType()) {
		case TokenTypes.VARIABLE_DEF:
			variableCount++;
			break;
		case TokenTypes.METHOD_DEF:
			methodCount++;
			break;
		}
	}

	/*
	 * logs the number of violations or non-violations on the test code reset the
	 * counters until next call
	 */
	public void printTree(DetailAST tree) {
		log(tree.getLineNo(), "blobVar", variableCount);
		if (variableCount > max) {
			log(tree.getLineNo(), "blobVarMax", max);
		}
		variableCount = 0;

		log(tree.getLineNo(), "methodCount", methodCount);
		if (methodCount > max) {
			log(tree.getLineNo(), "methodlimit", max);
		}
		methodCount = 0;
	}

}
