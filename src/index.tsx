import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-nexgo' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const Nexgo = NativeModules.Nexgo  ? NativeModules.Nexgo  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function multiply(a: number, b: number): Promise<number> {
  return Nexgo.multiply(a, b);
}

export function printReceipt(
  coverName: string,
  insuredName: string,
  paymentMethod: string,
  paymentAccount: string,
  amount: string
):Promise<any> {
  return Nexgo.printReceipt(coverName,insuredName, paymentMethod, paymentAccount, amount);
}
