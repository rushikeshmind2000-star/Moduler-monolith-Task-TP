package com.tech.emailvalidation.offerletter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.tech.emailvalidation.offerletter.dto.OfferLetterRequest;
import com.tech.emailvalidation.offerletter.dto.OfferLetterResponse;
import com.tech.emailvalidation.offerletter.service.OfferLetterService;

@SpringBootTest
@Disabled("Run manually when verifying live email delivery")
class LiveEmailOfferLetterTest {

    @Autowired
    private OfferLetterService offerLetterService;

    @Test
    @DisplayName("Test generating offer letter PDF and sending live email to recipient")
    void testLiveOfferLetterEmail() {
        OfferLetterRequest request = OfferLetterRequest.builder()
                .employeeName("Rahul Sharma")
                .email("mindrushi55@gmail.com")
                .address("Pune, Maharashtra")
                .packageAmount("₹ 6,00,000")
                .designation("Java Developer")
                .department("Engineering")
                .joiningDate("15 November 2026")
                .companyName("ABC Technologies Pvt. Ltd.")
                .sendEmail(true)
                .build();

        OfferLetterResponse response = offerLetterService.generateAndSendOfferLetter(request);

        System.out.println(">>> LIVE OFFER LETTER RESULT: " + response.getStatus());
        System.out.println(">>> Email sent: " + response.getEmailSent());
        System.out.println(">>> Message: " + response.getMessage());
        System.out.println(">>> Email Delivery Status: " + response.getEmailDeliveryStatus());

        assertNotNull(response);
        assertNotNull(response.getId());
        assertTrue(response.getEmailSent(), "Email should be sent successfully with configured credentials");
    }
}
