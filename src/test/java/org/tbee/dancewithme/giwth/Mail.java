package org.tbee.dancewithme.giwth;

import org.tbee.dancewithme.infrastructure.mail.MailSenderStub;
import org.tbee.giwth.Then;

import static org.assertj.core.api.Assertions.assertThat;

public class Mail {

    public static ShouldBeSent shouldHaveBeenSent() {
        return new ShouldBeSent();
    }

    public static class ShouldBeSent implements Then<StepContext> {

        private String to;
        private String subject;
        private String textContaining;

        public ShouldBeSent to(String v) {
            this.to = v;
            return this;
        }

        public ShouldBeSent subject(String v) {
            this.subject = v;
            return this;
        }

        public ShouldBeSent textContaining(String v) {
            this.textContaining = v;
            return this;
        }

        @Override
        public void run(StepContext sc) {
            assertThat(MailSenderStub.sentSimpleMessages).anySatisfy(mail -> {
                if (to != null) {
                    assertThat(mail.getTo()).contains(to);
                }
                if (subject != null) {
                    assertThat(mail.getSubject()).isEqualTo(subject);
                }
                if (textContaining != null) {
                    assertThat(mail.getText()).contains(textContaining);
                }
            });
        }
    }
}