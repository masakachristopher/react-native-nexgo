# react-native-nexgo

Support hardware interaction with smartpos devices.
## Installation

```sh
npm install react-native-nexgo
```

## Usage

```js
import { printReceipt } from "react-native-nexgo";

// ...

const result = await printReceipt({
              Name: 'David Banda',
              PaymentMethod: 'VODA',
              Phone: '555-55555555',
              Amount: '1.00'
            })

```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

## Future work
- MSR - read swiped card and display card information
- EMV - read card for payment processing, including using PIN entry
- Camera Scanner - read barcode

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
