package com.khanabook.pos.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Component
public class QrCodeGenerator {

    @Value("${qr.code.base-url}")
    private String baseUrl;

    @Value("${qr.code.size}")
    private int size;

    public String generateQrCode(String qrToken) throws Exception {
        String url = baseUrl + "?token=" + qrToken;
        
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, size, size);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        
        byte[] qrCodeBytes = outputStream.toByteArray();
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(qrCodeBytes);
    }
}
