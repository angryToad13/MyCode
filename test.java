package com.bnpparibas.mql.strategy;

import com.bnpparibas.dpw.mql.model.ExternalTNTRequest;
import com.bnpparibas.mql.bean.externalTrackAndTrace.Attachment;
import com.bnpparibas.mql.bean.externalTrackAndTrace.TnxRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class XmlStrategyContextTest {

    @Mock
    private XmlStrategy<TnxRecord> strategyA;

    @Mock
    private XmlStrategy<TnxRecord> strategyB;

    @Mock
    private ExternalTNTRequest externalTNTRequest;

    private XmlStrategyContext xmlStrategyContext;

    @BeforeEach
    void setUp() {
        when(strategyA.getProductCode()).thenReturn("PROD_A");
        when(strategyB.getProductCode()).thenReturn("PROD_B");

        Set<XmlStrategy<? extends TnxRecord>> strategies = new HashSet<>();
        strategies.add(strategyA);
        strategies.add(strategyB);

        xmlStrategyContext = new XmlStrategyContext(strategies);
    }

    @Test
    void shouldDelegateGenerateXmlToCorrectStrategy() {
        File expectedFile = new File("test.xml");
        List<Attachment> attachments = Collections.emptyList();

        when(externalTNTRequest.getProdCode()).thenReturn("PROD_A");
        when(strategyA.generateXml(externalTNTRequest, "/tmp", attachments))
                .thenReturn(expectedFile);

        File result = xmlStrategyContext.generateXml(
                externalTNTRequest,
                "/tmp",
                attachments
        );

        assertNotNull(result);
        assertEquals(expectedFile, result);
        verify(strategyA, times(1))
                .generateXml(externalTNTRequest, "/tmp", attachments);
        verify(strategyB, never()).generateXml(any(), any(), any());
    }

    @Test
    void shouldUseAnotherStrategyBasedOnProductCode() {
        File expectedFile = new File("test-b.xml");
        List<Attachment> attachments = Collections.emptyList();

        when(externalTNTRequest.getProdCode()).thenReturn("PROD_B");
        when(strategyB.generateXml(externalTNTRequest, "/path", attachments))
                .thenReturn(expectedFile);

        File result = xmlStrategyContext.generateXml(
                externalTNTRequest,
                "/path",
                attachments
        );

        assertEquals(expectedFile, result);
        verify(strategyB).generateXml(externalTNTRequest, "/path", attachments);
        verify(strategyA, never()).generateXml(any(), any(), any());
    }

    @Test
    void shouldThrowExceptionWhenNoStrategyFoundForProductCode() {
        when(externalTNTRequest.getProdCode()).thenReturn("UNKNOWN");

        assertThrows(NullPointerException.class, () ->
                xmlStrategyContext.generateXml(
                        externalTNTRequest,
                        "/tmp",
                        Collections.emptyList()
                )
        );
    }

    @Test
    void shouldInitializeAllStrategiesInConstructor() {
        when(strategyA.getProductCode()).thenReturn("PROD_A");
        when(strategyB.getProductCode()).thenReturn("PROD_B");

        Set<XmlStrategy<? extends TnxRecord>> strategies = new HashSet<>();
        strategies.add(strategyA);
        strategies.add(strategyB);

        XmlStrategyContext context = new XmlStrategyContext(strategies);

        when(externalTNTRequest.getProdCode()).thenReturn("PROD_A");

        assertDoesNotThrow(() ->
                context.generateXml(
                        externalTNTRequest,
                        "/tmp",
                        Collections.emptyList()
                )
        );
    }
}