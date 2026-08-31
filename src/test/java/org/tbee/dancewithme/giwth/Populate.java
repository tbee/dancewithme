package org.tbee.dancewithme.giwth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.DancerDancestyle;
import org.tbee.dancewithme.domain.DancerSearchingFor;
import org.tbee.dancewithme.domain.Dancestyle;
import org.tbee.dancewithme.domain.repository.DancerRepository;
import org.tbee.dancewithme.domain.repository.DancestyleRepository;
import org.tbee.dancewithme.domain.valueobject.Role;
import org.tbee.dancewithme.domain.valueobject.SearchCriteriaSex;
import org.tbee.dancewithme.domain.valueobject.Sex;
import org.tbee.giwth.Given;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.tbee.dancewithme.domain.valueobject.Role.FOLLOW;
import static org.tbee.dancewithme.domain.valueobject.Role.LEAD;

public class Populate {

    public static final String EMPTY_DANCER = "empty@example.com";
    public static final String BALLROOM_BEGINNER_DANCER = "ballroom_beginner@example.com";

    static public Given<StepContext> standardSetExists() {
        return sc -> {
            sc.inTransaction(() -> {
                DancerRepository dancerRepository = sc.beanFactory.getBean(DancerRepository.class);
                DancestyleRepository dancestyleRepository = sc.beanFactory.getBean(DancestyleRepository.class);
                PasswordEncoder passwordEncoder = sc.beanFactory.getBean(PasswordEncoder.class);

                Dancestyle ballroom = dancestyleRepository.findBallroom();

                LocalDateTime now = sc.nowSupplier.get();

                dancerRepository.save(new Dancer()
                        .name("Only bare minimum fields are filled")
                        .email(EMPTY_DANCER)
                        .password(passwordEncoder.encode(EMPTY_DANCER))
                        .emailConfirmedAt(now)
                        .privacyAgreementAcceptedAt(now));

                dancerRepository.save(new Dancer()
                        .name("Ballroom beginner lead, 1-2/week, 50km")
                        .email(BALLROOM_BEGINNER_DANCER)
                        .password(passwordEncoder.encode(BALLROOM_BEGINNER_DANCER))
                        .emailConfirmedAt(now)
                        .privacyAgreementAcceptedAt(now))
                        .sex(Sex.MALE)
                        .weekFrequencyMin(1)
                        .weekFrequencyMax(2)
                        .distanceMax(50)
                        .dancestyles(List.of(new DancerDancestyle().dancestyle(ballroom).role(LEAD).skilllevel(1)))
                        .searchingFor(List.of(new DancerSearchingFor().dancestyle(ballroom).sex(SearchCriteriaSex.FEMALE).role(FOLLOW).skilllevelMin(1).skilllevelMax(4)));
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
