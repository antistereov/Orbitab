import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {BehaviorSubject, firstValueFrom, Observable, of} from "rxjs";
import {environment} from '../../environment/environment';
import {DeviceService} from '../services/device.service';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private authInfoSubject = new BehaviorSubject<AuthInfo | undefined>(undefined);
    authInfo$ = this.authInfoSubject.asObservable();
    private baseUrl = environment.baseUrl;


    constructor(
        private httpClient: HttpClient,
        private deviceService: DeviceService
    ) {}

    async initializeAuthInfo(): Promise<void> {
        await this.fetchAuthInfo();
    }

    loginOrRegisterGuest(): Observable<any> {
        const authInfo = this.authInfoSubject.value;
        if (authInfo) {
            return this.httpClient.post<any>(`${this.baseUrl}/guest/login`, this.deviceService.getDeviceInfo())
        } else {
            return of(undefined)
        }
    }

    loginUser(data: any): Observable<void> {
        const payload = {
            ...data,
            device: this.deviceService.getDeviceInfo()
        };

        return this.httpClient.post<any>(`${this.baseUrl}/user/login`, payload);
    }

    async logout(): Promise<void> {
        const authInfo = await this.getAuthInfo();
        if (authInfo) {
            const accountType = authInfo.account_type
            switch (accountType) {
                case "GUEST" :
                    return await firstValueFrom(
                        this.httpClient.post<any>(`${this.baseUrl}/guest/logout`, this.deviceService.getDeviceInfo())
                    )
                case "REGISTERED":
                    return await firstValueFrom(
                        this.httpClient.post<any>(`${this.baseUrl}/user/logout`, this.deviceService.getDeviceInfo())
                    )
            }
        }
    }

    async getAuthInfo(): Promise<AuthInfo | undefined> {
        return this.authInfoSubject.value;
    }

    private async fetchAuthInfo(): Promise<AuthInfo> {
        const authInfo = await firstValueFrom(this.httpClient.get<AuthInfo>(`${this.baseUrl}/account/check`));
        this.authInfoSubject.next(authInfo);
        return authInfo;
    }

    refreshToken(): Observable<any> {
        const authInfo = this.authInfoSubject.value;
        if (authInfo) {
            const accountType = authInfo.account_type;
            switch (accountType) {
                case "GUEST":
                    return this.httpClient.post<any>(`${this.baseUrl}/guest/refresh`, this.deviceService.getDeviceInfo())
                case "REGISTERED":
                    return this.httpClient.post<any>(`${this.baseUrl}/user/refresh`, this.deviceService.getDeviceInfo())
            }
        } else {
            return of(undefined);
        }
    }
}

export interface AuthInfo {
    account_id: string;
    account_type: "GUEST" | "REGISTERED";
    authenticated: boolean;
}
