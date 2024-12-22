import {HttpErrorResponse, HttpInterceptorFn} from '@angular/common/http';
import {catchError, switchMap, throwError} from 'rxjs';
import {AuthService} from '../auth/auth.service';
import {inject} from '@angular/core';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);

    const authReq = req.clone({
        withCredentials: true
    });

    return next(authReq).pipe(
        catchError((error: HttpErrorResponse) => {
            if (error.status === 401 && !req.url.includes('/refresh')) {
                return authService.refreshToken().pipe(
                    switchMap(() => {
                        const refreshedReq = req.clone({
                            withCredentials: true
                        });
                        return next(refreshedReq);
                    }),
                    catchError((refreshError) => {
                        authService.logout().then();
                        return authService.loginOrRegisterGuest().pipe(
                            switchMap(() => {
                                const refreshedReq = req.clone({
                                    withCredentials: true
                                });
                                return next(refreshedReq);
                            })
                        );
                    })
                );
            }

            return throwError(() => error);
        })
    );
}
