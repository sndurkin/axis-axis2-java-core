package org.apache.axis2.context;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.FileSystemConfigurationCreator;
import org.apache.axis2.description.ModuleDescription;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurationCreator;
import org.apache.axis2.modules.Module;
import org.apache.axis2.phaseresolver.PhaseException;
import org.apache.axis2.phaseresolver.PhaseResolver;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.TransportSender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class ConfigurationContextFactory {
    private Log log = LogFactory.getLog(getClass());

    /**
     * To create a AxisConfiguration depending on the user requiremnt , this method can be used.
     * First create a AxisConfigurationCreator object giving necessary parameters into it.
     * Depending on the implementation getAxisConfiguration(); will give the AxisConfiguration and
     * using the ConfigurationContext will be created and return that.
     *
     * @param axisConfigurationCreator
     * @return
     * @throws AxisFault
     */
    public ConfigurationContext getConfigurationContext(
            AxisConfigurationCreator axisConfigurationCreator) throws AxisFault {
        AxisConfiguration axisConfig = axisConfigurationCreator.getAxisConfiguration();
        ConfigurationContext configContext = new ConfigurationContext(axisConfig);
        init(configContext);
        return configContext;
    }

    /**
     * Builds the configuration for the client.
     *
     * @param axis2home the value can be null and resolves to the default axis2.xml file
     * @return Returns ConfigurationContext.
     * @throws DeploymentException
     */
    public ConfigurationContext buildClientConfigurationContext(String axis2home) throws AxisFault {
        if (axis2home == null) {
            // if user has set the axis2 home variable try to get that from System properties
            axis2home = System.getProperty(Constants.AXIS2_HOME);
        }
        AxisConfigurationCreator repoBasedConfigCreator =
                new FileSystemConfigurationCreator(axis2home, false);
        return getConfigurationContext(repoBasedConfigCreator);
    }

    /**
     * Builds the configuration for the Server.
     *
     * @param repositoryName
     * @return Returns the built ConfigurationContext.
     * @throws DeploymentException
     */
    public ConfigurationContext buildConfigurationContext(String repositoryName) throws AxisFault {
        AxisConfigurationCreator repoBasedConfigCreator =
                new FileSystemConfigurationCreator(repositoryName, true);
        return getConfigurationContext(repoBasedConfigCreator);

    }

    /**
     * To initilizae modules and , create Tranpsorts, this method is bean used
     */
    private void init(ConfigurationContext configContext) throws AxisFault {
        try {
            PhaseResolver phaseResolver = new PhaseResolver(configContext.getAxisConfiguration());

            phaseResolver.buildTranspotsChains();
            initModules(configContext);
            initTransports(configContext);
        } catch (PhaseException e) {
            throw new AxisFault(e);
        } catch (DeploymentException e) {
            throw new AxisFault(e);
        }
    }

    /**
     * Initializes the modules. If the module needs to perform some recovery process
     * it can do so in init and this is different from module.engage().
     *
     * @param context
     * @throws DeploymentException
     */
    private void initModules(ConfigurationContext context) throws DeploymentException {
        try {
            HashMap modules = context.getAxisConfiguration().getModules();
            Collection col = modules.values();

            for (Iterator iterator = col.iterator(); iterator.hasNext();) {
                ModuleDescription axismodule = (ModuleDescription) iterator.next();
                Module module = axismodule.getModule();

                if (module != null) {
                    module.init(context.getAxisConfiguration());
                }
            }
        } catch (AxisFault e) {
            throw new DeploymentException(e);
        }
    }

    /**
     * Initializes TransportSenders and TransportListeners with appropriate configuration information
     *
     * @param configContext
     */
    public void initTransports(ConfigurationContext configContext) {
        AxisConfiguration axisConf = configContext.getAxisConfiguration();

        // Initialize Transport Ins
        HashMap transportIns = axisConf.getTransportsIn();
        Iterator values = transportIns.values().iterator();

        while (values.hasNext()) {
            TransportInDescription transportIn = (TransportInDescription) values.next();
            TransportListener listener = transportIn.getReceiver();

            if (listener != null) {
                try {
                    listener.init(configContext, transportIn);
                } catch (AxisFault axisFault) {
                    log.info("Transport-IN initialization error : "
                            + transportIn.getName().getLocalPart());
                }
            }
        }

        // Initialize Transport Outs
        HashMap transportOuts = axisConf.getTransportsOut();

        values = transportOuts.values().iterator();

        while (values.hasNext()) {
            TransportOutDescription transportOut = (TransportOutDescription) values.next();
            TransportSender sender = transportOut.getSender();

            if (sender != null) {
                try {
                    sender.init(configContext, transportOut);
                } catch (AxisFault axisFault) {
                    log.info("Transport-OUT initialization error : "
                            + transportOut.getName().getLocalPart());
                }
            }
        }
    }

    /**
     * To get the default configuration context  , this will return a AxisConfiguration
     * which is created by fileSystem based AxisConfiguration creator
     *
     * @return ConfigurationContext
     */
    public ConfigurationContext getDafaultConfigurationContext() {
        AxisConfiguration axisConfig = new AxisConfiguration();

        return new ConfigurationContext(axisConfig);
    }
}
