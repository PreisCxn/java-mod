package de.alive.pricecxn.networking.cdn;

import de.alive.api.networking.cdn.CdnFileHandler;
import de.alive.pricecxn.networking.HttpImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CdnFileHandlerImplTest {
    @Mock
    private HttpImpl http;

    private CdnFileHandler cdnFileHandler;
    private AutoCloseable closeable;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        cdnFileHandler = new CdnFileHandlerImpl(http);
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void getModVersions_returnsExpectedVersions() {
        when(http.GET(anyString(), anyString())).thenReturn(Mono.just("[\"1.0\", \"1.1\", \"1.2\"]"));

        StepVerifier.create(cdnFileHandler.getVersions("mod"))
                .expectNext(List.of("1.0", "1.1", "1.2"))
                .verifyComplete();
    }

    @Test
    public void getModVersions_returnsEmptyListWhenNoVersions() {
        when(http.GET(anyString(), anyString())).thenReturn(Mono.just("[]"));

        StepVerifier.create(cdnFileHandler.getVersions("mod"))
                .expectNext(List.of())
                .verifyComplete();
    }

    @Test
    public void getNewestVersion_returnsExpectedVersion() {
        when(http.GET(anyString(), anyString())).thenReturn(Mono.just( "1.2"));

        StepVerifier.create(cdnFileHandler.getNewestVersion("mod"))
                .expectNext("1.2")
                .verifyComplete();
    }

    @Test
    public void getNewestVersion_returnsEmptyStringWhenNoVersion() {
        when(http.GET(anyString(), anyString())).thenReturn(Mono.just(""));

        StepVerifier.create(cdnFileHandler.getNewestVersion("mod"))
                .expectNext("")
                .verifyComplete();
    }

    @Test
    public void getHash_returnsExpectedHash() {
        String expectedHash = "abc123";
        when(http.GET(anyString(), anyString())).thenReturn(Mono.just(expectedHash));

        StepVerifier.create(cdnFileHandler.getHash("file", "version"))
                .expectNext(expectedHash)
                .verifyComplete();
    }

    @Test
    public void getFiles_returnsExpectedFiles() {
        String expectedFilesJson = "[\"file1\", \"file2\", \"file3\"]";
        when(http.GET(anyString(), anyString())).thenReturn(Mono.just(expectedFilesJson));

        StepVerifier.create(cdnFileHandler.getFiles("prefix"))
                .expectNext(List.of("file1", "file2", "file3"))
                .verifyComplete();
    }

    @Test
    public void getFile_returnsExpectedFile() {
        byte[] expectedFile = new byte[] {1, 2, 3};
        when(http.getBytes(anyString(), anyString())).thenReturn(Mono.just(expectedFile));

        StepVerifier.create(cdnFileHandler.getFile("file", "version"))
                .expectNext(expectedFile)
                .verifyComplete();
    }
}