package com.verisure.xad.poc.inv.kstream;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * This provides a test to check certain properties of a utility class.
 * Primarily that it has only one private constructor and no non-static methods
 * and it is final.
 *
 * http://stackoverflow.com/questions/4520216/how-to-add-test-coverage-to-a-private-constructor/10872497#10872497
 *
 * @author Archimedes Trajano
 */
@SuppressWarnings("java:S5960") // Avoid "Assertions" Sonar scanning for this utility class.
public final class UtilityClassTestUtil {
	/**
	 * Verifies that a utility class is well defined.
	 *
	 * @param clazz
	 *            utility class to verify.
	 */
	public static boolean assertUtilityClassWellDefined(final Class<?> clazz)
			throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		assertTrue(Modifier.isFinal(clazz.getModifiers()), "Class must be final");
		assertEquals(1, clazz.getDeclaredConstructors().length, "There must be only one constructor");
		final Constructor<?> constructor = clazz.getDeclaredConstructor();
		if (constructor.isAccessible() || !Modifier.isPrivate(constructor.getModifiers())) {
			fail("constructor is not private");
		}
		constructor.setAccessible(true);
		constructor.newInstance();
		constructor.setAccessible(false);
		for (final Method method : clazz.getMethods()) {
			if (!Modifier.isStatic(method.getModifiers()) && method.getDeclaringClass().equals(clazz)) {
				fail("there exists a non-static method:" + method);
			}
		}
		return true;
	}

	/**
	 * Prevent instantiation of utility class.
	 */
	private UtilityClassTestUtil() {
	}
}