package org.tbee.dancewithme.giwth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.repository.DancerRepository;
import org.tbee.giwth.Given;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

public class Populate {

    public static final String DANCER1EMAIL = "dancer1@example.com";

    static public Given<StepContext> standardSetExists() {
        return sc -> {
            sc.inTransaction(() -> {
                DancerRepository dancerRepository = sc.beanFactory.getBean(DancerRepository.class);
                PasswordEncoder passwordEncoder = sc.beanFactory.getBean(PasswordEncoder.class);

                dancerRepository.save(new Dancer()
                        .name("Dancer1")
                        .email(DANCER1EMAIL)
                        .password(passwordEncoder.encode(DANCER1EMAIL))
                        .emailConfirmedAt(sc.nowSupplier.get())
                        .privacyAgreementAcceptedAt(sc.nowSupplier.get()));
            });
        };
    }

    private static byte[] readAsBytes(String resourcePath) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = Populate.class.getResourceAsStream(resourcePath)) {
            inputStream.transferTo(byteArrayOutputStream);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
