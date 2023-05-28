import * as React from 'react';

import { StyleSheet, View, Text, Button } from 'react-native';
import { printReceipt } from 'react-native-nexgo';

export default function App() {
  const [message, setMessage] = React.useState<String>('');

  return (
    <View style={styles.container}>
      {message ? <Text>Status: {message}</Text> : null}
      <Button
        title="print receipt"
        onPress={() => {
          try {
            printReceipt({
              Name: 'David Banda',
              PaymentMethod: 'VODA',
              Phone: '555-55555555',
            })
              .then(setMessage)
              .catch((err) => console.log(err));
          } catch (e) {
            console.log(e);
          }
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
