package net.sf.eclipsecs.sample.checks;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;

/**
 * <p>
 * Checks for potential locations of Swiss Army Knife code smells.
 * </p>
 * <p>
 * This check looks for classes which implement a lot of interfaces
 * and have too many methods
 * </p>
 * * <ul>
 * <li>
 * Property {@code maxMethods} - How many methods a class can declare 
 * before being flagged
 * Type is {@code integer}.
 * Default value is {@code 5}.
 * </li>
 * <li>
 * Property {@code maxInterfaces} - How many interfaces can be 
 * implemented before being flagged
 * Default value is {@code 5}.
 * </li>
 * </ul>
 * <p>
 * Parent is {@code com.puppycrawl.tools.checkstyle.TreeWalker}
 * </p>
 */

public class SwissArmyKnifeCheck extends AbstractCheck {
     
    private int maxMethods = 5;
    private int maxInterfaces = 5;
    
    private int interfaceCount = 0;
    private int methodCount = 0;
    
    /**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     */
    public static final String MSG_KEY = "army.knife";
    

    /**
     * Sets max number of methods.
     * @param maxMethods
     */
    public void setMaxMethods(int maxMethods) {
        System.out.println("Setting Max");
        this.maxMethods = maxMethods;
    }

    
    /**
     * Sets max number of implemented interfaces.
     * @param maxInterfaces
     */
    public void setMaxInterfaces(int maxInterfaces) {
        this.maxInterfaces = maxInterfaces;
    }

    
    /**
    /* returns a set of TokenTypes which are processed in visitToken() method by default.*/
    @Override
    public int[] getDefaultTokens() {
        return new int[] { TokenTypes.METHOD_DEF, TokenTypes.IMPLEMENTS_CLAUSE, TokenTypes.CLASS_DEF };
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
     * Visits each token
     * @param ast
     */
    @Override
    public void visitToken(DetailAST ast) {
        System.out.println("Max Interfaces " + maxInterfaces );
        System.out.println("Max Methods " + maxMethods );
        if (ast.getType() == TokenTypes.CLASS_DEF) {
            DetailAST impToken = ast.findFirstToken(TokenTypes.IMPLEMENTS_CLAUSE);
            if (impToken != null) {
                interfaceCount = impToken.getChildCount(TokenTypes.IDENT);
                System.out.println("Now have found " + interfaceCount + " Interfaces");
                checkViolations(ast);
            }
        }
        else {
            if (ast.getType() == TokenTypes.METHOD_DEF) {
                methodCount++;
                System.out.println("Now have found " + methodCount + " Methods");
                checkViolations(ast);
            }
            System.out.println("No interface");
        }
    }
    
    /**
     * Tests for and logs violations
     * @param ast
     */
    public void checkViolations(DetailAST ast) {
        if (interfaceCount > maxInterfaces && methodCount > maxMethods) {
            System.out.println("Logging");
            log(ast.getLineNo(), MSG_KEY, "XX");
          }
    }
}
