package com.tech.emailvalidation.offerletter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tech.emailvalidation.offerletter.dto.OfferLetterRequest;
import com.tech.emailvalidation.offerletter.dto.OfferLetterResponse;
import com.tech.emailvalidation.offerletter.service.OfferLetterService;

@SpringBootTest
@AutoConfigureMockMvc
class OfferLetterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OfferLetterService offerLetterService;

    @Test
    @DisplayName("POST /api/offer-letter/generate-and-send should return 201 with offer letter details")
    void testGenerateAndSendEndpoint() throws Exception {
        OfferLetterRequest request = OfferLetterRequest.builder()
                .employeeName("Rushikesh Shinde")
                .email("rushikesh@example.com")
                .address("Flat 402, Skyline Towers, Hinjewadi, Pune 411057")
                .packageAmount("12 LPA")
                .designation("Senior Java Developer")
                .department("Engineering")
                .joiningDate("2026-11-15")
                .build();

        OfferLetterResponse mockResponse = OfferLetterResponse.builder()
                .id(1L)
                .referenceNumber("TP-OFFER-2026-1048")
                .employeeName("Rushikesh Shinde")
                .email("rushikesh@example.com")
                .address("Flat 402, Skyline Towers, Hinjewadi, Pune 411057")
                .packageAmount("12 LPA")
                .designation("Senior Java Developer")
                .department("Engineering")
                .joiningDate("2026-11-15")
                .companyName("TechPlus Technologies Pvt. Ltd.")
                .status("SUCCESS")
                .message("Offer letter generated successfully and sent to rushikesh@example.com")
                .emailSent(true)
                .emailDeliveryStatus("Email delivered with attached PDF")
                .generatedAt(LocalDateTime.now())
                .pdfDownloadUrl("/api/offer-letter/1/download")
                .build();

        when(offerLetterService.generateAndSendOfferLetter(any(OfferLetterRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/offer-letter/generate-and-send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeName").value("Rushikesh Shinde"))
                .andExpect(jsonPath("$.email").value("rushikesh@example.com"))
                .andExpect(jsonPath("$.packageAmount").value("12 LPA"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.emailSent").value(true));
    }

    @Test
    @DisplayName("POST /api/offer-letter/generate-and-send should support alias fields: mail and package")
    void testAliasFields() throws Exception {
        String jsonPayload = """
                {
                    "employee_name": "Rohan Patil",
                    "mail": "rohan@example.com",
                    "address": "Baner Road, Pune 411045",
                    "package": "10 LPA"
                }
                """;

        OfferLetterResponse mockResponse = OfferLetterResponse.builder()
                .id(2L)
                .referenceNumber("TP-OFFER-2026-1049")
                .employeeName("Rohan Patil")
                .email("rohan@example.com")
                .address("Baner Road, Pune 411045")
                .packageAmount("10 LPA")
                .status("SUCCESS")
                .emailSent(false)
                .build();

        when(offerLetterService.generateAndSendOfferLetter(any(OfferLetterRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/offer-letter/generate-and-send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeName").value("Rohan Patil"))
                .andExpect(jsonPath("$.email").value("rohan@example.com"))
                .andExpect(jsonPath("$.packageAmount").value("10 LPA"));
    }

    @Test
    @DisplayName("POST /api/offer-letter/generate-pdf should stream application/pdf")
    void testGeneratePdfDirectStream() throws Exception {
        OfferLetterRequest request = OfferLetterRequest.builder()
                .employeeName("Rushikesh Shinde")
                .email("rushikesh@example.com")
                .address("Hinjewadi, Pune")
                .packageAmount("12 LPA")
                .build();

        byte[] fakePdf = "%PDF-1.4 Fake PDF Content".getBytes(StandardCharsets.UTF_8);
        when(offerLetterService.generatePdfOnly(any(OfferLetterRequest.class))).thenReturn(fakePdf);

        mockMvc.perform(post("/api/offer-letter/generate-pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Offer_Letter_Rushikesh_Shinde.pdf\""))
                .andExpect(content().bytes(fakePdf));
    }
}
