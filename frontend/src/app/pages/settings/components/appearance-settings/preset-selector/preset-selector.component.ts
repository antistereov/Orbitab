import {Component, OnInit} from '@angular/core';
import {Preset} from './preset.enum';
import {PresetSelectorService} from './preset-selector.service';
import {Select} from 'primeng/select';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-preset-selector',
  standalone: true,
    imports: [
        Select,
        FormsModule
    ],
  templateUrl: './preset-selector.component.html',
  styleUrl: './preset-selector.component.css'
})
export class PresetSelectorComponent implements OnInit {
    presetOptions = [
        { label: 'Aura', value: Preset.Aura },
        { label: 'Lara', value: Preset.Lara },
        { label: 'Nora', value: Preset.Nora }
    ];
    selectedPreset: Preset = Preset.Aura;

    constructor(private presetSelectorService: PresetSelectorService) {}

    ngOnInit() {
        this.presetSelectorService.preset$.subscribe((preset) => (this.selectedPreset = preset));
    }

    onPresetChange(event: any) {
        this.presetSelectorService.setPreset(event.value as Preset);
    }

}
