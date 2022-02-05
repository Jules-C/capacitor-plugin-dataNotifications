import { registerPlugin } from '@capacitor/core';

import type { DataNotificationPlugin } from './definitions';
const DataNotification = registerPlugin<DataNotificationPlugin>('DataNotification');
export * from './definitions';
export {DataNotification}
