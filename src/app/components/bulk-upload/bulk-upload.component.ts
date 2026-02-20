import { Component } from '@angular/core';
import { LabelService } from '../../services/label.service';

@Component({
    selector: 'app-bulk-upload',
    templateUrl: './bulk-upload.component.html',
    styleUrls: ['./bulk-upload.component.scss']
})
export class BulkUploadComponent {
    pendingFiles: any[] = [];
    uploading = false;

    constructor(private labelService: LabelService) { }

    onFilesSelected(event: any) {
        const files: FileList = event.target.files;
        this.pendingFiles = [];

        for (let i = 0; i < files.length; i++) {
            const file = files[i];
            const extractedSku = this.extractSku(file.name);
            this.pendingFiles.push({
                file: file,
                sku: extractedSku,
                status: 'Pending'
            });
        }
    }

    private extractSku(filename: string): string {
        // Basic logic: Split by _ or - and take the first part
        // Or just look for known patterns. In a hackathon, we assume simple SKU-first naming
        return filename.split(/[_-]/)[0].toUpperCase();
    }

    uploadAll() {
        this.uploading = true;
        let completed = 0;

        this.pendingFiles.forEach(pf => {
            pf.status = 'Uploading...';
            this.labelService.uploadLabel(pf.sku, pf.file).subscribe(
                res => {
                    pf.status = 'Success ✅';
                    completed++;
                    if (completed === this.pendingFiles.length) this.uploading = false;
                },
                err => {
                    pf.status = 'Error ❌ (SKU Not Found?)';
                    completed++;
                    if (completed === this.pendingFiles.length) this.uploading = false;
                }
            );
        });
    }
}
