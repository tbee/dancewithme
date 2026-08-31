package org.tbee.dancewithme.giwth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.DancerDancestyle;
import org.tbee.dancewithme.domain.DancerSearchingFor;
import org.tbee.dancewithme.domain.Dancestyle;
import org.tbee.dancewithme.domain.Role;
import org.tbee.dancewithme.domain.Skilllevel;
import org.tbee.dancewithme.domain.repository.DancerRepository;
import org.tbee.dancewithme.domain.repository.DancestyleRepository;
import org.tbee.dancewithme.domain.repository.RoleRepository;
import org.tbee.dancewithme.domain.repository.SkilllevelRepository;
import org.tbee.dancewithme.domain.valueobject.SearchCriteriaSex;
import org.tbee.dancewithme.domain.valueobject.Sex;
import org.tbee.giwth.Given;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public class Populate {

    public static final String EMPTY_DANCER = "empty@example.com";
    public static final String BALLROOM_BEGINNER_DANCER = "ballroom_beginner@example.com";

    static public Given<StepContext> standardSetExists() {
        return sc -> {
            sc.inTransaction(() -> {
                DancerRepository dancerRepository = sc.beanFactory.getBean(DancerRepository.class);
                DancestyleRepository dancestyleRepository = sc.beanFactory.getBean(DancestyleRepository.class);
                RoleRepository roleRepository = sc.beanFactory.getBean(RoleRepository.class);
                SkilllevelRepository skilllevelRepository = sc.beanFactory.getBean(SkilllevelRepository.class);
                PasswordEncoder passwordEncoder = sc.beanFactory.getBean(PasswordEncoder.class);

                Dancestyle ballroom = dancestyleRepository.findByName("ballroom").orElseThrow();

                Role lead = roleRepository.findLead();
                Role follow = roleRepository.findFollow();

                Skilllevel beginner1 = skilllevelRepository.findByLevel(1).orElseThrow();
                Skilllevel novice2 = skilllevelRepository.findByLevel(2).orElseThrow();
                Skilllevel intermediateSocial3 = skilllevelRepository.findByLevel(3).orElseThrow();
                Skilllevel advancedSocial4 = skilllevelRepository.findByLevel(4).orElseThrow();
                Skilllevel precompetitive5 = skilllevelRepository.findByLevel(5).orElseThrow();
                Skilllevel regionalCompetitor6 = skilllevelRepository.findByLevel(6).orElseThrow();
                Skilllevel nationalCompetitor7 = skilllevelRepository.findByLevel(7).orElseThrow();
                Skilllevel nationalFinalist8 = skilllevelRepository.findByLevel(8).orElseThrow();
                Skilllevel international9 = skilllevelRepository.findByLevel(9).orElseThrow();
                Skilllevel worldElite10 = skilllevelRepository.findByLevel(10).orElseThrow();

                dancerRepository.save(new Dancer()
                        .name("Only bare minimum fields are filled")
                        .email(EMPTY_DANCER)
                        .password(passwordEncoder.encode(EMPTY_DANCER))
                        .emailConfirmedAt(sc.nowSupplier.get())
                        .privacyAgreementAcceptedAt(sc.nowSupplier.get()));

                dancerRepository.save(new Dancer()
                        .name("Ballroom beginner lead, 1-2/week, 50km")
                        .email(BALLROOM_BEGINNER_DANCER)
                        .password(passwordEncoder.encode(BALLROOM_BEGINNER_DANCER))
                        .emailConfirmedAt(sc.nowSupplier.get())
                        .privacyAgreementAcceptedAt(sc.nowSupplier.get()))
                        .sex(Sex.MALE)
                        .weekFrequencyMin(1)
                        .weekFrequencyMax(2)
                        .distanceMax(50)
                        .dancestyles(List.of(new DancerDancestyle().dancestyle(ballroom).role(lead).skilllevel(beginner1)))
                        .searchingFor(List.of(new DancerSearchingFor().dancestyle(ballroom).sex(SearchCriteriaSex.FEMALE).role(follow).skilllevelMax(beginner1).skilllevelMax(advancedSocial4)));
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
