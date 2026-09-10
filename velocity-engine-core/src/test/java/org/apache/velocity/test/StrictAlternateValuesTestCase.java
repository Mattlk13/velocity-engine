package org.apache.velocity.test;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * Tests the alternate value of a reference under <code>runtime.references.strict</code>,
 * in both its spellings: strict mode is where the two differ the most from an elvis
 * operator elsewhere, so every case is asserted for <code>|</code> and for
 * <code>?:</code> alike.
 *
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author Nathan Bubna
 * @version $Id: AlternateValuesTestCase.java 1843764 2018-10-13 14:52:28Z cbrisson $
 */
public class StrictAlternateValuesTestCase extends BaseTestCase
{
    public StrictAlternateValuesTestCase(String name)
    {
        super(name);
    }

    @Override
    protected void setUpEngine(VelocityEngine engine)
    {
        engine.setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT, Boolean.TRUE);
    }

    @Override
    protected void setUpContext(VelocityContext context)
    {
        context.put("foo", null);
    }

    /**
     * Asserts a template rendering, then the same template with every pipe rewritten
     * as an elvis: the two spellings must be interchangeable, strict mode included.
     */
    private void assertBothSpellings(String expected, String vtl)
    {
        assertEvalEquals(expected, vtl);
        assertEvalEquals(expected, vtl.replace("|", "?:"));
    }

    /**
     * Same, for the templates that strict mode rejects.
     */
    private void assertBothSpellingsThrow(String vtl)
    {
        assertEvalException(vtl, VelocityException.class);
        assertEvalException(vtl.replace("|", "?:"), VelocityException.class);
    }

    public void testDefault()
    {
        assertBothSpellings("<foo>", "<${foo|'foo'}>");
        assertBothSpellings("bar", "#set($bar='bar')${foo|$bar}");
        assertBothSpellings("bar", "#set($bar='bar')${foo|${bar}}");
        // strict mode faults on the navigation, before the alternate value is reached
        assertBothSpellingsThrow("${foo.bar.baz()[5]|'hop'}");
        assertBothSpellings("{foo}", "{${foo|'foo'}}");
        assertEvalException("$foo", VelocityException.class);
    }

    public void testComplexEval()
    {
        assertBothSpellingsThrow("<${date.format('medium', $date.date)|'no date tool'}>");
        assertBothSpellings("true", "#set($val=false)${val.toString().replace(\"false\", \"true\")|'so what'}");
        assertBothSpellings("so what", "#set($foo='foo')${foo.contains('bar')|'so what'}");
        assertBothSpellings("so what", "#set($val=false)${val.toString().contains('bar')|'so what'}");
        assertBothSpellings("true", "#set($val=false)${val.toString().contains('false')|'so what'}");
        // an alternate value that is itself an unset variable faults in strict mode
        assertBothSpellingsThrow("$!{null|$null}");
        assertBothSpellings("null", "$!{null|'null'}");
        assertBothSpellings("so what", "#set($spaces='   ')${spaces.trim()|'so what'}");
    }

}
