import {Component, OnInit} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {PrimeNGConfig} from 'primeng/api';
import {ButtonModule} from 'primeng/button';
import {ToggleButton} from 'primeng/togglebutton'
import {ThemeSelectorComponent} from './pages/settings/components/appearance-settings/theme-selector/theme-selector.component';
import {AccentColorSelectorComponent} from './pages/settings/components/appearance-settings/accent-color-selector/accent-color-selector.component';
import {SettingsComponent} from './pages/settings/settings.component';
import {Aura} from 'primeng/themes/aura';
import {UnsplashWallpaperComponent} from './connector/unsplash/unsplash-wallpaper/unsplash-wallpaper.component';
import {DynamicGridComponent} from './components/shared/dynamic-grid/dynamic-grid.component';
import {SpotifyPlaybackComponent} from './connector/spotify/spotify-playback/spotify-playback.component';
import {WallpaperComponent} from './components/shared/wallpaper/wallpaper.component';
import {TranslateService} from '@ngx-translate/core';
import {AppearanceComponent} from './components/appearance/appearance.component';
import {AuthService} from './auth/auth.service';

@Component({
    selector: 'app-root',
    standalone: true,
    imports: [
        RouterOutlet,
        ButtonModule,
        ToggleButton,
        ThemeSelectorComponent,
        AccentColorSelectorComponent,
        SettingsComponent,
        UnsplashWallpaperComponent,
        DynamicGridComponent,
        SpotifyPlaybackComponent,
        WallpaperComponent,
        AppearanceComponent
    ],
    templateUrl: './app.component.html',
    styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
    title = 'frontend';

    constructor(
        private primeConfig: PrimeNGConfig,
        private translate: TranslateService,
        private authService: AuthService,
    ) {
        this.primeConfig.theme.set({
            options: {
                preset: Aura,
                prefix: 'p',
                darkModeSelector: '.dark-mode',
                cssLayer: {
                    name: 'primeng',
                    order: 'tailwind-base, primeng, tailwind-utilities'
                },
            }
        });

        this.translate.setDefaultLang('en');

        this.authService.initializeAuthInfo().then();
    }

    ngOnInit() {
        this.primeConfig.ripple.set(true);
    }

    switchLanguage(lang: string) {
        this.translate.use(lang);
    }
}

