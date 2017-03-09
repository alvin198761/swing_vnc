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
package com.glavsoft.viewer;

import com.glavsoft.rfb.protocol.ProtocolSettings;
import com.glavsoft.viewer.swing.ParametersHandler;
import com.glavsoft.viewer.swing.Utils;
import com.glavsoft.viewer.swing.gui.ConnectionDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;

public class ConnectionManager implements Serializable {

    private final WindowListener appWindowListener;
    volatile boolean forceConnectionDialog;
    private JFrame containerFrame;
    private final boolean isApplet;

    public ConnectionManager(WindowListener appWindowListener, boolean isApplet) {
        this.appWindowListener = appWindowListener;
        this.isApplet = isApplet;
    }

    protected void showReconnectDialog(String title, String message) {
        JOptionPane reconnectPane = new JOptionPane(message + "\nTry another connection?",
                JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
        final JDialog reconnectDialog = reconnectPane.createDialog(containerFrame, title);
        reconnectDialog.setModalityType(isApplet
                ? Dialog.ModalityType.APPLICATION_MODAL : Dialog.ModalityType.TOOLKIT_MODAL);
        try {
            reconnectDialog.setAlwaysOnTop(true);
        } catch (SecurityException e) { /*
             * nop
             */ }
        java.util.List<Image> icons = Utils.getIcons();
        if (icons.size() != 0) {
            reconnectDialog.setIconImages(icons);
        }
        reconnectDialog.setVisible(true);
        if (reconnectPane.getValue() == null
                || (Integer) reconnectPane.getValue() == JOptionPane.NO_OPTION) {
            appWindowListener.windowClosing(null);
        } else {
            forceConnectionDialog = !isApplet;
        }
    }

    Socket connectToHost(final ParametersHandler.ConnectionParams connectionParams, ProtocolSettings settings) {
        Socket socket = null;
        ConnectionDialog connectionDialog = null;
        boolean wasError = false;
        do {
            if (forceConnectionDialog || wasError
                    || connectionParams.isHostNameEmpty()
                    || -1 == connectionParams.portNumber) {
                forceConnectionDialog = false;
                if (null == connectionDialog) {
                    connectionDialog = new ConnectionDialog(containerFrame,
                            appWindowListener, connectionParams.hostName, connectionParams.portNumber,
                            settings, isApplet);
                }
                connectionDialog.setVisible(true);
                connectionParams.hostName = connectionDialog.getServerNameString();
                connectionParams.portNumber = connectionDialog.getPort();
            }
            Viewer.logger.info("Connecting to host " + connectionParams.hostName + ":" + connectionParams.portNumber);
            try {
                socket = new Socket(connectionParams.hostName, connectionParams.portNumber);
                wasError = false;
            } catch (UnknownHostException e) {
                Viewer.logger.severe("Unknown host: " + connectionParams.hostName);
                showConnectionErrorDialog("Unknown host: '" + connectionParams.hostName + "'");
                wasError = true;
            } catch (IOException e) {
                Viewer.logger.severe("Couldn't connect to: "
                        + connectionParams.hostName + ":" + connectionParams.portNumber
                        + ": " + e.getMessage());
                showConnectionErrorDialog("Couldn't connect to: '" + connectionParams.hostName
                        + "'\n" + e.getMessage());
                wasError = true;
            }
        } while (!isApplet && (connectionParams.isHostNameEmpty() || wasError));
        if (connectionDialog != null) {
            connectionDialog.dispose();
        }
        return socket;
    }

    public void showConnectionErrorDialog(final String message) {
        JOptionPane errorPane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE);
        final JDialog errorDialog = errorPane.createDialog(containerFrame, "Connection error");
        errorDialog.setModalityType(isApplet ? Dialog.ModalityType.APPLICATION_MODAL : Dialog.ModalityType.TOOLKIT_MODAL);
        try {
            errorDialog.setAlwaysOnTop(true);
        } catch (SecurityException e) { /*
             * nop
             */ }
        errorDialog.setVisible(true);
    }

    public void setContainerFrame(JFrame containerFrame) {
        this.containerFrame = containerFrame;
    }
}