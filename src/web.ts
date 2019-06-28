import { WebPlugin } from '@capacitor/core';
import { CapBackgroundGeoPlugin } from './definitions';

export class CapBackgroundGeoWeb extends WebPlugin implements CapBackgroundGeoPlugin {
  constructor() {
    super({
      name: 'CapBackgroundGeo',
      platforms: ['web']
    });
  }

  async echo(options: { value: string }): Promise<{value: string}> {
    console.log('ECHO', options);
    return options;
  }
}

const CapBackgroundGeo = new CapBackgroundGeoWeb();

export { CapBackgroundGeo };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(CapBackgroundGeo);
