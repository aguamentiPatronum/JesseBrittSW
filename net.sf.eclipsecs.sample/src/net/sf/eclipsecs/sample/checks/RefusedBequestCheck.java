package net.sf.eclipsecs.sample.checks;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.List;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;

public class RefusedBequestCheck extends AbstractCheck {
    private List<String>methodNames = new ArrayList<String>();
    private List<String>methodCalls = new ArrayList<String>();
    private List<String>localMethodNames = new ArrayList<String>();
    /**
    /* returns a set of TokenTypes which are processed in visitToken() method by default.*/
    @Override
    public int[] getDefaultTokens() {
        return new int[] { TokenTypes.CLASS_DEF, TokenTypes.METHOD_DEF, TokenTypes.METHOD_CALL};
    }

    /* returns a set, which contains all the TokenTypes that can be processed by the check. 
     * Both DefaultTokens and RequiredTokens and any custom set of TokenTypes are subsets 
     * of AcceptableTokens. */
    @Override
    public int[] getAcceptableTokens() {
        return getDefaultTokens();
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
    	if(ast.getType() == TokenTypes.CLASS_DEF) {
    		
    		//Get the parent (super) class
    		DetailAST parent = ast.getParent();
    		
    		//Null check required since if there is no super class the checkstyle would crash
    		if(parent != null) {
    			//Find the first sibling that is a method call and store the string value
        		DetailAST nextMethod = parent.findFirstToken(TokenTypes.METHOD_DEF);
        		
        		//Need the first child of the method call to find the string value for the method
        		DetailAST firstMethodChild = nextMethod.getFirstChild();
        		if(firstMethodChild.getType() == TokenTypes.IDENT) {
        			//We found the method text the first time, store it
        			methodNames.add(nextMethod.getText());
        		} else {
        			//Loop through all the siblings of METHOD_DEF looking for IDENT
        			while(firstMethodChild.getNextSibling() != null) {
        				DetailAST methodChildSibling = firstMethodChild.getNextSibling();
        				if(methodChildSibling.getType() == TokenTypes.IDENT) {
        					//Found the method definition string, save it
        					methodNames.add(methodChildSibling.getText());
        				}
        			}
        		}
        		  	
        		//Loop through all siblings of CLASS_DEF looking for method definitions and store their IDENT string
        		while(nextMethod.getNextSibling() != null) {
        			if(nextMethod.getNextSibling().getType() == TokenTypes.METHOD_DEF) {
        				firstMethodChild = nextMethod.getFirstChild();
        	    		if(firstMethodChild.getType() == TokenTypes.IDENT) {
        	    			//Just to be sure, make sure the first sibling is not IDENT
        	    			methodNames.add(nextMethod.getText());
        	    		} else {
        	    			while(firstMethodChild.getNextSibling() != null) {
        	    				//Find and store the method name
        	    				DetailAST methodChildSibling = firstMethodChild.getNextSibling();
        	    				if(methodChildSibling.getType() == TokenTypes.IDENT) {
        	    					methodNames.add(methodChildSibling.getText());
        	    				}
        	    			}
        	    		}
        			}
        		}
        		
        		
        		//Now look for method calls in the current AST
        		if(ast.getType() == TokenTypes.METHOD_CALL) {
        			DetailAST firstChild = ast.findFirstToken(TokenTypes.IDENT);
        			methodCalls.add(firstChild.getText());
        		}
        		
        		if(ast.getType() == TokenTypes.METHOD_DEF) {
        			//Store the method definition name
        			DetailAST methodCall = ast.findFirstToken(TokenTypes.IDENT);
        			
        			//Store the method definition in this class, drill down to see if the annotation ident is 
        			//@Override.  If so, store it to compare
        			DetailAST modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);
        			DetailAST annotations = modifiers.findFirstToken(TokenTypes.ANNOTATION);
        			DetailAST overrides = annotations.findFirstToken(TokenTypes.IDENT);
        				if(overrides.getText().equals("@Override")) {
        					localMethodNames.add(methodCall.getText());
        				}
        			}
        		}
        		
        		if(ast.getType() == TokenTypes.EOF) {
        			//We are at the end of parsing the class.  Look to make sure that each meth def is in the call list
        			for(String method : methodNames) {
        				if(!methodCalls.contains(method)) {
        					log(ast.getLineNo(), "Refused Bequest: Class is not using methods from their super class");
        				} else if(localMethodNames.contains(method)) {
        					log(ast.getLineNo(), "Refused Bequest: Same method in this class as the super class");
        				}
        			}
        		}
    		}
    	}
    }
