## Configuration file format ##

The configuration file is a plain XML file. It is deserialized through [XStream](http://xstream.codehaus.org/) library. The specification of deserialization can be found [here](https://code.google.com/p/sammanager/source/browse/base/fileconfig/src/main/java/pl/edu/agh/samm/fileconfig/XStreamFactory.java)

Configuration elements:
  * `<resourceSet>` - defines which resources will be monitored
    * `<resource>` - represents a single resource; `uri` attribute defines the unique URI of this resource in SAMM's structures
      * `<type>` - defines the type of resource as URI in SAMM's ontology (see [samm\_1.owl](https://code.google.com/p/sammanager/source/browse/base/knowledge/src/main/resources/samm_1.owl))
      * `<property>` - element defining resource properties (eg. communication properties like JMX url to connect to); there can be more than one such element

  * `<ruleSet>`
    * `<rule>` - rule specification; there can be many rules specified - each of them has to specify a name with use of the `name` attribute
      * `<resourceUri>` - URI of resource, which is going to be monitored (see URI specified in `<resource>` element) (**optional argument**)
      * `<resourceTypeUri>` - URI of type of resource, which is going to be monitored (URI in SAMM's ontology) (**optional argument**)
      * `<condition>` - condition which has to be met. The condition will be a part of an [EPL](http://esper.codehaus.org/esper-4.2.0/doc/reference/en/html/epl_clauses.html) statement evaluated by [Esper](http://esper.codehaus.org/) engine on the stream of metric values (**optional argument**)
      * `<metricUri>` - URI in SAMM's ontology of metric to observe (**optional argument**)
      * `<customStatement>` - a custom EPL statement which is going to be used for processing metrics values stream; when this element is specified, the `<metricUri>`, `<condition>`, `<resourceTypeUri>` and `<resourceUri>` are ignored (**optional argument**)
      * `<actionToExecute>` - specification of action to execute
        * `<actionURI>` - URI of action in SAMM's ontology
        * `<parameterValues>` - contains parameters used to execute the action (eg. connection specification)
          * `<entry>` - contains two `<string>` elements; first defines the key in properties map, second defines the value

  * `<metricSet>` - defines which metrics are going to be used for observation
    * `<metric>` - specifies a single metric to use; has three attributes:
      * `resourceURI` - which resource is being monitored
      * `metricURI` - URI of metric in SAMM's topology
      * `metricPollTimeInterval` - time between subsequent metric value polls (in milliseconds)

**Notice: because of problems with converting java.lang.Number in Esper the metric value variable (`value`) has to be explicitly casted to a double value, eg `cast(value,double)`**

## How `<rule>` elements are used to create an EPL statement? ##

Metrics' values in SAMM are processed as a stream of events with use of Esper CEP engine. There are two ways to specify the statement which is going to be used:
  * use the `<customStatement>` element - more flexible mechanism but more complicated to use
  * use the `<metricUri>`, `<condition>` and `<resourceUri>` - easier but limited

When You use the second method, SAMM fills the values into a template statement:

```
select metric, value from IMetricEvent(metric.resourceURI like <resourceUri> and metric.metricURI like <metricUri> and resourceType like <resourceTypeURI>) where <condition>
```

Each time SAMM's core notices an event, which satisfies the conditions of this statement, the action is going to be executed. Each rule specifies own statement.

Please note, that `<metricUri>`, `<condition>`, `<resourceTypeUri>` and `<resourceUri>` are _optional_ - if they are not passed, the action will be executed each time a new value of any metric enters the system.

## Sample configuration file ##

Sample configuration file is distributed within SAMM archive file. You can also find it in [git repository](https://code.google.com/p/sammanager/source/browse/base/fileconfig/src/main/resources/config_tutorial.xml)