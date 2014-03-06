/**
 * This file is part of SAMM.
 *
 * SAMM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SAMM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SAMM.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.edu.agh.samm.fileconfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.edu.agh.samm.api.core.ICoreManagement;
import pl.edu.agh.samm.api.core.ResourceAlreadyRegisteredException;
import pl.edu.agh.samm.api.core.Rule;
import pl.edu.agh.samm.api.metrics.IMetric;
import pl.edu.agh.samm.api.metrics.Metric;

import com.thoughtworks.xstream.XStream;

/**
 * @author koperek
 */
public class FileConfigurator {

    public static final String PROPERTIES_FILENAME_KEY = "configFile";
    private static final Logger logger = LoggerFactory.getLogger(FileConfigurator.class);

    private ICoreManagement coreManagement;
    private XStreamFactory xStreamFactory;
    private XStream xstream;

    public void init() {
        logger.info("Starting Property File Configurator!");

        xstream = xStreamFactory.createXStream();

        String configFilePath = System.getProperty(PROPERTIES_FILENAME_KEY);

        if (configFilePath == null) {
            logger.warn("No config file specified! Please use the -D"
                    + PROPERTIES_FILENAME_KEY + " VM option!");
        } else {
            File configFile = new File(configFilePath);
            try {
                Configuration configuration = (Configuration) xstream
                        .fromXML(new FileReader(configFile));

                nullSafeRegisterRules(configuration.getRuleSet());
                nullSafeRegisterResources(configuration.getResourceSet());
                nullSafeRegisterMetrics(configuration.getMetricSet());
            } catch (FileNotFoundException e) {
                logger.error("File (" + configFilePath + ") doesn't exist! ", e);
            }
        }

        logger.info("Starting Property File Configurator finished!");
    }

    private void nullSafeRegisterRules(RuleSet ruleSet) {
        if (ruleSet != null && ruleSet.getRules() != null) {
            registerRules(ruleSet.getRules());
        } else {
            logger.warn("No rules specified!");
        }
    }

    private void registerRules(List<Rule> rules) {
        for (Rule rule : rules) {
            logger.info("Adding rule: " + rule);
            coreManagement.addRule(rule);
        }
    }

    private void nullSafeRegisterResources(ConfigurationResourceSet resourceSet) {
        if (resourceSet != null && resourceSet.getResources() != null) {
            registerResources(resourceSet.getResources());
        }
    }

    private void nullSafeRegisterMetrics(ConfigurationMetricSet metricSet) {
        if (metricSet != null && metricSet.getMetrics() != null) {
            registerMetrics(metricSet.getMetrics());
        }
    }

    private void registerMetrics(List<IMetric> metrics) {
        for (IMetric metric : metrics) {
            // means that user didn't set it - set the default value
            // then
            if (metric.getMetricPollTimeInterval() == 0) {
                metric.setMetricPollTimeInterval(Metric.DEFAULT_METRIC_POLL_TIME_INTERVAL);
            }

            logger.info("Adding metric: " + metric.getMetricURI()
                    + " for " + metric.getResourceURI());

            logger.info("Starting metric: " + metric);
            coreManagement.startMetric(metric);
        }
    }

    private void registerResources(List<ConfigurationResource> resources) {
        for (ConfigurationResource configurationResource : resources) {
            try {
                coreManagement.registerResource(configurationResource.getResource());
            } catch (ResourceAlreadyRegisteredException e) {
                logger.error("Cannot add resource: "
                        + configurationResource, e);
            }
        }
    }

    public void setCoreManagement(ICoreManagement coreManagement) {
        this.coreManagement = coreManagement;
    }

    public void setxStreamFactory(XStreamFactory xStreamFactory) {
        this.xStreamFactory = xStreamFactory;
    }

}
