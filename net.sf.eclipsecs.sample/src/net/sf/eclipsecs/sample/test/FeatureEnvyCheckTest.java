package net.sf.eclipsecs.sample.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Before;
import org.junit.Test;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;


import static org.junit.Assert.*;
import org.junit.Test;
import java.lang.reflect.Method; 
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor; 
import antlr.CommonHiddenStreamToken;

import static com.puppycrawl.tools.checkstyle.checks.naming.AbstractNameCheck.MSG_INVALID_PATTERN;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.Context;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.blocks.LeftCurlyCheck;
import com.puppycrawl.tools.checkstyle.checks.coding.EmptyStatementCheck;
import com.puppycrawl.tools.checkstyle.checks.coding.HiddenFieldCheck;
import com.puppycrawl.tools.checkstyle.checks.indentation.CommentsIndentationCheck;
import com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocPackageCheck;
import com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocParagraphCheck;
import com.puppycrawl.tools.checkstyle.checks.naming.ConstantNameCheck;
import com.puppycrawl.tools.checkstyle.checks.naming.MemberNameCheck;
import com.puppycrawl.tools.checkstyle.checks.naming.TypeNameCheck;
import com.puppycrawl.tools.checkstyle.filters.SuppressWithNearbyCommentFilter;
import com.puppycrawl.tools.checkstyle.filters.SuppressionCommentFilter;
import com.puppycrawl.tools.checkstyle.filters.SuppressionXpathFilter;

import java.util.ArrayList;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import net.sf.eclipsecs.sample.checks.FeatureEnvyCheck;
import net.sf.eclipsecs.sample.checks.TypeCheckingCheck;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import net.sf.eclipsecs.sample.checks.FeatureEnvyCheck;

public class FeatureEnvyCheckTest {

    public DetailAST tree;
    public FeatureEnvyCheck tester;
    public FeatureEnvyCheck mockTester;
    public FeatureEnvyCheck spyTester;
    public int[] acceptableTokens;
    public int[] defaultTokens;
    public int[] unacceptableTokens;
    public int[] requiredTokens;
    ArrayList<Integer> tokenArr;

    /**
     * Set up a mock instance of the Spaghetti Code Checker Including the acceptable
     * and required tokens, saved to arrays, to use in tests
     */
    @Before
    public void setUp() throws Exception {
        acceptableTokens = new int[0];
        defaultTokens = new int[] { TokenTypes.METHOD_DEF };
        unacceptableTokens = new int[] { TokenTypes.ABSTRACT, TokenTypes.ANNOTATION, TokenTypes.ANNOTATION_ARRAY_INIT,
                TokenTypes.ANNOTATION_DEF, TokenTypes.ANNOTATION_FIELD_DEF, TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR,
                TokenTypes.ANNOTATIONS, TokenTypes.ARRAY_DECLARATOR, TokenTypes.ARRAY_INIT, TokenTypes.ASSIGN,
                TokenTypes.AT };
        requiredTokens = new int[0];

        tree = new DetailAST();

        // What is this?
        // tree.setLineNo(42);

        tester = new FeatureEnvyCheck();
        mockTester = mock(FeatureEnvyCheck.class);
        spyTester = spy(tester);

        tokenArr = new ArrayList<Integer>();
        tokenArr.add(TokenTypes.METHOD_DEF);
    }
    
    /**
     * This section contains tests of the generic function visitToken
     */
    @Test
    public void testVisitToken() {
        DetailAST parentAST = mockAST(TokenTypes.METHOD_DEF, "parent", null, 0, 0);
        DetailAST childAST = mockAST(TokenTypes.METHOD_DEF, "child", null, 0, 0);
        DetailAST siblingAST = mockAST(TokenTypes.METHOD_DEF, "sibling", null, 0, 0);
        DetailAST sibling2AST = mockAST(TokenTypes.METHOD_DEF, "sibling", null, 0, 0);

        parentAST.addChild(childAST);
        parentAST.addChild(siblingAST);
        parentAST.addChild(sibling2AST);
         
        spyTester.visitToken(parentAST);
    }
    
    /**
     * This section contains tests of the more custom functions, specific to this
     * particular check
     */
    
