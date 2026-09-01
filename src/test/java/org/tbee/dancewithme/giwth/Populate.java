package org.tbee.dancewithme.giwth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.DancerDancestyle;
import org.tbee.dancewithme.domain.DancerSearchingFor;
import org.tbee.dancewithme.domain.Dancestyle;
import org.tbee.dancewithme.domain.repository.DancerRepository;
import org.tbee.dancewithme.domain.repository.DancestyleRepository;
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
    public static final String BALLROOM_LEAD_MALE_BEGINNER = "B_L_M_B@example.com";
    public static final String BALLROOM_FOLLOW_FEMALE_BEGINNER = "B_F_F_B@example.com";
    public static final String LATIN_LEAD_MALE_BEGINNER = "L_L_M_B@example.com";
    public static final String LATIN_FOLLOW_FEMALE_BEGINNER = "L_F_F_B@example.com";

    static public Given<StepContext> standardSetExists() {
        return sc -> {
            sc.inTransaction(() -> {
                DancerRepository dancerRepository = sc.beanFactory.getBean(DancerRepository.class);
                DancestyleRepository dancestyleRepository = sc.beanFactory.getBean(DancestyleRepository.class);
                PasswordEncoder passwordEncoder = sc.beanFactory.getBean(PasswordEncoder.class);

                Dancestyle ballroom = sc.ballroom();
                Dancestyle latin = sc.latin();

                LocalDateTime now = sc.nowSupplier.get();

                dancerRepository.save(new Dancer()
                        .name("Only bare minimum fields are filled")
                        .email(EMPTY_DANCER)
                        .password(passwordEncoder.encode(EMPTY_DANCER))
                        .emailConfirmedAt(now)
                        .privacyAgreementAcceptedAt(now));

                dancerRepository.save(new Dancer()
                        .name("Ballroom male lead beginner lead, 1-2/week, 50km")
                        .email(BALLROOM_LEAD_MALE_BEGINNER)
                        .password(passwordEncoder.encode(BALLROOM_LEAD_MALE_BEGINNER))
                        .emailConfirmedAt(now)
                        .privacyAgreementAcceptedAt(now))
                        .sex(Sex.MALE)
                        .weekFrequencyMin(1)
                        .weekFrequencyMax(2)
                        .distanceMax(50)
                        .dancestyles(List.of(new DancerDancestyle().dancestyle(ballroom).role(LEAD).skilllevel(1)))
                        .searchingFor(List.of(new DancerSearchingFor().dancestyle(ballroom).sex(SearchCriteriaSex.FEMALE).role(FOLLOW).skilllevelMin(1).skilllevelMax(4)));

                dancerRepository.save(new Dancer()
                                .name("Ballroom female lead beginner lead, 1-2/week, 50km")
                                .email(BALLROOM_FOLLOW_FEMALE_BEGINNER)
                                .password(passwordEncoder.encode(BALLROOM_FOLLOW_FEMALE_BEGINNER))
                                .emailConfirmedAt(now)
                                .privacyAgreementAcceptedAt(now))
                        .sex(Sex.FEMALE)
                        .weekFrequencyMin(1)
                        .weekFrequencyMax(2)
                        .distanceMax(50)
                        .dancestyles(List.of(new DancerDancestyle().dancestyle(ballroom).role(FOLLOW).skilllevel(1)))
                        .searchingFor(List.of(new DancerSearchingFor().dancestyle(ballroom).sex(SearchCriteriaSex.MALE).role(LEAD).skilllevelMin(1).skilllevelMax(4)));

                dancerRepository.save(new Dancer()
                                .name("Latin male lead beginner lead, 1-2/week, 50km")
                                .email(LATIN_LEAD_MALE_BEGINNER)
                                .password(passwordEncoder.encode(LATIN_LEAD_MALE_BEGINNER))
                                .emailConfirmedAt(now)
                                .privacyAgreementAcceptedAt(now))
                        .sex(Sex.MALE)
                        .weekFrequencyMin(1)
                        .weekFrequencyMax(2)
                        .distanceMax(50)
                        .dancestyles(List.of(new DancerDancestyle().dancestyle(latin).role(LEAD).skilllevel(1)))
                        .searchingFor(List.of(new DancerSearchingFor().dancestyle(ballroom).sex(SearchCriteriaSex.FEMALE).role(FOLLOW).skilllevelMin(1).skilllevelMax(4)));

                dancerRepository.save(new Dancer()
                                .name("Latin female lead beginner lead, 1-2/week, 50km")
                                .email(LATIN_FOLLOW_FEMALE_BEGINNER)
                                .password(passwordEncoder.encode(LATIN_FOLLOW_FEMALE_BEGINNER))
                                .emailConfirmedAt(now)
                                .privacyAgreementAcceptedAt(now))
                        .sex(Sex.FEMALE)
                        .weekFrequencyMin(1)
                        .weekFrequencyMax(2)
                        .distanceMax(50)
                        .dancestyles(List.of(new DancerDancestyle().dancestyle(latin).role(FOLLOW).skilllevel(1)))
                        .searchingFor(List.of(new DancerSearchingFor().dancestyle(latin).sex(SearchCriteriaSex.MALE).role(LEAD).skilllevelMin(1).skilllevelMax(4)));
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
