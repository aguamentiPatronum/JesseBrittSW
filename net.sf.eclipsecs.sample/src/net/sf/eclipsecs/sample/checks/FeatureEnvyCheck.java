package net.sf.eclipsecs.sample.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;


/**
 *  Check potential feature envy
 *  in a class 
 * 
 *  @author yang
 *
 */

public class FeatureEnvyCheck extends AbstractCheck {
    
    /**
     * FeatureEnvy happens when the function of object 1
     * relays on some fields of object 2. Therefore, all fields
     * need to be private to prevent potential feature envy
     * 
     */
    @Override
    public void visitToken(DetailAST ast) {
        if(ast != null) {
            final DetailAST ast1 = ast.findFirstToken(TokenTypes.OBJBLOCK);
            final DetailAST ast2 = ast1.findFirstToken(TokenTypes.VARIABLE_DEF);
            DetailAST codeBlock = ast2;
            while(codeBlock.getType() == TokenTypes.VARIABLE_DEF) {
                DetailAST modifierName = codeBlock.getFirstChild().getFirstChild();
                //System.out.println(modifierName.getText());
                if(modifierName == null || !modifierName.getText().equals("private")) {
                    detected();
                    ast.getLineNo();
                    //log(ast.getLineNo(), "FeatureEnvyCheck", 0);
                }
                    
                
                codeBlock = codeBlock.getNextSibling();
            }
        }
    }
    
    public void detected() {
        
    }
    @Override
    public int[] getAcceptableTokens() {
        // TODO Auto-generated method stub
        return getDefaultTokens();
    }

    @Override
    public int[] getDefaultTokens() {
        // TODO Auto-generated method stub
        return new int[] { TokenTypes.CLASS_DEF};
    }

    @Override
    public int[] getRequiredTokens() {
        // TODO Auto-generated method stub
        return getDefaultTokens();
    }

}







