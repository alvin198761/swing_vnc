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
package com.glavsoft.rfb.protocol;

import com.glavsoft.core.SettingsChangedEvent;
import com.glavsoft.rfb.CapabilityContainer;
import com.glavsoft.rfb.IChangeSettingsListener;
import com.glavsoft.rfb.RfbCapabilityInfo;
import com.glavsoft.rfb.encoding.EncodingType;
import com.glavsoft.rfb.protocol.auth.SecurityType;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Protocol Settings class
 */
public class ProtocolSettings implements Cloneable {

    private static final EncodingType DEFAULT_PREFERRED_ENCODING = EncodingType.TIGHT;
    public static final int DEFAULT_JPEG_QUALITY = 6;
    private static final int DEFAULT_COMPRESSION_LEVEL = -6;
    // bits per pixel constants
    public static final int BPP_32 = 32;
    public static final int BPP_16 = 16;
    public static final int BPP_8 = 8;
    public static final int BPP_6 = 6;
    public static final int BPP_3 = 3;
    public static final int BPP_SERVER_SETTINGS = 0;
    private static final int DEFAULT_BITS_PER_PIXEL = BPP_32;
    public static final int CHANGED_VIEW_ONLY = 1 << 0;
    public static final int CHANGED_ENCODINGS = 1 << 1;
    public static final int CHANGED_ALLOW_COPY_RECT = 1 << 2;
    public static final int CHANGED_SHOW_REMOTE_CURSOR = 1 << 3;
    public static final int CHANGED_MOUSE_CURSOR_TRACK = 1 << 4;
    public static final int CHANGED_COMPRESSION_LEVEL = 1 << 5;
    public static final int CHANGED_JPEG_QUALITY = 1 << 6;
    public static final int CHANGED_ALLOW_CLIPBOARD_TRANSFER = 1 << 7;
    public static final int CHANGED_CONVERT_TO_ASCII = 1 << 8;
    public static final int CHANGED_BITS_PER_PIXEL = 1 << 9;
    private int changedSettingsMask;
    private boolean sharedFlag;
    private boolean viewOnly;
    private EncodingType preferredEncoding;
    private boolean allowCopyRect;
    private boolean showRemoteCursor;
    private LocalPointer mouseCursorTrack;
    private int compressionLevel;
    private int jpegQuality;
    private boolean allowClipboardTransfer;
    private boolean convertToAscii;
    private int bitsPerPixel;
    private String protocolVersion;
    private boolean isTight;
    public LinkedHashSet<EncodingType> encodings;
    private final List<IChangeSettingsListener> listeners;
    public CapabilityContainer tunnelingCapabilities,
            authCapabilities,
            serverMessagesCapabilities,
            clientMessagesCapabilities,
            encodingTypesCapabilities;
    private String remoteCharsetName;

    public static ProtocolSettings getDefaultSettings() {
        ProtocolSettings settings = new ProtocolSettings();
        settings.initKnownAuthCapabilities(settings.authCapabilities);
        settings.initKnownEncodingTypesCapabilities(settings.encodingTypesCapabilities);

        settings.sharedFlag = true;
        settings.viewOnly = false;
        settings.showRemoteCursor = true;
        settings.mouseCursorTrack = LocalPointer.ON;
        settings.preferredEncoding = DEFAULT_PREFERRED_ENCODING;
        settings.allowCopyRect = true;
        settings.compressionLevel = DEFAULT_COMPRESSION_LEVEL;
        settings.jpegQuality = DEFAULT_JPEG_QUALITY;
        settings.convertToAscii = false;
        settings.allowClipboardTransfer = true;
        settings.bitsPerPixel = 0;//DEFAULT_BITS_PER_PIXEL;
        settings.refine();
        settings.changedSettingsMask = 0;
        return settings;
    }

