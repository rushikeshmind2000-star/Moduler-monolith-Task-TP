package com.tech.emailvalidation.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tech.emailvalidation.dto.EmailVerificationResult;
import com.tech.emailvalidation.exception.EmailValidationException;

@Service
public class EmailVerifierServiceImpl implements EmailVerifierService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerifierServiceImpl.class);

    // RFC 5322 compliant regex for robust email syntax verification
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,15}$"
    );

    // Common disposable and temporary email domains
    private static final Set<String> DISPOSABLE_DOMAINS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "mailinator.com", "tempmail.com", "10minutemail.com", "guerrillamail.com",
            "throwawaymail.com", "sharklasers.com", "yopmail.com", "trashmail.com",
            "getairmail.com", "dispostable.com", "crazymailing.com", "fakemailgenerator.com",
            "generator.email", "mohmal.com", "mytemp.email", "tempail.com", "inboxkitten.com",
            "burnermail.io", "maildrop.cc", "dropmail.me", "nada.ltd"
    )));

    // Fast in-memory cache for domain MX resolution (TTL = 12 hours)
    private final Map<String, DomainMxCache> mxCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 12 * 60 * 60 * 1000L;

    private static class DomainMxCache {
        final boolean mxValid;
        final List<String> mxRecords;
        final long timestamp;

        DomainMxCache(boolean mxValid, List<String> mxRecords) {
            this.mxValid = mxValid;
            this.mxRecords = mxRecords;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return (System.currentTimeMillis() - timestamp) > CACHE_TTL_MS;
        }
    }

    @Override
    public EmailVerificationResult verifyEmail(String email) {
        if (email == null || email.isBlank()) {
            return EmailVerificationResult.builder()
                    .email(email)
                    .valid(false)
                    .formatValid(false)
                    .mxValid(false)
                    .disposable(false)
                    .failureReason("Email address cannot be empty or blank")
                    .build();
        }

        String trimmedEmail = email.trim().toLowerCase();

        // 1. Syntax check
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches() || trimmedEmail.length() > 254) {
            return EmailVerificationResult.builder()
                    .email(trimmedEmail)
                    .valid(false)
                    .formatValid(false)
                    .mxValid(false)
                    .disposable(false)
                    .failureReason("Invalid email address syntax or format")
                    .build();
        }

        String[] parts = trimmedEmail.split("@", 2);
        if (parts.length != 2) {
            return EmailVerificationResult.builder()
                    .email(trimmedEmail)
                    .valid(false)
                    .formatValid(false)
                    .mxValid(false)
                    .disposable(false)
                    .failureReason("Invalid email address structure")
                    .build();
        }

        String domain = parts[1].trim();

        // 2. Disposable check
        boolean isDisposable = isDisposableDomain(domain);
        if (isDisposable) {
            return EmailVerificationResult.builder()
                    .email(trimmedEmail)
                    .domain(domain)
                    .valid(false)
                    .formatValid(true)
                    .mxValid(false)
                    .disposable(true)
                    .failureReason("Disposable or temporary email domains are not allowed")
                    .build();
        }

        // 3. DNS MX check (cached)
        DomainMxCache cached = mxCache.get(domain);
        List<String> mxRecords;
        boolean mxValid;

        if (cached != null && !cached.isExpired()) {
            mxValid = cached.mxValid;
            mxRecords = cached.mxRecords;
        } else {
            mxRecords = resolveMxRecords(domain);
            mxValid = !mxRecords.isEmpty();
            mxCache.put(domain, new DomainMxCache(mxValid, mxRecords));
        }

        String primaryMx = mxRecords.isEmpty() ? null : mxRecords.get(0);

        if (!mxValid) {
            return EmailVerificationResult.builder()
                    .email(trimmedEmail)
                    .domain(domain)
                    .valid(false)
                    .formatValid(true)
                    .mxValid(false)
                    .disposable(false)
                    .failureReason("Domain '" + domain + "' does not exist or has no active mail servers (MX records)")
                    .build();
        }

        return EmailVerificationResult.builder()
                .email(trimmedEmail)
                .domain(domain)
                .valid(true)
                .formatValid(true)
                .mxValid(true)
                .disposable(false)
                .mxRecords(mxRecords)
                .primaryMx(primaryMx)
                .build();
    }

    @Override
    public void validateCandidateEmail(String email) {
        EmailVerificationResult result = verifyEmail(email);
        if (!result.isValid()) {
            log.warn("Candidate email validation rejected for '{}': {}", email, result.getFailureReason());
            throw new EmailValidationException(result.getFailureReason());
        }
    }

    private boolean isDisposableDomain(String domain) {
        if (DISPOSABLE_DOMAINS.contains(domain)) {
            return true;
        }
        for (String disposable : DISPOSABLE_DOMAINS) {
            if (domain.endsWith("." + disposable)) {
                return true;
            }
        }
        return false;
    }

    private List<String> resolveMxRecords(String domain) {
        List<String> records = new ArrayList<>();
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            env.put("com.sun.jndi.dns.timeout.initial", "2000");
            env.put("com.sun.jndi.dns.timeout.retries", "1");

            DirContext ictx = new InitialDirContext(env);
            Attributes attrs = ictx.getAttributes(domain, new String[]{"MX"});
            Attribute attr = attrs.get("MX");

            if (attr != null) {
                NamingEnumeration<?> en = attr.getAll();
                while (en.hasMore()) {
                    String record = (String) en.next();
                    String[] parts = record.split("\\s+");
                    String host = parts.length > 1 ? parts[1] : parts[0];
                    if (host.endsWith(".")) {
                        host = host.substring(0, host.length() - 1);
                    }
                    records.add(host);
                }
            }

            // Fallback: per RFC 5321, if no MX record exists, an A record may accept mail
            if (records.isEmpty()) {
                Attributes aAttrs = ictx.getAttributes(domain, new String[]{"A"});
                if (aAttrs.get("A") != null) {
                    records.add(domain);
                }
            }
        } catch (NamingException e) {
            log.debug("DNS MX lookup failed for domain '{}': {}", domain, e.getMessage());
        }
        return records;
    }
}
