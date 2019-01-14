/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt
 */

package dk.dbc.util;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

public class RunSchedule {
    private static final CronDefinition CRON_DEFINITION =
        CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);

    private static Cron parse(final String expression) {
        final CronParser parser = new CronParser(CRON_DEFINITION);
        return parser.parse(expression);
    }

    private final String expression;
    private final Cron cron;
    private final ExecutionTime executionTime;

    private Locale locale = Locale.getDefault();
    private ZoneId timezone = ZoneId.systemDefault();

    /**
     * @param expression schedule expression
     * @throws IllegalArgumentException on invalid expression
     */
    public RunSchedule(final String expression) throws IllegalArgumentException {
        this.expression = expression;
        this.cron = parse(expression);
        this.executionTime = ExecutionTime.forCron(this.cron);
    }

    public Locale getLocale() {
        return locale;
    }

    public RunSchedule withLocale(final Locale locale) {
        if (locale != null) {
            this.locale = locale;
        }
        return this;
    }

    public ZoneId getTimezone() {
        return timezone;
    }

    public RunSchedule withTimezone(final ZoneId timezone) {
        if (timezone != null) {
            this.timezone = timezone;
        }
        return this;
    }

    /**
     * Indicates whether the given date satisfies this run schedule.
     * Note that seconds are ignored, so two dates falling on different
     * seconds of the same minute will always have the same result here.
     * @param date the date to evaluate
     * @return a boolean indicating whether the given date satisfies the
     * run schedule
     */
    public boolean isSatisfiedBy(final Date date) {
        if (date != null) {
            return isSatisfiedBy(date.toInstant());
        }
        return false;
    }

    /**
     * Indicates whether the given date satisfies this run schedule
     * and is later than the optional date of the last run by more
     * than sixty seconds
     * @param date the date to evaluate
     * @param lastRun the date of the last run, can be null
     * @return a boolean indicating whether the given date satisfies the
     * run schedule
     */
    public boolean isSatisfiedBy(final Date date, final Date lastRun) {
        if (date != null) {
            return isSatisfiedBy(date.toInstant(),
                    lastRun != null ? lastRun.toInstant() : null);
        }
        return false;
    }

    /**
     * Indicates whether the given instant satisfies this run schedule.
     * Note that seconds are ignored, so two instants falling on different
     * seconds of the same minute will always have the same result here.
     * @param instant the instant to evaluate
     * @return a boolean indicating whether the given instant satisfies the
     * run schedule
     */
    public boolean isSatisfiedBy(final Instant instant) {
        if (instant != null) {
            final ZonedDateTime time = instant.atZone(timezone);
            return executionTime.isMatch(time);
        }
        return false;
    }

    /**
     * Indicates whether the given instant satisfies this run schedule
     * and is later than the optional instant of the last run by more
     * than sixty seconds
     * @param instant the instant to evaluate
     * @param lastRun the instant of the last run, can be null
     * @return a boolean indicating whether the given instant satisfies the
     * run schedule
     */
    public boolean isSatisfiedBy(final Instant instant, final Instant lastRun) {
        final boolean satisfied = isSatisfiedBy(instant);
        if (satisfied) {
            if (lastRun != null) {
                return isSatisfiedByLastRun(instant, lastRun);
            }
            return true;
        }
        return false;
    }

    /**
     * @return schedule in its original form
     */
    @Override
    public String toString() {
        return expression;
    }

    /**
     * @return schedule in locale specific human readable format
     */
    public String toDisplayString() {
        final CronDescriptor descriptor = CronDescriptor.instance(locale);
        return descriptor.describe(cron);
    }

    private boolean isSatisfiedByLastRun(Instant instant, Instant lastRun) {
        final ZonedDateTime zonedInstant = instant.atZone(timezone);
        final ZonedDateTime zonedLastRun = lastRun.atZone(timezone);
        final Comparator<ZonedDateTime> granularityComparator =
                Comparator.comparing(
                        time -> time.truncatedTo(ChronoUnit.MINUTES));
        if (granularityComparator.compare(zonedInstant, zonedLastRun) == 0) {
            return false;
        }
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(zonedLastRun);
        if (nextExecution.isPresent()) {
            final Comparator<ZonedDateTime> nextExecutionComparator =
                    Comparator.comparing(
                            time -> time.truncatedTo(ChronoUnit.MINUTES));
            return nextExecutionComparator.compare(nextExecution.get(), zonedInstant) < 0;
        }
        return true;
    }
}
