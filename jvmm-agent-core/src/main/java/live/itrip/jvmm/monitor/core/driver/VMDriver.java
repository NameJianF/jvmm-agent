package live.itrip.jvmm.monitor.core.driver;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import live.itrip.jvmm.logging.AgentLogFactory;
import live.itrip.jvmm.util.*;
import sun.tools.attach.HotSpotVirtualMachine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 15:30 2021/5/17
 *
 * @author fengjianfeng
 */
public class VMDriver {

    private static final Logger log = AgentLogFactory.getLogger(VMDriver.class);

    private static volatile VMDriver INSTANCE;

    private volatile ClassLoader toolsClassLoader;

    private VMDriver() throws Throwable {
        initToolsClassLoader();
    }

    public static synchronized VMDriver get() throws Throwable {
        if (INSTANCE == null) {
            INSTANCE = new VMDriver();
        }
        return INSTANCE;
    }

    protected synchronized void initToolsClassLoader() throws Throwable {
        if (toolsClassLoader == null) {
            String javaHome = JavaEnvUtil.findJavaHome();
            File toolsJar = JavaEnvUtil.findToolsJar(javaHome);

            if (JavaVersionUtils.isLessThanJava9()) {
                if (toolsJar == null || !toolsJar.exists()) {
                    throw new IllegalArgumentException("Can not find tools.jar under java home: " + javaHome);
                }
                try {
                    toolsClassLoader = ClassLoaderUtil.systemLoadJar(toolsJar.toURI().toURL());
                    //log.debug("Init tools classes successful.");
                } catch (MalformedURLException e) {
                    //  ignored
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                   // log.error("Init class loader failed. " + e.getMessage(), e);
                    throw e;
                }
            } else {
                toolsClassLoader = ClassLoader.getSystemClassLoader();
            }
        }
    }

    public void attachAgent(long targetPid, String agentJarPath, String serverJarPath, String args) throws Exception {
        if (targetPid <= 0) {
            throw new IllegalArgumentException("Can not attach to virtual machine with illegal pid " + targetPid);
        }
        VirtualMachineDescriptor virtualMachineDescriptor = null;
        for (VirtualMachineDescriptor descriptor : VirtualMachine.list()) {
            String pid = descriptor.id();
            if (pid.equals(Long.toString(targetPid))) {
                virtualMachineDescriptor = descriptor;
                break;
            }
        }
        VirtualMachine virtualMachine = null;
        try {
            if (null == virtualMachineDescriptor) {
                virtualMachine = VirtualMachine.attach(Long.toString(targetPid));
            } else {
                virtualMachine = VirtualMachine.attach(virtualMachineDescriptor);
            }

            Properties targetSystemProperties = virtualMachine.getSystemProperties();
            String targetJavaVersion = JavaVersionUtils.javaVersionStr(targetSystemProperties);
            String currentJavaVersion = JavaVersionUtils.javaVersionStr();
            if (targetJavaVersion != null && currentJavaVersion != null) {
                if (!targetJavaVersion.equals(currentJavaVersion)) {
//                    log.warn("Current VM java version: {} do not match target VM java version: {}, attach may fail.",
//                            currentJavaVersion, targetJavaVersion);
//                    log.warn("Target VM JAVA_HOME is {}, jvmm-server JAVA_HOME is {}, try to set the same JAVA_HOME.",
//                            targetSystemProperties.getProperty("java.home"), System.getProperty("java.home"));
                }
            }
            virtualMachine.loadAgent(agentJarPath.replaceAll("\\\\", "/"),
                    serverJarPath.replaceAll("\\\\", "/") + ";" + args);
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Non-numeric value found")) {
                // log.warn("An exception occurred when attaching: {}", e.getMessage());
            }
        } finally {
            if (virtualMachine != null) {
                virtualMachine.detach();
            }
        }
    }

    public String heapHisto() throws Exception {
        HotSpotVirtualMachine vm = (HotSpotVirtualMachine) VirtualMachine.attach(Long.toString(PidUtil.currentPid()));
        try {
            InputStream is = vm.heapHisto("-live");
            return IOUtil.toString(is);
        } finally {
            vm.detach();
        }
    }
}