    /*
     * Tests the function which counts a node's children
     * by asserting a value gets returned
     */
    @Test
    public void testGetChildCount() {
        FeatureEnvyCheck fe = new FeatureEnvyCheck();
        DetailAST methodAST = mockAST(TokenTypes.METHOD_DEF, null, null, 0, 0);
        // Need to mock a call to getChildCount - out of my control
        // Will use pre-created DetailAST 'tree'
        ReflectionTestUtils.setField(methodAST, "childCount", 3);
        assertEquals(3,methodAST.getChildCount());
    }
    
    /*
     * Tests the checkSiblings function when countInstances returns false
     */
    @Test
    public void testCheckSiblings1() {
        //As described in deliverable, this set of nodes acts as a mock-up
        // of a part of an actual parse tree from the treewalker GUI
        DetailAST parent = mockAST(TokenTypes.METHOD_DEF, "parent", null, 0, 0);
        DetailAST child1 = mockAST(TokenTypes.MODIFIERS, "MODIFIERS", null, 0, 0);
        DetailAST child2 = mockAST(TokenTypes.TYPE, "TYPE", null, 0, 0);
        DetailAST child3 = mockAST(TokenTypes.IDENT, "FE_This", null, 0, 0);
        DetailAST child4 = mockAST(TokenTypes.LPAREN, "(", null, 0, 0);
        DetailAST child5 = mockAST(TokenTypes.PARAMETERS, "PARAMETERS", null, 0, 0);
        DetailAST child6 = mockAST(TokenTypes.RPAREN, ")", null, 0, 0);
        DetailAST child7 = mockAST(TokenTypes.SLIST, "{", null, 0, 0);
        DetailAST grandchild = mockAST(TokenTypes.VARIABLE_DEF, "VARIABLE_DEF", null, 0, 0);
        DetailAST ggchild1 = mockAST(TokenTypes.MODIFIERS, "MODIFIERS", null, 0, 0);
        DetailAST ggchild2 = mockAST(TokenTypes.TYPE, "TYPE", null, 0, 0);
        DetailAST gggchild1 = mockAST(TokenTypes.IDENT, "String", null, 0, 0);
        DetailAST ggchild3 = mockAST(TokenTypes.IDENT, "newString1", null, 0, 0);
        DetailAST ggchild4 = mockAST(TokenTypes.ASSIGN, "=", null, 0, 0);
        DetailAST gggchild2 = mockAST(TokenTypes.EXPR, "EXPR", null, 0, 0);
        DetailAST ggggchild1 = mockAST(TokenTypes.METHOD_CALL, "(", null, 0, 0);
        DetailAST gggggchild1 = mockAST(TokenTypes.DOT, ".", null, 0, 0);
        DetailAST ggggggchild1 = mockAST(TokenTypes.LITERAL_THIS, "this", null, 0, 0);
        DetailAST ggggggchild2 = mockAST(TokenTypes.IDENT, "method1", null, 0, 0);
        DetailAST gggggchild2 = mockAST(TokenTypes.ELIST, "ELIST", null, 0, 0);
        DetailAST gggggchild3 = mockAST(TokenTypes.RPAREN, ")", null, 0, 0);
        DetailAST ggchild5 = mockAST(TokenTypes.SEMI, ";", null, 0, 0);
        
        parent.addChild(child1);
        parent.addChild(child2);
        parent.addChild(child3);
        parent.addChild(child4);
        parent.addChild(child5);
        parent.addChild(child6);
        parent.addChild(child7);
        child7.addChild(grandchild);
        grandchild.addChild(ggchild1);
        grandchild.addChild(ggchild2);
        ggchild2.addChild(gggchild1);
        grandchild.addChild(ggchild3);
        grandchild.addChild(ggchild4);
        ggchild4.addChild(gggchild2);
        gggchild2.addChild(ggggchild1);
        ggggchild1.addChild(gggggchild1);
        gggggchild1.addChild(ggggggchild1);
        gggggchild1.addChild(ggggggchild2);
        ggggchild1.addChild(gggggchild2);
        ggggchild1.addChild(gggggchild3);
        ggchild4.addChild(ggchild5);

        Dictionary<String, Integer> classFeaturesLocal = new Hashtable<>();
        int count = 3;

        // Need to mock a call to countInstances
        Mockito.doReturn(false).when(spyTester).countInstances(classFeaturesLocal, child1);
        
        spyTester.checkSiblings(child1, count);
        assertFalse(spyTester.FV_Found);
    }
    
