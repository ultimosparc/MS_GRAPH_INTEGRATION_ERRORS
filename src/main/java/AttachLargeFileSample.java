import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.models.AttachmentCreateUploadSessionParameterSet;
import com.microsoft.graph.models.AttachmentItem;
import com.microsoft.graph.models.AttachmentType;
import com.microsoft.graph.models.UploadSession;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.tasks.IProgressCallback;
import com.microsoft.graph.tasks.LargeFileUploadTask;
import okhttp3.Request;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class AttachLargeFileSample implements SampleClass {

    public static final int SMALL_ATTACHMENT_BOUNDARY = 3145728;
    public static final int BIG_ATTACHMENT_BOUNDARY = 157286400;

    @Override
    public void run() throws IOException {

        final String primaryUser = "office365userEmail";
        final String eventID = "eventID";

        final GraphServiceClient<Request> graphClient =
                GraphServiceClient
                        .builder()
                        .authenticationProvider(getAuthenticationProvider())
                        .buildClient();

        final File file = File.createTempFile("testFile", "txt");
        //FileUtils.writeByteArrayToFile(file, "testContent".getBytes());
        InputStream fileStream = new FileInputStream(file);

        final AttachmentItem attachmentItem = new AttachmentItem();
        attachmentItem.attachmentType = AttachmentType.FILE;
        attachmentItem.name = file.getName();
        attachmentItem.size = file.getTotalSpace();

        final AttachmentCreateUploadSessionParameterSet attachmentCreateUploadSessionParameterSet =
                AttachmentCreateUploadSessionParameterSet.newBuilder()
                        .withAttachmentItem(attachmentItem)
                        .build();

        final UploadSession uploadSession = graphClient
                    .users(primaryUser)
                    .events(eventID)
                    .attachments()
                    .createUploadSession(attachmentCreateUploadSessionParameterSet)
                    .buildRequest()
                    .post();

        // Called after each slice of the file is uploaded
        final IProgressCallback callback =
                (current, max) -> System.out.println("Uploaded "
                        + current + " bytes of " + max +
                        " total bytes");

        final LargeFileUploadTask<AttachmentItem> uploadTask =
                new LargeFileUploadTask<>(uploadSession, graphClient,
                        fileStream,
                        file.length(), AttachmentItem.class);

        // upload (default: chunkSize is 5 MB)
        uploadTask.upload(0, null, callback);

    }

    @Override
    public String getToken() {
        return null;
    }

    @Override
    public IAuthenticationProvider getAuthenticationProvider() {
        return new IAuthenticationProvider() {
            private String hostNameToCheck = "graph";
            @Override
            public CompletableFuture<String> getAuthorizationTokenAsync(URL requestUrl) {
                CompletableFuture<String> future = new CompletableFuture<>();
                if(requestUrl.getHost().toLowerCase().contains(hostNameToCheck)){
                    future.complete(getToken());
                } else{
                    future.complete(null);
                }
                return future;
            }
        };
    }

}