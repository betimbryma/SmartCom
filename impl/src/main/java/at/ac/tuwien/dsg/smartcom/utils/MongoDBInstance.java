package at.ac.tuwien.dsg.smartcom.utils;

import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.extract.UserTempNaming;
import de.flapdoodle.embed.process.io.directories.FixedPath;
import de.flapdoodle.embed.process.runtime.Network;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class MongoDBInstance {
    private final int port;

    private MongodExecutable mongodExe;
    private MongodProcess mongod;

    public MongoDBInstance(int port) {
        if (port <= 0) {
            this.port = 12345;
        } else {
            this.port = port;
        }
    }

    public void setUp() throws IOException {
        Command command = Command.MongoD;

        File file = new File("mongo");
        if (file.exists()) {
            FileUtils.forceDelete(file);
        }
        file.mkdir();

        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                .defaults(command)
                .artifactStore(new ArtifactStoreBuilder()
                        .defaults(command)
                        .tempDir(new FixedPath("mongo"))
                        .download(new DownloadConfigBuilder()
                                .defaultsForCommand(command))
                        .executableNaming(new UserTempNaming()))
                .build();

        MongodStarter starter = MongodStarter.getInstance(runtimeConfig);

        IMongodConfig mongodConfig = new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(port, Network.localhostIsIPv6()))
                .build();

        mongodExe = starter.prepare(mongodConfig);
        mongod = mongodExe.start();
    }

    public void tearDown() {
        mongod.stop();
        mongodExe.stop();
    }

    public MongoClient getMongoClient() {
        try {
            return new MongoClient("localhost", port);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
