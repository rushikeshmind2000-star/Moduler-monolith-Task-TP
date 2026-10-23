package com.tech.emailvalidation.document.service;

import com.tech.emailvalidation.document.dto.CustomerRequest;
import com.tech.emailvalidation.document.exception.DocumentProcessingException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Service for manipulating Word (.docx) documents using Apache POI.
 *
 * <h3>Placeholder Handling</h3>
 * Word processors like Microsoft Word and Google Docs often split text across
 * multiple XML "runs" within a paragraph. For example, the placeholder
 * {@code {{customerName}}} might be stored as three runs:
 * <pre>
 *   Run 1: "{{customer"
 *   Run 2: "Name"
 *   Run 3: "}}"
 * </pre>
 * This service handles this by:
 * <ol>
 *   <li>Concatenating all run texts within a paragraph.</li>
 *   <li>Checking if the concatenated text contains any placeholder.</li>
 *   <li>Performing replacement on the concatenated text.</li>
 *   <li>Setting the result on the first run and clearing subsequent runs.</li>
 * </ol>
 *
 * <h3>Scope of replacement</h3>
 * Placeholders are replaced in:
 * <ul>
 *   <li>Document body paragraphs</li>
 *   <li>Table cells</li>
 *   <li>Headers and footers</li>
 * </ul>
 */
@Service
public class DocumentGenerationService {

    private static final Logger log = LoggerFactory.getLogger(DocumentGenerationService.class);

