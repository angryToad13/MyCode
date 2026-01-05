import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalTNTHelperTest {

    @Mock
    private ControllerUtils controllerUtils;

    @Mock
    private TxnRecordMapper txnRecordMapper;

    @InjectMocks
    private ExternalTNTHelper externalTNTHelper;

    @Test
    void getXmlAttachments_whenNoDocuments_thenReturnNull() {
        ExternalTNTRequest request = new ExternalTNTRequest();
        List<Attachment> result =
                externalTNTHelper.getXmlAttachments(Collections.emptyList(), request);
        assertNull(result);
    }

    @Test
    void getXmlAttachments_whenSwiftSource_thenSetSwiftTitle() {
        DocumentMetaDataDocstoreWithVersioning doc = mock(DocumentMetaDataDocstoreWithVersioning.class);
        when(doc.isEligibleForConnexis()).thenReturn(true);
        when(doc.getTitle()).thenReturn("test.pdf");
        when(doc.getDocumentSource()).thenReturn("SWIFT");
        when(doc.getDocumentCategory()).thenReturn("12345");

        ExternalTNTRequest request = new ExternalTNTRequest();

        List<Attachment> result =
                externalTNTHelper.getXmlAttachments(List.of(doc), request);

        assertEquals(1, result.size());
        assertEquals("SWIFT : 12345", result.get(0).getTitle());
        assertEquals("test.pdf", result.get(0).getFileName());
    }

    @Test
    void getXmlAttachments_whenNonSwiftSource_thenUseDocumentValue() {
        DocumentMetaDataDocstoreWithVersioning doc = mock(DocumentMetaDataDocstoreWithVersioning.class);
        when(doc.isEligibleForConnexis()).thenReturn(true);
        when(doc.getTitle()).thenReturn("invoice.xml");
        when(doc.getDocumentSource()).thenReturn("EMAIL");

        ExternalTNTRequest request = new ExternalTNTRequest();

        List<Attachment> result =
                externalTNTHelper.getXmlAttachments(List.of(doc), request);

        assertEquals("DOCUMENT_VALUE : invoice", result.get(0).getTitle());
    }

    @Test
    void setConnexisRequestRefIdFromMFR_whenValuePresent_thenSet() {
        ExternalTNTRequest request = new ExternalTNTRequest();
        request.setMfr("MFR123");
        request.setBranchCode("BR");
        request.setCountryCode("IN");

        when(controllerUtils.getConnexisReqRefIdThroughMfr("BR", "IN", "MFR123"))
                .thenReturn("REF123");

        externalTNTHelper.setConnexisRequestRefIdFromMFR(request);

        assertEquals("REF123", request.getConnexisRequestRefIdMFR());
    }

    @Test
    void setConnexisRequestRefIdFromMFR_whenNoValue_thenDoNothing() {
        ExternalTNTRequest request = new ExternalTNTRequest();
        request.setMfr("MFR123");

        when(controllerUtils.getConnexisReqRefIdThroughMfr(any(), any(), any()))
                .thenReturn(null);

        externalTNTHelper.setConnexisRequestRefIdFromMFR(request);

        assertNull(request.getConnexisRequestRefIdMFR());
    }

    @Test
    void getCourierPartnerValue_shouldDelegateToControllerUtils() {
        ExternalTNTRequest request = new ExternalTNTRequest();
        request.setBranchCode("BR");
        request.setCountryCode("IN");
        request.setEventId("EVT1");

        Courier courier = new Courier();

        when(controllerUtils.getCourierDetails("BR", "IN", "EVT1"))
                .thenReturn(courier);

        Courier result = externalTNTHelper.getCourierPartnerValue(request);

        assertSame(courier, result);
    }

    @Test
    void setCommonFields_whenNoFlags_thenApplyAllFields() {
        ExternalTNTRequest request = new ExternalTNTRequest();
        TxnRecord txnRecord = new TxnRecord();

        externalTNTHelper.setCommonFields(txnRecord, request);

        verify(txnRecordMapper, atLeastOnce()).map(any(), any());
    }

    @Test
    void setCommonFields_whenSpecificFieldsProvided_thenApplyOnlyThose() {
        ExternalTNTRequest request = new ExternalTNTRequest();
        Flags flags = new Flags();
        flags.setCommonFields(List.of(CommonTxnRecordField.FIELD1));
        request.setFlags(flags);

        TxnRecord txnRecord = new TxnRecord();

        externalTNTHelper.setCommonFields(txnRecord, request);

        verify(txnRecordMapper, times(1)).map(any(), any());
    }

    @Test
    void getCustomerReference_whenCustomerIdStartsWithBranch_thenReturnCustomerId() {
        ExternalTNTRequest request = new ExternalTNTRequest();
        request.setCustomerId("BR12345");
        request.setBranchCode("BR");
        request.setEventId("EVT");
        request.setProdCode("PRD");

        String result = externalTNTHelper.getCustomerReference(request);

        assertEquals("BR12345", result);
    }

    @Test
    void getCustomerReference_whenShortCustomerId_thenPrefixBranch() {
        ExternalTNTRequest request = new ExternalTNTRequest();
        request.setCustomerId("123");
        request.setBranchCode("BR");

        String result = externalTNTHelper.getCustomerReference(request);

        assertEquals("BR123", result);
    }

    @Test
    void getCustomerReference_whenBlankInputs_thenReturnNull() {
        ExternalTNTRequest request = new ExternalTNTRequest();
        assertNull(externalTNTHelper.getCustomerReference(request));
    }
}