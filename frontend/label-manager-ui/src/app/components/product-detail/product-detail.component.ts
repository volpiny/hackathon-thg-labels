import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { LabelService } from '../../services/label.service';

@Component({
  selector: 'app-product-detail',
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.scss']
})
export class ProductDetailComponent implements OnInit {
  sku: string = '';
  product: any;
  masterProduct: any;
  childProducts: any[] = [];
  labels: any[] = [];
  currentPreviewUrl: string | null = null;
  productImage: string = '';
  selectedFile: File | null = null;
  availableTerritories: string[] = ['EU', 'Australia', 'India', 'USA', 'China', 'Japan'];

  // Toast UI state
  toastMessage: string | null = null;
  isToastVisible: boolean = false;
  isErrorToast: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private labelService: LabelService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.sku = params['sku'];
      this.loadProduct();
      this.loadLabels();
    });
  }

  loadProduct() {
    this.labelService.getLocalProduct(this.sku).subscribe(
      res => {
        this.product = res;

        // Attempt to fetch latest reference data from Catalogue to ensure "linking"
        this.labelService.getProductById(this.sku).subscribe(
          catRes => {
            if (catRes) {
              // Update local view with latest title/barcode from catalogue
              this.product.title = catRes.title || catRes.name || this.product.title;
              this.product.barcode = catRes.barcode || this.product.barcode;
            }
          },
          err => console.warn('Catalogue fetch failed, using local fallback', err)
        );

        this.fetchImage(res.sku);
        if (!res.masterProduct && res.masterSku) {
          this.loadMaster(res.masterSku);
        } else if (res.masterProduct) {
          this.loadChildren();
        }
      },
      err => console.error('Error loading product', err)
    );
  }

  loadMaster(masterSku: string) {
    this.labelService.getLocalProduct(masterSku).subscribe(
      res => this.masterProduct = res,
      err => console.error('Error loading master', err)
    );
  }

  loadChildren() {
    this.labelService.getChildren(this.sku).subscribe(
      res => this.childProducts = res,
      err => console.error('Error loading children', err)
    );
  }

  saveProductAttributes() {
    if (!this.product) return;
    this.labelService.createLocalProduct(this.product).subscribe(
      res => {
        this.showToast('Attributes saved successfully!');
        this.loadProduct();
      },
      err => {
        console.error('Error saving attributes', err);
        this.showToast('Failed to save attributes', true);
      }
    );
  }

  showToast(message: string, isError: boolean = false) {
    this.toastMessage = message;
    this.isErrorToast = isError;
    this.isToastVisible = true;
    setTimeout(() => {
      this.isToastVisible = false;
    }, 3000);
  }

  isTerritorySelected(t: string): boolean {
    return this.product?.marketTerritories?.includes(t) || false;
  }

  toggleTerritory(t: string) {
    if (!this.product.marketTerritories) {
      this.product.marketTerritories = [];
    }
    const index = this.product.marketTerritories.indexOf(t);
    if (index > -1) {
      this.product.marketTerritories.splice(index, 1);
    } else {
      this.product.marketTerritories.push(t);
    }
  }

  previewLabel(labelId: number) {
    this.currentPreviewUrl = `/api/labels/${labelId}/preview`;
  }

  closePreview() {
    this.currentPreviewUrl = null;
  }

  loadLabels() {
    this.labelService.getProductLabels(this.sku).subscribe(
      res => this.labels = res,
      err => console.error('Error loading labels', err)
    );
  }

  fetchImage(sku: string) {
    this.labelService.getProductImageInfo(sku).subscribe(
      imgRes => {
        const largeProductPath = imgRes?.LARGEPRODUCT;
        if (largeProductPath) {
          this.productImage = `https://s1.thcdn.com/productimg${largeProductPath}`;
        }
      }
    );
  }

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
  }

  uploadLabel() {
    if (!this.selectedFile) return;
    this.labelService.uploadLabel(this.sku, this.selectedFile).subscribe(
      res => {
        this.showToast('Label uploaded successfully!');
        this.loadLabels();
        this.selectedFile = null;
      },
      err => {
        const msg = err.error?.message || 'Error uploading label';
        this.showToast(msg, true);
      }
    );
  }

  deleteLabel(id: number) {
    if (confirm('Are you sure?')) {
      this.labelService.deleteLabel(id).subscribe(
        () => this.loadLabels(),
        err => console.error('Error deleting label', err)
      );
    }
  }

  downloadAll() {
    window.open(this.labelService.getBulkDownloadUrl(this.sku));
  }

  goBack() {
    this.router.navigate(['/']);
  }
}
