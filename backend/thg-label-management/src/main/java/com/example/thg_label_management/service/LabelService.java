package com.example.thg_label_management.service;

import com.example.thg_label_management.model.Label;
import com.example.thg_label_management.repository.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Core service for label lifecycle operations.
 * Manages PDF extraction (OCR), MinIO storage integration, and soft-delete logic.
 */
@Service
@RequiredArgsConstructor
public class LabelService {

    private final LabelRepository labelRepository;
    private final FileStorageService fileStorageService;

    /**
     * Uploads a new label PDF file, performs OCR validation, and creates a new label record.
     * This method handles versioning by deactivating previous active labels for the same SKU
     * and setting the newly uploaded label as the active one with an incremented version.
     *
     * @param sku The Stock Keeping Unit (SKU) associated with the label.
     * @param file The MultipartFile representing the PDF label to upload.
     * @return The newly created and saved Label entity.
     * @throws IOException If there is an error reading the file or during MinIO upload.
     */
    @Transactional
    public Label uploadLabel(String sku, MultipartFile file) throws IOException {
        // 1. Find the current max version for this SKU
        Integer currentVersion = labelRepository.findFirstBySkuAndDeletedFalseOrderByVersionDesc(sku)
                .map(Label::getVersion)
                .orElse(0);

        // 2. Set all existing labels for this SKU to active = false
        labelRepository.deactivateAllBySku(sku);

        // 3. Save file to MinIO
        String s3Key = "labels/" + sku + "/v" + (currentVersion + 1) + "_" + file.getOriginalFilename();
        fileStorageService.uploadFile(s3Key, file.getInputStream(), file.getSize(), file.getContentType());

        // 4. Perform Smart Validation (OCR)
        boolean skuMatched = validateSkuInPdf(sku, file.getInputStream());

        // 5. Save new Label record with version + 1 and active = true
        Label newLabel = new Label();
        newLabel.setSku(sku);
        newLabel.setVersion(currentVersion + 1);
        newLabel.setFileName(file.getOriginalFilename());
        newLabel.setS3Key(s3Key);
        newLabel.setActive(true);
        newLabel.setSkuMatched(skuMatched);
        newLabel.setCreatedAt(LocalDateTime.now());
        newLabel.setCreatedBy("Dummy User");

        return labelRepository.save(newLabel);
    }

    /**
     * Soft deletes a label by setting its 'deleted' flag to true and 'active' flag to false.
     * If the deleted label was the active version for its SKU, this method will
     * automatically activate the next most recent non-deleted version.
     *
     * @param labelId The ID of the label to be deleted.
     * @throws RuntimeException If the label with the given ID is not found.
     */
    @Transactional
    public void deleteLabel(Long labelId) {
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new RuntimeException("Label not found"));

        String sku = label.getSku();
        boolean wasActive = label.isActive();

        // Soft delete: set deleted = true, active = false
        label.setDeleted(true);
        label.setActive(false);
        labelRepository.save(label);

        // If it was the active label, make the previous version active
        if (wasActive) {
            labelRepository.findFirstBySkuAndDeletedFalseOrderByVersionDesc(sku)
                    .ifPresent(prevLabel -> {
                        prevLabel.setActive(true);
                        labelRepository.save(prevLabel);
                    });
        }
    }

    /**
     * Retrieves a list of all non-deleted labels for a given SKU, ordered by version in descending order.
     *
     * @param sku The Stock Keeping Unit (SKU) to search for.
     * @return A list of Label entities associated with the specified SKU.
     */
    public List<Label> getLabelsForProduct(String sku) {
        return labelRepository.findBySkuAndDeletedFalseOrderByVersionDesc(sku);
    }

    /**
     * Performs automated validation of the PDF content.
     * Extracts text using Apache PDFBox and checks if the product SKU is present
     * anywhere in the document text.
     *
     * @param sku The SKU to search for.
     * @param inputStream The PDF file stream.
     * @return true if the SKU is found, false otherwise.
     */
    private boolean validateSkuInPdf(String sku, InputStream inputStream) {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return text != null && text.contains(sku);
        } catch (Exception e) {
            System.err.println("Failed to perform OCR validation: " + e.getMessage());
            return false;
        }
    }
}