    /*
     * Tests checkSiblings function when findClassCalls is called inside the loop
     */
    @Test
    public void testCheckSiblings2() {
        //As described in deliverable, this set of nodes acts as a mock-up
        // of a part of an actual parse tree from the treewalker GUI
        DetailAST parent = mockAST(TokenTypes.METHOD_DEF, "parent", null, 0, 0);
        DetailAST child1 = mockAST(TokenTypes.MODIFIERS, "MODIFIERS", null, 0, 0);
        DetailAST child2 = mockAST(TokenTypes.TYPE, "TYPE", null, 0, 0);
        DetailAST child3 = mockAST(TokenTypes.IDENT, "FE_This", null, 0, 0);
        DetailAST child4 = mockAST(TokenTypes.LPAREN, "(", null, 0, 0);
        DetailAST child5 = mockAST(TokenTypes.PARAMETERS, "PARAMETERS", null, 0, 0);
        DetailAST child6 = mockAST(TokenTypes.RPAREN, ")", null, 0, 0);
        DetailAST child7 = mockAST(TokenTypes.SLIST, "{", null, 0, 0);
        DetailAST grandchild = mockAST(TokenTypes.VARIABLE_DEF, "VARIABLE_DEF", null, 0, 0);
        DetailAST ggchild1 = mockAST(TokenTypes.MODIFIERS, "MODIFIERS", null, 0, 0);
        DetailAST ggchild2 = mockAST(TokenTypes.TYPE, "TYPE", null, 0, 0);
        DetailAST gggchild1 = mockAST(TokenTypes.IDENT, "String", null, 0, 0);
        DetailAST ggchild3 = mockAST(TokenTypes.IDENT, "newString1", null, 0, 0);
        DetailAST ggchild4 = mockAST(TokenTypes.ASSIGN, "=", null, 0, 0);
        DetailAST gggchild2 = mockAST(TokenTypes.EXPR, "EXPR", null, 0, 0);
        DetailAST ggggchild1 = mockAST(TokenTypes.METHOD_CALL, "(", null, 0, 0);
        DetailAST gggggchild1 = mockAST(TokenTypes.DOT, ".", null, 0, 0);
        DetailAST ggggggchild1 = mockAST(TokenTypes.LITERAL_THIS, "this", null, 0, 0);
        DetailAST ggggggchild2 = mockAST(TokenTypes.IDENT, "method1", null, 0, 0);
        DetailAST gggggchild2 = mockAST(TokenTypes.ELIST, "ELIST", null, 0, 0);
        DetailAST gggggchild3 = mockAST(TokenTypes.RPAREN, ")", null, 0, 0);
        DetailAST ggchild5 = mockAST(TokenTypes.SEMI, ";", null, 0, 0);
        
        parent.addChild(child1);
        parent.addChild(child2);
        parent.addChild(child3);
        parent.addChild(child4);
        parent.addChild(child5);
        parent.addChild(child6);
        parent.addChild(child7);
        child7.addChild(grandchild);
        grandchild.addChild(ggchild1);
        grandchild.addChild(ggchild2);
        ggchild2.addChild(gggchild1);
        grandchild.addChild(ggchild3);
        grandchild.addChild(ggchild4);
        ggchild4.addChild(gggchild2);
        gggchild2.addChild(ggggchild1);
        ggggchild1.addChild(gggggchild1);
        gggggchild1.addChild(ggggggchild1);
        gggggchild1.addChild(ggggggchild2);
        ggggchild1.addChild(gggggchild2);
        ggggchild1.addChild(gggggchild3);
        ggchild4.addChild(ggchild5);


        Dictionary<String, Integer> classFeaturesLocal = new Hashtable<>();
        int count = 3;

        // Need to mock a call to countInstances
        Mockito.doReturn(classFeaturesLocal).when(spyTester).findClassCalls(child2, classFeaturesLocal);
        
        spyTester.checkSiblings(child1, count);
        assertFalse(spyTester.FV_Found);
    }
    
