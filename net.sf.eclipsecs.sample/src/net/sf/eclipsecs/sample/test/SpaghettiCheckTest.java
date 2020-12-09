package net.sf.eclipsecs.sample.test;

import static org.junit.Assert.*;
import org.junit.Test;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import antlr.CommonHiddenStreamToken;
import java.util.ArrayList;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import net.sf.eclipsecs.sample.checks.SpaghettiCodeCheck;
import org.junit.Before;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.*;

//These are needed to use PowerMock/EasyMocks
@PrepareForTest(SpaghettiCodeCheck.class)
public class SpaghettiCheckTest {

    public DetailAST tree;
    public SpaghettiCodeCheck tester;
    public SpaghettiCodeCheck mockTester;
    public SpaghettiCodeCheck spyTester;
    public int[] acceptableTokens;
    public int[] defaultTokens;
    public int[] unacceptableTokens;
    public int[] requiredTokens;
    ArrayList<Integer> tokenArr;

    /**
     * Set up a mock instance of the Spaghetti Code Checker Including the acceptable
     * and required tokens, saved to arrays
     */
    @Before
    public void setUp() throws Exception {
        acceptableTokens = new int[0];
        defaultTokens = new int[] { TokenTypes.METHOD_DEF, TokenTypes.CTOR_DEF, TokenTypes.IMPLEMENTS_CLAUSE,
                TokenTypes.EXTENDS_CLAUSE, TokenTypes.CLASS_DEF };
        unacceptableTokens = new int[] { TokenTypes.ABSTRACT, TokenTypes.ANNOTATION, TokenTypes.ANNOTATION_ARRAY_INIT,
                TokenTypes.ANNOTATION_DEF, TokenTypes.ANNOTATION_FIELD_DEF, TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR,
                TokenTypes.ANNOTATIONS, TokenTypes.ARRAY_DECLARATOR, TokenTypes.ARRAY_INIT, TokenTypes.ASSIGN,
                TokenTypes.AT };
        requiredTokens = new int[0];

        tree = mockAST(TokenTypes.PACKAGE_DEF, "package", "", 0, 0);

        tester = new SpaghettiCodeCheck();
        mockTester = mock(SpaghettiCodeCheck.class);
        spyTester = spy(tester);

        tokenArr = new ArrayList<Integer>();
        tokenArr.add(TokenTypes.METHOD_DEF);
        tokenArr.add(TokenTypes.CTOR_DEF);
        tokenArr.add(TokenTypes.IMPLEMENTS_CLAUSE);
        tokenArr.add(TokenTypes.EXTENDS_CLAUSE);
        tokenArr.add(TokenTypes.CLASS_DEF);
    }

    /**
     * This section tests the visitToken function
     */
    
    /**
     * Tests passing a CTOR_DEF token 
     */
    @Test
    public void testVisitTokenCTOR_DEF() {
        DetailAST ast1 = mockAST(TokenTypes.CTOR_DEF, "CTOR_DEF", "", 0, 0);
        Mockito.doReturn(0).when(spyTester).checkParamCount(ast1);
        spyTester.visitToken(ast1);
    }
    
    /**
     * Tests passing a Method_Def token
     */
    @Test
    public void testVisitTokenMETHOD_DEF() {
        DetailAST ast2 = mockAST(TokenTypes.METHOD_DEF, "METHOD_DEF", "", 0, 0);
        Mockito.doReturn(0).when(spyTester).checkParamCount(ast2);
        spyTester.visitToken(ast2);
    }
    
    /**
     * Tests passing a class definition token to visitToken
     */
    @Test
    public void testVisitTokenCLASS_DEF1() {
        DetailAST ast3 = mockAST(TokenTypes.CLASS_DEF, "CLASS_DEF", "", 0, 0);
        DetailAST astBlock = mockAST(TokenTypes.OBJBLOCK, "OBJBLOCK", "", 0, 0);
        DetailAST astOpening = mockAST(TokenTypes.LCURLY, "{", "", 0, 0);
        
        ast3.addChild(astBlock);    
        astBlock.addChild(astOpening);
        
        Mockito.doReturn(true).when(spyTester).checkClassVariables(astOpening);
        Mockito.doReturn(5).when(spyTester).checkClassLength(astBlock, astOpening);
        ReflectionTestUtils.setField(spyTester, "maxMethodLength", 6);
        spyTester.visitToken(ast3);
    }
    
