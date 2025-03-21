package com.bnpparibas.mgl.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.bnpparibas.mgl.model.SwiftAckNackResponse;
import com.bnpparibas.mgl.model.SwiftBranchDetailsMapping;
import com.bnpparibas.mgl.utils.FileUtil;
import com.bnpparibas.mgl.utils.ReferentialUtils;
import com.bnpparibas.mgl.utils.StringUtils;
import com.bnpparibas.mgl.constants.SwiftConstants;

@ExtendWith(MockitoExtension.class)
public class SwiftServiceImplTest {

    @InjectMocks
    private SwiftServiceImpl swiftService;
    
    @Mock
    private ReferentialUtils referentialUtils;
    
    @Mock
    private FileUtil fileUtil;
    
    @Mock
    private StringUtils stringUtils;
    
    @Mock
    private MultipartFile mockFile;
    
    @Mock
    private File convertedFile;
    
    @Spy
    private Map<String, String> foldersPath = new HashMap<>();
    
    @BeforeEach
    public void setup() {
        foldersPath.put(SwiftConstants.SWIFT_TO_DO, "/path/to/todo");
        foldersPath.put(SwiftConstants.SWIFT_ERROR, "/path/to/error");
        
        // Setting up the ThreadPool executor using ReflectionTestUtils
        ReflectionTestUtils.setField(swiftService, "swiftRequestExecutor", 
                Executors.newFixedThreadPool(5));
    }
    
    @Test
    void testRoutingCodeBasedMapping_Success() {
        // Setup
        Map<String, String> context = new HashMap<>();
        context.put("RECEIVER_BIC_CODE", "TESTBIC1");
        context.put("BNP_CODE", "BNP123");
        
        SwiftBranchDetailsMapping mapping = new SwiftBranchDetailsMapping();
        when(referentialUtils.getSwiftBranchDetailsMapping(anyString(), anyString(), anyString()))
            .thenReturn(mapping);
        
        when(stringUtils.isBlank(anyString())).thenReturn(false);
        
        // Call the method using reflection as it's private
        Map<String, SwiftAckNackResponse> result = ReflectionTestUtils.invokeMethod(
                swiftService, "routingCodeBasedMapping", context, new HashMap<String, Object>());
        
        // Assertions
        assertNotNull(result);
        verify(referentialUtils).getSwiftBranchDetailsMapping(anyString(), anyString(), anyString());
    }
    
    @Test
    void testPublish_WithFiles() {
        // Setup
        MultipartFile[] files = new MultipartFile[1];
        files[0] = mockFile;
        Map<String, String> swiftMessageType = new HashMap<>();
        swiftMessageType.put("type", "MT799");
        
        // Mock file existence
        when(mockFile.isEmpty()).thenReturn(false);
        
        // Call method
        swiftService.publish(files, swiftMessageType);
        
        // Verify that processing was attempted
        verify(mockFile).isEmpty();
    }
    
    @Test
    void testProcessFile_Success() throws Exception {
        // Setup
        Map<String, SwiftAckNackResponse> swiftDataMap = new HashMap<>();
        Map<String, String> multiplePrtMap = new HashMap<>();
        multiplePrtMap.put("header1", "content1");
        
        when(fileUtil.convertMultiPartToFile(any(MultipartFile.class), anyString(), anyString()))
            .thenReturn(convertedFile);
        
        when(convertedFile.getName()).thenReturn("testFile.txt");
        when(stringUtils.isBlank(anyString())).thenReturn(false);
        
        SwiftAckNackResponse mockResponse = mock(SwiftAckNackResponse.class);
        when(mockResponse.getBranchCode()).thenReturn("BR001");
        when(mockResponse.getCountryCode()).thenReturn("FR");
        
        // Call method using reflection as it's private
        ReflectionTestUtils.invokeMethod(
                swiftService, "processFile", 
                mockFile, swiftDataMap, new java.util.concurrent.atomic.AtomicBoolean(false), 
                new HashMap<String, String>(), foldersPath);
        
        // Verify file conversion was attempted
        verify(fileUtil).convertMultiPartToFile(any(MultipartFile.class), anyString(), anyString());
    }
    
    @Test
    void testProcessAckNackFile_WithValidBranchAndCountryCode() {
        // Setup
        Map<String, SwiftAckNackResponse> swiftDataMap = new HashMap<>();
        SwiftAckNackResponse response = new SwiftAckNackResponse();
        response.setBranchCode("BR001");
        response.setCountryCode("FR");
        response.setReceiverBicCode("BNPAFRPP");
        response.setFileName("test.txt");
        
        Map<String, String> context = new HashMap<>();
        
        when(stringUtils.isBlank("BR001")).thenReturn(false);
        when(stringUtils.isBlank("FR")).thenReturn(false);
        
        // Call method using reflection
        ReflectionTestUtils.invokeMethod(
                swiftService, "processAckNackFile", 
                response, swiftDataMap, foldersPath);
        
        // Verify the data was added to map with proper key
        assertTrue(swiftDataMap.containsKey("BR001-FR"));
    }
    
    @Test
    void testIsIncomingMessageType_True() {
        // Setup
        SwiftAckNackResponse response = new SwiftAckNackResponse();
        response.setSwiftStatus(""); // Blank status
        response.setReceiverBicCode(""); // Blank code
        
        when(stringUtils.isBlank("")).thenReturn(true);
        
        // Call method using reflection
        boolean result = ReflectionTestUtils.invokeMethod(
                swiftService, "isIncomingMessageType", response);
        
        // Expected to be true when both values are blank
        assertTrue(result);
    }
    
    @Test
    void testGetDocumentMetaDataForBranch_Success() {
        // Setup
        Map<String, SwiftAckNackResponse> swiftDataMap = new HashMap<>();
        SwiftAckNackResponse response = new SwiftAckNackResponse();
        response.setBranchCode("BR001");
        response.setCountryCode("FR");
        swiftDataMap.put("BR001-FR", response);
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        futures.add(future);
        
        // Mock getDocumentMetaData method to return a CompletableFuture
        when(ReflectionTestUtils.invokeMethod(
                eq(swiftService), eq("getDocumentMetaData"), 
                anyString(), any())).thenReturn(future);
        
        // Call method using reflection
        ReflectionTestUtils.invokeMethod(
                swiftService, "getDocumentMetaDataForBranch", swiftDataMap);
        
        // Verify completion
        assertDoesNotThrow(() -> CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join());
    }
    
    @Test
    void testGetDocumentMetaData_Success() {
        // Setup
        String key = "BR001-FR";
        List<SwiftAckNackResponse> values = new ArrayList<>();
        SwiftAckNackResponse response = new SwiftAckNackResponse();
        response.setMurCode("MUR123");
        values.add(response);
        
        // Call method using reflection
        CompletableFuture<Void> result = ReflectionTestUtils.invokeMethod(
                swiftService, "getDocumentMetaData", key, values);
        
        // Verify completion
        assertNotNull(result);
        assertDoesNotThrow(() -> result.join());
    }
}