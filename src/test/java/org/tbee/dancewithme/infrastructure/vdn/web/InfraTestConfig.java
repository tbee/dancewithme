package org.tbee.dancewithme.infrastructure.vdn.web;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.Scope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.SessionScope;
import org.tbee.dancewithme.infrastructure.mail.MailSenderStub;

/// Shared by ALL infra/web tests (via InfraTestBase), so the whole suite uses a single Spring context
/// (Vaadin does not survive a cached context being reused after another context was created in the same JVM).
@TestConfiguration
public class InfraTestConfig {

    // Captures mails instead of sending them
    @Bean
    public JavaMailSender javaMailSender() {
        return new MailSenderStub();
    }

    // SessionSettings (used by e.g. SessionsValidator to format issue messages) is session-scoped, which is
    // not active outside a web request. During web requests the real session scope is used; on test threads
    // a thread-bound scope suffices.
    @Bean
    static BeanFactoryPostProcessor sessionScopeRegistrar() {
        return beanFactory -> beanFactory.registerScope("session", new Scope() {
            private final SessionScope sessionScope = new SessionScope();
            private final SimpleThreadScope threadScope = new SimpleThreadScope();

            private Scope activeScope() {
                return RequestContextHolder.getRequestAttributes() == null ? threadScope : sessionScope;
            }

            @Override
            public Object get(String name, ObjectFactory<?> objectFactory) {
                return activeScope().get(name, objectFactory);
            }

            @Override
            public Object remove(String name) {
                return activeScope().remove(name);
            }

            @Override
            public void registerDestructionCallback(String name, Runnable callback) {
                activeScope().registerDestructionCallback(name, callback);
            }

            @Override
            public Object resolveContextualObject(String key) {
                return activeScope().resolveContextualObject(key);
            }

            @Override
            public String getConversationId() {
                return activeScope().getConversationId();
            }
        });
    }
}
