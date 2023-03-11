package com.camera.finder.util;


import android.net.DnsResolver;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import javax.jmdns.JmDNS;
import javax.jmdns.impl.DNSOutgoing;
import javax.jmdns.impl.JmDNSImpl;
import javax.jmdns.impl.constants.DNSConstants;


/**
 * Created by 郭攀峰 on 2015/10/23.
 */
public abstract class UdpCommunicate
{
    private static final String tag = UdpCommunicate.class.getSimpleName();

    private byte[] mBuffer = new byte[1024];
    private byte[] mBytes;

    private DatagramSocket mUdpSocket;

    public abstract String getPeerIp();

    public abstract int getPort();

    public abstract byte[] getSendContent();

    protected UdpCommunicate() throws SocketException
    {
        mUdpSocket = new DatagramSocket();
        mUdpSocket.setSoTimeout(14500);
    }

    public static void f(final ByteArrayOutputStream byteArrayOutputStream, final int n) {
        byteArrayOutputStream.write(n >> 8 & 0xFF);
        byteArrayOutputStream.write(n & 0xFF);
    }
    public static void e(final ByteArrayOutputStream byteArrayOutputStream, String s) {
        while (true) {
            int n;
            if ((n = s.indexOf(46)) < 0) {
                n = s.length();
            }
            if (n <= 0) {
                break;
            }
            final String substring = s.substring(0, n);
            final int length = substring.length();
            int i = 0;
            int n2 = 0;
            while (i < length) {
                final char char1 = substring.charAt(0 + i);
                if (char1 >= '\u0001' && char1 <= '\u007f') {
                    ++n2;
                }
                else if (char1 > '\u07ff') {
                    n2 += 3;
                }
                else {
                    n2 += 2;
                }
                ++i;
            }
            byteArrayOutputStream.write(n2 & 0xFF);
            for (int j = 0; j < length; ++j) {
                final char char2 = substring.charAt(0 + j);
                if (char2 >= '\u0001' && char2 <= '\u007f') {
                    byteArrayOutputStream.write(char2 & '\u00ff');
                }
                else if (char2 > '\u07ff') {
                    byteArrayOutputStream.write(((char2 >> 12 & 0xF) | 0xE0) & 0xFF);
                    byteArrayOutputStream.write(((char2 >> 6 & 0x3F) | 0x80) & 0xFF);
                    byteArrayOutputStream.write(((char2 >> 0 & 0x3F) | 0x80) & 0xFF);
                }
                else {
                    byteArrayOutputStream.write(((char2 >> 6 & 0x1F) | 0xC0) & 0xFF);
                    byteArrayOutputStream.write(((char2 >> 0 & 0x3F) | 0x80) & 0xFF);
                }
            }
            final String s2 = s = s.substring(n);
            if (!s2.startsWith(".")) {
                continue;
            }
            s = s2.substring(1);
        }
        byteArrayOutputStream.write(0);
    }
    public static byte[] c(final int n, final String s) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        f(byteArrayOutputStream, n);
        f(byteArrayOutputStream, 256);
        f(byteArrayOutputStream, 1);
        f(byteArrayOutputStream, 0);
        f(byteArrayOutputStream, 0);
        f(byteArrayOutputStream, 0);
        e(byteArrayOutputStream, s);
        return byteArrayOutputStream.toByteArray();
    }

    protected void send() throws IOException
    {

//        mBytes = c(1,"140.2.168.192.in-addr.arpa.");
        mBytes = getSendContent();
        DatagramPacket dp = new DatagramPacket(mBytes, mBytes.length,
            InetAddress.getByName(getPeerIp()), getPort());
        mUdpSocket.send(dp);
    }



    protected DatagramPacket receive() throws IOException
    {
        DatagramPacket dp = new DatagramPacket(mBuffer, mBuffer.length);
        mUdpSocket.receive(dp);
        return dp;
    }

    protected void close()
    {
        if (mUdpSocket != null)
        {
            mUdpSocket.close();
        }
    }

}
