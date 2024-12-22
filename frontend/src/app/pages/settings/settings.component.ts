import {Component} from '@angular/core';
import {ButtonModule} from 'primeng/button';
import {SidebarModule} from 'primeng/sidebar';
import {ThemeSelectorComponent} from './components/appearance-settings/theme-selector/theme-selector.component';
import {AccentColorSelectorComponent} from './components/appearance-settings/accent-color-selector/accent-color-selector.component';
import {NgIf} from '@angular/common';
import {AvatarModule} from 'primeng/avatar';
import {UserSettingsComponent} from './components/user-settings/user-settings.component';
import {DividerModule} from 'primeng/divider';
import {AppearanceSettingsComponent} from './components/appearance-settings/appearance-settings.component';
import {UnsplashAuthComponent} from '../../connector/unsplash/unsplash-auth/unsplash-auth.component';
import {Drawer} from 'primeng/drawer';
import {SpotifyAuthComponent} from '../../connector/spotify/spotify-auth/spotify-auth.component';
import {CardModule} from 'primeng/card';
import {UnsplashWallpaperComponent} from '../../connector/unsplash/unsplash-wallpaper/unsplash-wallpaper.component';
import {Router} from '@angular/router';

@Component({
  selector: 'app-settings',
  standalone: true,
    imports: [
        ButtonModule,
        SidebarModule,
        ThemeSelectorComponent,
        AccentColorSelectorComponent,
        NgIf,
        AvatarModule,
        UserSettingsComponent,
        DividerModule,
        AppearanceSettingsComponent,
        UnsplashAuthComponent,
        Drawer,
        SpotifyAuthComponent,
        CardModule,
        UnsplashWallpaperComponent
    ],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.css'
})
export class SettingsComponent {
    constructor(private router: Router) {
    }

    navigateToHome() {
        this.router.navigate(['./']).then();
    }


}
