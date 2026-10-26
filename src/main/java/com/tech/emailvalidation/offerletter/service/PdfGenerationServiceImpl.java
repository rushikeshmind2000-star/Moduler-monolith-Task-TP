package com.tech.emailvalidation.offerletter.service;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.tech.emailvalidation.offerletter.config.OfferLetterProperties;
import com.tech.emailvalidation.offerletter.dto.OfferLetterRequest;

@Service
public class PdfGenerationServiceImpl implements PdfGenerationService {

    private static final Logger log = LoggerFactory.getLogger(PdfGenerationServiceImpl.class);

    private final OfferLetterProperties properties;

    // Palette Colors matching the sample image
    private static final Color NAVY_DARK = new Color(10, 37, 80);        // #0A2550
    private static final Color NAVY_PRIMARY = new Color(15, 59, 108);    // #0F3B6C
    private static final Color ACCENT_BLUE = new Color(25, 118, 210);    // #1976D2
    private static final Color CYAN_ACCENT = new Color(41, 182, 246);    // #29B6F6
    private static final Color BANNER_BG = new Color(235, 245, 253);     // #EBF5FD
    private static final Color TABLE_BORDER = new Color(207, 226, 248);  // #CFE2F8
    private static final Color TEXT_DARK = new Color(33, 43, 54);        // #212B36
    private static final Color TEXT_MUTED = new Color(100, 116, 139);    // #64748B

