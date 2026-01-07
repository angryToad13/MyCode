package com.bnpparibas.mql.helper;

import com.bnpparibas.dpw.controller.model.RequestEvent;
import com.bnpparibas.dpw.mql.model.AdditionalStatusData;
import com.bnpparibas.dpw.mql.model.ExternalTNTRequest;
import com.bnpparibas.dpw.referential.model.ExternalTrackAndTraceMapping;
import com.bnpparibas.mql.model.RequestExtended;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CXTHelperTest {

    private CXTHelper helper;

    @BeforeEach
    void setUp() {
        helper = new CXTHelper();
    }

    @Test
    void shouldReturnNullWhenMappingIsNull() {
        ExternalTNTRequest result =
                helper.toExternalTNTRequest(null, mock(RequestExtended.class), null);

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenRequestIsNull() {
        ExternalTNTRequest result =
                helper.toExternalTNTRequest(mock(ExternalTrackAndTraceMapping.class), null, null);

        assertNull(result);
    }

    @Test
    void shouldPopulateFieldsFromRequestWhenEventIsNull() {
        ExternalTrackAndTraceMapping mapping = mock(ExternalTrackAndTraceMapping.class);
        RequestExtended request = mock(RequestExtended.class);

        when(request.getBranchCode()).thenReturn("BR");
        when(request.getCountryCode()).thenReturn("IN");
        when(request.getEventId()).thenReturn("EVT1");
        when(request.getConnexisRequestRefId()).thenReturn("REF1");
        when(request.getConnexisRequestTxnId()).thenReturn("TXN1");
        when(request.getEbCusId()).thenReturn("EBCUS");
        when(request.getProdCode()).thenReturn("PROD");

        when(mapping.getTxnTypeCode()).thenReturn("TT");
        when(mapping.getTxnStatCode()).thenReturn("TS");
        when(mapping.getStatusCxt()).thenReturn("CTX");
        when(mapping.getProdCode()).thenReturn(null);

        ExternalTNTRequest result =
                helper.toExternalTNTRequest(mapping, request, null);

        assertNotNull(result);
        assertEquals("BR", result.getBranchCode());
        assertEquals("IN", result.getCountryCode());
        assertEquals("EVT1", result.getEventId());
        assertEquals("REF1", result.getConnexisRequestRefId());
        assertEquals("TXN1", result.getConnexisRequestTxnId());
        assertEquals("EVT1", result.getBoRefId());
        assertEquals("EBCUS", result.getEbCusId());
        assertEquals("TT", result.getTxnTypeCode());
        assertEquals("TS", result.getTxnStatCode());
        assertEquals("PROD", result.getProdCode());
        assertEquals("CTX", result.getStatusCxt());

        AdditionalStatusData flags = result.getFlags();
        assertNotNull(flags);
        assertTrue(flags.isCourierFlag());
        assertTrue(flags.isConnexisRequestRefIdMprFlag());
        assertTrue(flags.isDocAttachmentFlag());
    }

    @Test
    void shouldPopulateFieldsFromEventWhenEventIsPresent() {
        ExternalTrackAndTraceMapping mapping = mock(ExternalTrackAndTraceMapping.class);
        RequestExtended request = mock(RequestExtended.class);
        RequestEvent event = mock(RequestEvent.class);

        when(event.getId()).thenReturn("EVT_EVT");
        when(event.getConnexisRequestRefId()).thenReturn("REF_EVT");
        when(event.getConnexisRequestTxnId()).thenReturn("TXN_EVT");
        when(event.getMfr()).thenReturn("MFR");
        when(event.getCurCode()).thenReturn("EUR");
        when(event.getCustMstNo()).thenReturn("CUST1");

        when(request.getBranchCode()).thenReturn("BR");
        when(request.getCountryCode()).thenReturn("FR");
        when(request.getEbCusId()).thenReturn("EBCUS");

        when(mapping.getTxnTypeCode()).thenReturn("");
        when(mapping.getTxnStatCode()).thenReturn("");
        when(mapping.getStatusCxt()).thenReturn("CTX");
        when(mapping.getProdCode()).thenReturn("PROD_EVT");

        ExternalTNTRequest result =
                helper.toExternalTNTRequest(mapping, request, event);

        assertNotNull(result);
        assertEquals("EVT_EVT", result.getEventId());
        assertEquals("REF_EVT", result.getConnexisRequestRefId());
        assertEquals("TXN_EVT", result.getConnexisRequestTxnId());
        assertEquals("MFR", result.getMfr());
        assertEquals("EUR", result.getCurCode());
        assertEquals("CUST1", result.getCustomerId());
        assertEquals("PROD_EVT", result.getProdCode());

        assertNull(result.getTxnTypeCode());
        assertNull(result.getTxnStatCode());
    }

    @Test
    void shouldSetBoRefIdToNullWhenEventIdIsBlank() {
        ExternalTrackAndTraceMapping mapping = mock(ExternalTrackAndTraceMapping.class);
        RequestExtended request = mock(RequestExtended.class);

        when(request.getEventId()).thenReturn("   ");
        when(request.getBranchCode()).thenReturn("BR");
        when(request.getCountryCode()).thenReturn("IN");

        ExternalTNTRequest result =
                helper.toExternalTNTRequest(mapping, request, null);

        assertNull(result.getBoRefId());
    }
}