/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino;

import io.trino.jdbc.TestJdbcResultSet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Optional;

import static io.trino.JdbcDriverCapabilities.testedVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
public class TestJdbcResultSetCompatibilityOldDriver
        extends TestJdbcResultSet
{
    private static final Optional<Integer> VERSION_UNDER_TEST = testedVersion();

    // Historical test JARs use TestNG lifecycle methods, which JUnit does not invoke.
    @BeforeAll
    public void setupLegacyServer()
    {
        if (!TestJdbcResultSet.class.isAnnotationPresent(TestInstance.class)) {
            setupServer();
        }
    }

    @AfterAll
    public void tearDownLegacyServer()
            throws Exception
    {
        if (!TestJdbcResultSet.class.isAnnotationPresent(TestInstance.class)) {
            tearDownServer();
        }
    }

    @Test
    public void ensureProperTestVersionLoaded()
    {
        assumeTrue(VERSION_UNDER_TEST.isPresent(), "Information about JDBC version under test is missing");

        assertThat(TestJdbcResultSet.class.getPackage().getImplementationVersion())
                .isEqualTo(VERSION_UNDER_TEST.get().toString());
    }

    @Override
    @Test
    public void testDate()
            throws Exception
    {
        try {
            super.testDate();
        }
        catch (AssertionError e) {
            // Old drivers are bundled with outdated tzdata which causes some of the tested dates to have a different representation
            assertThat(e).hasMessageContaining("1970-01-01");
        }
    }
}