    /**
     * Tests how visitToken handles running into a null token
     */
    @Test
    public void testVisitTokenCLASS_DEF_Null() {
        DetailAST ast3 = mockAST(TokenTypes.CLASS_DEF, "CLASS_DEF", "", 0, 0);
        DetailAST astBlock = mockAST(TokenTypes.OBJBLOCK, "OBJBLOCK", "", 0, 0);
        DetailAST astOpening = null;
        
        ast3.addChild(astBlock);    
        astBlock.addChild(astOpening);
        spyTester.visitToken(ast3);
    }
    
    /**
     * This section tests the main custom functions of the program
     */
    
    /**
     * Tests that the function to check for inheritance reports true
     * when an implements token is seen
     */
    @Test
    public void checkInheritance1() {
        assertFalse(spyTester.checkInheritance(tree));
        
        //recreate part of a tree of a class which implements another class
        DetailAST CLASS_DEFast = mockAST(TokenTypes.CLASS_DEF, "", "", 0, 0);
        DetailAST MODIFIERSast = mockAST(TokenTypes.MODIFIERS, "", "", 0, 0);
        DetailAST LITERAL_CLASSast = mockAST(TokenTypes.LITERAL_CLASS, "", "", 0, 0);
        DetailAST IDENTast = mockAST(TokenTypes.IDENT, "", "", 0, 0);
        DetailAST IMPLEMENTSast = mockAST(TokenTypes.IMPLEMENTS_CLAUSE, "", "", 0, 0);
        DetailAST OBJBLOCKast = mockAST(TokenTypes.OBJBLOCK, "", "", 0, 0);
        
        // now link them together in the right structure
        CLASS_DEFast.addChild(MODIFIERSast);
        CLASS_DEFast.addChild(LITERAL_CLASSast);
        CLASS_DEFast.addChild(IDENTast);
        CLASS_DEFast.addChild(IMPLEMENTSast);
        CLASS_DEFast.addChild(OBJBLOCKast);
        
        assertTrue(spyTester.checkInheritance(CLASS_DEFast));
    }
    
    /**
     * Tests that the function to check for inheritance reports true
     * when an extends token is seen
     */
    @Test
    public void checkInheritance2() {
        assertFalse(spyTester.checkInheritance(tree));
        
        //recreate part of a tree of a class which implements another class
        DetailAST CLASS_DEFast = mockAST(TokenTypes.CLASS_DEF, "", "", 0, 0);
        DetailAST MODIFIERSast = mockAST(TokenTypes.MODIFIERS, "", "", 0, 0);
        DetailAST LITERAL_CLASSast = mockAST(TokenTypes.LITERAL_CLASS, "", "", 0, 0);
        DetailAST IDENTast = mockAST(TokenTypes.IDENT, "", "", 0, 0);
        DetailAST EXTENDSast = mockAST(TokenTypes.EXTENDS_CLAUSE, "", "", 0, 0);
        DetailAST OBJBLOCKast = mockAST(TokenTypes.OBJBLOCK, "", "", 0, 0);
        
        // now link them together in the right structure
        CLASS_DEFast.addChild(MODIFIERSast);
        CLASS_DEFast.addChild(LITERAL_CLASSast);
        CLASS_DEFast.addChild(IDENTast);
        CLASS_DEFast.addChild(EXTENDSast);
        CLASS_DEFast.addChild(OBJBLOCKast);
        
        assertTrue(spyTester.checkInheritance(CLASS_DEFast));
    }

    /**
     * Tests that the get length function returns expected numeric values
     */
    @Test
    public void testGetLengthOfBlock() {
        DetailAST astOpeningBrace = new DetailAST();
        DetailAST astClosingBrace = new DetailAST();
        astOpeningBrace.setType(TokenTypes.LCURLY);
        astOpeningBrace.setType(TokenTypes.RCURLY);
        assertEquals(1, spyTester.getLengthOfBlock(astOpeningBrace, astClosingBrace));
        assertNotEquals(0, spyTester.getLengthOfBlock(astOpeningBrace, astClosingBrace));
        assertNotEquals(2, spyTester.getLengthOfBlock(astOpeningBrace, astClosingBrace));
    }

