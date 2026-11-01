package com.jdimension.jlawyer.test.email;

import com.jdimension.jlawyer.email.AttachmentInfo;
import com.jdimension.jlawyer.email.CommonMailUtils;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Properties;
import java.util.zip.CRC32;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Inline parts and attachments without a file name used to be invisible: they were neither
 * listed nor retrievable by name (#1694). They now get a generated name derived from a checksum
 * of their content and from the content type, while the text of the message body is still not
 * listed.
 */
public class NamelessAttachmentTest {

    private static final byte[] PNG = new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n', 0, 0, 0, 13, 'I', 'H', 'D', 'R', 1, 2, 3, 4};
    private static final byte[] PDF = "%PDF-1.4 inline test".getBytes(StandardCharsets.US_ASCII);

    private MimeMessage buildMessage() throws Exception {
        String eml = "From: sender@example.org\r\n"
                + "To: recipient@example.org\r\n"
                + "Subject: Inline ohne Dateinamen\r\n"
                + "MIME-Version: 1.0\r\n"
                + "Content-Type: multipart/mixed; boundary=\"outer\"\r\n"
                + "\r\n"
                + "--outer\r\n"
                + "Content-Type: multipart/related; boundary=\"inner\"\r\n"
                + "\r\n"
                + "--inner\r\n"
                + "Content-Type: text/html; charset=UTF-8\r\n"
                + "Content-Disposition: inline\r\n"
                + "\r\n"
                + "<html><body><p>Hallo</p><img src=\"cid:logo@example.org\"></body></html>\r\n"
                + "--inner\r\n"
                + "Content-Type: image/png\r\n"
                + "Content-Transfer-Encoding: base64\r\n"
                + "Content-Disposition: inline\r\n"
                + "Content-ID: <logo@example.org>\r\n"
                + "\r\n"
                + Base64.getEncoder().encodeToString(PNG) + "\r\n"
                + "--inner--\r\n"
                + "\r\n"
                + "--outer\r\n"
                + "Content-Type: text/plain; charset=UTF-8\r\n"
                + "Content-Disposition: inline\r\n"
                + "\r\n"
                + "Gruss\r\n"
                + "--outer\r\n"
                + "Content-Type: application/pdf\r\n"
                + "Content-Transfer-Encoding: base64\r\n"
                + "Content-Disposition: attachment\r\n"
                + "\r\n"
                + Base64.getEncoder().encodeToString(PDF) + "\r\n"
                + "--outer\r\n"
                + "Content-Type: text/plain; charset=UTF-8\r\n"
                + "Content-Disposition: attachment; filename=\"notiz.txt\"\r\n"
                + "\r\n"
                + "named attachment\r\n"
                + "--outer--\r\n";
        return new MimeMessage(Session.getInstance(new Properties()), new ByteArrayInputStream(eml.getBytes(StandardCharsets.UTF_8)));
    }

    private AttachmentInfo find(ArrayList<AttachmentInfo> infos, String prefix, String suffix) {
        for (AttachmentInfo i : infos) {
            if (i.getFileName().startsWith(prefix) && i.getFileName().endsWith(suffix)) {
                return i;
            }
        }
        return null;
    }

    @Test
    public void namelessPartsAreListedWithGeneratedNames() throws Exception {
        MimeMessage msg = buildMessage();
        ArrayList<AttachmentInfo> infos = CommonMailUtils.getAttachmentInfo(msg.getContent());
        ArrayList<String> names = new ArrayList<>();
        for (AttachmentInfo i : infos) {
            names.add(i.getFileName());
        }

        assertEquals("expected the inline image, the PDF and the named attachment, got " + names, 3, infos.size());
        assertTrue("named attachment missing, got " + names, names.contains("notiz.txt"));

        AttachmentInfo image = find(infos, "Anhang_", ".png");
        assertNotNull("inline image without name not listed, got " + names, image);
        assertTrue(image.isInline());

        AttachmentInfo pdf = find(infos, "Anhang_", ".pdf");
        assertNotNull("attachment without name not listed, got " + names, pdf);
        assertFalse(pdf.isInline());
    }

    @Test
    public void bodyTextIsNotListed() throws Exception {
        MimeMessage msg = buildMessage();
        for (AttachmentInfo i : CommonMailUtils.getAttachmentInfo(msg.getContent())) {
            assertFalse("body text listed as " + i.getFileName(), i.getFileName().endsWith(".html"));
            assertFalse("body text listed as " + i.getFileName(), i.getFileName().startsWith("Anhang_") && i.getFileName().endsWith(".txt"));
        }
    }

    @Test
    public void namelessPartsCanBeRetrievedByGeneratedName() throws Exception {
        MimeMessage msg = buildMessage();
        ArrayList<AttachmentInfo> infos = CommonMailUtils.getAttachmentInfo(msg.getContent());

        String imageName = find(infos, "Anhang_", ".png").getFileName();
        assertArrayEquals(PNG, CommonMailUtils.getAttachmentBytes(imageName, msg));

        String pdfName = find(infos, "Anhang_", ".pdf").getFileName();
        assertArrayEquals(PDF, CommonMailUtils.getAttachmentBytes(pdfName, msg));

        assertEquals("named attachment", new String(CommonMailUtils.getAttachmentBytes("notiz.txt", msg), StandardCharsets.UTF_8).trim());
    }

    @Test
    public void namelessPartsOfTheSameSizeGetDifferentNames() throws Exception {
        byte[] pdfA = "%PDF-1.4 content A".getBytes(StandardCharsets.US_ASCII);
        byte[] pdfB = "%PDF-1.4 content B".getBytes(StandardCharsets.US_ASCII);
        String eml = "From: sender@example.org\r\n"
                + "Subject: zwei Anhaenge ohne Dateinamen\r\n"
                + "MIME-Version: 1.0\r\n"
                + "Content-Type: multipart/mixed; boundary=\"outer\"\r\n"
                + "\r\n"
                + "--outer\r\n"
                + "Content-Type: application/pdf\r\n"
                + "Content-Transfer-Encoding: base64\r\n"
                + "Content-Disposition: attachment\r\n"
                + "\r\n"
                + Base64.getEncoder().encodeToString(pdfA) + "\r\n"
                + "--outer\r\n"
                + "Content-Type: application/pdf\r\n"
                + "Content-Transfer-Encoding: base64\r\n"
                + "Content-Disposition: attachment\r\n"
                + "\r\n"
                + Base64.getEncoder().encodeToString(pdfB) + "\r\n"
                + "--outer--\r\n";
        MimeMessage msg = new MimeMessage(Session.getInstance(new Properties()), new ByteArrayInputStream(eml.getBytes(StandardCharsets.UTF_8)));

        ArrayList<AttachmentInfo> infos = CommonMailUtils.getAttachmentInfo(msg.getContent());
        assertEquals(2, infos.size());
        String nameA = infos.get(0).getFileName();
        String nameB = infos.get(1).getFileName();
        assertFalse("parts with different content must not share a name: " + nameA, nameA.equals(nameB));
        assertArrayEquals(pdfA, CommonMailUtils.getAttachmentBytes(nameA, msg));
        assertArrayEquals(pdfB, CommonMailUtils.getAttachmentBytes(nameB, msg));
    }

    @Test
    public void fileNameTakesPrecedenceOverGeneratedName() throws Exception {
        // a part whose file name equals the generated name of a nameless part, e.g. a saved and edited copy
        CRC32 crc = new CRC32();
        crc.update(PNG);
        String generatedName = "Anhang_" + String.format("%08x", crc.getValue()) + ".png";
        byte[] edited = "edited image".getBytes(StandardCharsets.US_ASCII);
        String eml = "From: sender@example.org\r\n"
                + "Subject: Namenskollision\r\n"
                + "MIME-Version: 1.0\r\n"
                + "Content-Type: multipart/mixed; boundary=\"outer\"\r\n"
                + "\r\n"
                + "--outer\r\n"
                + "Content-Type: image/png\r\n"
                + "Content-Transfer-Encoding: base64\r\n"
                + "Content-Disposition: inline\r\n"
                + "\r\n"
                + Base64.getEncoder().encodeToString(PNG) + "\r\n"
                + "--outer\r\n"
                + "Content-Type: image/png\r\n"
                + "Content-Transfer-Encoding: base64\r\n"
                + "Content-Disposition: attachment; filename=\"" + generatedName + "\"\r\n"
                + "\r\n"
                + Base64.getEncoder().encodeToString(edited) + "\r\n"
                + "--outer--\r\n";
        MimeMessage msg = new MimeMessage(Session.getInstance(new Properties()), new ByteArrayInputStream(eml.getBytes(StandardCharsets.UTF_8)));

        assertArrayEquals(edited, CommonMailUtils.getAttachmentBytes(generatedName, msg));
    }
}
