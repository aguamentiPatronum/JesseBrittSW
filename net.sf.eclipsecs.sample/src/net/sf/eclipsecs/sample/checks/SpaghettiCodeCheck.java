package net.sf.eclipsecs.sample.checks;


import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;

/**
 * <p>
 * Checks for potential locations of Spaghetti Code code smells.
 * </p>
 * <p>
 * This check looks for long classes with no structure or inheritance, or
 * declaring long methods with no parameters, and utilising too many 
 * global variables.
 * </p>
 * * <ul>
 * <li>
 * Property {@code maxGlobalVars} - Most variables per class which
 * can be globals
 * Type is {@code integer}.
 * Default value is {@code 5}.
 * </li>
 * <li>
 * Property {@code maxClassLength} - Size class can be before getting
 * flagged for being too long
 * Default value is {@code 1000}.
 * </li>
 * <li>
 * Property {@code maxMethodLength} - Size methods can be before getting
 * flagged for being too long
 * Default value is {@code 150}.
 * </li>
 * </ul>
 * <p>
 * Parent is {@code com.puppycrawl.tools.checkstyle.TreeWalker}
 * </p>
 */

// Attribution: getBlockLength based some code off of getMethodLengthCHeck on checkstyle's 
// github page: https://github.com/checkstyle/checkstyle/blob/master/src/main
// /java/com/puppycrawl/tools/checkstyle/checks/sizes/MethodLengthCheck.java
public class SpaghettiCodeCheck extends AbstractCheck {
    
    private int maxGlobalVars = 5;
    private int maxClassLength = 55;
    private int maxMethodLength = 10;
    
    /**
     * Sets max number of global variables.
     * @param maxMethods
     */
    public void setMaxGlobalVars(int maxGlobalVars) {
        System.out.println("Setting maxGlobalVars");
        this.maxGlobalVars = maxGlobalVars;
    }
    
    /**
     * Gets max number of global variables.
     * @param maxMethods
     */
    public int getMaxGlobalVars() {
        return maxGlobalVars;
    }
    
    /**
     * Sets max length of any one class.
     * @param maxMethods
     */
    public void setMaxClassLength(int maxClassLength) {
        System.out.println("Setting class length");
        this.maxClassLength = maxClassLength;
    }
    
    /**
     * Gets max length of any one class.
     * @param maxMethods
     */
    public int getMaxClassLength() {
        return this.maxClassLength;
    }
    
    /**
     * Sets max number of lines in any one method.
     * @param maxMethods
     */
    public void setMaxLines(int maxMethodLength) {
        System.out.println("Setting method length");
        this.maxMethodLength = maxMethodLength;
    }
    
    /**
     * Gets max number of lines in any one method.
     * @param maxMethods
     */
    public int getMaxLines() {
        return this.maxMethodLength;
    }
    
    public int currentGlobalsCount = 0;
    
    /** Vars for local use **/
    public boolean TOO_MANY_GLOBALS = false;
    public boolean INHERITANCE = false;

    /**
    /* returns a set of TokenTypes which are processed in visitToken() method by default.*/
    @Override
    public int[] getDefaultTokens() {
        // begin with ones for checking length of method
        return new int[] {TokenTypes.METHOD_DEF, TokenTypes.CTOR_DEF, TokenTypes.IMPLEMENTS_CLAUSE, 
                TokenTypes.EXTENDS_CLAUSE, TokenTypes.CLASS_DEF};
    }

    /* returns a set, which contains all the TokenTypes that can be processed by the check. 
     * Both DefaultTokens and RequiredTokens and any custom set of TokenTypes are subsets 
     * of AcceptableTokens. */
    @Override
    public int[] getAcceptableTokens() {
        return getRequiredTokens();
    }

    /* returns a set of TokenTypes which Check must be subscribed to for a valid execution. 
     * If the user wants to specify a custom set of TokenTypes then this set must contain 
     * all the TokenTypes from RequiredTokens. */
    @Override
    public int[] getRequiredTokens() {
        return new int[0];
    }
    
    /**
     * Default function to process each node as the treewalker proceeds
     * 
     * @param ast, or a node
     */
    @Override
    public void visitToken(DetailAST ast) {
        System.out.println("token is found " + ast.getText());
        // look for methods that break the rules
        if (ast.getType() == TokenTypes.METHOD_DEF || ast.getType() == TokenTypes.CTOR_DEF) {
            // look for long methods with no parameters
            int numberOfParameters = checkParamCount(ast);
            if (numberOfParameters == 0) {
                int length = checkMethodLength(ast);
                System.out.println("Method length: " + length + "Max length: " + this.maxMethodLength);
                if (length > maxMethodLength && this.INHERITANCE == false) {
                    logSpaghettiCode(ast);
                }
            }
        }
        // Look for long classes using the get length function
        if (ast.getType() == TokenTypes.CLASS_DEF) {
            // we have found a new class, so reset our inheritance flag
            // Set flag if inheritance found
            this.INHERITANCE = checkInheritance(ast);
            System.out.println("Class found ");
            // Again, we are starting a new class sos tart the count at 0
            currentGlobalsCount = 0;
            DetailAST block = ast.findFirstToken(TokenTypes.OBJBLOCK);
            // check the length of the class and how many globals it has
            if (block != null) {
                System.out.println("OBJBLOCK found in check class");
                // Need opening and closing tokens for the class
                DetailAST openingBrace = block.findFirstToken(TokenTypes.LCURLY);
                if (openingBrace != null) {
                    // check globals
                    this.TOO_MANY_GLOBALS = checkClassVariables(openingBrace);
                    // check length
                    int length = checkClassLength(block, openingBrace);
                    if (length > this.maxClassLength && this.TOO_MANY_GLOBALS && this.INHERITANCE == false) {
                        logSpaghettiCode(ast);
                    }
                }
            }
        }
    }
    
