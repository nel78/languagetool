/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.commandline;

import junit.framework.TestCase;
import org.languagetool.Language;

public class CommandLineParserTest extends TestCase {

  public void testUsage() throws Exception {
    final CommandLineParser parser = new CommandLineParser();
    try {
      parser.parseOptions(new String[]{});
      fail();
    } catch (IllegalArgumentException expected) {}

    try {
      parser.parseOptions(new String[]{"--help"});
      fail();
    } catch (IllegalArgumentException expected) {}
  }

  public void testErrors() throws Exception {
    final CommandLineParser parser = new CommandLineParser();
    try {
      parser.parseOptions(new String[]{"--apply", "--taggeronly"});
      fail();
    } catch (IllegalArgumentException expected) {}
  }

  public void testSimple() throws Exception {
    final CommandLineParser parser = new CommandLineParser();
    CommandLineOptions options;

    options = parser.parseOptions(new String[]{"filename.txt"});
    assertNull(options.getLanguage());
    assertEquals("filename.txt", options.getFilename());
    assertFalse(options.isVerbose());

    options = parser.parseOptions(new String[]{"--language", "de", "filename.txt"});
    assertEquals(Language.GERMAN, options.getLanguage());
    assertEquals("filename.txt", options.getFilename());
    assertFalse(options.isVerbose());

    options = parser.parseOptions(new String[]{"-l", "de", "filename.txt"});
    assertEquals(Language.GERMAN, options.getLanguage());
    assertEquals("filename.txt", options.getFilename());
    assertFalse(options.isVerbose());

    options = parser.parseOptions(new String[]{"-v", "-l", "de", "filename.txt"});
    assertEquals(Language.GERMAN, options.getLanguage());
    assertEquals("filename.txt", options.getFilename());
    assertTrue(options.isVerbose());
  }

}