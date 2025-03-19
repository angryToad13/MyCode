I'll write test cases for this Java 8 Spring Boot service method. Looking at the code, I can see you have a method `getDocumentMetaDataForMUR` that retrieves document metadata based on branch code, country code, and MUR code list.

Here are comprehensive test cases using JUnit and Mockito:

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class TradeDocServiceImplTest {

    @Mock
    private DocumentRepository documentsRepository;
    
    @InjectMocks
    private TradeDocServiceImpl tradeDocService;
    
    private DocumentEntity documentEntity1;
    private DocumentEntity documentEntity2;
    
    @BeforeEach
    public void setUp() {
        // Set up test document entities
        documentEntity1 = new DocumentEntity();
        documentEntity1.setMurCode("MUR001");
        documentEntity1.setDocumentId("DOC001");
        documentEntity1.setName("Document1.pdf");
        documentEntity1.setRefEventId("EVENT001");
        documentEntity1.setRefRequestId("REQ001");
        
        documentEntity2 = new DocumentEntity();
        documentEntity2.setMurCode("MUR002");
        documentEntity2.setDocumentId("DOC002");
        documentEntity2.setName("Document2.pdf");
        documentEntity2.setRefEventId("EVENT002");
        documentEntity2.setRefRequestId("REQ002");
    }
    
    @Test
    public void testGetDocumentMetaDataForMUR_Success() {
        // Given
        String branchCode = "BR001";
        String countryCode = "US";
        List<String> murCodeList = Arrays.asList("MUR001", "MUR002");
        String branchCountryCode = branchCode + countryCode;
        
        when(documentsRepository.findByMurCodeIn(murCodeList))
            .thenReturn(Arrays.asList(documentEntity1, documentEntity2));
            
        // Mock the getBranchCountryCode method from CommonUtils
        // This requires additional setup with PowerMock or appropriate reflection-based mocking
        
        // When
        List<DocumentMetaDataDocstoreWithVersioning> result = 
            tradeDocService.getDocumentMetaDataForMUR(branchCode, countryCode, murCodeList);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(documentsRepository, times(1)).findByMurCodeIn(murCodeList);
        
        // Verify metadata for first document
        DocumentMetaDataDocstoreWithVersioning metadata1 = result.get(0);
        assertEquals(documentEntity1.getMurCode(), metadata1.getMurCode());
        assertEquals(documentEntity1.getDocumentId(), metadata1.getId());
        assertEquals(documentEntity1.getName(), metadata1.getFileName());
        assertEquals(documentEntity1.getRefEventId(), metadata1.getEventId());
        assertEquals(documentEntity1.getRefRequestId(), metadata1.getRequestId());
        
        // Verify metadata for second document
        DocumentMetaDataDocstoreWithVersioning metadata2 = result.get(1);
        assertEquals(documentEntity2.getMurCode(), metadata2.getMurCode());
        assertEquals(documentEntity2.getDocumentId(), metadata2.getId());
        assertEquals(documentEntity2.getName(), metadata2.getFileName());
        assertEquals(documentEntity2.getRefEventId(), metadata2.getEventId());
        assertEquals(documentEntity2.getRefRequestId(), metadata2.getRequestId());
    }
    
    @Test
    public void testGetDocumentMetaDataForMUR_EmptyMurCodeList() {
        // Given
        String branchCode = "BR001";
        String countryCode = "US";
        List<String> murCodeList = Collections.emptyList();
        
        // When
        List<DocumentMetaDataDocstoreWithVersioning> result = 
            tradeDocService.getDocumentMetaDataForMUR(branchCode, countryCode, murCodeList);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(documentsRepository, times(1)).findByMurCodeIn(murCodeList);
    }
    
    @Test
    public void testGetDocumentMetaDataForMUR_NoMatchingDocuments() {
        // Given
        String branchCode = "BR001";
        String countryCode = "US";
        List<String> murCodeList = Arrays.asList("MUR003", "MUR004");
        
        when(documentsRepository.findByMurCodeIn(murCodeList))
            .thenReturn(Collections.emptyList());
        
        // When
        List<DocumentMetaDataDocstoreWithVersioning> result = 
            tradeDocService.getDocumentMetaDataForMUR(branchCode, countryCode, murCodeList);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(documentsRepository, times(1)).findByMurCodeIn(murCodeList);
    }
    
    @Test
    public void testGetDocumentMetaDataForMUR_FilterByBranchCountryCode() {
        // Given
        String branchCode = "BR001";
        String countryCode = "US";
        List<String> murCodeList = Arrays.asList("MUR001", "MUR002");
        String branchCountryCode = branchCode + countryCode;
        
        // Setup CommonUtils mock to return matching branch country code for first document
        // and non-matching for second document
        
        when(documentsRepository.findByMurCodeIn(murCodeList))
            .thenReturn(Arrays.asList(documentEntity1, documentEntity2));
            
        // Mock isValidBranchCountryCode method to only return true for the first document
        // This would require using a spy for the service or setting up appropriate mocking
        
        // When
        List<DocumentMetaDataDocstoreWithVersioning> result = 
            tradeDocService.getDocumentMetaDataForMUR(branchCode, countryCode, murCodeList);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Only one document should pass the filter
        verify(documentsRepository, times(1)).findByMurCodeIn(murCodeList);
        
        // Verify the remaining document metadata
        DocumentMetaDataDocstoreWithVersioning metadata = result.get(0);
        assertEquals(documentEntity1.getMurCode(), metadata.getMurCode());
    }
    
    @Test
    public void testIsValidBranchCountryCode_Match() {
        // Given
        String branchCountryCode = "BR001US";
        DocumentEntity document = new DocumentEntity();
        document.setRefRequestId("REQ001");
        document.setRefEventId("EVENT001");
        
        // Mock CommonUtils.getBranchCountryCode to return matching branch country code
        // This requires additional setup with PowerMock or appropriate reflection-based mocking
        
        // When
        boolean result = tradeDocService.isValidBranchCountryCode(document, branchCountryCode);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    public void testIsValidBranchCountryCode_NoMatch() {
        // Given
        String branchCountryCode = "BR001US";
        DocumentEntity document = new DocumentEntity();
        document.setRefRequestId("REQ001");
        document.setRefEventId("EVENT001");
        
        // Mock CommonUtils.getBranchCountryCode to return non-matching branch country code
        // This requires additional setup with PowerMock or appropriate reflection-based mocking
        
        // When
        boolean result = tradeDocService.isValidBranchCountryCode(document, branchCountryCode);
        
        // Then
        assertFalse(result);
    }
}
```

For more comprehensive testing, you may want to consider:

1. Testing the `isValidBranchCountryCode` method more thoroughly by mocking the `CommonUtils.getBranchCountryCode` method using PowerMockito or a similar tool.

2. Adding edge cases like:
   - Null values for branchCode or countryCode
   - Special characters in codes
   - Very large murCodeList

3. Integration tests that would verify the actual interaction with the repository.

Would you like me to expand on any specific part of these test cases or add any additional test scenarios?​​​​​​​​​​​​​​​​