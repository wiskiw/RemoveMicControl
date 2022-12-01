//
//  BluetoothMicControlApp.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 30.11.22.
//

import SwiftUI

@main
struct BluetoothMicControlApp: App {
    
    @State var currentNumber: String = "1"

    var body: some Scene {
            WindowGroup {
                ContentView(vm:MicControlViewModel())
            }
//            MenuBarExtra(currentNumber, systemImage: "\(currentNumber).circle") {
//
//                Button("One") {
//                    currentNumber = "1"
//                }
//                Button("Two") {
//                    currentNumber = "2"
//                }
//                Button("Three") {
//                    currentNumber = "3"
//                }
//            }
        }
    
}
