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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.edu.agh.samm.common.core.IActionExecutionListener;
import pl.edu.agh.samm.common.core.IAlarmListener;
import pl.edu.agh.samm.common.core.ICoreManagement;
import pl.edu.agh.samm.common.core.ILearningStageListener;
import pl.edu.agh.samm.common.core.IResourceInstancesManager;
import pl.edu.agh.samm.common.core.IResourceListener;
import pl.edu.agh.samm.common.core.ResourceAlreadyRegisteredException;
import pl.edu.agh.samm.common.core.ResourceNotRegisteredException;
import pl.edu.agh.samm.common.core.SLAException;
import pl.edu.agh.samm.common.decision.IServiceLevelAgreement;
import pl.edu.agh.samm.common.impl.StringHelper;
import pl.edu.agh.samm.common.metrics.IConfiguredMetric;
import pl.edu.agh.samm.common.metrics.IMetricListener;
import pl.edu.agh.samm.common.metrics.IMetricsManagerListener;
import pl.edu.agh.samm.common.metrics.MetricNotRunningException;
import pl.edu.agh.samm.common.tadapter.IResourceDiscoveryEvent;
import pl.edu.agh.samm.common.tadapter.IResourceDiscoveryListener;
import pl.edu.agh.samm.common.tadapter.ResourceDiscoveryEventType;
import pl.edu.agh.samm.metrics.ICriteriaValidator;
import pl.edu.agh.samm.metrics.IMetricsManager;
import pl.edu.agh.samm.sla.ISLAValidator;

/**
 * The Core element of SAMM.
 * 
 * @author Pawel Koperek <pkoperek@gmail.com>
 * @author Mateusz Kupisz <mkupisz@gmail.com>
 * 
 */
public class CoreManagementImpl implements IResourceDiscoveryListener, ICoreManagement {

	private static final Logger logger = LoggerFactory.getLogger(CoreManagementImpl.class);

	private Map<IResourceListener, IResourceListener> resourceListenersProxies = new HashMap<IResourceListener, IResourceListener>();
	private IResourceInstancesManager resourceInstancesManager = null;
	private IMetricsManager runningMetricsManager = null;
	private IResourceDiscoveryAgent resourceDiscoveryAgent = null;
	private IMetricFactory metricFactory = null;
	private ISLAValidator slaValidator = null;
	private ICriteriaValidator knowledgeAlarmsMetricListener = null;
	private IServiceLevelAgreement serviceLevelAgreement = null;
	private ICurrentCostEvaluator currentCostEvaluator = null;
	private IExperimentator experimentator = null;
	private boolean slaValidationRunning = false;
	private IDecisionMaker decisionMaker = null;
	private IActionExecutor actionExecutor = null;

	/**
	 * @return the actionExecutor
	 */
	public IActionExecutor getActionExecutor() {
		return actionExecutor;
	}

	/**
	 * @param actionExecutor
	 *            the actionExecutor to set
	 */
	public void setActionExecutor(IActionExecutor actionExecutor) {
		this.actionExecutor = actionExecutor;
	}

	/**
	 * @return the knowledgeAlarmsMetricListener
	 */
	public ICriteriaValidator getKnowledgeAlarmsMetricListener() {
		return knowledgeAlarmsMetricListener;
	}

	/**
	 * @return the decisionMaker
	 */
	public IDecisionMaker getDecisionMaker() {
		return decisionMaker;
	}

	/**
	 * @param decisionMaker
	 *            the decisionMaker to set
	 */
	public void setDecisionMaker(IDecisionMaker decisionMaker) {
		this.decisionMaker = decisionMaker;
	}

	/**
	 * @param knowledgeAlarmsMetricListener
	 *            the knowledgeAlarmsMetricListener to set
	 */
	public void setKnowledgeAlarmsMetricListener(ICriteriaValidator knowledgeAlarmsMetricListener) {
		this.knowledgeAlarmsMetricListener = knowledgeAlarmsMetricListener;
	}

	public IResourceDiscoveryAgent getResourceDiscoveryAgent() {
		return resourceDiscoveryAgent;
	}

	public IMetricFactory getMetricFactory() {
		return metricFactory;
	}

	public void setMetricFactory(IMetricFactory metricFactory) {
		this.metricFactory = metricFactory;
	}

	public void setResourceDiscoveryAgent(IResourceDiscoveryAgent resourceDiscoveryAgent) {
		this.resourceDiscoveryAgent = resourceDiscoveryAgent;
	}

	public IMetricsManager getRunningMetricsManager() {
		return runningMetricsManager;
	}

	public void setRunningMetricsManager(IMetricsManager runningMetricsManager) {
		this.runningMetricsManager = runningMetricsManager;
	}

