import {Component, OnInit} from '@angular/core';
import {AppearanceService} from './appearance.service';

@Component({
  selector: 'app-appearance',
  standalone: true,
  templateUrl: './appearance.component.html',
  styleUrl: './appearance.component.css'
})
export class AppearanceComponent implements OnInit {
    constructor(private appearanceService: AppearanceService) {}

    ngOnInit() {
        this.appearanceService.initialize();
    }
}