    // Typography
    private final Font companyNameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15f, NAVY_DARK);
    private final Font taglineFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7f, ACCENT_BLUE);
    private final Font headerContactFont = FontFactory.getFont(FontFactory.HELVETICA, 8f, TEXT_MUTED);

    private final Font docTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f, NAVY_PRIMARY);
    private final Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 9f, TEXT_DARK);

    private final Font toLabelFont = FontFactory.getFont(FontFactory.HELVETICA, 9f, TEXT_DARK);
    private final Font candidateNameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, NAVY_DARK);
    private final Font candidateDetailsFont = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, TEXT_DARK);

    private final Font subjectFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, NAVY_PRIMARY);

    private final Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 8.8f, TEXT_DARK);
    private final Font bodyBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.8f, NAVY_PRIMARY);

    private final Font sectionHeadingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f, NAVY_PRIMARY);
    private final Font tableLabelFont = FontFactory.getFont(FontFactory.HELVETICA, 8.8f, TEXT_MUTED);
    private final Font tableValueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.8f, NAVY_PRIMARY);

    private final Font signoffTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, NAVY_DARK);
    private final Font signoffSubFont = FontFactory.getFont(FontFactory.HELVETICA, 8.2f, TEXT_MUTED);
    private final Font footerTextFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7f, ACCENT_BLUE);

    public PdfGenerationServiceImpl(OfferLetterProperties properties) {
        this.properties = properties;
    }

    @Override
    public byte[] generateOfferLetterPdf(OfferLetterRequest request, String referenceNumber) {
        log.info("Generating Offer Letter PDF for employee: {} (Ref: {})", request.getEmployeeName(), referenceNumber);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Tight margins (36 left/right, 20 top, 24 bottom) to ensure 100% single-page fit
            Document document = new Document(PageSize.A4, 38, 38, 20, 24);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            // Add background event for header top-right subtle curve and bottom waves
            writer.setPageEvent(new ModernOfferLetterPageEvent());

            document.open();

            String companyName = resolveCompanyName(request);
            String designation = resolveDesignation(request);
            String joiningDate = resolveJoiningDate(request);
            String packageAmount = formatPackageAmount(request.getPackageAmount());
            String address = request.getAddress().trim();

            // 1. Top Header: Logo + Company Name & Tagline (Left) vs Contact Info (Right)
            addTopHeader(document, companyName);

            // 2. Document Title: "LETTER OF OFFER" with underline + Date
            addDocumentTitleAndDate(document);

            // 3. Candidate Address Block
            addCandidateBlock(document, request);

            // 4. Subject Banner: "Subject: Offer of Employment – [Designation]"
            addSubjectBanner(document, designation);

            // 5. Salutation & Opening Paragraph
            addSalutationAndIntro(document, request.getEmployeeName(), designation, companyName);

            // 6. Offer Details Card Table
            addOfferDetailsTable(document, designation, packageAmount, joiningDate, address);

            // 7. Terms and Conditions
            addTermsAndConditions(document);

            // 8. Closing Paragraphs
            addClosingParagraphs(document);

            // 9. Signatures Block with Stylized Signature
            addSignoffAndSignature(document, companyName);

            // 10. Footer Line and Confidential Notice
            addFooterNotice(document, companyName);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate Offer Letter PDF for {}", request.getEmployeeName(), e);
            throw new RuntimeException("Error occurred while generating Offer Letter PDF: " + e.getMessage(), e);
        }
    }

    private void addTopHeader(Document doc, String companyName) throws Exception {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{60, 40});
        headerTable.setSpacingAfter(4f);

        // Left Cell: Modern Geometric Logo + Company Name + Tagline
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(PdfPCell.NO_BORDER);
        leftCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPTable brandingTable = new PdfPTable(2);
        brandingTable.setWidthPercentage(100);
        brandingTable.setWidths(new float[]{22, 78});

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(PdfPCell.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Image logoImg = Image.getInstance(createLogoImage(), null);
        logoImg.scaleToFit(38, 38);
        logoCell.addElement(logoImg);
        brandingTable.addCell(logoCell);

        PdfPCell textCell = new PdfPCell();
        textCell.setBorder(PdfPCell.NO_BORDER);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        textCell.setPaddingLeft(4f);

        Paragraph compPara = new Paragraph(companyName.toUpperCase(), companyNameFont);
        compPara.setSpacingAfter(1f);
        textCell.addElement(compPara);

        Paragraph taglinePara = new Paragraph("INNOVATION  •  TECHNOLOGY  •  GROWTH", taglineFont);
        textCell.addElement(taglinePara);

        brandingTable.addCell(textCell);
        leftCell.addElement(brandingTable);
        headerTable.addCell(leftCell);

        // Right Cell: Address, Email, Web
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(PdfPCell.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        rightCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Paragraph contactPara = new Paragraph();
        contactPara.setAlignment(Element.ALIGN_RIGHT);
        contactPara.setLeading(10.5f);
        contactPara.add(new Chunk("📍 " + properties.getCompany().getAddress() + "\n", headerContactFont));
        contactPara.add(new Chunk("✉  " + properties.getCompany().getEmail() + "\n", headerContactFont));
        contactPara.add(new Chunk("🌐 " + properties.getCompany().getWebsite(), headerContactFont));

        rightCell.addElement(contactPara);
        headerTable.addCell(rightCell);

        doc.add(headerTable);

        // Thin blue divider line
        LineSeparator line = new LineSeparator(1.2f, 100, ACCENT_BLUE, Element.ALIGN_CENTER, -1);
        doc.add(line);
    }

    private void addDocumentTitleAndDate(Document doc) throws Exception {
        // Center Document Title
        Paragraph title = new Paragraph("LETTER OF OFFER", docTitleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(4f);
        title.setSpacingAfter(2f);
        doc.add(title);

        // Cyan accent line beneath title
        LineSeparator titleUnderline = new LineSeparator(2f, 15, CYAN_ACCENT, Element.ALIGN_CENTER, -1);
        doc.add(titleUnderline);

        // Date right-aligned
        String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy"));
        Paragraph datePara = new Paragraph("Date: " + formattedDate, dateFont);
        datePara.setAlignment(Element.ALIGN_RIGHT);
        datePara.setSpacingBefore(3f);
        datePara.setSpacingAfter(5f);
        doc.add(datePara);
    }

    private void addCandidateBlock(Document doc, OfferLetterRequest req) throws Exception {
        Paragraph toPara = new Paragraph("To,", toLabelFont);
        toPara.setSpacingAfter(1f);
        doc.add(toPara);

        Paragraph namePara = new Paragraph(req.getEmployeeName(), candidateNameFont);
        namePara.setSpacingAfter(1f);
        doc.add(namePara);

        Paragraph addressPara = new Paragraph(req.getAddress(), candidateDetailsFont);
        addressPara.setSpacingAfter(1f);
        doc.add(addressPara);

        Paragraph emailPara = new Paragraph("Email: " + req.getEmail(), candidateDetailsFont);
        emailPara.setSpacingAfter(6f);
        doc.add(emailPara);
    }

    private void addSubjectBanner(Document doc, String designation) throws Exception {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingAfter(6f);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(BANNER_BG);
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setPadding(6f);
        cell.setPaddingLeft(10f);

        Paragraph p = new Paragraph("Subject: Offer of Employment – " + designation, subjectFont);
        cell.addElement(p);

        table.addCell(cell);
        doc.add(table);
    }

    private void addSalutationAndIntro(Document doc, String candidateName, String designation, String companyName) throws Exception {
        Paragraph salutation = new Paragraph("Dear " + candidateName + ",", bodyFont);
        salutation.setSpacingAfter(3f);
        doc.add(salutation);

        Paragraph intro = new Paragraph();
        intro.setLeading(12f);
        intro.add(new Chunk("We are pleased to offer you the position of ", bodyFont));
        intro.add(new Chunk(designation, bodyBold));
        intro.add(new Chunk(" at ", bodyFont));
        intro.add(new Chunk(companyName + ". ", bodyBold));
        intro.add(new Chunk("Congratulations on your selection! We are excited about the possibility of you joining our team.", bodyFont));
        intro.setSpacingAfter(8f);
        doc.add(intro);
    }

    private void addOfferDetailsTable(Document doc, String designation, String packageAmount, String joiningDate, String address) throws Exception {
        // Section Header with Briefcase Icon
        PdfPTable cardHeader = new PdfPTable(1);
        cardHeader.setWidthPercentage(100);

        PdfPCell headCell = new PdfPCell();
        headCell.setBackgroundColor(BANNER_BG);
        headCell.setBorderColor(TABLE_BORDER);
        headCell.setBorderWidth(0.8f);
        headCell.setBorderWidthBottom(0f);
        headCell.setPadding(5f);
        headCell.setPaddingLeft(10f);

        Paragraph headerText = new Paragraph("💼  Offer Details", sectionHeadingFont);
        headCell.addElement(headerText);
        cardHeader.addCell(headCell);
        doc.add(cardHeader);

        // Table with 2 columns
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{38, 62});
        table.setSpacingAfter(8f);

        addDetailRow(table, "Designation", designation);
        addDetailRow(table, "Annual Package (CTC)", packageAmount);
        addDetailRow(table, "Joining Date", joiningDate);
        addDetailRow(table, "Work Location", address);

        doc.add(table);
    }

    private void addDetailRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, tableLabelFont));
        labelCell.setPadding(4.5f);
        labelCell.setPaddingLeft(10f);
        labelCell.setBorderColor(TABLE_BORDER);
        labelCell.setBorderWidth(0.8f);
        labelCell.setBackgroundColor(Color.WHITE);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, tableValueFont));
        valueCell.setPadding(4.5f);
        valueCell.setPaddingLeft(10f);
        valueCell.setBorderColor(TABLE_BORDER);
        valueCell.setBorderWidth(0.8f);
        valueCell.setBackgroundColor(Color.WHITE);
        table.addCell(valueCell);
    }

    private void addTermsAndConditions(Document doc) throws Exception {
        Paragraph heading = new Paragraph("Terms and Conditions", sectionHeadingFont);
        heading.setSpacingAfter(4f);
        doc.add(heading);

        String[] terms = {
                "Your employment will be subject to the company’s policies and applicable employment terms and conditions.",
                "Your detailed salary structure and benefits will be provided as part of the official employment documentation.",
                "You are expected to join on the date mentioned above and complete the required onboarding formalities.",
                "This offer is subject to any applicable background verification and documentation requirements specified by the company."
        };

        for (int i = 0; i < terms.length; i++) {
            Paragraph termPara = new Paragraph();
            termPara.setLeading(11.5f);
            termPara.setSpacingAfter(2.5f);
            termPara.setIndentationLeft(12f);
            termPara.setFirstLineIndent(-12f);
            termPara.add(new Chunk((i + 1) + ".   ", bodyFont));
            termPara.add(new Chunk(terms[i], bodyFont));
            doc.add(termPara);
        }
    }

    private void addClosingParagraphs(Document doc) throws Exception {
        Paragraph p1 = new Paragraph(
                "We believe your skills and contributions will be valuable to our organization. We look forward to welcoming you to our team.",
                bodyFont
        );
        p1.setSpacingBefore(4f);
        p1.setSpacingAfter(3f);
        doc.add(p1);

        Paragraph p2 = new Paragraph(
                "To accept this offer, please confirm your acceptance to the HR department.",
                bodyFont
        );
        p2.setSpacingAfter(8f);
        doc.add(p2);
    }

    private void addSignoffAndSignature(Document doc, String companyName) throws Exception {
        Paragraph signoff = new Paragraph("Warm regards,", bodyFont);
        signoff.setSpacingAfter(2f);
        doc.add(signoff);

        // Handwritten styled signature image
        Image signatureImg = Image.getInstance(createSignatureImage(), null);
        signatureImg.scaleToFit(90, 26);
        signatureImg.setSpacingAfter(2f);
        doc.add(signatureImg);

        Paragraph name = new Paragraph(properties.getHr().getSignatoryName(), signoffTitleFont);
        name.setSpacingAfter(0f);
        doc.add(name);

        Paragraph title = new Paragraph(properties.getHr().getSignatoryTitle(), signoffSubFont);
        title.setSpacingAfter(0f);
        doc.add(title);

        Paragraph comp = new Paragraph(companyName, signoffSubFont);
        comp.setSpacingAfter(2f);
        doc.add(comp);

        Paragraph contact = new Paragraph();
        contact.setLeading(10.5f);
        contact.add(new Chunk("✉  " + properties.getHr().getSignatoryEmail() + "\n", signoffSubFont));
        contact.add(new Chunk("📞  " + properties.getHr().getSignatoryPhone(), signoffSubFont));
        contact.setSpacingAfter(8f);
        doc.add(contact);
    }

    private void addFooterNotice(Document doc, String companyName) throws Exception {
        LineSeparator line = new LineSeparator(0.7f, 100, TABLE_BORDER, Element.ALIGN_CENTER, -1);
        doc.add(line);

        Paragraph footer = new Paragraph(
                "CONFIDENTIAL EMPLOYMENT DOCUMENT   •   " + companyName.toUpperCase(),
                footerTextFont
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(3f);
        doc.add(footer);
    }

    private String formatPackageAmount(String amount) {
        if (amount == null || amount.isBlank()) {
            return "₹ 6,00,000 (Six Lakh Only)";
        }
        String trimmed = amount.trim();
        // If it's a plain number like "12" or "6"
        if (trimmed.matches("^\\d+$")) {
            return "₹ " + trimmed + " LPA";
        }
        // If it doesn't start with ₹ or Rs. or INR, prepend ₹
        if (!trimmed.startsWith("₹") && !trimmed.toLowerCase().startsWith("rs") && !trimmed.toLowerCase().startsWith("inr")) {
            return "₹ " + trimmed;
        }
        return trimmed;
    }

    private java.awt.Image createLogoImage() {
        int w = 240, h = 240;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Path2D.Double leftPath = new Path2D.Double();
        leftPath.moveTo(110, 20);
        leftPath.lineTo(20, 200);
        leftPath.lineTo(75, 200);
        leftPath.lineTo(135, 75);
        leftPath.closePath();
        g2.setColor(new Color(10, 61, 145)); // #0A3D91
        g2.fill(leftPath);

        Path2D.Double rightPath = new Path2D.Double();
        rightPath.moveTo(125, 20);
        rightPath.lineTo(145, 60);
        rightPath.lineTo(185, 140);
        rightPath.lineTo(220, 200);
        rightPath.lineTo(165, 200);
        rightPath.lineTo(110, 95);
        rightPath.closePath();
        g2.setColor(new Color(41, 182, 246)); // #29B6F6
        g2.fill(rightPath);

        Path2D.Double bar = new Path2D.Double();
        bar.moveTo(85, 175);
        bar.lineTo(155, 175);
        bar.lineTo(140, 200);
        bar.lineTo(70, 200);
        bar.closePath();
        g2.setColor(new Color(25, 118, 210)); // #1976D2
        g2.fill(bar);

        g2.dispose();
        return img;
    }

    private java.awt.Image createSignatureImage() {
        int w = 320, h = 100;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g2.setColor(new Color(15, 45, 90));
        g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        Path2D.Double sig = new Path2D.Double();
        sig.moveTo(25, 70);
        sig.curveTo(35, 25, 40, 15, 55, 15);
        sig.curveTo(75, 15, 80, 35, 65, 50);
        sig.curveTo(50, 60, 30, 50, 42, 85);

        sig.moveTo(55, 55);
        sig.curveTo(65, 48, 75, 45, 85, 56);
        sig.curveTo(92, 45, 100, 48, 105, 58);
        sig.curveTo(115, 45, 125, 46, 130, 60);

        sig.moveTo(135, 58);
        sig.curveTo(150, 35, 170, 40, 185, 55);
        sig.curveTo(200, 40, 220, 45, 240, 52);

        sig.moveTo(40, 88);
        sig.curveTo(90, 80, 180, 78, 260, 82);

        g2.draw(sig);
        g2.dispose();
        return img;
    }

    private String resolveCompanyName(OfferLetterRequest req) {
        if (req.getCompanyName() != null && !req.getCompanyName().isBlank()) {
            return req.getCompanyName().trim();
        }
        return properties.getCompany().getName();
    }

    private String resolveDesignation(OfferLetterRequest req) {
        if (req.getDesignation() != null && !req.getDesignation().isBlank()) {
            return req.getDesignation().trim();
        }
        return "Java Developer";
    }

    private String resolveJoiningDate(OfferLetterRequest req) {
        if (req.getJoiningDate() != null && !req.getJoiningDate().isBlank()) {
            return req.getJoiningDate().trim();
        }
        return LocalDate.now().plusDays(15).format(DateTimeFormatter.ofPattern("d MMMM yyyy"));
    }

    private static class ModernOfferLetterPageEvent extends PdfPageEventHelper {

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContentUnder();
            float width = document.getPageSize().getWidth();

            cb.saveState();

            // 1. Top right subtle decorative quarter circle
            cb.setColorFill(new Color(235, 245, 253));
            cb.moveTo(width - 80, document.getPageSize().getHeight());
            cb.curveTo(width - 35, document.getPageSize().getHeight() - 8, width, document.getPageSize().getHeight() - 35, width, document.getPageSize().getHeight() - 80);
            cb.lineTo(width, document.getPageSize().getHeight());
            cb.closePath();
            cb.fill();

            // 2. Bottom Wave 1 - Accent Blue (#29B6F6)
            cb.setColorFill(new Color(41, 182, 246, 200));
            cb.moveTo(0, 0);
            cb.lineTo(width, 0);
            cb.lineTo(width, 32);
            cb.curveTo(width * 0.75f, 10, width * 0.45f, 24, 0, 8);
            cb.closePath();
            cb.fill();

            // 3. Bottom Wave 2 - Deep Navy Blue (#0A3D91)
            cb.setColorFill(new Color(10, 61, 145));
            cb.moveTo(0, 0);
            cb.lineTo(width, 0);
            cb.lineTo(width, 18);
            cb.curveTo(width * 0.70f, 4, width * 0.35f, 20, 0, 5);
            cb.closePath();
            cb.fill();

            cb.restoreState();
        }
    }
}
