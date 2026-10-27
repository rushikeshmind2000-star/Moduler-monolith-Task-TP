package com.tech.emailvalidation.offerletter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.tech.emailvalidation.offerletter.entity.OfferLetter;

@Repository
public interface OfferLetterRepository extends JpaRepository<OfferLetter, Long> {

    Optional<OfferLetter> findByReferenceNumber(String referenceNumber);

    List<OfferLetter> findByEmailOrderByCreatedAtDesc(String email);

    List<OfferLetter> findAllByOrderByCreatedAtDesc();

    @Modifying
    @Transactional
    @Query("UPDATE OfferLetter o SET o.emailSent = :emailSent, o.emailStatus = :emailStatus WHERE o.id = :id")
    int updateEmailDeliveryStatus(@Param("id") Long id, @Param("emailSent") Boolean emailSent, @Param("emailStatus") String emailStatus);
}
