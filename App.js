/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */

import React,{useState,useEffect} from 'react';
import {
  SafeAreaView,
  StyleSheet,
  ScrollView,
  View,
  Text,
  StatusBar,
  TouchableOpacity,
  NativeModules,
  Image
} from 'react-native';

import {
  Header,
  LearnMoreLinks,
  Colors,
  DebugInstructions,
  ReloadInstructions,
} from 'react-native/Libraries/NewAppScreen';

const App = () => {

  const [appIcon,setAppIcon]=useState([])


  useEffect(()=>{
    NativeModules.ToastExample.loadAppUsage(err=>{
      
   
    },data=>{
      setAppIcon(data, "<<<----")
      console.log(data)
    })
  },[])

  return (
    <>
      <StatusBar barStyle="dark-content" />
      <SafeAreaView>
      <Text>Phone Data</Text>
         <ScrollView >
           {
             appIcon.length > 0 && appIcon.map(item=>(
                <View style={{flexDirection : "row" , alignItems  :"center" , justifyContent :"center"}}>
                   <Image  style={{width : 25,height:25}} source={{uri : item.packageName}} />
                   <Text>{item.appName}</Text>
                   <Text>{item.usageDuration}</Text>

                </View>

             ))
           }
         </ScrollView>
         
        
      </SafeAreaView>
    </>
  );
};

const styles = StyleSheet.create({
  scrollView: {
    backgroundColor: Colors.lighter,
  },
  engine: {
    position: 'absolute',
    right: 0,
  },
  body: {
    backgroundColor: Colors.white,
  },
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
    color: Colors.black,
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
    color: Colors.dark,
  },
  highlight: {
    fontWeight: '700',
  },
  footer: {
    color: Colors.dark,
    fontSize: 12,
    fontWeight: '600',
    padding: 4,
    paddingRight: 12,
    textAlign: 'right',
  },
});

export default App;
