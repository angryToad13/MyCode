package com.bnpparbas.dpw.docstore.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;

import com.bnpparbas.dpw.docstore.model.DocumentDetails;
import com.bnpparbas.dpw.docstore.model.Request;
import com.bnpparbas.dpw.docstore.model.StringResponse;
import com.bnpparbas.dpw.docstore.service.ApigeeDocumentService;
import com.bnpparbas.dpw.docstore.util.DocumentUtil;

@ExtendWith(MockitoExtension.class)
class TracerAdviceServiceImplTest {

```
@Mock
private Environment env;

@Mock
private RequestApi requestApi;

@Mock
private ApigeeDocumentService apigeeDocumentService;

@Mock
private DocumentUtil documentUtil;

@InjectMocks
private TracerAdviceServiceImpl tracerAdviceService;

private String testGroup;
private String testBranchCountryCode;
private List<DocumentDetails> mockDocumentDetails;
private Request mockRequest;

@BeforeEach
void setUp() {
    testGroup = "TEST_GROUP";
    testBranchCountryCode = "US_NYC";
    
    // Set up private fields using reflection
    ReflectionTestUtils.setField(tracerAdviceService, "tracerAdvices", 
        Arrays.asList("advice1", "advice2", "advice3"));
    ReflectionTestUtils.setField(tracerAdviceService, "technicalUser", "testuser");

    // Mock document details
    DocumentDetails doc1 = createMockDocumentDetails("1", "REF001", "EVENT001", "US", "NYC");
    DocumentDetails doc2 = createMockDocumentDetails("2", "REF002", "EVENT002", "US", "NYC");
    mockDocumentDetails = Arrays.asList(doc1, doc2);

    // Mock request
    mockRequest = new Request();
    mockRequest.setBranchCode("NYC");
    mockRequest.setCountryCode("US");
    mockRequest.setRfr("RFR001");
    mockRequest.setLivr("LIVR001");
}

private DocumentDetails createMockDocumentDetails(String id, String refId, String eventId, 
                                                String countryCode, String branchCode) {
    DocumentDetails doc = new DocumentDetails();
    doc.setDocumentDataId(id);
    doc.setReferenceId(refId);
    doc.setEventId(eventId);
    doc.setDocumentCategory("CATEGORY");
    doc.setCountryCode(countryCode);
    doc.setBranchCode(branchCode);
    return doc;
}

@Test
void testIntegrateTracerAdvices_Success() {
    // Given
    String expectedBranchesProperty = "branch1,branch2,branch3";
    when(env.getProperty("uv.branches." + testGroup)).thenReturn(expectedBranchesProperty);

    // When
    StringResponse response = tracerAdviceService.integrateTracerAdvices(testGroup);

    // Then
    assertNotNull(response);
    assertEquals("Integrated tracer advices successfully", response.getResponse());
    verify(env).getProperty("uv.branches." + testGroup);
}

@Test
void testIntegrateTracerAdvices_EmptyBranchesList() {
    // Given
    when(env.getProperty("uv.branches." + testGroup)).thenReturn("");

    // When
    StringResponse response = tracerAdviceService.integrateTracerAdvices(testGroup);

    // Then
    assertNotNull(response);
    assertEquals("Integrated tracer advices successfully", response.getResponse());
    verify(env).getProperty("uv.branches." + testGroup);
}

@Test
void testIntegrateTracerAdvices_NullBranchesList() {
    // Given
    when(env.getProperty("uv.branches." + testGroup)).thenReturn(null);

    // When
    StringResponse response = tracerAdviceService.integrateTracerAdvices(testGroup);

    // Then
    assertNotNull(response);
    assertEquals("Integrated tracer advices successfully", response.getResponse());
    verify(env).getProperty("uv.branches." + testGroup);
}

@Test
void testCreateEventForTracerAdvices_Success() {
    // Given
    when(apigeeDocumentService.getDocumentListForTracerFromUVByBranchAndCountry(
        testBranchCountryCode.split("_")[0], testBranchCountryCode.split("_")[1]))
        .thenReturn(mockDocumentDetails);

    // When
    tracerAdviceService.createEventForTracerAdvices(testBranchCountryCode);

    // Then
    verify(apigeeDocumentService).getDocumentListForTracerFromUVByBranchAndCountry("US", "NYC");
    verify(documentUtil, times(2)).uploadDocumentsToDocStore(any(), any(), any(), any());
}

@Test
void testCreateEventForTracerAdvices_EmptyDocumentList() {
    // Given
    when(apigeeDocumentService.getDocumentListForTracerFromUVByBranchAndCountry(
        testBranchCountryCode.split("_")[0], testBranchCountryCode.split("_")[1]))
        .thenReturn(Collections.emptyList());

    // When
    tracerAdviceService.createEventForTracerAdvices(testBranchCountryCode);

    // Then
    verify(apigeeDocumentService).getDocumentListForTracerFromUVByBranchAndCountry("US", "NYC");
    verify(documentUtil, never()).uploadDocumentsToDocStore(any(), any(), any(), any());
}

@Test
void testCreateEventForTracerAdvices_NullDocumentList() {
    // Given
    when(apigeeDocumentService.getDocumentListForTracerFromUVByBranchAndCountry(
        testBranchCountryCode.split("_")[0], testBranchCountryCode.split("_")[1]))
        .thenReturn(null);

    // When
    tracerAdviceService.createEventForTracerAdvices(testBranchCountryCode);

    // Then
    verify(apigeeDocumentService).getDocumentListForTracerFromUVByBranchAndCountry("US", "NYC");
    verify(documentUtil, never()).uploadDocumentsToDocStore(any(), any(), any(), any());
}

@Test
void testCreateUVRequestModel_Success() {
    // Given
    DocumentDetails documentDetails = createMockDocumentDetails("1", "REF001", "EVENT001", "US", "NYC");

    // When
    tracerAdviceService.createUVRequestModel(Arrays.asList(documentDetails), "NYC", "US");

    // Then
    verify(requestApi).createRequest(any(Request.class));
}

@Test
void testCreateUVRequestModel_VerifyRequestFields() {
    // Given
    DocumentDetails documentDetails = createMockDocumentDetails("1", "REF001", "EVENT001", "US", "NYC");
    
    // When
    tracerAdviceService.createUVRequestModel(Arrays.asList(documentDetails), "NYC", "US");

    // Then
    verify(requestApi).createRequest(argThat(request -> {
        assertEquals("REF001", request.getRfr());
        assertEquals("EVENT001", request.getLivr());
        assertEquals("NYC", request.getBranchCode());
        assertEquals("US", request.getCountryCode());
        assertEquals("UV", request.getChannelName());
        assertEquals("03", request.getExternalStatus());
        assertEquals("03", request.getStatusInuv());
        assertEquals(LocalDateTime.now().atZone(ZoneOffset.UTC).toLocalDateTime().getDayOfYear(),
                    request.getRequestCreationDateTime().getDayOfYear());
        assertEquals("Completed", request.getStatus());
        return true;
    }));
}

@Test
void testCreateRequestAndUploadDocs_Success() {
    // Given
    CompletableFuture<Request> mockFuture = CompletableFuture.completedFuture(mockRequest);
    when(requestApi.createRequest(any(Request.class))).thenReturn(mockFuture);

    // When
    tracerAdviceService.createRequestAndUploadDocs(mockRequest, mockDocumentDetails);

    // Then
    verify(requestApi).createRequest(mockRequest);
    verify(documentUtil).uploadDocumentsToDocStore(
        eq(mockDocumentDetails), 
        eq(mockRequest), 
        any(), 
        eq(mockRequest.getEventId())
    );
}

@Test
void testCreateRequestAndUploadDocs_HttpStatusException() {
    // Given
    CompletableFuture<Request> mockFuture = new CompletableFuture<>();
    mockFuture.completeExceptionally(new RuntimeException("HTTP 404 Not Found"));
    when(requestApi.createRequest(any(Request.class))).thenReturn(mockFuture);

    // When
    tracerAdviceService.createRequestAndUploadDocs(mockRequest, mockDocumentDetails);

    // Then
    verify(requestApi).createRequest(mockRequest);
    verify(documentUtil, never()).uploadDocumentsToDocStore(any(), any(), any(), any());
    // Should log error message about HTTP exception
}

@Test
void testCreateRequestAndUploadDocs_RestClientException() {
    // Given
    CompletableFuture<Request> mockFuture = new CompletableFuture<>();
    mockFuture.completeExceptionally(new RestClientException("Connection timeout"));
    when(requestApi.createRequest(any(Request.class))).thenReturn(mockFuture);

    // When
    tracerAdviceService.createRequestAndUploadDocs(mockRequest, mockDocumentDetails);

    // Then
    verify(requestApi).createRequest(mockRequest);
    verify(documentUtil, never()).uploadDocumentsToDocStore(any(), any(), any(), any());
    // Should log error message about RestClient exception
}

@Test
void testCreateRequestAndUploadDocs_GenericException() {
    // Given
    CompletableFuture<Request> mockFuture = new CompletableFuture<>();
    mockFuture.completeExceptionally(new RuntimeException("Unexpected error"));
    when(requestApi.createRequest(any(Request.class))).thenReturn(mockFuture);

    // When
    tracerAdviceService.createRequestAndUploadDocs(mockRequest, mockDocumentDetails);

    // Then
    verify(requestApi).createRequest(mockRequest);
    verify(documentUtil, never()).uploadDocumentsToDocStore(any(), any(), any(), any());
    // Should log generic error message
}

@Test
void testIntegrateTracerAdvices_WithSpecialCharactersInGroup() {
    // Given
    String specialGroup = "TEST-GROUP_123";
    when(env.getProperty("uv.branches." + specialGroup)).thenReturn("branch1,branch2");

    // When
    StringResponse response = tracerAdviceService.integrateTracerAdvices(specialGroup);

    // Then
    assertNotNull(response);
    assertEquals("Integrated tracer advices successfully", response.getResponse());
    verify(env).getProperty("uv.branches." + specialGroup);
}

@Test
void testCreateEventForTracerAdvices_WithDifferentBranchCountryFormat() {
    // Given
    String branchCountryCode = "UK_LON";
    when(apigeeDocumentService.getDocumentListForTracerFromUVByBranchAndCountry("UK", "LON"))
        .thenReturn(mockDocumentDetails);

    // When
    tracerAdviceService.createEventForTracerAdvices(branchCountryCode);

    // Then
    verify(apigeeDocumentService).getDocumentListForTracerFromUVByBranchAndCountry("UK", "LON");
}

@Test
void testCreateUVRequestModel_WithEmptyDocumentList() {
    // Given
    List<DocumentDetails> emptyList = Collections.emptyList();

    // When
    tracerAdviceService.createUVRequestModel(emptyList, "NYC", "US");

    // Then
    // Should handle empty list gracefully
    verify(requestApi, never()).createRequest(any(Request.class));
}

@Test
void testCreateUVRequestModel_WithNullDocumentList() {
    // When & Then
    assertDoesNotThrow(() -> {
        tracerAdviceService.createUVRequestModel(null, "NYC", "US");
    });
    
    verify(requestApi, never()).createRequest(any(Request.class));
}
```

}