declare module "@capacitor/core" {
  interface PluginRegistry {
    CapBackgroundGeo: CapBackgroundGeoPlugin;
  }
}

export interface CapBackgroundGeoPlugin {
  echo(options: { value: string }): Promise<{value: string}>;
  start(): Promise<void>;
  stop(): Promise<void>;
}