    private ProtocolSettings() {
        listeners = new LinkedList<IChangeSettingsListener>();
        tunnelingCapabilities = new CapabilityContainer();
        authCapabilities = new CapabilityContainer();
        serverMessagesCapabilities = new CapabilityContainer();
        clientMessagesCapabilities = new CapabilityContainer();
        encodingTypesCapabilities = new CapabilityContainer();
        changedSettingsMask = 0;
    }

    private ProtocolSettings(ProtocolSettings s) {
        this();

        changedSettingsMask = s.changedSettingsMask;

        sharedFlag = s.sharedFlag;
        viewOnly = s.viewOnly;
        preferredEncoding = s.preferredEncoding;
        allowCopyRect = s.allowCopyRect;
        showRemoteCursor = s.showRemoteCursor;
        mouseCursorTrack = s.mouseCursorTrack;
        compressionLevel = s.compressionLevel;
        jpegQuality = s.jpegQuality;
        allowClipboardTransfer = s.allowClipboardTransfer;
        convertToAscii = s.convertToAscii;
        bitsPerPixel = s.bitsPerPixel;

        encodings = s.encodings;

        protocolVersion = s.protocolVersion;
        isTight = s.isTight;
    }

    private void initKnownAuthCapabilities(CapabilityContainer cc) {
        cc.addEnabled(SecurityType.NONE_AUTHENTICATION.getId(),
                RfbCapabilityInfo.VENDOR_STANDARD, RfbCapabilityInfo.AUTHENTICATION_NO_AUTH);
        cc.addEnabled(SecurityType.VNC_AUTHENTICATION.getId(),
                RfbCapabilityInfo.VENDOR_STANDARD, RfbCapabilityInfo.AUTHENTICATION_VNC_AUTH);
        //cc.addEnabled( 19, "VENC", "VENCRYPT");
        //cc.addEnabled( 20, "GTKV", "SASL____");
        //cc.addEnabled(129, RfbCapabilityInfo.TIGHT_VNC_VENDOR, "ULGNAUTH");
        //cc.addEnabled(130, RfbCapabilityInfo.TIGHT_VNC_VENDOR, "XTRNAUTH");
    }

    private void initKnownEncodingTypesCapabilities(CapabilityContainer cc) {
        cc.add(EncodingType.COPY_RECT.getId(),
                RfbCapabilityInfo.VENDOR_STANDARD, RfbCapabilityInfo.ENCODING_COPYRECT);
        cc.add(EncodingType.HEXTILE.getId(),
                RfbCapabilityInfo.VENDOR_STANDARD, RfbCapabilityInfo.ENCODING_HEXTILE);
        cc.add(EncodingType.ZLIB.getId(),
                RfbCapabilityInfo.VENDOR_TRIADA, RfbCapabilityInfo.ENCODING_ZLIB);
        cc.add(EncodingType.ZRLE.getId(),
                RfbCapabilityInfo.VENDOR_TRIADA, RfbCapabilityInfo.ENCODING_ZRLE);
        cc.add(EncodingType.RRE.getId(),
                RfbCapabilityInfo.VENDOR_STANDARD, RfbCapabilityInfo.ENCODING_RRE);
        cc.add(EncodingType.TIGHT.getId(),
                RfbCapabilityInfo.VENDOR_TIGHT, RfbCapabilityInfo.ENCODING_TIGHT);

        cc.add(EncodingType.RICH_CURSOR.getId(),
                RfbCapabilityInfo.VENDOR_TIGHT, RfbCapabilityInfo.ENCODING_RICH_CURSOR);
        cc.add(EncodingType.CURSOR_POS.getId(),
                RfbCapabilityInfo.VENDOR_TIGHT, RfbCapabilityInfo.ENCODING_CURSOR_POS);
        cc.add(EncodingType.DESKTOP_SIZE.getId(),
                RfbCapabilityInfo.VENDOR_TIGHT, RfbCapabilityInfo.ENCODING_DESKTOP_SIZE);
    }

