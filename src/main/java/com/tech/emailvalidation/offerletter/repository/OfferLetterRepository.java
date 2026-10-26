package com.tech.emailvalidation.offerletter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tech.emailvalidation.offerletter.entity.OfferLetter;

@Repository
public interface OfferLetterRepository extends JpaRepository<OfferLetter, Long> {

    Optional<OfferLetter> findByReferenceNumber(String referenceNumber);

    List<OfferLetter> findByEmailOrderByCreatedAtDesc(String email);

    List<OfferLetter> findAllByOrderByCreatedAtDesc();
}
