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

        List<Dancestyle> dancestyles = dancestyleRepository.findAll();
        Role lead = role("lead");
        Role follow = role("follow");

        createDancer("Tbee", 1970, "Aalten", dancestyles.get(0), lead);
        createDancer("Anna", 1992, "Amsterdam", dancestyles.get(0), follow);
        createDancer("Bram", 1985, "Rotterdam", dancestyles.get(1 % dancestyles.size()), lead);
        createDancer("Carmen", 1998, "Utrecht", dancestyles.get(2 % dancestyles.size()), follow);
        createDancer("Daan", 1979, "Haarlem", dancestyles.get(3 % dancestyles.size()), lead);
        createDancer("Evi", 1990, "Eindhoven", dancestyles.get(4 % dancestyles.size()), follow);
    }

    private void createDancer(String name, int yearOfBirth, String cityName, Dancestyle dancestyle, Role role) {
        Skilllevel ownSkill = skilllevel();
        Dancer dancer = new Dancer()
                .email(name.toLowerCase() + "@tbee.org")
                .password(passwordEncoder.encode("password"))
                .name(name)
                .yearOfBirth(yearOfBirth)
                .city(city(cityName))
                .mugshot(generateMugshot(name))
                .whoami("Hi, I'm " + name + " and I love dancing!")
                .whatdoiwant("Looking for a dance partner in the " + cityName + " area.")
                .weekFrequencyMin(1)
                .weekFrequencyMax(3)
                .distanceToPartnerMax(50)
                .ageDistanceToPartnerMax(15)
                .active(true)
                .publiclyFindable(true)
                .privacyAgreementAcceptedAt(LocalDateTime.now());
        dancer.addDancestyle(dancestyle, role, ownSkill);
        // searching for a partner with the complementary role in the same style,
        // accepting a range around the dancer's own skill level
        List<Skilllevel> skilllevels = skilllevelRepository.findAllByOrderByLevelAsc();
        int ownIndex = skilllevels.indexOf(ownSkill);
        dancer.addSearchingFor(dancestyle, role("lead".equals(role.name()) ? "follow" : "lead"),
                skilllevels.get(Math.max(0, ownIndex - 2)),
                skilllevels.get(Math.min(skilllevels.size() - 1, ownIndex + 2)));
        dancerRepository.save(dancer);
    }

    private Skilllevel skilllevel() {
        List<Skilllevel> skilllevels = skilllevelRepository.findAllByOrderByLevelAsc();
        // a believable skill level: somewhere between novice and advanced social
        return skilllevels.get(1 + random.nextInt(3));
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