    /**
     * Function to send notice of possible spaghetti code smell discovered
     */
    public void logSpaghettiCode(DetailAST ast) {
        log(ast.getLineNo(), "potential spaghetti code with long methods or too many globals", maxClassLength);
    }
    
    /**
     * Function to search a class definition's child for a token
     * which signifies inheritance
     */
    public boolean checkInheritance(DetailAST ast) {
        // to check inheritance look for Implements and Extends
        DetailAST imps = ast.findFirstToken(TokenTypes.IMPLEMENTS_CLAUSE);
        DetailAST exts = ast.findFirstToken(TokenTypes.EXTENDS_CLAUSE);
        if (imps != null || exts != null) {
            System.out.println("Inheritance is " + this.INHERITANCE);
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * Checks if a method has no parameters
     */
    public int checkParamCount(DetailAST ast) {
        // Spaghetti code in part is when methods with no parameters are really long
        // Since we found a method declaration, see if it has any parameters
        // if it does not have parameters, and its class has a lot of global variables
        // then there is a high likelihood of Spaghetti Code so we investigate
        DetailAST paramChild = ast.findFirstToken(TokenTypes.PARAMETERS);  
        int paramNumber = paramChild.getChildCount(TokenTypes.PARAMETER_DEF);
        if (paramNumber == 0) {
            return 0;
        }
        else {
            return 1;
        }
    }
    
    /**
     * Gets the total length of a specific method
     */
    public int checkMethodLength(DetailAST ast) {
        if (this.INHERITANCE == false) {
            if (this.TOO_MANY_GLOBALS == true) {
                DetailAST openingBrace = ast.findFirstToken(TokenTypes.SLIST);
                // The final requirement is that the method be too long
                // Use the pre-existing length function to check method length
                if (openingBrace != null) {
                    final DetailAST closingBrace = openingBrace.findFirstToken(TokenTypes.RCURLY);
                    final int length = getLengthOfBlock(openingBrace, closingBrace);
                    return length;
                }
            }
        }
        return 0;
    }
            
    /**
     * Checks top-level variables of a class to see if they are declared as public
     */
    public boolean checkClassVariables(DetailAST openingBrace) {
        // to find global variables: search for public variable at top-level of class
        // look for Var defs that are immediate children of objblock
        DetailAST blockChild = openingBrace.getNextSibling();
        while (blockChild != null) {
            // Find the variable definitions
            if(blockChild.getType() != TokenTypes.VARIABLE_DEF) {
                blockChild = blockChild.getNextSibling();
            }
            else {
                if (checkModifiers(blockChild) == true) {
                    return true;
                }
            }
            if(blockChild != null && blockChild.getNextSibling() != null) {
                blockChild = blockChild.getNextSibling();
            }
            else {
                blockChild = null;
            }
        }
        return false;
    }
    
    /**
     * Gets the length of a specific class
     */
    public int checkClassLength(DetailAST block, DetailAST openingBrace) {
        // Use the pre-existing length function to check class length
        DetailAST closingBrace = block.findFirstToken(TokenTypes.RCURLY);
        int length = getLengthOfBlock(openingBrace, closingBrace);
        System.out.println("Class length: " + length + "max class length: " + this.maxClassLength);
        return length;
    }

    public boolean checkModifiers(DetailAST blockChild) {
        // Then check the variable's modifiers
        DetailAST childType = blockChild.findFirstToken(TokenTypes.MODIFIERS);
        if (childType.getFirstChild() != null) {
            // if we find a variable outside a method that is public
            // count it as a global variable instance
            if (childType.getFirstChild().getType() == TokenTypes.LITERAL_PUBLIC) {
                this.currentGlobalsCount++;
                System.out.println("found public var: " + this.currentGlobalsCount);
                if (this.currentGlobalsCount > maxGlobalVars && this.INHERITANCE == false) {
                    System.out.println("found too many public var: " + this.currentGlobalsCount);
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Returns length of code only without comments and blank lines.
     *
     * @param openingBrace block opening brace
     * @param closingBrace block closing brace
     * @return number of lines with code for current block
     * Code for calculating length from getMethodLengthCHeck on checkstyle's github page
     */
    public int getLengthOfBlock(DetailAST openingBrace, DetailAST closingBrace) {
        int length = closingBrace.getLineNo() - openingBrace.getLineNo() + 1;
//        if (!countEmpty) {
//            final FileContents contents = getFileContents();
//            final int lastLine = closingBrace.getLineNo();
//            // lastLine - 1 is actual last line index. Last line is line with curly brace,
//            // which is always not empty. So, we make it lastLine - 2 to cover last line that
//            // actually may be empty.
//            for (int i = openingBrace.getLineNo() - 1; i <= lastLine - 2; i++) {
//                if (contents.lineIsBlank(i) || contents.lineIsComment(i)) {
//                    length--;
//                }
//            }
//        }
        return length;
    }

}
