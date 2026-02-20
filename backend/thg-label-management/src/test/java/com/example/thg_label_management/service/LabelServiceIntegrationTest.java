package com.example.thg_label_management.service;

import com.example.thg_label_management.model.Label;
import com.example.thg_label_management.repository.LabelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class LabelServiceIntegrationTest {

    @Autowired
    private LabelService labelService;

    @Autowired
    private LabelRepository labelRepository;

    @BeforeEach
    void setUp() {
        labelRepository.deleteAll();
    }

    @Test
    void testUploadLabelFirstTime() {
        String sku = "SKU123";
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "dummy content".getBytes());

        Label result = labelService.uploadLabel(sku, file);

        assertNotNull(result.getId());
        assertEquals(sku, result.getSku());
        assertEquals(1, result.getVersion());
        assertTrue(result.isActive());
        assertEquals("test.pdf", result.getFileName());
        assertNotNull(result.getCreatedAt());
        assertEquals("Dummy User", result.getCreatedBy());
    }

    @Test
    void testUploadLabelVersioning() {
        String sku = "SKU123";
        MockMultipartFile file1 = new MockMultipartFile("file", "test1.pdf", "application/pdf", "content 1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "test2.pdf", "application/pdf", "content 2".getBytes());

        labelService.uploadLabel(sku, file1);
        Label secondLabel = labelService.uploadLabel(sku, file2);

        assertEquals(2, secondLabel.getVersion());
        assertTrue(secondLabel.isActive());

        List<Label> labels = labelRepository.findAll();
        assertEquals(2, labels.size());

        Label firstLabelFromDb = labels.stream().filter(l -> l.getVersion() == 1).findFirst().orElseThrow();
        assertFalse(firstLabelFromDb.isActive(), "First label should be deactivated");
        
        Label secondLabelFromDb = labels.stream().filter(l -> l.getVersion() == 2).findFirst().orElseThrow();
        assertTrue(secondLabelFromDb.isActive(), "Second label should be active");
    }
}
