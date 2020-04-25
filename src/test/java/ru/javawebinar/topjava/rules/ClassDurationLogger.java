package ru.javawebinar.topjava.rules;

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ClassDurationLogger implements TestRule {

    private final List<TestDuration> durations = new ArrayList<>();

    @Override
    public Statement apply(Statement base, Description description) {
        Logger log = LoggerFactory.getLogger(description.getClassName());
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    base.evaluate();
                } catch (Throwable ex) {
                    log.error(ex.getMessage());
                } finally {
                    log.info("Test durations:");
                    durations.stream().map(TestDuration::toString).forEach(log::info);
                }
            }
        };
    }

    @Rule
    public TestRule methodDurationLogger() {
        return this::applyMethod;
    }

    private Statement applyMethod(Statement base, Description description) {
        Logger log = LoggerFactory.getLogger(description.getClassName());
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                LocalTime start = LocalTime.now();
                try {
                    base.evaluate();
                } catch (Throwable ex) {
                    log.error(ex.getMessage());
                } finally {
                    Duration duration = Duration.between(start, LocalTime.now());
                    TestDuration testDuration = new TestDuration(description, duration);
                    durations.add(testDuration);
                    log.info(testDuration.toString());
                }
            }
        };
    }


    private static class TestDuration {
        private final Description description;
        private final Duration duration;

        public TestDuration(Description description, Duration duration) {
            this.description = description;
            this.duration = duration;
        }

        @Override
        public String toString() {
            return description.getMethodName() + "-" + duration.toMillis() + " ms";
        }
    }
}
