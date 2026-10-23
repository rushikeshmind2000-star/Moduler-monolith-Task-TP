package com.tech.emailvalidation.document.service;

import com.tech.emailvalidation.document.dto.CustomerRequest;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentGenerationServiceTest {

    private final DocumentGenerationService service = new DocumentGenerationService();

    @Test
    void testDynamicUpdatesAcrossMultipleHits() throws IOException {
        // Step 1: Create initial template with placeholders (like in Google Docs)
        byte[] templateBytes;
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            addParagraph(doc, "Customer Details");
            addParagraph(doc, "Customer Name: {{customerName}}");
            addParagraph(doc, "Customer Address: {{customerAddress}}");
            addParagraph(doc, "Customer Email: {{customerEmail}}");
            addParagraph(doc, "Customer Phone: {{customerPhone}}");

            doc.write(out);
            templateBytes = out.toByteArray();
        }

        // Step 2: First hit - Rahul Sharma
        CustomerRequest rahul = new CustomerRequest(
                "Rahul Sharma",
                "Pune, Maharashtra",
                "rahul@example.com",
                "9876543210"
        );
        byte[] docAfterRahul = service.generateDocument(templateBytes, rahul);
        String textAfterRahul = extractText(docAfterRahul);

        assertTrue(textAfterRahul.contains("Customer Name: Rahul Sharma"));
        assertTrue(textAfterRahul.contains("Customer Address: Pune, Maharashtra"));
        assertTrue(textAfterRahul.contains("Customer Email: rahul@example.com"));
        assertTrue(textAfterRahul.contains("Customer Phone: 9876543210"));
        assertFalse(textAfterRahul.contains("{{customerName}}"));

        // Step 3: Second hit - Rushikesh (sent using docAfterRahul where placeholders are already replaced with Rahul)
        CustomerRequest rushikesh = new CustomerRequest(
                "Rushikesh Patil",
                "Mumbai, Maharashtra",
                "rushikesh@example.com",
                "9988776655"
        );
        byte[] docAfterRushikesh = service.generateDocument(docAfterRahul, rushikesh);
        String textAfterRushikesh = extractText(docAfterRushikesh);

        assertTrue(textAfterRushikesh.contains("Customer Name: Rushikesh Patil"),
                "Should contain Rushikesh Patil even though the template previously had Rahul Sharma");
        assertTrue(textAfterRushikesh.contains("Customer Address: Mumbai, Maharashtra"));
        assertTrue(textAfterRushikesh.contains("Customer Email: rushikesh@example.com"));
        assertTrue(textAfterRushikesh.contains("Customer Phone: 9988776655"));
        assertFalse(textAfterRushikesh.contains("Rahul Sharma"),
                "Should not contain old customer Rahul Sharma");

        // Step 4: Third hit - Rahul Sharma again (using docAfterRushikesh)
        byte[] docAfterRahulAgain = service.generateDocument(docAfterRushikesh, rahul);
        String textAfterRahulAgain = extractText(docAfterRahulAgain);

        assertTrue(textAfterRahulAgain.contains("Customer Name: Rahul Sharma"));
        assertFalse(textAfterRahulAgain.contains("Rushikesh Patil"));
    }

    private void addParagraph(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(text);
    }

    private String extractText(byte[] docBytes) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (ByteArrayInputStream in = new ByteArrayInputStream(docBytes);
             XWPFDocument doc = new XWPFDocument(in)) {
            for (XWPFParagraph p : doc.getParagraphs()) {
                sb.append(p.getText()).append("\n");
            }
        }
        return sb.toString();
    }
}
