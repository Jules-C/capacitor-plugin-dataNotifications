import { WebPlugin } from '@capacitor/core';
import { DataNotificationPlugin } from './definitions';

export class DataNotificationWeb extends WebPlugin implements DataNotificationPlugin {
  constructor() {
    super({
      name: 'DataNotification',
      platforms: ['web'],
    });
  }

  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}

const DataNotification = new DataNotificationWeb();

export { DataNotification };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(DataNotification);
