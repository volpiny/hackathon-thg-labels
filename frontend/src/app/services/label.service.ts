import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LabelService {

  constructor(private http: HttpClient) { }

  private getCatalogueHeaders() {
    return {
      'Authorization': 'Bearer dummy-token-for-catalogue', // User suggested dummy user
      'X-Organisation': 'default'
    };
  }

  getHello(): Observable<any> {
    return this.http.get('/api/hello');
  }

  // Catalogue Service (via proxy)
  getProductByTitle(title: string): Observable<any> {
    return this.http.get(`/CatalogueService/product/title/${title}?organisation=default`, { headers: this.getCatalogueHeaders() });
  }

  getProductByBarcode(barcode: string): Observable<any> {
    return this.http.get(`/CatalogueService/product/barcode/${barcode}?organisation=default`, { headers: this.getCatalogueHeaders() });
  }

  getCatalogueByBarcode(barcode: string): Observable<any> {
    return this.http.get(`/CatalogueService/catalogue/${barcode}?organisation=default`, { headers: this.getCatalogueHeaders() });
  }

  getProductById(productId: string): Observable<any> {
    return this.http.get(`/CatalogueService/product/${productId}?organisation=default`, { headers: this.getCatalogueHeaders() });
  }

  // MilkyWay Service (via proxy)
  getProductImageInfo(productId: string): Observable<any> {
    return this.http.get(`/MilkyWay/imagesbyproduct/productid/${productId}`, { headers: this.getCatalogueHeaders() });
  }

  // Local Backend API
  searchLocalProducts(query: string): Observable<any[]> {
    return this.http.get<any[]>(`/api/products/search?query=${query}`);
  }

  getLocalProduct(sku: string): Observable<any> {
    return this.http.get<any>(`/api/products/${sku}`);
  }

  getChildren(sku: string): Observable<any[]> {
    return this.http.get<any[]>(`/api/products/${sku}/children`);
  }

  createLocalProduct(product: any): Observable<any> {
    return this.http.post<any>('/api/products', product);
  }

  getProductLabels(sku: string): Observable<any[]> {
    return this.http.get<any[]>(`/api/products/${sku}/labels`);
  }

  uploadLabel(sku: string, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<any>(`/api/products/${sku}/labels`, formData);
  }

  deleteLabel(id: number): Observable<any> {
    return this.http.delete<any>(`/api/labels/${id}`);
  }

  getBulkDownloadUrl(sku: string): string {
    return `/api/products/${sku}/labels/bulk-download`;
  }

  getDashboardStats(): Observable<any> {
    return this.http.get('/api/dashboard/stats');
  }
}
