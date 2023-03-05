/*
 * Copyright (C) 2009-2010 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 2, see README
 */

//am start -a android.intent.action.MAIN -n com.android.settings/.wifi.WifiSettings
package com.camera.finder.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: IPv6 support

public class NetInfo {
    private final String TAG = "NetInfo";
    private static final int BUF = 8 * 1024;
    private static final String CMD_IP = " -f inet addr show %s";
    private static final String PTN_IP1 = "\\s*inet [0-9\\.]+\\/([0-9]+) brd [0-9\\.]+ scope global %s$";
    private static final String PTN_IP2 = "\\s*inet [0-9\\.]+ peer [0-9\\.]+\\/([0-9]+) scope global %s$"; // FIXME: Merge with PTN_IP1
    private static final String PTN_IF = "^%s: ip [0-9\\.]+ mask ([0-9\\.]+) flags.*";
    private static final String NOIF = "0";
    public static final String NOIP = "0.0.0.0";
    public static final String NOMASK = "255.255.255.255";
    public static final String NOMAC = "00:00:00:00:00:00";
    private Context ctxt;
    private WifiInfo info;
    private SharedPreferences prefs;

    public String intf = "eth0";
    public String ip = NOIP;
    public int cidr = 24;

    public int speed = 0;
    public String ssid = null;
    public String bssid = null;
    public String carrier = null;
    public String macAddress = NOMAC;
    public String netmaskIp = NOMASK;
    public String gatewayIp = NOIP;

    public NetInfo(final Context ctxt) {
        this.ctxt = ctxt;
        prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
        getIp();
        getWifiInfo();

        // Set ARP enabled
        // try {
        // Runtime.getRuntime().exec("su -C ip link set dev " + intf +
        // " arp on");
        // } catch (Exception e) {
        // Log.e(TAG, e.getMessage());
        // }
        // Runtime.getRuntime().exec("echo 1 > /proc/sys/net/ipv4/conf/" + intf
        // + "/proxy_arp");
        // Runtime.getRuntime().exec("echo 1 > /proc/sys/net/ipv4/conf/tun0/proxy_arp");
    }


    public void getIp() {
        try {

            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                    .hasMoreElements();) {
                NetworkInterface ni = en.nextElement();
                intf = ni.getName();
                ip = getInterfaceFirstIp(ni);
                if (ip != NOIP) {
                    break;
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, e.getMessage());
            //Editor edit = prefs.edit();
            //edit.putString(Prefs.KEY_INTF, Prefs.DEFAULT_INTF);
            //edit.commit();
        }
        getCidr();
    }

    private String getInterfaceFirstIp(NetworkInterface ni) {
        if (ni != null) {
            for (Enumeration<InetAddress> nis = ni.getInetAddresses(); nis.hasMoreElements();) {
                InetAddress ia = nis.nextElement();
                if (!ia.isLoopbackAddress()) {
                    if (ia instanceof Inet6Address) {
                        Log.i(TAG, "IPv6 detected and not supported yet!");
                        continue;
                    }
                    return ia.getHostAddress();
                }
            }
        }
        return NOIP;
    }

    private void getCidr() {
        if (netmaskIp != NOMASK) {
            cidr = IpToCidr(netmaskIp);
        } else {
            String match;
            // Running ip tools
            try {
                if ((match = runCommand("/system/xbin/ip", String.format(CMD_IP, intf), String.format(PTN_IP1, intf))) != null) {
                    cidr = Integer.parseInt(match);
                    return;
                } else if ((match = runCommand("/system/xbin/ip", String.format(CMD_IP, intf), String.format(PTN_IP2, intf))) != null) {
                    cidr = Integer.parseInt(match);
                    return;
                } else if ((match = runCommand("/system/bin/ifconfig", " " + intf, String.format(PTN_IF, intf))) != null) {
                    cidr = IpToCidr(match);
                    return;
                } else {
                    Log.i(TAG, "cannot find cidr, using default /24");
                }
            } catch (NumberFormatException e) {
                Log.i(TAG, e.getMessage()+ " -> cannot find cidr, using default /24");
            }
        }
    }

    // FIXME: Factorize, this isn't a generic runCommand()
    private String runCommand(String path, String cmd, String ptn) {
        try {
            if (new File(path).exists() == true) {
                String line;
                Matcher matcher;
                Pattern ptrn = Pattern.compile(ptn);
                Process p = Runtime.getRuntime().exec(path + cmd);
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()), BUF);
                while ((line = r.readLine()) != null) {
                    matcher = ptrn.matcher(line);
                    if (matcher.matches()) {
                        return matcher.group(1);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Can't use native command: " + e.getMessage());
            return null;
        }
        return null;
    }

    public boolean getMobileInfo() {
        TelephonyManager tm = (TelephonyManager) ctxt.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            carrier = tm.getNetworkOperatorName();
        }
        return false;
    }

    public boolean getWifiInfo() {
        WifiManager wifi = (WifiManager) ctxt.getSystemService(Context.WIFI_SERVICE);
        if (wifi != null) {
            info = wifi.getConnectionInfo();
            // Set wifi variables
            speed = info.getLinkSpeed();
            ssid = info.getSSID();
            bssid = info.getBSSID();
            macAddress = info.getMacAddress();
            gatewayIp = getIpFromIntSigned(wifi.getDhcpInfo().gateway);
            // broadcastIp = getIpFromIntSigned((dhcp.ipAddress & dhcp.netmask)
            // | ~dhcp.netmask);
            netmaskIp = getIpFromIntSigned(wifi.getDhcpInfo().netmask);
            return true;
        }
        return false;
    }

    public String getNetIp() {
        int shift = (32 - cidr);
        int start = ((int) getUnsignedLongFromIp(ip) >> shift << shift);
        return getIpFromLongUnsigned((long) start);
    }


    public SupplicantState getSupplicantState() {
        return info.getSupplicantState();
    }



    public static long getUnsignedLongFromIp(String ip_addr) {
        String[] a = ip_addr.split("\\.");
        return (Integer.parseInt(a[0]) * 16777216 + Integer.parseInt(a[1]) * 65536
                + Integer.parseInt(a[2]) * 256 + Integer.parseInt(a[3]));
    }

    public static String getIpFromIntSigned(int ip_int) {
        String ip = "";
        for (int k = 0; k < 4; k++) {
            ip = ip + ((ip_int >> k * 8) & 0xFF) + ".";
        }
        return ip.substring(0, ip.length() - 1);
    }

    public static String getIpFromLongUnsigned(long ip_long) {
        String ip = "";
        for (int k = 3; k > -1; k--) {
            ip = ip + ((ip_long >> k * 8) & 0xFF) + ".";
        }
        return ip.substring(0, ip.length() - 1);
    }

    private int IpToCidr(String ip) {
        double sum = -2;
        String[] part = ip.split("\\.");
        for (String p : part) {
            sum += 256D - Double.parseDouble(p);
        }
        return 32 - (int) (Math.log(sum) / Math.log(2d));
    }

}
