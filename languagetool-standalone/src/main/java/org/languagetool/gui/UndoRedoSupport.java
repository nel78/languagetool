/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.gui;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import org.languagetool.JLanguageTool;

/**
 * Provides Undo/Redo support and actions for JTextComponent
 *
 * @author Panagiotis Minos
 */
class UndoRedoSupport {

  final UndoAction undoAction;
  final RedoAction redoAction;
  private final UndoManager undoManager;

  UndoRedoSupport(JTextComponent textComponent) {
    undoManager = new UndoManager();
    undoAction = new UndoAction();
    redoAction = new RedoAction();
    textComponent.getDocument().addUndoableEditListener(new UndoableEditListener() {
      @Override
      public void undoableEditHappened(UndoableEditEvent e) {
        undoManager.addEdit(e.getEdit());
        undoAction.updateUndoState();
        redoAction.updateRedoState();
      }
    });
    InputMap inputMap = textComponent.getInputMap();
    KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_Z,
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    inputMap.put(key, "undo");
    textComponent.getActionMap().put("undo", undoAction);
    key = KeyStroke.getKeyStroke(KeyEvent.VK_Z,
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | java.awt.event.InputEvent.SHIFT_DOWN_MASK);
    inputMap.put(key, "redo");
    textComponent.getActionMap().put("redo", redoAction);
  }

  class UndoAction extends AbstractAction {

    private UndoAction() {
      super("Undo");
      Image img;
      img = Toolkit.getDefaultToolkit().getImage(
              JLanguageTool.getDataBroker().getFromResourceDirAsUrl("sc_undo.png"));
      putValue(Action.SMALL_ICON, new ImageIcon(img));
      img = Toolkit.getDefaultToolkit().getImage(
              JLanguageTool.getDataBroker().getFromResourceDirAsUrl("lc_undo.png"));
      putValue(Action.LARGE_ICON_KEY, new ImageIcon(img));
      KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_Z,
              Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
      putValue(Action.ACCELERATOR_KEY, key);
      putValue(Action.MNEMONIC_KEY, KeyEvent.VK_U);
      setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      try {
        undoManager.undo();
      } catch (CannotUndoException ex) {
        // ignore
      }
      updateUndoState();
      redoAction.updateRedoState();
    }

    private void updateUndoState() {
      if (undoManager.canUndo()) {
        setEnabled(true);
        putValue(Action.NAME, undoManager.getUndoPresentationName());
      } else {
        setEnabled(false);
        putValue(Action.NAME, "Undo");
      }
    }
  }

  class RedoAction extends AbstractAction {

    private RedoAction() {
      super("Redo");
      Image img;
      img = Toolkit.getDefaultToolkit().getImage(
              JLanguageTool.getDataBroker().getFromResourceDirAsUrl("sc_redo.png"));
      putValue(Action.SMALL_ICON, new ImageIcon(img));
      img = Toolkit.getDefaultToolkit().getImage(
              JLanguageTool.getDataBroker().getFromResourceDirAsUrl("lc_redo.png"));
      putValue(Action.LARGE_ICON_KEY, new ImageIcon(img));
      KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_Z,
              Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | java.awt.event.InputEvent.SHIFT_DOWN_MASK);
      putValue(Action.ACCELERATOR_KEY, key);
      putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
      setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      try {
        undoManager.redo();
      } catch (CannotRedoException ex) {
        // ignore
      }
      updateRedoState();
      undoAction.updateUndoState();
    }

    private void updateRedoState() {
      if (undoManager.canRedo()) {
        setEnabled(true);
        putValue(Action.NAME, undoManager.getRedoPresentationName());
      } else {
        setEnabled(false);
        putValue(Action.NAME, "Redo");
      }
    }
  }
}
