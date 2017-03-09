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

import com.glavsoft.rfb.encoding.EncodingType;
import com.glavsoft.rfb.protocol.LocalPointer;
import com.glavsoft.rfb.protocol.ProtocolSettings;
import com.glavsoft.utils.Strings;
import com.glavsoft.viewer.Viewer;
import com.glavsoft.viewer.cli.Parser;

import javax.swing.*;

public class ParametersHandler {
	public static final String ARG_LOCAL_POINTER = "LocalPointer";
	public static final String ARG_SCALING_FACTOR = "ScalingFactor";
	public static final String ARG_COLOR_DEPTH = "ColorDepth";
	public static final String ARG_JPEG_IMAGE_QUALITY = "JpegImageQuality";
	public static final String ARG_COMPRESSION_LEVEL = "CompressionLevel";
	public static final String ARG_ENCODING = "Encoding";
	public static final String ARG_SHARE_DESKTOP = "ShareDesktop";
	public static final String ARG_ALLOW_COPY_RECT = "AllowCopyRect";
	public static final String ARG_VIEW_ONLY = "ViewOnly";
	public static final String ARG_SHOW_CONTROLS = "ShowControls";
	public static final String ARG_OPEN_NEW_WINDOW = "OpenNewWindow";
	public static final String ARG_PASSWORD = "password";
	public static final String ARG_PORT = "port";
	public static final String ARG_HOST = "host";
	public static final String ARG_HELP = "help";
	public static final String ARG_CONVERT_TO_ASCII = "ConvertToASCII";
	public static final String ARG_ALLOW_CLIPBOARD_TRANSFER = "AllowClipboardTransfer";
	public static final String ARG_REMOTE_CHARSET = "RemoteCharset";

	public static class ConnectionParams {
		public String hostName;
		public int portNumber = Viewer.DEFAULT_PORT;
		public boolean isHostNameEmpty() {
			return Strings.isTrimmedEmpty(hostName);
		}
		void parsePortNumber(String port) {
			try {
				portNumber = Integer.parseInt(port);
			} catch (NumberFormatException e) { /*nop*/ }
		}
	}

	public static boolean showControls;
	public static boolean isSeparateFrame;

	public static void completeParserOptions(Parser parser) {
		parser.addOption(ARG_HELP, null, "Print this help.");
		parser.addOption(ARG_HOST, null, "Server host name.");
		parser.addOption(ARG_PORT, "5900", "Port number.");
		parser.addOption(ARG_PASSWORD, null, "Password to the server.");
		parser.addOption(ARG_SHOW_CONTROLS, null, "Set to \"No\" if you want to get rid of that " +
				"button panel at the top. Default: \"Yes\".");
		parser.addOption(ARG_VIEW_ONLY, null, "When set to \"Yes\", then all keyboard and mouse " +
				"events in the desktop window will be silently ignored and will not be passed " +
				"to the remote side. Default: \"No\".");
		parser.addOption(ARG_ALLOW_CLIPBOARD_TRANSFER, null, "When set to \"Yes\", transfer of clipboard contents is allowed. " +
				"Default: \"Yes\".");
		parser.addOption(ARG_REMOTE_CHARSET, null, "Charset encoding is used on remote system. Use this option to specify character encoding will be used for encoding clipboard text content to. Default value: local system default character encoding. Set the value to 'standard' for using 'Latin-1' charset which is only specified by rfb standard for clipboard transfers.");
		parser.addOption(ARG_SHARE_DESKTOP, null, "Share the connection with other clients " +
				"on the same VNC server. The exact behaviour in each case depends on the server " +
				"configuration. Default: \"Yes\".");
		parser.addOption(ARG_ALLOW_COPY_RECT, null, "The \"CopyRect\" encoding saves bandwidth " +
				"and drawing time when parts of the remote screen are moving around. " +
				"Most likely, you don't want to change this setting. Default: \"Yes\".");
		parser.addOption(ARG_ENCODING, null, "The preferred encoding. Possible values: \"Tight\", " +
				"\"Hextile\", \"ZRLE\", and \"Raw\". Default: \"Tight\".");
		parser.addOption(ARG_COMPRESSION_LEVEL, null, "Use specified compression level for " +
				"\"Tight\" and \"Zlib\" encodings. Values: 1-9. Level 1 uses minimum of CPU " +
				"time on the server but achieves weak compression ratios. Level 9 offers best " +
				"compression but may be slow.");
		parser.addOption(ARG_JPEG_IMAGE_QUALITY, null, "Use the specified image quality level " +
				"in \"Tight\" encoding. Values: 1-9, Lossless. Default value: " +
				(ProtocolSettings.DEFAULT_JPEG_QUALITY > 0 ?
						String.valueOf(ProtocolSettings.DEFAULT_JPEG_QUALITY) :
						"\"Lossless\"")
				+ ". To prevent server of using " +
				"lossy JPEG compression in \"Tight\" encoding, use \"Lossless\" value here.");
		parser.addOption(ARG_LOCAL_POINTER, null, "Possible values: on/yes/true (draw pointer locally), off/no/false (let server draw pointer), hide). " +
		"Default: \"On\".");
		parser.addOption(ARG_CONVERT_TO_ASCII, null, "Whether to convert keyboard input to ASCII ignoring locale. Possible values: yes/true, no/false). " +
		"Default: \"No\".");
		parser.addOption(ARG_COLOR_DEPTH, null, "Bits per pixel color format. Possible values: 3 (for 8 colors), 6 (64 colors), 8 (256 colors), 16 (65 536 colors), 24 (16 777 216 colors), 32 (same as 24).");
		parser.addOption(ARG_SCALING_FACTOR, null, "Scale local representation of the remote desktop on startup. " +
				"The value is interpreted as scaling factor in percents. The default value of 100% " +
				"corresponds to the original framebuffer size.");
	}

