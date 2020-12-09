
package net.sf.eclipsecs.sample.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Checkstyle looking for abusive OOP, switch statements, and AND/OR statements
 */
public class TypeCheckingCheck extends AbstractCheck{

      //Static data members used for max amount of logical tokens
      private int max_instances = 2;
      private int max_switches = 1;
      private long max_and_ors = 5;
      private int max_cases = 4;
      
      //Data Members to count the instances of logical tokens
      
      /**Data Member to keep track of how many "instanceof" Tokens were found*/
      private int instances = 0;
      
      /**Data Memeber to keep track of how many switch statements were found*/
      private int switches = 0;
      
      private int cases = 0;
      
      private String values=null;
      
      public void setMaxInstanceOf(int i) {
    	  max_instances = i;
      }
      
      public void setMaxSwitches(int i) {
    	  max_switches = i;
      }
      
      public void setMaxAndOrs(int i ) {
    	  max_and_ors = i;
      }
      
      public void setMaxCases(int i) {
    	  max_cases = i;
      }
      
      public int getMaxInstanceOf() {
    	  return max_instances;
      }
      
      public long getMaxAndOrs() {
    	  return max_and_ors;
      }
      
      public int getMaxCases() {
    	  return max_cases;
      }
      
      public int getMaxSwitches() {
    	  return max_switches;
      }
      
      
      @Override
      /**{@inheritDoc}*/
      public int[] getAcceptableTokens() {
        return new int[] {TokenTypes.LITERAL_SWITCH, 
                TokenTypes.CASE_GROUP, TokenTypes.LITERAL_INSTANCEOF, TokenTypes.EXPR, TokenTypes.LITERAL_IF};
      }

      @Override
      /**{@inheritDoc}*/
      public int[] getRequiredTokens() {
        return new int[0];
      }
		
	  @Override
	  /**{@inheritDoc}*/
	  public int[] getDefaultTokens() {
	    return new int[] {TokenTypes.LITERAL_INSTANCEOF, TokenTypes.LITERAL_SWITCH, TokenTypes.CASE_GROUP, 
	    		TokenTypes.EXPR, TokenTypes.LITERAL_IF};
	  }


	  @Override
	  /**
	   * Method that will utilize tree walker to walk through all code.  This method will stop
	   * at the specified tokens and count the instances
	   */
	  public void visitToken(DetailAST ast) {
		  checkForInstances(ast);
		  checkForSwitchStatements(ast);
		  checkForLongLogic(ast);
		  checkForCasesInSwitch(ast);
	  }
	  
	  /**
	   * Method that will check for abusive uses of switches.  This check will look for 
	   * switches that have too many cases and classes with too many switch statements
	   * @param ast - DetaiST
	   */
	  private final void checkForSwitchStatements(final DetailAST ast) {
		  //Literal Switch equals "switch(value)"
		  if(ast.getType() == TokenTypes.LITERAL_SWITCH) {
			  switches++;
			  if(switches >= max_switches) {
				  log(ast.getLineNo(), "Type Check violation detected:  Too many switches: ");
			  }
		  }
	  }
	  
	  private final void checkForCasesInSwitch(final DetailAST ast) {
		//Literal Switch equals "switch(value)"
		  if(ast.getType() == TokenTypes.LITERAL_SWITCH) {
			  int caseChildren = ast.getChildCount(TokenTypes.CASE_GROUP);
			  if(caseChildren >= max_cases) {
				  log(ast.getLineNo(), "Type Check violation detected:  Too many cases in switch");
			  }
		  }
	  }
	  
	  /**
	   * Method to look for cases of instanceof which could be an abuse of OOP
	   * @param ast - DetailAST
	   */
	  private final void checkForInstances(final DetailAST ast) {
		  if (ast.getType() == TokenTypes.EXPR) {
			  //
			  final DetailAST firstChild = ast.getFirstChild();
			  final DetailAST lastChild = ast.getLastChild();
			  //Get both children to look for instanceOf
			  if(firstChild != null && lastChild != null) {
				  if(firstChild.getType() == TokenTypes.LITERAL_INSTANCEOF || lastChild.getType() == TokenTypes.LITERAL_INSTANCEOF) {
					  instances++;
					  if(instances >= max_instances) {
						  log(ast.getLineNo(), "Type Check violation detected:  Too many instances of instanceof");
					  }
				  }
			  }
		  }
	  }
	  
	  /**
	   * Method that will look for excessive logical operations of && and || for an if statement
	   * @param ast - DetailAST
	   */
	  private final void checkForLongLogic(final DetailAST ast) {
		  if(ast.getType() == TokenTypes.LITERAL_IF) {
			  System.out.println(values);
			  if(!ast.toStringTree().contains("null")) {
				  values = ast.toStringTree();
			  }
			  if(values.indexOf("(") != -1 && values.indexOf(";") != -1) {
				  String substring =  values.substring(values.indexOf("(") + 1, values.indexOf(";"));
				  int countOr, countAnd, total = 0;
				  countAnd = count(substring, "&&");
				  countOr = count(substring, "||");
				  total = countOr + countAnd;
				  if(total >= max_and_ors) {
					  log(ast.getLineNo(), "Type Check violation detected: Too many logical operations per line");
				  }
			  }
		  }
		  
	  }
	  
	  /**
	   * Method to count the instance of a string within a string
	   * @param text - string to search in
	   * @param find - string to look for
	   * @return - the count of instances of find in text
	   */
	  public final int count(final String text, final String find) {
		  int lastIndex = 0;
		  int count = 0;

		  while((lastIndex = text.indexOf(find, lastIndex)) != -1) {
		       count++;
		       lastIndex += find.length() - 1;
		  }
	      return count;
	  }
}
