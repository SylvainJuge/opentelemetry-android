/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.android.instrumentation.crash;

import io.opentelemetry.android.instrumentation.InstrumentedApplication;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import java.util.List;

/** Entrypoint for installing the crash reporting instrumentation. */
public final class CrashReporter {

    /** Returns a new {@link CrashReporter} with the default settings. */
    public static CrashReporter create() {
        return builder().build();
    }

    /** Returns a new {@link CrashReporterBuilder}. */
    public static CrashReporterBuilder builder() {
        return new CrashReporterBuilder();
    }

    private final List<AttributesExtractor<CrashDetails, Void>> additionalExtractors;

    CrashReporter(CrashReporterBuilder builder) {
        this.additionalExtractors = builder.additionalExtractors;
    }

    /**
     * Installs the crash reporting instrumentation on the given {@link InstrumentedApplication}.
     */
    public void installOn(InstrumentedApplication instrumentedApplication) {
        Thread.UncaughtExceptionHandler existingHandler =
                Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(
                new CrashReportingExceptionHandler(
                        buildInstrumenter(instrumentedApplication.getOpenTelemetrySdk()),
                        instrumentedApplication.getOpenTelemetrySdk().getSdkTracerProvider(),
                        existingHandler));
    }

    private Instrumenter<CrashDetails, Void> buildInstrumenter(OpenTelemetry openTelemetry) {
        return Instrumenter.<CrashDetails, Void>builder(
                        openTelemetry, "io.opentelemetry.crash", CrashDetails::spanName)
                .addAttributesExtractor(new CrashDetailsAttributesExtractor())
                .addAttributesExtractors(additionalExtractors)
                .buildInstrumenter();
    }
}
