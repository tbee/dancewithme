package org.tbee.dancewithme.application;

/**
 * Port for sending email. Implemented by the infrastructure layer (e.g. SMTP).
 */
public interface EmailService {

    /**
     * Sends the email confirmation message with the given confirmation code and link.
     *
     * @param to              the recipient email address
     * @param code            the confirmation code (for manual entry)
     * @param confirmationUrl the absolute URL the recipient must click to confirm
     */
    void sendConfirmationEmail(String to, String code, String confirmationUrl);
}