    public void addListener(IChangeSettingsListener listener) {
        listeners.add(listener);
    }

    public byte getSharedFlag() {
        return (byte) (sharedFlag ? 1 : 0);
    }

    public boolean isShared() {
        return sharedFlag;
    }

    public void setSharedFlag(boolean sharedFlag) {
        this.sharedFlag = sharedFlag;
    }

    public boolean isViewOnly() {
        return viewOnly;
    }

    public void setViewOnly(boolean viewOnly) {
        if (this.viewOnly != viewOnly) {
            this.viewOnly = viewOnly;
            changedSettingsMask |= CHANGED_VIEW_ONLY;
        }
    }

    public void enableAllEncodingCaps() {
        encodingTypesCapabilities.setAllEnable(true);

    }

    public int getBitsPerPixel() {
        return bitsPerPixel;
    }

    /**
     * Set bpp only in 3, 6, 8, 16, 32. When bpp is wrong, it resets to {@link #DEFAULT_BITS_PER_PIXEL}
     */
    public void setBitsPerPixel(int bpp) {
        if (bitsPerPixel != bpp) {
            changedSettingsMask |= CHANGED_BITS_PER_PIXEL;
            switch (bpp) {
                case BPP_32:
                case BPP_16:
                case BPP_8:
                case BPP_6:
                case BPP_3:
                case BPP_SERVER_SETTINGS:
                    bitsPerPixel = bpp;
                    break;
                default:
                    bitsPerPixel = DEFAULT_BITS_PER_PIXEL;
            }
            refine();
        }
    }

    private void refine() {
        LinkedHashSet<EncodingType> encodings = new LinkedHashSet<EncodingType>();
        if (EncodingType.RAW_ENCODING == preferredEncoding) {
            // when RAW selected send no ordinary encodings so only default RAW encoding will be enabled
        } else {
            encodings.add(preferredEncoding); // preferred first
            encodings.addAll(EncodingType.ordinaryEncodings);
            if (compressionLevel > 0 && compressionLevel < 10) {
                encodings.add(EncodingType.byId(
                        EncodingType.COMPRESS_LEVEL_0.getId() + compressionLevel));
            }
            if (jpegQuality > 0 && jpegQuality < 10
                    && (bitsPerPixel == BPP_32 || bitsPerPixel == BPP_SERVER_SETTINGS)) {
                encodings.add(EncodingType.byId(
                        EncodingType.JPEG_QUALITY_LEVEL_0.getId() + jpegQuality));
            }
            if (allowCopyRect) {
                encodings.add(EncodingType.COPY_RECT);
            }
        }
        switch (mouseCursorTrack) {
            case OFF:
                setShowRemoteCursor(false);
                break;
            case HIDE:
                setShowRemoteCursor(false);
                encodings.add(EncodingType.RICH_CURSOR);
                encodings.add(EncodingType.CURSOR_POS);
                break;
            case ON:
            default:
                setShowRemoteCursor(true);
                encodings.add(EncodingType.RICH_CURSOR);
                encodings.add(EncodingType.CURSOR_POS);
        }
        encodings.add(EncodingType.DESKTOP_SIZE);
        if (isEncodingsChanged(this.encodings, encodings) || isChangedEncodings()) {
            this.encodings = encodings;
            changedSettingsMask |= CHANGED_ENCODINGS;
        }
    }

    private boolean isEncodingsChanged(LinkedHashSet<EncodingType> encodings1, LinkedHashSet<EncodingType> encodings2) {
        if (null == encodings1 || encodings1.size() != encodings2.size()) {
            return true;
        }
        Iterator<EncodingType> it1 = encodings1.iterator();
        Iterator<EncodingType> it2 = encodings2.iterator();
        while (it1.hasNext()) {
            EncodingType v1 = it1.next();
            EncodingType v2 = it2.next();
            if (v1 != v2) {
                return true;
            }
        }
        return false;
    }

