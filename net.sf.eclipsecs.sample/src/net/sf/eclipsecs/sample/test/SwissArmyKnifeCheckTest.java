package net.sf.eclipsecs.sample.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.test.util.ReflectionTestUtils;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import net.sf.eclipsecs.sample.checks.SwissArmyKnifeCheck;

@PrepareForTest(SwissArmyKnifeCheck.class)
public class SwissArmyKnifeCheckTest {

	@Test
    public void testSetMaxInterfacesTest() {
        SwissArmyKnifeCheck check = new SwissArmyKnifeCheck();
        check.setMaxInterfaces(3);
        int interfaces = (int)ReflectionTestUtils.getField(check, "maxInterfaces");
        assertEquals(3, interfaces);
        assertNotEquals(4, interfaces);
        
        check.setMaxInterfaces(4);
        interfaces = (int)ReflectionTestUtils.getField(check, "maxInterfaces");
        assertEquals(4, interfaces);
        assertNotEquals(3, interfaces);
    }

    
    @Test
    public void testSetMaxMethodsTest() {
    	SwissArmyKnifeCheck check = new SwissArmyKnifeCheck();
        check.setMaxMethods(3);
        int methods = (int)ReflectionTestUtils.getField(check, "maxMethods");
        assertEquals(3, methods);
        assertNotEquals(4, methods);
        
        check.setMaxMethods(4);
        methods = (int)ReflectionTestUtils.getField(check, "maxMethods");
        assertEquals(4, methods);
        assertNotEquals(3, methods);
    }

    @Test
    public void testGetDefaultTokens() {
    	SwissArmyKnifeCheck check = new SwissArmyKnifeCheck();
		checkTokens(TokenTypes.METHOD_DEF,  check.getDefaultTokens());
		checkTokens(TokenTypes.IMPLEMENTS_CLAUSE,  check.getDefaultTokens());
		checkTokens(TokenTypes.CLASS_DEF,  check.getDefaultTokens());
		checkTokens(TokenTypes.EXPR,  check.getDefaultTokens());
		checkTokens(TokenTypes.LITERAL_IF,  check.getDefaultTokens());
    }

    @Test
    public void testGetAcceptableTokens() {
    	SwissArmyKnifeCheck check = PowerMockito.spy(new SwissArmyKnifeCheck());	
    	int[] tokens = check.getAcceptableTokens();
    	verify(check, times(1)).getRequiredTokens();
    }

    @Test
    public void testGetRequiredTokens() {
    	SwissArmyKnifeCheck check = new SwissArmyKnifeCheck();
		assertEquals(check.getRequiredTokens().length, 0);
		assertNotEquals(check.getRequiredTokens().length, -1);
		assertNotEquals(check.getRequiredTokens().length, 1);
    }
    
    @Test
	public void testVisitToken() {
    	//Use power to spy and verify that indeed checkViolations was called
		SwissArmyKnifeCheck check = PowerMockito.spy(new SwissArmyKnifeCheck());
		DetailAST ast = new DetailAST();
		ast.setType(TokenTypes.CLASS_DEF);
		DetailAST child = new DetailAST();
		child.setType(TokenTypes.IMPLEMENTS_CLAUSE);
		ast.setFirstChild(child);
		DetailAST nextChild = new DetailAST();
		nextChild.setType(TokenTypes.IDENT);
		child.addChild(nextChild);
		
		//Check for Class that has a child interface where the child interface has a child ident
		check.visitToken(ast);
		verify(check, times(1)).checkViolations(ast);
		
		child.removeChildren();
		
		//Check for Class that has a child interface with no children
		check.visitToken(ast);
		
		ast.removeChildren();
		child = new DetailAST();
		child.setType(TokenTypes.LITERAL_BOOLEAN);
		
		//Check Class that has a child of a type not of interface
		check.visitToken(child);
		
		ast.removeChildren();
		child = null;
		
		//Check for a class with no children
		check.visitToken(ast);
		
		ast.removeChildren();
		ast = new DetailAST();
		ast.setType(TokenTypes.METHOD_DEF);
		
		//Check for a Method DEF token
		check.visitToken(ast);
	}
	
	public void checkTokens(int desiredType, int[]acceptableTokens) {
		for(int i = 0; i < acceptableTokens.length; i++) {
			if(acceptableTokens[i] == desiredType) {
				assertTrue(true);
				break;
			}
		}
		assertFalse(false);
	}

}