    /**
     * Replaces placeholders in the given .docx bytes with customer details.
     *
     * @param templateBytes the original document bytes downloaded from Drive
     * @param request       the customer data to insert
     * @return updated .docx bytes with all placeholders replaced
     * @throws DocumentProcessingException if reading/writing the document fails
     */
    public byte[] generateDocument(byte[] templateBytes, CustomerRequest request) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(templateBytes);
             XWPFDocument document = new XWPFDocument(inputStream)) {

            // Replace in body paragraphs
            replaceCustomerDataInParagraphs(document.getParagraphs(), request);

            // Replace in tables
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    List<XWPFTableCell> cells = row.getTableCells();
                    if (cells.size() >= 2) {
                        String labelCellText = cells.get(0).getText().trim();
                        if (labelCellText.matches("(?i).*(?:customer\\s+)?name.*")) {
                            updateCellText(cells.get(1), request.getCustomerName());
                        } else if (labelCellText.matches("(?i).*(?:customer\\s+)?address.*")) {
                            updateCellText(cells.get(1), request.getCustomerAddress());
                        } else if (labelCellText.matches("(?i).*(?:customer\\s+)?email.*")) {
                            updateCellText(cells.get(1), request.getCustomerEmail());
                        } else if (labelCellText.matches("(?i).*(?:customer\\s+)?phone.*")) {
                            updateCellText(cells.get(1), request.getCustomerPhone());
                        }
                    }
                    for (XWPFTableCell cell : cells) {
                        replaceCustomerDataInParagraphs(cell.getParagraphs(), request);
                    }
                }
            }

            // Replace in headers
            document.getHeaderList().forEach(header ->
                    replaceCustomerDataInParagraphs(header.getParagraphs(), request));

            // Replace in footers
            document.getFooterList().forEach(footer ->
                    replaceCustomerDataInParagraphs(footer.getParagraphs(), request));

            // Write the updated document to bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.write(outputStream);

            byte[] result = outputStream.toByteArray();
            log.info("Document generated successfully for customer '{}', size={} bytes",
                    request.getCustomerName(), result.length);
            return result;

        } catch (IOException e) {
            throw new DocumentProcessingException("Failed to process the Word document", e);
        }
    }

    /**
     * Replaces placeholders or updates existing customer field values in paragraphs.
     * Supports both:
     * 1. Direct placeholders (e.g. {{customerName}}, {{customerAddress}})
     * 2. Overwriting previous customer data (e.g. Customer Name: Rahul Sharma -> Customer Name: Rushikesh)
     */
    private void replaceCustomerDataInParagraphs(List<XWPFParagraph> paragraphs, CustomerRequest request) {
        if (paragraphs == null) {
            return;
        }

        for (XWPFParagraph paragraph : paragraphs) {
            List<XWPFRun> runs = paragraph.getRuns();
            if (runs == null || runs.isEmpty()) {
                continue;
            }

            // Case A: Label is in Run 0 (e.g. "Customer Name: ") and value is in Run 1+
            if (runs.size() >= 2) {
                String run0Text = runs.get(0).getText(0);
                if (run0Text != null) {
                    String trimmedRun0 = run0Text.trim();
                    String matchedField = getMatchedField(trimmedRun0);
                    if (matchedField != null) {
                        String newValue = getFieldValue(matchedField, request);
                        runs.get(1).setText(newValue, 0);
                        for (int i = 2; i < runs.size(); i++) {
                            runs.get(i).setText("", 0);
                        }
                        log.debug("Updated run 1 for label '{}' with '{}'", run0Text, newValue);
                        continue;
                    }
                }
            }

            // Case B: Combine all runs and perform string/regex replacements
            StringBuilder fullText = new StringBuilder();
            for (XWPFRun run : runs) {
                String text = run.getText(0);
                if (text != null) {
                    fullText.append(text);
                }
            }

            String originalText = fullText.toString();
            String replacedText = applyReplacements(originalText, request);

            if (!originalText.equals(replacedText)) {
                runs.get(0).setText(replacedText, 0);
                for (int i = 1; i < runs.size(); i++) {
                    runs.get(i).setText("", 0);
                }
                log.debug("Replaced paragraph text: '{}' -> '{}'", originalText, replacedText);
            }
        }
    }

    private void updateCellText(XWPFTableCell cell, String newText) {
        List<XWPFParagraph> paragraphs = cell.getParagraphs();
        if (paragraphs != null && !paragraphs.isEmpty()) {
            XWPFParagraph p = paragraphs.get(0);
            List<XWPFRun> runs = p.getRuns();
            if (runs != null && !runs.isEmpty()) {
                runs.get(0).setText(newText, 0);
                for (int i = 1; i < runs.size(); i++) {
                    runs.get(i).setText("", 0);
                }
            } else {
                p.createRun().setText(newText);
            }
        }
    }

    private String getMatchedField(String labelText) {
        if (labelText.matches("(?i)^(?:Customer\\s+)?Name\\s*:$")) {
            return "name";
        } else if (labelText.matches("(?i)^(?:Customer\\s+)?Address\\s*:$")) {
            return "address";
        } else if (labelText.matches("(?i)^(?:Customer\\s+)?Email(?:\\s+Address)?\\s*:$")) {
            return "email";
        } else if (labelText.matches("(?i)^(?:Customer\\s+)?Phone(?:\\s+Number)?\\s*:$")) {
            return "phone";
        }
        return null;
    }

    private String getFieldValue(String field, CustomerRequest request) {
        return switch (field) {
            case "name" -> request.getCustomerName();
            case "address" -> request.getCustomerAddress();
            case "email" -> request.getCustomerEmail();
            case "phone" -> request.getCustomerPhone();
            default -> "";
        };
    }

    /**
     * Applies both placeholder and label-based dynamic replacements to text.
     */
    public String applyReplacements(String text, CustomerRequest request) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;

        // 1. Placeholder replacements (supports {{customerName}}, {{name}}, {{ customerName }}, etc.)
        result = result.replaceAll("(?i)\\{\\{\\s*customername\\s*\\}\\}|\\{\\{\\s*name\\s*\\}\\}",
                Matcher.quoteReplacement(request.getCustomerName()));
        result = result.replaceAll("(?i)\\{\\{\\s*customeraddress\\s*\\}\\}|\\{\\{\\s*address\\s*\\}\\}",
                Matcher.quoteReplacement(request.getCustomerAddress()));
        result = result.replaceAll("(?i)\\{\\{\\s*customeremail\\s*\\}\\}|\\{\\{\\s*email\\s*\\}\\}",
                Matcher.quoteReplacement(request.getCustomerEmail()));
        result = result.replaceAll("(?i)\\{\\{\\s*customerphone\\s*\\}\\}|\\{\\{\\s*phone\\s*\\}\\}",
                Matcher.quoteReplacement(request.getCustomerPhone()));

        // 2. Pattern-based replacement for existing values on subsequent API hits:
        // E.g. "Customer Name: Rahul Sharma" -> "Customer Name: Rushikesh"
        result = result.replaceAll("(?i)((?:Customer\\s+)?Name\\s*:\\s*)[^\\r\\n]*",
                "$1" + Matcher.quoteReplacement(request.getCustomerName()));
        result = result.replaceAll("(?i)((?:Customer\\s+)?Address\\s*:\\s*)[^\\r\\n]*",
                "$1" + Matcher.quoteReplacement(request.getCustomerAddress()));
        result = result.replaceAll("(?i)((?:Customer\\s+)?Email(?:\\s+Address)?\\s*:\\s*)[^\\r\\n]*",
                "$1" + Matcher.quoteReplacement(request.getCustomerEmail()));
        result = result.replaceAll("(?i)((?:Customer\\s+)?Phone(?:\\s+Number)?\\s*:\\s*)[^\\r\\n]*",
                "$1" + Matcher.quoteReplacement(request.getCustomerPhone()));

        return result;
    }
}