	@Override
	public void addResourceListener(IResourceListener listener) {
		IResourceListener proxyListener = new ProxyResourceListener(listener);
		resourceListenersProxies.put(listener, proxyListener);
		resourceInstancesManager.addResourceListener(proxyListener);

		// send all currently available resources
		for (String resource : resourceInstancesManager.getAllRegisteredResources()) {
			try {
				listener.processEvent(new InitialResourceEvent(resource));
			} catch (Exception e) {
				logger.error("Error initial sending of available resources!", e);
			}
		}
	}

	@Override
	public void removeResourceListener(IResourceListener listener) {
		IResourceListener proxyListener = resourceListenersProxies.get(listener);
		if (proxyListener != null) {
			resourceInstancesManager.removeResourceListener(listener);
		}
	}

	public IResourceInstancesManager getResourceInstancesManager() {
		return resourceInstancesManager;
	}

	public void setResourceInstancesManager(IResourceInstancesManager resourceInstancesManager) {
		this.resourceInstancesManager = resourceInstancesManager;
	}

	public void setSlaValidator(ISLAValidator slaValidator) {
		this.slaValidator = slaValidator;
	}

	public CoreManagementImpl() {
		logger.info("Core Bean created...");
	}

	/**
	 * Initializes Core
	 */
	public void init() {
		logger.info("Core Bean initialization");
		this.resourceInstancesManager.addResourceListener(resourceDiscoveryAgent);
	}

