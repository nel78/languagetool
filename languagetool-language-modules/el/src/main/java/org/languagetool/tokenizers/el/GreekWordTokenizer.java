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
package org.languagetool.tokenizers.el;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.languagetool.tokenizers.Tokenizer;

/**
 *
 * @author Panagiotis Minos <pminos@gmail.com>
 */
public class GreekWordTokenizer implements Tokenizer {

  private GreekWordTokenizerImpl tokenizer = new GreekWordTokenizerImpl(new StringReader(""));

  public GreekWordTokenizer() {
  }

  @Override
  public List<String> tokenize(final String text) {
    final List<String> tokens = new ArrayList<>();
    tokenizer.yyreset(new StringReader(text));
    try {
      while (tokenizer.getNextToken() != GreekWordTokenizerImpl.YYEOF) {
        tokens.add(tokenizer.getText());
      }
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    return tokens;
  }
}
