package main.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class SerializationUtil {
    public static byte[] serializeStringGzip(String batch) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream gzos = null;

        try {
            gzos = new DeflaterOutputStream(baos, new Deflater(Deflater.BEST_COMPRESSION, true));
            gzos.write(batch.getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (gzos != null) try { gzos.close(); } catch (IOException ioException) {

            } ;
        }

        return baos.toByteArray();
    }
}
