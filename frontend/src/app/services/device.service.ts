import { Injectable } from '@angular/core';
import platform from 'platform';

@Injectable({
  providedIn: 'root'
})
export class DeviceService {

    getDeviceInfo(): DeviceInfo {
        let deviceId = localStorage.getItem('device_id');
        if (!deviceId) {
            deviceId = 'device-' + Math.random().toString(36).substring(2) + Date.now().toString(36);
            localStorage.setItem('device_id', deviceId);
        }
        const browser = platform.name
        const os = platform.os?.family
        return { deviceId, browser, os };
    }
}

export interface DeviceInfo {
    deviceId: string;
    browser: string | undefined;
    os: string | undefined;
}