    /**
     * Tests that public variables are discovered and returns true
     */
    @Test 
    public void testCheckModifiersTrue() {
        ReflectionTestUtils.setField(spyTester, "currentGlobalsCount", 5);
        ReflectionTestUtils.setField(spyTester, "INHERITANCE", false);
        DetailAST blockChild = mockAST(TokenTypes.IMPLEMENTS_CLAUSE, "", "", 0, 0);
        DetailAST modChild = mockAST(TokenTypes.MODIFIERS, "MODIFIERS", "", 0, 0);
        DetailAST publicChild = mockAST(TokenTypes.LITERAL_PUBLIC, "public", "", 0, 0);
        blockChild.addChild(modChild);
        modChild.addChild(publicChild);
        assertTrue(spyTester.checkModifiers(blockChild));
    }
    
    /**
     * Tests that non-public variables returns false
     */
    @Test 
    public void testCheckModifiersFalse() {
        ReflectionTestUtils.setField(spyTester, "currentGlobalsCount", 5);
        ReflectionTestUtils.setField(spyTester, "INHERITANCE", false);
        DetailAST blockChild = mockAST(TokenTypes.IMPLEMENTS_CLAUSE, "", "", 0, 0);
        DetailAST modChild = mockAST(TokenTypes.MODIFIERS, "MODIFIERS", "", 0, 0);
        DetailAST nonPublicChild = mockAST(TokenTypes.LITERAL_PRIVATE, "public", "", 0, 0);
        blockChild.addChild(modChild);
        modChild.addChild(nonPublicChild);
        assertFalse(spyTester.checkModifiers(blockChild));
    }
    
    /**
     * Tests that methods with no parameters return 0
     */
    @Test
    public void testCheckParamCountEquals0() {
        ReflectionTestUtils.setField(spyTester, "TOO_MANY_GLOBALS", true);
        ReflectionTestUtils.setField(spyTester, "INHERITANCE", false);
        SpaghettiCodeCheck sCheck = new SpaghettiCodeCheck();
        
        DetailAST ast = mockAST(TokenTypes.METHOD_DEF, "METHOD_DEF", "", 0, 0);
        DetailAST paramChild = mockAST(TokenTypes.PARAMETERS, "PARAMETERS", "", 0, 0);
        
        ast.addChild(paramChild);
        
        DetailAST astOpening = mockAST(TokenTypes.SLIST, "{", "", 0, 0);
        DetailAST midChild = mockAST(TokenTypes.METHOD_DEF, "METHOD_DEF", "", 0, 0);
        DetailAST astClosing = mockAST(TokenTypes.RCURLY, "}", "", 0, 0);
        
        ast.addChild(astOpening);
        astOpening.addChild(midChild);
        astOpening.addChild(astClosing);

        when(spyTester.getLengthOfBlock(astOpening, astClosing)).thenReturn(7);
        assertEquals(0,spyTester.checkParamCount(ast));
    }
    
    /**
     * Tests that methods with parameters return not 0
     */
    @Test
    public void testCheckParamCountNotEquals0() {
        DetailAST ast = mockAST(TokenTypes.METHOD_DEF, "METHOD_DEF", "", 0, 0);
        DetailAST paramChild = mockAST(TokenTypes.PARAMETERS, "PARAMETERS", "", 0, 0);
        DetailAST paramDefChild = mockAST(TokenTypes.PARAMETER_DEF, "PARAMETER_DEF", "", 0, 0);
        
        ast.addChild(paramChild);
        assertEquals(0, spyTester.checkParamCount(ast));
        
        paramChild.addChild(paramDefChild);
        assertEquals(1, spyTester.checkParamCount(ast));
    }
    
