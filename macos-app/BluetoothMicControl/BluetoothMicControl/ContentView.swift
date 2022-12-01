//
//  ContentView.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 30.11.22.
//

import SwiftUI
import Cocoa
import CoreAudio
import AudioToolbox

struct ContentView: View {
    
    
    @StateObject private var vm : MicControlViewModel

    init(vm : MicControlViewModel){
        self._vm = StateObject(wrappedValue: vm)
    }
    

    var body: some View {
        VStack(alignment: .center) {
            Text("Some text here")
                .padding()
            Spacer()
            Text(vm.debugMessage)
                .padding()
                .task {
                    vm.populateUi()
                }
        }.frame(width: 300, height: 300)
    }

}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView(vm : MicControlViewModel())
    }
}


