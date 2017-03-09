// Copyright (C) 2010, 2011, 2012 GlavSoft LLC.
// All rights reserved.
//
//-------------------------------------------------------------------------
// This file is part of the TightVNC software.  Please visit our Web site:
//
//                       http://www.tightvnc.com/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//-------------------------------------------------------------------------
//
package com.glavsoft.viewer.swing;

import com.glavsoft.core.SettingsChangedEvent;
import com.glavsoft.drawing.Renderer;
import com.glavsoft.rfb.IChangeSettingsListener;
import com.glavsoft.rfb.IRepaintController;
import com.glavsoft.rfb.encoding.PixelFormat;
import com.glavsoft.rfb.encoding.decoder.FramebufferUpdateRectangle;
import com.glavsoft.rfb.protocol.ProtocolContext;
import com.glavsoft.rfb.protocol.ProtocolSettings;
import com.glavsoft.transport.Reader;
import com.glavsoft.viewer.Viewer;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class Surface extends JPanel implements IRepaintController, IChangeSettingsListener {

    private int width;
    private int height;
    private SoftCursorImpl cursor;
    private RendererImpl renderer;
    private MouseEventListener mouseEventListener;
    private KeyEventListener keyEventListener;
    private boolean showCursor;
    private ModifierButtonEventListener modifierButtonListener;
    private boolean isUserInputEnabled = false;
    private final ProtocolContext context;
    private double scaleFactorx,scaleFactory;
    private final Viewer viewer;
    public Dimension oldSize;


    @Override
    public boolean isDoubleBuffered() {
        // TODO returning false in some reason may speed ups drawing, but may
        // not. Needed in challenging.
        return false;
    }

    public Surface(ProtocolContext context, Viewer viewer) {
        this.context = context;
        this.viewer = viewer;
        init(context.getFbWidth(), context.getFbHeight());
        oldSize = getPreferredSize();

        if (!context.getSettings().isViewOnly()) {
            setUserInputEnabled(true, context.getSettings().isConvertToAscii());
        }
        showCursor = context.getSettings().isShowRemoteCursor();
    }

    private void setUserInputEnabled(boolean enable, boolean convertToAscii) {
        if (enable == isUserInputEnabled) {
            return;
        }
        isUserInputEnabled = enable;
        if (enable) {
            if (null == mouseEventListener) {
                mouseEventListener = new MouseEventListener(this, context, 0,0);
            }
            addMouseListener(mouseEventListener);
            addMouseMotionListener(mouseEventListener);
            addMouseWheelListener(mouseEventListener);

            setFocusTraversalKeysEnabled(false);
            if (null == keyEventListener) {
                keyEventListener = new KeyEventListener(context);
                if (modifierButtonListener != null) {
                    keyEventListener.addModifierListener(modifierButtonListener);
                }
            }
            keyEventListener.setConvertToAscii(convertToAscii);
            addKeyListener(keyEventListener);
            enableInputMethods(false);
        } else {
            removeMouseListener(mouseEventListener);
            removeMouseMotionListener(mouseEventListener);
            removeMouseWheelListener(mouseEventListener);
            removeKeyListener(keyEventListener);
        }
    }

    @Override
    public Renderer createRenderer(Reader reader, int width, int height, PixelFormat pixelFormat) {
        renderer = new RendererImpl(reader, width, height, pixelFormat);
        synchronized (renderer) {
            cursor = renderer.getCursor();
        }
        init(renderer.getWidth(), renderer.getHeight());
        updateFrameSize();
        return renderer;
    }

    private void init(int width, int height) {
        this.width = width;
        this.height = height;
        setSize(getPreferredSize());
    }

    private void updateFrameSize() {
        setSize(getPreferredSize());
        viewer.packContainer();
        requestFocus();
    }

    @Override
    public void paintComponent(Graphics g) {
        ((Graphics2D) g).scale(scaleFactorx, scaleFactory);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        synchronized (renderer) {
            Image offscreenImage = renderer.getOffscreenImage();
            if (offscreenImage != null) {
                g.drawImage(offscreenImage, 0, 0, null);
            }
        }
//        synchronized (cursor) {
//            Image cursorImage = cursor.getImage();
//            if (showCursor && cursorImage != null
//                    && (scaleFactor != 1
//                    || g.getClipBounds().intersects(cursor.rX, cursor.rY, cursor.width, cursor.height))) {
//                g.drawImage(cursorImage, cursor.rX, cursor.rY, null);
//            }
//        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension((int) (this.width * scaleFactorx), (int) (this.height * scaleFactory));
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    /**
     * Saves context and simply invokes native JPanel repaint method which
     * asyncroniously register repaint request using invokeLater to repaint be
     * runned in Swing event dispatcher thread. So may be called from other
     * threads.
     */
    @Override
    public void repaintBitmap(FramebufferUpdateRectangle rect) {
        repaintBitmap(rect.x, rect.y, rect.width, rect.height);
    }

    @Override
    public void repaintBitmap(int x, int y, int width, int height) {
        repaint((int) (x * scaleFactorx), (int) (y * scaleFactory),
                (int) Math.ceil(width * scaleFactorx), (int) Math.ceil(height * scaleFactory));
    }

    @Override
    public void repaintCursor() {
        synchronized (cursor) {
            repaint((int) (cursor.oldRX * scaleFactorx), (int) (cursor.oldRY * scaleFactory),
                    (int) Math.ceil(cursor.oldWidth * scaleFactorx) + 1, (int) Math.ceil(cursor.oldHeight * scaleFactory) + 1);
            repaint((int) (cursor.rX * scaleFactorx), (int) (cursor.rY * scaleFactory),
                    (int) Math.ceil(cursor.width * scaleFactorx) + 1, (int) Math.ceil(cursor.height * scaleFactory) + 1);
        }
    }

    @Override
    public void updateCursorPosition(short x, short y) {
        synchronized (cursor) {
            cursor.updatePosition(x, y);
            repaintCursor();
        }
    }

    private void showCursor(boolean show) {
        synchronized (cursor) {
            showCursor = show;
        }
    }

    public void addModifierListener(ModifierButtonEventListener modifierButtonListener) {
        this.modifierButtonListener = modifierButtonListener;
        if (keyEventListener != null) {
            keyEventListener.addModifierListener(modifierButtonListener);
        }
    }

    @Override
    public void settingsChanged(SettingsChangedEvent e) {
        if (ProtocolSettings.isRfbSettingsChangedFired(e)) {
            ProtocolSettings settings = (ProtocolSettings) e.getSource();
            setUserInputEnabled(!settings.isViewOnly(), settings.isConvertToAscii());
            showCursor(settings.isShowRemoteCursor());
        } else if (UiSettings.isUiSettingsChangedFired(e)) {
            UiSettings settings = (UiSettings) e.getSource();
            oldSize = getPreferredSize();
        }
        mouseEventListener.setScaleFactor(scaleFactorx,scaleFactory);
        updateFrameSize();
    }

    @Override
    public void setPixelFormat(PixelFormat pixelFormat) {
        if (renderer != null) {
            renderer.initPixelFormat(pixelFormat);
        }
    }
}
