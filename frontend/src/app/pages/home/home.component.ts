import { Component } from '@angular/core';
import {DynamicGridComponent} from "../../components/shared/dynamic-grid/dynamic-grid.component";
import {Router, RouterOutlet} from "@angular/router";
import {UnsplashWallpaperComponent} from "../../connector/unsplash/unsplash-wallpaper/unsplash-wallpaper.component";
import {SettingsComponent} from "../settings/settings.component";
import {ButtonModule} from 'primeng/button';

@Component({
  selector: 'app-home',
  standalone: true,
    imports: [
        DynamicGridComponent,
        RouterOutlet,
        UnsplashWallpaperComponent,
        SettingsComponent,
        ButtonModule
    ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {
    constructor(private  router: Router) {
    }

    navigateToSettings() {
        this.router.navigate(['/settings']).then();
    }
}
