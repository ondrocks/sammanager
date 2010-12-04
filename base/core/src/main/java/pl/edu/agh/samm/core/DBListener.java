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

package pl.edu.agh.samm.core;

import java.util.Collection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.edu.agh.samm.common.core.ICoreManagement;
import pl.edu.agh.samm.common.db.IStorageService;
import pl.edu.agh.samm.common.metrics.IConfiguredMetric;
import pl.edu.agh.samm.common.metrics.IMetricListener;
import pl.edu.agh.samm.common.metrics.IMetricsManagerListener;
import pl.edu.agh.samm.common.metrics.MetricNotRunningException;
import pl.edu.agh.samm.common.tadapter.ICapabilityEvent;
import pl.edu.agh.samm.common.tadapter.IMeasurementListener;

/**
 * @author Pawel Koperek <pkoperek@gmail.com>
 * @author Mateusz Kupisz <mkupisz@gmail.com>
 * 
 */
public class DBListener implements IMeasurementListener, IMetricListener, IMetricsManagerListener {

	private static final Logger logger = LoggerFactory.getLogger(DBListener.class);

	private ICoreManagement coreManagement = null;
	private IStorageService storageService = null;

	public void init() {
		coreManagement.addRunningMetricsManagerListener(this);
	}

	/**
	 * @param storageService
	 *            the storageService to set
	 */
	public void setStorageService(IStorageService storageService) {
		this.storageService = storageService;
	}

	@Override
	public void processEvent(ICapabilityEvent event) {
		storageService.storeMeasurement(event.getInstanceUri(), event.getCapabilityUri(), new Date(),
				event.getValue());
	}

	@Override
	public void notifyMetricValue(IConfiguredMetric metric, Number value) throws Exception {
		storageService.storeMetricValue(metric, value);
	}

	@Override
	public void notifyMetricsHasStopped(Collection<IConfiguredMetric> stoppedMetrics) {

		for (IConfiguredMetric metric : stoppedMetrics) {
			coreManagement.removeRunningMetricListener(metric, this);
		}
	}

	@Override
	public void notifyNewMetricsStarted(Collection<IConfiguredMetric> startedMetrics) {
		for (IConfiguredMetric metric : startedMetrics) {
			try {
				coreManagement.addRunningMetricListener(metric, this);
			} catch (MetricNotRunningException e) {
				logger.error("Exception!", e);
			}
		}

	}

	public void setCoreManagement(ICoreManagement coreManagement) {
		this.coreManagement = coreManagement;
	}

}
