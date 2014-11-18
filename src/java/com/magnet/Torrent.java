package com.magnet;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Map.Entry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.List;
import java.util.Vector;

public class Torrent {
    private File fitxategia;
    private String izena;
    private List<String> trackerrak;
    private Map<String,Object> info;
    private Map<String,Object> metaInfo;
    private String magnetLotura;

    public Torrent(File fitx) {
        fitxategia = fitx;
        trackerrak = new Vector<String>();
        magnetLotura = null;
        trackerraGehitu("udp://tracker.publicbt.com:80");
    }

    public void trackerraGehitu(String tracker) {
        this.trackerrak.add(tracker);
    }

    public void sortu() throws IOException {
        createTorrent(fitxategia, trackerrak.get(0));
    }

    public void gorde(File file) throws IOException {
        OutputStream out = new FileOutputStream(file);
        encodeMap(metaInfo, out);
        out.close();
    }

    public String lortuMagnetLotura() throws IOException{
        if (magnetLotura != null) {
            return magnetLotura;
        }

        String infoHash = "";
        OutputStream infoOut = new ByteArrayOutputStream();
        encodeMap(info, infoOut);

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
            byte[] content = ((ByteArrayOutputStream) infoOut).toByteArray();
            byte[] output = md.digest(content);
            infoHash = byteArray2Hex(output);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        infoOut.close();
        magnetLotura = "magnet:?xt=urn:btih:" + infoHash + "&dn=" + izena;
        for (String t : trackerrak) {
            magnetLotura += "&tr=" + t;
        }
        return magnetLotura;
    }

    private static void encodeObject(Object o, OutputStream out) throws IOException {
        if (o instanceof String)
            encodeString((String)o, out);
        else if (o instanceof Map)
            encodeMap((Map)o, out);
        else if (o instanceof byte[])
            encodeBytes((byte[])o, out);
        else if (o instanceof Number)
            encodeLong(((Number) o).longValue(), out);
        else
            throw new Error("Unencodable type");
    }
    private static void encodeLong(long value, OutputStream out) throws IOException {
        out.write('i');
        out.write(Long.toString(value).getBytes("US-ASCII"));
        out.write('e');
    }
    private static void encodeBytes(byte[] bytes, OutputStream out) throws IOException {
        out.write(Integer.toString(bytes.length).getBytes("US-ASCII"));
        out.write(':');
        out.write(bytes);
    }
    private static void encodeString(String str, OutputStream out) throws IOException {
        encodeBytes(str.getBytes("UTF-8"), out);
    }
    private static void encodeMap(Map<String,Object> map, OutputStream out) throws IOException{
        // Sort the map. A generic encoder should sort by key bytes
        SortedMap<String,Object> sortedMap = new TreeMap<String, Object>(map);
        out.write('d');
        for (Entry<String, Object> e : sortedMap.entrySet()) {
            encodeString(e.getKey(), out);
            encodeObject(e.getValue(), out);
        }
        out.write('e');
    }
    private static byte[] hashPieces(File file, int pieceLength) throws IOException {
        MessageDigest sha1;
        try {
            sha1 = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            throw new Error("SHA1 not supported");
        }
        InputStream in = new FileInputStream(file);
        ByteArrayOutputStream pieces = new ByteArrayOutputStream();
        byte[] bytes = new byte[pieceLength];
        int pieceByteCount  = 0, readCount = in.read(bytes, 0, pieceLength);
        while (readCount != -1) {
            pieceByteCount += readCount;
            sha1.update(bytes, 0, readCount);
            if (pieceByteCount == pieceLength) {
                pieceByteCount = 0;
                pieces.write(sha1.digest());
            }
            readCount = in.read(bytes, 0, pieceLength-pieceByteCount);
        }
        in.close();
        if (pieceByteCount > 0)
            pieces.write(sha1.digest());
        return pieces.toByteArray();
    }

    public void createTorrent(File sharedFile, String announceURL) throws IOException {
        final int pieceLength = 512*1024;
        izena = sharedFile.getName();
        info = new HashMap<String,Object>();
        info.put("name", sharedFile.getName());
        info.put("length", sharedFile.length());
        info.put("piece length", pieceLength);
        info.put("pieces", hashPieces(sharedFile, pieceLength));
        metaInfo = new HashMap<String,Object>();
        metaInfo.put("announce", announceURL);
        metaInfo.put("info", info);
    }

    private String byteArray2Hex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