    /**
     * Tests that iterating over variables in a class works
     * as expected
     */
    @Test
    public void testCheckClassVariablesTrue() {  
        DetailAST astOpening = mockAST(TokenTypes.LCURLY, "{", "", 0, 0);
        DetailAST block = null;
        astOpening.addChild(block);
        assertFalse(spyTester.checkClassVariables(astOpening));
        
        DetailAST astchild1 = mockAST(TokenTypes.VARIABLE_DEF, "VARIABLE_DEF", "", 0, 0);
        DetailAST astchild2 = mockAST(TokenTypes.VARIABLE_DEF, "VARIABLE_DEF", "", 0, 0);
        DetailAST astClosing = mockAST(TokenTypes.RCURLY, "}", "", 0, 0);
        DetailAST childChild = mockAST(TokenTypes.MODIFIERS, "MODIFIERS", "", 0, 0);
        DetailAST block2 = mockAST(TokenTypes.OBJBLOCK, "OBJBLOCK", "", 0, 0);
        
        block2.addChild(astOpening);
        block2.addChild(astchild1);
        block2.addChild(astchild2);
        block2.addChild(astClosing);
        
        astchild1.addChild(childChild);
        astchild2.addChild(childChild);
        
        //ReflectionTestUtils.invokeMethod(sCheck,"getLengthOfBlock", astOpening, astClosing);
        when(spyTester.checkModifiers(astchild2)).thenReturn(true);
        assertTrue(spyTester.checkClassVariables(astOpening));
    }
    
    /**
     * Tests that iterating over variables in a class works
     * as expected
     */
    @Test
    public void testCheckClassVariablesFalse() {  
        DetailAST astOpening = mockAST(TokenTypes.LCURLY, "{", "", 0, 0);
        DetailAST astchild1 = mockAST(TokenTypes.VARIABLE_DEF, "VARIABLE_DEF", "", 0, 0);
        DetailAST astchild2 = mockAST(TokenTypes.VARIABLE_DEF, "VARIABLE_DEF", "", 0, 0);
        DetailAST astClosing = mockAST(TokenTypes.RCURLY, "}", "", 0, 0);
        DetailAST childChild = mockAST(TokenTypes.MODIFIERS, "MODIFIERS", "", 0, 0);
        DetailAST block2 = mockAST(TokenTypes.OBJBLOCK, "OBJBLOCK", "", 0, 0);
        
        block2.addChild(astOpening);
        block2.addChild(astchild1);
        block2.addChild(astchild2);
        block2.addChild(astClosing);
        
        astchild1.addChild(childChild);
        astchild2.addChild(childChild);
        
        //ReflectionTestUtils.invokeMethod(sCheck,"getLengthOfBlock", astOpening, astClosing);
        when(spyTester.checkModifiers(astchild2)).thenReturn(false);
        assertFalse(spyTester.checkClassVariables(astOpening));
    }
    
    /**
     * Tests that measuring a class's length returns expected numeric values
     */
    @Test
    public void testCheckClassLength() {
        DetailAST block = mockAST(TokenTypes.OBJBLOCK, "OBJBLOCK", "", 0, 0);
        DetailAST astOpening = mockAST(TokenTypes.LCURLY, "{", "", 0, 0);
        DetailAST astClosing = mockAST(TokenTypes.RCURLY, "}", "", 0, 0);
        block.addChild(astOpening);
        block.addChild(astClosing);

        when(spyTester.getLengthOfBlock(astOpening, astClosing)).thenReturn(1);
        assertEquals(1, spyTester.checkClassLength(block,  astOpening));
        
        when(spyTester.getLengthOfBlock(astOpening, astClosing)).thenReturn(1000);
        assertEquals(1000, spyTester.checkClassLength(block, astOpening));
    }

    /**
     * Tests that measuring a method's length returns expected numeric values
     * when inheritance is true and too many globals is true
     */
    @Test
    public void testCheckMethodLength1() {
        DetailAST ast = mockAST(TokenTypes.OBJBLOCK, "OBJBLOCK", "", 0, 0);
        ReflectionTestUtils.setField(spyTester, "INHERITANCE", true);
        ReflectionTestUtils.setField(spyTester, "TOO_MANY_GLOBALS", true);
        assertEquals(0, spyTester.checkMethodLength(ast));
    }
    
