import {Component, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {Router} from '@angular/router';
import {LoginFormComponent} from './login-form/login-form.component';
import {SettingsComponent} from '../settings/settings.component';
import {AppearanceSettingsComponent} from '../settings/components/appearance-settings/appearance-settings.component';

@Component({
  selector: 'app-login',
  standalone: true,
    imports: [ReactiveFormsModule, RouterModule, LoginFormComponent, SettingsComponent, AppearanceSettingsComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
    router = inject(Router);
}