	public static void completeSettingsFromCLI(Parser parser, ConnectionParams connectionParams, ProtocolSettings rfbSettings, UiSettings uiSettings) {
		completeSettings(parser.getValueFor(ARG_HOST), parser.getValueFor(ARG_PORT),
				parser.getValueFor(ARG_SHOW_CONTROLS), parser.getValueFor(ARG_VIEW_ONLY),
				parser.getValueFor(ARG_ALLOW_CLIPBOARD_TRANSFER), parser.getValueFor(ARG_REMOTE_CHARSET),
				parser.getValueFor(ARG_ALLOW_COPY_RECT), parser.getValueFor(ARG_SHARE_DESKTOP),
				parser.getValueFor(ARG_ENCODING), parser.getValueFor(ARG_COMPRESSION_LEVEL),
				parser.getValueFor(ARG_JPEG_IMAGE_QUALITY), parser.getValueFor(ARG_COLOR_DEPTH),
				parser.getValueFor(ARG_SCALING_FACTOR), parser.getValueFor(ARG_LOCAL_POINTER),
				parser.getValueFor(ARG_CONVERT_TO_ASCII), connectionParams, rfbSettings, uiSettings);
		// when hostName == a.b.c.d:3 where :3 is display num (X Window) we need add display num to port number
		if ( ! Strings.isTrimmedEmpty(connectionParams.hostName)) {
			splitConnectionParams(connectionParams, connectionParams.hostName);
		}
		if (parser.isSetPlainOptions()) {
			splitConnectionParams(connectionParams, parser.getPlainOptionAt(0));
			if (parser.getPlainOptionsNumber() > 1) {
				connectionParams.parsePortNumber(parser.getPlainOptionAt(1));
			}
		}
	}


	/**
	 * Split host string into hostName + port number and set ConnectionParans.
	 * a.b.c.d:5000 -> hostName == a.b.c.d, portNumber == 5000
	 * a.b.c.d::5000 -> hostName == a.b.c.d, portNumber == 5000
	 */
	public static void splitConnectionParams(final ConnectionParams connectionParams, String host) {
		int displayNum = 0;
		int indexOfColon = host.indexOf(':');
		if (indexOfColon > 0) {
			String[] splitted = host.split(":");
			connectionParams.hostName = splitted[0];
			if (splitted.length > 1) {
				connectionParams.parsePortNumber(splitted[splitted.length - 1]);
			}
		} else {
			connectionParams.hostName = host;
		}
	}

