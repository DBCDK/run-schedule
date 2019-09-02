/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt
 */

package dk.dbc.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    void isSatisfiedBy_InstantAndNextExactMatch() {
        final RunSchedule runSchedule = new RunSchedule("19 07 * * *")
               .withTimezone(ZoneId.of("Europe/Copenhagen"));
        final Instant instant = ZonedDateTime.parse("2019-01-24T07:19:00.00+01:00").toInstant();
        assertThat(runSchedule.isSatisfiedBy(
                instant, instant.minus(1, ChronoUnit.DAYS)),
                is(true));
    }

    @Test
    void isOverdue_Date() {
        final RunSchedule runSchedule = new RunSchedule("45 * * JAN *");
        final Instant instant = Instant.parse("2019-01-14T07:00:00.00Z");
        assertThat("null date, null last-run",
                runSchedule.isOverdue((Date)null, (Date)null),
                is(false));
        assertThat("null date, non-null last-run",
                runSchedule.isOverdue(null, Date.from(instant)),
                is(false));
        assertThat("non-null date, null last-run",
                runSchedule.isOverdue(Date.from(instant), null),
                is(false));
        assertThat("non-matching date, last-run on schedule",
                runSchedule.isOverdue(
                        Date.from(instant),
                        Date.from(instant.minus(15, ChronoUnit.MINUTES))),
                is(false));
        assertThat("matching date, last-run on schedule",
                runSchedule.isOverdue(
                        Date.from(instant.plus(45, ChronoUnit.MINUTES)),
                        Date.from(instant.minus(15, ChronoUnit.MINUTES))),
                is(false));
        assertThat("matching date, last-run before last schedule",
                runSchedule.isOverdue(
                        Date.from(instant.plus(45, ChronoUnit.MINUTES)),
                        Date.from(instant.plus(45, ChronoUnit.MINUTES)
                                .minus(1, ChronoUnit.DAYS))),
                is(true));
        assertThat("matching date, last-run after last schedule",
                runSchedule.isOverdue(
                        Date.from(instant.plus(45, ChronoUnit.MINUTES)),
                        Date.from(instant)),
                is(false));
        assertThat("non-matching date, last-run on schedule",
                runSchedule.isOverdue(
                        Date.from(instant),
                        Date.from(instant.minus(15, ChronoUnit.MINUTES))),
                is(false));
        assertThat("non-matching date, last-run after last schedule",
                runSchedule.isOverdue(Date.from(instant), Date.from(instant)),
                is(false));
        assertThat("non-matching date, last-run before last schedule",
                runSchedule.isOverdue(
                        Date.from(instant),
                        Date.from(instant.minus(2, ChronoUnit.HOURS))),
                is(true));
    }

    @Test
    void isOverdue_Instant() {
        final RunSchedule runSchedule = new RunSchedule("45 * * JAN *");
        final Instant instant = Instant.parse("2019-01-14T07:00:00.00Z");
        assertThat("null instant, null last-run",
                runSchedule.isOverdue((Instant)null, (Instant)null),
                is(false));
        assertThat("null instant, non-null last-run",
                runSchedule.isOverdue(null, instant),
                is(false));
        assertThat("non-null instant, null last-run",
                runSchedule.isOverdue(instant, null),
                is(false));
        assertThat("non-matching instant, last-run on schedule",
                runSchedule.isOverdue(instant, instant.minus(15, ChronoUnit.MINUTES)),
                is(false));
        assertThat("matching instant, last-run on schedule",
                runSchedule.isOverdue(
                        instant.plus(45, ChronoUnit.MINUTES),
                        instant.minus(15, ChronoUnit.MINUTES)),
                is(false));
        assertThat("matching instant, last-run before last schedule",
                runSchedule.isOverdue(
                        instant.plus(45, ChronoUnit.MINUTES),
                        instant.plus(45, ChronoUnit.MINUTES)
                                .minus(1, ChronoUnit.DAYS)),
                is(true));
        assertThat("matching instant, last-run after last schedule",
                runSchedule.isOverdue(instant.plus(45, ChronoUnit.MINUTES), instant),
                is(false));
        assertThat("non-matching instant, last-run on schedule",
                runSchedule.isOverdue(instant, instant.minus(15, ChronoUnit.MINUTES)),
                is(false));
        assertThat("non-matching instant, last-run after last schedule",
                runSchedule.isOverdue(instant, instant),
                is(false));
        assertThat("non-matching instant, last-run before last schedule",
                runSchedule.isOverdue(instant, instant.minus(2, ChronoUnit.HOURS)),
                is(true));
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