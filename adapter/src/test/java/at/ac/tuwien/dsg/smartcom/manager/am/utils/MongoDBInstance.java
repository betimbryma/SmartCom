package at.ac.tuwien.dsg.smartcom.manager.am.utils;

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

import java.io.File;
import java.io.IOException;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class MongoDBInstance {
    private static final MongodStarter starter;
    private static final int port = 12345;

    static {
        Command command = Command.MongoD;

        File file = new File("mongo");
        if (file.exists()) {
            file.setWritable(true);
            file.delete();
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

        starter = MongodStarter.getInstance(runtimeConfig);
    }

    private MongodExecutable mongodExe;
    private MongodProcess mongod;

    public void setUp() throws IOException {
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
}
