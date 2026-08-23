package org.tbee.dancewithme;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tbee.dancewithme.domain.City;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.Dancestyle;
import org.tbee.dancewithme.domain.Role;
import org.tbee.dancewithme.domain.valueobject.SearchCriteriaSex;
import org.tbee.dancewithme.domain.valueobject.Sex;
import org.tbee.dancewithme.domain.Skilllevel;
import org.tbee.dancewithme.domain.repository.CityRepository;
import org.tbee.dancewithme.domain.repository.DancerRepository;
import org.tbee.dancewithme.domain.repository.DancestyleRepository;
import org.tbee.dancewithme.domain.repository.RoleRepository;
import org.tbee.dancewithme.domain.repository.SkilllevelRepository;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * Temporary test data: a few dancers with a generated profile picture.
 * Only active in the dev-testcontainer profile (started via DancewithmeTestContainer),
 * and only when the dancer table is empty. Remove once a database dump is available.
 */
@Component
@Profile("dev-testcontainer")
public class TestDataSeeder implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestDataSeeder.class);

    private final DancerRepository dancerRepository;
    private final CityRepository cityRepository;
    private final DancestyleRepository dancestyleRepository;
    private final RoleRepository roleRepository;
    private final SkilllevelRepository skilllevelRepository;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    public TestDataSeeder(DancerRepository dancerRepository, CityRepository cityRepository,
                          DancestyleRepository dancestyleRepository, RoleRepository roleRepository,
                          SkilllevelRepository skilllevelRepository, PasswordEncoder passwordEncoder) {
        this.dancerRepository = dancerRepository;
        this.cityRepository = cityRepository;
        this.dancestyleRepository = dancestyleRepository;
        this.roleRepository = roleRepository;
        this.skilllevelRepository = skilllevelRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (dancerRepository.count() > 0) {
            return;
        }
        LOGGER.info("Seeding test dancers");

        createDancer("Tbee", Sex.MALE, 1970, "Aalten", List.of(
                canDo("Ballroom", "lead", "advanced_social"),
                canDo("Latin", "lead", "intermediate_social")));
        createDancer("Anna", Sex.FEMALE, 1992, "Amsterdam", List.of(
                canDo("Ballroom", "follow", "intermediate_social")));
        createDancer("Bram", Sex.MALE, 1985, "Rotterdam", List.of(
                canDo("Latin", "lead", "novice"),
                canDo("Salsa", "lead", "advanced_social")));
        createDancer("Carmen", Sex.FEMALE, 1998, "Utrecht", List.of(
                canDo("Ballroom", "follow", "pre_competitive"),
                canDo("Latin", "follow", "pre_competitive")));
        createDancer("Daan", Sex.MALE, 1979, "Haarlem", List.of(
                canDo("Tango argentine", "lead", "intermediate_social"),
                canDo("Ballroom", "lead", "novice")));
        createDancer("Evi", Sex.FEMALE, 1990, "Eindhoven", List.of(
                canDo("West coast swing", "follow", "novice"),
                canDo("Salsa", "follow", "intermediate_social"),
                canDo("Ballroom", "follow", "absolute_beginner")));
    }

    /** What a dancer can do: dancestyle, role and skill level. */
    private record CanDo(Dancestyle dancestyle, Role role, Skilllevel skilllevel) {
    }

    private CanDo canDo(String dancestyleName, String roleName, String skilllevelCode) {
        return new CanDo(dancestyle(dancestyleName), role(roleName), skilllevel(skilllevelCode));
    }

    private void createDancer(String name, Sex sex, int yearOfBirth, String cityName, List<CanDo> canDos) {
        Dancer dancer = new Dancer()
                .email(name.toLowerCase() + "@tbee.org")
                .password(passwordEncoder.encode("password"))
                .name(name)
                .sex(sex)
                .yearOfBirth(yearOfBirth)
                .city(city(cityName))
                .mugshot(generateMugshot(name))
                .whoami("Hi, I'm " + name + " and I love dancing!")
                .whatdoiwant("Looking for a dance partner in the " + cityName + " area.")
                .weekFrequencyMin(1)
                .weekFrequencyMax(3)
                .distanceMax(125)
                .ageDistanceMax(50)
                .active(true)
                .publiclyFindable(true)
                .privacyAgreementAcceptedAt(LocalDateTime.now());
        List<Skilllevel> skilllevels = skilllevelRepository.findAllByOrderByLevelAsc();
        for (CanDo canDo : canDos) {
            dancer.addDancestyle(canDo.dancestyle(), canDo.role(), canDo.skilllevel());
            // searching for a partner with the complementary role in the same style,
            // accepting a range around the dancer's own skill level
            int ownIndex = skilllevels.indexOf(canDo.skilllevel());
            dancer.addSearchingFor(canDo.dancestyle(),
                    sex == Sex.MALE ? SearchCriteriaSex.FEMALE : SearchCriteriaSex.MALE,
                    role("lead".equals(canDo.role().name()) ? "follow" : "lead"),
                    skilllevels.get(Math.max(0, ownIndex - 2)),
                    skilllevels.get(Math.min(skilllevels.size() - 1, ownIndex + 2)));
        }
        dancerRepository.save(dancer);
    }

    private Skilllevel skilllevel(String code) {
        return skilllevelRepository.findAll().stream()
                .filter(skilllevel -> skilllevel.code().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Skilllevel not found: " + code));
    }

    private Dancestyle dancestyle(String name) {
        return dancestyleRepository.findAll().stream()
                .filter(dancestyle -> dancestyle.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Dancestyle not found: " + name));
    }

    private Role role(String name) {        return roleRepository.findAll().stream()
                .filter(role -> role.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Role not found: " + name));
    }

    private City city(String name) {
        return cityRepository.findAllByOrderByNameAsc().stream()
                .filter(city -> city.name().equals(name))
                .findFirst()
                .orElse(null);
    }

    /** Generates a simple profile picture: initials on a colored background. */
    private byte[] generateMugshot(String name) {
        int size = 400;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            Color background = new Color(100 + random.nextInt(156), 100 + random.nextInt(156), 100 + random.nextInt(156));
            g.setColor(background);
            g.fillRect(0, 0, size, size);

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, size / 2));
            String initials = name.substring(0, 1).toUpperCase();
            int textWidth = g.getFontMetrics().stringWidth(initials);
            g.drawString(initials, (size - textWidth) / 2, size / 2 + g.getFontMetrics().getAscent() / 2 - 20);
        }
        finally {
            g.dispose();
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