	/**
	 * Destroys Core
	 */
	public void destroy() {
		this.resourceInstancesManager.removeResourceListener(resourceDiscoveryAgent);
		logger.info("Core Bean destroyed");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.edu.agh.samm.common.impl.core.ICoreManagement#startMetric(pl.edu
	 * .agh. samm.common.metrics.IConfiguredMetric)
	 */
	@Override
	public void startMetric(IConfiguredMetric runningMetric) {
		this.runningMetricsManager.startMetric(runningMetric);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.edu.agh.samm.common.impl.core.ICoreManagement#stopMetric(pl.edu.
	 * agh.samm .common.metrics.IRunningMetric)
	 */
	@Override
	public void stopMetric(IConfiguredMetric metric) {
		this.runningMetricsManager.stopMetric(metric);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pl.edu.agh.samm.common.impl.core.ICoreManagement#addRunningMetricListener
	 * (pl.edu.agh.samm.common.impl.metrics.IMetric,
	 * pl.edu.agh.samm.common.impl.metrics.IMetricListener)
	 */
	@Override
	public void addRunningMetricListener(IConfiguredMetric metric, IMetricListener listener)
			throws MetricNotRunningException {
		this.runningMetricsManager.addMetricListener(metric, listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seepl.edu.agh.samm.common.core.ICoreManagement#
	 * addRunningMetricsManagerListener
	 * (pl.edu.agh.samm.common.impl.metrics.IMetricsManagerListener)
	 */
	@Override
	public void addRunningMetricsManagerListener(IMetricsManagerListener listener) {
		this.runningMetricsManager.addMetricsManagerListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seepl.edu.agh.samm.common.impl.core.ICoreManagement#
	 * removeRunningMetricListener (pl.edu.agh.samm.common.impl.metrics.IMetric,
	 * pl.edu.agh.samm.common.impl.metrics.IMetricListener)
	 */
	@Override
	public void removeRunningMetricListener(IConfiguredMetric metric, IMetricListener listener) {
		this.runningMetricsManager.removeMetricListener(metric, listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seepl.edu.agh.samm.common.core.ICoreManagement#
	 * removeRunningMetricsManagerListener
	 * (pl.edu.agh.samm.common.impl.metrics.IMetricsManagerListener)
	 */
	@Override
	public void removeRunningMetricsManagerListener(IMetricsManagerListener listener) {
		this.runningMetricsManager.removeMetricsManagerListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seepl.edu.agh.samm.common.core.ICoreManagement#
	 * startMetricAndAddRunningMetricListener
	 * (pl.edu.agh.samm.common.impl.metrics.IMetric, java.util.Collection)
	 */
	@Override
	public void startMetricAndAddRunningMetricListener(IConfiguredMetric runningMetric,
			Collection<IMetricListener> listeners) {
		this.runningMetricsManager.startMetricAndAddRunningMetricListener(runningMetric, listeners);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seepl.edu.agh.samm.common.core.ICoreManagement#
	 * startMetricAndAddRunningMetricListener
	 * (pl.edu.agh.samm.common.impl.metrics.IMetric,
	 * pl.edu.agh.samm.common.impl.metrics.IMetricListener)
	 */
	@Override
	public void startMetricAndAddRunningMetricListener(IConfiguredMetric runningMetric,
			IMetricListener listener) {
		if (listener != null) {
			Collection<IMetricListener> listeners = new LinkedList<IMetricListener>();
			listeners.add(listener);
			startMetricAndAddRunningMetricListener(runningMetric, listeners);
		}
	}

	@Override
	public boolean isMetricRunning(IConfiguredMetric metric) {
		return runningMetricsManager.isMetricRunning(metric);
	}

	@Override
	public boolean isResourceRegistered(String uri) {
		return this.resourceInstancesManager.isResourceRegistered(uri);
	}

	@Override
	public Collection<String> getAllRegisteredResources() {
		return resourceInstancesManager.getAllRegisteredResources();
	}

	@Override
	public String getResourceType(String uri) {
		return resourceInstancesManager.getResourceType(uri);
	}

	@Override
	public void registerResource(String uri, String type, Map<String, Object> parameters)
			throws ResourceAlreadyRegisteredException {
		logger.info("Registering uri: " + uri);

		String parentURI = StringHelper.getParentURI(uri);
		boolean resourceRegistered = this.resourceInstancesManager.isResourceRegistered(parentURI);
		if (resourceRegistered) {
			try {
				this.resourceInstancesManager.addChildResource(parentURI, uri, type, parameters);
			} catch (ResourceNotRegisteredException e) {
				logger.error("Resource " + parentURI + " was claimed to be registered, not found during "
						+ uri + " registration.", e);
			}
		} else {
			this.resourceInstancesManager.addResource(uri, type, parameters);
		}
	}

	@Override
	public void addResourceParameters(String uri, Map<String, Object> parameters)
			throws ResourceNotRegisteredException {
		if (!this.resourceInstancesManager.isResourceRegistered(uri)) {
			throw new ResourceNotRegisteredException(uri);
		}
		resourceInstancesManager.addResourceParameters(uri, parameters);
	}

	@Override
	public void processEvent(IResourceDiscoveryEvent event) {
		if (event.getEventType().equals(ResourceDiscoveryEventType.NEW_RESOURCES_DISCOVERED)) {
			String parentUri = event.getParentResourceURI();
			List<String> resources = event.getResources();
			try {
				for (String resource : resources) {
					this.resourceInstancesManager.addChildResource(parentUri, resource, event
							.getResourcesTypes().get(resource), event.getResourcesProperties().get(resource));
					startMetricsForNewResource(resource);
				}
			} catch (ResourceNotRegisteredException e) {
				logger.error("Event contains parent URI which is not registered!", e);
			}
		}
	}

	private void startMetricsForNewResource(String resourceURI) {
		if (isSLAValidationRunning()) {
			for (String pattern : serviceLevelAgreement.getInvolvedPatterns()) {
				if (Pattern.matches(pattern, resourceURI)) {
					List<String> metrics = serviceLevelAgreement.getMetricsForResource(pattern);

					for (String metricURI : metrics) {
						IConfiguredMetric configuredMetric = this.createRunningMetricInstance(metricURI,
								resourceURI);
						List<IMetricListener> listeners = new LinkedList<IMetricListener>();
						listeners.add(currentCostEvaluator);
						listeners.add(slaValidator);
						this.startMetricAndAddRunningMetricListener(configuredMetric, listeners);
					}
				}
			}
		}

	}

	@Override
	public IConfiguredMetric createRunningMetricInstance(String metricURI, String resourceURI) {
		return metricFactory.createMetric(metricURI, resourceURI);
	}

	@Override
	public List<String> getResourceCapabilities(String uri) throws ResourceNotRegisteredException {
		return this.resourceInstancesManager.getResourceCapabilities(uri);
	}

	@Override
	public void updateMetricPollTimeInterval(IConfiguredMetric metric) throws MetricNotRunningException {
		runningMetricsManager.updateMetricPollTime(metric);
	}

	@Override
	public void addAlarmListener(IAlarmListener listener) {
		knowledgeAlarmsMetricListener.addAlarmListener(listener);
		slaValidator.addAlarmListener(listener);
	}

	@Override
	public void removeAlarmListener(IAlarmListener listener) {
		knowledgeAlarmsMetricListener.removeAlarmListener(listener);
		slaValidator.removeAlarmListener(listener);
	}

	@Override
	public void startSLAValidation(IServiceLevelAgreement serviceLevelAgreement) throws SLAException {
		if (slaValidationRunning) {
			throw new SLAException("SLA Validation already running! Please update instead of starting!");
		}

		logger.info("Starting SLA Validation: " + serviceLevelAgreement);
		this.slaValidationRunning = true;
		this.serviceLevelAgreement = serviceLevelAgreement;

		configureMonitoringForSLA(serviceLevelAgreement);
		logger.info("SLA Validation started");
	}

	@Override
	public void updateSLA(IServiceLevelAgreement serviceLevelAgreement) {
		logger.info("Updating SLA: " + serviceLevelAgreement);
		if (this.slaValidationRunning) {
			unconfigureMonitoringForSLA(this.serviceLevelAgreement);
		} else {
			logger.info("No configured SLA: configuring received one");
		}
		configureMonitoringForSLA(serviceLevelAgreement);
		logger.info("SLA updated");
	}

	/**
	 * Automatic configuration of SLA
	 * 
	 * @param serviceLevelAgreement
	 */
	private void configureMonitoringForSLA(IServiceLevelAgreement serviceLevelAgreement) {
		this.currentCostEvaluator.setupSLA(serviceLevelAgreement);
		this.slaValidator.setupSLA(serviceLevelAgreement);
		this.decisionMaker.setupSLA(serviceLevelAgreement);
		for (String pattern : serviceLevelAgreement.getInvolvedPatterns()) {

			for (String resourceURI : resourceInstancesManager.getAllRegisteredResources()) {
				if (Pattern.matches(pattern, resourceURI)) {
					String resourceType = serviceLevelAgreement.getResourceType(pattern);
					Map<String, Object> parameters = serviceLevelAgreement.getParameters(pattern);

					try {
						this.registerResource(resourceURI, resourceType, parameters);
					} catch (ResourceAlreadyRegisteredException e) {
						// if we already monitor this resource - nothing
						// happens...
						// try to add metrics for it...
					}

					List<String> metrics = serviceLevelAgreement.getMetricsForResource(pattern);

					for (String metricURI : metrics) {
						IConfiguredMetric configuredMetric = this.createRunningMetricInstance(metricURI,
								resourceURI);
						List<IMetricListener> listeners = new LinkedList<IMetricListener>();
						listeners.add(currentCostEvaluator);
						listeners.add(slaValidator);
						this.startMetricAndAddRunningMetricListener(configuredMetric, listeners);
					}
				}
			}
		}
	}

	private void unconfigureMonitoringForSLA(IServiceLevelAgreement serviceLevelAgreement) {
		this.currentCostEvaluator.setupSLA(null);
		this.slaValidator.setupSLA(null);
		this.decisionMaker.setupSLA(null);
		for (String pattern : serviceLevelAgreement.getInvolvedPatterns()) {

			for (String resourceURI : resourceInstancesManager.getAllRegisteredResources()) {
				if (Pattern.matches(pattern, resourceURI)) {
					List<String> metrics = serviceLevelAgreement.getMetricsForResource(pattern);
					if (metrics == null) {
						continue;
					}
					for (String metricURI : metrics) {
						IConfiguredMetric configuredMetric = this.createRunningMetricInstance(metricURI,
								resourceURI);
						this.runningMetricsManager.removeMetricListener(configuredMetric,
								currentCostEvaluator);
						this.runningMetricsManager.removeMetricListener(configuredMetric, slaValidator);
						this.stopMetric(configuredMetric);
					}
				}
			}

		}

	}

	@Override
	public void stopSLAValidation() throws SLAException {
		if (slaValidationRunning) {
			this.unconfigureMonitoringForSLA(serviceLevelAgreement);
			this.slaValidationRunning = false;
		} else {
			throw new SLAException("SLA Validation not running! Please start SLA validation first!");
		}
	}

	@Override
	public boolean isSLAValidationRunning() {
		return this.slaValidationRunning;
	}

	@Override
	public void unregisterResource(String uri) {
		this.resourceInstancesManager.removeResource(uri);
	}

	/**
	 * @return the currentCostEvaluator
	 */
	public ICurrentCostEvaluator getCurrentCostEvaluator() {
		return currentCostEvaluator;
	}

	/**
	 * @param currentCostEvaluator
	 *            the currentCostEvaluator to set
	 */
	public void setCurrentCostEvaluator(ICurrentCostEvaluator currentCostEvaluator) {
		this.currentCostEvaluator = currentCostEvaluator;
	}

	@Override
	public void startLearning(ILearningStageListener finishListener) {
		experimentator.executeInitialLearning(finishListener);
	}

	/**
	 * @param experimentator
	 *            the experimentator to set
	 */
	public void setExperimentator(IExperimentator experimentator) {
		this.experimentator = experimentator;
	}

	@Override
	public IServiceLevelAgreement retrieveCurrentSLA() throws SLAException {
		if (!slaValidationRunning) {
			throw new SLAException("No SLA set!");
		}
		return this.serviceLevelAgreement;
	}

	@Override
	public void addActionExecutorListener(IActionExecutionListener listener) {
		this.actionExecutor.addActionExecutorListener(listener);
	}

	@Override
	public void removeActionExecutorListener(IActionExecutionListener listener) {
		this.actionExecutor.removeActionExecutorListener(listener);
	}
}