    public void fireListeners() {
        final SettingsChangedEvent event = new SettingsChangedEvent(new ProtocolSettings(this));
        changedSettingsMask = 0;
        for (IChangeSettingsListener listener : listeners) {
            listener.settingsChanged(event);
        }
    }

    public static boolean isRfbSettingsChangedFired(SettingsChangedEvent event) {
        return event.getSource() instanceof ProtocolSettings;
    }

    public void setPreferredEncoding(EncodingType preferredEncoding) {
        if (this.preferredEncoding != preferredEncoding) {
            this.preferredEncoding = preferredEncoding;
            changedSettingsMask |= CHANGED_ENCODINGS;
            refine();
        }
    }

    public EncodingType getPreferredEncoding() {
        return preferredEncoding;
    }

    public void setAllowCopyRect(boolean allowCopyRect) {
        if (this.allowCopyRect != allowCopyRect) {
            this.allowCopyRect = allowCopyRect;
            changedSettingsMask |= CHANGED_ALLOW_COPY_RECT;
            refine();
        }
    }

    public boolean isAllowCopyRect() {
        return allowCopyRect;
    }

    private void setShowRemoteCursor(boolean showRemoteCursor) {
        if (this.showRemoteCursor != showRemoteCursor) {
            this.showRemoteCursor = showRemoteCursor;
            changedSettingsMask |= CHANGED_SHOW_REMOTE_CURSOR;
        }
    }

    public boolean isShowRemoteCursor() {
        return showRemoteCursor;
    }

    public void setMouseCursorTrack(LocalPointer mouseCursorTrack) {
        if (this.mouseCursorTrack != mouseCursorTrack) {
            this.mouseCursorTrack = mouseCursorTrack;
            changedSettingsMask |= CHANGED_MOUSE_CURSOR_TRACK;
            refine();
        }
    }

    public LocalPointer getMouseCursorTrack() {
        return mouseCursorTrack;
    }

    public void setCompressionLevel(int compressionLevel) {
        if (this.compressionLevel != compressionLevel) {
            this.compressionLevel = compressionLevel;
            changedSettingsMask |= CHANGED_COMPRESSION_LEVEL;
            refine();
        }
    }

    public int getCompressionLevel() {
        return compressionLevel;
    }

    public void setJpegQuality(int jpegQuality) {
        if (this.jpegQuality != jpegQuality) {
            this.jpegQuality = jpegQuality;
            changedSettingsMask |= CHANGED_JPEG_QUALITY;
            refine();
        }
    }

    public int getJpegQuality() {
        return jpegQuality;
    }

    public void setAllowClipboardTransfer(boolean enable) {
        if (this.allowClipboardTransfer != enable) {
            this.allowClipboardTransfer = enable;
            changedSettingsMask |= CHANGED_ALLOW_CLIPBOARD_TRANSFER;
        }
    }

    public boolean isAllowClipboardTransfer() {
        return allowClipboardTransfer;
    }

    public void setTight(boolean isTight) {
        this.isTight = isTight;
    }

    public boolean isTight() {
        return isTight;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public boolean isConvertToAscii() {
        return convertToAscii;
    }

    public void setConvertToAscii(boolean convertToAscii) {
        if (this.convertToAscii != convertToAscii) {
            this.convertToAscii = convertToAscii;
            changedSettingsMask |= CHANGED_CONVERT_TO_ASCII;
        }
    }

    public boolean isChangedEncodings() {
        return (changedSettingsMask & CHANGED_ENCODINGS) == CHANGED_ENCODINGS;
    }

    public boolean changedBitsPerPixel() {
        return (changedSettingsMask & CHANGED_BITS_PER_PIXEL) == CHANGED_BITS_PER_PIXEL;
    }

    public void setRemoteCharsetName(String remoteCharsetName) {
        this.remoteCharsetName = remoteCharsetName;
    }

    public String getRemoteCharsetName() {
        return remoteCharsetName;
    }
}
