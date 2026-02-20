package com.example.thg_label_management.controller;

import com.example.thg_label_management.model.Label;
import com.example.thg_label_management.model.Product;
import com.example.thg_label_management.service.LabelService;
import com.example.thg_label_management.service.ProductService;
import com.example.thg_label_management.service.FileStorageService;
import com.example.thg_label_management.repository.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Primary controller for Product and Label lifecycle management.
 * Handles product discovery, label uploads (with OCR), versioning, and bulk operations.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LabelManagementController {

    private final ProductService productService;
    private final LabelService labelService;
    private final FileStorageService fileStorageService;
    private final LabelRepository labelRepository;

    @GetMapping("/products/search")
    public List<Product> searchProducts(@RequestParam String query) {
        return productService.searchProducts(query);
    }

    @PostMapping("/products")
    public Product createProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }

    @GetMapping("/products/{sku}")
    public ResponseEntity<Product> getProduct(@PathVariable String sku) {
        return productService.getProduct(sku)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/products/{sku}/labels")
    public List<Label> getLabels(@PathVariable String sku) {
        return labelService.getLabelsForProduct(sku);
    }

    /**
     * Uploads a new PDF label for a specific product.
     * Triggers automated OCR validation to verify SKU matching within the document.
     * Increments version number and sets the new label as ACTIVE.
     *
     * @param sku The product SKU.
     * @param file The PDF label file.
     * @return The persists Label entity.
     * @throws IOException If file processing fails.
     */
    @PostMapping("/products/{sku}/labels")
    public Label uploadLabel(@PathVariable String sku, @RequestParam("file") MultipartFile file) throws IOException {
        Product product = productService.getProduct(sku)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (product.isMasterProduct()) {
            throw new RuntimeException("Labels can only be uploaded to child products");
        }
        
        return labelService.uploadLabel(sku, file);
    }

    @GetMapping("/products/{sku}/children")
    public List<Product> getChildProducts(@PathVariable String sku) {
        return productService.getChildProducts(sku);
    }

    @DeleteMapping("/labels/{id}")
    public ResponseEntity<Void> deleteLabel(@PathVariable Long id) {
        labelService.deleteLabel(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/products/{sku}/labels/bulk-download", produces = "application/zip")
    public ResponseEntity<byte[]> bulkDownload(@PathVariable String sku) throws IOException {
        List<Label> labels = labelService.getLabelsForProduct(sku);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Label label : labels) {
                byte[] content = fileStorageService.downloadFile(label.getS3Key());
                ZipEntry entry = new ZipEntry(label.getVersion() + "_" + label.getFileName());
                zos.putNextEntry(entry);
                zos.write(content);
                zos.closeEntry();
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename("labels_" + sku + ".zip").build());
        return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
    }

    /**
     * Streams a label PDF directly to the browser for in-app preview.
     * Uses ByteArrayResource to serve content from MinIO with inline disposition.
     *
     * @param labelId The unique ID of the label.
     * @return ResponseEntity containing the PDF stream.
     * @throws IOException If retrieval fails.
     */
    @GetMapping("/labels/{labelId}/preview")
    public ResponseEntity<Resource> previewLabel(@PathVariable Long labelId) throws IOException {
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new RuntimeException("Label not found"));

        byte[] content = fileStorageService.downloadFile(label.getS3Key());
        // If the stored file is not a valid PDF (e.g., dummy placeholder), generate a simple PDF on the fly
        if (!isPdf(content)) {
            content = generatePlaceholderPdf(label.getFileName());
        }
        ByteArrayResource resource = new ByteArrayResource(content);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + label.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    /**
     * Very lightweight check to see if the byte array starts with the PDF magic number "%PDF-".
     */
    private boolean isPdf(byte[] data) {
        if (data == null || data.length < 5) return false;
        String header = new String(data, 0, Math.min(5, data.length));
        return header.startsWith("%PDF-");
    }

    /**
     * Generates a minimal PDF containing the label file name as title.
     */
    private byte[] generatePlaceholderPdf(String title) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 20);
                cs.newLineAtOffset(100, 700);
                cs.showText("Label Preview: " + title);
                cs.endText();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    @GetMapping("/hello")
    public ResponseEntity<?> hello() {
        return ResponseEntity.ok().body("{\"message\": \"Hello World\"}");
    }
}
