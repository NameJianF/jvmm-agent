package live.itrip.jvmm.agent.server;

import live.itrip.jvmm.agent.server.entity.conf.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * description: TODO
 * date 15:36 2023/2/1
 * @author fengjianfeng
 */
public class ServerApplication {

    private static Configuration loadConf(String[] args) throws IOException {
        if (args.length > 0) {
            File file = new File(args[0]);
            if (file.exists()) {
                return Configuration.parseFromUrl(args[0]);
            } else {
                System.err.println("Config file not exists: " + args[0]);
                System.exit(-1);
            }
        } else {
            InputStream is = ServerApplication.class.getResourceAsStream("/config.yml");
            if (is == null) {
                File file = new File(System.getProperty("user.dir") + "/config.yml");
                if (file.exists()) {
                    return Configuration.parseFromJsonFile(file);
                } else {
                    file = new File(System.getProperty("user.dir") + "/config/config.yml");
                    if (file.exists()) {
                        return Configuration.parseFromJsonFile(file);
                    }
                }
            } else {
                return Configuration.parseFromStream(is);
            }
        }
        return null;
    }

    public static void main(String[] args) throws Throwable {
        ServerBootstrap.getInstance(loadConf(args)).start();
    }
}
