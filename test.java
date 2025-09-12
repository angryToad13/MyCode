import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.env.Environment;

public class TracerAdviceServiceImplTest {

    @InjectMocks
    private TracerAdviceServiceImpl tracerAdviceService;

    @Mock
    private Environment env;

    @Mock
    private RequestApi requestApi;

    @Mock
    private ApigeeDocumentService apigeeDocumentService;

    @Mock
    private DocumentUtil documentUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tracerAdviceService = new TracerAdviceServiceImpl(env, requestApi, apigeeDocumentService, documentUtil);
        ReflectionTestUtils.setField(tracerAdviceService, "tracerAdvices", Arrays.asList("CATEGORY1", "CATEGORY2"));
        ReflectionTestUtils.setField(tracerAdviceService, "technicalUser", "techUser");
    }

    @Test
    void testIntegrateTracerAdvices_withBranches() {
        when(env.getProperty("uv.branches.+" + "GROUP1")).thenReturn("branch1,branch2");

        StringResponse response = tracerAdviceService.integrateTracerAdvices("GROUP1");

        assertEquals("Integrated tracer advices successfully", response.getMessage());
    }

    @Test
    void testIntegrateTracerAdvices_noBranches() {
        when(env.getProperty("uv.branches.+" + "GROUP1")).thenReturn(null);

        StringResponse response = tracerAdviceService.integrateTracerAdvices("GROUP1");

        assertEquals("Integrated tracer advices successfully", response.getMessage());
    }

    @Test
    void testCreateEventForTracerAdvices_withDocuments() {
        String branchCountryCode = "BR-CO";
        String branchCode = "BR";
        String countryCode = "CO";

        DocumentDetails document = mock(DocumentDetails.class);
        when(document.getDocumentCategory()).thenReturn("CATEGORY1");
        when(document.getReferenceId()).thenReturn("refId");
        when(document.getEventId()).thenReturn("eventId");

        List<DocumentDetails> documents = Arrays.asList(document);
        when(apigeeDocumentService.getDocumentListForTracerFromUVByBranchAndCountry(branchCode, countryCode))
                .thenReturn(documents);

        tracerAdviceService.createEventForTracerAdvices(branchCountryCode);
    }

    @Test
    void testCreateEventForTracerAdvices_emptyDocuments() {
        String branchCountryCode = "BR-CO";
        String branchCode = "BR";
        String countryCode = "CO";

        when(apigeeDocumentService.getDocumentListForTracerFromUVByBranchAndCountry(branchCode, countryCode))
                .thenReturn(Collections.emptyList());

        tracerAdviceService.createEventForTracerAdvices(branchCountryCode);
    }

    @Test
    void testCreateRequestModel() {
        List<DocumentDetails> documents = new ArrayList<>();
        DocumentDetails document = mock(DocumentDetails.class);
        when(document.getReferenceId()).thenReturn("refId");
        when(document.getEventId()).thenReturn("eventId");
        documents.add(document);

        tracerAdviceService.createRequestModel(documents, "BR", "CO");
    }

    @Test
    void testCreateRequestAndUploadDocs() throws Exception {
        Request request = mock(Request.class);
        DocumentDetails document = mock(DocumentDetails.class);

        when(request.getBranchCode()).thenReturn("BR");
        when(request.getCountryCode()).thenReturn("CO");
        when(request.getMfr()).thenReturn("MFR");
        when(request.getLivr()).thenReturn("LIVR");
        when(request.getId()).thenReturn("REQ_ID");

        List<DocumentDetails> documents = Arrays.asList(document);
        when(document.getEventId()).thenReturn("EVENT_ID");

        doNothing().when(documentUtil).uploadDocumentsToDocStore(documents, request, "EVENT_ID", "REQ_ID",
                "BR", "CO");

        tracerAdviceService.createRequestAndUploadDocs(request, documents);
    }
}