	private static void completeSettings(String hostName, String portNumber,
	                                     String showControlsParam, String viewOnlyParam, String allowClipboardTransfer,
	                                     String remoteCharsetName,
	                                     String allowCopyRectParam,
	                                     String shareDesktopParam, String encodingParam, String compressionLevelParam,
	                                     String jpegQualityParam, String colorDepthParam, String scaleFactorParam,
	                                     String localPointerParam,
	                                     String convertToAsciiParam, ConnectionParams connectionParams, ProtocolSettings rfbSettings, UiSettings uiSettings) {
		connectionParams.hostName = hostName;
		try {
			connectionParams.portNumber = Integer.parseInt(portNumber);
		} catch (NumberFormatException e) { /* nop */ }

		showControls = parseBooleanOrDefault(showControlsParam, true);
		rfbSettings.setViewOnly(parseBooleanOrDefault(viewOnlyParam, false));
		rfbSettings.setAllowClipboardTransfer(parseBooleanOrDefault(allowClipboardTransfer, true));
		rfbSettings.setRemoteCharsetName(remoteCharsetName);
	    rfbSettings.setAllowCopyRect(parseBooleanOrDefault(allowCopyRectParam, true));
		rfbSettings.setSharedFlag(parseBooleanOrDefault(shareDesktopParam, true));
		rfbSettings.setConvertToAscii(parseBooleanOrDefault(convertToAsciiParam, false));
		if (EncodingType.TIGHT.getName().equalsIgnoreCase(encodingParam)) {
			rfbSettings.setPreferredEncoding(EncodingType.TIGHT);
		}
		if (EncodingType.HEXTILE.getName().equalsIgnoreCase(encodingParam)) {
			rfbSettings.setPreferredEncoding(EncodingType.HEXTILE);
		}
		if (EncodingType.ZRLE.getName().equalsIgnoreCase(encodingParam)) {
			rfbSettings.setPreferredEncoding(EncodingType.ZRLE);
		}
		if (EncodingType.RAW_ENCODING.getName().equalsIgnoreCase(encodingParam)) {
			rfbSettings.setPreferredEncoding(EncodingType.RAW_ENCODING);
		}
		try {
			int compLevel = Integer.parseInt(compressionLevelParam);
			if (compLevel > 0 && compLevel <= 9) {
				rfbSettings.setCompressionLevel(compLevel);
			}
		} catch (NumberFormatException e) { /* nop */ }
		try {
			int jpegQuality = Integer.parseInt(jpegQualityParam);
			if (jpegQuality > 0 && jpegQuality <= 9) {
				rfbSettings.setJpegQuality(jpegQuality);
			}
		} catch (NumberFormatException e) {
			if ("lossless".equalsIgnoreCase(jpegQualityParam)) {
				rfbSettings.setJpegQuality( - Math.abs(rfbSettings.getJpegQuality()));
			}
		}
		try {
			int colorDepth = Integer.parseInt(colorDepthParam);
			rfbSettings.setBitsPerPixel(colorDepth);
		} catch (NumberFormatException e) { /* nop */ }
		if (scaleFactorParam != null) {
			try {
				int scaleFactor = Integer.parseInt(scaleFactorParam.replaceAll("\\D", ""));
				if (scaleFactor >= 10 && scaleFactor <= 200) {
					uiSettings.setScalePercent(scaleFactor);
				}
			} catch (NumberFormatException e) { /* nop */ }
		}

		if ("on".equalsIgnoreCase(localPointerParam) ||
			"true".equalsIgnoreCase(localPointerParam) ||
			"yes".equalsIgnoreCase(localPointerParam)) {
				rfbSettings.setMouseCursorTrack(LocalPointer.ON);
		}
		if ("off".equalsIgnoreCase(localPointerParam) ||
			"no".equalsIgnoreCase(localPointerParam) ||
			"false".equalsIgnoreCase(localPointerParam)) {
				rfbSettings.setMouseCursorTrack(LocalPointer.OFF);
		}
		if ("hide".equalsIgnoreCase(localPointerParam) ||
			"hidden".equalsIgnoreCase(localPointerParam)) {
				rfbSettings.setMouseCursorTrack(LocalPointer.HIDE);
		}
	}

	static boolean parseBooleanOrDefault(String param, boolean defaultValue) {
		return defaultValue ?
				! ("no".equalsIgnoreCase(param) || "false".equalsIgnoreCase(param)) :
				"yes".equalsIgnoreCase(param) || "true".equalsIgnoreCase(param);
	}

	public static void completeSettingsFromApplet(JApplet applet,
			ConnectionParams connectionParams, ProtocolSettings rfbSettings, UiSettings uiSettings) {

		String host = applet.getParameter(ARG_HOST);
		if (Strings.isTrimmedEmpty(host)) {
			host = applet.getCodeBase().getHost();
		}
		completeSettings(host,
				applet.getParameter(ARG_PORT),
				applet.getParameter(ARG_SHOW_CONTROLS),
				applet.getParameter(ARG_VIEW_ONLY),
				applet.getParameter(ARG_ALLOW_CLIPBOARD_TRANSFER),
				applet.getParameter(ARG_REMOTE_CHARSET),
				applet.getParameter(ARG_ALLOW_COPY_RECT),
				applet.getParameter(ARG_SHARE_DESKTOP),
				applet.getParameter(ARG_ENCODING),
				applet.getParameter(ARG_COMPRESSION_LEVEL),
				applet.getParameter(ARG_JPEG_IMAGE_QUALITY),
				applet.getParameter(ARG_COLOR_DEPTH),
				applet.getParameter(ARG_SCALING_FACTOR),
				applet.getParameter(ARG_LOCAL_POINTER),
				applet.getParameter(ARG_CONVERT_TO_ASCII),
				connectionParams, rfbSettings, uiSettings);
		isSeparateFrame = parseBooleanOrDefault(applet.getParameter(ARG_OPEN_NEW_WINDOW), true);
	}


}
