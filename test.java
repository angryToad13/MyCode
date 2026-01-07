package com.bnpparibas.mql.strategy;

import com.bnpparibas.dpw.error.exception.DpwException;
import com.bnpparibas.dpw.mql.model.ExternalTNTRequest;
import com.bnpparibas.mql.bean.externalTrackAndTrace.AdditionalField;
import com.bnpparibas.mql.bean.externalTrackAndTrace.Attachment;
import com.bnpparibas.mql.bean.externalTrackAndTrace.TnxRecord;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class XmlStrategyTest {

    @TempDir
    Path tempDir;

    private final XmlStrategy<TnxRecord> strategy = new TestXmlStrategy();

    @Test
    void shouldGenerateXmlFileSuccessfully() throws Exception {
        TnxRecord record = new TnxRecord();
        String eventId = "EVT123";

        File file = strategy.generateXmlFile(
                eventId,
                record,
                tempDir.toString()
        );

        assertNotNull(file);
        assertTrue(file.exists());
        assertTrue(file.getName().contains(eventId));
    }

    @Test
    void shouldThrowDpwExceptionWhenJaxbFails() throws Exception {
        TnxRecord record = new TnxRecord();

        try (MockedStatic<JAXBContext> mocked = mockStatic(JAXBContext.class)) {
            mocked.when(() -> JAXBContext.newInstance(any(Class.class)))
                    .thenThrow(new JAXBException("boom"));

            assertThrows(DpwException.class, () ->
                    strategy.generateXmlFile(
                            "EVT_FAIL",
                            record,
                            tempDir.toString()
                    )
            );
        }
    }

    @Test
    void shouldSetAttachmentsAndCourierWhenCourierPresent() {
        TnxRecord record = spy(new TnxRecord());
        ExternalTNTRequest request = mock(ExternalTNTRequest.class);

        ExternalTNTRequest.CourierPartnerWayBill courier =
                mock(ExternalTNTRequest.CourierPartnerWayBill.class);

        when(request.getCourierPartnerWayBill()).thenReturn(courier);
        when(courier.getCourierPartnerCategory()).thenReturn("DHL");
        when(courier.getCourierPartnerWaybillNo()).thenReturn("WB123");
        when(request.getEventId()).thenReturn("EVT1");

        List<Attachment> attachments = Collections.emptyList();

        strategy.setAttachmentAndCourier(record, request, attachments);

        verify(record).setAttachments(attachments);
        verify(record).setCourier_partner("DHL");
        verify(record).setCourier_partner_waybill_no("WB123");

        List<AdditionalField> fields = record.getAdditionalFields();
        assertEquals(1, fields.size());
        assertEquals("mo_event_id", fields.get(0).getName());
    }

    @Test
    void shouldSetAttachmentsAndAdditionalFieldWhenCourierAbsent() {
        TnxRecord record = spy(new TnxRecord());
        ExternalTNTRequest request = mock(ExternalTNTRequest.class);

        when(request.getCourierPartnerWayBill()).thenReturn(null);
        when(request.getEventId()).thenReturn("EVT2");

        strategy.setAttachmentAndCourier(
                record,
                request,
                Collections.emptyList()
        );

        verify(record).setAttachments(any());
        verify(record, never()).setCourier_partner(any());
        verify(record, never()).setCourier_partner_waybill_no(any());

        assertEquals(1, record.getAdditionalFields().size());
    }

    private static class TestXmlStrategy implements XmlStrategy<TnxRecord> {

        @Override
        public String getProductCode() {
            return "TEST";
        }

        @Override
        public File generateXml(
                ExternalTNTRequest externalTandTRequest,
                String xmlFilePath,
                List<Attachment> documentAttachments
        ) {
            return null;
        }
    }
}