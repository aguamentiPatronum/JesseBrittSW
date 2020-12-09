package net.sf.eclipsecs.sample.test;

import static org.junit.Assert.*;

import java.lang.reflect.Field;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import net.sf.eclipsecs.sample.checks.TypeCheckingCheck;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.mockito.verification.PrivateMethodVerification;
import org.powermock.core.classloader.annotations.PrepareForTest;

public class TypeCheckingCheckTest {

	@Test
	public void testSetMaxCases() {
		TypeCheckingCheck tc = new TypeCheckingCheck();
		tc.setMaxCases(3);
		assertEquals(3, tc.getMaxCases());
		assertNotEquals(2, tc.getMaxCases());
	}
	
	@Test
	public void testSetMaxOrs() {
		TypeCheckingCheck tc = new TypeCheckingCheck();
		tc.setMaxAndOrs(3);
		assertEquals(3, tc.getMaxAndOrs());
		assertNotEquals(2, tc.getMaxAndOrs());
	}
	
	@Test
	public void testSetMaxInstanceOf() {
		TypeCheckingCheck tc = new TypeCheckingCheck();
		tc.setMaxInstanceOf(1);
		assertEquals(1, tc.getMaxInstanceOf());
		assertNotEquals(2, tc.getMaxInstanceOf());
	}
	
	@Test
	public void testSetMaxSwitches() {
		TypeCheckingCheck tc = new TypeCheckingCheck();
		tc.setMaxSwitches(3);
		assertEquals(3, tc.getMaxSwitches());
		assertNotEquals(2, tc.getMaxSwitches());
	}
	
	@Test
	public void testGetAcceptableTokens() {
		TypeCheckingCheck tc = new TypeCheckingCheck();
		checkTokens(TokenTypes.LITERAL_INSTANCEOF,  tc.getAcceptableTokens());
		checkTokens(TokenTypes.LITERAL_SWITCH,  tc.getAcceptableTokens());
		checkTokens(TokenTypes.CASE_GROUP,  tc.getAcceptableTokens());
		checkTokens(TokenTypes.EXPR,  tc.getAcceptableTokens());
		checkTokens(TokenTypes.LITERAL_IF,  tc.getAcceptableTokens());
	}
	
	@Test
	public void testGetDefaultTokens() {
		TypeCheckingCheck tc = new TypeCheckingCheck();
		checkTokens(TokenTypes.LITERAL_INSTANCEOF,  tc.getDefaultTokens());
		checkTokens(TokenTypes.LITERAL_SWITCH,  tc.getDefaultTokens());
		checkTokens(TokenTypes.CASE_GROUP,  tc.getDefaultTokens());
		checkTokens(TokenTypes.EXPR,  tc.getDefaultTokens());
		checkTokens(TokenTypes.LITERAL_IF,  tc.getDefaultTokens());
	}
	
	@Test
	public void testGetRequiredTokens() {
		TypeCheckingCheck tc = new TypeCheckingCheck();
		assertEquals(tc.getRequiredTokens().length, 0);
		assertNotEquals(tc.getRequiredTokens().length, -1);
		assertNotEquals(tc.getRequiredTokens().length, 1);
	}
	