    /*
     * Tests the case where instanceDict.get("this") == null by passing in a 
     * completely empty dictionary
     */
    @Test
    public void testCountInstancesNull() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        DetailAST childAST = mockAST(TokenTypes.EXPR, "testEXPR", "testString2", 0, 0);
        //let's create an empty dictionary
        Dictionary<String, Integer> tempDict = new Hashtable<>();
        assertFalse(spyTester.countInstances(tempDict, childAST));
    }
    
    /*
     * Tests where the following line evaluates to false:
     * (k != "none" && instanceDict.get(k) > thisClassCount)
     */
    @Test
    public void testCountInstancesFalse() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        DetailAST childAST = mockAST(TokenTypes.EXPR, "testEXPR", "testString2", 0, 0);
        //let's create an empty dictionary
        Dictionary<String, Integer> tempDict = new Hashtable<>();
        // We need to add to the dict at least one "none"
        tempDict.put("none", 1);
        // We need to add to the dict at least one "this"
        tempDict.put("this", 2);
        // also let's add two references to bicycle
        tempDict.put("bicycle", 1);
        // should get false
        assertFalse(spyTester.countInstances(tempDict, childAST));
    }
    
    /*
     * Tests where the following line evaluates to true:
     * (k != "none" && instanceDict.get(k) > thisClassCount)
     */
    @Test
    public void testCountInstancesTrue() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        DetailAST childAST = mockAST(TokenTypes.EXPR, "testEXPR", "testString2", 0, 0);
        //let's create an empty dictionary
        Dictionary<String, Integer> tempDict = new Hashtable<>();
        // We need to add to the dict at least one "none"
        tempDict.put("none", 1);
        // We need to add to the dict at least one "this"
        tempDict.put("this", 2);
        // also let's add two references to bicycle
        tempDict.put("bicycle", 3);
        // should get true
        assertTrue(spyTester.countInstances(tempDict, childAST));
    }
    
    /*
     * Tests when a Method Call has no children
     */
    @Test
    public void testCheckSLIST_none0() {
        DetailAST exprAST = mockAST(TokenTypes.METHOD_CALL, "testEXPR", "testString2", 0, 0);
        // now we need to reach the "none" line
        assertEquals("none", spyTester.checkSLIST(exprAST));
    }
    
    /*
     * Tests when EXPR is not followed by a method call
     */
    @Test
    public void testCheckSLIST_none1() {
        DetailAST exprAST = mockAST(TokenTypes.EXPR, "testEXPR", "testString2", 0, 0);
        DetailAST methodAST = mockAST(TokenTypes.LITERAL_INSTANCEOF, "instanceof", null, 0, 0);

        exprAST.addChild(methodAST);
        
        // now we need to reach the "none" line
        assertEquals("none", spyTester.checkSLIST(exprAST));
    }
    
    /*
     * Tests when a method call is not followed by an IDENT or DOT token
     */
    @Test
    public void testCheckSLIST_none2() {
        DetailAST exprAST = mockAST(TokenTypes.EXPR, "testEXPR", "testString2", 0, 0);
        DetailAST methodAST = mockAST(TokenTypes.METHOD_CALL, null, null, 0, 0);
        DetailAST identAST = mockAST(TokenTypes.ASSIGN, "=", null, 0, 0);

        exprAST.addChild(methodAST);
        methodAST.addChild(identAST);
        
        // now we need to reach the "none" line
        assertEquals("none", spyTester.checkSLIST(exprAST));
    }
    
    /*
     * Tests when a DOT token is not followed by an IDENT token
     */
    @Test
    public void testCheckSLIST_none3() {
        DetailAST exprAST = mockAST(TokenTypes.EXPR, "testEXPR", "testString2", 0, 0);
        DetailAST methodAST = mockAST(TokenTypes.METHOD_CALL, null, null, 0, 0);
        DetailAST dotAST = mockAST(TokenTypes.DOT, "6.", null, 0, 0);
        DetailAST identAST = mockAST(TokenTypes.ASSIGN, "=", null, 0, 0);

        exprAST.addChild(methodAST);
        methodAST.addChild(dotAST);
        dotAST.addChild(identAST);
        
        // now we need to reach the "none" line
        assertEquals("none", spyTester.checkSLIST(exprAST));
    }
    
    /*
     * Tests path through checkSLIST that returns string "this"
     */
    @Test
    public void testCheckSLIST_this() {
        DetailAST exprAST = mockAST(TokenTypes.EXPR, "testEXPR", "testString2", 0, 0);
        DetailAST methodAST = mockAST(TokenTypes.METHOD_CALL, null, null, 0, 0);
        DetailAST identAST = mockAST(TokenTypes.IDENT, "this", null, 0, 0);
        
        exprAST.addChild(methodAST);
        methodAST.addChild(identAST);
        
        assertEquals("this", spyTester.checkSLIST(exprAST));
    }
    
    /*
     * Tests path through checkSLIST that returns a name of another class
     */
    @Test
    public void testCheckSLIST_other() {
        DetailAST exprAST = mockAST(TokenTypes.EXPR, "testEXPR", "testString2", 0, 0);
        DetailAST methodAST = mockAST(TokenTypes.METHOD_CALL, null, null, 0, 0);
        DetailAST dotAST = mockAST(TokenTypes.DOT, null, null, 0, 0);
        DetailAST identAST = mockAST(TokenTypes.IDENT, "bicycle", null, 0, 0);
        
        exprAST.addChild(methodAST);
        methodAST.addChild(dotAST);
        dotAST.addChild(identAST);

        assertEquals("bicycle", spyTester.checkSLIST(exprAST));
    }
    
    /*
     * Tests that findClassCalls adds to the dictionary when the right tokens are seen
     */
    @Test 
    public void testFindClassCalls1() {
        DetailAST parent = mockAST(TokenTypes.METHOD_DEF, "METHOD_DEF", null, 0, 0);
        DetailAST siblingAST = mockAST(TokenTypes.EXPR, "firstChild", null, 0, 0);
        DetailAST siblingSiblingAST = mockAST(TokenTypes.SLIST, "secondChild", null, 0, 0);
        DetailAST siblingChildAST = mockAST(TokenTypes.VARIABLE_DEF, "VARIABLE_DEF", null, 0, 0);
        DetailAST siblingChild3AST = null;
        
        // set up tree
        parent.addChild(siblingAST);
        parent.addChild(siblingSiblingAST);
        siblingSiblingAST.addChild(siblingChildAST);
        siblingSiblingAST.addChild(siblingChild3AST);
        
        //let's create an empty dictionary
        Dictionary<String, Integer> emptyDict = new Hashtable<>();
        emptyDict.put("this", 1);
        
        //let's create a dictionary with "none": 0, "this":1
        Dictionary<String, Integer> thisDict = new Hashtable<>();
        thisDict.put("this", 2);
        
        Mockito.doReturn("this").when(spyTester).checkSLIST(siblingChildAST);
        //let's call findclasscalls and assert expected results
        assertEquals(thisDict, spyTester.findClassCalls(siblingAST, emptyDict));
    }
    
    /*
     * Tests that the loop only goes through once
     */
    @Test 
    public void testFindClassCalls2() {
        DetailAST parent = mockAST(TokenTypes.METHOD_DEF, "METHOD_DEF", null, 0, 0);
        DetailAST siblingAST = mockAST(TokenTypes.EXPR, "firstChild", null, 0, 0);
        DetailAST siblingSiblingAST = mockAST(TokenTypes.SLIST, "secondChild", null, 0, 0);
        DetailAST siblingChildAST = mockAST(TokenTypes.VARIABLE_DEF, "VARIABLE_DEF", null, 0, 0);
        DetailAST siblingChild3AST = null;
        
        // set up tree
        parent.addChild(siblingAST);
        parent.addChild(siblingSiblingAST);
        siblingSiblingAST.addChild(siblingChildAST);
        siblingSiblingAST.addChild(siblingChild3AST);
        
        //let's create an empty dictionary
        Dictionary<String, Integer> emptyDict = new Hashtable<>();
        
        //let's create a dictionary with "none": 0, "this":1
        Dictionary<String, Integer> thisDict = new Hashtable<>();
        thisDict.put("this", 2);
        
        Mockito.doReturn("this").when(spyTester).checkSLIST(siblingChildAST);
        //let's call findclasscalls and assert expected results
        assertNotEquals(thisDict, spyTester.findClassCalls(siblingAST, emptyDict));
    }
    


    /**
     * This section contains basic unit tests for the getters and setters in the
     * AbstractCheck Class
     */
    
    /*
     * Tests the default token are set up as expected and the returned result 
     * matches what we think the default tokens are
     */
    @Test
    public void testGetDefaultTokens() {
        assertArrayEquals(defaultTokens, spyTester.getDefaultTokens());
        verify(spyTester, times(1)).getDefaultTokens();
    }

    /*
     * Tests that when compared to a random array of tokens, the getDefaultTokens
     * function produces an array of tokens which doesn't match
     */
    @Test
    public void testGetNotDefaultTokens() {
        assertNotEquals(unacceptableTokens, spyTester.getDefaultTokens());
        verify(spyTester, times(1)).getDefaultTokens();
    }

    /*
     * Tests that the required tokens match what we expect and that the function
     * is called
     */
    @Test
    public void testGetRequiredTokens() {
        assertArrayEquals(requiredTokens, spyTester.getRequiredTokens());
        verify(spyTester, times(1)).getRequiredTokens();
    }

    /*
     * Tests that when compared to a random array of tokens, the 
     * getReuiredTokens function produces an array of tokens which 
     * doesn't match
     */
    @Test
    public void testGetUnrequiredTokens() {
        assertNotEquals(unacceptableTokens, spyTester.getRequiredTokens());
        verify(spyTester, times(1)).getRequiredTokens();
    }

    /*
     * Tests that getRequiredTokens does not return null
     */
    @Test
    public void testGetNullRequiredTokens() {
        assertNotEquals(null, spyTester.getRequiredTokens());
        verify(spyTester, times(1)).getRequiredTokens();
    }
    
    /*
     * Tests that the array of acceptable tokens matches what we expect
     */
    @Test
    public void testGetAcceptableTokens() {
        assertArrayEquals(acceptableTokens, spyTester.getAcceptableTokens());
        verify(spyTester, times(1)).getAcceptableTokens();
    }

    /*
     * Tests that when compared to a random array of tokens, the 
     * accweptable tokens does not match
     */
    @Test
    public void testGetUnacceptableTokens() {
        assertNotEquals(unacceptableTokens, spyTester.getAcceptableTokens());
        verify(spyTester, times(1)).getAcceptableTokens();
    }

    /*
     * Tests that getAcceptableTokens does not return null
     */
    @Test
    public void testGetNullAcceptableTokens() {
        assertNotEquals(null, spyTester.getAcceptableTokens());
        verify(spyTester, times(1)).getAcceptableTokens();
    }

