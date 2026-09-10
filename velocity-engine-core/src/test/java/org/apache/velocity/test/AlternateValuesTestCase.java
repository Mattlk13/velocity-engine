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

import org.apache.velocity.exception.ParseErrorException;

/**
 * Tests the alternate value of a reference, in both its spellings: the original
 * <code>${foo|alt}</code> pipe, deprecated since 2.5, and <code>${foo?:alt}</code>,
 * which the pipe defers to. The two are the same syntax slot, the same node and the
 * same falsy rule, so every case below is asserted twice.
 *
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author Nathan Bubna
 * @version $Id$
 */
public class AlternateValuesTestCase extends BaseTestCase
{
    public AlternateValuesTestCase(String name)
    {
        super(name);
    }

    /**
     * Asserts a template rendering, then the same template with every pipe
     * rewritten as an elvis: the two spellings must be interchangeable.
     */
    private void assertBothSpellings(String expected, String vtl)
    {
        assertEvalEquals(expected, vtl);
        assertEvalEquals(expected, vtl.replace("|", "?:"));
    }

    public void testDefault()
    {
        assertBothSpellings("<foo>", "<${foo|'foo'}>");
        assertBothSpellings("bar", "#set($bar='bar')${foo|$bar}");
        assertBothSpellings("bar", "#set($bar='bar')${bar|'foo'}");
        assertBothSpellings("bar", "#set($bar='bar')${foo|${bar}}");
        assertBothSpellings("baz", "${foo|${baz|'baz'}}");
        assertBothSpellings("hop", "${foo.bar.baz()[5]|'hop'}");
        assertBothSpellings("{foo}", "{${foo|'foo'}}");
        assertBothSpellings("<1>", "<${foo|1}>");
        assertBothSpellings("<1.1>", "<${foo|1.1}>");
    }

    public void testComplexEval()
    {
        assertBothSpellings("<no date tool>", "<${date.format('medium', $date.date)|'no date tool'}>");
        assertBothSpellings("true", "#set($val=false)${val.toString().replace(\"false\", \"true\")|'so what'}");
        assertBothSpellings("so what", "#set($foo='foo')${foo.contains('bar')|'so what'}");
        assertBothSpellings("so what", "#set($val=false)${val.toString().contains('bar')|'so what'}");
        assertBothSpellings("true", "#set($val=false)${val.toString().contains('false')|'so what'}");
        assertBothSpellings("", "$!{null|$null}");
        assertBothSpellings("null", "$!{null|'null'}");
        assertBothSpellings("so what", "#set($spaces='   ')${spaces.trim()|'so what'}");
    }

    /**
     * The two spellings can be mixed freely, including one nested in the other.
     */
    public void testSpellingsAreInterchangeable()
    {
        assertEvalEquals("foo-foo", "${a|'foo'}-${a?:'foo'}");
        assertEvalEquals("bar-bar", "#set($b='bar')${a|$b}-${a?:$b}");
        assertEvalEquals("deep", "${a?:${c|'deep'}}");
        assertEvalEquals("deep", "${a|${d?:'deep'}}");
    }

    /**
     * Same quiet behaviour as the pipe: a quiet reference whose alternate value is
     * itself undefined renders nothing, a non-quiet one renders its literal.
     */
    public void testQuietForm()
    {
        assertEvalEquals("", "$!{a?:$b}");
        assertEvalEquals("alt", "$!{a?:'alt'}");
        assertEvalEquals("${a?:$b}", "${a?:$b}");
    }

    /**
     * The right hand side is an expression, exactly as for the pipe: a bare word is
     * a parse error in both spellings (inside ${}, only the very first reference name
     * goes without its '$').
     */
    public void testBareWordIsAParseError()
    {
        assertEvalException("${a?:bar}", ParseErrorException.class);
        assertEvalException("${a|bar}", ParseErrorException.class);
    }

    /**
     * Like the pipe, the elvis is formal-only: in free text it is not an operator but
     * plain schmoo following the reference.
     */
    public void testElvisIsFormalOnly()
    {
        // an undefined reference renders its own literal, and the operator is plain text
        assertEvalEquals("$a?:'foo'", "$a?:'foo'");
        assertEvalEquals("$a|'foo'", "$a|'foo'");
        assertEvalEquals("bar?:'foo'", "#set($a='bar')$a?:'foo'");
    }
}
