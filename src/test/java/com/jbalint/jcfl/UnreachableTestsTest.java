package com.jbalint.jcfl;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.complexible.stardog.api.Connection;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.api.admin.AdminConnection;
import com.complexible.stardog.api.admin.AdminConnectionConfiguration;

import com.stardog.stark.Literal;
import com.stardog.stark.query.SelectQueryResult;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.jbalint.jora.proto.ClassFileToRdf;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by jbalint on 2020/01/07.
 *
 * TODO : should use the proxied Stardog but it doesn't work through the Java API
 */
public class UnreachableTestsTest {

	private static final String DB = "jcfl_unit_tests";

	@Test
	public void findReachableTests() throws IOException {
		try (AdminConnection admin = AdminConnectionConfiguration.toServer("http://localhost").connect()) {
			if (admin.list().contains(DB)) {
				admin.drop(DB);
			}
			admin.newDatabase(DB).create();
		}

		try (Connection conn = ConnectionConfiguration.to(DB)
		                                              .server("http://localhost")
		                                              .credentials("admin", "admin")
		                                              .connect()) {
//			File dir = new File("out/production/jcfl/com/jbalint/jcfl");
			File dir = new File("out/test/jcfl/com/jbalint/jcfl/testcode");
			conn.begin();
			for (File file : dir.listFiles()) {
				ClassFile cf = ClassFileParser.parse(file);
				conn.add().graph(ClassFileToRdf.toRdf(cf));
				ClassFileToRdf.writeTurtleString(ClassFileToRdf.toRdf(cf), System.err);
			}
			conn.commit();

			try (SelectQueryResult res = conn.select(Files.toString(new File("src/test/resources/UnreachableTest_Reachable_from_root.rq"), Charsets.UTF_8)).execute()) {
				Set<String> results = new HashSet<>();
				while (res.hasNext()) {
					results.add(((Literal) res.next().get("testMethodName")).label());
				}
				// TODO : there should be a third method, but we need to traverse the suites
				Set<String> expected = Sets.newHashSet("com.jbalint.jcfl.testcode.UnreachableTestsClasses%24TestClassReferencedViaSubclass.anAbstractTest%28%29V",
				                                       "com.jbalint.jcfl.testcode.UnreachableTestsClasses%24TestClassReferencedDirectly.someTest%28%29V");
				assertEquals(expected, results);
			}

		}
	}
}
