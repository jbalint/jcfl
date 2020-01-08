package com.jbalint.jcfl.testcode;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

/**
 * Code which has a test method which is not reachable from a suite. The
 * set of suites which all tests must be reachable from is fixed.
 */
public class UnreachableTestsClasses {

	@RunWith(org.junit.runners.Suite.class)
	@org.junit.runners.Suite.SuiteClasses(
		{TestClassReferencedDirectly.class,
		 TheSubclass.class,
		 IntermediateSuite.class,
		})
	public static class Suite {
	}

	/**
	 * Class is used directly in a suite
	 */
	public static class TestClassReferencedDirectly {
		@Test
		public void someTest() {
			assertTrue(System.currentTimeMillis() > 0);
		}
	}

	public abstract static class TestClassReferencedViaSubclass {
		@Test
		public void anAbstractTest() {
			assertTrue(System.currentTimeMillis() > 0);
		}
	}

	public static class TheSubclass extends TestClassReferencedViaSubclass {
	}

	public static class TestClassReferencedViaIntermediateSuite {
		@Test
		public void refViaIntermediateSuite() {
			assertTrue(System.currentTimeMillis() > 0);
		}
	}

	@RunWith(org.junit.runners.Suite.class)
	@org.junit.runners.Suite.SuiteClasses(TestClassReferencedViaIntermediateSuite.class)
	public static class IntermediateSuite {
	}

	public static class Test2_Unreachable {

		/**
		 * We are searching for methods such as this which are never used in a suite
		 */
		@Test
		public void woeIsMeUnreachabletest() {
			assertTrue(System.currentTimeMillis() > 0);
		}
	}
}
