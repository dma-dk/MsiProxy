package dk.dma.msiproxy.common.repo;

import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Will asynchronously load attachments and referenced files from a remote MSI provider.
 */
@Singleton
@Lock(LockType.READ)
public class RemoteAttachmentLoader {

    /**
     * Defined a worker pool size of 2 to constrain load
     */
    private static final int EXECUTOR_POOL_SIZE = 2;

    @Inject
    Logger log;

    private ExecutorService processPool;

    /**
     * Create the worker process pool
     */
    @PostConstruct
    private void init() {
        processPool = Executors.newFixedThreadPool(EXECUTOR_POOL_SIZE);
    }

    /**
     * Close down the the worker process pool
     */
    @PreDestroy
    private void closeDown() {
        if (processPool != null && !processPool.isShutdown()) {
            processPool.shutdown();
            processPool = null;
        }
    }

    /**
     * Loads the remote attachments asynchronously
     * @param attachments the attachments to load
     */
    public void loadRemoteAttachments(Collection<RemoteAttachment> attachments) {
        // Submit the attachments to the worked pool
        attachments.forEach(att ->
            processPool.submit(() -> loadRemoteAttachment(att))
        );
    }

    /**
     * Loads the remote attachment
     * @param att the attachment to load
     */
    private void loadRemoteAttachment(RemoteAttachment att) {
        long t0 = System.currentTimeMillis();
        try {
            // Create the directory if necessary
            if (!Files.exists(att.getLocalFileRepoPath().getParent())) {
                Files.createDirectories(att.getLocalFileRepoPath().getParent());
            }

            // Set up a few timeouts and fetch the attachment
            URLConnection con = new URL(att.getRemoteFileUrl()).openConnection();
            con.setConnectTimeout(5000); //  5 seconds
            con.setReadTimeout(10000);   // 10 seconds
            try (ReadableByteChannel rbc = Channels.newChannel(con.getInputStream());
                 FileOutputStream fos = new FileOutputStream(att.getLocalFileRepoPath().toFile()))  {
                fos.getChannel().transferFrom(rbc, 0, 1 << 24);
            }
            log.info(String.format("Copied %s -> %s in %d ms",
                    att.getRemoteFileUrl(),
                    att.getLocalFileRepoPath(),
                    System.currentTimeMillis() - t0));

        } catch (Exception e) {
            log.error("Failed loading attachment " + att.getRemoteFileUrl() + ": " + e.getMessage());
        }
    }

}