    /**
     * Tests that measuring a method's length returns expected numeric values
     * when inheritance is true and too many globals is false
     */
    @Test
    public void testCheckMethodLength2() {
        DetailAST ast = mockAST(TokenTypes.OBJBLOCK, "OBJBLOCK", "", 0, 0);
        ReflectionTestUtils.setField(spyTester, "INHERITANCE", true);
        ReflectionTestUtils.setField(spyTester, "TOO_MANY_GLOBALS", false);
        assertEquals(0, spyTester.checkMethodLength(ast));
    }
    
    /**
     * Tests that measuring a method's length returns expected numeric values
     * when inheritance is false and too many globals is false
     */
    @Test
    public void testCheckMethodLength3() {
        DetailAST ast = mockAST(TokenTypes.OBJBLOCK, "OBJBLOCK", "", 0, 0);
        ReflectionTestUtils.setField(spyTester, "INHERITANCE", false);
        ReflectionTestUtils.setField(spyTester, "TOO_MANY_GLOBALS", false);
        assertEquals(0, spyTester.checkMethodLength(ast));
    }
    
    /**
     * Tests that measuring a method's length returns expected numeric values
     * when inheritance is false and too many globals is true
     */
    @Test
    public void testCheckMethodLength4_NotNull1() {
        DetailAST ast = mockAST(TokenTypes.OBJBLOCK, "OBJBLOCK", "", 0, 0);
        // Now test the combo that gets past the opening if's
        ReflectionTestUtils.setField(spyTester, "INHERITANCE", false);
        ReflectionTestUtils.setField(spyTester, "TOO_MANY_GLOBALS", true);

        DetailAST astOpening = mockAST(TokenTypes.SLIST, "{", "", 0, 0);
        DetailAST astClosing = mockAST(TokenTypes.RCURLY, "}", "", 0, 0);
        ast.addChild(astOpening);
        astOpening.addChild(astClosing);

        when(spyTester.getLengthOfBlock(astOpening, astClosing)).thenReturn(1000);
        assertEquals(1000, spyTester.checkMethodLength(ast));
    }
    
    /**
     * Tests that measuring a method's length returns expected numeric values
     * when inheritance is false and too many globals is true
     * And the method is longer than 0
     */
    @Test
    public void testCheckMethodLength4_NotNull2() {
        DetailAST ast = mockAST(TokenTypes.OBJBLOCK, "OBJBLOCK", "", 0, 0);
        // Now test the combo that gets past the opening if's
        ReflectionTestUtils.setField(spyTester, "INHERITANCE", false);
        ReflectionTestUtils.setField(spyTester, "TOO_MANY_GLOBALS", true);

        DetailAST astOpening = mockAST(TokenTypes.SLIST, "{", "", 0, 0);
        DetailAST astClosing = mockAST(TokenTypes.RCURLY, "}", "", 0, 0);
        ast.addChild(astOpening);
        astOpening.addChild(astClosing);

        when(spyTester.getLengthOfBlock(astOpening, astClosing)).thenReturn(1);
        assertEquals(1, spyTester.checkMethodLength(ast));
    }
    
    /**
     * Tests that measuring a method's length returns expected numeric values
     * when inheritance is false and too many globals is true
     * And the method is of length 0
     */
    @Test
    public void testCheckMethodLength4_Null() {
        DetailAST ast = mockAST(TokenTypes.OBJBLOCK, "OBJBLOCK", "", 0, 0);
        // Now test the combo that gets past the opening if's
        ReflectionTestUtils.setField(spyTester, "INHERITANCE", false);
        ReflectionTestUtils.setField(spyTester, "TOO_MANY_GLOBALS", true);

        DetailAST astOpening = null;
        ast.addChild(astOpening);

        assertEquals(0, spyTester.checkMethodLength(ast));
    }
    
    /**
     * This section contains basic unit tests for the getters and setters outside of
     * the default ones in the AbstractCheck Class
     */
    
