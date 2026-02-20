import { Component, OnInit } from '@angular/core';
import { LabelService } from '../../services/label.service';

@Component({
    selector: 'app-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
    stats: any = null;

    constructor(private labelService: LabelService) { }

    ngOnInit(): void {
        this.labelService.getDashboardStats().subscribe(
            res => this.stats = res,
            err => console.error('Error fetching dashboard stats', err)
        );
    }
}
