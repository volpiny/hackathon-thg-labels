import { Component, OnInit } from '@angular/core';
import { LabelService } from '../../services/label.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-product-search',
  templateUrl: './product-search.component.html',
  styleUrls: ['./product-search.component.scss']
})
export class ProductSearchComponent implements OnInit {
  helloMessage: string = '';
  searchQuery: string = ''; // Consolidated query

  localProducts: any[] = [];
  catalogueProduct: any;
  productImage: string = '';
  showOnlyActive: boolean = false;

  // Loading states
  isLoadingLocal: boolean = false;
  isLoadingTitle: boolean = false;
  isLoadingBarcode: boolean = false;
  isLoadingId: boolean = false;

  // Search History
  recentSearches: string[] = [];

  // Toast state
  toastMessage: string | null = null;
  isToastVisible: boolean = false;
  isErrorToast: boolean = false;

  constructor(private labelService: LabelService, private router: Router) { }

  ngOnInit(): void {
    this.labelService.getHello().subscribe(
      res => this.helloMessage = res.message,
      err => console.error('Hello API error', err)
    );
    this.loadRecentSearches();
    this.searchBySkuLocal(); // Initial load
  }

  loadRecentSearches() {
    const saved = localStorage.getItem('recentSearches');
    if (saved) {
      this.recentSearches = JSON.parse(saved);
    }
  }

  saveRecentSearch(query: string) {
    if (!query || query.trim().length === 0) return;
    const q = query.trim();
    this.recentSearches = this.recentSearches.filter(s => s !== q);
    this.recentSearches.unshift(q);
    if (this.recentSearches.length > 5) {
      this.recentSearches.pop();
    }
    localStorage.setItem('recentSearches', JSON.stringify(this.recentSearches));
  }

  applyRecentSearch(query: string) {
    this.searchQuery = query;
    this.searchBySkuLocal();
  }

  showToast(message: string, isError: boolean = false) {
    this.toastMessage = message;
    this.isErrorToast = isError;
    this.isToastVisible = true;
    setTimeout(() => {
      this.isToastVisible = false;
    }, 4000); // 4 seconds for better visibility
  }

  /** Search local database by SKU or title query */
  searchBySkuLocal() {
    this.isLoadingLocal = true;
    this.labelService.searchLocalProducts(this.searchQuery).subscribe(
      res => {
        this.localProducts = this.showOnlyActive
          ? res.filter((p: any) => p.labels && p.labels.some((l: any) => l.active))
          : res;
        this.showToast(`Found ${this.localProducts.length} local total products`);
        this.saveRecentSearch(this.searchQuery);
        this.isLoadingLocal = false;
      },
      err => {
        console.error('Local search error', err);
        this.showToast('Local search failed: ' + (err.status || 'Error'), true);
        this.isLoadingLocal = false;
      }
    );
  }

  /** Search Product Catalogue by barcode */
  searchByBarcode() {
    if (!this.searchQuery.trim()) return;
    this.catalogueProduct = null;
    this.isLoadingBarcode = true;
    this.labelService.getProductByBarcode(this.searchQuery).subscribe(
      res => {
        if (res) {
          this.catalogueProduct = res;
          this.handleProductImage(res);
          this.showToast('Product found in Catalogue!');
          this.saveRecentSearch(this.searchQuery);
        } else {
          this.showToast('No product found for this barcode', true);
        }
        this.isLoadingBarcode = false;
      },
      err => {
        console.error('Catalogue barcode search error', err);
        const msg = err.status === 401 ? 'Catalogue access unauthorized (VPN required?)' : 'Catalogue search failed (Barcode)';
        this.showToast(msg, true);
        this.isLoadingBarcode = false;
      }
    );
  }

  /** Search Product Catalogue by product title */
  searchByTitle() {
    if (!this.searchQuery.trim()) return;
    this.catalogueProduct = null;
    this.isLoadingTitle = true;
    this.labelService.getProductByTitle(this.searchQuery).subscribe(
      res => {
        if (res) {
          this.catalogueProduct = res;
          this.handleProductImage(res);
          this.showToast('Product(s) found in Catalogue!');
          this.saveRecentSearch(this.searchQuery);
        } else {
          this.showToast('No product found with this title', true);
        }
        this.isLoadingTitle = false;
      },
      err => {
        console.error('Catalogue title search error', err);
        const msg = err.status === 401 ? 'Catalogue access unauthorized (VPN required?)' : 'Catalogue search failed (Title)';
        this.showToast(msg, true);
        this.isLoadingTitle = false;
      }
    );
  }

  /** Search Product Catalogue by Product ID (e.g. 12779183) */
  searchById() {
    if (!this.searchQuery.trim()) return;
    this.catalogueProduct = null;
    this.isLoadingId = true;
    this.labelService.getProductById(this.searchQuery).subscribe(
      res => {
        if (res) {
          this.catalogueProduct = res;
          this.handleProductImage(res);
          this.showToast('Product found in Catalogue!');
          this.saveRecentSearch(this.searchQuery);
        } else {
          this.showToast('No product found for this ID', true);
        }
        this.isLoadingId = false;
      },
      err => {
        console.error('Catalogue ID search error', err);
        const msg = err.status === 401 ? 'Catalogue access unauthorized (VPN required?)' : 'Catalogue search failed (ID)';
        this.showToast(msg, true);
        this.isLoadingId = false;
      }
    );
  }

  private handleProductImage(data: any) {
    const productId = data?.id || data?.productId;
    if (productId) {
      this.labelService.getProductImageInfo(productId).subscribe(
        imgRes => {
          // MilkyWay response is keyed by productId, then by image index (e.g. "1")
          const productData = imgRes[productId];
          const firstImage = productData ? productData["1"] : null;
          const largeProductPath = firstImage?.LARGEPRODUCT;

          if (largeProductPath) {
            this.productImage = `https://s1.thcdn.com/productimg${largeProductPath}`;
          } else {
            this.productImage = '';
          }
        },
        err => {
          console.error('Image fetch error', err);
          this.productImage = '';
        }
      );
    }
  }

  viewDetail(sku: string) {
    this.router.navigate(['/product', sku]);
  }

  addToLabelManager() {
    if (!this.catalogueProduct) return;

    const newProduct = {
      sku: String(this.catalogueProduct.id || this.catalogueProduct.productId || 'temp-sku'),
      title: this.catalogueProduct.title || this.catalogueProduct.name,
      barcode: this.catalogueProduct.barcode,
      catalogueNumber: this.catalogueProduct.catalogue,
      category: 'Supplement',
      type: 'Solid',
      marketTerritories: ['EU'],
      masterProduct: false
    };

    this.labelService.createLocalProduct(newProduct).subscribe(
      res => {
        this.showToast('Product added to Label Manager!');
        this.searchBySkuLocal();
        this.catalogueProduct = null;
      },
      err => {
        console.error('Error adding product', err);
        this.showToast('Failed to add product: ' + (err.status || 'Error'), true);
      }
    );
  }
}