/////////// mocking method from test
    /**
     * A function borrowed from github user Ivanov-Alex: 
     * https://gist.github.com/ivanov-alex/e0cc14d3dc6fc1520283
     * Creates MOCK lexical token and returns AST node for this token
     * From Wikipedia: ANTLR can generate lexers, parsers, tree parsers, 
     * and combined lexer-parsers. Parsers can automatically generate 
     * parse trees or abstract syntax trees, which can be further processed 
     * with tree parsers.
     * @param tokenType     type of token
     * @param tokenText     text of token
     * @param tokenFileName file name of token
     * @param tokenRow      token position in a file (row)
     * @param tokenColumn   token position in a file (column)
     * @return AST node for the token
     */
    private static DetailAST mockAST(final int tokenType, final String tokenText, final String tokenFileName,
            final int tokenRow, final int tokenColumn) {
        CommonHiddenStreamToken tokenImportSemi = new CommonHiddenStreamToken();
        tokenImportSemi.setType(tokenType);
        tokenImportSemi.setText(tokenText);
        tokenImportSemi.setLine(tokenRow);
        tokenImportSemi.setColumn(tokenColumn);
        tokenImportSemi.setFilename(tokenFileName);
        DetailAST astSemi = new DetailAST();
        astSemi.initialize(tokenImportSemi);
        return astSemi;
    }

}
