/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.alvin.vncdemo;

import com.glavsoft.exceptions.*;
import com.glavsoft.rfb.IPasswordRetriever;
import com.glavsoft.rfb.IRfbSessionListener;
import com.glavsoft.rfb.client.KeyEventMessage;
import com.glavsoft.rfb.protocol.Protocol;
import com.glavsoft.rfb.protocol.ProtocolSettings;
import com.glavsoft.transport.Reader;
import com.glavsoft.transport.Writer;
import com.glavsoft.utils.Keymap;
import com.glavsoft.viewer.swing.ClipboardControllerImpl;
import com.glavsoft.viewer.swing.ParametersHandler;
import com.glavsoft.viewer.swing.UiSettings;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author Administrator
 */
public class VNCViewer {

    private String password;
    private String hostName = "10.20.65.26";
    private int port = 5900;
    private Protocol workingProtocol;
    private Socket workingSocket;
    private Surface surface;

    public VNCViewer() throws IOException, UnsupportedProtocolVersionException, UnsupportedSecurityTypeException, AuthenticationFailedException, TransportException, FatalException {
        this("10.20.65.26", null, 5900, null);
    }

    public void close() throws IOException {
        if (workingProtocol != null) {
            workingProtocol.cleanUpSession();
        }
        if (workingSocket != null) {
            workingSocket.close();
        }
    }

    public void sendCtrlAltDel() {
        workingProtocol.sendMessage(new KeyEventMessage(Keymap.K_CTRL_LEFT, true));
        workingProtocol.sendMessage(new KeyEventMessage(Keymap.K_ALT_LEFT, true));
        workingProtocol.sendMessage(new KeyEventMessage(Keymap.K_DELETE, true));
        workingProtocol.sendMessage(new KeyEventMessage(Keymap.K_DELETE, false));
        workingProtocol.sendMessage(new KeyEventMessage(Keymap.K_ALT_LEFT, false));
        workingProtocol.sendMessage(new KeyEventMessage(Keymap.K_CTRL_LEFT, false));
        if (surface != null && !surface.requestFocusInWindow()) {
            surface.requestFocus();
        }
    }

    public VNCViewer(String hostName, final String password, int port, IRfbSessionListener listener) throws IOException, UnsupportedProtocolVersionException, UnsupportedSecurityTypeException, AuthenticationFailedException, TransportException, FatalException {
//        ParametersHandler.ConnectionParams connectionParams = new ParametersHandler.ConnectionParams();  
        ProtocolSettings settings = ProtocolSettings.getDefaultSettings();
        UiSettings uiSettings = new UiSettings();
        workingSocket = new Socket(hostName, port);
        workingSocket.setTcpNoDelay(true); // disable Nagle algorithm
        Reader reader = new Reader(workingSocket.getInputStream());
        Writer writer = new Writer(workingSocket.getOutputStream());
        workingProtocol = new Protocol(reader, writer, new IPasswordRetriever() {
            @Override
            public String getPassword() {
                return password;
            }
        }, settings);
        workingProtocol.handshake();

        ClipboardControllerImpl clipboardController = new ClipboardControllerImpl(workingProtocol, settings.getRemoteCharsetName());
        clipboardController.setEnabled(settings.isAllowClipboardTransfer());
        settings.addListener(clipboardController);
        surface = new Surface(workingProtocol);
        uiSettings.addListener(surface);
        if (listener == null) {
            listener = new IRfbSessionListener() {
                @Override
                public void rfbSessionStopped(String reason) {
                    System.out.println("rfbSessionStopped");
                }
            };
        }
        workingProtocol.startNormalHandling(listener, surface, clipboardController);
    }

    /**
     * @return the surface
     */
    public Surface getSurface() {
        return surface;
    }
}