import {WebPlugin} from '@capacitor/core';
import {CapBackgroundGeoPlugin} from './definitions';

export class CapBackgroundGeoWeb extends WebPlugin implements CapBackgroundGeoPlugin {
    watchId: number = null;

    constructor() {
        super({
            name: 'CapBackgroundGeo',
            platforms: ['web']
        });
    }

    async echo(options: { value: string }): Promise<{ value: string }> {
        console.log('ECHO', options);
        return options;
    }

    async start(): Promise<void> {
        if (this.watchId === null) {
            if ('navigator' in window) {
                this.watchId = navigator.geolocation.watchPosition((pos: Position) => {
                        const {latitude, longitude} = pos.coords;
                        const coords: any = {
                            lat: latitude,
                            lng: longitude
                        };
                        super.notifyListeners('geo-update', coords);
                    },
                    err => {
                        console.error(err);
                    },
                    {
                        enableHighAccuracy: true
                    });
                return Promise.resolve();
            } else {
                return Promise.reject('navigator not found in window');
            }
        } else {
            return Promise.reject('Already watching location');
        }
    }

    async stop(): Promise<void> {
        if (this.watchId && 'navigator' in window) {
            navigator.geolocation.clearWatch(this.watchId);
            this.watchId = null;
        }
    }
}

const CapBackgroundGeo = new CapBackgroundGeoWeb();

export { CapBackgroundGeo };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(CapBackgroundGeo);
