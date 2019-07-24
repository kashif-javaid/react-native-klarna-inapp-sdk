/**
 * Sample React Native App
 *
 * adapted from App.js generated by the following command:
 *
 * react-native init example
 *
 * https://github.com/facebook/react-native
 */

import React, { Component } from 'react';
import { Platform, StyleSheet, Text, View, Button, ScrollView } from 'react-native';
import KlarnaPaymentView from 'react-native-klarna-payment-view';



export default class App extends Component<{}> {

  actionButtons = (paymentMethod) => {
    return (
      <View style={styles.buttonsContainer}>
        <Button
          onPress={() => {
            this.refs[paymentMethod].initialize(authToken, 'returnUrl://')
          }}
          title="Init."
          style={styles.button} />
        <Button
          onPress={() => {
            this.refs[paymentMethod].load()
          }}
          title="Load"
          style={styles.button} />
        <Button
          onPress={() => {
            this.refs[paymentMethod].authorize()
          }}
          title="Authorize"
          style={styles.button} />
      </View>
    )
  }

  onEvent = (event) => {
    window.console.warn(JSON.stringify(event.nativeEvent))
  }

  render = () => {
    return (
      <View style={styles.outer}>
      <ScrollView vertical style={styles.scrollView} contentContainerStyle={styles.scrollViewContentContainer}>
        <Text style={styles.header}>☆Klarna Payments Test App</Text>
        {paymentMethods.map(paymentMethod => {
          return (
            <View style={styles.container} key={paymentMethod}>
              <Text style={styles.title}>{paymentMethod}</Text>
              <KlarnaPaymentView
                category={paymentMethod}
                ref={paymentMethod}
                style={styles.paymentView}
                onEvent={this.onEvent} />
              {this.actionButtons(paymentMethod)}
            </View>
          )
        })}
      </ScrollView>
      </View>
    );
  }
}

const authToken = 'eyJhbGciOiJSUzI1NiJ9.ewogICJzZXNzaW9uX2lkIiA6ICIwOWExMzA5OC0wMGFmLTc0M2UtYTc2My0zMDI3ZWIxMTYyYzgiLAogICJiYXNlX3VybCIgOiAiaHR0cHM6Ly9rbGFybmEtcGF5bWVudHMtZXUucGxheWdyb3VuZC5rbGFybmEuY29tIiwKICAiZGVzaWduIiA6ICJrbGFybmEiLAogICJsYW5ndWFnZSIgOiAic3YiLAogICJwdXJjaGFzZV9jb3VudHJ5IiA6ICJTRSIsCiAgInRyYWNlX2Zsb3ciIDogZmFsc2UsCiAgImVudmlyb25tZW50IiA6ICJwbGF5Z3JvdW5kIiwKICAibWVyY2hhbnRfbmFtZSIgOiAiUGxheWdyb3VuZCBEZW1vIE1lcmNoYW50IiwKICAic2Vzc2lvbl90eXBlIiA6ICJQQVlNRU5UUyIsCiAgImNsaWVudF9ldmVudF9iYXNlX3VybCIgOiAiaHR0cHM6Ly9ldnQucGxheWdyb3VuZC5rbGFybmEuY29tIiwKICAiZXhwZXJpbWVudHMiIDogWyBdCn0.Vf9j13i75k1TNmbibUJzffRH88DgoSHhYKfmlF6byXrZFxEWnWKLgv6s2tPPXhBbtvVVgn7ko1q9RWa6wbHRfCjmruza8iO6Eoma7n-2pbvtn4ZAgJUCGZUwen5uTB9rlMKk9zdW7hvogHDX2D7yIeK7duTmjyxV6SKDITbDWz8UP2Sg8QT5MRW1qglb4Aor7UymJGiofPU8apo9BnaeGIlQw1t3Okk9739EokmQkXZl9OLD3W50qBr4ucHr2-xzQkZEPFPMqaTMFLwOPYBX6_pH-FSlx7PhtyraMvsou1FRLh2BWO5zrm_hUAE6tRpvSvnkcsae6-evhE7IKGmo2Q';

const paymentMethods = ['pay_now', 'pay_later', 'slice_it'];

const styles = StyleSheet.create({
  outer: {
    flex: 1,
    flexGrow: 1
  },
  scrollView: {
    flex: 1,
    flexGrow: 1
    },
  scrollViewContentContainer: {
    // flex: 1,
    // flexGrow: 1,
    justifyContent: 'space-between'
  },

  container: {
    // flex: 1,
    // flexGrow: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
    width: "100%"
  },
  header: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  paymentView: {
    // flex: 1,
    width: "100%",
    flexGrow: 1,
    height: 400
  },
  title: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
  buttonsContainer: {
    flexDirection: "row",
    justifyContent: "space-around",
    alignItems: "center",
    margin: 10
  },
  button: {
    height: 10
  },
});