    /**
     * Tests setting the max number of global variables
     */
    @Test
    public void testSetMaxGlobalVars() {
        SpaghettiCodeCheck sCheck = new SpaghettiCodeCheck();
        sCheck.setMaxGlobalVars(3);
        assertEquals(3, sCheck.getMaxGlobalVars());
        // assertNotEquals(2, sCheck.getMaxGlobalVars());
    }

    /**
     * Tests setting the max length of a class
     */
    @Test
    public void testSetMaxClassLength() {
        SpaghettiCodeCheck sCheck = new SpaghettiCodeCheck();
        sCheck.setMaxClassLength(3);
        assertEquals(3, sCheck.getMaxClassLength());
        assertNotEquals(2, sCheck.getMaxClassLength());
    }

    /**
     * Tests setting the max length of a method
     */
    @Test
    public void testSetMaxLines() {
        SpaghettiCodeCheck sCheck = new SpaghettiCodeCheck();
        sCheck.setMaxLines(3);
        assertEquals(3, sCheck.getMaxLines());
        assertNotEquals(2, sCheck.getMaxLines());
    }

    /**
     * This section contains basic unit tests for the getters and setters in the
     * AbstractCheck Class
     */
    
    /**
     * Tests that the returned default token array is what we expect
     */
    @Test
    public void testGetDefaultTokens() {
        assertArrayEquals(defaultTokens, spyTester.getDefaultTokens());
        verify(spyTester, times(1)).getDefaultTokens();
    }

    /**
     * Tests that the returned default token array is
     * not reported as equivalent to a random array of tokens
     */
    @Test
    public void testGetNotDefaultTokens() {
        assertNotEquals(unacceptableTokens, spyTester.getDefaultTokens());
        verify(spyTester, times(1)).getDefaultTokens();
    }

    /**
     * Tests that the function to get a default array of tokens
     * is called
     */
    @Test
    public void testGetNullDefaultTokens() {
        assertNotEquals(null, spyTester.getDefaultTokens());
        verify(spyTester, times(1)).getDefaultTokens();
    }

    /**
     * Tests that the returned required token array is what we expect
     */
    @Test
    public void testGetRequiredTokens() {
        assertArrayEquals(requiredTokens, spyTester.getRequiredTokens());
        verify(spyTester, times(1)).getRequiredTokens();
    }

    /**
     * Tests that the returned required tokens array is not reported as
     * equivalent to a random array of tokens
     */
    @Test
    public void testGetUnrequiredTokens() {
        assertNotEquals(unacceptableTokens, spyTester.getRequiredTokens());
        verify(spyTester, times(1)).getRequiredTokens();
    }

    /**
     * Tests that the function to get required tokens is called
     */
    @Test
    public void testGetNullRequiredTokens() {
        assertNotEquals(null, spyTester.getRequiredTokens());
        verify(spyTester, times(1)).getRequiredTokens();
    }

    /**
     * Tests that the returned acceptable token array is what we expect
     */
    @Test
    public void testGetAcceptableTokens() {
        assertArrayEquals(acceptableTokens, spyTester.getAcceptableTokens());
        verify(spyTester, times(1)).getAcceptableTokens();
    }

    /**
     * Tests that the returned acceptable token array is
     * not reported as equivalent to a random array of
     * tokens
     */
    @Test
    public void testGetUnacceptableTokens() {
        assertNotEquals(unacceptableTokens, spyTester.getAcceptableTokens());
        verify(spyTester, times(1)).getAcceptableTokens();
    }

    /**
     * Tests that the function to get the acceptable tokens array
     * does get called
     */
    @Test
    public void testGetNullAcceptableTokens() {
        assertNotEquals(null, spyTester.getAcceptableTokens());
        verify(spyTester, times(1)).getAcceptableTokens();
    }

/////////// mocking method from test
    /**
     * A function borrowed from github user Ivanov-Alex:
     * https://gist.github.com/ivanov-alex/e0cc14d3dc6fc1520283 Creates MOCK lexical
     * token and returns AST node for this token From Wikipedia: ANTLR can generate
     * lexers, parsers, tree parsers, and combined lexer-parsers. Parsers can
     * automatically generate parse trees or abstract syntax trees, which can be
     * further processed with tree parsers.
     * 
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
