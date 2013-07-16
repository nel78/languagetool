/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.wikipedia;

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.languagetool.JLanguageTool;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.de.GermanSpellerRule;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SuggestionReplacerTest extends TestCase {

  public void testApplySuggestionToOriginalText() throws Exception {
    JLanguageTool langTool = getLanguageTool();
    SwebleWikipediaTextFilter filter = new SwebleWikipediaTextFilter();
    applySuggestion(langTool, filter, "Die CD ROM.", "Die <s>CD-ROM.</s>");
    applySuggestion(langTool, filter, "Die [[verlinkte]] CD ROM.", "Die [[verlinkte]] <s>CD-ROM.</s>");
    applySuggestion(langTool, filter, "Die [[Link|verlinkte]] CD ROM.", "Die [[Link|verlinkte]] <s>CD-ROM.</s>");
    applySuggestion(langTool, filter, "Die [[CD ROM]].", "Die <s>[[CD-ROM]].</s>");
    applySuggestion(langTool, filter, "Der [[Abschied]].\n\n==Überschrift==\n\nEin Ab schied.",
                                      "Der [[Abschied]].\n\n==Überschrift==\n\nEin <s>Abschied.</s>");
    applySuggestion(langTool, filter, "Ein ökonomischer Gottesdienst.",
                                      "Ein <s>ökumenisch</s> Gottesdienst.");  // known problem: does not inflect
    applySuggestion(langTool, filter, "Ein ökonomischer Gottesdienst mit ökonomischer Planung.",
                                      "Ein <s>ökumenisch</s> Gottesdienst mit ökonomischer Planung.");
    applySuggestion(langTool, filter, "\nEin ökonomischer Gottesdienst.\n",
                                      "\nEin <s>ökumenisch</s> Gottesdienst.\n");
    applySuggestion(langTool, filter, "\n\nEin ökonomischer Gottesdienst.\n",
                                      "\n\nEin <s>ökumenisch</s> Gottesdienst.\n");
  }

  public void testComplexText() throws Exception {
    String markup = "{{Dieser Artikel|behandelt die freie Onlineenzyklopädie Wikipedia; zu dem gleichnamigen Asteroiden siehe [[(274301) Wikipedia]].}}\n" +
            "\n" +
            "{{Infobox Website\n" +
            "| Name = '''Wikipedia'''\n" +
            "| Logo = [[Datei:Wikipedia-logo-v2-de.svg|180px|Das Wikipedia-Logo]]\n" +
            "| url = [//de.wikipedia.org/ de.wikipedia.org] (deutschsprachige Version)<br />\n" +
            "[//www.wikipedia.org/ www.wikipedia.org] (Übersicht aller Sprachen)\n" +
            "| Kommerziell = nein\n" +
            "| Beschreibung = [[Wiki]] einer freien kollektiv erstellten Online-Enzyklopädie\n" +
            "}}\n" +
            "\n" +
            "'''Wikipedia''' [{{IPA|ˌvɪkiˈpeːdia}}] (auch: ''die Wikipedia'') ist ein am [[15. Januar|15.&nbsp;Januar]] [[2001]] gegründetes Projekt. Und und so.\n";
    JLanguageTool langTool = getLanguageTool();
    SwebleWikipediaTextFilter filter = new SwebleWikipediaTextFilter();
    applySuggestion(langTool, filter, markup, markup.replace("Und und so.", "<s>Und so.</s>"));
  }

  public void testCompleteText() throws Exception {
    InputStream stream = SuggestionReplacerTest.class.getResourceAsStream("/org/languagetool/dev/wikipedia/wikipedia.txt");
    String origMarkup = IOUtils.toString(stream);
    JLanguageTool langTool = new JLanguageTool(new GermanyGerman());
    langTool.disableRule(GermanSpellerRule.RULE_ID);
    langTool.disableRule("DE_AGREEMENT");
    langTool.disableRule("GERMAN_WORD_REPEAT_BEGINNING_RULE");
    langTool.disableRule("COMMA_PARENTHESIS_WHITESPACE");
    langTool.disableRule("DE_CASE");
    langTool.disableRule("ABKUERZUNG_LEERZEICHEN");
    langTool.disableRule("TYPOGRAFISCHE_ANFUEHRUNGSZEICHEN");
    SwebleWikipediaTextFilter filter = new SwebleWikipediaTextFilter();
    PlainTextMapping mapping = filter.filter(origMarkup);
    List<RuleMatch> matches = langTool.check(mapping.getPlainText());
    assertThat("Expected 3 matches, got: " + matches, matches.size(), is(3));
    String markup = origMarkup;
    int oldPos = 0;
    for (RuleMatch match : matches) {
      SuggestionReplacer replacer = new SuggestionReplacer(mapping, markup, "<s>", "</s>");
      List<RuleApplication> ruleApplications = replacer.applySuggestionsToOriginalText(match);
      assertThat(ruleApplications.size(), is(1));
      RuleApplication ruleApplication = ruleApplications.get(0);
      assertThat(StringUtils.countMatches(ruleApplication.getTextWithCorrection(), "absichtlicher absichtlicher"), is(2));
      int pos = ruleApplication.getTextWithCorrection().indexOf("<s>absichtlicher</s> Fehler");
      if (pos == -1) {
        // markup area varies because our mapping is sometimes a bit off:
        pos = ruleApplication.getTextWithCorrection().indexOf("<s>absichtlicher Fehler</s>");
      }
      assertTrue("Found correction at: " + pos, pos > oldPos);
      oldPos = pos;
    }
  }

  public void testCompleteText2() throws Exception {
    InputStream stream = SuggestionReplacerTest.class.getResourceAsStream("/org/languagetool/dev/wikipedia/wikipedia2.txt");
    String origMarkup = IOUtils.toString(stream);
    JLanguageTool langTool = new JLanguageTool(new GermanyGerman());
    langTool.activateDefaultPatternRules();
    SwebleWikipediaTextFilter filter = new SwebleWikipediaTextFilter();
    PlainTextMapping mapping = filter.filter(origMarkup);
    List<RuleMatch> matches = langTool.check(mapping.getPlainText());
    assertTrue("Expected >= 30 matches, got: " + matches, matches.size() >= 30);
    String markup = origMarkup;
    for (RuleMatch match : matches) {
      SuggestionReplacer replacer = new SuggestionReplacer(mapping, markup, "<s>", "</s>");
      List<RuleApplication> ruleApplications = replacer.applySuggestionsToOriginalText(match);
      if (ruleApplications.size() == 0) {
        continue;
      }
      RuleApplication ruleApplication = ruleApplications.get(0);
      assertThat(StringUtils.countMatches(ruleApplication.getTextWithCorrection(), "<s>"), is(1));
    }
  }

  private JLanguageTool getLanguageTool() throws IOException {
    JLanguageTool langTool = new JLanguageTool(new GermanyGerman());
    langTool.activateDefaultPatternRules();
    langTool.disableRule("DE_CASE");
    return langTool;
  }

  private void applySuggestion(JLanguageTool langTool, SwebleWikipediaTextFilter filter, String text, String expected) throws IOException {
    PlainTextMapping mapping = filter.filter(text);
    List<RuleMatch> matches = langTool.check(mapping.getPlainText());
    assertThat("Expected 1 match, got: " + matches, matches.size(), is(1));
    SuggestionReplacer replacer = new SuggestionReplacer(mapping, text, "<s>", "</s>");
    List<RuleApplication> ruleApplications = replacer.applySuggestionsToOriginalText(matches.get(0));
    assertThat(ruleApplications.size(), is(1));
    assertThat(ruleApplications.get(0).getTextWithCorrection(), is(expected));
  }

}