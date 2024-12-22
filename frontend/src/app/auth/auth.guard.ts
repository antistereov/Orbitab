import { CanActivateFn } from '@angular/router';
import {inject} from '@angular/core';
import {AuthService} from './auth.service';
import {Router} from '@angular/router';
import {map} from 'rxjs';

export const authGuard: CanActivateFn = (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    return authService.authInfo$.pipe(
        map(authInfo => {
            if (authInfo) {
                if (!authInfo.authenticated) {
                    authService.loginOrRegisterGuest().subscribe(() => {
                        return true;
                    });
                }
                return true;
            }
            return false;
        })
    )
};
