package org.jlawyer.test.client.mail;

import com.jdimension.jlawyer.client.mail.EmailUtils;
import com.jdimension.jlawyer.client.mail.MessageContainer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import org.junit.Assert;
import org.junit.Test;

/**
 * An attachment without file name is listed under a generated name (#1694). The client must be able to
 * open / save it by that name - also when the list was built from a copy of the message, as MailContentUI
 * does.
 */
public class NamelessAttachmentTest {

    private static final byte[] PDF = "%PDF-1.4 nameless attachment".getBytes(StandardCharsets.US_ASCII);

    private MimeMessage buildMessageInMemory() throws Exception {
        MimeMultipart mixed = new MimeMultipart();

        MimeBodyPart body = new MimeBodyPart();
        body.setText("siehe Anhang", "UTF-8");
        mixed.addBodyPart(body);

        MimeBodyPart pdf = new MimeBodyPart();
        pdf.setDataHandler(new DataHandler(new ByteArrayDataSource(PDF, "application/pdf")));
        pdf.setDisposition(Part.INLINE);
        mixed.addBodyPart(pdf);

        MimeMessage msg = new MimeMessage(Session.getInstance(new Properties()));
        msg.setSubject("Anhang ohne Dateinamen");
        msg.setContent(mixed);
        msg.saveChanges();
        return msg;
    }

    @Test
    public void namelessAttachmentCanBeOpened() throws Exception {
        MimeMessage original = buildMessageInMemory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        original.writeTo(out);
        MimeMessage copy = new MimeMessage(Session.getInstance(new Properties()), new ByteArrayInputStream(out.toByteArray()));

        // listed from the copy ...
        ArrayList<String> names = EmailUtils.getAttachmentNames(copy.getContent());
        Assert.assertEquals("expected only the PDF, got " + names, 1, names.size());
        String name = names.get(0);
        Assert.assertTrue(name, name.startsWith("Anhang_") && name.endsWith(".pdf"));

        // ... opened / saved from the original
        Assert.assertArrayEquals(PDF, EmailUtils.getAttachmentBytes(name, new MessageContainer(original, "test", true)));
    }

    @Test
    public void fileNameTakesPrecedenceOverGeneratedName() throws Exception {
        MimeMessage msg = buildMessageInMemory();
        String generatedName = EmailUtils.getAttachmentNames(msg.getContent()).get(0);

        // another attachment carries the generated name as its file name, e.g. a saved and edited copy
        byte[] edited = "%PDF-1.4 edited copy".getBytes(StandardCharsets.US_ASCII);
        MimeBodyPart named = new MimeBodyPart();
        named.setDataHandler(new DataHandler(new ByteArrayDataSource(edited, "application/pdf")));
        named.setFileName(generatedName);
        named.setDisposition(Part.ATTACHMENT);
        ((MimeMultipart) msg.getContent()).addBodyPart(named);
        msg.saveChanges();

        Assert.assertArrayEquals(edited, EmailUtils.getAttachmentBytes(generatedName, new MessageContainer(msg, "test", true)));
    }
}
