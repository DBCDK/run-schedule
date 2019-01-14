/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt
 */

package dk.dbc.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RunScheduleTest {
    @Test
    void invalidExpression() {
        assertThrows(IllegalArgumentException.class, () -> new RunSchedule("invalid"));
    }

    @Test
    void defaultLocale() {
        final RunSchedule runSchedule = new RunSchedule("*/45 * * * *");
        assertThat(runSchedule.toDisplayString(), is("every 45 minutes"));
    }

    @Test
    void changeLocale() {
        final RunSchedule runSchedule = new RunSchedule("*/45 * * * *")
                .withLocale(Locale.GERMANY);
        assertThat(runSchedule.toDisplayString(), is("jede 45 Minuten"));
    }

    @Test
    void isSatisfiedBy_Date() {
        final RunSchedule runSchedule = new RunSchedule("45 * * JAN *");
        final Instant instant = Instant.parse("2019-01-14T07:00:00.00Z");
        assertThat("null date", runSchedule.isSatisfiedBy((Date)null),
                is(false));
        assertThat("non-matching date", runSchedule.isSatisfiedBy(Date.from(instant)),
                is(false));
        assertThat("matching date", runSchedule.isSatisfiedBy(Date.from(
                instant.plus(45, ChronoUnit.MINUTES))),
                is(true));
    }

    @Test
    void isSatisfiedBy_DateAndLastRun() {
        final RunSchedule runSchedule = new RunSchedule("45 * * JAN *");
        final Instant instant = Instant.parse("2019-01-14T07:00:00.00Z");
        assertThat("null instant, null last-run",
                runSchedule.isSatisfiedBy((Date)null, (Date)null),
                is(false));
        assertThat("non-matching instant, null last-run",
                runSchedule.isSatisfiedBy(Date.from(instant), null),
                is(false));
        assertThat("matching instant, null last-run",
                runSchedule.isSatisfiedBy(
                        Date.from(instant.plus(45, ChronoUnit.MINUTES)),
                        null),
                is(true));
        assertThat("matching instant, last-run day before",
                runSchedule.isSatisfiedBy(
                        Date.from(instant.plus(45, ChronoUnit.MINUTES)),
                        Date.from(instant.plus(45, ChronoUnit.MINUTES)
                                .minus(1, ChronoUnit.DAYS))),
                is(true));
        assertThat("matching instant, last-run day after",
                runSchedule.isSatisfiedBy(
                        Date.from(instant.plus(45, ChronoUnit.MINUTES)),
                        Date.from(instant.plus(45, ChronoUnit.MINUTES)
                                .plus(1, ChronoUnit.DAYS))),
                is(false));
        assertThat("matching instant, last-run before but within 60 seconds granularity",
                runSchedule.isSatisfiedBy(
                        Date.from(instant.plus(45, ChronoUnit.MINUTES)),
                        Date.from(instant.plus(45, ChronoUnit.MINUTES)
                                .plus(30, ChronoUnit.SECONDS))),
                is(false));
    }

    @Test
    void isSatisfiedBy_Instant() {
        final RunSchedule runSchedule = new RunSchedule("45 * * JAN *");
        final Instant instant = Instant.parse("2019-01-14T07:00:00.00Z");
        assertThat("null instant", runSchedule.isSatisfiedBy((Instant)null),
                is(false));
        assertThat("non-matching instant", runSchedule.isSatisfiedBy(instant),
                is(false));
        assertThat("matching instant", runSchedule.isSatisfiedBy(
                instant.plus(45, ChronoUnit.MINUTES)),
                is(true));
    }

    @Test
    void isSatisfiedBy_InstantAndLastRun() {
        final RunSchedule runSchedule = new RunSchedule("45 * * JAN *");
        final Instant instant = Instant.parse("2019-01-14T07:00:00.00Z");
        assertThat("null instant, null last-run",
                runSchedule.isSatisfiedBy((Instant)null, (Instant)null),
                is(false));
        assertThat("non-matching instant, null last-run",
                runSchedule.isSatisfiedBy(instant, null),
                is(false));
        assertThat("matching instant, null last-run",
                runSchedule.isSatisfiedBy(instant.plus(45, ChronoUnit.MINUTES), null),
                is(true));
        assertThat("matching instant, last-run day before",
                runSchedule.isSatisfiedBy(
                        instant.plus(45, ChronoUnit.MINUTES),
                        instant.plus(45, ChronoUnit.MINUTES)
                                .minus(1, ChronoUnit.DAYS)),
                is(true));
        assertThat("matching instant, last-run day after",
                runSchedule.isSatisfiedBy(
                        instant.plus(45, ChronoUnit.MINUTES),
                        instant.plus(45, ChronoUnit.MINUTES)
                                .plus(1, ChronoUnit.DAYS)),
                is(false));
        assertThat("matching instant, last-run before but within 60 seconds granularity",
                runSchedule.isSatisfiedBy(
                        instant.plus(45, ChronoUnit.MINUTES),
                        instant.plus(45, ChronoUnit.MINUTES)
                                .plus(30, ChronoUnit.SECONDS)),
                is(false));
    }

    @Test
    void changeTimezone() {
        final RunSchedule runSchedule = new RunSchedule("* 7 * * *")
                .withTimezone(ZoneId.of("UTC+1"));
        final Instant instant = Instant.parse("2019-01-14T07:00:00.00Z");
        assertThat("non-matching date", runSchedule.isSatisfiedBy(instant),
                is(false));
    }
}