	@Test
	public void testCount() {
		TypeCheckingCheck tc = new TypeCheckingCheck();
		String substring = "CptsHelloWorldCptsCptUselessStringCpts";
		int count = tc.count(substring, "Cpts");
		assertEquals(3, count);
		if(count > 3 || count < 3) {
			assertFalse(false);
		}
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
	
	@Test
	public void testCheckForCaseGroupStatements() {
		TypeCheckingCheck tc = new TypeCheckingCheck();	
		tc.setMaxSwitches(3);
		DetailAST ast = new DetailAST();
		ast.setType(TokenTypes.CASE_GROUP);
		ast.addChild(new DetailAST());
		ast.addChild(new DetailAST());
		ast.addChild(new DetailAST());
		ast.addChild(new DetailAST());
		
		ReflectionTestUtils.setField(tc, "switches", 1);
		ReflectionTestUtils.invokeMethod(tc,"checkForSwitchStatements", ast);	
	}

	@Test
	public void testCheckForSwitchStatements() {
		TypeCheckingCheck tc = new TypeCheckingCheck();	
		tc.setMaxSwitches(3);
		
		DetailAST ast = new DetailAST();
		ast.setType(TokenTypes.LITERAL_SWITCH);
		
		ReflectionTestUtils.setField(tc, "switches", 1);
		ReflectionTestUtils.invokeMethod(tc,"checkForSwitchStatements", ast);	
		
		ast = new DetailAST();
		ast.setType(TokenTypes.LITERAL_IF);
		
		ReflectionTestUtils.setField(tc, "switches", 1);
		ReflectionTestUtils.invokeMethod(tc,"checkForSwitchStatements", ast);
	}
	
	@Test
	public void testCheckForCasesInSwitch() {
		TypeCheckingCheck tc = new TypeCheckingCheck();	
		tc.setMaxCases(3);
		DetailAST ast = new DetailAST();
		ast.setType(TokenTypes.LITERAL_IF);
		
		ReflectionTestUtils.setField(tc, "cases", 1);
		ReflectionTestUtils.invokeMethod(tc,"checkForCasesInSwitch", ast);
		
		ast = new DetailAST();
		ast.setType(TokenTypes.LITERAL_SWITCH);
		
		ReflectionTestUtils.setField(tc, "cases", 1);
		ReflectionTestUtils.invokeMethod(tc,"checkForCasesInSwitch", ast);
		
	}
	
	@Test
	public void testVisitToken() {
		TypeCheckingCheck tc = PowerMockito.spy(new TypeCheckingCheck());
		
		DetailAST ast = new DetailAST();
		ast.setType(TokenTypes.LITERAL_IF);
		ReflectionTestUtils.setField(tc, "values", "(test string;", String.class);
		ReflectionTestUtils.invokeMethod(tc,"checkForLongLogic", ast);
		
		tc.visitToken(ast);
		
		try {
			//Test that all three methods in visit token were called
			PrivateMethodVerification privateMethodInvocation = PowerMockito.verifyPrivate(tc);
			privateMethodInvocation.invoke("checkForSwitchStatements", ast);
			privateMethodInvocation.invoke("checkForInstances", ast);
			privateMethodInvocation.invoke("checkForLongLogic", ast);
			privateMethodInvocation.invoke("checkForCasesInSwitch", ast);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	@Test
	public void testCheckForInstances() {
		TypeCheckingCheck tc = PowerMockito.spy(new TypeCheckingCheck());
		tc.setMaxInstanceOf(3);
		
		DetailAST ast = new DetailAST();
		DetailAST firstChild = new DetailAST();
		DetailAST lastChild = new DetailAST();	
		firstChild.setType(TokenTypes.LITERAL_INSTANCEOF);
		lastChild.setType(TokenTypes.LITERAL_INSTANCEOF);
		
		ast.addChild(firstChild);
		ast.addChild(lastChild);	
		ast.setType(TokenTypes.EXPR);
		
		//Test the happy path
		ReflectionTestUtils.setField(tc, "instances", 1);
		ReflectionTestUtils.invokeMethod(tc,"checkForInstances", ast);	
		
		//Test is first child has a different type
		firstChild = new DetailAST();
		lastChild = new DetailAST();	
		firstChild.setType(TokenTypes.LITERAL_BOOLEAN);
		lastChild.setType(TokenTypes.LITERAL_INSTANCEOF);
		ast = new DetailAST();
		ast.setType(TokenTypes.EXPR);
		
		ast.addChild(firstChild);
		ast.addChild(lastChild);
		ReflectionTestUtils.setField(tc, "instances", 1);
		ReflectionTestUtils.invokeMethod(tc,"checkForInstances", ast);	
		
		//Test is last child has a different type
		firstChild = new DetailAST();
		lastChild = new DetailAST();	
		firstChild.setType(TokenTypes.LITERAL_INSTANCEOF);
		lastChild.setType(TokenTypes.LITERAL_BOOLEAN);
		ast = new DetailAST();
		ast.setType(TokenTypes.EXPR);
		ast.addChild(firstChild);
		ast.addChild(lastChild);
		ReflectionTestUtils.setField(tc, "instances", 1);
		ReflectionTestUtils.invokeMethod(tc,"checkForInstances", ast);
		
		//Test if both children have different types
		firstChild = new DetailAST();
		lastChild = new DetailAST();
		firstChild.setType(TokenTypes.LITERAL_BOOLEAN);
		lastChild.setType(TokenTypes.LITERAL_BOOLEAN);
		ast = new DetailAST();
		ast.setType(TokenTypes.EXPR);
		ast.addChild(firstChild);
		ast.addChild(lastChild);
		ReflectionTestUtils.setField(tc, "instances", 1);
		ReflectionTestUtils.invokeMethod(tc,"checkForInstances", ast);
		
		//Check if first child is null
		firstChild = null;
		lastChild = new DetailAST();
		lastChild.setType(TokenTypes.LITERAL_INSTANCEOF);
		ast = new DetailAST();
		ast.setType(TokenTypes.EXPR);
		ast.addChild(firstChild);
		ast.addChild(lastChild);
		ReflectionTestUtils.setField(tc, "instances", 1);
		ReflectionTestUtils.invokeMethod(tc,"checkForInstances", ast);
		
		//Check if last child is null
		firstChild = new DetailAST();
		lastChild = null;
		firstChild.setType(TokenTypes.LITERAL_INSTANCEOF);
		ast = new DetailAST();
		ast.setType(TokenTypes.EXPR);
		ast.addChild(firstChild);
		ast.addChild(lastChild);
		ReflectionTestUtils.setField(tc, "instances", 1);
		ReflectionTestUtils.invokeMethod(tc,"checkForInstances", ast);
		
		//Check when both children are null
		firstChild = null;
		lastChild = null;
		ast = new DetailAST();
		ast.setType(TokenTypes.EXPR);
		ast.addChild(firstChild);
		ast.addChild(lastChild);
		ReflectionTestUtils.setField(tc, "instances", 1);
		ReflectionTestUtils.invokeMethod(tc,"checkForInstances", ast);
	}
	
	@Test
	public void testCheckForLongLogic() {
		TypeCheckingCheck tc = PowerMockito.spy(new TypeCheckingCheck());	
		DetailAST ast = new DetailAST();
		ast.setType(TokenTypes.LITERAL_IF);
		
		ReflectionTestUtils.setField(tc, "values", "(test string;", String.class);
		ReflectionTestUtils.invokeMethod(tc,"checkForLongLogic", ast);
		
		ReflectionTestUtils.setField(tc, "values", "(test string");
		ReflectionTestUtils.invokeMethod(tc,"checkForLongLogic", ast);
		
		ReflectionTestUtils.setField(tc, "values", "test string;");
		ReflectionTestUtils.invokeMethod(tc,"checkForLongLogic", ast);
		
		ReflectionTestUtils.setField(tc, "values", "null");
		ReflectionTestUtils.invokeMethod(tc,"checkForLongLogic", ast);
		
	}
}
