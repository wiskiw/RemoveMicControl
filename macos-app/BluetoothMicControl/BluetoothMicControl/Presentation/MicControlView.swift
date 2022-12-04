//
//  ContentView.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 30.11.22.
//

import SwiftUI

struct MicControlView: View {
    
    
    @StateObject private var vm : MicControlViewModel
    
    init(vm : MicControlViewModel){
        self._vm = StateObject(wrappedValue: vm)
    }
    
    
    var body: some View {
        VStack(alignment: .center) {
            Text(vm.micState == .muted ? "Mic is MUTED" : "Mic is ACTIVE")
                .padding()
                      
            // To close any other statusbar windows if this app was opened
            // idk why it's works :/
            let emptyList : [Int] = []
            List(emptyList, id: \.self) { _ in}.fixedSize()
        }
    }
    
}
