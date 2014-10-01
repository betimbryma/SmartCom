package at.ac.tuwien.dsg.smartcom.utils;

import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.exceptions.DistributionException;
import de.flapdoodle.embed.process.extract.UserTempNaming;
import de.flapdoodle.embed.process.io.directories.FixedPath;
import de.flapdoodle.embed.process.io.progress.IProgressListener;
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
    private static final int MAX_START_TRIES = 5;
    private static MongodStarter starter;
    private final int port;

    public MongoDBInstance(int port) {
        if (port <= 0) {
            this.port = 12345;
        } else {
            this.port = port;
        }
    }

    public MongoDBInstance() {
        this.port = 12345;
    }

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
                                .defaultsForCommand(command)
                                .progressListener(new IProgressListener() {

                                    @Override
                                    public void start(String label) {
                                    }

                                    @Override
                                    public void progress(String label, int percent) {
                                    }

                                    @Override
                                    public void info(String label, String message) {
                                    }

                                    @Override
                                    public void done(String label) {
                                    }
                                }))
                        .executableNaming(new UserTempNaming()))
                .processOutput(ProcessOutput.getDefaultInstanceSilent())
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

        boolean started = false;
        for (int i = 0; i < MAX_START_TRIES && !started; i++) {
            try {
                mongodExe = starter.prepare(mongodConfig);
                mongod = mongodExe.start();
                started = true;
            } catch (IOException | DistributionException e) {
                System.err.println("MongoDB failed to start (" + e.getLocalizedMessage() + ")... retrying");
                setUpStatic();
            }
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