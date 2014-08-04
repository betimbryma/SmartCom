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
    private static MongodStarter starter;
    private static final int port = 12345;

    //do this statically otherwise tests might behave unexpectedly
    static {
        setUpStatic();
    }

    private static void setUpStatic() {
        Command command = Command.MongoD;

        File file = new File("mongo");
        if (file.exists()) {
            try {
                FileUtils.forceDelete(file);
            } catch (IOException e) {/* nothing to do */}
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
        try {
            mongod = mongodExe.start();
        } catch (IOException e) {
            System.err.println("MongoDB failed to start ("+e.getLocalizedMessage()+")... retrying");
            setUpStatic();
            mongodExe = starter.prepare(mongodConfig);
            mongod = mongodExe.start();
        }
    }

    public MongoClient getClient() throws UnknownHostException {
        return new MongoClient("localhost", port);
    }

    public void tearDown() {
        mongod.stop();
        mongodExe.stop();
    }
}