//package net.sf.eclipsecs.sample.checks;
//
//import java.util.Dictionary;
//import java.util.Enumeration;
//import java.util.Hashtable;
//import com.puppycrawl.tools.checkstyle.FileStatefulCheck;
//import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
//import com.puppycrawl.tools.checkstyle.api.DetailAST;
//import com.puppycrawl.tools.checkstyle.api.TextBlock;
//import com.puppycrawl.tools.checkstyle.api.TokenTypes;
//
///**
// * <p>
// * Checks for potential locations of Feature Envy code smells.
// * </p>
// * <p>
// * This check looks for a class which references any other class more often than
// * itself
// * </p>
// * <p>
// * Parent is {@code com.puppycrawl.tools.checkstyle.TreeWalker}
// * </p>
// */
//
//public class FeatureEnvyCheck extends AbstractCheck {
//    
//    public boolean FV_Found = false;
//
//    /**
//     * /* returns a set of TokenTypes which are processed in visitToken() method by
//     * default.
//     */
//    @Override
//    public int[] getDefaultTokens() {
//        // begin with ones for checking length of method
//        return new int[] { TokenTypes.METHOD_DEF };
//    }
//
//    /*
//     * returns a set, which contains all the TokenTypes that can be processed by the
//     * check. Both DefaultTokens and RequiredTokens and any custom set of TokenTypes
//     * are subsets of AcceptableTokens.
//     */
//    @Override
//    public int[] getAcceptableTokens() {
//        return getRequiredTokens();
//    }
//
//    /*
//     * returns a set of TokenTypes which Check must be subscribed to for a valid
//     * execution. If the user wants to specify a custom set of TokenTypes then this
//     * set must contain all the TokenTypes from RequiredTokens.
//     */
//    @Override
//    public int[] getRequiredTokens() {
//        return new int[0];
//    }
//
//    /**
//     * Checks each token
//     * 
//     * @param ast
//     */
//    @Override
//    public void visitToken(DetailAST ast) {
//        int count = ast.getChildCount();
//        System.out.println("Count is:");
//        System.out.println(count);
//        // call function in each method_def to count all references to classes
//        checkSiblings(ast.getFirstChild(), count);
//    }
//
//    /*
//     * returns a set of TokenTypes which Check must be subscribed to for a valid
//     * execution. If the user wants to specify a custom set of TokenTypes then this
//     * set must contain all the TokenTypes from RequiredTokens.
//     */
//    public void checkSiblings(DetailAST child, int count) {
//        // Initializing a Dictionary
//        Dictionary<String, Integer> classFeatures = new Hashtable<>();
//        Dictionary<String, Integer> classFeaturesLocal = new Hashtable<>();
//
//        System.out.println("Child: " + child.getText());
//        DetailAST sibling = child.getNextSibling();
//        if (sibling != null) {
//            System.out.println("sibling is : " + sibling.getText());
//            // while sibling is not SLIST or null
//            // get next sibling
//            classFeaturesLocal = findClassCalls(sibling, classFeatures);
//
//        }
//        this.FV_Found = countInstances(classFeaturesLocal, child);
//        if (this.FV_Found == true) {
//            logFeatureEnvy(child);
//        }
//    }
//    
//    /*
//     * returns a set of TokenTypes which Check must be subscribed to for a valid
//     * execution. If the user wants to specify a custom set of TokenTypes then this
//     * set must contain all the TokenTypes from RequiredTokens.
//     */
//    public void logFeatureEnvy(DetailAST child) {
//        System.out.println("Feature Envy Found");
//        log(child.getLineNo(), "Feature Envy Found");
//    }
//
//    /*
//     * returns a set of TokenTypes which Check must be subscribed to for a valid
//     * execution. If the user wants to specify a custom set of TokenTypes then this
//     * set must contain all the TokenTypes from RequiredTokens.
//     */
//    public Dictionary<String, Integer> findClassCalls(DetailAST sibling, Dictionary<String, Integer> classFeatures) {
//        // while sibling is not SLIST or null
//        // get next sibling
//        while (sibling != null) {
//            System.out.println("Sibling while looking for SLIST: " + sibling.getText());
//            if (sibling.getType() == TokenTypes.SLIST) {
//                System.out.println("SLIST FOUND");
//                // Pass to a function to count the number of variables and method calls
//                DetailAST s_list_child = sibling.getFirstChild();
//                while (s_list_child != null) {
//                    // Cycle through children looking for method or variable calls
//                    String class_called = checkSLIST(s_list_child);
//                    if (((Hashtable<String, Integer>) classFeatures).containsKey(class_called)) {
//                        classFeatures.put(class_called, classFeatures.get(class_called) + 1);
//                    } else {
//                        classFeatures.put(class_called, 1);
//                    }
//                    s_list_child = s_list_child.getNextSibling();
//                }
//            }
//            sibling = sibling.getNextSibling();
//        }
//        return classFeatures;
//    }
//
//    /*
//     * returns a set of TokenTypes which Check must be subscribed to for a valid
//     * execution. If the user wants to specify a custom set of TokenTypes then this
//     * set must contain all the TokenTypes from RequiredTokens.
//     */
//    public boolean countInstances(Dictionary<String, Integer> instanceDict, DetailAST child) {
//        // now we count how many references to internal
//        // vars and compare to external vars
//        System.out.println("Classes: " + instanceDict);
//        
//        int thisClassCount = 0;
//        Enumeration<String> e = instanceDict.keys();
//        
//        if (instanceDict.get("this") != null) {
//            thisClassCount = instanceDict.get("this");
//        }
//        
//        while (e.hasMoreElements()) {
//            String k = e.nextElement();
//            if (k != "none" && instanceDict.get(k) > thisClassCount) {
//                // more calls to external classes than to internal
//                return true;
//            }
//            System.out.println(k + ": " + instanceDict.get(k));
//        }
//        return false;
//    }
//
//    /**
//     * When SLIST token found, explore subtree here
//     * 
//     * @param child
//     */
//    public String checkSLIST(DetailAST child) {
//        if (child.getType() == TokenTypes.EXPR) {
//            child = child.getFirstChild();
//            System.out.println("Found EXPR");
//            if (child.getType() == TokenTypes.METHOD_CALL) {
//                child = child.getFirstChild();
//                System.out.println("Found METHOD_CALL");
//                if (child.getType() == TokenTypes.IDENT) {
//                    System.out.println("Found IDENT");
//                    return "this";
//                } else if (child.getType() == TokenTypes.DOT) {
//                    child = child.getFirstChild();
//                    System.out.println("Found DOT");
//                    if (child.getType() == TokenTypes.IDENT) {
//                        System.out.println("Found IDENT");
//                        return child.getText();
//                    }
//                }
//            }
//        }
//        return "none";
//    }
//}