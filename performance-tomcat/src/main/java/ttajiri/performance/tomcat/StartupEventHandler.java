package ttajiri.performance.tomcat;

import io.micrometer.core.instrument.*;
import org.slf4j.*;
import org.springframework.boot.context.event.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.*;

import java.util.*;
import java.util.function.*;

@Component
public class StartupEventHandler {

    private static Logger logger = LoggerFactory.getLogger(StartupEventHandler.class);

    private static final String[] METRICS = {"jvm.memory.used", "jvm.classes.loaded", "jvm.threads.live"};
    private static final String METRIC_MESSAGE_FORMAT = "Startup Metric >> {} = {}";

    private MeterRegistry meterRegistry;

    public StartupEventHandler(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @EventListener
    public void getStartupMetric(ApplicationReadyEvent event) {
        Arrays.asList(METRICS).forEach(this::logActuatorMetric);
    }

    private void logActuatorMetric(String metric) {
        var meter = meterRegistry.find(metric).meter();
        Map<Statistic, Double> stats = getSamples(meter);

        logger.info(METRIC_MESSAGE_FORMAT, metric, stats.get(Statistic.VALUE).longValue());
    }

    private Map<Statistic, Double> getSamples(Meter meter) {
        var samples = new LinkedHashMap<Statistic, Double>();
        mergeMeasurements(samples, meter);

        return samples;
    }

    private void mergeMeasurements(Map<Statistic, Double> samples, Meter meter) {
        // @formatter:off
        meter.measure()
             .forEach(measurement -> samples.merge(measurement.getStatistic(),
                                                   measurement.getValue(),
                                                   mergeFunction(measurement.getStatistic())
             ));
        // @formatter:on
    }

    /*
        NOTE: you can rewrite this as inline upper method:
        v1: current map value if key exists
        v2: measurement.getValue()
        (v1, v2) -> Statistic.MAX.equals(measurement.getStatistic()) ? Double.max(v1, v2) : Double.sum(v1,v2)
     */
    private BiFunction<Double, Double, Double> mergeFunction(Statistic statistic) {
        return Statistic.MAX.equals(statistic) ? Double::max : Double::sum;
    }
}
