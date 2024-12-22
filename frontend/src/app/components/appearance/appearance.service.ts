import { Injectable } from '@angular/core';
import {ThemeSelectorService} from '../../pages/settings/components/appearance-settings/theme-selector/theme-selector.service';
import {AccentColorSelectorService} from '../../pages/settings/components/appearance-settings/accent-color-selector/accent-color-selector.service';
import {PresetSelectorService} from '../../pages/settings/components/appearance-settings/preset-selector/preset-selector.service';

@Injectable({
    providedIn: 'root'
})
export class AppearanceService {
    private initialized = false;

    constructor(
        private accentColorService: AccentColorSelectorService,
        private presetSelector: PresetSelectorService,
        private themeSelectorService: ThemeSelectorService,
    ) {}

    initialize() {
        if (!this.initialized) {
            setTimeout(() => {
                this.accentColorService.initialize();
                this.presetSelector.initialize();
                this.themeSelectorService.initialize();
                this.initialized = true;
            }, 0);
        }
    }
}
